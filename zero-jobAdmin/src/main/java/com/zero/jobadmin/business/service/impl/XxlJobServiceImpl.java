package com.zero.jobadmin.business.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.constant.TriggerStatus;
import com.zero.jobadmin.business.mapper.*;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLogReport;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.misfire.MisfireStrategyEnum;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouteStrategyEnum;
import com.zero.jobadmin.business.scheduler.thread.JobScheduleHelper;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import com.zero.jobadmin.business.scheduler.type.ScheduleTypeEnum;
import com.zero.jobadmin.business.service.XxlJobService;
import com.zero.jobadmin.business.util.ExecutorAddressUtil;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * 作者: xuxueli
 * 时间: 2016/5/28 15:30:33
 * 说明: 负责任务配置维护、启停和触发，并校验执行器组权限
 */
@Slf4j
@Service
public class XxlJobServiceImpl implements XxlJobService {
	/** xxl_job_info 中 name、alarm_email、executor_handler 和 child_jobid 的字符上限。 */
	private static final int JOB_TEXT_LIMIT = 255;
	/** xxl_job_info.author 的字符上限。 */
	private static final int JOB_AUTHOR_LIMIT = 64;
	/** xxl_job_info.executor_param 为 TEXT，按 UTF-8 字节计算容量。 */
	private static final int JOB_EXECUTOR_PARAM_BYTE_LIMIT = 65535;

