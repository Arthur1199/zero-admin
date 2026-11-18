package com.zero.jobadmin.business.scheduler.type.strategy;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.scheduler.type.ScheduleType;

import java.util.Date;

public class NoneScheduleType extends ScheduleType {

    @Override
    public Date generateNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) throws Exception {
        // generate none trigger-time
        return null;
    }

}
