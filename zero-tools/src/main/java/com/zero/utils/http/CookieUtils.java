package com.zero.utils.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: Cookie 工具类
 */
public final class CookieUtils {

    private static Logger logger = LoggerFactory.getLogger(CookieUtils.class);

    /**
     * 登录令牌
     */
    public static final String ACCESS_TOKEN = "access_token";
    
    
    /**
     * 游客登录令牌
     */
//    public static final String YK_ACCESS_TOKEN = "yk_access_token";
    
    
    /**
     * 普通会员登录令牌
     */
//    public static final String HY_ACCESS_TOKEN = "hy_access_token";
    
    /**
     * 用户登录类型
     */
//    public static final String LOGIN_TYPE = "login_type";
    
    /**
     * 选择的语言
     */
    public static final String LANGUAGE_TOKEN = "language";
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取请求中指定名称的 Cookie 原始值。
     * @param request 携带 Cookie 的 HTTP 请求
     * @param cookieName Cookie 名称
     * @return
     * 返回值: String
     * 返回值说明: 匹配到的值，未找到时返回 null
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        return getCookieValue(request, cookieName, false);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按名称读取第一个匹配的 Cookie，可以选择使用 UTF-8 进行 URL 解码。
     * @param request 携带 Cookie 的 HTTP 请求
     * @param cookieName Cookie 名称
     * @param isDecoder 为 true 时执行 UTF-8 URL 解码
     * @return
     * 返回值: String
     * 返回值说明: 匹配到的值，未找到或解码失败时返回 null
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookieList.length; i++) {
                if (cookieList[i].getName().equals(cookieName)) {
                    if (isDecoder) {
                        retValue = URLDecoder.decode(cookieList[i].getValue(), "UTF-8");
                    } else {
                        retValue = cookieList[i].getValue();
                    }
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按名称读取第一个匹配的 Cookie，使用指定字符集进行 URL 解码。
     * @param request 携带 Cookie 的 HTTP 请求
     * @param cookieName Cookie 名称
     * @param encodeString 解码使用的字符集名称
     * @return
     * 返回值: String
     * 返回值说明: 解码后的值，未找到或解码失败时返回 null
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, String encodeString) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookieList.length; i++) {
                if (cookieList[i].getName().equals(cookieName)) {
                    retValue = URLDecoder.decode(cookieList[i].getValue(), encodeString);
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值 不设置生效时间默认浏览器关闭即失效,也不编码
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值 不设置生效时间默认浏览器关闭即失效,也不编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue) {
        setCookie(request, response, cookieName, cookieValue, -1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值 在指定时间内生效,但不编码
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @param cookieMaxAge 有效期秒数，仅正数会写入 Max-Age
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值 在指定时间内生效,但不编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue, int cookieMaxAge) {
        setCookie(request, response, cookieName, cookieValue, cookieMaxAge, false);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值 不设置生效时间,但编码
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @param isEncode 是否执行 URL 编码
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值 不设置生效时间,但编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue, boolean isEncode) {
        setCookie(request, response, cookieName, cookieValue, -1, isEncode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值 在指定时间内生效, 编码参数
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @param cookieMaxAge 有效期秒数，仅正数会写入 Max-Age
     * @param isEncode 是否执行 URL 编码
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值 在指定时间内生效, 编码参数
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue, int cookieMaxAge, boolean isEncode) {
        doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, isEncode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值 在指定时间内生效, 编码参数(指定编码)
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @param cookieMaxAge 有效期秒数，仅正数会写入 Max-Age
     * @param encodeString URL 编解码使用的字符集名称
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值 在指定时间内生效, 编码参数(指定编码)
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue, int cookieMaxAge, String encodeString) {
        doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, encodeString);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用空值覆盖路径为 / 的 Cookie。内部仅为正数设置 Max-Age，本方法传入 -1。
     * @param request 原始请求，供内部读取请求地址
     * @param response 写入 Cookie 的响应
     * @param cookieName 要清空值的 Cookie 名称
     * @return
     * 返回值: void
     * 返回值说明: 在响应中添加同名空值 Cookie
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response,
                                    String cookieName) {
        doSetCookie(request, response, cookieName, "", -1, false);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值，并使其在指定时间内生效
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @param cookieMaxAge cookie生效的最大秒数
     * @param isEncode 是否执行 URL 编码
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值，并使其在指定时间内生效
     */
    @SuppressWarnings("unused")
	private static final void doSetCookie(HttpServletRequest request, HttpServletResponse response,
                                          String cookieName, String cookieValue, int cookieMaxAge, boolean isEncode) {
        try {

            if (cookieValue == null) {

                cookieValue = "";

            } else if (isEncode) {

                cookieValue = URLEncoder.encode(cookieValue, "utf-8");

            }

            Cookie cookie = new Cookie(cookieName, cookieValue);

            if (cookieMaxAge > 0)

                cookie.setMaxAge(cookieMaxAge);

            if (null != request) {// 设置域名的cookie

                String domainName = getDomainName(request);

//                if (!"localhost".equals(domainName)) {
//
//                    cookie.setDomain(domainName);
//
//                }

            }

            cookie.setPath("/");

            response.addCookie(cookie);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置Cookie的值，并使其在指定时间内生效
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param cookieName Cookie 名称
     * @param cookieValue 要写入的 Cookie 值
     * @param cookieMaxAge cookie生效的最大秒数
     * @param encodeString URL 编解码使用的字符集名称
     * @return
     * 返回值: void
     * 返回值说明: 设置Cookie的值，并使其在指定时间内生效
     */
    private static final void doSetCookie(HttpServletRequest request, HttpServletResponse response,
                                          String cookieName, String cookieValue, int cookieMaxAge, String encodeString) {
        try {

            if (cookieValue == null) {

                cookieValue = "";
            } else {

                cookieValue = URLEncoder.encode(cookieValue, encodeString);
            }

            Cookie cookie = new Cookie(cookieName, cookieValue);

            logger.info("开始写入Cookie，CookieName: " + cookieName + ",CookieValue: " + cookieValue);

            if (cookieMaxAge > 0)

                cookie.setMaxAge(cookieMaxAge);

            if (null != request) {// 设置域名的cookie

                String domainName = getDomainName(request);

                if (!"localhost".equals(domainName)) {

                    cookie.setDomain(domainName);

                }

                logger.info("设置Cookie域名: " + domainName);

            }

            cookie.setPath("/");

            response.addCookie(cookie);

            logger.info("写入Cookie成功，CookieName: " + cookieName);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 得到cookie的域名
     * @param request 当前 HTTP 请求
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    private static String getDomainName(HttpServletRequest request) {
        String domainName = null;

        String serverName = request.getRequestURL().toString();

        if (serverName.isEmpty()) {

            domainName = "";

        } else {

            serverName = serverName.toLowerCase();

            serverName = serverName.substring(7);

            final int end = serverName.indexOf("/");

            serverName = serverName.substring(0, end);

            domainName = serverName;

        }

        if (domainName.indexOf(":") > 0) {

            String[] ary = domainName.split(":");

            domainName = ary[0];
        }

        return domainName;
    }

}
