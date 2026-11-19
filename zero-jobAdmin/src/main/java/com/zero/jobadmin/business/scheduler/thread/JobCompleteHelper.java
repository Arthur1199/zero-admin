package com.zero.jobadmin.business.scheduler.thread;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.constant.RegistTypeEnum;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.openapi.admin.dto.CallbackData;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.response.Response;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * 作者: xuxueli
 * 时间: 2015/9/1 18:05:56
 * 说明: 提交执行器回调结果，并监控长时间未回调且离线的任务。
 */
@Slf4j
public class JobCompleteHelper {
	/** 任务运行超过该时长后，才检查执行器心跳是否失效。 */
	private static final int LOST_JOB_MINUTES = 10;
	/** 单轮最多检查的待回调任务数。 */
	private static final int LOST_JOB_BATCH_SIZE = 1000;
	
	// ---------------------- monitor ----------------------

	private CyclicThread jobMonitorThread;
	/** 停止时拒绝接收新的回调批次，由执行器保留并重试。 */
	private volatile boolean stopping;

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2015/9/1 18:05
	 * 说明: 接收执行器回调，并启动执行结果丢失监控。
	 * @return
	 * 返回值: void
	 * 返回值说明: 启动后台监控线程
	 */
	public void start(){
		stopping = false;
		jobMonitorThread = new CyclicThread("JobCompleteHelper#jobMonitorThread", true,
				() -> markLostJobs(XxlJobAdminBootstrap.getInstance().getXxlJobLogMapper(), new Date()),
				60 * 1000L, true);
		jobMonitorThread.start();

	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:16
	 * 说明: 根据执行器心跳检查待回调日志，写失败结果前再次校验当前状态。
	 * @param logMapper 调度日志数据接口
	 * @param now 本轮查询和更新所使用的基准时间
	 * @return
	 * 返回值: void
	 * 返回值说明: 仅把仍未完成且执行器离线的自动注册任务标记为失败
	 */
	private void markLostJobs(XxlJobLogMapper logMapper, Date now) {
		Date triggerBefore = DateTool.addMinutes(now, -LOST_JOB_MINUTES);
		Date heartbeatSince = new Date(now.getTime() - TimeUnit.SECONDS.toMillis(Const.DEAD_TIMEOUT));
		String registryGroup = RegistTypeEnum.EXECUTOR.name();
		List<Long> candidates = logMapper.findLostJobIds(triggerBefore, registryGroup, heartbeatSince, LOST_JOB_BATCH_SIZE);
		if (candidates == null) {
			return;
		}
		for (long logId : candidates) {
			XxlJobLog log = new XxlJobLog();
			log.setId(logId);
			log.setHandleTime(now);
			log.setHandleCode(XxlJobContext.HANDLE_CODE_FAIL);
			log.setHandleMsg(I18nUtil.getString("joblog_lost_fail"));
			try {
				logMapper.markLostJob(log, triggerBefore, registryGroup, heartbeatSince);
			} catch (RuntimeException e) {
				JobCompleteHelper.log.error("Failed to mark lost job, logId={}", logId, e);
			}
		}
	}

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2015/9/1 18:05:56
	 * 说明: 拒绝新回调，停止待回调任务的离线检查。
	 * @return
	 * 返回值: void
	 * 返回值说明: 已进入请求处理的批次继续返回实际写入结果
	 */
	public void stop(){
		stopping = true;
		if (jobMonitorThread != null) {
			jobMonitorThread.stop();
		}
	}


	// ---------------------- helper ----------------------

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:08
	 * 说明: 逐条确认回调落库，写入出错时让执行器保留整批并重试。
	 * @param jobGroupId API 认证确定的执行器组 ID
	 * @param callbackParamList 执行器回传的完成记录
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 可处理的记录已提交或已有终态时返回成功
	 */
	public Response<String> callback(int jobGroupId, List<CallbackData> callbackParamList) {
		if (jobGroupId <= 0 || callbackParamList == null) {
			return Response.ofFail("Illegal Argument.");
		}
		if (stopping) {
			return Response.ofFail("Callback service is stopping. Retry the batch.");
		}
		boolean failed = false;
		for (CallbackData callbackRequest : callbackParamList) {
			if (callbackRequest == null) {
				continue;
			}
			try {
				doCallback(jobGroupId, callbackRequest);
			} catch (RuntimeException e) {
				failed = true;
				log.error("Failed to process executor callback, logId={}", callbackRequest.getLogId(), e);
			}
		}
		return failed ? Response.ofFail("Failed to process callback. Retry the batch.") : Response.ofSuccess();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:08
	 * 说明: 校验日志归属，只有认证执行器组对应的日志可以写回终态。
	 * @param jobGroupId 认证通过的执行器组 ID
	 * @param handleCallbackParam 单条回调记录
	 * @return
	 * 返回值: void
	 * 返回值说明: 跳过无效或已结束的记录，数据写入异常由批次入口报告
	 */
	private void doCallback(int jobGroupId, CallbackData handleCallbackParam) {
		if (handleCallbackParam.getLogId() <= 0 || handleCallbackParam.getHandleCode() <= 0) {
			return;
		}
		// valid log item
		XxlJobLog log = XxlJobAdminBootstrap.getInstance().getXxlJobLogMapper().load(handleCallbackParam.getLogId());
		if (log == null || log.getJobGroup() != jobGroupId || log.getHandleCode() > 0) {
			return;
		}

		// handle msg
		StringBuilder handleMsg = new StringBuilder();
		if (log.getHandleMsg()!=null) {
			handleMsg.append(log.getHandleMsg()).append("<br>");
		}
		if (handleCallbackParam.getHandleMsg() != null) {
			handleMsg.append(handleCallbackParam.getHandleMsg());
		}

		// success, save log
		XxlJobLog result = new XxlJobLog();
		result.setId(log.getId());
		result.setJobId(log.getJobId());
		result.setJobGroup(jobGroupId);
		result.setHandleTime(new Date());
		result.setHandleCode(handleCallbackParam.getHandleCode());
		result.setHandleMsg(handleMsg.toString());
		XxlJobAdminBootstrap.getInstance().getJobCompleter().complete(result);
	}



}
