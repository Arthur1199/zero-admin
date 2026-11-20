package com.zero.framework.statistics;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 01:28
 * 说明: 在隔离 Redis 中验证统计采样按持有者释放锁。
 */
@EnabledIfEnvironmentVariable(named = "ZERO_ADMIN_REDIS_OPERATIONS_TEST_PORT", matches = "[0-9]+")
class StatisticsSamplingLockRedisTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:28
     * 说明: 采样超时后由另一节点取得锁，旧节点的清理不能删除新锁。
     * @return
     * 返回值: void
     * 返回值说明: 覆盖锁接管、持有者正常释放以及重复释放
     */
    @Test
    void leavesReplacementOwnerLockIntact()
    {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("127.0.0.1",
                Integer.parseInt(System.getenv("ZERO_ADMIN_REDIS_OPERATIONS_TEST_PORT")));
        factory.afterPropertiesSet();
        try
        {
            StringRedisTemplate redis = new StringRedisTemplate(factory);
            var pipeline = new StatisticsPipeline(null, redis, null, null, null, null);
            String key = "isolated:statistics:sample:" + UUID.randomUUID();
            redis.opsForValue().set(key, "old", Duration.ofMillis(20));
            await().atMost(Duration.ofSeconds(5)).until(() -> !Boolean.TRUE.equals(redis.hasKey(key)));

            assertThat(redis.opsForValue().setIfAbsent(key, "replacement", Duration.ofMinutes(1))).isTrue();
            assertThat(pipeline.releaseSampleLock(key, "old")).isFalse();
            assertThat(redis.opsForValue().get(key)).isEqualTo("replacement");
            assertThat(pipeline.releaseSampleLock(key, "replacement")).isTrue();
            assertThat(pipeline.releaseSampleLock(key, "replacement")).isFalse();
        }
        finally
        {
            factory.destroy();
        }
    }
}
