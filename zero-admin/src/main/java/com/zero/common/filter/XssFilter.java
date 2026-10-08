package com.zero.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.zero.common.utils.StringUtils;
import com.zero.common.enums.HttpMethod;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 防止XSS攻击的过滤器
 */
public class XssFilter implements Filter
{
    /**
     * 排除链接
     */
    public List<String> excludes = new ArrayList<>();
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
        String tempExcludes = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(tempExcludes))
        {
            String[] urls = tempExcludes.split(",");
            for (String url : urls)
            {
                excludes.add(url);
            }
        }
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
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp))
        {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 处理Exclude地址
     * @param request 请求参数
     * @param response 响应参数
     * @return
     * 返回值: boolean
     * 返回值说明：处理Exclude地址
     */

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response)
    {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method))
        {
            return true;
        }
        return StringUtils.matches(url, excludes);
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