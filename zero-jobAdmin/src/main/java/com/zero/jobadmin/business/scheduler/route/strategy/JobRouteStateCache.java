package com.zero.jobadmin.business.scheduler.route.strategy;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 22:52
 * 说明: 为有状态的执行器路由保存任务状态，每隔一天整体更新缓存。
 */
final class JobRouteStateCache<T> {

    /** 路由状态保留时间，沿用原路由策略的一天缓存周期。 */
    private static final long VALIDITY_NANOS = Duration.ofDays(1).toNanos();

    /** 到期时整体替换，避免清理时移除其他线程刚初始化的状态。 */
    private volatile StateWindow<T> window = new StateWindow<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:52
     * 说明: 同一缓存周期内为同一个任务返回同一个路由状态。
     * @param jobId 任务 ID
     * @param create 任务首次参与路由时使用的状态工厂
     * @return
     * 返回值: T
     * 返回值说明: 非空路由状态，调用方负责同步其后续读写
     */
    T get(int jobId, IntFunction<T> create) {
        StateWindow<T> current = window;
        if (System.nanoTime() - current.startedAt >= VALIDITY_NANOS) {
            synchronized (this) {
                if (System.nanoTime() - window.startedAt >= VALIDITY_NANOS) {
                    window = new StateWindow<>();
                }
                current = window;
            }
        }
        return current.states.computeIfAbsent(jobId, create::apply);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:52
     * 说明: 保存同一有效期内的任务路由状态。
     */
    private static final class StateWindow<T> {
        /** 起始单调时间，系统时钟调整不影响过期判断。 */
        private final long startedAt = System.nanoTime();
        /** 当前周期的任务 ID 到路由状态的映射。 */
        private final ConcurrentMap<Integer, T> states = new ConcurrentHashMap<>();
    }
}
