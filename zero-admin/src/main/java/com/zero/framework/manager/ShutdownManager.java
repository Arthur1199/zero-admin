package com.zero.framework.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 22:37
 * 说明: 管理后台上下文关闭时释放本次启动创建的异步任务线程池。
 */
@Slf4j(topic = "sys-user")
@Component
@RequiredArgsConstructor
public class ShutdownManager
{
    /** 关闭上下文时使用同一上下文中的任务管理器。 */
    private final AsyncManager asyncManager;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:37
     * 说明: 应用退出或重启关闭旧上下文时，停止该上下文的异步任务。
     * @return
     * 返回值: void
     * 返回值说明: 触发后台任务线程池的关闭
     */
    @PreDestroy
    public void destroy()
    {
        shutdownAsyncManager();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:37
     * 说明: 关闭当前上下文的异步任务线程池，并记录关闭异常。
     * @return
     * 返回值: void
     * 返回值说明: 等待异步任务线程池结束
     */
    private void shutdownAsyncManager()
    {
        try
        {
            log.info("====关闭后台任务任务线程池====");
            asyncManager.shutdown();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
