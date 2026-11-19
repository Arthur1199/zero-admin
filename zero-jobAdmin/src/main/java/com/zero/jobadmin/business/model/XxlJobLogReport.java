package com.zero.jobadmin.business.model;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 对应 xxl_job_log_report 的逐日调度统计记录，也用于承载各统计字段的合计。
 */
public class XxlJobLogReport {

    /** 逐日报表记录主键。 */
    private int id;
    /** 调度统计所属日期。 */
    private Date triggerDay;

    /** 统计时尚未结束的调度记录数。 */
    private int runningCount;
    /** 统计中执行成功的记录数。 */
    private int sucCount;
    /** 统计中最终判定为失败的记录数。 */
    private int failCount;

    /** 统计快照计算时间，用于识别更新较新的统计数据。 */
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTriggerDay() {
        return triggerDay;
    }

    public void setTriggerDay(Date triggerDay) {
        this.triggerDay = triggerDay;
    }

    public int getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(int runningCount) {
        this.runningCount = runningCount;
    }

    public int getSucCount() {
        return sucCount;
    }

    public void setSucCount(int sucCount) {
        this.sucCount = sucCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
