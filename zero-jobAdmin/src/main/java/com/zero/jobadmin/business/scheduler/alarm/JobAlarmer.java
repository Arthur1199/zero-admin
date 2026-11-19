package com.zero.jobadmin.business.scheduler.alarm;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.xxl.tool.core.CollectionTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 调度中心统一的告警入口，调用已装配的告警实现处理失败任务。
 */
@Slf4j
@Component
public class JobAlarmer {
    @Autowired
    private List<JobAlarm> jobAlarmList;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对失败任务逐一调用配置的告警实现。
     * @param info 包含告警配置的任务
     * @param jobLog 失败的调度日志
     * @return
     * 返回值: boolean
     * 返回值说明: 至少有一个告警实现且全部成功时返回 true
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
