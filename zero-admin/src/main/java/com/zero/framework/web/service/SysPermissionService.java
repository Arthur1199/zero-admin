package com.zero.framework.web.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:04
     * 说明: 在接口鉴权前读取数据库中已提交的角色权限，禁用旧会话快照中的授权。
     * @param loginUser 已验证的登录会话，用户对象和用户ID来自现有会话
     * @return 会话权限是否已更新，需要调用方将更新后的快照写回
     */
    public boolean refreshRoleAuthorization(LoginUser loginUser)
    {
        SysUser user = loginUser.getUser();
        if (user.isAdmin())
        {
            return false;
        }
        List<SysRole> currentRoles = menuService.selectRoleAuthorizationsByUserId(loginUser.getUserId());
        Set<String> currentPermissions = new HashSet<>();
        for (SysRole role : currentRoles)
        {
            currentPermissions.addAll(role.getPermissions());
        }
        if (sameRoleAuthorization(user.getRoles(), currentRoles)
                && Objects.equals(loginUser.getPermissions(), currentPermissions))
        {
            return false;
        }

        user.setRoles(currentRoles);
        loginUser.setPermissions(currentPermissions);
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:04
     * 说明: 按角色比较鉴权字段，避免多个角色合并后的权限相同却保留旧的数据范围或角色权限。
     * @param cachedRoles 会话持有的角色列表，可为空
     * @param currentRoles 最新加载的角色列表，可为空
     * @return 角色关联、数据范围、状态、标识和各自权限是否一致
     */
    public static boolean sameRoleAuthorization(List<SysRole> cachedRoles, List<SysRole> currentRoles)
    {
        if (CollectionUtils.isEmpty(cachedRoles))
        {
            return CollectionUtils.isEmpty(currentRoles);
        }
        if (currentRoles == null || cachedRoles.size() != currentRoles.size())
        {
            return false;
        }
        Map<Long, SysRole> cachedById = new HashMap<>();
        cachedRoles.forEach(role -> cachedById.put(role.getRoleId(), role));
        return currentRoles.stream().allMatch(role -> {
            SysRole cached = cachedById.get(role.getRoleId());
            return cached != null
                    && Objects.equals(cached.getRoleKey(), role.getRoleKey())
                    && Objects.equals(cached.getStatus(), role.getStatus())
                    && Objects.equals(cached.getDataScope(), role.getDataScope())
                    && samePermissions(cached.getPermissions(), role.getPermissions());
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:04
     * 说明: 兼容缓存中未授权角色的空权限字段。
     * @param cached 缓存权限
     * @param current 已查询的权限
     * @return 两个权限集合是否一致
     */
    private static boolean samePermissions(Set<String> cached, Set<String> current)
    {
        return CollectionUtils.isEmpty(cached) ? CollectionUtils.isEmpty(current) : cached.equals(current);
    }
}
