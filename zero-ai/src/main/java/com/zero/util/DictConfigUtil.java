package com.zero.util;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zero.constant.AiDataSourceConstants;
import com.zero.mapper.AiDictItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 20:36
 * 说明: 查询业务字典并缓存有效结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictConfigUtil {

    /** 与 zero-report 业务字典查询及失效标记共用的键前缀。 */
    private static final String CACHE_PREFIX = "zero:dict:config:";

    /** 固定字典项类型，单项和分组均以列表存储。 */
    private static final TypedJsonJacksonCodec ITEMS_CODEC = new TypedJsonJacksonCodec(new TypeReference<List<com.zero.domain.DictItem>>() {
    });

    /** 负责按分组编码和字典项编码查询业务字典。 */
    private final AiDictItemMapper dictItemMapper;

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
    @DS(AiDataSourceConstants.REPORT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public com.zero.domain.DictItem findActiveItem(String groupCode, String itemCode) {
        if (groupCode == null || itemCode == null) {
            return null;
        }
        List<com.zero.domain.DictItem> items = findCached(itemKeyPrefix(groupCode) + itemCode, () -> {
            com.zero.domain.DictItem item = dictItemMapper.selectActiveByCodes(groupCode, itemCode);
            return item == null ? List.of() : List.of(item);
        }, item -> groupCode.equals(item.getLookupGroupCode()) && itemCode.equals(item.getItemCode()));
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
    @DS(AiDataSourceConstants.REPORT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<com.zero.domain.DictItem> findActiveItems(String groupCode) {
        if (groupCode == null) {
            return List.of();
        }
        return findCached("group:" + groupCode,
                () -> dictItemMapper.selectActiveByGroupCode(groupCode),
                item -> groupCode.equals(item.getLookupGroupCode()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 18:43
     * 说明: 只读检查单项或分组缓存。
     * @param groupCode 已校验的分组编码
     * @param itemCode 已校验的字典项编码，为 null 时检查分组列表缓存
     * @return 缓存状态和存在时的剩余有效期
     * 返回值: Map<String, Object>
     * 返回值说明: status 区分未装配、未命中、有效数据和 Redis 异常，ttlMillis 为剩余毫秒数，-1 表示无有效期
     */
    public Map<String, Object> inspectCache(String groupCode, String itemCode) {
        RedissonClient client = clients.getIfAvailable();
        if (client == null) {
            return Map.of("status", "REDIS_DISABLED");
        }
        try {
            String lookupKey = itemCode == null ? "group:" + groupCode
                    : itemKeyPrefix(groupCode) + itemCode;
            RBucket<List<com.zero.domain.DictItem>> bucket = client.getBucket(CACHE_PREFIX + lookupKey, ITEMS_CODEC);
            List<com.zero.domain.DictItem> cached = bucket.get();
            if (cached == null || cached.isEmpty()) {
                return Map.of("status", "MISS");
            }
            long ttl = bucket.remainTimeToLive();
            return ttl == -2 ? Map.of("status", "MISS")
                    : Map.of("status", "PRESENT", "ttlMillis", ttl);
        } catch (RedisException ex) {
            log.debug("检查业务字典缓存失败，异常类型 {}", ex.getClass().getSimpleName());
            return Map.of("status", "REDIS_UNAVAILABLE");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:56
     * 说明: 缓存命中直接返回，未命中查库。有结果且编码与库中一致时写入无有效期缓存。
     * @param lookupKey 区分单项和列表的查询键
     * @param query 读取已提交数据的数据库查询
     * @param matchesStoredCodes 校验请求编码与库中原始编码一致，保证修改后能定向清理
     * @return 缓存结果或回源结果
     * 返回值: List<DictItem>
     * 返回值说明: 当前查询的有效项列表
     */
    private List<com.zero.domain.DictItem> findCached(String lookupKey, Supplier<List<com.zero.domain.DictItem>> query,
                                     Predicate<com.zero.domain.DictItem> matchesStoredCodes) {
        RedissonClient client = clients.getIfAvailable();
        if (client == null) {
            return query.get();
        }

        RBucket<List<com.zero.domain.DictItem>> bucket;
        try {
            bucket = client.getBucket(CACHE_PREFIX + lookupKey, ITEMS_CODEC);
            List<com.zero.domain.DictItem> cached = bucket.get();
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (RedisException ex) {
            log.debug("业务字典缓存读取失败，回源查询，异常类型 {}", ex.getClass().getSimpleName());
            bucket = null;
        }

        List<com.zero.domain.DictItem> result = query.get();
        // 按报表服务的失效约定，只为与库中原始编码一致的有效结果回填缓存。
        if (bucket != null && !result.isEmpty() && matchesStoredCodes.test(result.get(0))) {
            try {
                // 只补充尚无缓存的键，保留并发请求先写入的结果。
                bucket.setIfAbsent(result);
            } catch (RedisException ex) {
                log.debug("业务字典缓存回填失败，异常类型 {}", ex.getClass().getSimpleName());
            }
        }
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 22:30
     * 说明: 生成单项键的分组前缀，含冒号的组用长度标记消除分隔歧义。
     * @param groupCode 原始分组编码
     * @return 单项键前缀
     * 返回值: String
     * 返回值说明: 末尾包含分隔冒号，可直接拼接原始字典项编码
     */
    private static String itemKeyPrefix(String groupCode) {
        if (groupCode.indexOf(':') >= 0) {
            return "item-colon:" + groupCode.length() + ":" + groupCode + ":";
        }
        return "item:" + groupCode + ":";
    }

}
