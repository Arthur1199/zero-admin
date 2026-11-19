package com.zero.framework.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.UserStatus;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.MessageUtils;
import com.zero.common.utils.StringUtils;
import com.zero.system.service.ISysUserService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户验证处理
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    /**
     * log字段
     */
    /**
     * 用户服务字段
     */

    @Autowired
    private ISysUserService userService;
    /**
     * 密码服务字段
     */

    @Autowired
    private SysPasswordService passwordService;
    /**
     * 权限服务字段
     */

    @Autowired
    private SysPermissionService permissionService;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 加载用户ByUsername
     * @param username username参数
     * @return
     * 返回值: UserDetails
     * 返回值说明：加载用户ByUsername
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 创建登录用户
     * @param user 用户参数
     * @return
     * 返回值: UserDetails
     * 返回值说明：创建登录用户
     */

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
