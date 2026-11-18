package com.zero.jobadmin.business.model.dto;

import com.zero.jobadmin.business.model.XxlJobInfo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 20:48
 * 说明: 调度扫描写入游标时同时携带读取时的计划，避免覆盖并发修改。
 * @param expectedGroup 扫描时的执行器组 ID
 * @param expectedLastTime 扫描时的上次触发时间戳，单位毫秒
 * @param expectedNextTime 扫描时的下次触发时间戳，单位毫秒
 * @param expectedScheduleType 扫描时的调度类型
 * @param expectedScheduleConf 扫描时的调度配置，允许为空
 * @param expectedMisfireStrategy 扫描时的过期策略
 * @param job 匹配原计划后要写入的任务游标
 */
public record JobScheduleUpdate(int expectedGroup, long expectedLastTime, long expectedNextTime,
                                String expectedScheduleType, String expectedScheduleConf, String expectedMisfireStrategy,
                                XxlJobInfo job) {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:48
     * 说明: 在推进游标前保存原计划，保留任务对象供扫描计算新游标。
     * @param job 本轮查询到的任务
     * @return
     * 返回值: JobScheduleUpdate
     * 返回值说明: 带有原计划快照的待写入任务
     */
    public static JobScheduleUpdate capture(XxlJobInfo job) {
        return new JobScheduleUpdate(job.getJobGroup(), job.getTriggerLastTime(), job.getTriggerNextTime(),
                job.getScheduleType(), job.getScheduleConf(), job.getMisfireStrategy(), job);
    }
}
