package com.zero.jobadmin.business.scheduler.type.strategy;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.scheduler.cron.CronExpression;
import com.zero.jobadmin.business.scheduler.type.ScheduleType;

import java.util.Date;

public class CronScheduleType extends ScheduleType {

    @Override
    public Date generateNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) throws Exception {
        // generate next trigger time, with cron
        return new CronExpression(jobInfo.getScheduleConf()).getNextValidTimeAfter(fromTime);
    }

}
