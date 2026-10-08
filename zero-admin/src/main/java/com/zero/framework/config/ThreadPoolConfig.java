package com.zero.framework.config;

import com.zero.common.utils.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 线程池配置
 */
@Configuration
public class ThreadPoolConfig
{
    // 核心线程池大小
    /**
     * corePool大小字段
     */
    private int corePoolSize = 50;

    // 最大可创建的线程数
    /**
     * maxPool大小字段
     */
    private int maxPoolSize = 200;

    // 队列最大长度
    /**
     * queueCapacity字段
     */
    private int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间
    /**
     * keepAliveSeconds字段
     */
    private int keepAliveSeconds = 300;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: threadPoolTaskExecutor
     * @return
     * 返回值: ThreadPoolTaskExecutor
     * 返回值说明：threadPoolTaskExecutor
     */

    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 执行周期性或定时任务
     * @return
     * 返回值: ScheduledExecutorService
     * 返回值说明：执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService()
    {
        return new ScheduledThreadPoolExecutor(corePoolSize,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy())
        {
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: afterExecute
             * @param r r参数
             * @param t t参数
             * @return
             * 返回值: void
             * 返回值说明：afterExecute
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t)
            {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
