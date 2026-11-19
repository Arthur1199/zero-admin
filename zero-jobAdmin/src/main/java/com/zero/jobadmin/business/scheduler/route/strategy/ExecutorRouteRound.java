package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

import com.zero.jobadmin.framework.util.I18nUtil;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按任务轮询选取执行器节点。
 */
public class ExecutorRouteRound extends ExecutorRouter {

    /** 计数器回绕阈值，达到上限后重新随机起步。 */
    private static final int COUNT_LIMIT = 1_000_000;
    /** 首次轮询的随机起点范围。 */
    private static final int START_BOUND = 100;
    /** 每个任务的轮询计数。 */
    private static final JobRouteStateCache<AtomicInteger> COUNTS = new JobRouteStateCache<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:52
     * 说明: 为任务原子地预留一次轮询序号，首次访问时随机分散压力。
     * @param jobId 任务 ID
     * @return
     * 返回值: int
     * 返回值说明: 本次触发独占的轮询序号
     */
    private static int count(int jobId) {
        AtomicInteger state = COUNTS.get(jobId, ignored -> new AtomicInteger(-1));
        return state.updateAndGet(previous -> previous < 0 || previous > COUNT_LIMIT
                ? ThreadLocalRandom.current().nextInt(START_BOUND) : previous + 1);
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
        return Response.ofSuccess(addressList.get(count(triggerParam.getJobId()) % addressList.size()));
    }
}
