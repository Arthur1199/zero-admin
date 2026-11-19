package com.zero.jobadmin.business.model;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 对应 xxl_job_log 的单次调度日志，记录触发、执行和告警状态。
 */
public class XxlJobLog {
	
	/** 单次调度记录的主键。 */
	private long id;
	
	// job info
	/** 关联的执行器组主键。 */
	private int jobGroup;
	/** 关联的调度任务主键。 */
	private int jobId;

	// execute info
	/** 本次任务路由选择的执行器地址。 */
	private String executorAddress;
	/** 本次执行使用的任务处理器名称。 */
	private String executorHandler;
	/** 本次调度实际发送的运行参数。 */
	private String executorParam;
	/** 本次调度携带的分片参数。 */
	private String executorShardingParam;
	/** 生成本次记录时配置的剩余重试次数。 */
	private int executorFailRetryCount;
	
	// trigger info
	/** 调度中心发起本次触发的时间。 */
	private Date triggerTime;
	/** 执行器返回的触发结果码，用于判断触发阶段是否成功。 */
	private int triggerCode;
	/** 调度中心记录的触发过程和结果说明。 */
	private String triggerMsg;
	
	// handle info
	/** 执行结果回调或收尾处理的记录时间。 */
	private Date handleTime;
	/** 任务执行结果码，未结束时使用初始值。 */
	private int handleCode;
	/** 任务执行结果或异常处理说明。 */
	private String handleMsg;

	// alarm info
	/** 任务失败的监控处理和告警状态。 */
	private int alarmStatus;

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

	public Date getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime) {
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

	public Date getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(Date handleTime) {
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
