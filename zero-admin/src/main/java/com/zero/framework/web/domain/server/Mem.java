package com.zero.framework.web.domain.server;

import com.zero.common.utils.Arith;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 內存相关信息
 */
public class Mem
{
    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;
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
        return Arith.div(total, (1024 * 1024 * 1024), 2);
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

    public void setTotal(long total)
    {
        this.total = total;
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
        return Arith.div(used, (1024 * 1024 * 1024), 2);
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

    public void setUsed(long used)
    {
        this.used = used;
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
        return Arith.div(free, (1024 * 1024 * 1024), 2);
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

    public void setFree(long free)
    {
        this.free = free;
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
        return Arith.mul(Arith.div(used, total, 4), 100);
    }
}
