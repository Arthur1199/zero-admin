package com.zero.common.exception.user;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 验证码失效异常类
 */
public class CaptchaExpireException extends UserException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 验证码Expire异常
     */

    public CaptchaExpireException()
    {
        super("user.jcaptcha.expire", null);
    }
}
