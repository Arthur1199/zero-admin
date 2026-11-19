package com.zero.jobadmin.business.controller;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogGlueMapper;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLogGlue;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.zero.jobadmin.framework.util.XssUtil;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.crypto.Sha256Tool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * job code controller
 *
 * @author xuxueli 2015-12-19 16:13:16
 */
@Slf4j
@Controller
@RequestMapping("/jobcode")
public class JobCodeController {
	
	@Resource
	private XxlJobInfoMapper xxlJobInfoMapper;
	@Resource
	private XxlJobLogGlueMapper xxlJobLogGlueMapper;

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:50
	 * 说明: 校验执行器组权限，在同一数据库快照中加载 GLUE 源码和版本历史。
	 * @param request 当前登录请求
	 * @param model GLUE 编辑器视图模型
	 * @param jobId 任务 ID
	 * @return
	 * 返回值: String
	 * 返回值说明: GLUE 编辑器模板路径
	 */
	@RequestMapping
	@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
	public String index(HttpServletRequest request, Model model, @RequestParam("jobId") int jobId) {
		XxlJobInfo jobInfo = xxlJobInfoMapper.loadById(jobId);

		if (jobInfo == null) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}
		JobGroupPermissionUtil.validJobGroupPermission(request, jobInfo.getJobGroup());
		GlueTypeEnum glueType = GlueTypeEnum.match(jobInfo.getGlueType());
		if (glueType == null || glueType == GlueTypeEnum.BEAN) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_gluetype_invalid"));
		}

		// Glue类型-字典
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());

		List<XxlJobLogGlue> history = xxlJobLogGlueMapper.findByJobId(jobId);
		Integer latestVersionId = history.isEmpty() ? null : history.get(0).getId();
		model.addAttribute("jobInfo", jobInfo);
		model.addAttribute("glueVersion", glueVersion(jobInfo, latestVersionId));
		model.addAttribute("jobLogGlues", history);
		return "business/job.code";
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:50
	 * 说明: 持有任务行锁时验证组权限和最新版本，更新 GLUE 脚本并保存历史。
	 * @param request 当前登录请求
	 * @param id 任务 ID
	 * @param glueSource GLUE 源码
	 * @param glueRemark 保存备注，去除首尾空白后为 4 至 100 个字符
	 * @param glueVersion 打开编辑器时的版本摘要
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 保存结果或版本冲突等失败原因
	 */
	@PostMapping("/save")
	@ResponseBody
	@Transactional
	public Response<String> save(HttpServletRequest request,
								 @RequestParam("id") int id,
								 @RequestParam("glueSource") String glueSource,
								 @RequestParam("glueRemark") String glueRemark,
								 @RequestParam("glueVersion") String glueVersion) {

		// valid
		if (StringTool.isBlank(glueSource)) {
			return Response.ofFail( (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_glue_source")) );
		}
		if (StringTool.isBlank(glueRemark)) {
			return Response.ofFail( (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_glue_remark")) );
		}
		glueRemark = glueRemark.trim();
		if (glueRemark.length()<4 || glueRemark.length()>100) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_remark") + I18nUtil.getString("system_length_limit") + " 4~100");
		}
		if (XssUtil.hasXss(glueRemark)) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_remark") + I18nUtil.getString("system_invalid"));
		}
		XxlJobInfo existsJobInfo = xxlJobInfoMapper.loadByIdForUpdate(id);
		if (existsJobInfo == null) {
			return Response.ofFail( I18nUtil.getString("jobinfo_glue_jobid_invalid"));
		}

		// valid jobGroup permission
		LoginInfo loginInfo = JobGroupPermissionUtil.validJobGroupPermission(request, existsJobInfo.getJobGroup());
		GlueTypeEnum glueType = GlueTypeEnum.match(existsJobInfo.getGlueType());
		if (glueType == null || glueType == GlueTypeEnum.BEAN) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_gluetype_invalid"));
		}
		Integer latestVersionId = xxlJobLogGlueMapper.findLatestVersionId(id);
		if (!glueVersion(existsJobInfo, latestVersionId).equals(glueVersion)) {
			return Response.ofFail(I18nUtil.getString("jobinfo_glue_conflict"));
		}

		Date now = new Date();
		existsJobInfo.setGlueSource(glueSource);
		existsJobInfo.setGlueRemark(glueRemark);
		existsJobInfo.setGlueUpdatetime(now);
		existsJobInfo.setUpdateTime(now);
		if (xxlJobInfoMapper.updateGlue(existsJobInfo) != 1) {
			throw new IllegalStateException(I18nUtil.getString("system_opt_fail"));
		}

		// 每次保存都追加历史，以独立的主键区分内容相同的版本。
		XxlJobLogGlue xxlJobLogGlue = new XxlJobLogGlue();
		xxlJobLogGlue.setJobId(existsJobInfo.getId());
		xxlJobLogGlue.setGlueType(existsJobInfo.getGlueType());
		xxlJobLogGlue.setGlueSource(glueSource);
		xxlJobLogGlue.setGlueRemark(glueRemark);

		xxlJobLogGlue.setAddTime(now);
		xxlJobLogGlue.setUpdateTime(now);
		if (xxlJobLogGlueMapper.save(xxlJobLogGlue) != 1) {
			throw new IllegalStateException(I18nUtil.getString("system_opt_fail"));
		}

		// remove code backup more than 30
		xxlJobLogGlueMapper.removeOld(existsJobInfo.getId(), 30);

		// write operation log
		log.info(">>>>>>>>>>> xxl-job operation log: operator = {}, type = jobcode-update, jobId = {}",
				loginInfo.getUserName(), id);
		return Response.ofSuccess();
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:08
	 * 说明: 将历史主键纳入摘要，源码和备注恢复成旧内容后仍能识别过期窗口。
	 * @param job 数据库中的任务快照
	 * @param latestVersionId 最新历史主键，从未保存过时为空
	 * @return
	 * 返回值: String
	 * 返回值说明: 历史主键、GLUE 模式、源码和备注的 SHA-256 摘要
	 */
	private String glueVersion(XxlJobInfo job, Integer latestVersionId) {
		String source = Objects.toString(job.getGlueSource(), "");
		return Sha256Tool.sha256(latestVersionId + ":" + job.getGlueType() + ":" + source.length() + ":" + source
				+ Objects.toString(job.getGlueRemark(), ""));
	}
}
