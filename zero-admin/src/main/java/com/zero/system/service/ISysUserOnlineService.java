package com.zero.system.service;

import com.zero.common.core.domain.model.LoginUser;
import com.zero.system.domain.SysUserOnline;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 在线用户 服务层
 */
public interface ISysUserOnlineService
{
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
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user);

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
    public SysUserOnline selectOnlineByUserName(String userName, LoginUser user);

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
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置在线用户信息及含Bearer前缀的完整授权令牌，调用入口须校验monitor:online:list权限
     * @param user 用户信息
     * @return
     * 返回值: 在线用户
     * 返回值说明：设置在线用户信息
     */
    public SysUserOnline loginUserToUserOnline(LoginUser user);
}
