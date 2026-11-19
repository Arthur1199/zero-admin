package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

import com.zero.jobadmin.framework.util.I18nUtil;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按任务选取距离上次使用最久的执行器节点。
 */
public class ExecutorRouteLRU extends ExecutorRouter {

    /** 每个任务的节点使用顺序，队首为最久未使用的节点。 */
    private static final JobRouteStateCache<LinkedHashSet<String>> USAGE = new JobRouteStateCache<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:52
     * 说明: 同步维护同一任务的执行器使用顺序，选中后将其移至队尾。
     * @param jobId 任务 ID
     * @param addressList 当前执行器地址列表
     * @return
     * 返回值: String
     * 返回值说明: 本次选中的地址，地址列表为空时返回 null
     */
    public String route(int jobId, List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }
        LinkedHashSet<String> usage = USAGE.get(jobId, ignored -> new LinkedHashSet<>());
        synchronized (usage) {
            usage.retainAll(new HashSet<>(addressList));
            usage.addAll(addressList);
            var oldest = usage.iterator();
            String address = oldest.next();
            oldest.remove();
            usage.add(address);
            return address;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:52
     * 说明: 为任务选择执行器地址。
     * @param triggerParam 调度请求，提供任务 ID
     * @param jobGroup 执行器组，提供当前可选节点
     * @return
     * 返回值: Response<String>
     * 返回值说明: 成功时返回执行器地址，无可用节点时返回失败
     */
    @Override
    public Response<String> route(TriggerRequest triggerParam, XxlJobGroup jobGroup) {
        List<String> addressList = jobGroup == null ? null : jobGroup.getRegistryList();
        if (triggerParam == null || addressList == null || addressList.isEmpty()) {
            return Response.ofFail(I18nUtil.getString("jobconf_trigger_address_empty"));
        }
        return Response.ofSuccess(route(triggerParam.getJobId(), addressList));
    }
}
