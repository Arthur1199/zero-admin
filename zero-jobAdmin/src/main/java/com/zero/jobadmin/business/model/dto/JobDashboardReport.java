package com.zero.jobadmin.business.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 19:08
 * 说明: 运行报表在同一查询范围内返回趋势、失败榜和待回调分页
 */
public record JobDashboardReport(
        List<DayResult> trend, List<FailedJob> failedJobs, List<PendingJob> pendingJobs,
        long pendingTotal, int page, int pageSize, LocalDateTime asOf) {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 按调度日期统计各阶段结果，忽略尚未记录派发或回调结果的瞬时记录
     */
    public record DayResult(LocalDate day, long succeeded, long dispatchFailed, long executionFailed, long pending) {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 任务在指定执行器组和所选日期内的失败次数、尝试次数及最后失败时间
     */
    public record FailedJob(int jobGroup, int jobId, String jobName, long failed, long attempts,
                            LocalDateTime lastFailure) {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 已成功派发且暂未收到执行结果的调度记录
     */
    public record PendingJob(String logId, int jobGroup, int jobId, String jobName,
                             String executorAddress, LocalDateTime triggerTime) {
    }
}
