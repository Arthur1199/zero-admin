package com.zero.common.exception.user;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户不存在异常类
 */
public class UserNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 用户NotExists异常
     */

    public UserNotExistsException()
    {
        super("user.not.exists", null);
    }
}
