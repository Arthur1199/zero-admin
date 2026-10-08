package com.zero.framework.web.domain.server;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 系统相关信息
 */
public class Sys
{
    /**
     * 服务器名称
     */
    private String computerName;

    /**
     * 服务器Ip
     */
    private String computerIp;

    /**
     * 项目路径
     */
    private String userDir;

    /**
     * 操作系统
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Computer名称
     * @return
     * 返回值: String
     * 返回值说明：获取Computer名称
     */

    public String getComputerName()
    {
        return computerName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Computer名称
     * @param computerName computer名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Computer名称
     */

    public void setComputerName(String computerName)
    {
        this.computerName = computerName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取ComputerIP
     * @return
     * 返回值: String
     * 返回值说明：获取ComputerIP
     */

    public String getComputerIp()
    {
        return computerIp;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置ComputerIP
     * @param computerIp computerIP参数
     * @return
     * 返回值: void
     * 返回值说明：设置ComputerIP
     */

    public void setComputerIp(String computerIp)
    {
        this.computerIp = computerIp;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户Dir
     * @return
     * 返回值: String
     * 返回值说明：获取用户Dir
     */

    public String getUserDir()
    {
        return userDir;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户Dir
     * @param userDir 用户Dir参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户Dir
     */

    public void setUserDir(String userDir)
    {
        this.userDir = userDir;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Os名称
     * @return
     * 返回值: String
     * 返回值说明：获取Os名称
     */

    public String getOsName()
    {
        return osName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Os名称
     * @param osName os名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Os名称
     */

    public void setOsName(String osName)
    {
        this.osName = osName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取OsArch
     * @return
     * 返回值: String
     * 返回值说明：获取OsArch
     */

    public String getOsArch()
    {
        return osArch;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置OsArch
     * @param osArch osArch参数
     * @return
     * 返回值: void
     * 返回值说明：设置OsArch
     */

    public void setOsArch(String osArch)
    {
        this.osArch = osArch;
    }
}
