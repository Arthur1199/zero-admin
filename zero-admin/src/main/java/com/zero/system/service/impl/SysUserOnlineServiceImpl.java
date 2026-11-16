package com.zero.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.zero.framework.web.service.TokenService;
import com.zero.common.constant.Constants;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysUserOnline;
import com.zero.system.service.ISysUserOnlineService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 在线用户 服务层处理
 */
@Service
public class SysUserOnlineServiceImpl implements ISysUserOnlineService
{
    /** 还原现有会话JWT，不修改登录缓存 */
    @Autowired
    private TokenService tokenService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过登录地址查询信息
     * @param ipaddr 登录地址
     * @param user 用户信息
     * @return
     * 返回值: 在线用户信息
     * 返回值说明：通过登录地址查询信息
     */
    @Override
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user)
    {
        if (user != null && StringUtils.equals(ipaddr, user.getIpaddr()))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过用户名称查询信息
     * @param userName 用户名称
     * @param user 用户信息
     * @return
     * 返回值: 在线用户信息
     * 返回值说明：通过用户名称查询信息
     */
    @Override
    public SysUserOnline selectOnlineByUserName(String userName, LoginUser user)
    {
        if (user != null && user.getUser() != null && StringUtils.equals(userName, user.getUsername()))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过登录地址/用户名称查询信息
     * @param ipaddr 登录地址
     * @param userName 用户名称
     * @param user 用户信息
     * @return
     * 返回值: 在线用户信息
     * 返回值说明：通过登录地址/用户名称查询信息
     */
    @Override
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user)
    {
        if (user != null && user.getUser() != null && StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername()))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置在线用户信息及完整JWT，调用入口须校验monitor:online:list权限；不刷新会话
     * @param user 用户信息
     * @return
     * 返回值: 在线用户
     * 返回值说明：设置在线用户信息
     */
    @Override
    public SysUserOnline loginUserToUserOnline(LoginUser user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUser()))
        {
            return null;
        }
        SysUserOnline sysUserOnline = new SysUserOnline();
        sysUserOnline.setTokenId(user.getToken());
        String token = tokenService.getExistingToken(user);
        sysUserOnline.setAuthorizationToken(StringUtils.isEmpty(token) ? "" : Constants.TOKEN_PREFIX + token);
        sysUserOnline.setUserName(user.getUsername());
        sysUserOnline.setIpaddr(user.getIpaddr());
        sysUserOnline.setLoginLocation(user.getLoginLocation());
        sysUserOnline.setBrowser(user.getBrowser());
        sysUserOnline.setOs(user.getOs());
        sysUserOnline.setLoginTime(user.getLoginTime());
        if (StringUtils.isNotNull(user.getUser().getDept()))
        {
            sysUserOnline.setDeptName(user.getUser().getDept().getDeptName());
        }
        return sysUserOnline;
    }
}
