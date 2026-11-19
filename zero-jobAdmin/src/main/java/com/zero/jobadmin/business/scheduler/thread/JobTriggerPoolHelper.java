package com.zero.jobadmin.business.scheduler.thread;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 *
 * 作者: xuxueli
 * 时间: 2018/7/3 21:08:07
 * 说明: 管理快慢派发线程池，记录提交被拒绝或停机前未能开始的调度。
 */
@Slf4j
public class JobTriggerPoolHelper {
    /** 两组派发线程池共用的停机排空等待时间，单位秒。 */
    private static final long SHUTDOWN_GRACE_SECONDS = 10;

    // ---------------------- trigger pool ----------------------

    // fast/slow thread pool
    private ThreadPoolExecutor fastTriggerPool = null;
    private ThreadPoolExecutor slowTriggerPool = null;

    /**
     *
     * 作者: xuxueli
     * 时间: 2018/7/3 21:08:07
     * 说明: 初始化快慢派发线程池，队列满或线程池关闭时向提交方报告拒绝。
     * @return
     * 返回值: void
     * 返回值说明: 创建两组有界派发线程池
     */
    public void start(){
        fastTriggerPool = new ThreadPoolExecutor(
                10,
                XxlJobAdminBootstrap.getInstance().getTriggerPoolFastMax(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "xxl-job, admin JobTriggerPoolHelper-fastTriggerPool-" + r.hashCode());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy());

        slowTriggerPool = new ThreadPoolExecutor(
                10,
                XxlJobAdminBootstrap.getInstance().getTriggerPoolSlowMax(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(5000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "xxl-job, admin JobTriggerPoolHelper-slowTriggerPool-" + r.hashCode());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     *
     * 作者: xuxueli
     * 时间: 2018/7/3 21:08:07
     * 说明: 停止接收新派发，先等待已接收任务，再记录强制关闭时尚未开始的任务。
     * @return
     * 返回值: void
     * 返回值说明: 关闭快慢派发线程池，保留调用线程的中断状态
     */
    public void stop() {
        List<ThreadPoolExecutor> pools = Stream.of(fastTriggerPool, slowTriggerPool).filter(Objects::nonNull).toList();
        pools.forEach(ThreadPoolExecutor::shutdown);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(SHUTDOWN_GRACE_SECONDS);
        boolean interrupted = false;
        try {
            for (ThreadPoolExecutor pool : pools) {
                pool.awaitTermination(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException e) {
            interrupted = true;
        } finally {
            // 先停止两个池的取队动作，再写日志，避免慢库拖住另一个池的关闭。
            List<Runnable> abandoned = pools.stream().filter(pool -> !pool.isTerminated())
                    .flatMap(pool -> pool.shutdownNow().stream()).toList();
            try {
                for (Runnable task : abandoned) {
                    if (task instanceof DispatchTask dispatch) {
                        dispatch.onShutdown().run();
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Job trigger pools closed, queued tasks abandoned={}", abandoned.size());
        }
    }


    // job timeout count
    private volatile long minTim = System.currentTimeMillis()/60000;     // ms > min
    private volatile ConcurrentMap<Integer, AtomicInteger> jobTimeoutCountMap = new ConcurrentHashMap<>();


    // ---------------------- tool ----------------------

    /**
     *
     * 作者: xuxueli
     * 时间: 2018/7/3 21:08:07
     * 说明: 提交异步派发，被线程池拒绝时记失败日志并通知调用方。
     * @param jobId 任务 ID
     * @param triggerType 触发类型
     * @param failRetryCount 剩余重试次数，小于 0 时使用任务配置
     * @param executorShardingParam 可选分片编号，格式为索引/总数
     * @param executorParam 临时任务参数，为空时使用任务配置
     * @param addressList 临时执行器地址列表，为空时使用组内配置
     * @return
     * 返回值: void
     * 返回值说明: 任务已交给线程池，拒绝时抛出 RejectedExecutionException
     */
    public void trigger(final int jobId,
                        final TriggerTypeEnum triggerType,
                        final int failRetryCount,
                        final String executorShardingParam,
                        final String executorParam,
                        final String addressList) {

        // choose thread pool
        ThreadPoolExecutor triggerPool_ = fastTriggerPool;
        AtomicInteger jobTimeoutCount = jobTimeoutCountMap.get(jobId);
        if (jobTimeoutCount!=null && jobTimeoutCount.get() > 10) {      // job-timeout 10 times in 1 min
            triggerPool_ = slowTriggerPool;
        }

        Runnable work = new Runnable() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();

                try {
                    // do trigger
                    XxlJobAdminBootstrap.getInstance().getJobTrigger().trigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, addressList);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                } finally {

                    // check timeout-count-map
                    long minTim_now = System.currentTimeMillis()/60000;
                    if (minTim != minTim_now) {
                        minTim = minTim_now;
                        jobTimeoutCountMap.clear();
                    }

                    // incr timeout-count-map
                    long cost = System.currentTimeMillis()-start;
                    if (cost > 500) {       // ob-timeout threshold 500ms
                        AtomicInteger timeoutCount = jobTimeoutCountMap.putIfAbsent(jobId, new AtomicInteger(1));
                        if (timeoutCount != null) {
                            timeoutCount.incrementAndGet();
                        }
                    }

                }

            }
            @Override
            public String toString() {
                return "Job Runnable, jobId:"+jobId;
            }
        };
        DispatchTask dispatch = new DispatchTask(work, () -> reportDispatchFailure(
                jobId, triggerType, failRetryCount, executorShardingParam, executorParam, true));
        try {
            if (triggerPool_ == null) {
                throw new RejectedExecutionException("Job trigger pool is unavailable");
            }
            triggerPool_.execute(dispatch);
        } catch (RejectedExecutionException e) {
            reportDispatchFailure(jobId, triggerType, failRetryCount, executorShardingParam, executorParam, false);
            throw e;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:54
     * 说明: 记录未开始的派发，日志错误交由服务日志跟踪，继续处理其他任务。
     * @param jobId 任务 ID
     * @param triggerType 触发类型
     * @param retryCount 剩余重试次数，小于 0 时使用任务配置
     * @param sharding 分片编号，可为空
     * @param executorParam 临时执行参数，可为空
     * @param cancelledAfterAccept 已入队但因停机取消时为 true
     * @return
     * 返回值: void
     * 返回值说明: 尝试写入调度失败日志
     */
    private void reportDispatchFailure(int jobId, TriggerTypeEnum triggerType, int retryCount, String sharding,
                                       String executorParam, boolean cancelledAfterAccept) {
        try {
            XxlJobAdminBootstrap.getInstance().getJobTrigger().recordUndispatchedTrigger(
                    jobId, triggerType, retryCount, sharding, executorParam, cancelledAfterAccept);
        } catch (RuntimeException e) {
            log.error("Failed to record undispatched job, jobId={}, triggerType={}", jobId, triggerType, e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:54
     * 说明: 封装派发操作与停机取消操作，两者由线程池队列状态决定。
     */
    private record DispatchTask(Runnable work, Runnable onShutdown) implements Runnable {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/18 20:54
         * 说明: 执行从队列取出的派发操作。
         * @return
         * 返回值: void
         * 返回值说明: 调用执行器派发流程
         */
        @Override
        public void run() {
            work.run();
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/18 20:54
         * 说明: 让线程池拒绝异常带上任务编号。
         * @return
         * 返回值: String
         * 返回值说明: 派发操作的日志描述
         */
        @Override
        public String toString() {
            return work.toString();
        }
    }
}
