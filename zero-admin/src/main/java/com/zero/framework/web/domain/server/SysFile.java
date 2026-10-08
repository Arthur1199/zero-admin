package com.zero.framework.web.domain.server;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 系统文件相关信息
 */
public class SysFile
{
    /**
     * 盘符路径
     */
    private String dirName;

    /**
     * 盘符类型
     */
    private String sysTypeName;

    /**
     * 文件类型
     */
    private String typeName;

    /**
     * 总大小
     */
    private String total;

    /**
     * 剩余大小
     */
    private String free;

    /**
     * 已经使用量
     */
    private String used;

    /**
     * 资源的使用率
     */
    private double usage;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Dir名称
     * @return
     * 返回值: String
     * 返回值说明：获取Dir名称
     */

    public String getDirName()
    {
        return dirName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Dir名称
     * @param dirName dir名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Dir名称
     */

    public void setDirName(String dirName)
    {
        this.dirName = dirName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取系统类型名称
     * @return
     * 返回值: String
     * 返回值说明：获取系统类型名称
     */

    public String getSysTypeName()
    {
        return sysTypeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置系统类型名称
     * @param sysTypeName 系统类型名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置系统类型名称
     */

    public void setSysTypeName(String sysTypeName)
    {
        this.sysTypeName = sysTypeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取类型名称
     * @return
     * 返回值: String
     * 返回值说明：获取类型名称
     */

    public String getTypeName()
    {
        return typeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置类型名称
     * @param typeName 类型名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置类型名称
     */

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Total
     * @return
     * 返回值: String
     * 返回值说明：获取Total
     */

    public String getTotal()
    {
        return total;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Total
     * @param total total参数
     * @return
     * 返回值: void
     * 返回值说明：设置Total
     */

    public void setTotal(String total)
    {
        this.total = total;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Free
     * @return
     * 返回值: String
     * 返回值说明：获取Free
     */

    public String getFree()
    {
        return free;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Free
     * @param free free参数
     * @return
     * 返回值: void
     * 返回值说明：设置Free
     */

    public void setFree(String free)
    {
        this.free = free;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Used
     * @return
     * 返回值: String
     * 返回值说明：获取Used
     */

    public String getUsed()
    {
        return used;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Used
     * @param used used参数
     * @return
     * 返回值: void
     * 返回值说明：设置Used
     */

    public void setUsed(String used)
    {
        this.used = used;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Usage
     * @return
     * 返回值: double
     * 返回值说明：获取Usage
     */

    public double getUsage()
    {
        return usage;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Usage
     * @param usage usage参数
     * @return
     * 返回值: void
     * 返回值说明：设置Usage
     */

    public void setUsage(double usage)
    {
        this.usage = usage;
    }
}
