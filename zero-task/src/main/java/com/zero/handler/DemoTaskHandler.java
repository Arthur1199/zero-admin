package com.zero.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:19
 * 说明: 提供可由 ZERO-JOB 派发的示例，用于检查任务参数、日志和执行结果。
 */
@Component
public class DemoTaskHandler {

    /** 示例中用于返回失败结果的参数。 */
    private static final String FAILURE_PARAMETER = "fail";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:19
     * 说明: 记录任务参数长度和分片信息，避免将参数原文写入日志。
     * @return
     * 返回值: void
     * 返回值说明: 参数为 fail 时回调失败，其他参数或空参数回调成功
     */
    @XxlJob("zeroTaskDemoHandler")
    public void execute() {
        String parameter = XxlJobHelper.getJobParam();
        XxlJobHelper.log("示例任务开始，参数长度={}，分片索引={}，分片总数={}",
                parameter == null ? 0 : parameter.length(), XxlJobHelper.getShardIndex(), XxlJobHelper.getShardTotal());
        if (parameter != null && FAILURE_PARAMETER.equals(parameter.strip())) {
            XxlJobHelper.handleFail("示例任务按参数返回失败");
            return;
        }
        XxlJobHelper.handleSuccess("示例任务执行成功");
    }
}
