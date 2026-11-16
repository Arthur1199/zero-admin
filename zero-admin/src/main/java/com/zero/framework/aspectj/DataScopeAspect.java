package com.zero.framework.aspectj;

import java.util.ArrayList;
import java.util.List;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zero.common.exception.ServiceException;
import com.zero.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.zero.common.annotation.DataScope;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.text.Convert;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.framework.security.context.PermissionContextHolder;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 数据过滤处理
 */
@Aspect
@Component
public class DataScopeAspect
{
    /**
     * 全部数据权限
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 自定数据权限
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 部门数据权限
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 部门及以下数据权限
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 仅本人数据权限
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";

    /** 当前请求内的部门ID缓存前缀，后缀为已认证用户ID，不跨请求保存 */
    private static final String CURRENT_DEPARTMENT_ATTRIBUTE = DataScopeAspect.class.getName() + ".department.";

    /** 仅按当前用户主键读取最新部门，避免调用带数据权限的Service引发切面递归 */
    @Autowired
    private SysUserMapper userMapper;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: doBefore
     * @param point point参数
     * @param controllerDataScope 控制器数据Scope参数
     * @return
     * 返回值: void
     * 返回值说明：doBefore
     */

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) throws Throwable
    {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 按登录角色过滤数据，部门范围使用当前账号的最新部门，不改写登录缓存或角色授权
     * @param joinPoint joinPoint参数
     * @param controllerDataScope 控制器数据Scope参数
     * @return
     * 返回值: void
     * 返回值说明：处理数据Scope
     */

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope)
    {
        // 获取当前的用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser))
        {
            SysUser currentUser = loginUser.getUser();
            // 如果是超级管理员，则不过滤数据
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin())
            {
                String permission = StringUtils.defaultIfEmpty(controllerDataScope.permission(), PermissionContextHolder.getContext());
                SysUser scopedUser = resolveDepartmentUser(currentUser, permission);
                dataScopeFilter(joinPoint, scopedUser, controllerDataScope.deptAlias(), controllerDataScope.userAlias(), permission);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 仅部门相关范围读取最新部门，全部、自定义或本人范围保持原规则，不修改共享登录对象
     * @param user 登录快照中的已认证用户与角色
     * @param permission 当前查询涉及的权限标识
     * @return
     * 返回值: SysUser
     * 返回值说明: 部门范围使用临时身份副本，其它范围返回原对象
     */
    private SysUser resolveDepartmentUser(SysUser user, String permission)
    {
        boolean departmentScope = false;
        if (user.getRoles() != null)
        {
            for (SysRole role : user.getRoles())
            {
                if (role == null || UserConstants.ROLE_DISABLE.equals(role.getStatus())
                    || !StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission))) continue;
                if (DATA_SCOPE_ALL.equals(role.getDataScope())) return user;
                if (DATA_SCOPE_DEPT.equals(role.getDataScope()) || DATA_SCOPE_DEPT_AND_CHILD.equals(role.getDataScope())) departmentScope = true;
            }
        }
        if (!departmentScope) return user;
        if (user.getUserId() == null) throw new ServiceException("当前账号信息不完整，请重新登录");
        SysUser scopedUser = new SysUser();
        scopedUser.setUserId(user.getUserId());
        scopedUser.setRoles(user.getRoles());
        scopedUser.setDeptId(loadCurrentDepartment(user.getUserId()));
        return scopedUser;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 每个请求只按主键读取一次最新部门，临时挂起分页，避免辅助查询消耗列表分页上下文
     * @param userId 当前认证用户ID
     * @return
     * 返回值: Long
     * 返回值说明: 当前部门ID；读取失败不使用旧部门，异常时清理未消费的分页防止线程复用污染
     */
    private Long loadCurrentDepartment(Long userId)
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        String attribute = CURRENT_DEPARTMENT_ATTRIBUTE + userId;
        Object cached = attributes == null ? null : attributes.getAttribute(attribute, RequestAttributes.SCOPE_REQUEST);
        if (cached instanceof Long) return (Long) cached;
        Page<?> pendingPage = PageHelper.getLocalPage();
        PageHelper.clearPage();
        boolean resolved = false;
        try
        {
            Long departmentId = userMapper.selectCurrentDeptIdByUserId(userId);
            if (departmentId == null) throw new ServiceException("当前账号所属部门不存在，无法查询部门范围数据");
            if (attributes != null) attributes.setAttribute(attribute, departmentId, RequestAttributes.SCOPE_REQUEST);
            resolved = true;
            return departmentId;
        }
        finally
        {
            PageHelper.clearPage();
            if (resolved && pendingPage != null) PageHelper.setLocalPage(pendingPage);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 数据范围过滤
     * @param joinPoint 切点
     * @param user 用户
     * @param deptAlias 部门别名
     * @param userAlias 用户别名
     * @param permission 权限字符
     * @return
     * 返回值: void
     * 返回值说明：数据范围过滤
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, String deptAlias, String userAlias, String permission)
    {
        StringBuilder sqlString = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        List<String> scopeCustomIds = new ArrayList<String>();
        user.getRoles().forEach(role -> {
            if (DATA_SCOPE_CUSTOM.equals(role.getDataScope()) && StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL) && StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                scopeCustomIds.add(Convert.toStr(role.getRoleId()));
            }
        });

        for (SysRole role : user.getRoles())
        {
            String dataScope = role.getDataScope();
            if (conditions.contains(dataScope) || StringUtils.equals(role.getStatus(), UserConstants.ROLE_DISABLE))
            {
                continue;
            }
            if (!StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                continue;
            }
            if (DATA_SCOPE_ALL.equals(dataScope))
            {
                sqlString = new StringBuilder();
                conditions.add(dataScope);
                break;
            }
            else if (DATA_SCOPE_CUSTOM.equals(dataScope))
            {
                if (scopeCustomIds.size() > 1)
                {
                    // 多个自定数据权限使用in查询，避免多次拼接。
                    sqlString.append(StringUtils.format(" OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id in ({}) ) ", deptAlias, String.join(",", scopeCustomIds)));
                }
                else
                {
                    sqlString.append(StringUtils.format(" OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias, role.getRoleId()));
                }
            }
            else if (DATA_SCOPE_DEPT.equals(dataScope))
            {
                sqlString.append(StringUtils.format(" OR {}.dept_id = {} ", deptAlias, user.getDeptId()));
            }
            else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope))
            {
                sqlString.append(StringUtils.format(" OR {}.dept_id IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} or find_in_set( {} , ancestors ) )", deptAlias, user.getDeptId(), user.getDeptId()));
            }
            else if (DATA_SCOPE_SELF.equals(dataScope))
            {
                if (StringUtils.isNotBlank(userAlias))
                {
                    sqlString.append(StringUtils.format(" OR {}.user_id = {} ", userAlias, user.getUserId()));
                }
                else
                {
                    // 数据权限为仅本人且没有userAlias别名不查询任何数据
                    sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
                }
            }
            conditions.add(dataScope);
        }

        // 角色都不包含传递过来的权限字符，这个时候sqlString也会为空，所以要限制一下,不查询任何数据
        if (StringUtils.isEmpty(conditions))
        {
            sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
        }

        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
            {
                BaseEntity baseEntity = (BaseEntity) params;
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 拼接权限sql前先清空params.dataScope参数防止注入
     * @param joinPoint joinPoint参数
     * @return
     * 返回值: void
     * 返回值说明：拼接权限sql前先清空params.dataScope参数防止注入
     */
    private void clearDataScope(final JoinPoint joinPoint)
    {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
        {
            BaseEntity baseEntity = (BaseEntity) params;
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }
}
