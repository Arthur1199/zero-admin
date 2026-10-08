package com.zero.framework.web.domain.server;

import com.zero.common.utils.Arith;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: CPU相关信息
 */
public class Cpu
{
    /**
     * 核心数
     */
    private int cpuNum;

    /**
     * CPU总的使用率
     */
    private double total;

    /**
     * CPU系统使用率
     */
    private double sys;

    /**
     * CPU用户使用率
     */
    private double used;

    /**
     * CPU当前等待率
     */
    private double wait;

    /**
     * CPU当前空闲率
     */
    private double free;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取CpuNum
     * @return
     * 返回值: int
     * 返回值说明：获取CpuNum
     */

    public int getCpuNum()
    {
        return cpuNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置CpuNum
     * @param cpuNum cpuNum参数
     * @return
     * 返回值: void
     * 返回值说明：设置CpuNum
     */

    public void setCpuNum(int cpuNum)
    {
        this.cpuNum = cpuNum;
    }
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
        return Arith.round(Arith.mul(total, 100), 2);
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
     * 说明: 获取系统
     * @return
     * 返回值: double
     * 返回值说明：获取系统
     */

    public double getSys()
    {
        return Arith.round(Arith.mul(sys / total, 100), 2);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置系统
     * @param sys 系统参数
     * @return
     * 返回值: void
     * 返回值说明：设置系统
     */

    public void setSys(double sys)
    {
        this.sys = sys;
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
        return Arith.round(Arith.mul(used / total, 100), 2);
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

    public void setUsed(double used)
    {
        this.used = used;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Wait
     * @return
     * 返回值: double
     * 返回值说明：获取Wait
     */

    public double getWait()
    {
        return Arith.round(Arith.mul(wait / total, 100), 2);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Wait
     * @param wait wait参数
     * @return
     * 返回值: void
     * 返回值说明：设置Wait
     */

    public void setWait(double wait)
    {
        this.wait = wait;
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
        return Arith.round(Arith.mul(free / total, 100), 2);
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
}
