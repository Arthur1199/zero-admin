package com.zero.jobadmin.business.controller;

import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.model.dto.XxlJobLogDTO;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.exception.XxlJobException;
import com.zero.jobadmin.business.service.JobDashboardService;
import com.zero.jobadmin.business.service.JobLogCleanup;
import com.zero.jobadmin.business.service.XxlJobService;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.openapi.executor.ExecutorBiz;
import com.xxl.job.core.openapi.executor.dto.KillRequest;
import com.xxl.job.core.openapi.executor.dto.LogRequest;
import com.xxl.job.core.openapi.executor.dto.LogData;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: xuxueli
 * 时间: 2015/12/19 16:13:16
 * 说明: 调度日志列表、执行日志查看和日志维护入口，按执行器组校验访问权限
 */
@Controller
@RequestMapping("/joblog")
public class JobLogController {
	private static final Logger logger = LoggerFactory.getLogger(JobLogController.class);

	/** 调度日志的列表查询最多返回的记录数，兼容现有后台分页。 */
	private static final int MAX_LOG_PAGE_SIZE = 500;

	/** 与日志列表日期选择器一致，严格校验日期及时间。 */
	private static final DateTimeFormatter LOG_DATE_TIME =
			DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

	@Resource
	private XxlJobGroupMapper xxlJobGroupMapper;
	@Resource
	public XxlJobInfoMapper xxlJobInfoMapper;
	@Resource
	public XxlJobLogMapper xxlJobLogMapper;
    @Autowired
    private XxlJobService xxlJobService;

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:08
	 * 说明: 打开调度日志；从失败榜进入时保留已授权的历史执行器组和失败筛选
	 * @param request 当前登录请求，用于过滤执行器组
	 * @param model 调度日志页面参数
	 * @param jobGroup 执行器组 ID，普通入口可使用默认值 0
	 * @param jobId 任务 ID，普通入口可使用默认值 0
	 * @param failedOnly 是否从失败榜进入
	 * @param startDate 失败榜首日，格式 yyyy-MM-dd
	 * @param endDate 失败榜末日，含当天
	 * @return
	 * 返回值: String
	 * 返回值说明: 调度日志模板路径及上海时区的筛选基准日期
	 */
	@RequestMapping
	public String index(HttpServletRequest request,
						Model model,
						@RequestParam(value = "jobGroup", required = false, defaultValue = "0") Integer jobGroup,
						@RequestParam(value = "jobId", required = false, defaultValue = "0") Integer jobId,
						@RequestParam(defaultValue = "false") boolean failedOnly,
						@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
						@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		// 1、init JobGroupList
		// find all jobGroup
		List<XxlJobGroup> jobGroupListTotal =  xxlJobGroupMapper.findAll();

		// filter JobGroupList
		List<XxlJobGroup> jobGroupList = JobGroupPermissionUtil.filterJobGroupByPermission(request, jobGroupListTotal);
		if (CollectionTool.isEmpty(jobGroupList)) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}
		List<Integer> jobGroupIds = jobGroupList.stream().map(XxlJobGroup::getId).toList();

		// 2、check jobId
		if (failedOnly) {
			if (!jobGroupIds.contains(jobGroup)) {
				throw new XxlJobException(I18nUtil.getString("system_permission_limit"));
			}
			if (jobId <= 0 || startDate == null || endDate == null || startDate.isAfter(endDate)
					|| ChronoUnit.DAYS.between(startDate, endDate) >= JobDashboardService.MAX_DAYS) {
				throw new XxlJobException(I18nUtil.getString("job_report_invalid_range"));
			}
			model.addAttribute("reportStart", startDate.toString());
			model.addAttribute("reportEnd", endDate.toString());
		} else if (jobId > 0) {
			// valid jobId
			XxlJobInfo jobInfo = xxlJobInfoMapper.loadById(jobId);
			if (jobInfo == null) {
				throw new RuntimeException(I18nUtil.getString("jobinfo_field_id") + I18nUtil.getString("system_invalid"));
			}
			// valid jobGroup
			jobGroup = jobInfo.getJobGroup();
		}

		// 3、init jobGroup, default first 1
		if (!jobGroupIds.contains(jobGroup)) {
			jobGroup = jobGroupList.get(0).getId();
		}

		// 4、init jobInfoList
		List<XxlJobInfo> jobInfoList = xxlJobInfoMapper.getJobsByGroup(jobGroup);
		List<Integer> jobIds = jobInfoList.stream().map(XxlJobInfo::getId).toList();

		// 5、init JobId, default 0
		if (!failedOnly && !jobIds.contains(jobId)) {
			jobId = 0;
		}

