package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

import java.util.List;
import java.util.Random;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 从执行器组当前地址列表中随机选择本次派发目标。
 */
public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从当前地址列表随机选择执行器。
     * @param triggerParam 当前调度触发参数
     * @param jobGroup 用于提供候选执行器地址的组
     * @return
     * 返回值: Response<String>
     * 返回值说明: 本次派发使用的执行器地址
     */
    @Override
    public Response<String> route(TriggerRequest triggerParam, XxlJobGroup jobGroup) {
        List<String> addressList = jobGroup.getRegistryList();
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return Response.ofSuccess(address);
    }

}
