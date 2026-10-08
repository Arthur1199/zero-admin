package com.zero.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.common.core.domain.entity.SysUser;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户表 数据层
 */
public interface SysUserMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询用户列表
     * @param sysUser 用户信息
     * @return
     * 返回值: 用户信息集合信息
     * 返回值说明：根据条件分页查询用户列表
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询已配用户角色列表
     * @param user 用户信息
     * @return
     * 返回值: 用户信息集合信息
     * 返回值说明：根据条件分页查询已配用户角色列表
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
     * 说明: 修改用户头像
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return
     * 返回值: 结果
     * 返回值说明：修改用户头像
     */
    public int updateUserAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

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
    public int resetUserPwd(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 修改用户登录锁定状态
     *
     * @param userId 用户ID
     * @param loginLock 登录锁定状态
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateUserLoginLock(@Param("userId") Long userId, @Param("loginLock") Integer loginLock, @Param("updateBy") String updateBy);

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
     * 说明: 校验用户名称是否唯一
     * @param userName 用户名称
     * @return
     * 返回值: 结果
     * 返回值说明：校验用户名称是否唯一
     */
    public SysUser checkUserNameUnique(String userName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验手机号码是否唯一
     * @param phonenumber 手机号码
     * @return
     * 返回值: 结果
     * 返回值说明：校验手机号码是否唯一
     */
    public SysUser checkPhoneUnique(String phonenumber);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验email是否唯一
     * @param email 用户邮箱
     * @return
     * 返回值: 结果
     * 返回值说明：校验email是否唯一
     */
    public SysUser checkEmailUnique(String email);
}
