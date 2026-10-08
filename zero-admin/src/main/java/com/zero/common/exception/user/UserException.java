package com.zero.common.exception.user;

import com.zero.common.exception.base.BaseException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户信息异常类
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 用户异常
     * @param code 编码参数
     * @param args args参数
     */

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
