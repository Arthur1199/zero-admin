package com.zero.framework.aspectj;

import com.zero.framework.config.RedisConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
 * 说明: 使用隔离 Redis 回归接口固定窗口的原子计数和原有过期行为。
 */
@EnabledIfEnvironmentVariable(named = "ZERO_ADMIN_REDIS_OPERATIONS_TEST_PORT", matches = "[0-9]+")
class RateLimiterRedisTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:28
     * 说明: 并发请求只放行限额内的请求，超限后计数封顶，已有窗口到期后重新计数。
     * @return
     * 返回值: void
     * 返回值说明: 使用随机的隔离计数键验证原有字符串格式、次数上限及固定 TTL
     */
    @Test
    void limitsConcurrentRequestsAndResetsOnlyAfterExpiry() throws Exception
    {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("127.0.0.1",
                Integer.parseInt(System.getenv("ZERO_ADMIN_REDIS_OPERATIONS_TEST_PORT")));
        factory.afterPropertiesSet();
        var pool = Executors.newFixedThreadPool(8);
        try
        {
            RateLimiterAspect first = new RateLimiterAspect(new RedisConfig().redisTemplate(factory));
            RateLimiterAspect second = new RateLimiterAspect(new RedisConfig().redisTemplate(factory));
            StringRedisTemplate redis = new StringRedisTemplate(factory);
            String key = "isolated:admin:rate:" + UUID.randomUUID();
            int limit = 5;
            var start = new CountDownLatch(1);
            var pending = new ArrayList<java.util.concurrent.Future<Long>>();
            for (int i = 0; i < 24; i++)
            {
                RateLimiterAspect aspect = i % 2 == 0 ? first : second;
                pending.add(pool.submit(() -> {
                    start.await();
                    return aspect.increment(key, 60, limit);
                }));
            }
            start.countDown();
            List<Long> results = new ArrayList<>();
            for (var request : pending)
            {
                results.add(request.get(10, TimeUnit.SECONDS));
            }
            assertThat(results.stream().filter(value -> value <= limit).toList())
                    .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L);
            assertThat(redis.opsForValue().get(key)).isEqualTo("6");
            redis.expire(key, Duration.ofMillis(500));
            assertThat(first.increment(key, 60, limit)).isEqualTo(6);
            assertThat(redis.getExpire(key, TimeUnit.MILLISECONDS)).isBetween(0L, 500L);
            await().atMost(Duration.ofSeconds(5)).until(() -> !Boolean.TRUE.equals(redis.hasKey(key)));
            assertThat(second.increment(key, 60, limit)).isEqualTo(1);
            assertThat(redis.getExpire(key)).isBetween(1L, 60L);
        }
        finally
        {
            pool.shutdownNow();
            factory.destroy();
        }
    }
}