	@Resource
	private XxlJobGroupMapper xxlJobGroupMapper;
	@Resource
	private XxlJobInfoMapper xxlJobInfoMapper;
	@Resource
	public XxlJobLogMapper xxlJobLogMapper;
	@Resource
	private XxlJobLogGlueMapper xxlJobLogGlueMapper;
	@Resource
	private XxlJobLogReportMapper xxlJobLogReportMapper;
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:21
	 * 说明: 在同一数据库快照中查询任务列表和总数。
	 * @param offset 经过入口校验的分页偏移
	 * @param pagesize 每页条数，入口限制为 1 至 500
	 * @param jobGroup 当前账号有权查询的执行器组
	 * @param triggerStatus 调度状态，负数表示不筛选
	 * @param name 可选的任务名称筛选
	 * @param executorHandler 可选的 JobHandler 筛选
	 * @param author 可选的负责人筛选
	 * @return
	 * 返回值: Response<PageModel<XxlJobInfo>>
	 * 返回值说明: 同一快照中的任务记录和匹配总数
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
	public Response<PageModel<XxlJobInfo>> pageList(int offset, int pagesize, int jobGroup, int triggerStatus, String name, String executorHandler, String author) {

		// page list
		List<XxlJobInfo> list = xxlJobInfoMapper.pageList(offset, pagesize, jobGroup, triggerStatus, name, executorHandler, author);
		int list_count = xxlJobInfoMapper.pageListCount(offset, pagesize, jobGroup, triggerStatus, name, executorHandler, author);

		// package result
		PageModel<XxlJobInfo> pageModel = new PageModel<>();
		pageModel.setData(list);
		pageModel.setTotal(list_count);

		return Response.ofSuccess(pageModel);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:45
	 * 说明: 校验执行器组与任务配置，保存新任务
	 * @param jobInfo 待新增任务配置
	 * @param loginInfo 当前登录人
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 成功时返回任务 ID
	 */
	@Override
	public Response<String> add(XxlJobInfo jobInfo, LoginInfo loginInfo) {
		if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, jobInfo.getJobGroup())) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}

		// valid base
		if (StringTool.isBlank(jobInfo.getName())) {
			return Response.ofFail ( (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_jobdesc")) );
		}
		if (StringTool.isBlank(jobInfo.getAuthor())) {
			return Response.ofFail ( (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_author")) );
		}
		Response<String> fieldValidation = validateJobFieldSizes(jobInfo);
		if (!fieldValidation.isSuccess()) {
			return fieldValidation;
		}

		// valid group
		XxlJobGroup group = xxlJobGroupMapper.load(jobInfo.getJobGroup());
		if (group == null) {
			return Response.ofFail (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup"));
		}

		// valid trigger
		ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(jobInfo.getScheduleType(), null);
		Response<String> scheduleValidation = ScheduleTypeEnum.validateConfiguration(scheduleTypeEnum, jobInfo.getScheduleConf());
		if (!scheduleValidation.isSuccess()) {
			return scheduleValidation;
		}

		// valid job
		if (GlueTypeEnum.match(jobInfo.getGlueType()) == null) {
			return Response.ofFail ( (I18nUtil.getString("jobinfo_field_gluetype")+I18nUtil.getString("system_invalid")) );
		}
		if (GlueTypeEnum.BEAN==GlueTypeEnum.match(jobInfo.getGlueType()) && StringTool.isBlank(jobInfo.getExecutorHandler()) ) {
			return Response.ofFail ( (I18nUtil.getString("system_please_input")+"JobHandler") );
		}
		// 》fix "\r" in shell
		if (GlueTypeEnum.GLUE_SHELL==GlueTypeEnum.match(jobInfo.getGlueType()) && jobInfo.getGlueSource()!=null) {
			jobInfo.setGlueSource(jobInfo.getGlueSource().replaceAll("\r", ""));
		}

		// valid advanced
		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
			return Response.ofFail ( (I18nUtil.getString("jobinfo_field_executorRouteStrategy")+I18nUtil.getString("system_invalid")) );
		}
		if (MisfireStrategyEnum.match(jobInfo.getMisfireStrategy(), null) == null) {
			return Response.ofFail ( (I18nUtil.getString("misfire_strategy")+I18nUtil.getString("system_invalid")) );
		}
		if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
			return Response.ofFail ( (I18nUtil.getString("jobinfo_field_executorBlockStrategy")+I18nUtil.getString("system_invalid")) );
		}

		Response<String> childValidation = validateChildJobs(jobInfo, loginInfo);
		if (!childValidation.isSuccess()) {
			return childValidation;
		}

		// add in db
		jobInfo.setAddTime(new Date());
		jobInfo.setUpdateTime(new Date());
		jobInfo.setGlueUpdatetime(new Date());
		// remove the whitespace
		jobInfo.setExecutorHandler(Objects.toString(jobInfo.getExecutorHandler(), "").trim());

		xxlJobInfoMapper.save(jobInfo);
		if (jobInfo.getId() < 1) {
			return Response.ofFail ( (I18nUtil.getString("system_opt_add")+I18nUtil.getString("jobinfo_job")+I18nUtil.getString("system_fail")) );
		}

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = {}, jobId = {}, jobGroup = {}",
				loginInfo.getUserName(), "jobinfo-save", jobInfo.getId(), jobInfo.getJobGroup());

		return Response.ofSuccess(String.valueOf(jobInfo.getId()));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:45
	 * 说明: 更新任务配置，移组时同时校验原执行器组和目标组
	 * @param jobInfo 待更新的配置，执行器组为 0 时保留原组
	 * @param loginInfo 当前登录人
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 无权修改或任务已变更时返回失败
	 */
	@Override
	public Response<String> update(XxlJobInfo jobInfo, LoginInfo loginInfo) {
		XxlJobInfo exists_jobInfo = xxlJobInfoMapper.loadById(jobInfo.getId());
		if (exists_jobInfo == null) {
			return Response.ofFail(I18nUtil.getString("jobinfo_field_id") + I18nUtil.getString("system_not_found"));
		}
		int originalGroup = exists_jobInfo.getJobGroup();
		int targetGroup = jobInfo.getJobGroup() == 0 ? originalGroup : jobInfo.getJobGroup();
		if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, originalGroup)
				|| !JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, targetGroup)) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}
		if (targetGroup <= 0) {
			return Response.ofFail(I18nUtil.getString("jobinfo_field_jobgroup") + I18nUtil.getString("system_invalid"));
		}

		// valid base
		if (StringTool.isBlank(jobInfo.getName())) {
			return Response.ofFail ( (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_jobdesc")) );
		}
		if (StringTool.isBlank(jobInfo.getAuthor())) {
			return Response.ofFail ( (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_author")) );
		}
		Response<String> fieldValidation = validateJobFieldSizes(jobInfo);
		if (!fieldValidation.isSuccess()) {
			return fieldValidation;
		}
		if (GlueTypeEnum.BEAN == GlueTypeEnum.match(exists_jobInfo.getGlueType())
				&& StringTool.isBlank(jobInfo.getExecutorHandler())) {
			return Response.ofFail(I18nUtil.getString("system_please_input") + "JobHandler");
		}

		// valid group
		if (jobInfo.getJobGroup() > 0) {
			XxlJobGroup jobGroup = xxlJobGroupMapper.load(jobInfo.getJobGroup());
			if (jobGroup == null) {
				return Response.ofFail ( (I18nUtil.getString("jobinfo_field_jobgroup")+I18nUtil.getString("system_invalid")) );
			}
		}

		// valid trigger
		ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(jobInfo.getScheduleType(), null);
		Response<String> scheduleValidation = ScheduleTypeEnum.validateConfiguration(scheduleTypeEnum, jobInfo.getScheduleConf());
		if (!scheduleValidation.isSuccess()) {
			return scheduleValidation;
		}

		// valid advanced
		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
			return Response.ofFail ( (I18nUtil.getString("jobinfo_field_executorRouteStrategy")+I18nUtil.getString("system_invalid")) );
		}
		if (MisfireStrategyEnum.match(jobInfo.getMisfireStrategy(), null) == null) {
			return Response.ofFail ( (I18nUtil.getString("misfire_strategy")+I18nUtil.getString("system_invalid")) );
		}
		if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
			return Response.ofFail ( (I18nUtil.getString("jobinfo_field_executorBlockStrategy")+I18nUtil.getString("system_invalid")) );
		}

		Response<String> childValidation = validateChildJobs(jobInfo, loginInfo);
		if (!childValidation.isSuccess()) {
			return childValidation;
		}

		// next trigger time (5s后生效，避开预读周期)
		long nextTriggerTime = exists_jobInfo.getTriggerNextTime();
		boolean scheduleDataNotChanged = jobInfo.getScheduleType().equals(exists_jobInfo.getScheduleType())
				&& Objects.equals(jobInfo.getScheduleConf(), exists_jobInfo.getScheduleConf());
		boolean refreshNextTime = exists_jobInfo.getTriggerStatus() == TriggerStatus.RUNNING.getValue()
				&& !scheduleDataNotChanged;
		if (refreshNextTime) {
			try {
				// generate next trigger time
				Date nextValidTime = scheduleTypeEnum.getScheduleType().generateNextTriggerTime(jobInfo, new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
				if (nextValidTime == null) {
					return Response.ofFail ( (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_invalid")) );
				}
				nextTriggerTime = nextValidTime.getTime();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Response.ofFail ( (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_invalid")) );
			}
		}

		if (jobInfo.getJobGroup() > 0) {
			exists_jobInfo.setJobGroup(jobInfo.getJobGroup());
		}
		exists_jobInfo.setName(jobInfo.getName());
		exists_jobInfo.setAuthor(jobInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
		exists_jobInfo.setScheduleType(jobInfo.getScheduleType());
		exists_jobInfo.setScheduleConf(jobInfo.getScheduleConf());
		exists_jobInfo.setMisfireStrategy(jobInfo.getMisfireStrategy());
		exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorHandler(Objects.toString(jobInfo.getExecutorHandler(), "").trim());
		exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		exists_jobInfo.setExecutorTimeout(jobInfo.getExecutorTimeout());
		exists_jobInfo.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
		exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
		exists_jobInfo.setTriggerNextTime(nextTriggerTime);

		exists_jobInfo.setUpdateTime(new Date());
		if (xxlJobInfoMapper.updateInGroup(exists_jobInfo, originalGroup, refreshNextTime) != 1) {
			return Response.ofFail(I18nUtil.getString("system_opt_fail"));
		}

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = {}, jobId = {}, jobGroup = {}",
				loginInfo.getUserName(), "jobinfo-update", exists_jobInfo.getId(), exists_jobInfo.getJobGroup());

		return Response.ofSuccess();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:40
	 * 说明: 在执行 SQL 前检查任务配置字段容量，供新增和编辑共用。
	 * @param job 待保存的任务配置
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 可持久化时返回成功，否则返回对应字段错误
	 */
	private Response<String> validateJobFieldSizes(XxlJobInfo job) {
		if (exceedsCharacters(job.getName(), JOB_TEXT_LIMIT)) {
			return tooLong("jobinfo_field_jobdesc", JOB_TEXT_LIMIT);
		}
		if (exceedsCharacters(job.getAuthor(), JOB_AUTHOR_LIMIT)) {
			return tooLong("jobinfo_field_author", JOB_AUTHOR_LIMIT);
		}
		if (exceedsCharacters(job.getAlarmEmail(), JOB_TEXT_LIMIT)) {
			return tooLong("jobinfo_field_alarmemail", JOB_TEXT_LIMIT);
		}
		if (exceedsCharacters(Objects.toString(job.getExecutorHandler(), "").trim(), JOB_TEXT_LIMIT)) {
			return tooLong("jobinfo_field_executorHandler", JOB_TEXT_LIMIT);
		}
		return validateExecutorParam(job.getExecutorParam());
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 23:05
	 * 说明: 新增、编辑及立即触发共用 TEXT 参数容量检查。
	 * @param executorParam 待保存到任务配置或调度日志的参数，可为空
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 超出 UTF-8 字节容量时返回字段错误
	 */
	private Response<String> validateExecutorParam(String executorParam) {
		if (executorParam != null && executorParam.getBytes(StandardCharsets.UTF_8).length > JOB_EXECUTOR_PARAM_BYTE_LIMIT) {
			return Response.ofFail(MessageFormat.format(I18nUtil.getString("jobinfo_param_too_long"),
					JOB_EXECUTOR_PARAM_BYTE_LIMIT));
		}
		return Response.ofSuccess();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:40
	 * 说明: 按 Unicode 字符计算 VARCHAR 容量。
	 * @param value 可选字段值
	 * @param limit 字符上限
	 * @return
	 * 返回值: boolean
	 * 返回值说明: 超过字符上限时为 true
	 */
	private boolean exceedsCharacters(String value, int limit) {
		return value != null && value.codePointCount(0, value.length()) > limit;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:40
	 * 说明: 生成超出字段容量时的提示。
	 * @param fieldKey 字段名称的国际化键
	 * @param limit 字段最大字符数
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 带字段名称及上限的失败响应
	 */
	private Response<String> tooLong(String fieldKey, int limit) {
		return Response.ofFail(MessageFormat.format(I18nUtil.getString("jobinfo_field_too_long"),
				I18nUtil.getString(fieldKey), limit));
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:45
	 * 说明: 校验子任务编号、字段容量和执行器组权限，按首次出现的顺序保存唯一编号。
	 * @param jobInfo 待保存的任务配置
	 * @param loginInfo 当前登录人
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 校验成功时规范化任务编号，失败时说明对应子任务的问题
	 */
	private Response<String> validateChildJobs(XxlJobInfo jobInfo, LoginInfo loginInfo) {
		if (StringTool.isBlank(jobInfo.getChildJobId())) {
			return exceedsCharacters(jobInfo.getChildJobId(), JOB_TEXT_LIMIT)
					? tooLong("jobinfo_field_childJobId", JOB_TEXT_LIMIT) : Response.ofSuccess();
		}
		String[] childIds = jobInfo.getChildJobId().split(",");
		String label = I18nUtil.getString("jobinfo_field_childJobId");
		if (childIds.length == 0) {
			return Response.ofFail(label + I18nUtil.getString("system_invalid"));
		}
		String childJobIds = String.join(",", childIds);
		if (exceedsCharacters(childJobIds, JOB_TEXT_LIMIT)) {
			return tooLong("jobinfo_field_childJobId", JOB_TEXT_LIMIT);
		}
		Set<Integer> childJobs = new HashSet<>();
		StringJoiner normalizedChildren = new StringJoiner(",");
		for (String item : childIds) {
			String childId = item.strip();
			int childJobId;
			try {
				childJobId = Integer.parseInt(childId);
			} catch (NumberFormatException e) {
				return Response.ofFail(label + "(" + childId + ")" + I18nUtil.getString("system_invalid"));
			}
			if (childJobId <= 0 || childId.charAt(0) == '+' || childJobId == jobInfo.getId()) {
				return Response.ofFail(label + "(" + childId + ")" + I18nUtil.getString("system_invalid"));
			}
			if (!childJobs.add(childJobId)) {
				continue;
			}
			XxlJobInfo childJobInfo = xxlJobInfoMapper.loadById(childJobId);
			if (childJobInfo == null) {
				return Response.ofFail(label + "(" + childId + ")" + I18nUtil.getString("system_not_found"));
			}
			if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, childJobInfo.getJobGroup())) {
				return Response.ofFail(label + "(" + childId + ")" + I18nUtil.getString("system_permission_limit"));
			}
			normalizedChildren.add(Integer.toString(childJobId));
		}
		jobInfo.setChildJobId(normalizedChildren.toString());
		return Response.ofSuccess();
	}

	@Override
	public Response<String> remove(int id, LoginInfo loginInfo) {
		// valid job
		XxlJobInfo xxlJobInfo = xxlJobInfoMapper.loadById(id);
		if (xxlJobInfo == null) {
			return Response.ofSuccess();
		}

		// valid jobGroup permission
		if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, xxlJobInfo.getJobGroup())) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}

		xxlJobInfoMapper.delete(id);
		xxlJobLogMapper.delete(id);
		xxlJobLogGlueMapper.deleteByJobId(id);

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = {}, content = {}",
				loginInfo.getUserName(), "jobinfo-remove", GsonTool.toJson(xxlJobInfo));

		return Response.ofSuccess();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:49
	 * 说明: 在任务配置未变时启用调度，重复启用不会重置已生效的下次触发时间
	 * @param id 任务 ID
	 * @param loginInfo 当前登录人
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 已运行或启用成功时返回成功
	 */
	@Override
	public Response<String> start(int id, LoginInfo loginInfo) {
		// load and valid
		XxlJobInfo xxlJobInfo = xxlJobInfoMapper.loadById(id);
		if (xxlJobInfo == null) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}

		// valid jobGroup permission
		if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, xxlJobInfo.getJobGroup())) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}

		// valid ScheduleType: can not be none
		ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(xxlJobInfo.getScheduleType(), ScheduleTypeEnum.NONE);
		if (ScheduleTypeEnum.NONE == scheduleTypeEnum) {
			return Response.ofFail(I18nUtil.getString("schedule_type_none_limit_start"));
		}
		if (xxlJobInfo.getTriggerStatus() == TriggerStatus.RUNNING.getValue()) {
			return Response.ofSuccess();
		}

		// next trigger time (5s后生效，避开预读周期)
		long nextTriggerTime = 0;
		try {
			// generate next trigger time
			Date nextValidTime = scheduleTypeEnum.getScheduleType().generateNextTriggerTime(xxlJobInfo, new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));

			if (nextValidTime == null) {
				return Response.ofFail ( (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_invalid")) );
			}
			nextTriggerTime = nextValidTime.getTime();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Response.ofFail ( (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_invalid")) );
		}

		xxlJobInfo.setUpdateTime(new Date());
		if (xxlJobInfoMapper.updateTriggerState(xxlJobInfo, TriggerStatus.RUNNING.getValue(), nextTriggerTime) != 1) {
			return Response.ofFail(I18nUtil.getString("system_opt_fail"));
		}

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = {}, content = {}",
				loginInfo.getUserName(), "jobinfo-start", id);

		return Response.ofSuccess();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:49
	 * 说明: 停止任务后续调度并清空触发时间，保留任务配置和脚本
	 * @param id 任务 ID
	 * @param loginInfo 当前登录人
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 已停止或停止成功时返回成功
	 */
	@Override
	public Response<String> stop(int id, LoginInfo loginInfo) {
		// load and valid
        XxlJobInfo xxlJobInfo = xxlJobInfoMapper.loadById(id);
		if (xxlJobInfo == null) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}

		// valid jobGroup permission
		if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, xxlJobInfo.getJobGroup())) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}

		if (xxlJobInfo.getTriggerStatus() == TriggerStatus.STOPPED.getValue()) {
			return Response.ofSuccess();
		}
		xxlJobInfo.setUpdateTime(new Date());
		if (xxlJobInfoMapper.updateTriggerState(xxlJobInfo, TriggerStatus.STOPPED.getValue(), 0) != 1) {
			return Response.ofFail(I18nUtil.getString("system_opt_fail"));
		}

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = {}, content = {}",
				loginInfo.getUserName(), "jobinfo-stop", id);

		return Response.ofSuccess();
	}

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2016/5/28 15:30:33
	 * 说明: 校验执行器组权限和临时地址后手动派发，线程池拒绝时返回明确失败。
	 * @param loginInfo 当前登录用户
	 * @param jobId 任务 ID
	 * @param executorParam 手动指定的任务参数，缺省时传空字符串
	 * @param addressList 英文逗号分隔的临时执行器地址，可为空
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 进入派发队列返回成功，任务不存在、无权限、地址非法或提交被拒绝时返回失败
	 */
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 23:05
	 * 说明: 校验执行器组权限和覆盖字段容量后，将立即触发请求提交到派发队列。
	 * @param loginInfo 当前登录用户，必须有该任务的执行器组权限
	 * @param jobId 任务 ID
	 * @param executorParam 本次触发参数，缺省时使用空串
	 * @param addressList 临时执行器地址列表，空白时使用原配置
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 入队成功时返回成功，校验失败或队列拒绝时返回具体原因
	 */
	@Override
	public Response<String> trigger(LoginInfo loginInfo, int jobId, String executorParam, String addressList) {
		// valid job
		XxlJobInfo xxlJobInfo = xxlJobInfoMapper.loadById(jobId);
		if (xxlJobInfo == null) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}

		// valid jobGroup permission
		if (!JobGroupPermissionUtil.hasJobGroupPermission(loginInfo, xxlJobInfo.getJobGroup())) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}

		String addressOverride;
		try {
			addressOverride = ExecutorAddressUtil.normalize(addressList);
		} catch (IllegalArgumentException e) {
			return Response.ofFail(I18nUtil.getString("jobgroup_field_registryList_invalid"));
		}

		Response<String> paramCheck = validateExecutorParam(executorParam);
		if (!paramCheck.isSuccess()) {
			return paramCheck;
		}

		// force cover job param
		if (executorParam == null) {
			executorParam = "";
		}

		try {
			XxlJobAdminBootstrap.getInstance().getJobTriggerPoolHelper()
					.trigger(jobId, TriggerTypeEnum.MANUAL, -1, null, executorParam, addressOverride);
		} catch (RejectedExecutionException e) {
			return Response.ofFail(I18nUtil.getString("jobconf_trigger_pool_rejected"));
		}

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = {}, content = {}",
				loginInfo.getUserName(), "jobinfo-trigger", jobId);

		return Response.ofSuccess();
	}

	@Override
	public Map<String, Object> dashboardInfo() {

		int jobInfoCount = xxlJobInfoMapper.findAllCount();
		int jobLogCount = 0;
		int jobLogSuccessCount = 0;
		XxlJobLogReport xxlJobLogReport = xxlJobLogReportMapper.queryLogReportTotal();
		if (xxlJobLogReport != null) {
			jobLogCount = xxlJobLogReport.getRunningCount() + xxlJobLogReport.getSucCount() + xxlJobLogReport.getFailCount();
			jobLogSuccessCount = xxlJobLogReport.getSucCount();
		}

		// executor count
		Set<String> executorAddressSet = new HashSet<String>();
		List<XxlJobGroup> groupList = xxlJobGroupMapper.findAll();

		if (groupList!=null && !groupList.isEmpty()) {
			for (XxlJobGroup group: groupList) {
				if (group.getRegistryList()!=null && !group.getRegistryList().isEmpty()) {
					executorAddressSet.addAll(group.getRegistryList());
				}
			}
		}

		int executorCount = executorAddressSet.size();

		Map<String, Object> dashboardMap = new HashMap<String, Object>();
		dashboardMap.put("jobInfoCount", jobInfoCount);
		dashboardMap.put("jobLogCount", jobLogCount);
		dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
		dashboardMap.put("executorCount", executorCount);
		return dashboardMap;
	}

	@Override
	public Response<Map<String, Object>> chartInfo(Date startDate, Date endDate) {

		// process
		List<String> triggerDayList = new ArrayList<String>();
		List<Integer> triggerDayCountRunningList = new ArrayList<Integer>();
		List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
		List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
		int triggerCountRunningTotal = 0;
		int triggerCountSucTotal = 0;
		int triggerCountFailTotal = 0;

		List<XxlJobLogReport> logReportList = xxlJobLogReportMapper.queryLogReport(startDate, endDate);

		if (logReportList!=null && !logReportList.isEmpty()) {
			for (XxlJobLogReport item: logReportList) {
				String day = DateTool.formatDate(item.getTriggerDay());
				int triggerDayCountRunning = item.getRunningCount();
				int triggerDayCountSuc = item.getSucCount();
				int triggerDayCountFail = item.getFailCount();

				triggerDayList.add(day);
				triggerDayCountRunningList.add(triggerDayCountRunning);
				triggerDayCountSucList.add(triggerDayCountSuc);
				triggerDayCountFailList.add(triggerDayCountFail);

				triggerCountRunningTotal += triggerDayCountRunning;
				triggerCountSucTotal += triggerDayCountSuc;
				triggerCountFailTotal += triggerDayCountFail;
			}
		} else {
			for (int i = -6; i <= 0; i++) {
				triggerDayList.add(DateTool.formatDate(DateTool.addDays(new Date(), i)));
				triggerDayCountRunningList.add(0);
				triggerDayCountSucList.add(0);
				triggerDayCountFailList.add(0);
			}
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("triggerDayList", triggerDayList);
		result.put("triggerDayCountRunningList", triggerDayCountRunningList);
		result.put("triggerDayCountSucList", triggerDayCountSucList);
		result.put("triggerDayCountFailList", triggerDayCountFailList);

		result.put("triggerCountRunningTotal", triggerCountRunningTotal);
		result.put("triggerCountSucTotal", triggerCountSucTotal);
		result.put("triggerCountFailTotal", triggerCountFailTotal);

		return Response.ofSuccess(result);
	}

}
