package com.zero.utils.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:55
 * 说明: 本地内存缓存工具类，支持按key存取对象和按秒设置过期时间
 */
public class LocalCacheUtil {

    /**
     * 本地缓存容器，value包装缓存值和对应过期清理任务
     */
    private static final Map<String, Entity> MAP = new HashMap<>();

    /**
     * 过期缓存清理线程池，使用守护线程避免工具类阻塞应用退出
     */
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "local-cache-cleaner");
        thread.setDaemon(true);
        return thread;
    });

    private LocalCacheUtil() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 写入不过期的本地缓存
     * @param key 缓存键
     * @param data 缓存值，可为空
     * 返回值: void
     * 返回值说明: 写入或覆盖指定key的缓存值
     */
    public synchronized static void put(String key, Object data) {
        LocalCacheUtil.put(key, data, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 写入本地缓存，并按秒设置过期时间
     * @param key 缓存键
     * @param data 缓存值，可为空
     * @param expireSeconds 过期时间，单位秒；小于等于0表示不过期
     * 返回值: void
     * 返回值说明: 写入或覆盖指定key的缓存值，旧值的过期任务会被取消
     */
    public synchronized static void put(String key, Object data, long expireSeconds) {
        LocalCacheUtil.remove(key);
        if (expireSeconds > 0) {
            Future<?> future = EXECUTOR.schedule(() -> {
                synchronized (LocalCacheUtil.class) {
                    MAP.remove(key);
                }
            }, expireSeconds, TimeUnit.SECONDS);
            MAP.put(key, new Entity(data, future));
        } else {
            MAP.put(key, new Entity(data, null));
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 根据key读取缓存值
     * @param key 缓存键
     * @return
     * 返回值: Object
     * 返回值说明: 缓存值；key不存在时返回null
     */
    public synchronized static Object get(String key) {
        Entity entity = MAP.get(key);
        return entity == null ? null : entity.getValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 根据key读取缓存值并转换为指定类型
     * @param key 缓存键
     * @param clazz 目标类型
     * @return
     * 返回值: T
     * 返回值说明: 转换后的缓存值；key不存在时返回null，类型不匹配时抛出ClassCastException
     */
    public synchronized static <T> T get(String key, Class<T> clazz) {
        Object value = LocalCacheUtil.get(key);
        return value == null ? null : clazz.cast(value);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 删除指定key的缓存，并取消对应过期清理任务
     *
     * @param key 缓存键
     */
    public synchronized static void remove(String key) {
        Entity entity = MAP.remove(key);
        if (entity == null) {
            return;
        }
        Future<?> future = entity.getFuture();
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 清空本地缓存，并取消所有过期清理任务
     * 返回值: void
     * 返回值说明: 清空当前工具类维护的全部缓存
     */
    public synchronized static void clear() {
        for (Entity entity : MAP.values()) {
            Future<?> future = entity.getFuture();
            if (future != null) {
                future.cancel(false);
            }
        }
        MAP.clear();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 查询当前缓存数量
     * @return
     * 返回值: int
     * 返回值说明: 当前缓存键值对数量
     */
    public synchronized static int size() {
        return MAP.size();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:55
     * 说明: 本地缓存实体，保存缓存值和对应的过期清理任务
     */
    private static class Entity {

        /**
         * 缓存值
         */
        private final Object value;

        /**
         * 过期清理任务；不过期缓存为null
         */
        private final Future<?> future;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 23:55
         * 说明: 创建缓存实体
         * @param value 缓存值
         * @param future 过期清理任务，可为空
         */
        public Entity(Object value, Future<?> future) {
            this.value = value;
            this.future = future;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 23:55
         * 说明: 获取缓存值
         * @return
         * 返回值: Object
         * 返回值说明: 缓存值
         */
        public Object getValue() {
            return value;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/16 23:55
         * 说明: 获取过期清理任务
         * @return
         * 返回值: Future<?>
         * 返回值说明: 过期清理任务；不过期缓存返回null
         */
        public Future<?> getFuture() {
            return future;
        }
    }
}
