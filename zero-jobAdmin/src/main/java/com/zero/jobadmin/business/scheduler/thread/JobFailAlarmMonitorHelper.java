package com.zero.jobadmin.business.scheduler.thread;

import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.context.XxlJobContext;
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
	/** 日志已不满足处理条件或关联任务不存在。 */
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

	/** 关闭请求已发出时，当前批次不再开始下一条记录。 */
	private volatile boolean stopping;

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
		stopping = false;
		monitorThread = new CyclicThread("JobFailAlarmMonitorHelper#monitorThread", true,
				this::scanFailures, 10 * 1000L, true);
		monitorThread.start();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:40
	 * 说明: 分批处理失败日志，关停时留下尚未认领的记录供下一次启动处理。
	 * @return
	 * 返回值: void
	 * 返回值说明: 单轮最多扫描 1000 条，停止后不拉取新批次
	 */
	private void scanFailures() {
		if (stopping) {
			return;
		}
		XxlJobAdminBootstrap bootstrap = XxlJobAdminBootstrap.getInstance();
		XxlJobLogMapper logMapper = bootstrap.getXxlJobLogMapper();
		List<Long> ids = logMapper.findFailJobLogIds(BATCH_SIZE);
		if (ids == null) {
			return;
		}
		for (long id : ids) {
			if (stopping) {
				break;
			}
			processFailure(bootstrap, logMapper, id);
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:13
	 * 说明: 认领失败日志，读取阶段出错或收到关闭请求时放回待处理。
	 * @param bootstrap 当前调度中心
	 * @param logMapper 调度日志持久化接口
	 * @param id 待处理日志 ID
	 * @return
	 * 返回值: void
	 * 返回值说明: 读取失败时下一轮可重试，已开始外部操作的记录保留终态
	 */
	private void processFailure(XxlJobAdminBootstrap bootstrap, XxlJobLogMapper logMapper, long id) {
		boolean claimed = false;
		int status = PENDING;
		try {
			claimed = logMapper.updateAlarmStatus(id, PENDING, PROCESSING) == 1;
			if (!claimed || stopping) {
				return;
			}
			XxlJobLog log = logMapper.load(id);
			if (log == null || log.getHandleCode() == XxlJobContext.HANDLE_CODE_SUCCESS) {
				status = SKIPPED;
				return;
			}
			XxlJobInfo job = bootstrap.getXxlJobInfoMapper().loadById(log.getJobId());
			if (job == null) {
				status = SKIPPED;
				return;
			}
			if (stopping) {
				return;
			}
			// 从这里起可能已发生派发或发信，异常时避免回到待处理导致重复操作。
			status = ALARM_FAILED;
			if (log.getExecutorFailRetryCount() > 0) {
				try {
					bootstrap.getJobTriggerPoolHelper().trigger(log.getJobId(), TriggerTypeEnum.RETRY,
							log.getExecutorFailRetryCount() - 1, log.getExecutorShardingParam(), log.getExecutorParam(), null);
					String retryMsg = "<br><br><span style=\"color:#00c0ef;\"> >>>>>>>>>>>"
							+ I18nUtil.getString("jobconf_trigger_type_retry") + "<<<<<<<<<<< </span><br>";
					if (logMapper.appendRetryMessage(id, retryMsg) == 1) {
						log.setTriggerMsg((log.getTriggerMsg() == null ? "" : log.getTriggerMsg()) + retryMsg);
					}
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
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:40
	 * 说明: 先停止认领新记录，再等待监控线程退出。
	 * @return
	 * 返回值: void
	 * 返回值说明: 未初始化时也可安全调用
	 */
	public void stop(){
		stopping = true;
		if (monitorThread != null) {
			monitorThread.stop();
		}
	}

}
