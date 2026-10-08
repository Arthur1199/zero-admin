package com.zero.common.core.domain.model;

import com.alibaba.fastjson2.annotation.JSONField;
import com.zero.common.core.domain.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 登录用户身份权限
 */
public class LoginUser implements UserDetails
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 用户信息
     */
    private SysUser user;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 登录用户
     */

    public LoginUser()
    {
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 登录用户
     * @param user 用户参数
     * @param permissions permissions参数
     */

    public LoginUser(SysUser user, Set<String> permissions)
    {
        this.user = user;
        this.permissions = permissions;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 登录用户
     * @param userId 用户ID参数
     * @param deptId 部门ID参数
     * @param user 用户参数
     * @param permissions permissions参数
     */

    public LoginUser(Long userId, Long deptId, SysUser user, Set<String> permissions)
    {
        this.userId = userId;
        this.deptId = deptId;
        this.user = user;
        this.permissions = permissions;
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
     * 说明: 获取令牌
     * @return
     * 返回值: String
     * 返回值说明：获取令牌
     */

    public String getToken()
    {
        return token;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置令牌
     * @param token 令牌参数
     * @return
     * 返回值: void
     * 返回值说明：设置令牌
     */

    public void setToken(String token)
    {
        this.token = token;
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

    @JSONField(serialize = false)
    @Override
    public String getPassword()
    {
        return user.getPassword();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Username
     * @return
     * 返回值: String
     * 返回值说明：获取Username
     */

    @Override
    public String getUsername()
    {
        return user.getUserName();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 账户是否未过期,过期无法验证
     * @return
     * 返回值: boolean
     * 返回值说明：账户是否未过期,过期无法验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 指定用户是否解锁,锁定的用户无法进行身份验证
     * @return
     * 返回值: boolean
     * 返回值说明：指定用户是否解锁,锁定的用户无法进行身份验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     * @return
     * 返回值: boolean
     * 返回值说明：指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否可用 ,禁用的用户不能身份验证
     * @return
     * 返回值: boolean
     * 返回值说明：是否可用 ,禁用的用户不能身份验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isEnabled()
    {
        return true;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录时间
     * @return
     * 返回值: Long
     * 返回值说明：获取登录时间
     */

    public Long getLoginTime()
    {
        return loginTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置登录时间
     * @param loginTime 登录时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置登录时间
     */

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IP地址
     * @return
     * 返回值: String
     * 返回值说明：获取IP地址
     */

    public String getIpaddr()
    {
        return ipaddr;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IP地址
     * @param ipaddr IP地址参数
     * @return
     * 返回值: void
     * 返回值说明：设置IP地址
     */

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录Location
     * @return
     * 返回值: String
     * 返回值说明：获取登录Location
     */

    public String getLoginLocation()
    {
        return loginLocation;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置登录Location
     * @param loginLocation 登录Location参数
     * @return
     * 返回值: void
     * 返回值说明：设置登录Location
     */

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Browser
     * @return
     * 返回值: String
     * 返回值说明：获取Browser
     */

    public String getBrowser()
    {
        return browser;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Browser
     * @param browser browser参数
     * @return
     * 返回值: void
     * 返回值说明：设置Browser
     */

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Os
     * @return
     * 返回值: String
     * 返回值说明：获取Os
     */

    public String getOs()
    {
        return os;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Os
     * @param os os参数
     * @return
     * 返回值: void
     * 返回值说明：设置Os
     */

    public void setOs(String os)
    {
        this.os = os;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Expire时间
     * @return
     * 返回值: Long
     * 返回值说明：获取Expire时间
     */

    public Long getExpireTime()
    {
        return expireTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Expire时间
     * @param expireTime expire时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置Expire时间
     */

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
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
     * 说明: 获取用户
     * @return
     * 返回值: SysUser
     * 返回值说明：获取用户
     */

    public SysUser getUser()
    {
        return user;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户
     * @param user 用户参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户
     */

    public void setUser(SysUser user)
    {
        this.user = user;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Authorities
     * @return
     * 返回值: Collection< extends GrantedAuthority>
     * 返回值说明：获取Authorities
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return null;
    }
}
