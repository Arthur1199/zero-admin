package com.zero.common.core.domain.entity;

import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.annotation.Excel.Type;
import com.zero.common.annotation.Excels;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.xss.Xss;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户对象 sys_user
 */
public class SysUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;

    /**
     * 部门ID
     */
    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    /**
     * 用户账号
     */
    @Excel(name = "登录名称")
    private String userName;

    /**
     * 用户昵称
     */
    @Excel(name = "用户名称")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Excel(name = "用户邮箱")
    private String email;

    /**
     * 手机号码
     */
    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    private String phonenumber;

    /**
     * 用户性别
     */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    @Excel(name = "账号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    /**
     * 密码最后更新时间
     */
    private Date pwdUpdateDate;

    /**
     * 登录是否被锁定，0否 1是
     */
    private Integer loginLock;

    /** 部门对象 */
    @Excels({
        @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
        @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    /**
     * 部门字段
     */
    private SysDept dept;

    /**
     * 角色对象
     */
    private List<SysRole> roles;

    /**
     * 角色组
     */
    private Long[] roleIds;

    /**
     * 岗位组
     */
    private Long[] postIds;

    /**
     * 角色ID
     */
    private Long roleId;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统用户
     */

    public SysUser()
    {

    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统用户
     * @param userId 用户ID参数
     */

    public SysUser(Long userId)
    {
        this.userId = userId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户ID
     * @return
     * 返回值: Long
     * 返回值说明：获取用户ID
     */

    public Long getUserId()
    {
        return userId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户ID
     * @param userId 用户ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户ID
     */

    public void setUserId(Long userId)
    {
        this.userId = userId;
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
        return isAdmin(this.userId);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Admin
     * @param userId 用户ID参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Admin
     */

    public static boolean isAdmin(Long userId)
    {
        return userId != null && 1L == userId;
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
     * 说明: 获取Nick名称
     * @return
     * 返回值: String
     * 返回值说明：获取Nick名称
     */

    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    public String getNickName()
    {
        return nickName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Nick名称
     * @param nickName nick名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Nick名称
     */

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户名称
     * @return
     * 返回值: String
     * 返回值说明：获取用户名称
     */

    @Xss(message = "用户账号不能包含脚本字符")
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    public String getUserName()
    {
        return userName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户名称
     * @param userName 用户名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户名称
     */

    public void setUserName(String userName)
    {
        this.userName = userName;
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
     * 说明: 获取手机号
     * @return
     * 返回值: String
     * 返回值说明：获取手机号
     */

    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    public String getPhonenumber()
    {
        return phonenumber;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置手机号
     * @param phonenumber 手机号参数
     * @return
     * 返回值: void
     * 返回值说明：设置手机号
     */

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Sex
     * @return
     * 返回值: String
     * 返回值说明：获取Sex
     */

    public String getSex()
    {
        return sex;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Sex
     * @param sex sex参数
     * @return
     * 返回值: void
     * 返回值说明：设置Sex
     */

    public void setSex(String sex)
    {
        this.sex = sex;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取头像
     * @return
     * 返回值: String
     * 返回值说明：获取头像
     */

    public String getAvatar()
    {
        return avatar;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置头像
     * @param avatar 头像参数
     * @return
     * 返回值: void
     * 返回值说明：设置头像
     */

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取密码
     * @return
     * 返回值: String
     * 返回值说明：获取密码
     */

    public String getPassword()
    {
        return password;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置密码
     * @param password 密码参数
     * @return
     * 返回值: void
     * 返回值说明：设置密码
     */

    public void setPassword(String password)
    {
        this.password = password;
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
     * 说明: 获取登录IP
     * @return
     * 返回值: String
     * 返回值说明：获取登录IP
     */

    public String getLoginIp()
    {
        return loginIp;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置登录IP
     * @param loginIp 登录IP参数
     * @return
     * 返回值: void
     * 返回值说明：设置登录IP
     */

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录日期
     * @return
     * 返回值: Date
     * 返回值说明：获取登录日期
     */

    public Date getLoginDate()
    {
        return loginDate;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置登录日期
     * @param loginDate 登录日期参数
     * @return
     * 返回值: void
     * 返回值说明：设置登录日期
     */

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取密码修改日期
     * @return
     * 返回值: Date
     * 返回值说明：获取密码修改日期
     */

    public Date getPwdUpdateDate()
    {
        return pwdUpdateDate;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置密码修改日期
     * @param pwdUpdateDate 密码修改日期参数
     * @return
     * 返回值: void
     * 返回值说明：设置密码修改日期
     */

    public void setPwdUpdateDate(Date pwdUpdateDate)
    {
        this.pwdUpdateDate = pwdUpdateDate;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取用户登录锁定状态
     * @return
     * 返回值: Integer
     * 返回值说明：登录锁定状态，0-未锁定，1-已锁定，null按未锁定兼容处理
     */
    public Integer getLoginLock()
    {
        return loginLock;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置用户登录锁定状态
     * @param loginLock 登录锁定状态，0-未锁定，1-已锁定
     * @return
     * 返回值: void
     * 返回值说明：设置用户登录锁定状态
     */
    public void setLoginLock(Integer loginLock)
    {
        this.loginLock = loginLock;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取部门
     * @return
     * 返回值: SysDept
     * 返回值说明：获取部门
     */

    public SysDept getDept()
    {
        return dept;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置部门
     * @param dept 部门参数
     * @return
     * 返回值: void
     * 返回值说明：设置部门
     */

    public void setDept(SysDept dept)
    {
        this.dept = dept;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Roles
     * @return
     * 返回值: List<SysRole>
     * 返回值说明：获取Roles
     */

    public List<SysRole> getRoles()
    {
        return roles;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Roles
     * @param roles roles参数
     * @return
     * 返回值: void
     * 返回值说明：设置Roles
     */

    public void setRoles(List<SysRole> roles)
    {
        this.roles = roles;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取角色ID集合
     * @return
     * 返回值: Long[]
     * 返回值说明：获取角色ID集合
     */

    public Long[] getRoleIds()
    {
        return roleIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置角色ID集合
     * @param roleIds 角色ID集合参数
     * @return
     * 返回值: void
     * 返回值说明：设置角色ID集合
     */

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取岗位ID集合
     * @return
     * 返回值: Long[]
     * 返回值说明：获取岗位ID集合
     */

    public Long[] getPostIds()
    {
        return postIds;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置岗位ID集合
     * @param postIds 岗位ID集合参数
     * @return
     * 返回值: void
     * 返回值说明：设置岗位ID集合
     */

    public void setPostIds(Long[] postIds)
    {
        this.postIds = postIds;
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
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明：toString
     */

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("deptId", getDeptId())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("email", getEmail())
            .append("phonenumber", getPhonenumber())
            .append("sex", getSex())
            .append("avatar", getAvatar())
            .append("password", getPassword())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("loginIp", getLoginIp())
            .append("loginDate", getLoginDate())
            .append("pwdUpdateDate", getPwdUpdateDate())
            .append("loginLock", getLoginLock())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("dept", getDept())
            .toString();
    }
}
