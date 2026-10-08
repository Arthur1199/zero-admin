package com.zero.common.enums;

import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 请求方式
 */
public enum HttpMethod
{
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;
    /**
     * mappings字段
     */

    private static final Map<String, HttpMethod> mappings = new HashMap<>(16);

    static
    {
        for (HttpMethod httpMethod : values())
        {
            mappings.put(httpMethod.name(), httpMethod);
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: resolve
     * @param method 方法参数
     * @return
     * 返回值: HttpMethod
     * 返回值说明：resolve
     */

    @Nullable
    public static HttpMethod resolve(@Nullable String method)
    {
        return (method != null ? mappings.get(method) : null);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: matches
     * @param method 方法参数
     * @return
     * 返回值: boolean
     * 返回值说明：matches
     */

    public boolean matches(String method)
    {
        return (this == resolve(method));
    }
}
