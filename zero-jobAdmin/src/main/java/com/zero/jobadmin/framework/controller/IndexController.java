package com.zero.jobadmin.framework.controller;

import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.dto.JobDashboardReport;
import com.zero.jobadmin.business.service.JobDashboardService;
import com.zero.jobadmin.framework.constant.Consts;
import com.zero.jobadmin.framework.model.dto.XxlBootResourceDTO;
import com.zero.jobadmin.business.service.XxlJobService;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * index controller
 *
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

	@Resource
	private XxlJobService xxlJobService;
	@Resource
	private XxlJobGroupMapper xxlJobGroupMapper;
	@Resource
	private JobDashboardService jobDashboardService;

	/**
	 * index
	 */
	@RequestMapping("/")
	@XxlSso
	public String index(HttpServletRequest request, Model model) {

		// menu resource
		List<XxlBootResourceDTO> resourceList = findResourceList(request);
		model.addAttribute("resourceList", resourceList);

		return "framework/base/index";
	}

	/**
	 * fill menu data
	 */
	private List<XxlBootResourceDTO> findResourceList(HttpServletRequest request){
		// login check
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		// init menu-list
		List<XxlBootResourceDTO> resourceDTOList = Arrays.asList(
				new XxlBootResourceDTO(1, 0, I18nUtil.getString("job_dashboard_name"),1, "", "/dashboard", "fa-home", 1, 0, null),
				new XxlBootResourceDTO(2, 0, I18nUtil.getString("jobinfo_job") + I18nUtil.getString("system_manage"),1, "", "/jobinfo", " fa-clock-o", 2, 0, null),
				new XxlBootResourceDTO(3, 0, I18nUtil.getString("joblog_name"),1, "", "/joblog", " fa-database", 3, 0, null),
				new XxlBootResourceDTO(4, 0, I18nUtil.getString("jobgroup") + I18nUtil.getString("system_manage"),1, Consts.ADMIN_ROLE, "/jobgroup", " fa-cloud", 4, 0,null),
				new XxlBootResourceDTO(5, 0, I18nUtil.getString("user_name") + I18nUtil.getString("system_manage"),1, Consts.ADMIN_ROLE, "/user", "fa-users", 5, 0, null),
				new XxlBootResourceDTO(9, 0, I18nUtil.getString("admin_help"),1, "", "/help", "fa-book", 6, 0, null)
		);

		// filter by role
		if (!XxlSsoHelper.hasRole(loginInfoResponse.getData(), Consts.ADMIN_ROLE).isSuccess()) {
			resourceDTOList = resourceDTOList.stream()
					.filter(resourceDTO -> StringTool.isBlank(resourceDTO.getPermission() ))	// normal user had no permission
					.collect(Collectors.toList());
		}
		resourceDTOList.stream().sorted(Comparator.comparing(XxlBootResourceDTO::getOrder)).toList();
		return resourceDTOList;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:08
	 * 说明: 保留历史报表概况，新增报表仅提供当前账号可用的执行器组
	 * @param request 用于获取当前登录人
	 * @param model 报表视图参数
	 * @return
	 * 返回值: String
	 * 返回值说明: 运行报表模板路径
	 */
	@RequestMapping("/dashboard")
	@XxlSso
	public String dashboard(HttpServletRequest request, Model model) {

		model.addAllAttributes(xxlJobService.dashboardInfo());
		model.addAttribute("reportGroups", JobGroupPermissionUtil.filterJobGroupByPermission(request, xxlJobGroupMapper.findAll()));
		model.addAttribute("reportToday", LocalDate.now(JobDashboardService.REPORT_ZONE).toString());
		model.addAttribute("reportMaxDays", JobDashboardService.MAX_DAYS);

		return "framework/base/dashboard";
	}

	@RequestMapping("/chartInfo")
	@XxlSso
	@ResponseBody
	public Response<Map<String, Object>> chartInfo(@RequestParam("startDate") Date startDate, @RequestParam("endDate") Date endDate) {
		Response<Map<String, Object>> chartInfo = xxlJobService.chartInfo(startDate, endDate);
		return chartInfo;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:08
	 * 说明: 按调度自然日查询当前账号可见的趋势、失败榜和待回调记录
	 * @param request 用于确认可访问的执行器组
	 * @param startDate 首日，格式 yyyy-MM-dd
	 * @param endDate 末日，含当天，最多查询 31 天
	 * @param jobGroup 执行器组 ID，0 表示全部有权限的组
	 * @param page 待回调页码，默认 1
	 * @return
	 * 返回值: Response<JobDashboardReport>
	 * 返回值说明: 无权限的执行器组或无效日期返回错误信息
	 */
	@GetMapping("/dashboard/report")
	@XxlSso
	@ResponseBody
	public Response<JobDashboardReport> report(HttpServletRequest request,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "0") int jobGroup,
			@RequestParam(defaultValue = "1") int page) {
		if (startDate.isAfter(endDate) || endDate.isAfter(LocalDate.now(JobDashboardService.REPORT_ZONE))
				|| ChronoUnit.DAYS.between(startDate, endDate) >= JobDashboardService.MAX_DAYS || page < 1) {
			return Response.ofFail(I18nUtil.getString("job_report_invalid_range"));
		}
		List<Integer> groups = JobGroupPermissionUtil.filterJobGroupByPermission(request, xxlJobGroupMapper.findAll())
				.stream().map(XxlJobGroup::getId).toList();
		if (jobGroup != 0) {
			if (!groups.contains(jobGroup)) {
				return Response.ofFail(I18nUtil.getString("system_permission_limit"));
			}
			groups = List.of(jobGroup);
		}
		return Response.ofSuccess(jobDashboardService.report(startDate, endDate, groups, page));
	}

	/**
	 * help
	 */
	@RequestMapping("/help")
	@XxlSso
	public String help() {
		return "framework/base/help";
	}

	@RequestMapping(value = "/errorpage")
	@XxlSso(login = false)
	public ModelAndView errorPage(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) {

		String exceptionMsg = "HTTP Status Code: "+response.getStatus();

		mv.addObject("exceptionMsg", exceptionMsg);
		mv.setViewName("framework/common/common.errorpage");
		return mv;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
}
