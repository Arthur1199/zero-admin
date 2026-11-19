package com.zero.jobadmin.business.scheduler.misfire.strategy;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.misfire.MisfireHandler;

@Slf4j
public class MisfireDoNothing extends MisfireHandler {
    @Override
    public void handle(int jobId) {
        log.warn(">>>>>>>>>>> xxl-job, schedule MisfireDoNothing: jobId = " + jobId );
    }

}
