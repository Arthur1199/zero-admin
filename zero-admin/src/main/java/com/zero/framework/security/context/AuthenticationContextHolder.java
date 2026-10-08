package com.zero.framework.security.context;

import org.springframework.security.core.Authentication;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 身份验证信息
 */
public class AuthenticationContextHolder
{
    /**
     * contextHolder字段
     */
    private static final ThreadLocal<Authentication> contextHolder = new ThreadLocal<>();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Context
     * @return
     * 返回值: Authentication
     * 返回值说明：获取Context
     */

    public static Authentication getContext()
    {
        return contextHolder.get();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Context
     * @param context context参数
     * @return
     * 返回值: void
     * 返回值说明：设置Context
     */

    public static void setContext(Authentication context)
    {
        contextHolder.set(context);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空Context
     * @return
     * 返回值: void
     * 返回值说明：清空Context
     */

    public static void clearContext()
    {
        contextHolder.remove();
    }
}
