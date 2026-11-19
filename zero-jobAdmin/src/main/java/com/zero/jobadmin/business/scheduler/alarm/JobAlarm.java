package com.zero.jobadmin.business.scheduler.alarm;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义任务告警的发送接口，供失败监控调用不同的告警实现。
 */
public interface JobAlarm {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据任务与调度日志发送一次告警。
     * @param info 失败任务的配置
     * @param jobLog 对应的调度日志
     * @return
     * 返回值: boolean
     * 返回值说明: 当前告警实现是否处理成功
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}
