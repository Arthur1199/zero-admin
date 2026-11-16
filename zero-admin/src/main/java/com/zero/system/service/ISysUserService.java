package com.zero.system.service;

import java.util.List;
import com.zero.common.core.domain.entity.SysUser;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户 业务层
 */
public interface ISysUserService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件及数据范围分页查询用户，并批量补充当前结果的未删除角色展示信息
     * @param user 用户信息
     * @return
     * 返回值: 用户信息集合信息
     * 返回值说明：根据条件分页查询用户列表
     */
    public List<SysUser> selectUserList(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询已分配用户角色列表
     * @param user 用户信息
     * @return
     * 返回值: 用户信息集合信息
     * 返回值说明：根据条件分页查询已分配用户角色列表
     */
    public List<SysUser> selectAllocatedList(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询未分配用户角色列表
     * @param user 用户信息
     * @return
     * 返回值: 用户信息集合信息
     * 返回值说明：根据条件分页查询未分配用户角色列表
     */
    public List<SysUser> selectUnallocatedList(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过用户名查询用户
     * @param userName 用户名
     * @return
     * 返回值: 用户对象信息
     * 返回值说明：通过用户名查询用户
     */
    public SysUser selectUserByUserName(String userName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过用户ID查询用户
     * @param userId 用户ID
     * @return
     * 返回值: 用户对象信息
     * 返回值说明：通过用户ID查询用户
     */
    public SysUser selectUserById(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询用户所属角色组
     * @param userName 用户名
     * @return
     * 返回值: 结果
     * 返回值说明：根据用户ID查询用户所属角色组
     */
    public String selectUserRoleGroup(String userName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询用户所属岗位组
     * @param userName 用户名
     * @return
     * 返回值: 结果
     * 返回值说明：根据用户ID查询用户所属岗位组
     */
    public String selectUserPostGroup(String userName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验用户名称是否唯一
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验用户名称是否唯一
     */
    public boolean checkUserNameUnique(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验手机号码是否唯一
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验手机号码是否唯一
     */
    public boolean checkPhoneUnique(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验email是否唯一
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验email是否唯一
     */
    public boolean checkEmailUnique(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验用户是否允许操作
     * @param user 用户信息
     * @return
     * 返回值: void
     * 返回值说明：校验用户是否允许操作
     */
    public void checkUserAllowed(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验用户是否有数据权限
     * @param userId 用户id
     * @return
     * 返回值: void
     * 返回值说明：校验用户是否有数据权限
     */
    public void checkUserDataScope(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增用户信息
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增用户信息
     */
    public int insertUser(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改用户信息
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改用户信息
     */
    public int updateUser(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 用户授权角色
     * @param userId 用户ID
     * @param roleIds 角色组
     * @return
     * 返回值: void
     * 返回值说明：用户授权角色
     */
    public void insertUserAuth(Long userId, Long[] roleIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改用户状态
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改用户状态
     */
    public int updateUserStatus(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改用户基本信息
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改用户基本信息
     */
    public int updateUserProfile(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改用户头像
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return
     * 返回值: 结果
     * 返回值说明：修改用户头像
     */
    public boolean updateUserAvatar(Long userId, String avatar);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 重置用户密码
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：重置用户密码
     */
    public int resetPwd(SysUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 重置用户密码
     * @param userId 用户ID
     * @param password 密码
     * @return
     * 返回值: 结果
     * 返回值说明：重置用户密码
     */
    public int resetUserPwd(Long userId, String password);

    /**
     * 修改用户登录锁定状态
     *
     * @param userId 用户ID
     * @param loginLock 登录锁定状态
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateUserLoginLock(Long userId, Integer loginLock, String updateBy);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过用户ID删除用户
     * @param userId 用户ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过用户ID删除用户
     */
    public int deleteUserById(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除用户信息
     * @param userIds 需要删除的用户ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除用户信息
     */
    public int deleteUserByIds(Long[] userIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 导入用户数据
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return
     * 返回值: 结果
     * 返回值说明：导入用户数据
     */
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName);
}
