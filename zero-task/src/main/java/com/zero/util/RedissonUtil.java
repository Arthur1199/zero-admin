package com.zero.util;

import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:58
 * 说明: 任务服务的 Redisson 工具，复用 Spring 管理的客户端，提供缓存、计数器、事务、分布式锁和限流。
 * 示例中的 {@code cache} 指本工具实例，按需通过构造器注入。
 * 缓存键、计数器键、锁名称和限流键使用独立前缀。各服务实例使用相同的锁名称或限流键协调同一业务资源。
 */
@RequiredArgsConstructor
public class RedissonUtil {


	/** 应用共用的 Redisson 客户端。 */
	private final RedissonClient redissonClient;

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 覆盖单值并清除原有过期时间。
	 * <p>示例: 缓存长期有效的报表名称。
	 * {@code cache.setString("report:title:42", "销售日报");}
	 * @param <T> 单值的 Java 类型
	 * @param key 单值键
	 * @param value 待写入的单值
	 * @return
	 * 返回值: void
	 * 返回值说明: 写入单值
	 */
	public <T> void setString(String key, T value) {
		RBucket<T> bucket = redissonClient.getBucket(key);
		bucket.set(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 覆盖单值并设置新的过期时间。
	 * <p>示例: 缓存 5 分钟的查询结果。
	 * {@code cache.setString("report:result:42", "ready", 5, TimeUnit.MINUTES);}
	 * @param <T> 单值的 Java 类型
	 * @param key 单值键
	 * @param value 待写入的单值
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return
	 * 返回值: void
	 * 返回值说明: 写入单值并设置有效期
	 */
	public <T> void setString(String key, T value, long timeout, TimeUnit timeUnit) {
		RBucket<T> bucket = redissonClient.getBucket(key);
		bucket.set(value, timeout, timeUnit);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:06
	 * 说明: 键不存在时原子写入单值并设置有效期，适合短时间内的重复请求去重。
	 * 命中已有键时保留它的值及有效期。
	 * <p>示例: {@code boolean first = cache.setIfAbsent("report:request:42", "accepted", Duration.ofMinutes(5));}
	 * 返回 false 时调用方可跳过重复请求。
	 * @param <T> 单值的 Java 类型
	 * @param key 单值键，不能为空或空白
	 * @param value 待写入的非空值
	 * @param ttl 有效期，至少 1 毫秒
	 * @return 成功写入时返回 true，键已存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否取得本次请求的去重标记
	 * @throws IllegalArgumentException 键为空白、值为空或有效期不足 1 毫秒
	 */
	public <T> boolean setIfAbsent(String key, T value, Duration ttl) {
		Assert.hasText(key, "Redis 键不能为空");
		Assert.notNull(value, "缓存值不能为空");
		Assert.notNull(ttl, "有效期不能为空");
		Assert.isTrue(ttl.compareTo(Duration.ofMillis(1)) >= 0, "有效期至少为 1 毫秒");
		return redissonClient.getBucket(key).setIfAbsent(value, ttl);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 读取指定键保存的单值。
	 * <p>示例: 读取报表结果，返回 null 时可重新查询。
	 * {@code String result = cache.getString("report:result:42");}
	 * @param <T> 单值的 Java 类型
	 * @param key 单值键
	 * @return 解码后的值，键不存在时返回 null
	 * 返回值: T
	 * 返回值说明: 解码后的值，键不存在时返回 null
	 */
	public <T> T getString(String key) {
		RBucket<T> bucket = redissonClient.getBucket(key);
		return bucket.get();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 写入哈希字段，字段已存在时覆盖原值。
	 * <p>示例: 新增或覆盖报表状态字段。
	 * {@code boolean created = cache.addToHash("report:meta:42", "status", "ready");}
	 * @param <T> 哈希字段值类型
	 * @param key 哈希键
	 * @param field 哈希字段
	 * @param value 待写入的字段值
	 * @return 新增字段时返回 true，覆盖字段时返回 false
	 * 返回值: boolean
	 * 返回值说明: 新增字段时返回 true，覆盖字段时返回 false
	 */
	public <T> boolean addToHash(String key, Object field, T value) {
		RMap<Object, T> hash = redissonClient.getMap(key);
		return hash.fastPut(field, value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 写入哈希字段后为整个哈希键设置过期时间，两步分别执行。
	 * <p>示例: 保存状态并让整份临时元数据在 10 分钟后过期。
	 * {@code cache.addToHash("report:meta:42", "status", "ready", 10, TimeUnit.MINUTES);}
	 * @param <T> 哈希字段值类型
	 * @param key 哈希键
	 * @param field 哈希字段
	 * @param value 待写入的字段值
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return 新增字段且设置过期时间成功时返回 true，覆盖字段时返回 false
	 * 返回值: boolean
	 * 返回值说明: 新增字段且设置过期时间成功时返回 true，覆盖字段时返回 false
	 */
	public <T> boolean addToHash(String key, Object field, T value, long timeout, TimeUnit timeUnit) {
		RMap<Object, T> hash = redissonClient.getMap(key);
		boolean fastPut = hash.fastPut(field, value);
		boolean expire = hash.expire(timeout, timeUnit);
		return fastPut && expire;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 读取指定哈希字段。
	 * <p>示例: 只读报表状态字段。
	 * {@code String status = cache.getFromHash("report:meta:42", "status");}
	 * @param <T> 哈希字段值类型
	 * @param key 哈希键
	 * @param field 哈希字段
	 * @return 字段值，键或字段不存在时返回 null
	 * 返回值: T
	 * 返回值说明: 字段值，键或字段不存在时返回 null
	 */
	public <T> T getFromHash(String key, Object field) {
		RMap<Object, T> hash = redissonClient.getMap(key);
		return hash.get(field);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 一次读取哈希中的全部字段和值，适合字段数量可控的哈希。
	 * <p>示例: 读取字段数可控的报表元数据。
	 * {@code Map<Object, String> meta = cache.getFromHash("report:meta:42");}
	 * @param <T> 哈希字段值类型
	 * @param key 哈希键
	 * @return 全部字段和值的快照，键不存在时返回空 Map
	 * 返回值: Map<Object, T>
	 * 返回值说明: 全部字段和值的快照，键不存在时返回空 Map
	 */
	public <T> Map<Object, T> getFromHash(String key) {
		RMap<Object, T> hash = redissonClient.getMap(key);
		return hash.readAllMap();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 替换已有哈希字段的值。
	 * <p>示例: 只更新已经存在的状态字段。
	 * {@code boolean updated = cache.updateToHash("report:meta:42", "status", "published");}
	 * @param <T> 哈希字段值类型
	 * @param key 哈希键
	 * @param field 哈希字段
	 * @param value 用于替换的字段值
	 * @return 字段存在并已替换时返回 true，否则返回 false
	 * 返回值: boolean
	 * 返回值说明: 字段存在并已替换时返回 true，否则返回 false
	 */
	public <T> boolean updateToHash(String key, Object field, T value) {
		RMap<Object, T> hash = redissonClient.getMap(key);
		return hash.fastReplace(field, value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 批量删除指定哈希字段。
	 * <p>示例: 清理两个临时字段。
	 * {@code long removed = cache.removeFromHash("report:meta:42", "debug", "preview");}
	 * @param <T> 哈希字段类型
	 * @param key 哈希键
	 * @param hashKeys 待删除的哈希字段，可传入多个
	 * @return 实际删除的字段数量
	 * 返回值: long
	 * 返回值说明: 实际删除的字段数量
	 */
	@SafeVarargs
    public final <T> long removeFromHash(String key, T... hashKeys) {
		RMap<Object, T> hash = redissonClient.getMap(key);
		return hash.fastRemove(hashKeys);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 在列表尾部追加一个元素。
	 * <p>示例: 在报表事件队尾追加一条记录。
	 * {@code cache.addToList("report:events:42", "generated");}
	 * @param <T> 集合元素类型
	 * @param key 列表键
	 * @param value 待写入的元素
	 * @return 追加成功时返回 true
	 * 返回值: boolean
	 * 返回值说明: 追加成功时返回 true
	 */
	public <T> boolean addToList(String key, T value) {
		RList<T> list = redissonClient.getList(key);
		return list.add(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 按传入顺序在列表尾部追加多个元素。
	 * <p>示例: 按发生顺序追加一组事件。
	 * {@code cache.addToList("report:events:42", List.of("generated", "published"));}
	 * @param <T> 集合元素类型
	 * @param key 列表键
	 * @param value 待写入的元素列表
	 * @return 列表发生变化时返回 true，传入空列表时返回 false
	 * 返回值: boolean
	 * 返回值说明: 列表发生变化时返回 true，传入空列表时返回 false
	 */
	public <T> boolean addToList(String key, List<T> value) {
		RList<T> list = redissonClient.getList(key);
		return list.addAll(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 在列表尾部追加元素后为整个列表键设置过期时间，两步分别执行。
	 * <p>示例: 追加事件并刷新整条事件列表的有效期。
	 * {@code cache.addToList("report:events:42", "generated", 1, TimeUnit.HOURS);}
	 * @param <T> 集合元素类型
	 * @param key 列表键
	 * @param value 待写入的元素
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return 设置过期时间成功时返回 true
	 * 返回值: boolean
	 * 返回值说明: 设置过期时间成功时返回 true
	 */
	public <T> boolean addToList(String key, T value, long timeout, TimeUnit timeUnit) {
		RList<T> list = redissonClient.getList(key);
		list.add(value);
		return list.expire(timeout, timeUnit);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 按索引读取列表区间，包含起止位置，负索引从尾部计算。
	 * <p>示例: 读取前 10 条事件。
	 * {@code List<String> first = cache.getFromList("report:events:42", 0, 9);}
	 * @param <T> 集合元素类型
	 * @param key 列表键
	 * @param start 起始索引，从 0 开始
	 * @param end 结束索引，-1 表示最后一个元素
	 * @return 按列表顺序返回区间内的元素，范围内无元素时返回空列表
	 * 返回值: List<T>
	 * 返回值说明: 按列表顺序返回区间内的元素，范围内无元素时返回空列表
	 */
	public <T> List<T> getFromList(String key, int start, int end) {
		RList<T> list = redissonClient.getList(key);
		return list.range(start, end);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 一次读取列表的全部元素，适合长度可控的列表。
	 * <p>示例: 读取长度可控的事件列表。
	 * {@code List<String> events = cache.getFromList("report:events:42");}
	 * @param <T> 集合元素类型
	 * @param key 列表键
	 * @return 按原有顺序返回全部元素，键不存在时返回空列表
	 * 返回值: List<T>
	 * 返回值说明: 按原有顺序返回全部元素，键不存在时返回空列表
	 */
	public <T> List<T> getFromList(String key) {
		RList<T> list = redissonClient.getList(key);
		return list.readAll();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 按索引移除列表的首个元素。并发消费时使用 {@link #pollListFirst(String)} 原子取出并删除。
	 * <p>示例: 已确认列表非空且允许按位置删除时，移除最早事件。
	 * {@code cache.removeListLeft("report:events:42");}
	 * @param key 列表键
	 * @return
	 * 返回值: void
	 * 返回值说明: 移除索引 0 的元素
	 */
	public void removeListLeft(String key) {
		RList<Object> list = redissonClient.getList(key);
		list.fastRemove(0);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 先读取列表长度，再按索引移除末尾元素。并发消费时使用 {@link #pollListLast(String)}。
	 * <p>示例: 已确认列表非空且已协调并发访问时，移除最后一条事件。
	 * {@code cache.removeListRight("report:events:42");}
	 * @param key 列表键
	 * @return
	 * 返回值: void
	 * 返回值说明: 移除读取长度时对应的末尾位置元素
	 */
	public void removeListRight(String key) {
		RList<Object> list = redissonClient.getList(key);
		list.fastRemove(list.size() - 1);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 原子取出并删除列表首个元素，适合多个消费者按先进先出领取可丢失的临时数据。
	 * <p>示例: {@code String event = cache.pollListFirst("report:events:42");}
	 * 消费后需要确认、重试的任务应使用支持确认的消息机制。
	 * @param <T> 列表元素类型
	 * @param key 列表键，不能为空或空白
	 * @return 被取出的元素，列表为空或不存在时返回 null
	 * 返回值: T
	 * 返回值说明: 返回并删除队首元素，方法立即返回
	 * @throws IllegalArgumentException 键为空或空白
	 */
	public <T> T pollListFirst(String key) {
		Assert.hasText(key, "Redis 键不能为空");
		return redissonClient.<T>getDeque(key).pollFirst();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 原子取出并删除列表末尾元素，适合优先取出最新加入的临时记录。
	 * <p>示例: {@code String event = cache.pollListLast("report:events:42");}
	 * @param <T> 列表元素类型
	 * @param key 列表键，不能为空或空白
	 * @return 被取出的元素，列表为空或不存在时返回 null
	 * 返回值: T
	 * 返回值说明: 返回并删除队尾元素，方法立即返回
	 * @throws IllegalArgumentException 键为空或空白
	 */
	public <T> T pollListLast(String key) {
		Assert.hasText(key, "Redis 键不能为空");
		return redissonClient.<T>getDeque(key).pollLast();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 按索引移除指定位置的元素。
	 * <p>示例: 移除下标为 1 的第二条事件。
	 * {@code cache.removeFromList("report:events:42", 1);}
	 * @param key 列表键
	 * @param index 待移除元素的索引，从 0 开始
	 * @return
	 * 返回值: void
	 * 返回值说明: 移除指定位置的元素
	 */
	public void removeFromList(String key, int index) {
		RList<Object> list = redissonClient.getList(key);
		list.fastRemove(index);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 遍历列表并移除与指定值相等的所有元素。
	 * <p>示例: 移除所有内容为 draft 的事件。
	 * {@code boolean removed = cache.removeFromList("report:events:42", "draft");}
	 * @param <T> 集合元素类型
	 * @param key 列表键
	 * @param value 待移除的元素
	 * @return 至少移除一个匹配元素时返回 true，否则返回 false
	 * 返回值: boolean
	 * 返回值说明: 至少移除一个匹配元素时返回 true，否则返回 false
	 */
	public <T> boolean removeFromList(String key, T value) {
		RList<T> list = redissonClient.getList(key);
		return list.removeIf(o -> o.equals(value));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 向集合添加单个元素，集合自动去重。
	 * <p>示例: 给报表添加唯一标签。
	 * {@code boolean added = cache.addToSet("report:tags:42", "finance");}
	 * @param <T> 集合元素类型
	 * @param key 集合键
	 * @param value 待写入的成员
	 * @return 新增成员时返回 true，成员已存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 新增成员时返回 true，成员已存在时返回 false
	 */
	public <T> boolean addToSet(String key, T value) {
		RSet<T> set = redissonClient.getSet(key);
		return set.add(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 添加元素后为整个集合键设置过期时间，两步分别执行，成员已存在时也会尝试设置过期时间。
	 * <p>示例: 为临时报表添加标签并设置集合键有效期。
	 * {@code cache.addToSet("report:tags:42", "finance", 30, TimeUnit.MINUTES);}
	 * @param <T> 集合元素类型
	 * @param key 集合键
	 * @param value 待写入的成员
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return 新增成员且设置过期时间成功时返回 true
	 * 返回值: boolean
	 * 返回值说明: 新增成员且设置过期时间成功时返回 true
	 */
	public <T> boolean addToSet(String key, T value, long timeout, TimeUnit timeUnit) {
		RSet<T> set = redissonClient.getSet(key);
		boolean add = set.add(value);
		boolean expire = set.expire(timeout, timeUnit);
		return add && expire;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 批量向集合添加元素，集合自动去重。
	 * <p>示例: 批量补充报表标签并去重。
	 * {@code cache.addToSet("report:tags:42", List.of("finance", "daily"));}
	 * @param <T> 集合元素类型
	 * @param key 集合键
	 * @param values 待写入的元素列表
	 * @return 至少新增一个成员时返回 true，否则返回 false
	 * 返回值: boolean
	 * 返回值说明: 至少新增一个成员时返回 true，否则返回 false
	 */
	public <T> boolean addToSet(String key, List<T> values) {
		RSet<T> set = redissonClient.getSet(key);
		return set.addAll(values);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 批量添加成员后为整个集合键设置过期时间，两步分别执行。
	 * 传入空列表时只尝试给已有集合设置过期时间。
	 * <p>示例: 为临时报表批量添加标签，集合在 30 分钟后过期。
	 * {@code cache.addToSet("report:tags:42", List.of("finance", "daily"), 30, TimeUnit.MINUTES);}
	 * @param <T> 集合元素类型
	 * @param key 集合键，不能为空或空白
	 * @param values 待写入的元素列表，允许为空列表
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return 设置过期时间成功时返回 true
	 * 返回值: boolean
	 * 返回值说明: 是否设置了过期时间，全部成员已存在时仍可能返回 true
	 * @throws IllegalArgumentException 键为空白、列表或时间单位为 null
	 */
	public <T> boolean addToSet(String key, List<T> values, long timeout, TimeUnit timeUnit) {
		Assert.hasText(key, "Redis 键不能为空");
		Assert.notNull(values, "集合成员列表不能为空");
		Assert.notNull(timeUnit, "时间单位不能为空");
		RSet<T> set = redissonClient.getSet(key);
		set.addAll(values);
		return set.expire(timeout, timeUnit);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 查询集合是否包含指定成员，适合只检查单个标签或成员资格。
	 * <p>示例: {@code boolean tagged = cache.containsInSet("report:tags:42", "finance");}
	 * @param key 集合键，不能为空或空白
	 * @param value 待检查的成员，不能为空，类型应与写入时一致
	 * @return 成员存在时返回 true，集合键或成员不存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否包含指定成员
	 * @throws IllegalArgumentException 键为空白或成员为 null
	 */
	public boolean containsInSet(String key, Object value) {
		Assert.hasText(key, "Redis 键不能为空");
		Assert.notNull(value, "集合成员不能为空");
		return redissonClient.getSet(key).contains(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 一次读取集合的全部成员，结果没有固定顺序。
	 * <p>示例: 读取单份报表的全部标签。
	 * {@code Set<String> tags = cache.getFromSet("report:tags:42");}
	 * @param <T> 集合元素类型
	 * @param key 集合键
	 * @return 全部成员的快照，键不存在时返回空集合
	 * 返回值: Set<T>
	 * 返回值说明: 全部成员的快照，键不存在时返回空集合
	 */
	public <T> Set<T> getFromSet(String key) {
		RSet<T> set = redissonClient.getSet(key);
		return set.readAll();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 逐一移除指定集合成员，适合数量可控的成员列表。
	 * <p>示例: 批量清理过时标签。
	 * {@code cache.removeFromSet("report:tags:42", List.of("draft", "preview"));}
	 * @param <T> 集合元素类型
	 * @param key 集合键
	 * @param values 待移除的元素列表
	 * @return
	 * 返回值: void
	 * 返回值说明: 移除已存在的目标成员
	 */
	public <T> void removeFromSet(String key, List<T> values) {
		RSet<T> set = redissonClient.getSet(key);
		values.forEach(set::remove);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 移除一个指定集合成员。
	 * <p>示例: 取消一个标签。
	 * {@code boolean removed = cache.removeFromSet("report:tags:42", "draft");}
	 * @param <T> 集合元素类型
	 * @param key 集合键
	 * @param value 待移除的成员
	 * @return 成员已移除时返回 true，成员不存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 成员已移除时返回 true，成员不存在时返回 false
	 */
	public <T> boolean removeFromSet(String key, T value) {
		RSet<T> set = redissonClient.getSet(key);
		return set.remove(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 添加有序集合成员，成员已存在时更新分值。
	 * <p>示例: 为报表记录处理优先级分值。
	 * {@code cache.addToZSet("report:priority", "report-42", 10.0);}
	 * @param <T> 集合元素类型
	 * @param key 有序集合键
	 * @param value 待写入的成员
	 * @param score 成员用于排序的分值
	 * @return
	 * 返回值: void
	 * 返回值说明: 写入成员及其分值
	 */
	public <T> void addToZSet(String key, T value, double score) {
		RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
		sortedSet.add(score, value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 写入成员分值后为整个有序集合键设置过期时间，两步分别执行。
	 * <p>示例: 写入临时优先级并设置整个有序集合的有效期。
	 * {@code cache.addToZSet("report:priority", "report-42", 10.0, 1, TimeUnit.HOURS);}
	 * @param <T> 集合元素类型
	 * @param key 有序集合键
	 * @param value 待写入的成员
	 * @param score 成员用于排序的分值
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return
	 * 返回值: void
	 * 返回值说明: 写入成员分值并尝试设置键的有效期
	 */
	public <T> void addToZSet(String key, T value, double score, long timeout, TimeUnit timeUnit) {
		RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
		sortedSet.add(score, value);
		sortedSet.expire(timeout, timeUnit);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 按分值升序对应的排名读取指定区间，包含起止排名。
	 * <p>示例: 取分值最低的 10 个成员，用于批量处理。
	 * {@code Set<Object> candidates = cache.getFromZSet("report:priority", 0, 9);}
	 * @param <T> 集合元素类型
	 * @param key 有序集合键
	 * @param start 起始排名，从 0 开始
	 * @param end 结束排名，-1 表示最后一个元素
	 * @return 区间内的成员，返回集合不保留排名顺序，无匹配成员时返回空集合
	 * 返回值: Set<Object>
	 * 返回值说明: 区间内的成员，返回集合不保留排名顺序，无匹配成员时返回空集合
	 */
	public <T> Set<Object> getFromZSet(String key, int start, int end) {
		RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
		return new HashSet<>(sortedSet.valueRange(start, end));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 批量移除有序集合中的指定成员。
	 * <p>示例: 批量移除已处理的报表成员。
	 * {@code cache.removeFromZSet("report:priority", List.of("report-42", "report-43"));}
	 * @param <T> 集合元素类型
	 * @param key 有序集合键
	 * @param values 待移除的元素列表
	 * @return
	 * 返回值: void
	 * 返回值说明: 移除已存在的目标成员
	 */
	public <T> void removeFromZSet(String key, List<T> values) {
		RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
		sortedSet.removeAll(values);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 移除有序集合中的指定成员。
	 * <p>示例: 移除一份已取消的报表。
	 * {@code cache.removeFromZSet("report:priority", "report-42");}
	 * @param <T> 集合元素类型
	 * @param key 有序集合键
	 * @param value 待移除的成员
	 * @return
	 * 返回值: void
	 * 返回值说明: 移除目标成员
	 */
	public <T> void removeFromZSet(String key, T value) {
		RScoredSortedSet<T> sortedSet = redissonClient.getScoredSortedSet(key);
		sortedSet.remove(value);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 检查 Redis 键是否存在。
	 * <p>示例: 查询某份报表的缓存是否存在。
	 * {@code boolean cached = cache.exists("report:result:42");}
	 * @param key Redis 键
	 * @return 键存在时返回 true，否则返回 false
	 * 返回值: boolean
	 * 返回值说明: 键存在时返回 true，否则返回 false
	 */
	public boolean exists(String key) {
		return redissonClient.getBucket(key).isExists();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 删除整个 Redis 键及其关联数据。
	 * <p>示例: 在报表数据更新后使结果缓存失效。
	 * {@code boolean removed = cache.remove("report:result:42");}
	 * @param key Redis 键
	 * @return 实际删除键时返回 true，键不存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 实际删除键时返回 true，键不存在时返回 false
	 */
	public boolean remove(String key) {
		long delete = redissonClient.getKeys().delete(key);
		return delete > 0;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 为已存在的 Redis 键设置过期时间。
	 * <p>示例: 为已存在的查询结果重新设置 5 分钟有效期。
	 * {@code boolean updated = cache.expire("report:result:42", 5, TimeUnit.MINUTES);}
	 * @param key Redis 键
	 * @param timeout 键的过期时长
	 * @param timeUnit 过期时长的单位
	 * @return 设置过期时间成功时返回 true，键不存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 设置过期时间成功时返回 true，键不存在时返回 false
	 */
	public boolean expire(String key, long timeout, TimeUnit timeUnit) {
		return redissonClient.getBucket(key).expire(timeout, timeUnit);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 查询 Redis 键的剩余有效期。
	 * <p>示例: 读取剩余毫秒数，供调用方判断是否需要刷新缓存。
	 * {@code long remainingMs = cache.getExpire("report:result:42");}
	 * @param key 待查询的 Redis 键
	 * @return 剩余有效期（毫秒）或 Redis 状态值
	 * 返回值: Long
	 * 返回值说明: 大于等于 0 表示剩余毫秒数，-1 表示键存在但未设置过期时间，-2 表示键不存在。
	 */
	public Long getExpire(String key) {
		return redissonClient.getKeys().remainTimeToLive(key);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:06
	 * 说明: 清除键的过期时间，适合把临时缓存转为长期保留的数据。
	 * <p>示例: {@code boolean cleared = cache.clearExpire("report:config:snapshot");}
	 * @param key 待清除过期时间的 Redis 键，不能为空或空白
	 * @return 已清除过期时间时返回 true，键不存在或没有过期时间时返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否移除了该键已有的过期时间
	 * @throws IllegalArgumentException 键为空或空白
	 */
	public boolean clearExpire(String key) {
		Assert.hasText(key, "Redis 键不能为空");
		return redissonClient.getKeys().clearExpire(key);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 对整数计数器原子加上指定增量，键不存在时从 0 开始。
	 * <p>示例: 给查看次数加一。
	 * {@code long views = cache.increment("report:views:42", 1L);}
	 * @param key 计数器键
	 * @param delta 计数增量，可为负数
	 * @return 处理后的整数计数值
	 * 返回值: long
	 * 返回值说明: 处理后的整数计数值
	 */
	public long increment(String key, long delta) {
		RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
		return atomicLong.addAndGet(delta);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 对整数计数器原子减 1，键不存在时从 0 开始。按指定数量扣减可向 {@link #increment(String, long)} 传负数。
	 * <p>示例: 把报表的待处理数量减一。
	 * {@code long pending = cache.decrement("report:pending:42", 1L);}
	 * @param key 计数器键
	 * @param delta 保留参数，当前实现未读取该参数
	 * @return 处理后的整数计数值
	 * 返回值: long
	 * 返回值说明: 处理后的整数计数值
	 */
	public long decrement(String key, long delta) {
		RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
		return atomicLong.decrementAndGet();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 对浮点数计数器原子加上指定增量，键不存在时从 0 开始。
	 * <p>示例: 为累计处理时长增加 0.5 秒。
	 * {@code double seconds = cache.increment("report:total-seconds:42", 0.5);}
	 * @param key 计数器键
	 * @param delta 计数增量，可为负数
	 * @return 处理后的浮点数计数值
	 * 返回值: double
	 * 返回值说明: 处理后的浮点数计数值
	 */
	public double increment(String key, double delta) {
		RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(key);
		return atomicDouble.addAndGet(delta);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 对浮点数计数器原子减 1，键不存在时从 0 开始。按指定数量扣减可向 {@link #increment(String, double)} 传负数。
	 * <p>示例: 把浮点累计值减 1.0。
	 * {@code double seconds = cache.decrement("report:total-seconds:42", 1.0);}
	 * @param key 计数器键
	 * @param delta 保留参数，当前实现未读取该参数
	 * @return 处理后的浮点数计数值
	 * 返回值: double
	 * 返回值说明: 处理后的浮点数计数值
	 */
	public double decrement(String key, double delta) {
		RAtomicDouble atomicDouble = redissonClient.getAtomicDouble(key);
		return atomicDouble.decrementAndGet();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 读取整数计数器的当前值，适合展示次数或累计数量。
	 * <p>示例: {@code long views = cache.getLongCounter("report:views:42");}
	 * @param key 整数计数器键，不能为空或空白
	 * @return 当前值，键不存在时返回 0
	 * 返回值: long
	 * 返回值说明: 此次读取的计数快照
	 * @throws IllegalArgumentException 键为空或空白
	 */
	public long getLongCounter(String key) {
		Assert.hasText(key, "Redis 键不能为空");
		return redissonClient.getAtomicLong(key).get();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:06
	 * 说明: 获取可重入互斥锁，适合同一报表只允许一个线程生成或刷新。
	 * {@code tryLock(waitTime, unit)} 最多等待指定时间，持锁期间由 Redisson 看门狗续期。
	 * 指定 {@code leaseTime} 时锁到期自动释放，需确保业务能在租期内完成。
	 * <pre>{@code
	 * RLock lock = cache.getLock("report:lock:generate:42");
	 * if (lock.tryLock(2, TimeUnit.SECONDS)) {
	 *     try {
	 *         generateReport();
	 *     } finally {
	 *         lock.unlock();
	 *     }
	 * }
	 * }</pre>
	 * 示例所在方法可向上抛出 {@link InterruptedException}，捕获时应恢复线程中断状态。
	 * @param name 业务锁名称，不能为空或空白
	 * @return 尚未加锁的可重入锁对象
	 * 返回值: RLock
	 * 返回值说明: 成功加锁后由同一线程在 finally 中释放
	 * @throws IllegalArgumentException 锁名称为空或空白
	 */
	public RLock getLock(String name) {
		Assert.hasText(name, "锁名称不能为空");
		return redissonClient.getLock(name);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:10
	 * 说明: 在指定等待时间内尝试持普通锁同步执行操作，退出时释放本次取得的锁。
	 * 看门狗负责持锁续期。等待时间只限制加锁阶段，操作自身的耗时由业务控制。
	 * <p>示例: {@code boolean done = cache.tryWithLock("report:lock:generate:42", 2, TimeUnit.SECONDS, this::generateReport);}
	 * 返回 false 时调用方可提示任务正在处理。Redis 异常或操作异常向上抛出。
	 * @param name 业务锁名称，不能为空或空白
	 * @param waitTime 最大等待时间，0 表示立即尝试
	 * @param unit 等待时间单位，不能为空
	 * @param action 成功取得锁后在当前线程执行的操作，不能为空
	 * @return 操作正常执行并释放锁时返回 true，等待结束仍未取得锁时返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否完成了受锁保护的操作
	 * @throws InterruptedException 等待加锁时当前线程被中断
	 * @throws IllegalArgumentException 锁名称为空白、等待时间为负、时间单位或操作为 null
	 */
	public boolean tryWithLock(String name, long waitTime, TimeUnit unit, Runnable action) throws InterruptedException {
		Assert.isTrue(waitTime >= 0, "等待时间不能为负数");
		Assert.notNull(unit, "时间单位不能为空");
		Assert.notNull(action, "受锁保护的操作不能为空");
		RLock lock = getLock(name);
		if (!lock.tryLock(waitTime, unit)) {
			return false;
		}
		try {
			action.run();
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:06
	 * 说明: 获取按请求顺序分配的可重入锁，适合需要排队更新同一资源的场景。
	 * <pre>{@code
	 * RLock lock = cache.getFairLock("report:lock:publish:42");
	 * if (lock.tryLock(3, TimeUnit.SECONDS)) {
	 *     try {
	 *         publishReport();
	 *     } finally {
	 *         lock.unlock();
	 *     }
	 * }
	 * }</pre>
	 * @param name 业务锁名称，不能为空或空白
	 * @return 尚未加锁的公平锁对象
	 * 返回值: RLock
	 * 返回值说明: 成功加锁后由同一线程在 finally 中释放
	 * @throws IllegalArgumentException 锁名称为空或空白
	 */
	public RLock getFairLock(String name) {
		Assert.hasText(name, "锁名称不能为空");
		return redissonClient.getFairLock(name);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:06
	 * 说明: 获取读写锁，适合读多写少且读写需要互斥的场景。多个读线程可以共享读锁，写线程独占。
	 * 同一资源的读写使用相同锁名称，避免持有读锁时直接申请写锁。
	 * <pre>{@code
	 * RReadWriteLock lock = cache.getReadWriteLock("report:lock:snapshot:42");
	 * RLock readLock = lock.readLock();
	 * readLock.lock();
	 * try {
	 *     readSnapshot();
	 * } finally {
	 *     readLock.unlock();
	 * }
	 *
	 * RLock writeLock = lock.writeLock();
	 * writeLock.lock();
	 * try {
	 *     replaceSnapshot();
	 * } finally {
	 *     writeLock.unlock();
	 * }
	 * }</pre>
	 * @param name 读写双方共用的业务锁名称，不能为空或空白
	 * @return 提供读锁和写锁的对象，获取对象时尚未加锁
	 * 返回值: RReadWriteLock
	 * 返回值说明: 通过 readLock 或 writeLock 获取相应的锁，成功加锁后由同一线程释放
	 * @throws IllegalArgumentException 锁名称为空或空白
	 */
	public RReadWriteLock getReadWriteLock(String name) {
		Assert.hasText(name, "锁名称不能为空");
		return redissonClient.getReadWriteLock(name);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:14
	 * 说明: 首次初始化分布式限流，同一 Redis 库中使用同一个限流键的客户端共享配额。
	 * 规则已存在时返回 false，保留原有规则和已消耗的许可。
	 * <p>示例: 将所有报表实例调用同一外部接口的频率限制为每秒 10 次。
	 * {@code cache.tryInitRateLimiter("report:rate:vendor-api", 10, Duration.ofSeconds(1));}
	 * @param key 限流键，不能为空或空白
	 * @param permits 每个时间窗口的许可数量，必须大于 0
	 * @param interval 时间窗口，至少 1 毫秒
	 * @return 首次设置成功时返回 true，已存在规则时返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否初始化了共享配额
	 * @throws IllegalArgumentException 限流键为空白、许可数量非正或时间窗口不足 1 毫秒
	 */
	public boolean tryInitRateLimiter(String key, long permits, Duration interval) {
		validateRateRule(key, permits, interval);
		return redissonClient.getRateLimiter(key).trySetRate(RateType.OVERALL, permits, interval);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:23
	 * 说明: 仅在共享限流规则的许可数量或毫秒窗口变化时更新，保留仍在新窗口内的许可消耗记录。
	 * 窗口缩短时，已超出新窗口的记录会过期。窗口延长时沿用 Redis 中尚存的记录。
	 * 降额后，只有仍有效的消耗加上本次申请未超过新配额，才会放行。
	 * <p>示例: 在外部接口配额调整后，将规则改为每 2 秒 20 个许可。
	 * {@code boolean changed = cache.updateRateLimiter("report:rate:vendor-api", 20, Duration.ofSeconds(2));}
	 * 规则不存在时，通过 {@link #tryInitRateLimiter(String, long, Duration)} 单独初始化。
	 * @param key 通过本工具初始化的共享限流键，不能为空或空白
	 * @param permits 每个时间窗口的许可数量，必须大于 0
	 * @param interval 新时间窗口，至少 1 毫秒，按毫秒比较
	 * @return 更新成功时返回 true，配额和窗口均相同或规则不存在时返回 false
	 * 返回值: boolean
	 * 返回值说明: 本次调用是否修改了共享限流规则
	 * @throws IllegalArgumentException 限流键为空白、许可数量非正或时间窗口为空或不足 1 毫秒
	 * @throws IllegalStateException 已有规则使用非共享限流模式
	 */
	public boolean updateRateLimiter(String key, long permits, Duration interval) {
		validateRateRule(key, permits, interval);
		return RedissonRateRuleUpdate.update(redissonClient, key, permits, interval.toMillis());
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:20
	 * 说明: 统一校验限流规则初始化和更新的输入，避免将无效配置写入 Redis。
	 * @param key 限流键，不能为空或空白
	 * @param permits 窗口许可数量，必须大于 0
	 * @param interval 时间窗口，至少 1 毫秒
	 */
	private void validateRateRule(String key, long permits, Duration interval) {
		Assert.hasText(key, "限流键不能为空");
		Assert.isTrue(permits > 0, "许可数量必须大于 0");
		Assert.notNull(interval, "限流时间窗口不能为空");
		Assert.isTrue(interval.compareTo(Duration.ofMillis(1)) >= 0, "限流时间窗口至少为 1 毫秒");
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:14
	 * 说明: 立即申请指定数量的许可，适合配额不足时需要立即返回繁忙的接口。
	 * 调用前应初始化规则。获准后配额会按时间窗口恢复，业务结束时无需归还。
	 * <p>示例: {@code boolean allowed = cache.tryAcquirePermits("report:rate:vendor-api", 1);}
	 * 返回 false 时可以提示稍后重试。
	 * @param key 已初始化的限流键，不能为空或空白
	 * @param permits 本次申请的许可数量，必须大于 0 且不能超过规则配额
	 * @return 当前配额足以一次满足本次申请时返回 true，否则返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否立即取得全部许可。规则缺失、申请超过规则配额或 Redis 异常时，Redisson 会抛出异常
	 * @throws IllegalArgumentException 限流键为空白或许可数量非正
	 */
	public boolean tryAcquirePermits(String key, long permits) {
		Assert.hasText(key, "限流键不能为空");
		Assert.isTrue(permits > 0, "许可数量必须大于 0");
		return redissonClient.getRateLimiter(key).tryAcquire(permits);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:14
	 * 说明: 在指定时间内等待全部许可，适合允许短暂排队的后台任务。
	 * 调用前应初始化规则。等待时间仅用于申请许可。
	 * <p>示例: {@code boolean allowed = cache.tryAcquirePermits("report:rate:vendor-api", 2, Duration.ofMillis(300));}
	 * 等待超时后可交由调用方的任务重试策略处理。
	 * @param key 已初始化的限流键，不能为空或空白
	 * @param permits 本次申请的许可数量，必须大于 0 且不能超过规则配额
	 * @param timeout 等待时间，不能为空或为负，零表示立即尝试
	 * @return 在等待时间内取得全部许可时返回 true，超时返回 false
	 * 返回值: boolean
	 * 返回值说明: 是否取得全部许可。规则缺失、申请超过规则配额或 Redis 异常时，Redisson 会抛出异常
	 * @throws IllegalArgumentException 限流键为空白、许可数量非正或等待时间为空或为负
	 */
	public boolean tryAcquirePermits(String key, long permits, Duration timeout) {
		Assert.hasText(key, "限流键不能为空");
		Assert.isTrue(permits > 0, "许可数量必须大于 0");
		Assert.notNull(timeout, "等待时间不能为空");
		Assert.isTrue(!timeout.isNegative(), "等待时间不能为负数");
		return redissonClient.getRateLimiter(key).tryAcquire(permits, timeout);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/19 15:07
	 * 说明: 创建 Redisson 事务，由调用方通过事务对象操作 Redis 并负责提交或回滚。
	 * <p>示例: 在一次 Redis 事务中更新报表状态与版本，数据库事务需要单独管理。
	 * <pre>{@code
	 * RTransaction tx = cache.getTransaction();
	 * try {
	 *     tx.<String>getBucket("report:tx:status:42").set("ready");
	 *     tx.<String>getBucket("report:tx:version:42").set("v2");
	 * } catch (RuntimeException ex) {
	 *     tx.rollback();
	 *     throw ex;
	 * }
	 * tx.commit();
	 * }</pre>
	 * @return 事务对象，事务超时为 5 秒
	 * 返回值: RTransaction
	 * 返回值说明: 事务对象，事务超时为 5 秒
	 */
	public RTransaction getTransaction() {
		TransactionOptions options = TransactionOptions.defaults()
                .syncSlavesTimeout(5, TimeUnit.SECONDS)
                .responseTimeout(3, TimeUnit.SECONDS)
                .retryInterval(2, TimeUnit.SECONDS)
                .retryAttempts(3)
                .timeout(5, TimeUnit.SECONDS);
        return redissonClient.createTransaction(options);
    }

}
