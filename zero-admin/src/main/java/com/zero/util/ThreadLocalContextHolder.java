package com.zero.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 作者: Jack
 * 时间: 2022年3月15日下午3:43:30
 * 说明: 线程池中共享数据工具
 *
 */
public class ThreadLocalContextHolder {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(String key, Object value)
    {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? "" : value);
    }

    public static String get(String key)
    {
        Map<String, Object> map = getLocalMap();
        Object orDefault = map.getOrDefault(key, "");
        return orDefault.toString();
    }

    @SuppressWarnings("unchecked")
	public static <T> T get(String key, Class<T> clazz)
    {
        Map<String, Object> map = getLocalMap();
        Object orDefault = map.getOrDefault(key, null);
        return (T) orDefault;
    }

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

    public static void setLocalMap(Map<String, Object> threadLocalMap)
    {
        THREAD_LOCAL.set(threadLocalMap);
    }


    public static void remove()
    {
        THREAD_LOCAL.remove();
    }
}