		// write
		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobInfoList", jobInfoList);
		model.addAttribute("jobGroup", jobGroup);
		model.addAttribute("jobId", jobId);
		model.addAttribute("reportToday", LocalDate.now(JobDashboardService.REPORT_ZONE).toString());
		model.addAttribute("reportHistoricalJob", failedOnly && !jobIds.contains(jobId));

		return "business/log.list";
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:26
	 * 说明: 按已授权的执行器组分页查询调度日志，非法筛选条件直接返回错误
	 * @param request 当前登录请求
	 * @param offset 起始偏移，从 0 开始
	 * @param pagesize 每页条数，最大 500
	 * @param jobGroup 执行器组 ID
	 * @param jobId 任务 ID，0 表示组内全部任务
	 * @param logStatus 日志状态，1 成功，2 失败，3 进行中，-1 或 0 表示全部
	 * @param filterTime 调度时间区间，包含所选结束秒，空字符串表示不限时间
	 * @return
	 * 返回值: Response<PageModel<XxlJobLogDTO>>
	 * 返回值说明: 包含当前页记录和符合条件的总数
	 */
	@RequestMapping("/pageList")
	@ResponseBody
	public Response<PageModel<XxlJobLogDTO>> pageList(HttpServletRequest request,
													  @RequestParam(required = false, defaultValue = "0") int offset,
													  @RequestParam(required = false, defaultValue = "10") int pagesize,
													  @RequestParam int jobGroup,
													  @RequestParam int jobId,
													  @RequestParam int logStatus,
													  @RequestParam String filterTime) {

		if (jobGroup <= 0 || jobId < 0 || offset < 0 || pagesize < 1 || pagesize > MAX_LOG_PAGE_SIZE
				|| logStatus < -1 || logStatus > 3) {
			return Response.ofFail(I18nUtil.getString("system_invalid"));
		}
		JobGroupPermissionUtil.validJobGroupPermission(request, jobGroup);

		Date triggerTimeStart = null;
		Date triggerTimeEndExclusive = null;
		if (StringTool.isNotBlank(filterTime)) {
			String[] range = filterTime.trim().split(" - ", -1);
			if (range.length != 2) {
				return Response.ofFail(I18nUtil.getString("joblog_field_triggerTime") + " " + I18nUtil.getString("system_invalid"));
			}
			try {
				var start = LocalDateTime.parse(range[0], LOG_DATE_TIME);
				var end = LocalDateTime.parse(range[1], LOG_DATE_TIME);
				if (start.isAfter(end)) {
					return Response.ofFail(I18nUtil.getString("joblog_field_triggerTime") + " " + I18nUtil.getString("system_invalid"));
				}
				triggerTimeStart = Date.from(start.atZone(JobDashboardService.REPORT_ZONE).toInstant());
				triggerTimeEndExclusive = Date.from(end.atZone(JobDashboardService.REPORT_ZONE).toInstant().plusSeconds(1));
			} catch (DateTimeParseException e) {
				return Response.ofFail(I18nUtil.getString("joblog_field_triggerTime") + " " + I18nUtil.getString("system_invalid"));
			}
		}
		
		// page query
		List<XxlJobLog> list = xxlJobLogMapper.pageList(offset, pagesize, jobGroup, jobId, triggerTimeStart, triggerTimeEndExclusive, logStatus);
		int list_count = xxlJobLogMapper.pageListCount(offset, pagesize, jobGroup, jobId, triggerTimeStart, triggerTimeEndExclusive, logStatus);

		// model > dto
		List<XxlJobLogDTO> listDTO = list.stream().map(XxlJobLogDTO::new).toList();

		// package result
		PageModel<XxlJobLogDTO> pageModel = new PageModel<>();
		pageModel.setData(listDTO);
		pageModel.setTotal(list_count);

		return Response.ofSuccess(pageModel);
	}

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2015/12/19 16:13:16
	 * 说明: 为旧版日志页面转义原始日志，仅保留原有的换行和加粗标签。
	 * @param logContent 执行器返回的原始内容
	 * @return
	 * 返回值: String
	 * 返回值说明: 可供旧版页面按 HTML 显示的日志
	 */
	private String escapeLegacyLog(String logContent) {
		return HtmlUtils.htmlEscape(logContent, "UTF-8")
				.replace("&lt;br&gt;", "<br>")
				.replace("&lt;b&gt;", "<b>")
				.replace("&lt;/b&gt;", "</b>");
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:43
	 * 说明: 通过 POST 提交人工终止，校验原执行器组权限并记录执行器响应。
	 * @param request 当前登录请求
	 * @param id 调度日志 ID
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 写入人工终止结果后返回成功，状态已变化或写入失败时提示刷新
	 */
	@PostMapping("/logKill")
	@ResponseBody
	public Response<String> logKill(HttpServletRequest request, @RequestParam("id") long id){
		XxlJobLog log = xxlJobLogMapper.load(id);
		if (log == null) {
			return Response.ofFail(I18nUtil.getString("joblog_logid_invalid"));
		}
		JobGroupPermissionUtil.validJobGroupPermission(request, log.getJobGroup());
		XxlJobInfo jobInfo = xxlJobInfoMapper.loadById(log.getJobId());
		if (jobInfo == null || jobInfo.getJobGroup() != log.getJobGroup()) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}
		if (log.getHandleCode() != 0) {
			return Response.ofFail(I18nUtil.getString("joblog_kill_finished"));
		}
		if (log.getTriggerCode() != XxlJobContext.HANDLE_CODE_SUCCESS) {
			return Response.ofFail( I18nUtil.getString("joblog_kill_log_limit"));
		}
		XxlJobGroup jobGroup = xxlJobGroupMapper.load(log.getJobGroup());
		if (jobGroup == null || StringTool.isBlank(log.getExecutorAddress())) {
			return Response.ofFail(I18nUtil.getString("jobgroup_empty"));
		}

		// request of kill
		Response<String> runResult;
		try {
			ExecutorBiz executorBiz = XxlJobAdminBootstrap.getExecutorBiz(log.getExecutorAddress(), jobGroup);
			runResult = executorBiz.kill(new KillRequest(jobInfo.getId()));
		} catch (Exception e) {
			logger.error("Failed to terminate job for logId={}", id, e);
			return Response.ofFail(I18nUtil.getString("system_opt_fail"));
		}

		if (runResult == null) {
			return Response.ofFail(I18nUtil.getString("system_opt_fail"));
		}
		if (runResult.isSuccess()) {
			XxlJobLog result = new XxlJobLog();
			result.setId(id);
			result.setJobGroup(log.getJobGroup());
			result.setHandleCode(XxlJobContext.HANDLE_CODE_FAIL);
			result.setHandleMsg(I18nUtil.getString("joblog_kill_log_byman") + ":" + (runResult.getMsg() != null ? runResult.getMsg() : ""));
			result.setHandleTime(new Date());
			try {
				if (!XxlJobAdminBootstrap.getInstance().getJobCompleter().completeManualTermination(result)) {
					return Response.ofFail(I18nUtil.getString("joblog_kill_state_changed"));
				}
			} catch (RuntimeException e) {
				logger.error("Executor responded to termination but the result could not be saved, logId={}", id, e);
				return Response.ofFail(I18nUtil.getString("joblog_kill_store_failed"));
			}
			return Response.ofSuccess(runResult.getMsg());
		} else {
			return Response.ofFail(runResult.getMsg());
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:25
	 * 说明: 通过 POST 提交清理，按当前账号的执行器组权限处理历史调度日志。
	 * @param request 当前登录请求
	 * @param jobGroup 执行器组 ID
	 * @param jobId 任务 ID，0 表示组内全部任务
	 * @param type 保留期限或保留数量，9 表示清理范围内全部历史记录
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 本轮处理完成返回成功，批次出错时提示重试
	 */
	@PostMapping("/clearLog")
	@ResponseBody
	public Response<String> clearLog(HttpServletRequest request,
									@RequestParam("jobGroup") int jobGroup,
									@RequestParam("jobId") int jobId,
									@RequestParam("type") int type){
		if (jobGroup <= 0 || jobId < 0) {
			return Response.ofFail(I18nUtil.getString("system_invalid"));
		}
		JobGroupPermissionUtil.validJobGroupPermission(request, jobGroup);

		// opt
		Date clearBeforeTime = null;
		int clearBeforeNum = 0;
		if (type == 1) {
			clearBeforeTime = DateTool.addMonths(new Date(), -1);	// 清理一个月之前日志数据
		} else if (type == 2) {
			clearBeforeTime = DateTool.addMonths(new Date(), -3);	// 清理三个月之前日志数据
		} else if (type == 3) {
			clearBeforeTime = DateTool.addMonths(new Date(), -6);	// 清理六个月之前日志数据
		} else if (type == 4) {
			clearBeforeTime = DateTool.addYears(new Date(), -1);	// 清理一年之前日志数据
		} else if (type == 5) {
			clearBeforeNum = 1000;		// 清理一千条以前日志数据
		} else if (type == 6) {
			clearBeforeNum = 10000;		// 清理一万条以前日志数据
		} else if (type == 7) {
			clearBeforeNum = 30000;		// 清理三万条以前日志数据
		} else if (type == 8) {
			clearBeforeNum = 100000;	// 清理十万条以前日志数据
		} else if (type == 9) {
			clearBeforeNum = 0;			// 清理所有日志数据
		} else {
			return Response.ofFail(I18nUtil.getString("joblog_clean_type_invalid"));
		}

		try {
			JobLogCleanup.clear(xxlJobLogMapper, jobGroup, jobId, clearBeforeTime, clearBeforeNum);
			return Response.ofSuccess();
		} catch (RuntimeException e) {
			logger.error("Failed to finish log cleanup, jobGroup={}, jobId={}", jobGroup, jobId, e);
			return Response.ofFail(I18nUtil.getString("joblog_clean_incomplete"));
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:26
	 * 说明: 按日志所属执行器组验证权限，任务已删除或移组时仍可查看留存的执行日志
	 * @param request 当前登录请求
	 * @param id 调度日志 ID
	 * @param model 执行日志页面参数
	 * @return
	 * 返回值: String
	 * 返回值说明: 执行日志页面模板路径
	 */
	@RequestMapping("/logDetailPage")
	public String logDetailPage(HttpServletRequest request, @RequestParam("id") long id, Model model){

		// base check
		XxlJobLog jobLog = xxlJobLogMapper.load(id);
		if (jobLog == null) {
			throw new RuntimeException(I18nUtil.getString("joblog_logid_invalid"));
		}

		// valid permission
		JobGroupPermissionUtil.validJobGroupPermission(request, jobLog.getJobGroup());

		// load jobInfo
		XxlJobInfo jobInfo = xxlJobInfoMapper.loadById(jobLog.getJobId());

		// data
		model.addAttribute("triggerCode", jobLog.getTriggerCode());
		model.addAttribute("handleCode", jobLog.getHandleCode());
		model.addAttribute("logId", jobLog.getId());
		String jobName = jobInfo != null && jobInfo.getJobGroup() == jobLog.getJobGroup()
				&& StringTool.isNotBlank(jobInfo.getName()) ? jobInfo.getName() : "#" + jobLog.getJobId();
		model.addAttribute("jobName", jobName);
		return "business/log.detail";
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:43
	 * 说明: 从原执行器读取指定行开始的日志，先按调度记录权限验权并转义日志内容
	 * @param request 当前登录请求
	 * @param logId 调度日志 ID
	 * @param fromLineNum 起始行，从 1 开始
	 * @param plainText true 时返回原始日志，未指定时保留旧版 HTML 格式
	 * @return
	 * 返回值: Response<LogData>
	 * 返回值说明: 包含执行器的分段日志，失败时返回错误信息
	 */
	@RequestMapping("/logDetailCat")
	@ResponseBody
	public Response<LogData> logDetailCat(HttpServletRequest request,
	                                      @RequestParam("logId") long logId,
	                                      @RequestParam("fromLineNum") int fromLineNum,
	                                      @RequestParam(defaultValue = "false") boolean plainText){
		if (fromLineNum < 1) {
			return Response.ofFail(I18nUtil.getString("system_invalid"));
		}
		XxlJobLog jobLog = xxlJobLogMapper.load(logId);
		if (jobLog == null) {
			return Response.ofFail(I18nUtil.getString("joblog_logid_invalid"));
		}
		JobGroupPermissionUtil.validJobGroupPermission(request, jobLog.getJobGroup());
		if (jobLog.getTriggerTime() == null) {
			return Response.ofFail(I18nUtil.getString("joblog_rolling_log_triggerfail"));
		}
		XxlJobGroup jobGroup = xxlJobGroupMapper.load(jobLog.getJobGroup());
		if (jobGroup == null || StringTool.isBlank(jobLog.getExecutorAddress())) {
			return Response.ofFail(I18nUtil.getString("jobgroup_empty"));
		}
		try {
			// log cat
			ExecutorBiz executorBiz = XxlJobAdminBootstrap.getExecutorBiz(jobLog.getExecutorAddress(), jobGroup);
			Response<LogData> logResult = executorBiz.log(new LogRequest(logId, jobLog.getTriggerTime().getTime(), fromLineNum));

			if (logResult == null) {
				return Response.ofFail(I18nUtil.getString("system_opt_fail"));
			}
			LogData data = logResult.getData();
			if (data != null) {
				if (jobLog.getHandleCode() != 0 && data.getFromLineNum() > data.getToLineNum()) {
					data.setEnd(true);
				}
				if (!plainText && data.getLogContent() != null) {
					data.setLogContent(escapeLegacyLog(data.getLogContent()));
				}
			}

			return logResult;
		} catch (Exception e) {
			logger.error("logId({}) logDetailCat error: {}", logId, e.getMessage(), e);
			return Response.ofFail(I18nUtil.getString("system_opt_fail"));
		}
	}

}
