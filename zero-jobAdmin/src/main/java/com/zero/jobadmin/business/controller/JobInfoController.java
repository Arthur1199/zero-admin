package com.zero.jobadmin.business.controller;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.constant.TriggerStatus;
import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.scheduler.exception.XxlJobException;
import com.zero.jobadmin.business.scheduler.misfire.MisfireStrategyEnum;
import com.zero.jobadmin.business.scheduler.route.ExecutorRouteStrategyEnum;
import com.zero.jobadmin.business.scheduler.thread.JobScheduleHelper;
import com.zero.jobadmin.business.scheduler.type.ScheduleTypeEnum;
import com.zero.jobadmin.business.service.XxlJobService;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.zero.jobadmin.framework.util.PageQueryUtil;
import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 提供任务定义查询和维护、启停及手动触发入口，并根据登录人限制可操作的执行器组。
 */
@Slf4j
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {
	/** 单次时间预览返回的最多计划点数。 */
	private static final int PREVIEW_TIME_COUNT = 5;

	@Resource
	private XxlJobGroupMapper xxlJobGroupMapper;
	@Resource
	private XxlJobInfoMapper xxlJobInfoMapper;
	@Resource
	private XxlJobService xxlJobService;
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 加载调度任务管理页，准备登录用户可选的执行器组和任务配置枚举。
	 * @param request 当前调度控制台请求
	 * @param model 视图渲染模型
	 * @param jobGroup 页面初始选中的执行器组，默认 -1
	 * @return
	 * 返回值: String
	 * 返回值说明: 任务管理页视图名
	 */
	@RequestMapping
	public String index(HttpServletRequest request, Model model, @RequestParam(value = "jobGroup", required = false, defaultValue = "-1") int jobGroup) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
		model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
		model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

		// 执行器列表
		List<XxlJobGroup> jobGroupListTotal =  xxlJobGroupMapper.findAll();

		// filter group
		List<XxlJobGroup> jobGroupList = JobGroupPermissionUtil.filterJobGroupByPermission(request, jobGroupListTotal);
		if (CollectionTool.isEmpty(jobGroupList)) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}

		// parse jobGroup
		if (!(CollectionTool.isNotEmpty(jobGroupList)
				&& jobGroupList.stream().map(XxlJobGroup::getId).toList().contains(jobGroup))) {
			jobGroup = -1;
		}

		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);

		return "business/job.list";
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:19
	 * 说明: 仅查询当前登录账号有权访问的任务列表，越界分页直接返回错误。
	 * @param request 当前登录请求
	 * @param offset 分页偏移，默认 0
	 * @param pagesize 每页条数，默认 10，最大 500
	 * @param jobGroup 待查询的执行器组
	 * @param triggerStatus 调度状态筛选，负数表示全部
	 * @param name 可选的任务名称筛选
	 * @param executorHandler 可选的 JobHandler 筛选
	 * @param author 可选的负责人筛选
	 * @return
	 * 返回值: Response<PageModel<XxlJobInfo>>
	 * 返回值说明: 分页任务配置及匹配总数
	 */
	@RequestMapping("/pageList")
	@ResponseBody
	public Response<PageModel<XxlJobInfo>> pageList(HttpServletRequest request,
													@RequestParam(required = false, defaultValue = "0") int offset,
													@RequestParam(required = false, defaultValue = "10") int pagesize,
													@RequestParam int jobGroup,
													@RequestParam int triggerStatus,
													@RequestParam String name,
													@RequestParam String executorHandler,
													@RequestParam String author) {

		if (PageQueryUtil.isInvalid(offset, pagesize)) {
			return Response.ofFail(I18nUtil.getString("system_page_invalid"));
		}
		// valid jobGroup permission
		JobGroupPermissionUtil.validJobGroupPermission(request, jobGroup);

		// page
		return xxlJobService.pageList(offset, pagesize, jobGroup, triggerStatus, name, executorHandler, author);
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 提交新任务，校验当前账号对目标执行器组的权限。
	 * @param request 当前登录请求
	 * @param jobInfo 新任务配置
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 任务新增结果
	 */
	@PostMapping("/insert")
	@ResponseBody
	public Response<String> add(HttpServletRequest request, XxlJobInfo jobInfo) {
		// valid permission
		LoginInfo loginInfo = JobGroupPermissionUtil.validJobGroupPermission(request, jobInfo.getJobGroup());

		// opt
		return xxlJobService.add(jobInfo, loginInfo);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 保存任务配置，目标组权限由入口校验，原执行器组权限由业务服务校验。
	 * @param request 当前登录请求
	 * @param jobInfo 待保存的任务配置
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 任务配置保存结果
	 */
	@PostMapping("/update")
	@ResponseBody
	public Response<String> update(HttpServletRequest request, XxlJobInfo jobInfo) {
		// valid permission
		LoginInfo loginInfo = JobGroupPermissionUtil.validJobGroupPermission(request, jobInfo.getJobGroup());

		// opt
		return xxlJobService.update(jobInfo, loginInfo);
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 提交单条任务删除，由业务服务校验任务所属执行器组。
	 * @param request 当前登录请求
	 * @param ids 表单选中的任务 ID，只允许一条
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 任务删除结果
	 */
	@PostMapping("/delete")
	@ResponseBody
	public Response<String> delete(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids) {

		// valid
		if (CollectionTool.isEmpty(ids) || ids.size()!=1) {
			return Response.ofFail(I18nUtil.getString("system_please_choose") + I18nUtil.getString("system_one") + I18nUtil.getString("system_data"));
		}

		// invoke
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		return xxlJobService.remove(ids.get(0), loginInfoResponse.getData());
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 停止单条任务的后续调度，由业务服务校验执行器组权限。
	 * @param request 当前登录请求
	 * @param ids 表单选中的任务 ID，只允许一条
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 停止任务调度的结果
	 */
	@PostMapping("/stop")
	@ResponseBody
	public Response<String> pause(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids) {

		// valid
		if (CollectionTool.isEmpty(ids) || ids.size()!=1) {
			return Response.ofFail(I18nUtil.getString("system_please_choose") + I18nUtil.getString("system_one") + I18nUtil.getString("system_data"));
		}

		// invoke
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		return xxlJobService.stop(ids.get(0), loginInfoResponse.getData());
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 启动单条任务的调度，由业务服务校验执行器组权限。
	 * @param request 当前登录请求
	 * @param ids 表单选中的任务 ID，只允许一条
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 启动任务调度的结果
	 */
	@PostMapping("/start")
	@ResponseBody
	public Response<String> start(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids) {

		// valid
		if (CollectionTool.isEmpty(ids) || ids.size()!=1) {
			return Response.ofFail(I18nUtil.getString("system_please_choose") + I18nUtil.getString("system_one") + I18nUtil.getString("system_data"));
		}

		// invoke
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		return xxlJobService.start(ids.get(0), loginInfoResponse.getData());
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 手动派发一次任务，由业务服务按任务所属组校验权限。
	 * @param request 当前登录请求
	 * @param id 任务 ID
	 * @param executorParam 本次运行的参数，可为空字符串
	 * @param addressList 本次运行指定的执行器地址，可为空字符串
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 手动派发是否被调度中心接收
	 */
	@PostMapping("/trigger")
	@ResponseBody
	public Response<String> triggerJob(HttpServletRequest request,
									  @RequestParam("id") int id,
									  @RequestParam("executorParam") String executorParam,
									  @RequestParam("addressList") String addressList) {
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		return xxlJobService.trigger(loginInfoResponse.getData(), id, executorParam, addressList);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:55
	 * 说明: 对有权限的任务按当前保存配置和调度游标推算计划点。兼容仅传配置的预览请求。
	 * @param request 当前登录请求
	 * @param id 待查询的任务 ID，可省略
	 * @param scheduleType 未传任务 ID 时使用的调度类型
	 * @param scheduleConf 未传任务 ID 时使用的 Cron 表达式或固定间隔秒数
	 * @return
	 * 返回值: Response<List<String>>
	 * 返回值说明: 最多五个未来时间，无未来计划时返回空列表
	 */
	@RequestMapping("/nextTriggerTime")
	@ResponseBody
	public Response<List<String>> nextTriggerTime(HttpServletRequest request, @RequestParam(required = false) Integer id,
												 @RequestParam(required = false) String scheduleType,
												 @RequestParam(required = false) String scheduleConf) {
		if (id == null) {
			XxlJobInfo config = new XxlJobInfo();
			config.setScheduleType(scheduleType);
			config.setScheduleConf(scheduleConf);
			return previewSchedule(config, new Date(), null);
		}

		XxlJobInfo job = id > 0 ? xxlJobInfoMapper.loadById(id) : null;
		if (job == null) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}
		Response<LoginInfo> login = XxlSsoHelper.loginCheckWithAttr(request);
		if (!login.isSuccess() || !JobGroupPermissionUtil.hasJobGroupPermission(login.getData(), job.getJobGroup())) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}

		long now = System.currentTimeMillis();
		boolean running = job.getTriggerStatus() == TriggerStatus.RUNNING.getValue();
		// 正在运行的固定间隔任务有自己的调度相位，不能从点击预览的时间重新起算。
		Date scheduled = running && job.getTriggerNextTime() > now ? new Date(job.getTriggerNextTime()) : null;
		Date start = new Date(running ? now : now + JobScheduleHelper.PRE_READ_MS);
		return previewSchedule(job, start, scheduled);
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:55
	 * 说明: 复用调度策略计算未来计划，避免预览和任务保存使用不同的配置校验规则。
	 * @param job 保存的任务或待预览的调度配置
	 * @param start 没有未来游标时的计算起点
	 * @param scheduled 已保存的未来游标，可为空
	 * @return
	 * 返回值: Response<List<String>>
	 * 返回值说明: 最多五个时间点，配置非法时返回错误
	 */
	private Response<List<String>> previewSchedule(XxlJobInfo job, Date start, Date scheduled) {
		ScheduleTypeEnum type = ScheduleTypeEnum.match(job.getScheduleType(), null);
		Response<String> validation = ScheduleTypeEnum.validateConfiguration(type, job.getScheduleConf());
		if (!validation.isSuccess()) {
			return Response.ofFail(validation.getMsg());
		}
		if (type == ScheduleTypeEnum.NONE) {
			return Response.ofSuccess(List.of());
		}
		String invalid = I18nUtil.getString("schedule_type") + I18nUtil.getString("system_invalid");

		List<String> result = new ArrayList<>(PREVIEW_TIME_COUNT);
		try {
			Date lastTime = start;
			if (scheduled != null) {
				result.add(DateTool.formatDateTime(scheduled));
				lastTime = scheduled;
			}
			while (result.size() < PREVIEW_TIME_COUNT) {
				Date nextTime = type.getScheduleType().generateNextTriggerTime(job, lastTime);
				if (nextTime == null || !nextTime.after(lastTime)) {
					break;
				}
				result.add(DateTool.formatDateTime(nextTime));
				lastTime = nextTime;
			}
		} catch (java.text.ParseException e) {
			return Response.ofFail(invalid);
		} catch (Exception e) {
			log.warn("Failed to preview schedule, type={}, errorType={}", type, e.getClass().getSimpleName());
			return Response.ofFail(invalid);
		}
		return Response.ofSuccess(result);

	}

}
