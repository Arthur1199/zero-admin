package com.zero.framework.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import com.zero.common.utils.ServletUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 服务相关配置
 */
@Component
public class ServerConfig
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取完整的请求路径，包括：域名，端口，上下文访问路径
     * @return
     * 返回值: 服务地址
     * 返回值说明：获取完整的请求路径，包括：域名，端口，上下文访问路径
     */
    public String getUrl()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        return getDomain(request);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Domain
     * @param request 请求参数
     * @return
     * 返回值: String
     * 返回值说明：获取Domain
     */

    public static String getDomain(HttpServletRequest request)
    {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }
}
