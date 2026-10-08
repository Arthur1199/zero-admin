package com.zero.common.exception.user;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 验证码错误异常类
 */
public class CaptchaException extends UserException
{
    private static final long serialVersionUID = 1L;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 验证码异常
     */

    public CaptchaException()
    {
        super("user.jcaptcha.error", null);
    }
}
