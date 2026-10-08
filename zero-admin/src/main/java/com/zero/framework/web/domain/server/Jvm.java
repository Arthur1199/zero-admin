package com.zero.framework.web.domain.server;

import java.lang.management.ManagementFactory;
import com.zero.common.utils.Arith;
import com.zero.common.utils.DateUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: JVM相关信息
 */
public class Jvm
{
    /**
     * 当前JVM占用的内存总数(M)
     */
    private double total;

    /**
     * JVM最大可用内存总数(M)
     */
    private double max;

    /**
     * JVM空闲内存(M)
     */
    private double free;

    /**
     * JDK版本
     */
    private String version;

    /**
     * JDK路径
     */
    private String home;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Total
     * @return
     * 返回值: double
     * 返回值说明：获取Total
     */

    public double getTotal()
    {
        return Arith.div(total, (1024 * 1024), 2);
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

    public void setTotal(double total)
    {
        this.total = total;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Max
     * @return
     * 返回值: double
     * 返回值说明：获取Max
     */

    public double getMax()
    {
        return Arith.div(max, (1024 * 1024), 2);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Max
     * @param max max参数
     * @return
     * 返回值: void
     * 返回值说明：设置Max
     */

    public void setMax(double max)
    {
        this.max = max;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Free
     * @return
     * 返回值: double
     * 返回值说明：获取Free
     */

    public double getFree()
    {
        return Arith.div(free, (1024 * 1024), 2);
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

    public void setFree(double free)
    {
        this.free = free;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Used
     * @return
     * 返回值: double
     * 返回值说明：获取Used
     */

    public double getUsed()
    {
        return Arith.div(total - free, (1024 * 1024), 2);
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
        return Arith.mul(Arith.div(total - free, total, 4), 100);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取JDK名称
     * @return
     * 返回值: String
     * 返回值说明：获取JDK名称
     */
    public String getName()
    {
        return ManagementFactory.getRuntimeMXBean().getVmName();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Version
     * @return
     * 返回值: String
     * 返回值说明：获取Version
     */

    public String getVersion()
    {
        return version;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Version
     * @param version version参数
     * @return
     * 返回值: void
     * 返回值说明：设置Version
     */

    public void setVersion(String version)
    {
        this.version = version;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Home
     * @return
     * 返回值: String
     * 返回值说明：获取Home
     */

    public String getHome()
    {
        return home;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Home
     * @param home home参数
     * @return
     * 返回值: void
     * 返回值说明：设置Home
     */

    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: JDK启动时间
     * @return
     * 返回值: String
     * 返回值说明：JDK启动时间
     */
    public String getStartTime()
    {
        return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getServerStartDate());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: JDK运行时间
     * @return
     * 返回值: String
     * 返回值说明：JDK运行时间
     */
    public String getRunTime()
    {
        return DateUtils.timeDistance(DateUtils.getNowDate(), DateUtils.getServerStartDate());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 运行参数
     * @return
     * 返回值: String
     * 返回值说明：运行参数
     */
    public String getInputArgs()
    {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
    }
}
