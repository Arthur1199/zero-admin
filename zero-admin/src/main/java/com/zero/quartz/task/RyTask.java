package com.zero.quartz.task;

import org.springframework.stereotype.Component;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 定时任务调度测试
 */
@Component("ryTask")
public class RyTask
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: ryMultiple参数集合
     * @param s s参数
     * @param b b参数
     * @param l l参数
     * @param d d参数
     * @param i i参数
     * @return
     * 返回值: void
     * 返回值说明：ryMultiple参数集合
     */
    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: ry参数集合
     * @param params 参数集合参数
     * @return
     * 返回值: void
     * 返回值说明：ry参数集合
     */

    public void ryParams(String params)
    {
        System.out.println("执行有参方法：" + params);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: ryNo参数集合
     * @return
     * 返回值: void
     * 返回值说明：ryNo参数集合
     */

    public void ryNoParams()
    {
        System.out.println("执行无参方法");
    }
}
