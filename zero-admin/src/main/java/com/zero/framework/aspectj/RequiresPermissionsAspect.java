package com.zero.framework.aspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import com.zero.common.annotation.Logical;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.framework.web.service.PermissionService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 权限注解切面，处理RequiresPermissions标注的方法访问校验，支持AND和OR两种权限组合规则
 */
@Aspect
@Component
public class RequiresPermissionsAspect
{
    @Autowired
    private PermissionService permissionService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 方法执行前校验RequiresPermissions权限注解
     * @param requiresPermissions 权限注解，包含权限标识和逻辑关系
     * @return
     * 返回值: void
     * 返回值说明: 权限校验通过继续执行，未通过抛出AccessDeniedException
     */
    @Before("@annotation(requiresPermissions)")
    public void doBefore(RequiresPermissions requiresPermissions)
    {
        checkPermissions(requiresPermissions);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 按注解配置的逻辑关系分发权限校验
     * @param requiresPermissions 权限注解，value不能为空
     * @return
     * 返回值: void
     * 返回值说明: 根据AND或OR规则完成校验，权限为空或未通过时抛出AccessDeniedException
     */
    private void checkPermissions(RequiresPermissions requiresPermissions)
    {
        String[] permissions = requiresPermissions.value();
        if (permissions == null || permissions.length == 0)
        {
            throw new AccessDeniedException("权限标识不能为空");
        }
        if (requiresPermissions.logical() == Logical.OR)
        {
            checkAnyPermission(permissions);
            return;
        }
        checkAllPermissions(permissions);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 校验用户是否同时拥有全部权限标识
     * @param permissions 权限标识数组
     * @return
     * 返回值: void
     * 返回值说明: 全部权限通过无返回，任一权限缺失时抛出AccessDeniedException
     */
    private void checkAllPermissions(String[] permissions)
    {
        for (String permission : permissions)
        {
            if (!permissionService.hasPermi(permission))
            {
                throw new AccessDeniedException("无权限：" + permission);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 校验用户是否拥有任意一个权限标识
     * @param permissions 权限标识数组
     * @return
     * 返回值: void
     * 返回值说明: 任一权限通过无返回，全部缺失时抛出AccessDeniedException
     */
    private void checkAnyPermission(String[] permissions)
    {
        for (String permission : permissions)
        {
            if (permissionService.hasPermi(permission))
            {
                return;
            }
        }
        throw new AccessDeniedException("无权限");
    }
}
