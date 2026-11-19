package com.zero.framework.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 确保应用退出时能关闭后台线程
 */
@Slf4j(topic = "sys-user")
@Component
public class ShutdownManager
{
    /**
     * logger字段
     */
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: destroy
     * @return
     * 返回值: void
     * 返回值说明：destroy
     */

    @PreDestroy
    public void destroy()
    {
        shutdownAsyncManager();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 停止异步执行任务
     * @return
     * 返回值: void
     * 返回值说明：停止异步执行任务
     */
    private void shutdownAsyncManager()
    {
        try
        {
            log.info("====关闭后台任务任务线程池====");
            AsyncManager.me().shutdown();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
