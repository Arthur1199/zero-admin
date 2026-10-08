package com.zero.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色和菜单关联 sys_role_menu
 */
public class SysRoleMenu
{
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色ID
     * @return
     * 返回值: Long
     * 返回值说明：获取角色ID
     */

    public Long getRoleId()
    {
        return roleId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置角色ID
     * @param roleId 角色ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置角色ID
     */

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取菜单ID
     * @return
     * 返回值: Long
     * 返回值说明：获取菜单ID
     */

    public Long getMenuId()
    {
        return menuId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置菜单ID
     * @param menuId 菜单ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置菜单ID
     */

    public void setMenuId(Long menuId)
    {
        this.menuId = menuId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明：toString
     */

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("menuId", getMenuId())
            .toString();
    }
}
