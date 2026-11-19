package com.zero.task.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zero.constant.TaskDataSourceConstants;
import com.zero.task.domain.DictItem;
import com.zero.task.mapper.TaskDictItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 15:56
 * 说明: 从 zeroBiz 读取已提交的业务字典配置，使用与 zero-report 一致的缓存格式及失效版本。
 */
@Service
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class TaskBizDictLookup {

    /** 与 zero-report 业务字典查询及失效标记共用的键前缀。 */
    private static final String CACHE_PREFIX = "zero:report:biz-dict:";

    /** 固定字典项类型，空列表表示数据库中没有匹配的有效记录。 */
    private static final TypedJsonJacksonCodec ITEMS_CODEC = new TypedJsonJacksonCodec(new TypeReference<List<DictItem>>() {
    });

    /** 命中有效字典项时的缓存时长。 */
    private static final Duration HIT_TTL = Duration.ofSeconds(60);

    /** 空结果缓存时长，限制频繁查无结果产生的数据库压力。 */
    private static final Duration EMPTY_TTL = Duration.ofSeconds(15);

    /** 缓存未命中时最多等待另一个请求回填的时间，单位为毫秒。 */
    private static final long LOCK_WAIT_MILLIS = 150;

    /** 负责按分组编码和字典项编码查询业务字典。 */
    private final TaskDictItemMapper dictItemMapper;

    /** 使用现有 Redis 客户端，未装配 Redis 的测试可直接查询数据库。 */
    private final ObjectProvider<RedissonClient> clients;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 按组和项读取缓存，回源时挂起调用方事务并查询业务库已提交数据。
     * @param groupCode 业务字典分组编码
     * @param itemCode 分组内的业务字典项编码
     * @return
     * 返回值: DictItem
     * 返回值说明: 已启用且未删除的字典项，查不到时返回 null
     */
    @DS(TaskDataSourceConstants.REPORT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DictItem findActiveItem(String groupCode, String itemCode) {
        if (groupCode == null || itemCode == null) {
            return null;
        }
        List<DictItem> items = findCached("item:" + encode(groupCode) + ":" + encode(itemCode), () -> {
            DictItem item = dictItemMapper.selectActiveByCodes(groupCode, itemCode);
            return item == null ? List.of() : List.of(item);
        });
        return items.isEmpty() ? null : items.get(0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 按组读取已提交的有效字典项列表，缓存保留数据库排序。
     * @param groupCode 业务字典分组编码
     * @return
     * 返回值: List<DictItem>
     * 返回值说明: 按排序值和主键升序的字典项，查不到时返回空集合
     */
    @DS(TaskDataSourceConstants.REPORT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<DictItem> findActiveItems(String groupCode) {
        if (groupCode == null) {
            return List.of();
        }
        return findCached("group:" + encode(groupCode),
                () -> dictItemMapper.selectActiveByGroupCode(groupCode));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 查找当前版本的缓存，未命中时限时加锁回填。当前请求只向读取时取得的版本写入。
     * @param lookupKey 区分单项和列表的查询键
     * @param query 读取已提交数据的数据库查询
     * @return 缓存结果或回源结果
     * 返回值: List<DictItem>
     * 返回值说明: 当前查询的有效项列表
     */
    private List<DictItem> findCached(String lookupKey, Supplier<List<DictItem>> query) {
        RedissonClient client = clients.getIfAvailable();
        if (client == null) {
            return query.get();
        }

        RBucket<List<DictItem>> bucket;
        RLock lock;
        boolean locked;
        String version;
        try {
            version = readVersion(client);
        } catch (RedisException ex) {
            log.debug("业务字典缓存版本读取失败，回源查询，异常类型 {}", ex.getClass().getSimpleName());
            return query.get();
        }
        if (version == null) {
            return query.get();
        }
        try {
            String cacheKey = CACHE_PREFIX + "entry:" + version + ":" + lookupKey;
            bucket = client.getBucket(cacheKey, ITEMS_CODEC);
            List<DictItem> cached = bucket.get();
            if (cached != null) {
                return cached;
            }
            lock = client.getLock(cacheKey + ":lock");
            locked = lock.tryLock(LOCK_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return query.get();
        } catch (RedisException ex) {
            log.debug("业务字典缓存读取失败，回源查询，异常类型 {}", ex.getClass().getSimpleName());
            return query.get();
        }
        if (!locked) {
            return query.get();
        }
        try {
            try {
                List<DictItem> cached = bucket.get();
                if (cached != null) {
                    return cached;
                }
            } catch (RedisException ex) {
                log.debug("业务字典缓存再次读取失败，回源查询，异常类型 {}", ex.getClass().getSimpleName());
                return query.get();
            }
            long started = System.nanoTime();
            List<DictItem> result = query.get();
            // 从回源开始计算有效期，慢查询也不能延长失效失败时旧值的最长可见时间。
            Duration ttl = (result.isEmpty() ? EMPTY_TTL : HIT_TTL).minusNanos(System.nanoTime() - started);
            try {
                if (ttl.toMillis() > 0) {
                    bucket.set(result, ttl);
                }
            } catch (RedisException ex) {
                log.debug("业务字典缓存回填失败，异常类型 {}", ex.getClass().getSimpleName());
            }
            return result;
        } finally {
            try {
                lock.unlock();
            } catch (RedisException | IllegalMonitorStateException ex) {
                log.debug("业务字典缓存回填锁释放失败，异常类型 {}", ex.getClass().getSimpleName());
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 读取共享版本，首次读取通过原子写入选定版本，避免覆盖并发提交的版本。
     * @param client Redisson 客户端
     * @return 当前缓存版本，版本键被并发移除时可为空
     * 返回值: String
     * 返回值说明: 该次查询使用的版本标识
     */
    private String readVersion(RedissonClient client) {
        RBucket<String> bucket = client.getBucket(CACHE_PREFIX + "version", StringCodec.INSTANCE);
        String version = bucket.get();
        if (version != null) {
            return version;
        }
        String initialVersion = UUID.randomUUID().toString();
        return bucket.setIfAbsent(initialVersion) ? initialVersion : bucket.get();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 对单段编码做无损转义，避免分隔符冲突并保留数据库编码匹配语义。
     * @param value 字典组或字典项编码
     * @return URL 安全的 Base64 内容
     * 返回值: String
     * 返回值说明: 可用作 Redis 键的独立片段
     */
    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
