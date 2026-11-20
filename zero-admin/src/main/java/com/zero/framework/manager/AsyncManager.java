package com.zero.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.zero.common.utils.Threads;
import com.zero.common.utils.spring.SpringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 22:37
 * 说明: 管理当前应用上下文的异步任务，应用重启后使用新建的线程池。
 */
@Component
public class AsyncManager
{
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private final ScheduledExecutorService executor;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:37
     * 说明: 将异步任务管理器绑定到当前应用上下文的调度线程池。
     * @param executor 当前应用上下文提供的调度线程池
     */
    public AsyncManager(ScheduledExecutorService executor)
    {
        this.executor = executor;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:37
     * 说明: 获取当前 Spring 上下文中的异步任务管理器，兼容现有调用入口。
     * @return 当前上下文的异步任务管理器
     * 返回值: AsyncManager
     * 返回值说明: 重启后的调用使用新上下文中的管理器及调度线程池
     */

    public static AsyncManager me()
    {
        return SpringUtils.getBean(AsyncManager.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 执行任务
     * @param task 任务
     * @return
     * 返回值: void
     * 返回值说明：执行任务
     */
    public void execute(TimerTask task)
    {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 停止任务线程池
     * @return
     * 返回值: void
     * 返回值说明：停止任务线程池
     */
    public void shutdown()
    {
        Threads.shutdownAndAwaitTermination(executor);
    }
}
