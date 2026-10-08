package com.zero.common.utils;

import com.zero.common.constant.Constants;
import com.zero.common.core.text.Convert;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 客户端工具类
 */
public class ServletUtils
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取String参数
     * @param name 名称参数
     * @return
     * 返回值: String
     * 返回值说明：获取String参数
     */
    public static String getParameter(String name)
    {
        return getRequest().getParameter(name);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取String参数
     * @param name 名称参数
     * @param defaultValue default值参数
     * @return
     * 返回值: String
     * 返回值说明：获取String参数
     */
    public static String getParameter(String name, String defaultValue)
    {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Integer参数
     * @param name 名称参数
     * @return
     * 返回值: Integer
     * 返回值说明：获取Integer参数
     */
    public static Integer getParameterToInt(String name)
    {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Integer参数
     * @param name 名称参数
     * @param defaultValue default值参数
     * @return
     * 返回值: Integer
     * 返回值说明：获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue)
    {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Boolean参数
     * @param name 名称参数
     * @return
     * 返回值: Boolean
     * 返回值说明：获取Boolean参数
     */
    public static Boolean getParameterToBool(String name)
    {
        return Convert.toBool(getRequest().getParameter(name));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Boolean参数
     * @param name 名称参数
     * @param defaultValue default值参数
     * @return
     * 返回值: Boolean
     * 返回值说明：获取Boolean参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue)
    {
        return Convert.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获得所有请求参数
     * @param request 请求对象{@link ServletRequest}
     * @return
     * 返回值: Map
     * 返回值说明：获得所有请求参数
     */
    public static Map<String, String[]> getParams(ServletRequest request)
    {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获得所有请求参数
     * @param request 请求对象{@link ServletRequest}
     * @return
     * 返回值: Map
     * 返回值说明：获得所有请求参数
     */
    public static Map<String, String> getParamMap(ServletRequest request)
    {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet())
        {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取request
     * @return
     * 返回值: HttpServletRequest
     * 返回值说明：获取request
     */
    public static HttpServletRequest getRequest()
    {
        return getRequestAttributes().getRequest();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取response
     * @return
     * 返回值: HttpServletResponse
     * 返回值说明：获取response
     */
    public static HttpServletResponse getResponse()
    {
        return getRequestAttributes().getResponse();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取session
     * @return
     * 返回值: HttpSession
     * 返回值说明：获取session
     */
    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取请求Attributes
     * @return
     * 返回值: ServletRequestAttributes
     * 返回值说明：获取请求Attributes
     */

    public static ServletRequestAttributes getRequestAttributes()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 将字符串渲染到客户端
     * @param response 渲染对象
     * @param string 待渲染的字符串
     * @return
     * 返回值: void
     * 返回值说明：将字符串渲染到客户端
     */
    public static void renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 是否是Ajax异步请求
     * @param request 请求参数
     * @return
     * 返回值: boolean
     * 返回值说明：是否是Ajax异步请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request)
    {
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains("application/json"))
        {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest"))
        {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml"))
        {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 内容编码
     * @param str 内容
     * @return
     * 返回值: 编码后的内容
     * 返回值说明：内容编码
     */
    public static String urlEncode(String str)
    {
        try
        {
            return URLEncoder.encode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 内容解码
     * @param str 内容
     * @return
     * 返回值: 解码后的内容
     * 返回值说明：内容解码
     */
    public static String urlDecode(String str)
    {
        try
        {
            return URLDecoder.decode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }
}
