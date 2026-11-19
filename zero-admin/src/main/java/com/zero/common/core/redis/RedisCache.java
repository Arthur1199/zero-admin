package com.zero.common.core.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: spring redis 工具类
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    /**
     * RedisTemplate字段
     */
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将对象写入 Redis，当前重载不显式设置过期时间。
     * @param <T> 缓存对象类型
     * @param key Redis 缓存键
     * @param value 待写入的值
     * @return
     * 返回值: void
     * 返回值说明: 写入指定键
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将对象和对应的有效期写入 Redis。
     * @param <T> 缓存对象类型
     * @param key Redis 缓存键
     * @param value 待写入的值
     * @param timeout 缓存有效时长
     * @param timeUnit 有效时长的单位
     * @return
     * 返回值: void
     * 返回值说明: 写入缓存及其过期时间
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置有效时间
     * @param key Redis键
     * @param timeout 超时时间
     * @return
     * 返回值: true=设置成功；false=设置失败
     * 返回值说明：设置有效时间
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置有效时间
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return
     * 返回值: true=设置成功；false=设置失败
     * 返回值说明：设置有效时间
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取有效时间
     * @param key Redis键
     * @return
     * 返回值: 有效时间
     * 返回值说明：获取有效时间
     */
    public long getExpire(final String key)
    {
        return redisTemplate.getExpire(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断 key是否存在
     * @param key 键
     * @return
     * 返回值: true 存在 false不存在
     * 返回值说明：判断 key是否存在
     */
    public Boolean hasKey(String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获得缓存的基本对象。
     * @param <T> 当前方法处理的值类型
     * @param key 缓存键值
     * @return
     * 返回值: T
     * 返回值说明: 获得缓存的基本对象。
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 递增缓存数字
     * @param key 缓存键
     * @return
     * 返回值: Long
     * 返回值说明: 递增后的值
     */
    public Long incrementCacheObject(final String key)
    {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除单个对象
     * @param key 键名参数
     * @return
     * 返回值: boolean
     * 返回值说明：删除单个对象
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除集合对象
     * @param collection 多个对象
     * @return
     * 返回值: boolean
     * 返回值说明：删除集合对象
     */
    public boolean deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 缓存List数据
     * @param <T> 当前方法处理的值类型
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return
     * 返回值: long
     * 返回值说明: 缓存List数据
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获得缓存的list对象
     * @param <T> 当前方法处理的值类型
     * @param key 缓存的键值
     * @return
     * 返回值: List<T>
     * 返回值说明: 获得缓存的list对象
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 缓存Set
     * @param <T> 当前方法处理的值类型
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return
     * 返回值: BoundSetOperations<String, T>
     * 返回值说明: 缓存Set
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获得缓存的set
     * @param <T> 当前方法处理的值类型
     * @param key 键名参数
     * @return
     * 返回值: Set<T>
     * 返回值说明: 获得缓存的set
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将给定映射批量写入 Redis Hash。
     * @param <T> Hash 字段值类型
     * @param key Redis Hash 键
     * @param dataMap 待写入的字段和值
     * @return
     * 返回值: void
     * 返回值说明: 合并写入提供的字段
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获得缓存的Map
     * @param <T> 当前方法处理的值类型
     * @param key 键名参数
     * @return
     * 返回值: Map<String, T>
     * 返回值说明: 获得缓存的Map
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置 Redis Hash 中的单个字段。
     * @param <T> Hash 字段值类型
     * @param key Redis Hash 键
     * @param hKey Hash 字段名
     * @param value 待写入的字段值
     * @return
     * 返回值: void
     * 返回值说明: 写入或替换指定字段
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取Hash中的数据
     * @param <T> 当前方法处理的值类型
     * @param key Redis键
     * @param hKey Hash键
     * @return
     * 返回值: T
     * 返回值说明: 获取Hash中的数据
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取多个Hash中的数据
     * @param <T> 当前方法处理的值类型
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return
     * 返回值: List<T>
     * 返回值说明: 获取多个Hash中的数据
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除Hash中的某条数据
     * @param key Redis键
     * @param hKey Hash键
     * @return
     * 返回值: 是否成功
     * 返回值说明：删除Hash中的某条数据
     */
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获得缓存的基本对象列表
     * @param pattern 字符串前缀
     * @return
     * 返回值: 对象列表
     * 返回值说明：获得缓存的基本对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }
}
