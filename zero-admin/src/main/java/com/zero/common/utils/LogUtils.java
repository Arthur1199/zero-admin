package com.zero.common.utils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 处理并记录日志文件
 */
public class LogUtils
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Block
     * @param msg msg参数
     * @return
     * 返回值: String
     * 返回值说明：获取Block
     */
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
