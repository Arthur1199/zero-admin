package com.zero.common.exception.user;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户密码不正确或不符合规范异常类
 */
public class UserPasswordNotMatchException extends UserException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 用户密码NotMatch异常
     */

    public UserPasswordNotMatchException()
    {
        super("user.password.not.match", null);
    }
}
