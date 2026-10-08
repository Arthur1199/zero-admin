package com.zero.common.filter;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: Repeatable 过滤器
 */
public class RepeatableFilter implements Filter
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 初始化
     * @param filterConfig 过滤器参数配置参数
     * @return
     * 返回值: void
     * 返回值说明：初始化
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: do过滤器
     * @param request 请求参数
     * @param response 响应参数
     * @param chain chain参数
     * @return
     * 返回值: void
     * 返回值说明：do过滤器
     */

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        ServletRequest requestWrapper = null;
        if (request instanceof HttpServletRequest
                && StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE))
        {
            requestWrapper = new RepeatedlyRequestWrapper((HttpServletRequest) request, response);
        }
        if (null == requestWrapper)
        {
            chain.doFilter(request, response);
        }
        else
        {
            chain.doFilter(requestWrapper, response);
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: destroy
     * @return
     * 返回值: void
     * 返回值说明：destroy
     */

    @Override
    public void destroy()
    {

    }
}
