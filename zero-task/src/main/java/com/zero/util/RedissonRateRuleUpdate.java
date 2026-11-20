package com.zero.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.redisson.Redisson;
import org.redisson.RedissonObject;
import org.redisson.RedissonRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisException;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.convertor.VoidReplayConvertor;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.springframework.util.Assert;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 01:26
 * 说明: 通过 Redisson Java 客户端更新共享限流规则，兼容现有规则键和已消耗的许可记录。
 */
final class RedissonRateRuleUpdate
{
    /** 同一个限流键发生并发更新或许可变更时的重试上限。 */
    private static final int MAX_ATTEMPTS = 32;

    /** 事务排队时只接收状态应答，实际删除数由 EXEC 返回。 */
    private static final RedisCommand<Void> TRIM_PERMITS =
            new RedisCommand<>(RedisCommands.ZREMRANGEBYSCORE.getName(), new VoidReplayConvertor());

    /** 事务排队时只接收状态应答，继承规则键的到期时间。 */
    private static final RedisCommand<Void> EXPIRE_AT =
            new RedisCommand<>(RedisCommands.PEXPIREAT.getName(), new VoidReplayConvertor());

    /** 服务端时间的秒和微秒均按整数读取，用于计算子键的绝对过期时间。 */
    private static final RedisCommand<List<Object>> SERVER_TIME =
            new RedisCommand<>(RedisCommands.TIME.getName(), new ObjectListReplayDecoder<>());

    private RedissonRateRuleUpdate()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:26
     * 说明: 从规则所在的主节点取得独占连接，让 WATCH 和事务使用同一连接。
     * @param client 当前模块的 Redisson 客户端
     * @param key 共享限流逻辑键
     * @param rate 新许可数量
     * @param interval 新窗口毫秒数
     * @return 是否更新了规则
     * 返回值: boolean
     * 返回值说明: 规则缺失或配额和窗口均未变化时返回 false
     */
    static boolean update(RedissonClient client, String key, long rate, long interval)
    {
        RedissonRateLimiter limiter = (RedissonRateLimiter) client.getRateLimiter(key);
        String ruleKey = limiter.getRawName();
        String balanceKey = RedissonObject.suffixName(ruleKey, "value");
        String permitsKey = RedissonObject.suffixName(ruleKey, "permits");
        var manager = ((Redisson) client).getCommandExecutor().getConnectionManager();
        var node = manager.getEntry(ruleKey);
        Assert.state(node != null, "无法找到限流规则所在的 Redis 主节点");
        RedisConnection connection = node.getClient().connect();
        // 事务依赖原连接的 WATCH 状态，断线时关闭连接，避免重连后在未监听的连接上写入。
        connection.setDisconnectedListener(connection::closeAsync);
        try
        {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
            {
                connection.sync(StringCodec.INSTANCE, RedisCommands.WATCH, ruleKey, balanceKey, permitsKey);
                List<Object> rule = connection.sync(StringCodec.INSTANCE, RedisCommands.HMGET,
                        ruleKey, "type", "rate", "interval");
                if (rule.get(0) == null)
                {
                    return false;
                }
                Assert.state(Integer.toString(RateType.OVERALL.ordinal()).equals(rule.get(0)), "只能更新共享限流规则");
                if (Long.toString(rate).equals(rule.get(1)) && Long.toString(interval).equals(rule.get(2)))
                {
                    return false;
                }

                long cutoff = System.currentTimeMillis() - interval;
                List<Object> entries = connection.sync(ByteArrayCodec.INSTANCE, RedisCommands.ZRANGEBYSCORE_LIST,
                        permitsKey, "(" + cutoff, "+inf");
                long used = entries.stream().mapToLong(RedissonRateRuleUpdate::permitCount).sum();
                long expiresAt = expiryTime(connection, ruleKey);
                if (expiresAt == -2)
                {
                    return false;
                }

                connection.sync(RedisCommands.MULTI);
                connection.sync(StringCodec.INSTANCE, RedisCommands.HMSET, ruleKey, "rate", rate, "interval", interval);
                connection.sync(StringCodec.INSTANCE, TRIM_PERMITS, permitsKey, "-inf", cutoff);
                // 降额后的负余额必须保留，已有许可逐批到期后才逐步抵消。
                connection.sync(StringCodec.INSTANCE, RedisCommands.SET, balanceKey, rate - used);
                if (expiresAt >= 0)
                {
                    connection.sync(StringCodec.INSTANCE, EXPIRE_AT, balanceKey, expiresAt);
                    connection.sync(StringCodec.INSTANCE, EXPIRE_AT, permitsKey, expiresAt);
                }
                List<Object> result = connection.sync(RedisCommands.EXEC);
                if (result != null && !result.isEmpty())
                {
                    return true;
                }
            }
            throw new IllegalStateException("限流规则并发更新冲突次数过多");
        }
        finally
        {
            // 连接只用于本次更新，关闭后也会清理未提交的事务或监听。
            connection.close();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:40
     * 说明: 沿用规则的绝对到期时间，避免余额先于规则到期导致许可提前恢复。
     * @param connection 本次更新使用的主节点连接
     * @param ruleKey 限流规则的 Redis 键
     * @return Redis 记录的到期时间，单位为毫秒
     * 返回值: long
     * 返回值说明: -1 表示永久有效，-2 表示规则已经不存在
     */
    private static long expiryTime(RedisConnection connection, String ruleKey)
    {
        try
        {
            return connection.sync(StringCodec.INSTANCE, RedisCommands.PEXPIRETIME, ruleKey);
        }
        catch (RedisException failure)
        {
            // 兼容 Redis 7 之前的节点。先读剩余时间，再读服务端时钟，避免子键提前过期。
            if (failure.getMessage() == null || !failure.getMessage().contains("unknown command"))
            {
                throw failure;
            }
            long ttl = connection.sync(StringCodec.INSTANCE, RedisCommands.PTTL, ruleKey);
            if (ttl < 0)
            {
                return ttl;
            }
            List<Object> time = connection.sync(LongCodec.INSTANCE, SERVER_TIME);
            return (Long) time.get(0) * 1000L + (Long) time.get(1) / 1000 + ttl;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:26
     * 说明: 读取 Redisson 4.6.1 许可记录中紧随随机编号的无符号许可数。
     * @param entry Redis 有序集合中的二进制许可记录
     * @return 该条记录已消耗的许可数
     * 返回值: long
     * 返回值说明: 沿用当前部署 Redis 使用的原生字节序及现有二进制布局
     */
    private static long permitCount(Object entry)
    {
        byte[] bytes = (byte[]) entry;
        int offset = 1 + Byte.toUnsignedInt(bytes[0]);
        return Integer.toUnsignedLong(ByteBuffer.wrap(bytes, offset, Integer.BYTES)
                .order(ByteOrder.nativeOrder()).getInt());
    }
}
