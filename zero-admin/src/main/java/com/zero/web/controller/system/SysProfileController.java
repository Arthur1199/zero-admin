package com.zero.web.controller.system;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.zero.common.annotation.Log;
import com.zero.common.config.ZeroConfig;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.BusinessType;
import com.zero.common.utils.DateUtils;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.file.FileUtils;
import com.zero.framework.web.service.PermissionService;
import com.zero.framework.web.service.TokenService;
import com.zero.system.service.IAdminFileUploadService;
import com.zero.system.service.ISysUserService;
import com.zero.system.service.impl.SysProfileStatisticsService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 个人信息 业务处理
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController
{
    /** 备忘录菜单的查看权限标识。 */
    private static final String MEMO_LIST_PERMISSION = "systemTools:memo:list";

    /**
     * 用户服务字段
     */
    @Autowired
    private ISysUserService userService;
    /**
     * 令牌服务字段
     */

    @Autowired
    private TokenService tokenService;

    @Autowired
    private IAdminFileUploadService adminFileUploadService;

    /** 个人中心操作和备忘录统计服务。 */
    @Autowired
    private SysProfileStatisticsService profileStatisticsService;

    /** 复用管理后台的菜单权限校验。 */
    @Autowired
    private PermissionService permissionService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 个人信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明：个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
        return ajax;
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:16
     * 说明: 登录后查看本人操作统计，账号身份只从认证上下文获取
     * @param response 禁止缓存个人操作记录的响应
     * @return 近七日操作统计和五条最近操作的展示字段
     */
    @GetMapping("/statistics/operations")
    public AjaxResult operationStatistics(HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store");
        return success(profileStatisticsService.getOperations(getLoginUser().getUser()));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 20:12
     * 说明: 登录后查询当前会话的信息，只读取当前请求和登录态
     * @param request 当前请求，用于解析 IP 和设备
     * @param response 禁止缓存包含客户端信息的响应
     * @return 在线秒数、当前 IP、地区、浏览器和操作系统
     */
    @GetMapping("/statistics/session")
    public AjaxResult sessionStatistics(HttpServletRequest request, HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store");
        return success(profileStatisticsService.getSession(getLoginUser(), request));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 19:50
     * 说明: 返回登录用户的备忘录查看权限，有菜单权限时才查询本人创建的备忘录
     * @param response 禁止缓存备忘录元数据的响应
     * @return 查看权限标识，有权限时同时返回本人备忘录统计
     */
    @GetMapping("/statistics/memos")
    public AjaxResult memoStatistics(HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store");
        if (!permissionService.hasPermi(MEMO_LIST_PERMISSION))
        {
            return success(Map.of("canReadMemos", false));
        }
        return success(Map.of("canReadMemos", true, "statistics", profileStatisticsService.getMemos(getUserId())));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改用户
     * @param user 用户参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getUser();
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser) > 0)
        {
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 重置密码
     * @param params 参数集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = getLoginUser();
        Long userId = loginUser.getUserId();
        String password = loginUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userId, newPassword) > 0)
        {
            // 更新缓存用户密码&密码最后更新时间
            loginUser.getUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 头像上传
     * @param file 文件参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception
    {
        if (!file.isEmpty())
        {
            LoginUser loginUser = getLoginUser();
            String avatar = adminFileUploadService.uploadAvatar(file);
            if (userService.updateUserAvatar(loginUser.getUserId(), avatar))
            {
                String oldAvatar = loginUser.getUser().getAvatar();
                if (StringUtils.isNotEmpty(oldAvatar) && !StringUtils.ishttp(oldAvatar))
                {
                    FileUtils.deleteFile(ZeroConfig.getProfile() + FileUtils.stripPrefix(oldAvatar));
                }
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
