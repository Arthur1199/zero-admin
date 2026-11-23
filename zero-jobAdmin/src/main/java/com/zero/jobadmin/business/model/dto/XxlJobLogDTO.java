package com.zero.jobadmin.business.model.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.service.JobDashboardService;

import java.time.format.DateTimeFormatter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 21:22
 * 说明: 调度日志列表的显示数据，日志 ID 在浏览器接口中使用十进制字符串。
 */
public class XxlJobLogDTO {

    /** 按日志筛选的上海时区输出时间，保留已有的 JSON 日期格式。 */
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(JobDashboardService.REPORT_ZONE);

    private long id;

    // job info
    private int jobGroup;
    private int jobId;

    // execute info
    private String executorAddress;
    private String executorHandler;
    private String executorParam;
    private String executorShardingParam;
    private int executorFailRetryCount;

    // trigger info
    private String triggerTime;
    private int triggerCode;
    private String triggerMsg;

    // handle info
    private String handleTime;
    private int handleCode;
    private String handleMsg;

    // alarm info
    private int alarmStatus;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:51
     * 说明: 转为日志列表数据，页面时间与筛选区间统一使用上海时区
     * @param xxlJobLog 已读取的调度日志
     * @return
     * 返回值: 无
     * 返回值说明: 初始化展示字段，缺失的调度或执行时间保留为空
     */
    public XxlJobLogDTO(XxlJobLog xxlJobLog) {
        this.id = xxlJobLog.getId();
        this.jobGroup = xxlJobLog.getJobGroup();
        this.jobId = xxlJobLog.getJobId();
        this.executorAddress = xxlJobLog.getExecutorAddress();
        this.executorHandler = xxlJobLog.getExecutorHandler();
        this.executorParam = xxlJobLog.getExecutorParam();
        this.executorShardingParam = xxlJobLog.getExecutorShardingParam();
        this.executorFailRetryCount = xxlJobLog.getExecutorFailRetryCount();
        this.triggerTime = xxlJobLog.getTriggerTime() != null ? LOG_TIME_FORMAT.format(xxlJobLog.getTriggerTime().toInstant()) : null;
        this.triggerCode = xxlJobLog.getTriggerCode();
        this.triggerMsg = xxlJobLog.getTriggerMsg();
        this.handleTime = xxlJobLog.getHandleTime() != null ? LOG_TIME_FORMAT.format(xxlJobLog.getHandleTime().toInstant()) : null;
        this.handleCode = xxlJobLog.getHandleCode();
        this.handleMsg = xxlJobLog.getHandleMsg();
        this.alarmStatus = xxlJobLog.getAlarmStatus();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:22
     * 说明: 保留 Java 内部的 long，序列化时避免浏览器数字精度丢失。
     * @return
     * 返回值: long
     * 返回值说明: 数据库中的完整日志 ID，JSON 中输出十进制字符串
     */
    @JsonSerialize(using = ToStringSerializer.class)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(int jobGroup) {
        this.jobGroup = jobGroup;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getExecutorAddress() {
        return executorAddress;
    }

    public void setExecutorAddress(String executorAddress) {
        this.executorAddress = executorAddress;
    }

    public String getExecutorHandler() {
        return executorHandler;
    }

    public void setExecutorHandler(String executorHandler) {
        this.executorHandler = executorHandler;
    }

    public String getExecutorParam() {
        return executorParam;
    }

    public void setExecutorParam(String executorParam) {
        this.executorParam = executorParam;
    }

    public String getExecutorShardingParam() {
        return executorShardingParam;
    }

    public void setExecutorShardingParam(String executorShardingParam) {
        this.executorShardingParam = executorShardingParam;
    }

    public int getExecutorFailRetryCount() {
        return executorFailRetryCount;
    }

    public void setExecutorFailRetryCount(int executorFailRetryCount) {
        this.executorFailRetryCount = executorFailRetryCount;
    }

    public String getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(String triggerTime) {
        this.triggerTime = triggerTime;
    }

    public int getTriggerCode() {
        return triggerCode;
    }

    public void setTriggerCode(int triggerCode) {
        this.triggerCode = triggerCode;
    }

    public String getTriggerMsg() {
        return triggerMsg;
    }

    public void setTriggerMsg(String triggerMsg) {
        this.triggerMsg = triggerMsg;
    }

    public String getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public int getHandleCode() {
        return handleCode;
    }

    public void setHandleCode(int handleCode) {
        this.handleCode = handleCode;
    }

    public String getHandleMsg() {
        return handleMsg;
    }

    public void setHandleMsg(String handleMsg) {
        this.handleMsg = handleMsg;
    }

    public int getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(int alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

}
