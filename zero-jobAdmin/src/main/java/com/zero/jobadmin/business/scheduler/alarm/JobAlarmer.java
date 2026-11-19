package com.zero.jobadmin.business.scheduler.alarm;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.xxl.tool.core.CollectionTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * xxl-job alarmer
 *
 * @author xuxueli 17/7/13.
 */
@Slf4j
@Component
public class JobAlarmer {
    @Autowired
    private List<JobAlarm> jobAlarmList;

    /**
     * job alarm
     */
    public boolean alarm(XxlJobInfo info, XxlJobLog jobLog) {

        boolean result = false;
        if (CollectionTool.isNotEmpty(jobAlarmList)) {
            result = true;  // success means all-success
            for (JobAlarm alarm: jobAlarmList) {
                boolean resultItem = false;
                try {
                    resultItem = alarm.doAlarm(info, jobLog);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                if (!resultItem) {
                    result = false;
                }
            }
        }

        return result;
    }

    /*
    // implements SmartInitializingSingleton
    // implements ApplicationContextAware, InitializingBean
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, JobAlarm> serviceBeanMap = applicationContext.getBeansOfType(JobAlarm.class);
        if (MapTool.isNotEmpty(serviceBeanMap)) {
            jobAlarmList = new ArrayList<>(serviceBeanMap.values());
        }
    }*/

}
