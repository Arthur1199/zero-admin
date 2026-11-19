package com.zero.jobadmin.business.scheduler.route;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义从执行器组选择本次任务派发目标的路由接口。
 */
public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: route address
     * @param triggerRequest trigger request
     * @param jobGroup executor group
     * @return
     * 返回值: Response<String>
     * 返回值说明: ReturnT.content=address
     */
    public abstract Response<String> route(TriggerRequest triggerRequest, XxlJobGroup jobGroup);

}
