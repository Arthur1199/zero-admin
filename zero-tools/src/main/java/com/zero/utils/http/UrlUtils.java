package com.zero.utils.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:21
 * 说明: URL编码、解码和查询参数转换工具类
 */
public class UrlUtils {

    private UrlUtils() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:21
     * 说明: 使用UTF-8编码URL字符串
     * @param url 原始URL字符串
     * @return
     * 返回值: String
     * 返回值说明: 编码后的URL字符串，编码失败时返回原始字符串
     */
    public static String urlEncode(String url) {
        if (url == null) {
            return null;
        }
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:21
     * 说明: 使用UTF-8解码URL字符串
     * @param url 已编码URL字符串
     * @return
     * 返回值: String
     * 返回值说明: 解码后的URL字符串，解码失败时返回原始字符串
     */
    public static String urlDecode(String url) {
        if (url == null) {
            return null;
        }
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:21
     * 说明: 将Map参数转换为URL查询字符串
     * @param paramMap 参数Map
     * @return
     * 返回值: String
     * 返回值说明: key=value&key2=value2格式字符串，参数为空时返回空字符串
     */
    public static String toURL(Map<?, ?> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return "";
        }

        StringBuilder url = new StringBuilder();
        for (Iterator<?> iter = paramMap.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Object value = paramMap.get(key);
            url.append(key).append("=").append(value == null ? "" : value);
            if (iter.hasNext()) {
                url.append("&");
            }
        }
        return url.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:21
     * 说明: 将URL查询字符串转换为Map
     * @param url URL查询字符串，格式为key=value&key2=value2
     * @return
     * 返回值: Map
     * 返回值说明: 查询参数Map，参数为空时返回null
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map toMap(String url) {
        if (url == null) {
            return null;
        }

        Map paramMap = new HashMap();
        String[] paramArray = url.split("&");
        for (String item : paramArray) {
            String[] param = item.split("=");
            if (param.length == 2) {
                paramMap.put(param[0], param[1]);
            }
        }
        return paramMap;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:21
     * 说明: 从查询字符串中移除指定参数
     * @param queryString 查询字符串，格式为key=value&key2=value2
     * @param paramName 需要移除的参数名
     * @return
     * 返回值: String
     * 返回值说明: 移除指定参数后的查询字符串
     */
    public static String removeQueryParam(String queryString, String paramName) {
        if (isEmpty(queryString)) {
            return "";
        }
        if (isEmpty(paramName)) {
            return queryString;
        }

        String key = paramName + "=";
        int pos = queryString.indexOf(key);
        if (pos == -1) {
            return queryString;
        }

        String startStr = queryString.substring(0, pos);
        String endStr = queryString.substring(pos + key.length());
        pos = endStr.indexOf("&");
        if (pos == -1) {
            endStr = "";
        } else {
            endStr = endStr.substring(pos + 1);
        }

        String retStr = startStr + endStr;
        if (retStr.endsWith("&")) {
            retStr = retStr.substring(0, retStr.length() - 1);
        }
        return retStr;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:21
     * 说明: 判断字符串是否为空
     * @param value 待判断字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-为空，false-非空
     */
    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
