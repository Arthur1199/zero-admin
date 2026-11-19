package com.zero.jobadmin.framework.controller;

import com.zero.jobadmin.framework.mapper.XxlJobUserMapper;
import com.zero.jobadmin.framework.model.XxlJobUser;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.crypto.Sha256Tool;
import com.xxl.tool.id.UUIDTool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 处理调度控制台的独立登录、退出和当前用户密码修改。
 */
@Controller
@RequestMapping("/auth")
public class LoginController {

	@Resource
	private XxlJobUserMapper xxlJobUserMapper;

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按 SSO Cookie 判断已有会话，有效时直接转向控制台首页。
	 * @param request 控制台访问请求
	 * @param response 供 SSO 检查会话时写入的响应
	 * @param modelAndView 用于设置首页重定向的视图对象
	 * @return
	 * 返回值: ModelAndView
	 * 返回值说明: 首页重定向或登录页视图
	 */
	@RequestMapping("/login")
	@XxlSso(login = false)
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) {

		// xxl-sso, logincheck
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithCookie(request, response);

		if (loginInfoResponse.isSuccess()) {
			modelAndView.setView(new RedirectView("/",true,false));
			return modelAndView;
		}
		return new ModelAndView("framework/base/login");
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 校验调度中心独立账号及密码，成功时通过 XXL-SSO 建立登录会话。
	 * @param request 登录请求
	 * @param response 用于写入 SSO Cookie
	 * @param userName 调度控制台账号
	 * @param password 本次登录密码
	 * @param ifRemember 传 on 时使用记住登录选项
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 登录结果或参数、账号验证失败提示
	 */
	@RequestMapping(value="/doLogin", method=RequestMethod.POST)
	@ResponseBody
	@XxlSso(login=false)
	public Response<String> doLogin(HttpServletRequest request, HttpServletResponse response, String userName, String password, String ifRemember){

		// param
		boolean ifRem = StringTool.isNotBlank(ifRemember) && "on".equals(ifRemember);
		if (StringTool.isBlank(userName) || StringTool.isBlank(password)){
			return Response.ofFail( I18nUtil.getString("login_param_empty") );
		}

		// valid user、status
		XxlJobUser xxlJobUser = xxlJobUserMapper.loadByUserName(userName);
		if (xxlJobUser == null) {
			return Response.ofFail( I18nUtil.getString("login_param_invalid") );
		}

		// valid passowrd
		String passwordHash = Sha256Tool.sha256(password);
		if (!passwordHash.equals(xxlJobUser.getPassword())) {
			return Response.ofFail( I18nUtil.getString("login_param_invalid") );
		}

		// xxl-sso, do login
		LoginInfo loginInfo = new LoginInfo(String.valueOf(xxlJobUser.getId()), UUIDTool.getSimpleUUID());
		Response<String> result= XxlSsoHelper.loginWithCookie(loginInfo, response, ifRem);

		return Response.of(result.getCode(), result.getMsg());
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 通过 XXL-SSO 退出当前调度控制台会话并处理 Cookie。
	 * @param request 包含登录 Cookie 的退出请求
	 * @param response 用于处理登录 Cookie 的响应
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: SSO 退出操作的状态和说明
	 */
	@RequestMapping(value="/logout", method=RequestMethod.POST)
	@ResponseBody
	@XxlSso(login=false)
	public Response<String> logout(HttpServletRequest request, HttpServletResponse response){

		// xxl-sso, do logout
		Response<String> result = XxlSsoHelper.logoutWithCookie(request, response);

		return Response.of(result.getCode(), result.getMsg());
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:13
	 * 说明: 登录用户校验当前密码后仅更新自己的密码，校验后发生重置时拒绝覆盖。
	 * @param request 当前登录请求
	 * @param oldPassword 当前密码，不记录日志
	 * @param password 新密码，去除首尾空白后长度为 4 至 20
	 * @return
	 * 返回值: Response<String>
	 * 返回值说明: 修改结果，旧密码失效或账号不存在时返回相同提示
	 */
	@PostMapping("/updatePwd")
	@ResponseBody
	@XxlSso
	public Response<String> updatePwd(HttpServletRequest request, String oldPassword, String password){

		// valid
		if (oldPassword==null || oldPassword.trim().isEmpty()){
			return Response.ofFail(I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
		}
		if (password==null || password.trim().isEmpty()){
			return Response.ofFail(I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_newpwd"));
		}
		password = password.trim();
		if (!(password.length()>=4 && password.length()<=20)) {
			return Response.ofFail(I18nUtil.getString("system_length_limit")+"[4-20]" );
		}

		String oldPasswordHash = Sha256Tool.sha256(oldPassword);
		String passwordHash = Sha256Tool.sha256(password);

		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		LoginInfo login = loginInfoResponse.getData();
		if (!loginInfoResponse.isSuccess() || login == null) {
			return Response.ofFail(I18nUtil.getString("system_permission_limit"));
		}
		int userId = Integer.parseInt(login.getUserId());
		XxlJobUser existing = xxlJobUserMapper.loadById(userId);
		if (existing == null || !oldPasswordHash.equals(existing.getPassword())) {
			return Response.ofFail(I18nUtil.getString("change_pwd_field_oldpwd") + I18nUtil.getString("system_invalid"));
		}
		if (!oldPasswordHash.equals(passwordHash)
				&& xxlJobUserMapper.changePassword(userId, oldPasswordHash, passwordHash) != 1) {
			return Response.ofFail(I18nUtil.getString("change_pwd_field_oldpwd") + I18nUtil.getString("system_invalid"));
		}

		return Response.ofSuccess();
	}

}
