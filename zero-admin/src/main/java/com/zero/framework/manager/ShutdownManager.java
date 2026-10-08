package com.zero.framework.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 确保应用退出时能关闭后台线程
 */
@Component
public class ShutdownManager
{
    /**
     * logger字段
     */
    private static final Logger logger = LoggerFactory.getLogger("sys-user");
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
            logger.info("====关闭后台任务任务线程池====");
            AsyncManager.me().shutdown();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
