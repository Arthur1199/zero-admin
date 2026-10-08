package com.zero.feign;

import com.zero.common.constant.ForwardHeaderConstants;
import com.zero.common.core.domain.entity.SysDept;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.system.service.ISysDeptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:25
 * 说明: 后台跨服务请求头构建器，统一封装 admin 调用 report 时需要透传的登录用户、公司、角色和客户端IP上下文
 */
@Component
public class AdminForwardHeaderProvider
{
    /**
     * 部门服务，用于按当前用户部门计算公司名称
     */
    private final ISysDeptService deptService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 构造后台跨服务请求头构建器，通过构造注入保证依赖不可变
     * @param deptService 部门服务
     */
    public AdminForwardHeaderProvider(ISysDeptService deptService)
    {
        this.deptService = deptService;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 创建 admin 调用下游服务时需要透传的管理员上下文；无 Web 请求或未登录时只保留可安全获取的基础上下文
     * @return
     * 返回值: AdminForwardContext
     * 返回值说明: 管理员跨服务调用上下文，可继续转换为请求头
     */
    public AdminForwardContext createForwardContext()
    {
        AdminForwardContext context = new AdminForwardContext();
        context.setSourceService(ForwardHeaderConstants.ADMIN_SOURCE_SERVICE);

        HttpServletRequest request = getRequest();
        if (request != null)
        {
            context.setAuthorization(request.getHeader(ForwardHeaderConstants.AUTHORIZATION));
            context.setClientIp(IpUtils.getIpAddr(request));
        }

        LoginUser loginUser = getLoginUserSafely();
        if (loginUser == null || loginUser.getUser() == null)
        {
            return context;
        }

        SysUser user = loginUser.getUser();
        Long userId = loginUser.getUserId();
        String username = loginUser.getUsername();
        String nickName = user.getNickName();
        boolean admin = SecurityUtils.isAdmin(userId);

        context.setAdminId(userId == null ? "" : String.valueOf(userId));
        context.setAdminName(username);
        context.setAdmin(admin);
        context.setAdminNickName(nickName);
        context.setAdminCompanyName(resolveCompanyName(user, admin));
        context.setAdminRoleIds(resolveRoleIds(user));
        return context;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 获取当前线程绑定的 HTTP 请求；兼容异步任务、启动期调用和非 Servlet 请求上下文
     * @return
     * 返回值: HttpServletRequest
     * 返回值说明: 当前请求对象，不存在时返回 null
     */
    private HttpServletRequest getRequest()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes))
        {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 安全获取当前登录用户，避免匿名请求或异步线程调用 SecurityUtils 时抛出认证异常
     * @return
     * 返回值: LoginUser
     * 返回值说明: 当前登录用户，无法获取时返回 null
     */
    private LoginUser getLoginUserSafely()
    {
        Authentication authentication = SecurityUtils.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser))
        {
            return null;
        }
        return loginUser;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 计算当前用户所属公司名称；超级管理员不绑定公司，普通用户按部门祖级取公司层级
     * @param user 当前登录用户
     * @param admin 当前用户是否为超级管理员
     * @return
     * 返回值: String
     * 返回值说明: 公司名称，无法识别时返回空字符串
     */
    private String resolveCompanyName(SysUser user, boolean admin)
    {
        if (admin || user == null)
        {
            return "";
        }
        Long deptId = user.getDeptId();
        if (deptId == null && user.getDept() != null)
        {
            deptId = user.getDept().getDeptId();
        }
        if (deptId == null)
        {
            return "";
        }

        SysDept dept = deptService.selectDeptById(deptId);
        if (dept == null || !StringUtils.hasText(dept.getAncestors()))
        {
            return dept == null ? "" : nullToEmpty(dept.getDeptName());
        }

        String[] ancestors = dept.getAncestors().split(",");
        if (ancestors.length == 2)
        {
            return nullToEmpty(dept.getDeptName());
        }
        if (ancestors.length >= 3)
        {
            return resolveDeptName(ancestors[2]);
        }
        return "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 根据部门ID字符串读取部门名称，兼容历史祖级数据为空或非数字的情况
     * @param deptIdText 部门ID文本
     * @return
     * 返回值: String
     * 返回值说明: 部门名称，无法读取时返回空字符串
     */
    private String resolveDeptName(String deptIdText)
    {
        if (!StringUtils.hasText(deptIdText))
        {
            return "";
        }
        try
        {
            SysDept dept = deptService.selectDeptById(Long.valueOf(deptIdText));
            return dept == null ? "" : nullToEmpty(dept.getDeptName());
        }
        catch (NumberFormatException e)
        {
            return "";
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 03:10
     * 说明: 获取当前管理员全部角色ID，供 report 侧按管理员角色上下文处理业务
     * @param user 当前登录用户
     * @return
     * 返回值: List<Long>
     * 返回值说明: 管理员角色ID列表，未配置角色时返回空列表
     */
    private List<Long> resolveRoleIds(SysUser user)
    {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty())
        {
            return List.of();
        }
        return user.getRoles().stream()
                .filter(role -> role != null && role.getRoleId() != null)
                .map(role -> role.getRoleId())
                .toList();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 字符串空值兜底，避免请求头构建过程中出现 null 文本
     * @param value 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 非空原值或空字符串
     */
    private String nullToEmpty(String value)
    {
        return value == null ? "" : value;
    }
}
