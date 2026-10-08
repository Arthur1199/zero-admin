package com.zero.common.core.domain.entity;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色表 sys_role
 */
public class SysRole extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @Excel(name = "角色序号", cellType = ColumnType.NUMERIC)
    private Long roleId;

    /**
     * 角色名称
     */
    @Excel(name = "角色名称")
    private String roleName;

    /**
     * 角色权限
     */
    @Excel(name = "角色权限")
    private String roleKey;

    /**
     * 角色排序
     */
    @Excel(name = "角色排序")
    private Integer roleSort;

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）
     */
    @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    private boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     */
    private boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    private boolean flag = false;

    /**
     * 菜单组
     */
    private Long[] menuIds;

    /**
     * 部门组（数据权限）
     */
    private Long[] deptIds;

    /**
     * 角色菜单权限
     */
    private Set<String> permissions;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统角色
     */

    public SysRole()
    {

    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统角色
     * @param roleId 角色ID参数
     */

    public SysRole(Long roleId)
    {
        this.roleId = roleId;
    }
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
     * 说明: 判断Admin
     * @return
     * 返回值: boolean
     * 返回值说明：判断Admin
     */

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Admin
     * @param roleId 角色ID参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Admin
     */

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色名称
     * @return
     * 返回值: String
     * 返回值说明：获取角色名称
     */

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    public String getRoleName()
    {
        return roleName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置角色名称
     * @param roleName 角色名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置角色名称
     */

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色键名
     * @return
     * 返回值: String
     * 返回值说明：获取角色键名
     */

    @NotBlank(message = "角色英文名称不能为空")
    @Size(min = 0, max = 100, message = "角色英文名称长度不能超过100个字符")
    public String getRoleKey()
    {
        return roleKey;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置角色键名
     * @param roleKey 角色键名参数
     * @return
     * 返回值: void
     * 返回值说明：设置角色键名
     */

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色排序
     * @return
     * 返回值: Integer
     * 返回值说明：获取角色排序
     */

    @NotNull(message = "显示顺序不能为空")
    public Integer getRoleSort()
    {
        return roleSort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置角色排序
     * @param roleSort 角色排序参数
     * @return
     * 返回值: void
     * 返回值说明：设置角色排序
     */

    public void setRoleSort(Integer roleSort)
    {
        this.roleSort = roleSort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取数据Scope
     * @return
     * 返回值: String
     * 返回值说明：获取数据Scope
     */

    public String getDataScope()
    {
        return dataScope;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置数据Scope
     * @param dataScope 数据Scope参数
     * @return
     * 返回值: void
     * 返回值说明：设置数据Scope
     */

    public void setDataScope(String dataScope)
    {
        this.dataScope = dataScope;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断菜单校验Strictly
     * @return
     * 返回值: boolean
     * 返回值说明：判断菜单校验Strictly
     */

    public boolean isMenuCheckStrictly()
    {
        return menuCheckStrictly;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置菜单校验Strictly
     * @param menuCheckStrictly 菜单校验Strictly参数
     * @return
     * 返回值: void
     * 返回值说明：设置菜单校验Strictly
     */

    public void setMenuCheckStrictly(boolean menuCheckStrictly)
    {
        this.menuCheckStrictly = menuCheckStrictly;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断部门校验Strictly
     * @return
     * 返回值: boolean
     * 返回值说明：判断部门校验Strictly
     */

    public boolean isDeptCheckStrictly()
    {
        return deptCheckStrictly;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置部门校验Strictly
     * @param deptCheckStrictly 部门校验Strictly参数
     * @return
     * 返回值: void
     * 返回值说明：设置部门校验Strictly
     */

    public void setDeptCheckStrictly(boolean deptCheckStrictly)
    {
        this.deptCheckStrictly = deptCheckStrictly;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取状态
     * @return
     * 返回值: String
     * 返回值说明：获取状态
     */

    public String getStatus()
    {
        return status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置状态
     * @param status 状态参数
     * @return
     * 返回值: void
     * 返回值说明：设置状态
     */

    public void setStatus(String status)
    {
        this.status = status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取DelFlag
     * @return
     * 返回值: String
     * 返回值说明：获取DelFlag
     */

    public String getDelFlag()
    {
        return delFlag;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置DelFlag
     * @param delFlag delFlag参数
     * @return
     * 返回值: void
     * 返回值说明：设置DelFlag
     */

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Flag
     * @return
     * 返回值: boolean
     * 返回值说明：判断Flag
     */

    public boolean isFlag()
    {
        return flag;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Flag
     * @param flag flag参数
     * @return
     * 返回值: void
     * 返回值说明：设置Flag
     */

    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取菜单ID集合
     * @return
     * 返回值: Long[]
     * 返回值说明：获取菜单ID集合
     */

    public Long[] getMenuIds()
    {
        return menuIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置菜单ID集合
     * @param menuIds 菜单ID集合参数
     * @return
     * 返回值: void
     * 返回值说明：设置菜单ID集合
     */

    public void setMenuIds(Long[] menuIds)
    {
        this.menuIds = menuIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取部门ID集合
     * @return
     * 返回值: Long[]
     * 返回值说明：获取部门ID集合
     */

    public Long[] getDeptIds()
    {
        return deptIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置部门ID集合
     * @param deptIds 部门ID集合参数
     * @return
     * 返回值: void
     * 返回值说明：设置部门ID集合
     */

    public void setDeptIds(Long[] deptIds)
    {
        this.deptIds = deptIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Permissions
     * @return
     * 返回值: Set<String>
     * 返回值说明：获取Permissions
     */

    public Set<String> getPermissions()
    {
        return permissions;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Permissions
     * @param permissions permissions参数
     * @return
     * 返回值: void
     * 返回值说明：设置Permissions
     */

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
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
            .append("roleName", getRoleName())
            .append("roleKey", getRoleKey())
            .append("roleSort", getRoleSort())
            .append("dataScope", getDataScope())
            .append("menuCheckStrictly", isMenuCheckStrictly())
            .append("deptCheckStrictly", isDeptCheckStrictly())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
