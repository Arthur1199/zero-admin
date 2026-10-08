package com.zero.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 错误信息处理类。
 */
public class ExceptionUtil
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取exception的详细错误信息。
     * @param e e参数
     * @return
     * 返回值: String
     * 返回值说明：获取exception的详细错误信息。
     */
    public static String getExceptionMessage(Throwable e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取RootError消息
     * @param e e参数
     * @return
     * 返回值: String
     * 返回值说明：获取RootError消息
     */

    public static String getRootErrorMessage(Exception e)
    {
        Throwable root = ExceptionUtils.getRootCause(e);
        root = (root == null ? e : root);
        if (root == null)
        {
            return "";
        }
        String msg = root.getMessage();
        if (msg == null)
        {
            return "null";
        }
        return StringUtils.defaultString(msg);
    }
}
