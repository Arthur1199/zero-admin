package com.zero.framework.websocket;

import com.zero.common.core.redis.RedisWatchTransaction;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 用Redis有序集合限制同一登录会话在所有节点的连接数；租约自动清理宕机节点记录
 */
@Component
public class AdminWebSocketLease
{
    private final StringRedisTemplate redis;
    private final AdminWebSocketProperties properties;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 使用独立字符串序列化，不改变已有登录缓存格式
     * @param factory 项目现有Redis连接工厂
     * @param properties 环境及连接限制
     */
    public AdminWebSocketLease(RedisConnectionFactory factory, AdminWebSocketProperties properties)
    {
        this.redis = new StringRedisTemplate(factory);
        this.properties = properties;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 按Redis统一时间在所有节点间原子申请连接名额，Redis异常不放行
     * @param sessionId 已验证的登录会话编号
     * @param connectionId 服务端生成的全局连接编号
     * @return
     * 返回值: boolean
     * 返回值说明: true表示申请成功，false表示达到上限
     */
    public boolean acquire(String sessionId, String connectionId)
    {
        byte[] key = key(sessionId).getBytes(StandardCharsets.UTF_8);
        byte[] member = connectionId.getBytes(StandardCharsets.UTF_8);
        return RedisWatchTransaction.execute(redis, key, connection -> {
            long now = connection.serverCommands().time(TimeUnit.MILLISECONDS);
            boolean available = connection.zSetCommands().zCount(key, Range.rightUnbounded(Range.Bound.exclusive(now)))
                    < properties.maxConnectionsPerSession();
            connection.multi();
            connection.zSetCommands().zRemRangeByScore(key, Double.NEGATIVE_INFINITY, now);
            if (available)
            {
                writeLease(connection, key, member, now);
            }
            return RedisWatchTransaction.commit(connection) ? available : null;
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 按Redis统一时间维护活跃连接名额，节点长时间停顿后拒绝恢复已到期租约
     * @param sessionId 已验证的登录会话编号
     * @param connectionId 服务端连接编号
     * @return
     * 返回值: boolean
     * 返回值说明: true表示续租成功，false表示旧名额已失效
     */
    public boolean renew(String sessionId, String connectionId)
    {
        byte[] key = key(sessionId).getBytes(StandardCharsets.UTF_8);
        byte[] member = connectionId.getBytes(StandardCharsets.UTF_8);
        boolean valid = RedisWatchTransaction.execute(redis, key, connection -> {
            Double score = connection.zSetCommands().zScore(key, member);
            long now = connection.serverCommands().time(TimeUnit.MILLISECONDS);
            if (score == null || score <= now)
            {
                connection.unwatch();
                return false;
            }
            connection.multi();
            writeLease(connection, key, member, now);
            return RedisWatchTransaction.commit(connection) ? Boolean.TRUE : null;
        });
        if (valid) markOnline(sessionId, connectionId);
        return valid;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 只释放指定连接名额，不影响同账号其它连接
     * @param sessionId 登录会话编号
     * @param connectionId 服务端连接编号
     * @return
     * 返回值: void
     * 返回值说明: 删除该成员；Redis不可用时由租约到期兜底
     */
    public void release(String sessionId, String connectionId)
    {
        redis.opsForZSet().remove(key(sessionId), connectionId);
        redis.opsForZSet().remove(onlinePrefix() + sessionId, connectionId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 租约覆盖三个核对周期和空闲期限，允许短暂调度延迟
     * @return
     * 返回值: long
     * 返回值说明: 租约毫秒数
     */
    private long leaseTtl()
    {
        // 租期同时覆盖连接空闲窗口和三次会话检查间隔，给后台续租留出时间。
        return Math.max(properties.idleTimeout(), properties.sessionCheckInterval() * 3L);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 连接名额与消息频道使用相同的环境隔离前缀
     * @param sessionId 服务端会话编号
     * @return
     * 返回值: String
     * 返回值说明: 不含JWT的Redis租约键
     */
    private String key(String sessionId)
    {
        return properties.channel() + ":connections:" + sessionId;
    }

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 登记认证完成连接；Redis失败由调用方关闭连接
 * @param sessionId 已验签会话编号
 * @param connectionId 全局连接编号
 * @return
 * 返回值: void
 * 返回值说明: 完成操作或拒绝非法参数
 */

    public void markOnline(String sessionId, String connectionId) {
        byte[] key = (onlinePrefix() + sessionId).getBytes(StandardCharsets.UTF_8);
        byte[] member = connectionId.getBytes(StandardCharsets.UTF_8);
        RedisWatchTransaction.execute(redis, key, connection -> {
            long now = connection.serverCommands().time(TimeUnit.MILLISECONDS);
            connection.multi();
            connection.zSetCommands().zRemRangeByScore(key, Double.NEGATIVE_INFINITY, now);
            writeLease(connection, key, member, now);
            return RedisWatchTransaction.commit(connection) ? Boolean.TRUE : null;
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:23
     * 说明: 在事务中写入成员和键的过期时间，两个期限使用同一个 Redis 服务端时间。
     * @param connection 正在排队事务命令的 Redis 连接
     * @param key 配额或在线索引的 Redis 键
     * @param member 当前连接编号
     * @param now Redis 服务端时间，单位为毫秒
     * @return
     * 返回值: void
     * 返回值说明: 写入命令与调用方的清理或续租操作一并提交
     */
    private void writeLease(RedisConnection connection, byte[] key, byte[] member, long now)
    {
        long expiresAt = now + leaseTtl();
        connection.zSetCommands().zAdd(key, expiresAt, member);
        connection.keyCommands().pExpireAt(key, expiresAt);
    }
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 跨节点分批读取有效在线会话，调用方仍须核对登录缓存
 * @return
 * 返回值: java.util.Set<String>
 * 返回值说明: 仅服务端使用的会话编号集合
 */

    public Set<String> onlineSessions() {
        Set<String> sessions = new HashSet<>();
        String prefix = onlinePrefix();
        try (var cursor = redis.scan(ScanOptions.scanOptions().match(prefix + "*").count(200).build())) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                Long count = redis.execute((RedisCallback<Long>) connection -> {
                    long now = connection.serverCommands().time(TimeUnit.MILLISECONDS);
                    return connection.zSetCommands().zCount(key.getBytes(StandardCharsets.UTF_8),
                            Range.rightUnbounded(Range.Bound.exclusive(now)));
                });
                if (count != null && count > 0) sessions.add(key.substring(prefix.length()));
            }
        }
        return sessions;
    }
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 环境隔离的已认证在线索引前缀
 * @return
 * 返回值: String
 * 返回值说明: 不含JWT的Redis键前缀
 */

    private String onlinePrefix() { return properties.channel() + ":online:"; }
}
