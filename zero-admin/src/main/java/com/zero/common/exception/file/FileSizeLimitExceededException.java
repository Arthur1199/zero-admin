package com.zero.common.exception.file;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 文件名大小限制异常类
 */
public class FileSizeLimitExceededException extends FileException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件大小LimitExceeded异常
     * @param defaultMaxSize defaultMax大小参数
     */

    public FileSizeLimitExceededException(long defaultMaxSize)
    {
        super("upload.exceed.maxSize", new Object[] { defaultMaxSize });
    }
}
