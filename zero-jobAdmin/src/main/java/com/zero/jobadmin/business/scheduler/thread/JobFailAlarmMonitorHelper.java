package com.zero.jobadmin.business.scheduler.thread;

import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.tool.concurrent.CyclicThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * job fail-monitor helper
 *
 * @author xuxueli 2015-9-1 18:05:56
 */
public class JobFailAlarmMonitorHelper {
	private static final Logger logger = LoggerFactory.getLogger(JobFailAlarmMonitorHelper.class);

	/** 等待失败重试与告警处理。 */
	private static final int PENDING = 0;
	/** 已由当前调度中心认领。 */
	private static final int PROCESSING = -1;
	/** 日志关联的任务已不存在，无需提交重试或告警。 */
	private static final int SKIPPED = 1;
	/** 告警流程正常完成。 */
	private static final int ALARM_SUCCESS = 2;
	/** 告警或失败监控处理异常。 */
	private static final int ALARM_FAILED = 3;
	/** 单轮扫描的最大日志条数。 */
	private static final int BATCH_SIZE = 1000;
	

	// ---------------------- monitor ----------------------

	/**
	 * monitor thread
	 */
	private CyclicThread monitorThread;

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2015/9/1 18:05
	 * 说明: 启动失败重试及告警的周期扫描。
	 * @return
	 * 返回值: void
	 * 返回值说明: 启动后台监控线程
	 */
	public void start(){

		monitorThread = new CyclicThread("JobFailAlarmMonitorHelper#monitorThread", true, () -> {
			XxlJobAdminBootstrap bootstrap = XxlJobAdminBootstrap.getInstance();
			XxlJobLogMapper logMapper = bootstrap.getXxlJobLogMapper();
			List<Long> ids = logMapper.findFailJobLogIds(BATCH_SIZE);
			if (ids == null) {
				return;
			}
			for (long id : ids) {
				processFailure(bootstrap, logMapper, id);
			}
		}, 10 * 1000L, true);
		monitorThread.start();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:13
	 * 说明: 争抢单条失败日志的处理权，处理异常时尽量写入终态，批次继续扫描其他记录。
	 * @param bootstrap 当前调度中心
	 * @param logMapper 调度日志持久化接口
	 * @param id 待处理日志 ID
	 * @return
	 * 返回值: void
	 * 返回值说明: 已认领记录写入跳过、成功或失败状态
	 */
	private void processFailure(XxlJobAdminBootstrap bootstrap, XxlJobLogMapper logMapper, long id) {
		boolean claimed = false;
		int status = ALARM_FAILED;
		try {
			claimed = logMapper.updateAlarmStatus(id, PENDING, PROCESSING) == 1;
			if (!claimed) {
				return;
			}
			XxlJobLog log = logMapper.load(id);
			XxlJobInfo job = log == null ? null : bootstrap.getXxlJobInfoMapper().loadById(log.getJobId());
			if (job == null) {
				status = SKIPPED;
				return;
			}
			if (log.getExecutorFailRetryCount() > 0) {
				try {
					bootstrap.getJobTriggerPoolHelper().trigger(log.getJobId(), TriggerTypeEnum.RETRY,
							log.getExecutorFailRetryCount() - 1, log.getExecutorShardingParam(), log.getExecutorParam(), null);
					String retryMsg = "<br><br><span style=\"color:#00c0ef;\"> >>>>>>>>>>>"
							+ I18nUtil.getString("jobconf_trigger_type_retry") + "<<<<<<<<<<< </span><br>";
					logMapper.appendRetryMessage(id, retryMsg);
					log.setTriggerMsg((log.getTriggerMsg() == null ? "" : log.getTriggerMsg()) + retryMsg);
				} catch (RuntimeException e) {
					logger.error("Failed to submit or record job retry, logId={}", id, e);
				}
			}
			status = bootstrap.getJobAlarmer().alarm(job, log) ? ALARM_SUCCESS : ALARM_FAILED;
		} catch (RuntimeException e) {
			logger.error("Failed to process failed job, logId={}", id, e);
		} finally {
			if (claimed) {
				try {
					logMapper.updateAlarmStatus(id, PROCESSING, status);
				} catch (RuntimeException e) {
					logger.error("Failed to finalize alarm status, logId={}, status={}", id, status, e);
				}
			}
		}
	}

	/**
	 * stop
	 */
	public void stop(){
		monitorThread.stop();
	}

}
