package com.zero.jobadmin.business.scheduler.type.strategy;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.scheduler.cron.CronExpression;
import com.zero.jobadmin.business.scheduler.type.ScheduleType;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按任务的 Cron 表达式计算后续触发时间。
 */
public class CronScheduleType extends ScheduleType {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解析任务 Cron 配置并搜索起点之后的第一个匹配时间。
     * @param jobInfo 含 Cron 表达式的任务
     * @param fromTime 查找下一次触发的起点
     * @return
     * 返回值: Date
     * 返回值说明: 下次匹配时间，没有更多匹配时为 null
     */
    @Override
    public Date generateNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) throws Exception {
        // generate next trigger time, with cron
        return new CronExpression(jobInfo.getScheduleConf()).getNextValidTimeAfter(fromTime);
    }

}
