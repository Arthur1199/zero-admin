package com.zero.util;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.zero.constant.ReportDataSourceConstants;
import com.zero.domain.DictItem;
import com.zero.mapper.DictItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 20:36
 * 说明: 查询业务字典并缓存有效结果。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DictConfigUtil {

    /** 业务字典查询缓存及其失效标记的统一键前缀。 */
    private static final String CACHE_PREFIX = "zero:dict:config:";

    /** 固定字典项类型，单项和分组均以列表存储。 */
    private static final TypedJsonJacksonCodec ITEMS_CODEC = new TypedJsonJacksonCodec(new TypeReference<List<DictItem>>() {
    });

    /** 负责按分组编码和字典项编码查询业务字典。 */
    private final DictItemMapper dictItemMapper;

    /** 使用现有 Redis 客户端，未装配 Redis 的测试可直接查询数据库。 */
    private final ObjectProvider<RedissonClient> clients;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 按组和项读取缓存，回源时暂停调用方事务和待执行分页，读取业务库已提交数据。
     * @param groupCode 业务字典分组编码
     * @param itemCode 分组内的业务字典项编码
     * @return
     * 返回值: DictItem
     * 返回值说明: 已启用且未删除的字典项，查不到时返回 null
     */
    @DS(ReportDataSourceConstants.REPORT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DictItem findActiveItem(String groupCode, String itemCode) {
        if (groupCode == null || itemCode == null) {
            return null;
        }
        List<DictItem> items = findCached(itemKeyPrefix(groupCode) + itemCode, () -> {
            DictItem item = withoutPendingPagination(() -> dictItemMapper.selectActiveByCodes(groupCode, itemCode));
            return item == null ? List.of() : List.of(item);
        }, item -> groupCode.equals(item.getLookupGroupCode()) && itemCode.equals(item.getItemCode()));
        return items.isEmpty() ? null : items.get(0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 按组读取已提交的有效字典项列表，缓存保留数据库排序。
     * @param groupCode 业务字典分组编码
     * @return
     * 返回值: List<DictItem>
     * 返回值说明: 按排序值和主键升序的字典项，查不到时返回空集合
     */
    @DS(ReportDataSourceConstants.REPORT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<DictItem> findActiveItems(String groupCode) {
        if (groupCode == null) {
            return List.of();
        }
        return findCached("group:" + groupCode,
                () -> withoutPendingPagination(() -> dictItemMapper.selectActiveByGroupCode(groupCode)),
                item -> groupCode.equals(item.getLookupGroupCode()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:35
     * 说明: 清理实际变更的字典项及所属分组列表，保留同组其他单项。
     * @param written 数据库写入是否成功
     * @param itemsByGroup 按分组编码归集的新旧字典项编码
     * @return 原数据库写入结果
     * 返回值: boolean
     * 返回值说明: 数据库操作成功时为 true
     */
    public boolean invalidateItemsAfterWrite(boolean written, Map<String, Set<String>> itemsByGroup) {
        if (!written || itemsByGroup.isEmpty()) {
            return written;
        }
        var affectedKeys = new LinkedHashSet<String>();
        itemsByGroup.forEach((groupCode, itemCodes) -> {
            affectedKeys.add(CACHE_PREFIX + "group:" + groupCode);
            String itemPrefix = CACHE_PREFIX + itemKeyPrefix(groupCode);
            itemCodes.forEach(item -> affectedKeys.add(itemPrefix + item));
        });
        return invalidateAfterWrite(true, keys -> keys.delete(affectedKeys.toArray(String[]::new)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:35
     * 说明: 分组变更影响该组下所有单项，按涉及的分组编码清理。
     * @param written 数据库写入是否成功
     * @param groupCodes 涉及的分组编码，改名时同时传入旧编码
     * @return 原数据库写入结果
     * 返回值: boolean
     * 返回值说明: 数据库写入成功时为 true
     */
    public boolean invalidateGroupsAfterWrite(boolean written, List<String> groupCodes) {
        return invalidateAfterWrite(written, keys -> groupCodes.stream().distinct()
                .forEach(group -> {
                    keys.delete(CACHE_PREFIX + "group:" + group);
                    keys.deleteByPattern(escapeRedisPattern(CACHE_PREFIX + itemKeyPrefix(group)) + "*");
                }));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 19:35
     * 说明: 写入成功才提交缓存清理，有事务时等到提交后执行。
     * @param written 数据库写入是否成功
     * @param eviction 只清理受影响字典缓存的操作
     * @return 原数据库写入结果
     * 返回值: boolean
     * 返回值说明: 数据库写入成功时为 true
     */
    private boolean invalidateAfterWrite(boolean written, Consumer<RKeys> eviction) {
        if (!written) {
            return false;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            evictCache(eviction);
        } else if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /**
                 *
                 * 作者: Arthur 亚瑟
                 * 时间: 2026/11/19 15:28
                 * 说明: 数据库事务提交后清理字典缓存。
                 */
                @Override
                public void afterCommit() {
                    evictCache(eviction);
                }
            });
        } else {
            log.warn("业务字典写入事务未启用同步回调，受影响的缓存可能仍是旧值");
        }
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:28
     * 说明: 缓存命中直接返回，未命中查库。有结果且编码与库中一致时写入无有效期缓存。
     * @param lookupKey 区分单项和列表的查询键
     * @param query 读取已提交数据的数据库查询
     * @param matchesStoredCodes 校验请求编码与库中原始编码一致，保证修改后能定向清理
     * @return 缓存结果或回源结果
     * 返回值: List<DictItem>
     * 返回值说明: 当前查询的有效项列表
     */
    private List<DictItem> findCached(String lookupKey, Supplier<List<DictItem>> query,
                                     Predicate<DictItem> matchesStoredCodes) {
        RedissonClient client = clients.getIfAvailable();
        if (client == null) {
            return query.get();
        }

        RBucket<List<DictItem>> bucket;
        try {
            bucket = client.getBucket(CACHE_PREFIX + lookupKey, ITEMS_CODEC);
            List<DictItem> cached = bucket.get();
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (RedisException ex) {
            log.debug("业务字典缓存读取失败，回源查询，异常类型 {}", ex.getClass().getSimpleName());
            bucket = null;
        }

        List<DictItem> result = query.get();
        if (bucket != null && !result.isEmpty() && matchesStoredCodes.test(result.get(0))) {
            try {
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
     * 时间: 2026/11/19 15:28
     * 说明: 执行定向清理，失败时记录告警。
     * @param eviction 受影响字典键的清理操作
     */
    private void evictCache(Consumer<RKeys> eviction) {
        RedissonClient client = clients.getIfAvailable();
        if (client == null) {
            return;
        }
        try {
            eviction.accept(client.getKeys());
        } catch (RedisException | RejectedExecutionException ex) {
            log.warn("业务字典缓存失效失败，受影响的缓存可能仍是旧值，异常类型 {}", ex.getClass().getSimpleName());
        }
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 22:30
     * 说明: 在 Redis 匹配模式中按字面匹配键前缀，避免分组编码扩大清理范围。
     * @param prefix 原始 Redis 键前缀
     * @return 转义后的匹配模式前缀
     * 返回值: String
     * 返回值说明: 可附加通配符以匹配指定分组的单项
     */
    private static String escapeRedisPattern(String prefix) {
        return prefix.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?")
                .replace("[", "\\[").replace("]", "\\]");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:41
     * 说明: 字典查询不占用调用方待执行的 PageHelper 分页，查询完成后恢复原分页。
     * @param <T> 字典单项或集合的返回类型
     * @param query 当前业务字典查询
     * @return
     * 返回值: T
     * 返回值说明: 字典查询的结果
     */
    private <T> T withoutPendingPagination(Supplier<T> query) {
        var pendingPage = PageHelper.getLocalPage();
        PageHelper.clearPage();
        try {
            return query.get();
        } finally {
            if (pendingPage != null) {
                PageHelper.setLocalPage(pendingPage);
            }
        }
    }

}
