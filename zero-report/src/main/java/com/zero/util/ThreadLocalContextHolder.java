package com.zero.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 保存报表服务当前线程的临时数据。在线程池复用线程前清理上下文，避免后续任务读到旧数据。
 */
public class ThreadLocalContextHolder {

    /** 每个线程的上下文映射，首次访问时创建。 */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 给当前线程写入一项上下文，空值用空字符串存储。
     * @param key 上下文键
     * @param value 待写入的值
     * @return
     * 返回值: void
     * 返回值说明: 同名键会替换已有值
     */
    public static void set(String key, Object value)
    {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? "" : value);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按字符串读取当前线程的上下文。
     * @param key 上下文键
     * @return
     * 返回值: String
     * 返回值说明: 值的字符串表示，键不存在时返回空字符串
     */
    public static String get(String key)
    {
        Map<String, Object> map = getLocalMap();
        Object orDefault = map.getOrDefault(key, "");
        return orDefault.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取当前线程保存的对象，类型由调用方保证。
     * @param <T> 调用方期望的值类型
     * @param key 上下文键
     * @param clazz 调用方声明的类型，用于泛型推断
     * @return
     * 返回值: T
     * 返回值说明: 保存的对象，键不存在时返回 null
     */
    @SuppressWarnings("unchecked")
	public static <T> T get(String key, Class<T> clazz)
    {
        Map<String, Object> map = getLocalMap();
        Object orDefault = map.getOrDefault(key, null);
        return (T) orDefault;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 取当前线程上下文，首次访问时创建映射。
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: 当前线程保存的映射，可直接读写
     */
    public static Map<String, Object> getLocalMap()
    {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null)
        {
            map = new ConcurrentHashMap<String, Object>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为当前线程绑定调用方提供的映射。
     * @param threadLocalMap 要绑定的上下文映射
     * @return
     * 返回值: void
     * 返回值说明: 直接保存该映射的引用
     */
    public static void setLocalMap(Map<String, Object> threadLocalMap)
    {
        THREAD_LOCAL.set(threadLocalMap);
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解除当前线程的上下文绑定。
     * @return
     * 返回值: void
     * 返回值说明: 线程复用前释放上次任务的上下文
     */
    public static void remove()
    {
        THREAD_LOCAL.remove();
    }
}
