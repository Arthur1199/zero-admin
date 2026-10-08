package com.zero.system.domain;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 当前在线会话
 */
public class SysUserOnline
{
    /**
     * 会话编号
     */
    private String tokenId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地址
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
     * 登录时间
     */
    private Long loginTime;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取令牌ID
     * @return
     * 返回值: String
     * 返回值说明：获取令牌ID
     */

    public String getTokenId()
    {
        return tokenId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置令牌ID
     * @param tokenId 令牌ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置令牌ID
     */

    public void setTokenId(String tokenId)
    {
        this.tokenId = tokenId;
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
     * 说明: 获取用户名称
     * @return
     * 返回值: String
     * 返回值说明：获取用户名称
     */

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
}
