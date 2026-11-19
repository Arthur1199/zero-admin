package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 从执行器组当前地址列表中选择第一项作为派发目标。
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 选择当前地址列表中的首个执行器。
     * @param triggerParam 当前调度触发参数
     * @param jobGroup 用于提供候选执行器地址的组
     * @return
     * 返回值: Response<String>
     * 返回值说明: 本次派发使用的执行器地址
     */
    @Override
    public Response<String> route(TriggerRequest triggerParam, XxlJobGroup jobGroup){
        return Response.ofSuccess(jobGroup.getRegistryList().get(0));
    }

}
