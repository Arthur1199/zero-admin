package com.zero.framework.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.utils.StringUtils;
import com.zero.system.service.ISysMenuService;
import com.zero.system.service.ISysRoleService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户权限处理
 */
@Component
public class SysPermissionService
{
    /**
     * 角色服务字段
     */
    @Autowired
    private ISysRoleService roleService;
    /**
     * 菜单服务字段
     */

    @Autowired
    private ISysMenuService menuService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色数据权限
     * @param user 用户信息
     * @return
     * 返回值: 角色权限信息
     * 返回值说明：获取角色数据权限
     */
    public Set<String> getRolePermission(SysUser user)
    {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        if (user.isAdmin())
        {
            roles.add("admin");
        }
        else
        {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取菜单数据权限
     * @param user 用户信息
     * @return
     * 返回值: 菜单权限信息
     * 返回值说明：获取菜单数据权限
     */
    public Set<String> getMenuPermission(SysUser user)
    {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        if (user.isAdmin())
        {
            perms.add("*:*:*");
        }
        else
        {
            List<SysRole> roles = user.getRoles();
            if (!CollectionUtils.isEmpty(roles))
            {
                // 多角色设置permissions属性，以便数据权限匹配权限
                for (SysRole role : roles)
                {
                    if (StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL) && !role.isAdmin())
                    {
                        Set<String> rolePerms = menuService.selectMenuPermsByRoleId(role.getRoleId());
                        role.setPermissions(rolePerms);
                        perms.addAll(rolePerms);
                    }
                }
            }
            else
            {
                perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
            }
        }
        return perms;
    }
}
