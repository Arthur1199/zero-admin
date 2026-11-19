package com.zero.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 保存管理后台当前线程的临时数据。在线程池复用线程前清理上下文，避免后续任务读到旧数据。
 */
public class ThreadLocalContextHolder {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在当前线程存入一项上下文。
     * @param key 上下文键
     * @param value 要保存的值，可为空
     * @return
     * 返回值: void
     * 返回值说明: 同名键覆盖原值，空值以空字符串保存
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
     * 说明: 读取指定上下文项并转换成字符串。
     * @param key 上下文键
     * @return
     * 返回值: String
     * 返回值说明: 值的字符串表示，未保存该键时返回空字符串
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
     * 说明: 按调用方期望的类型读取当前线程保存的值。
     * @param <T> 调用方期望的类型
     * @param key 上下文键
     * @param clazz 用于泛型推断的类型声明
     * @return
     * 返回值: T
     * 返回值说明: 上下文项，未保存时返回 null
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
     * 说明: 访问当前线程的上下文，首次访问时创建映射。
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: 当前线程持有的映射
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
     * 说明: 将调用方提供的上下文映射绑定到当前线程。
     * @param threadLocalMap 要绑定的上下文映射
     * @return
     * 返回值: void
     * 返回值说明: 后续访问直接使用所传映射的引用
     */
    public static void setLocalMap(Map<String, Object> threadLocalMap)
    {
        THREAD_LOCAL.set(threadLocalMap);
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解除当前线程与上下文映射的绑定。
     * @return
     * 返回值: void
     * 返回值说明: 线程池复用线程时不再读到本次操作留下的上下文
     */
    public static void remove()
    {
        THREAD_LOCAL.remove();
    }
}
