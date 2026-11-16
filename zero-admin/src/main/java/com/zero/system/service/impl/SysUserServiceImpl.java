package com.zero.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.zero.common.annotation.DataScope;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.bean.BeanValidators;
import com.zero.common.utils.spring.SpringUtils;
import com.zero.system.domain.SysPost;
import com.zero.system.domain.SysUserPost;
import com.zero.system.domain.SysUserRole;
import com.zero.system.mapper.SysPostMapper;
import com.zero.system.mapper.SysRoleMapper;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.mapper.SysUserPostMapper;
import com.zero.system.mapper.SysUserRoleMapper;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.ISysDeptService;
import com.zero.system.service.ISysUserService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户 业务层处理
 */
@Service
public class SysUserServiceImpl implements ISysUserService
{
    /**
     * log字段
     */
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);
    /**
     * 用户数据访问字段
     */

    @Autowired
    private SysUserMapper userMapper;
    /**
     * 角色数据访问字段
     */

    @Autowired
    private SysRoleMapper roleMapper;
    /**
     * 岗位数据访问字段
     */

    @Autowired
    private SysPostMapper postMapper;
    /**
     * 用户角色数据访问字段
     */

    @Autowired
    private SysUserRoleMapper userRoleMapper;
    /**
     * 用户岗位数据访问字段
     */

    @Autowired
    private SysUserPostMapper userPostMapper;
    /**
     * 参数配置服务字段
     */

    @Autowired
    private ISysConfigService configService;
    /**
     * 部门服务字段
     */

    @Autowired
    private ISysDeptService deptService;
    /**
     * validator字段
     */

    @Autowired
    protected Validator validator;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 按删除标志查询用户，缺省仅查未删除；保留分页、数据范围及批量角色展示，恢复由独立入口校验
     * @param user 用户信息
     * @return
     * 返回值: 用户信息集合信息
     * 返回值说明：根据条件分页查询用户列表
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user)
    {
        if (user == null) throw new ServiceException("用户查询条件不能为空");
        if (StringUtils.isEmpty(user.getDelFlag())) user.setDelFlag(UserConstants.USER_NOT_DELETED);
        if (!UserConstants.USER_NOT_DELETED.equals(user.getDelFlag()) && !UserConstants.USER_DELETED.equals(user.getDelFlag()))
        {
            throw new ServiceException("删除状态无效");
        }
        List<SysUser> users = userMapper.selectUserList(user);
        if (CollectionUtils.isEmpty(users)) return users;
        Long[] userIds = users.stream().filter(Objects::nonNull).map(SysUser::getUserId)
            .filter(Objects::nonNull).distinct().toArray(Long[]::new);
        if (userIds.length == 0) return users;
        List<SysUser> userRoles = userMapper.selectUserRolesByUserIds(userIds);
        Map<Long, List<SysRole>> rolesByUser = new HashMap<>();
        if (userRoles != null)
        {
            for (SysUser item : userRoles)
            {
                if (item != null && item.getUserId() != null && item.getRoles() != null)
                {
                    rolesByUser.put(item.getUserId(), item.getRoles());
                }
            }
        }
        for (SysUser item : users)
        {
            if (item != null) item.setRoles(rolesByUser.getOrDefault(item.getUserId(), Collections.emptyList()));
        }
        // 保留PageHelper原集合，不能重新收集为普通List，否则分页total会丢失。
        return users;
    }

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
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

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
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

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
    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return userMapper.selectUserByUserName(userName);
    }

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
    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询用户所属角色组
     * @param userName 用户名
     * @return
     * 返回值: 结果
     * 返回值说明：查询用户所属角色组
     */
    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询用户所属岗位组
     * @param userName 用户名
     * @return
     * 返回值: 结果
     * 返回值说明：查询用户所属岗位组
     */
    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

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
    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验手机号码是否唯一
     * @param user 用户信息
     * @return
     * 返回值: boolean
     * 返回值说明：校验手机号码是否唯一
     */
    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验email是否唯一
     * @param user 用户信息
     * @return
     * 返回值: boolean
     * 返回值说明：校验email是否唯一
     */
    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

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
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

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
    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增保存用户信息
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增保存用户信息
     */
    @Override
    @Transactional
    public int insertUser(SysUser user)
    {
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改保存用户信息
     * @param user 用户信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改保存用户信息
     */
    @Override
    @Transactional
    public int updateUser(SysUser user)
    {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
        return userMapper.updateUser(user);
    }

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
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

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
    @Override
    public int updateUserStatus(SysUser user)
    {
        return userMapper.updateUser(user);
    }

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
    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateUser(user);
    }

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
    @Override
    public boolean updateUserAvatar(Long userId, String avatar)
    {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

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
    @Override
    public int resetPwd(SysUser user)
    {
        return userMapper.updateUser(user);
    }

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
    @Override
    public int resetUserPwd(Long userId, String password)
    {
        return userMapper.resetUserPwd(userId, password);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 修改用户登录锁定状态，用于登录失败锁定或解锁用户账号
     * @param userId 用户ID
     * @param loginLock 登录锁定状态，按用户表login_lock字段约定取值
     * @param updateBy 更新者用户名
     * @return
     * 返回值: int
     * 返回值说明: 受影响行数
     */
    @Override
    public int updateUserLoginLock(Long userId, Integer loginLock, String updateBy)
    {
        return userMapper.updateUserLoginLock(userId, loginLock, updateBy);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增用户角色信息
     * @param user 用户对象
     * @return
     * 返回值: void
     * 返回值说明：新增用户角色信息
     */
    public void insertUserRole(SysUser user)
    {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增用户岗位信息
     * @param user 用户对象
     * @return
     * 返回值: void
     * 返回值说明：新增用户岗位信息
     */
    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts))
        {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<SysUserPost>(posts.length);
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            userPostMapper.batchUserPost(list);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增用户角色信息
     * @param userId 用户ID
     * @param roleIds 角色组
     * @return
     * 返回值: void
     * 返回值说明：新增用户角色信息
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotEmpty(roleIds))
        {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>(roleIds.length);
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

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
    @Override
    @Transactional
    public int deleteUserById(Long userId)
    {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

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
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 校验已删除账号的数据范围后恢复，不补回授权或修改登录缓存
     * @param userId 已删除账号ID，不允许超级管理员
     * @param operator 当前登录操作人账号，不接收前端指定
     * @return
     * 返回值: int
     * 返回值说明: 成功返回1，越权或并发变化时抛出业务异常
     */
    @Override
    @Transactional
    public int restoreUser(Long userId, String operator)
    {
        if (userId == null || userId <= 0 || StringUtils.isBlank(operator))
        {
            throw new ServiceException("恢复账号参数无效");
        }
        SysUser user = userMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("账号不存在");
        }
        checkUserAllowed(user);
        if (!UserConstants.USER_DELETED.equals(user.getDelFlag()))
        {
            throw new ServiceException("账号未删除或已恢复，请刷新后重试");
        }
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            // 原校验默认仅查询未删除记录，恢复需显式查询已删除账号并经过AOP数据过滤。
            SysUser query = new SysUser();
            query.setUserId(userId);
            query.setDelFlag(UserConstants.USER_DELETED);
            if (StringUtils.isEmpty(SpringUtils.getAopProxy(this).selectUserList(query)))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
        user.setUpdateBy(operator);
        if (userMapper.restoreUser(user) != 1)
        {
            throw new ServiceException("账号状态或信息已变化，请刷新后重试");
        }
        return 1;
    }

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
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList)
        {
            try
            {
                // 验证是否存在这个用户
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u))
                {
                    BeanValidators.validateWithException(validator, user);
                    deptService.checkDeptDataScope(user.getDeptId());
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    userMapper.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    checkUserDataScope(u.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(u.getUserId());
                    user.setUpdateBy(operName);
                    userMapper.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getUserName() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
