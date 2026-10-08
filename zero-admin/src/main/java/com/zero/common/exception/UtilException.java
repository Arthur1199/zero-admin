package com.zero.common.exception;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 工具类异常
 */
public class UtilException extends RuntimeException
{
    private static final long serialVersionUID = 8247610319171014183L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 工具异常
     * @param e e参数
     */

    public UtilException(Throwable e)
    {
        super(e.getMessage(), e);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 工具异常
     * @param message 消息参数
     */

    public UtilException(String message)
    {
        super(message);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 工具异常
     * @param message 消息参数
     * @param throwable throwable参数
     */

    public UtilException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
