package com.zero.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色和部门关联 sys_role_dept
 */
public class SysRoleDept
{
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;
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
     * 说明: 获取部门ID
     * @return
     * 返回值: Long
     * 返回值说明：获取部门ID
     */

    public Long getDeptId()
    {
        return deptId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置部门ID
     * @param deptId 部门ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置部门ID
     */

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
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
            .append("deptId", getDeptId())
            .toString();
    }
}
