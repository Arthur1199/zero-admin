package com.zero.common.exception.file;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 文件上传异常类
 */
public class FileUploadException extends Exception
{

    private static final long serialVersionUID = 1L;
    /**
     * cause字段
     */

    private final Throwable cause;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件上传异常
     */

    public FileUploadException()
    {
        this(null, null);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件上传异常
     * @param msg msg参数
     */

    public FileUploadException(final String msg)
    {
        this(msg, null);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件上传异常
     * @param msg msg参数
     * @param cause cause参数
     */

    public FileUploadException(String msg, Throwable cause)
    {
        super(msg);
        this.cause = cause;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: printStackTrace
     * @param stream stream参数
     * @return
     * 返回值: void
     * 返回值说明：printStackTrace
     */

    @Override
    public void printStackTrace(PrintStream stream)
    {
        super.printStackTrace(stream);
        if (cause != null)
        {
            stream.println("Caused by:");
            cause.printStackTrace(stream);
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: printStackTrace
     * @param writer writer参数
     * @return
     * 返回值: void
     * 返回值说明：printStackTrace
     */

    @Override
    public void printStackTrace(PrintWriter writer)
    {
        super.printStackTrace(writer);
        if (cause != null)
        {
            writer.println("Caused by:");
            cause.printStackTrace(writer);
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Cause
     * @return
     * 返回值: Throwable
     * 返回值说明：获取Cause
     */

    @Override
    public Throwable getCause()
    {
        return cause;
    }
}
