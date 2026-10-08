package com.zero.common.core.domain.entity;

import com.zero.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 部门表 sys_dept
 */
public class SysDept extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门状态:0正常,1停用
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 父部门名称
     */
    private String parentName;

    /** 子部门 */
    private List<SysDept> children = new ArrayList<SysDept>();
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
     * 说明: 获取父节点ID
     * @return
     * 返回值: Long
     * 返回值说明：获取父节点ID
     */

    public Long getParentId()
    {
        return parentId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置父节点ID
     * @param parentId 父节点ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置父节点ID
     */

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Ancestors
     * @return
     * 返回值: String
     * 返回值说明：获取Ancestors
     */

    public String getAncestors()
    {
        return ancestors;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Ancestors
     * @param ancestors ancestors参数
     * @return
     * 返回值: void
     * 返回值说明：设置Ancestors
     */

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取部门名称
     * @return
     * 返回值: String
     * 返回值说明：获取部门名称
     */

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 0, max = 30, message = "部门名称长度不能超过30个字符")
    public String getDeptName()
    {
        return deptName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置部门名称
     * @param deptName 部门名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置部门名称
     */

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取OrderNum
     * @return
     * 返回值: Integer
     * 返回值说明：获取OrderNum
     */

    @NotNull(message = "显示顺序不能为空")
    public Integer getOrderNum()
    {
        return orderNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置OrderNum
     * @param orderNum orderNum参数
     * @return
     * 返回值: void
     * 返回值说明：设置OrderNum
     */

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Leader
     * @return
     * 返回值: String
     * 返回值说明：获取Leader
     */

    public String getLeader()
    {
        return leader;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Leader
     * @param leader leader参数
     * @return
     * 返回值: void
     * 返回值说明：设置Leader
     */

    public void setLeader(String leader)
    {
        this.leader = leader;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取手机号
     * @return
     * 返回值: String
     * 返回值说明：获取手机号
     */

    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    public String getPhone()
    {
        return phone;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置手机号
     * @param phone 手机号参数
     * @return
     * 返回值: void
     * 返回值说明：设置手机号
     */

    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取邮箱
     * @return
     * 返回值: String
     * 返回值说明：获取邮箱
     */

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    public String getEmail()
    {
        return email;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置邮箱
     * @param email 邮箱参数
     * @return
     * 返回值: void
     * 返回值说明：设置邮箱
     */

    public void setEmail(String email)
    {
        this.email = email;
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
     * 说明: 获取父节点名称
     * @return
     * 返回值: String
     * 返回值说明：获取父节点名称
     */

    public String getParentName()
    {
        return parentName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置父节点名称
     * @param parentName 父节点名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置父节点名称
     */

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取子节点
     * @return
     * 返回值: List<SysDept>
     * 返回值说明：获取子节点
     */

    public List<SysDept> getChildren()
    {
        return children;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置子节点
     * @param children 子节点参数
     * @return
     * 返回值: void
     * 返回值说明：设置子节点
     */

    public void setChildren(List<SysDept> children)
    {
        this.children = children;
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
            .append("deptId", getDeptId())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("deptName", getDeptName())
            .append("orderNum", getOrderNum())
            .append("leader", getLeader())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
