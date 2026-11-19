package com.zero.jobadmin.framework.util;

import com.zero.jobadmin.framework.constant.Consts;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 统一判断当前登录人可操作和查看的执行器组
 */
public class JobGroupPermissionUtil {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:43
     * 说明: 管理员可访问所有执行器组，普通用户只能访问已授权组
     * @param loginInfo 登录信息，可为空
     * @param jobGroup 执行器组 ID
     * @return
     * 返回值: boolean
     * 返回值说明: 具备执行器组权限时为 true
     */
    public static boolean hasJobGroupPermission(LoginInfo loginInfo, int jobGroup){
        return loginInfo != null && (XxlSsoHelper.hasRole(loginInfo, Consts.ADMIN_ROLE).isSuccess()
                || allowedJobGroups(loginInfo).contains(String.valueOf(jobGroup)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:43
     * 说明: 拒绝未登录或没有指定执行器组权限的请求
     * @param request 当前 HTTP 请求
     * @param jobGroup 执行器组 ID
     * @return
     * 返回值: LoginInfo
     * 返回值说明: 通过权限校验的登录信息
     */
    public static LoginInfo validJobGroupPermission(HttpServletRequest request, int jobGroup) {
        Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
        if (!loginInfoResponse.isSuccess() || !hasJobGroupPermission(loginInfoResponse.getData(), jobGroup)) {
            throw new IllegalArgumentException(I18nUtil.getString("system_permission_limit"));
        }
        return loginInfoResponse.getData();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:43
     * 说明: 过滤当前登录人可见的执行器组，缺少登录信息时返回空列表
     * @param request 当前 HTTP 请求
     * @param jobGroupListTotal 待过滤的执行器组，可为空
     * @return
     * 返回值: List<XxlJobGroup>
     * 返回值说明: 当前登录人可见的执行器组
     */
    public static List<XxlJobGroup> filterJobGroupByPermission(HttpServletRequest request, List<XxlJobGroup> jobGroupListTotal){
        Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
        LoginInfo loginInfo = loginInfoResponse.getData();
        if (!loginInfoResponse.isSuccess() || loginInfo == null || jobGroupListTotal == null) {
            return List.of();
        }
        if (XxlSsoHelper.hasRole(loginInfo, Consts.ADMIN_ROLE).isSuccess()) {
            return jobGroupListTotal;
        }
        List<String> jobGroups = allowedJobGroups(loginInfo);
        return jobGroupListTotal.stream().filter(jobGroup -> jobGroups.contains(String.valueOf(jobGroup.getId()))).toList();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从普通用户的登录扩展信息读取执行器组权限。
     * @param loginInfo 当前调度控制台的登录信息
     * @return
     * 返回值: List<String>
     * 返回值说明: 授权的执行器组 ID 列表，配置为空时返回空列表
     */
    private static List<String> allowedJobGroups(LoginInfo loginInfo) {
        String groupIds = loginInfo.getExtraInfo() == null ? null : loginInfo.getExtraInfo().get("jobGroups");
        return StringTool.isBlank(groupIds) ? List.of() : StringTool.split(groupIds, ",");
    }

}
