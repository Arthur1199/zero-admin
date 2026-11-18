package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.openapi.executor.dto.IdleBeatRequest;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

/**
 *
 * 作者: xuxueli
 * 时间: 2017/3/10
 * 说明: 按组内配置顺序选择可以执行当前任务的空闲节点。
 */
public class ExecutorRouteBusyover extends ExecutorRouter {

    /**
     *
     * 作者: xuxueli
     * 时间: 2017/3/10
     * 说明: 按组内配置顺序选择可以执行当前任务的空闲节点。
     * @param triggerParam 调度请求，空闲检测使用其中的任务 ID
     * @param jobGroup 执行器组，提供节点列表和通信配置
     * @return
     * 返回值: Response<String>
     * 返回值说明: 成功时包含选中地址和探测记录，全部探测失败时返回失败记录
     */
    @Override
    public Response<String> route(TriggerRequest triggerParam, XxlJobGroup jobGroup) {
        if (triggerParam == null) {
            return Response.ofFail(I18nUtil.getString("system_opt_fail"));
        }
        return ExecutorRouteProbe.select(jobGroup, I18nUtil.getString("jobconf_idleBeat"),
                executor -> executor.idleBeat(new IdleBeatRequest(triggerParam.getJobId())));
    }
}
