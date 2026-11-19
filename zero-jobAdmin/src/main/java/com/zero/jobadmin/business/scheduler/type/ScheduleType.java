package com.zero.jobadmin.business.scheduler.type;

import com.zero.jobadmin.business.model.XxlJobInfo;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义按任务的调度配置计算下一次触发时间的接口。
 */
public abstract class ScheduleType {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据任务的调度方式计算起点之后的触发时间。
     * @param jobInfo 含调度方式和对应配置的任务
     * @param fromTime 本次计算的起始时间
     * @return
     * 返回值: Date
     * 返回值说明: 下一次有效的触发时间，没有后续计划时为 null
     */
    public abstract Date generateNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) throws Exception;

}
