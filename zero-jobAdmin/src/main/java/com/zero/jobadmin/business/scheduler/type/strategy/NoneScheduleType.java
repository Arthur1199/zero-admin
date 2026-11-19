package com.zero.jobadmin.business.scheduler.type.strategy;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.scheduler.type.ScheduleType;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 表示没有自动运行计划的任务。
 */
public class NoneScheduleType extends ScheduleType {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 无自动调度计划，因此不生成后续触发点。
     * @param jobInfo 当前任务配置
     * @param fromTime 接口传入的计算起点
     * @return
     * 返回值: Date
     * 返回值说明: 固定返回 null
     */
    @Override
    public Date generateNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) throws Exception {
        // generate none trigger-time
        return null;
    }

}
