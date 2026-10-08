package com.zero.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 系统访问记录表 sys_logininfor
 */
public class SysLogininfor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Excel(name = "序号", cellType = ColumnType.NUMERIC)
    private Long infoId;

    /**
     * 用户账号
     */
    @Excel(name = "用户账号")
    private String userName;

    /**
     * 登录状态 0成功 1失败
     */
    @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /**
     * 登录IP地址
     */
    @Excel(name = "登录地址")
    private String ipaddr;

    /**
     * 登录地点
     */
    @Excel(name = "登录地点")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @Excel(name = "浏览器")
    private String browser;

    /**
     * 操作系统
     */
    @Excel(name = "操作系统")
    private String os;

    /**
     * 提示消息
     */
    @Excel(name = "提示消息")
    private String msg;

    /**
     * 访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取信息ID
     * @return
     * 返回值: Long
     * 返回值说明：获取信息ID
     */

    public Long getInfoId()
    {
        return infoId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置信息ID
     * @param infoId 信息ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置信息ID
     */

    public void setInfoId(Long infoId)
    {
        this.infoId = infoId;
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
     * 说明: 获取Msg
     * @return
     * 返回值: String
     * 返回值说明：获取Msg
     */

    public String getMsg()
    {
        return msg;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Msg
     * @param msg msg参数
     * @return
     * 返回值: void
     * 返回值说明：设置Msg
     */

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录时间
     * @return
     * 返回值: Date
     * 返回值说明：获取登录时间
     */

    public Date getLoginTime()
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

    public void setLoginTime(Date loginTime)
    {
        this.loginTime = loginTime;
    }
}
