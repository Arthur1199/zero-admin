package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

import com.zero.jobadmin.framework.util.I18nUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按任务选取累计使用次数最少的执行器节点。
 */
public class ExecutorRouteLFU extends ExecutorRouter {

    /** 单个节点计数超过此值后重新随机起步。 */
    private static final int COUNT_LIMIT = 1_000_000;
    /** 每个任务的执行器使用次数。 */
    private static final JobRouteStateCache<Map<String, Integer>> COUNTS = new JobRouteStateCache<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:52
     * 说明: 同步更新同一任务的节点使用次数，并选出使用次数最少的节点。
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
        Map<String, Integer> counts = COUNTS.get(jobId, ignored -> new HashMap<>());
        synchronized (counts) {
            counts.keySet().retainAll(new HashSet<>(addressList));
            for (String address : addressList) {
                counts.compute(address, (key, count) -> count == null || count > COUNT_LIMIT
                        ? ThreadLocalRandom.current().nextInt(addressList.size()) : count);
            }
            Map.Entry<String, Integer> leastUsed = Collections.min(counts.entrySet(), Map.Entry.comparingByValue());
            leastUsed.setValue(leastUsed.getValue() + 1);
            return leastUsed.getKey();
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
