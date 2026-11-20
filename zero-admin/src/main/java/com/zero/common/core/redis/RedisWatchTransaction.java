package com.zero.common.core.redis;

import java.util.List;
import java.util.function.Function;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 01:23
 * 说明: 在同一 Redis 连接内监听状态键并重试被并发写入取消的事务。
 */
public final class RedisWatchTransaction
{
    /** 热点键发生并发冲突时的有限重试次数。 */
    private static final int MAX_ATTEMPTS = 32;

    private RedisWatchTransaction()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:23
     * 说明: 按条件读取或写入 Redis，连接或命令异常由调用方处理。
     * @param redis 模块现有的 Redis 模板
     * @param key 需要监听的已序列化 Redis 键
     * @param operation 在同一连接读取状态，写入时调用 multi 和 commit，只读返回前调用 unwatch
     * @return 本次操作结果
     * 返回值: T
     * 返回值说明: 回调仅在事务因并发冲突取消时返回 null，随后重新读取状态并重试
     */
    public static <T> T execute(RedisTemplate<?, ?> redis, byte[] key, Function<RedisConnection, T> operation)
    {
        return redis.execute((RedisCallback<T>) connection -> {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
            {
                connection.watch(key);
                try
                {
                    T result = operation.apply(connection);
                    if (result != null)
                    {
                        return result;
                    }
                }
                catch (RuntimeException failure)
                {
                    try
                    {
                        if (connection.isQueueing())
                        {
                            connection.discard();
                        }
                        else
                        {
                            connection.unwatch();
                        }
                    }
                    catch (RuntimeException cleanupFailure)
                    {
                        failure.addSuppressed(cleanupFailure);
                    }
                    throw failure;
                }
            }
            throw new OptimisticLockingFailureException("Redis 状态变更冲突次数过多");
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:23
     * 说明: 提交至少包含一条有返回值的写命令的事务。
     * @param connection 当前绑定连接
     * @return 是否成功提交
     * 返回值: boolean
     * 返回值说明: Redis 因监听键发生变更而取消事务时返回 false
     */
    public static boolean commit(RedisConnection connection)
    {
        List<Object> result = connection.exec();
        return result != null && !result.isEmpty();
    }
}
