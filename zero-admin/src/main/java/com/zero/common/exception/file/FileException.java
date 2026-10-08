package com.zero.common.exception.file;

import com.zero.common.exception.base.BaseException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 文件信息异常类
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 文件异常
     * @param code 编码参数
     * @param args args参数
     */

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
