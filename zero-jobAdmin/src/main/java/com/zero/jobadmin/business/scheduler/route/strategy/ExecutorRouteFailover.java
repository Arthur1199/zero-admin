package com.zero.jobadmin.business.scheduler.route.strategy;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouter;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.openapi.executor.ExecutorBiz;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.response.Response;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按组内配置顺序选择第一个心跳正常的节点。
 */
public class ExecutorRouteFailover extends ExecutorRouter {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按组内配置顺序选择第一个心跳正常的节点。
     * @param triggerParam 调度请求，心跳检测与具体任务无关
     * @param jobGroup 执行器组，提供节点列表和通信配置
     * @return
     * 返回值: Response<String>
     * 返回值说明: 成功时包含选中地址和探测记录，全部探测失败时返回失败记录
     */
    @Override
    public Response<String> route(TriggerRequest triggerParam, XxlJobGroup jobGroup) {
        return ExecutorRouteProbe.select(jobGroup, I18nUtil.getString("jobconf_beat"),
                ExecutorBiz::beat);
    }
}
