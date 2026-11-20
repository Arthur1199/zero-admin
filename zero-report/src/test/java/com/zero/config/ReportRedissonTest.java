package com.zero.config;

import com.zero.util.RedissonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.redisson.Redisson;
import org.redisson.RedissonObject;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.RedisException;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:47
 * 说明: 校验 Redisson 启动配置和报表缓存调用。
 */
class ReportRedissonTest {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:47
     * 说明: 验证 JSON 编解码、按需连接和单节点连接池边界。
     */
    @Test
    void customizesStandalonePoolAndCodec() {
        var config = new Config();
        var server = config.useSingleServer();
        new RedissonAutoConfigCustomizer(8, 0).customize(config);
        assertThat(config.getCodec()).isInstanceOf(JsonJacksonCodec.class);
        assertThat(config.isLazyInitialization()).isTrue();
        assertThat(server.getConnectionPoolSize()).isEqualTo(8);
        assertThat(server.getConnectionMinimumIdleSize()).isZero();
        assertThatThrownBy(() -> new RedissonAutoConfigCustomizer(2, 3).customize(config))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("连接池");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:23
     * 说明: 使用独立 Redis 验证缓存、分布式锁及降额后旧许可分批过期的限流行为。
     */
    @Test
    @EnabledIfSystemProperty(named = "zero.report.test.redis-port", matches = "[0-9]+")
    void usesRedissonForNativeCacheAndSpringRedisOperations() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RedissonAutoConfigurationV2.class, RedisAutoConfiguration.class))
                .withUserConfiguration(RedissonAutoConfigCustomizer.class, RedissonUtil.class)
                .withPropertyValues("spring.data.redis.host=127.0.0.1",
                        "spring.data.redis.port=" + System.getProperty("zero.report.test.redis-port"),
                        "spring.data.redis.database=3", "spring.data.redis.timeout=5s",
                        "zero.redis.connection-pool-size=7", "zero.redis.connection-minimum-idle-size=1",
                        "zero.report.redis.connection-pool-size=8", "zero.report.redis.connection-minimum-idle-size=0")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(RedissonClient.class);
                    assertThat(context.getBean(RedisConnectionFactory.class))
                            .isInstanceOf(RedissonConnectionFactory.class);
                    var client = context.getBean(RedissonClient.class);
                    var server = client.getConfig().useSingleServer();
                    assertThat(server.getDatabase()).isEqualTo(3);
                    assertThat(server.getConnectionPoolSize()).isEqualTo(7);
                    assertThat(server.getConnectionMinimumIdleSize()).isEqualTo(1);

                    var cache = context.getBean(RedissonUtil.class);
                    var template = context.getBean(StringRedisTemplate.class);
                    String prefix = "zero-report:redisson-test:" + UUID.randomUUID();
                    String bucketKey = prefix + ":bucket";
                    String hashKey = prefix + ":hash";
                    String templateKey = prefix + ":template";
                    String requestKey = prefix + ":request";
                    try {
                        assertThat(cache.getExpire(bucketKey)).isEqualTo(-2L);
                        cache.setString(bucketKey, new HashMap<>(Map.of("state", "有效")), 2, TimeUnit.SECONDS);
                        assertThat(cache.getExpire(bucketKey)).isBetween(1L, 2000L);
                        assertThat(cache.<Map<String, String>>getString(bucketKey)).containsEntry("state", "有效");
                        assertThat(cache.addToHash(hashKey, "status", "有效")).isTrue();
                        assertThat(cache.<String>getFromHash(hashKey, "status")).isEqualTo("有效");
                        template.opsForValue().set(templateKey, "兼容模板");
                        assertThat(template.opsForValue().get(templateKey)).isEqualTo("兼容模板");
                        assertThat(cache.getExpire(templateKey)).isEqualTo(-1L);
                        assertThat(cache.setIfAbsent(requestKey, "first", Duration.ofMinutes(1))).isTrue();
                        assertThat(cache.setIfAbsent(requestKey, "second", Duration.ofMinutes(2))).isFalse();
                        assertThat(cache.<String>getString(requestKey)).isEqualTo("first");
                        assertThat(cache.getExpire(requestKey)).isBetween(1L, 60_000L);
                        assertThat(cache.clearExpire(requestKey)).isTrue();
                        assertThat(cache.getExpire(requestKey)).isEqualTo(-1L);
                        assertThat(cache.clearExpire(requestKey)).isFalse();
                        assertThat(cache.clearExpire(prefix + ":missing")).isFalse();
                        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                            assertThat(cache.exists(bucketKey)).isFalse();
                            assertThat(cache.getExpire(bucketKey)).isEqualTo(-2L);
                        });
                        verifyCollectionAndCounterOperations(client, cache, prefix);
                        verifyDistributedCoordination(client, cache, prefix);
                    } finally {
                        client.getKeys().delete(bucketKey, hashKey, templateKey, requestKey);
                    }
                });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:10
     * 说明: 验证列表两端取出、批量添加集合成员及有效期、成员查询和整数计数器读取。
     * @param client 独立 Redis 的测试客户端，用于清理测试键
     * @param cache 待验证的缓存工具
     * @param prefix 测试键前缀
     */
    private void verifyCollectionAndCounterOperations(RedissonClient client, RedissonUtil cache, String prefix) {
        String listKey = prefix + ":events";
        String setKey = prefix + ":tags";
        String counterKey = prefix + ":count";
        try {
            assertThat(cache.<String>pollListFirst(listKey)).isNull();
            assertThat(cache.addToList(listKey, List.of("first", "middle", "last"))).isTrue();
            assertThat(cache.<String>pollListFirst(listKey)).isEqualTo("first");
            assertThat(cache.<String>pollListLast(listKey)).isEqualTo("last");
            assertThat(cache.<String>getFromList(listKey)).containsExactly("middle");
            assertThat(cache.<String>pollListLast(listKey)).isEqualTo("middle");
            assertThat(cache.<String>pollListLast(listKey)).isNull();

            assertThat(cache.containsInSet(setKey, "finance")).isFalse();
            assertThat(cache.addToSet(setKey, List.of("finance", "daily", "finance"), 1, TimeUnit.MINUTES)).isTrue();
            assertThat(cache.<String>getFromSet(setKey)).containsExactlyInAnyOrder("finance", "daily");
            assertThat(cache.containsInSet(setKey, "finance")).isTrue();
            assertThat(cache.containsInSet(setKey, "weekly")).isFalse();
            assertThat(cache.getExpire(setKey)).isBetween(1L, 60_000L);

            assertThat(cache.getLongCounter(counterKey)).isZero();
            assertThat(cache.exists(counterKey)).isFalse();
            assertThat(cache.increment(counterKey, 5L)).isEqualTo(5L);
            assertThat(cache.increment(counterKey, -2L)).isEqualTo(3L);
            assertThat(cache.getLongCounter(counterKey)).isEqualTo(3L);
        } finally {
            client.getKeys().delete(listKey, setKey, counterKey);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:23
     * 说明: 从两个客户端验证锁竞争、读写互斥、异常释放锁及共享限流降额。
     * @param client 测试应用上下文中的 Redisson 客户端
     * @param cache 第一个客户端对应的缓存工具
     * @param prefix 用于隔离同一 Redis 库中的测试键
     */
    private void verifyDistributedCoordination(RedissonClient client, RedissonUtil cache, String prefix) throws InterruptedException {
        RedissonClient secondClient = Redisson.create(new Config(client.getConfig()));
        try {
            RedissonUtil secondCache = new RedissonUtil(secondClient);
            String lockName = prefix + ":lock";
            RLock lock = cache.getLock(lockName);
            AtomicInteger executions = new AtomicInteger();
            assertThat(lock.tryLock()).isTrue();
            try {
                assertThat(lock.tryLock()).isTrue();
                lock.unlock();
                assertThat(lock.isHeldByCurrentThread()).isTrue();
                assertThat(secondCache.getLock(lockName).tryLock()).isFalse();
                assertThat(secondCache.tryWithLock(lockName, 10, TimeUnit.MILLISECONDS, executions::incrementAndGet)).isFalse();
                assertThat(executions).hasValue(0);
            } finally {
                lock.unlock();
            }
            assertThat(secondCache.tryWithLock(lockName, 0, TimeUnit.MILLISECONDS, executions::incrementAndGet)).isTrue();
            assertThat(executions).hasValue(1);
            var actionFailure = new IllegalStateException("测试操作失败");
            assertThatThrownBy(() -> secondCache.tryWithLock(lockName, 0, TimeUnit.MILLISECONDS, () -> {
                throw actionFailure;
            })).isSameAs(actionFailure);
            assertThat(cache.tryWithLock(lockName, 0, TimeUnit.MILLISECONDS, executions::incrementAndGet)).isTrue();
            assertThat(executions).hasValue(2);

            String fairName = prefix + ":fair";
            RLock fairLock = cache.getFairLock(fairName);
            assertThat(fairLock.tryLock()).isTrue();
            try {
                assertThat(secondCache.getFairLock(fairName).tryLock()).isFalse();
            } finally {
                fairLock.unlock();
            }

            String readWriteName = prefix + ":read-write";
            var firstReadWriteLock = cache.getReadWriteLock(readWriteName);
            var secondReadWriteLock = secondCache.getReadWriteLock(readWriteName);
            RLock firstRead = firstReadWriteLock.readLock();
            RLock secondRead = secondReadWriteLock.readLock();
            RLock secondWrite = secondReadWriteLock.writeLock();
            assertThat(firstRead.tryLock()).isTrue();
            try {
                assertThat(secondRead.tryLock()).isTrue();
                secondRead.unlock();
                assertThat(secondWrite.tryLock()).isFalse();
            } finally {
                firstRead.unlock();
            }
            assertThat(secondWrite.tryLock()).isTrue();
            try {
                assertThat(firstRead.tryLock()).isFalse();
                assertThat(firstReadWriteLock.writeLock().tryLock()).isFalse();
            } finally {
                secondWrite.unlock();
            }
            verifySharedRateLimit(client, cache, secondCache, prefix);
            verifyRateRuleUpdate(client, cache, secondCache, prefix);
            verifyRateDecreaseWithStaggeredExpiry(client, cache, secondCache, prefix);
            verifyConcurrentRateUpdate(client, cache, secondCache, prefix);
        } finally {
            secondClient.shutdown();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:14
     * 说明: 验证多个客户端共享配额、重复初始化保留现有配额，以及立即和限时取许可。
     * @param client 测试客户端，用于清理限流规则及许可记录
     * @param cache 首个客户端对应的工具对象
     * @param secondCache 第二个客户端对应的工具对象
     * @param prefix 测试键前缀
     */
    private void verifySharedRateLimit(RedissonClient client, RedissonUtil cache, RedissonUtil secondCache, String prefix) {
        String sharedKey = prefix + ":rate:shared";
        String waitingKey = prefix + ":rate:waiting";
        try {
            Duration window = Duration.ofMinutes(1);
            assertThat(cache.tryInitRateLimiter(sharedKey, 2, window)).isTrue();
            assertThat(cache.tryAcquirePermits(sharedKey, 1)).isTrue();
            assertThat(secondCache.tryInitRateLimiter(sharedKey, 2, window)).isFalse();
            assertThat(secondCache.tryInitRateLimiter(sharedKey, 3, window)).isFalse();
            assertThat(secondCache.tryAcquirePermits(sharedKey, 2)).isFalse();
            assertThat(secondCache.tryAcquirePermits(sharedKey, 1)).isTrue();
            assertThat(cache.tryAcquirePermits(sharedKey, 1, Duration.ZERO)).isFalse();
            assertThat(cache.tryAcquirePermits(sharedKey, 1, Duration.ofMillis(100))).isFalse();

            assertThat(cache.tryInitRateLimiter(waitingKey, 1, Duration.ofSeconds(1))).isTrue();
            assertThat(cache.tryAcquirePermits(waitingKey, 1)).isTrue();
            assertThat(secondCache.tryAcquirePermits(waitingKey, 1, Duration.ofSeconds(5))).isTrue();
        } finally {
            client.getRateLimiter(sharedKey).delete();
            client.getRateLimiter(waitingKey).delete();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:20
     * 说明: 验证规则只在参数变化时更新，提高或降低配额及修改窗口时沿用仍有效的消耗记录。
     * @param client 测试客户端，用于校验规则参数并清理限流键
     * @param cache 首个客户端对应的工具对象
     * @param secondCache 第二个客户端对应的工具对象
     * @param prefix 测试键前缀
     */
    private void verifyRateRuleUpdate(RedissonClient client, RedissonUtil cache, RedissonUtil secondCache, String prefix) {
        String key = prefix + ":rate:update";
        var limiter = client.getRateLimiter(key);
        Duration original = Duration.ofMinutes(1);
        Duration longer = Duration.ofMinutes(2);
        try {
            assertThat(cache.updateRateLimiter(key, 2, original)).isFalse();
            assertThat(limiter.isExists()).isFalse();
            assertThat(cache.tryInitRateLimiter(key, 2, original)).isTrue();
            assertThat(cache.tryAcquirePermits(key, 2)).isTrue();
            assertThat(secondCache.updateRateLimiter(key, 2, Duration.ofMillis(original.toMillis()))).isFalse();
            assertThat(secondCache.tryAcquirePermits(key, 1)).isFalse();

            assertThat(secondCache.updateRateLimiter(key, 3, original)).isTrue();
            assertThat(limiter.getConfig().getRate()).isEqualTo(3L);
            assertThat(cache.tryAcquirePermits(key, 2)).isFalse();
            assertThat(cache.tryAcquirePermits(key, 1)).isTrue();

            assertThat(cache.updateRateLimiter(key, 3, longer)).isTrue();
            assertThat(limiter.getConfig().getRateInterval()).isEqualTo(longer.toMillis());
            assertThat(secondCache.tryAcquirePermits(key, 1)).isFalse();
            assertThat(secondCache.updateRateLimiter(key, 1, longer)).isTrue();
            assertThat(cache.tryAcquirePermits(key, 1)).isFalse();

            assertThat(cache.updateRateLimiter(key, 1, Duration.ofMillis(1))).isTrue();
            assertThat(secondCache.tryAcquirePermits(key, 1, Duration.ofSeconds(1))).isTrue();
        } finally {
            limiter.delete();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:23
     * 说明: 降额后逐批推进旧许可记录的时间，验证剩余有效消耗依然占用新配额。
     * @param client 测试客户端，用于调整许可记录时间和清理规则
     * @param cache 首个客户端对应的工具对象
     * @param secondCache 第二个客户端对应的工具对象
     * @param prefix 测试键前缀
     */
    private void verifyRateDecreaseWithStaggeredExpiry(
            RedissonClient client, RedissonUtil cache, RedissonUtil secondCache, String prefix) {
        String key = prefix + ":rate:decrease";
        Duration window = Duration.ofMinutes(1);
        var limiter = client.getRateLimiter(key);
        var usedPermits = client.<byte[]>getScoredSortedSet(RedissonObject.suffixName(key, "permits"), ByteArrayCodec.INSTANCE);
        try {
            assertThat(cache.tryInitRateLimiter(key, 4, window)).isTrue();
            assertThat(cache.tryAcquirePermits(key, 2)).isTrue();
            assertThat(secondCache.tryAcquirePermits(key, 2)).isTrue();
            assertThat(cache.updateRateLimiter(key, 2, window)).isTrue();
            assertThat(usedPermits.size()).isEqualTo(2);
            assertThat(secondCache.tryAcquirePermits(key, 1)).isFalse();

            // 两批都是 Redisson 实际产生的记录。调整分值模拟时间经过，避免依赖测试进程的休眠精度。
            long expiredScore = System.currentTimeMillis() - window.toMillis() - 1000;
            usedPermits.add(expiredScore, usedPermits.first());
            assertThat(secondCache.tryAcquirePermits(key, 1)).isFalse();
            assertThat(usedPermits.size()).isEqualTo(1);
            assertThat(cache.tryAcquirePermits(key, 1, Duration.ofMillis(100))).isFalse();

            usedPermits.add(expiredScore, usedPermits.first());
            assertThat(secondCache.tryAcquirePermits(key, 2, Duration.ofSeconds(1))).isTrue();
            assertThat(cache.tryAcquirePermits(key, 1)).isFalse();
        } finally {
            limiter.delete();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:29
     * 说明: 两个客户端同时更新规则并申领许可时，规则只变更一次，许可余额不会因并发写入被覆盖。
     * @param client 隔离 Redis 的主测试客户端
     * @param firstCache 第一个服务实例
     * @param secondCache 第二个服务实例
     * @param prefix 随机测试键前缀
     * @return
     * 返回值: void
     * 返回值说明: 并发过程中消耗的许可仍计入最终配额，规则及其子键保留到期时间
     */
    private void verifyConcurrentRateUpdate(RedissonClient client, RedissonUtil firstCache,
            RedissonUtil secondCache, String prefix) {
        String key = prefix + ":rate:concurrent";
        Duration window = Duration.ofMinutes(1);
        var limiter = client.getRateLimiter(key);
        var pool = Executors.newFixedThreadPool(3);
        var start = new CountDownLatch(1);
        try {
            assertThat(firstCache.tryInitRateLimiter(key, 8, window)).isTrue();
            assertThat(firstCache.tryAcquirePermits(key, 4)).isTrue();
            assertThat(limiter.expire(Duration.ofMinutes(2))).isTrue();
            Callable<Boolean> update = () -> {
                start.await();
                return firstCache.updateRateLimiter(key, 10, window);
            };
            var first = pool.submit(update);
            var second = pool.submit(() -> {
                start.await();
                return secondCache.updateRateLimiter(key, 10, window);
            });
            var acquires = pool.submit(() -> {
                start.await();
                int acquired = 0;
                for (int request = 0; request < 12; request++) {
                    if (secondCache.tryAcquirePermits(key, 1)) {
                        acquired++;
                    }
                }
                return acquired;
            });
            start.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            int acquired = acquires.get(10, TimeUnit.SECONDS);
            while (acquired <= 6 && secondCache.tryAcquirePermits(key, 1)) {
                acquired++;
            }
            assertThat(acquired).isEqualTo(6);
            var manager = ((Redisson) client).getCommandExecutor().getConnectionManager();
            var connection = manager.getEntry(key).getClient().connect();
            try {
                long expiry = connection.sync(RedisCommands.PEXPIRETIME, key);
                assertThat(expiry).isPositive();
                assertThat(connection.sync(RedisCommands.PEXPIRETIME, RedissonObject.suffixName(key, "value")))
                        .isEqualTo(expiry);
                assertThat(connection.sync(RedisCommands.PEXPIRETIME, RedissonObject.suffixName(key, "permits")))
                        .isEqualTo(expiry);
            } catch (RedisException failure) {
                if (failure.getMessage() == null || !failure.getMessage().contains("unknown command")) {
                    throw failure;
                }
                assertThat(limiter.remainTimeToLive()).isPositive();
                assertThat(client.getBucket(RedissonObject.suffixName(key, "value")).remainTimeToLive()).isPositive();
            } finally {
                connection.close();
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError("等待并发限流验证被中断", error);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException error) {
            throw new AssertionError("并发限流验证失败", error);
        } finally {
            pool.shutdownNow();
            limiter.delete();
        }
    }
}
