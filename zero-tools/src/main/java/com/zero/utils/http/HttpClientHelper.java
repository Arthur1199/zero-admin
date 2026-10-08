package com.zero.utils.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 00:08
 * 说明: HTTP请求工具类，基于JDK17内置HttpClient提供GET、POST表单、POST JSON、POST XML和文件上传能力
 */
public class HttpClientHelper {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

    /**
     * 默认响应字符集，接口返回内容统一按UTF-8读取。
     */
    private static final String CHARSET_UTF_8 = StandardCharsets.UTF_8.name();

    /**
     * XML请求Content-Type。
     */
    private static final String CONTENT_TYPE_TEXT_XML = "text/xml";

    /**
     * 表单请求Content-Type。
     */
    private static final String CONTENT_TYPE_FORM_URL = "application/x-www-form-urlencoded";

    /**
     * JSON请求Content-Type。
     */
    private static final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";

    /**
     * 默认请求超时时间，单位秒。
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * 复用的JDK HTTP客户端，替代已停止维护的Apache HttpClient 4.x。
     */
    private static final HttpClient HTTP_CLIENT = buildHttpClient();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 创建HTTP客户端，沿用历史工具类信任证书策略，避免自签名HTTPS接口升级后不可用
     * @return
     * 返回值: HttpClient
     * 返回值说明: 可复用的HTTP客户端
     */
    private static HttpClient buildHttpClient() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm(null);
            return HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                    .sslContext(sslContext)
                    .sslParameters(sslParameters)
                    .build();
        } catch (Exception e) {
            logger.error("init jdk http client failed", e);
            return HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                    .build();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 构建请求超时时间，未传或小于等于0时使用默认30秒
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: Duration
     * 返回值说明: JDK HttpClient使用的请求超时时间
     */
    private static Duration buildTimeout(int timeoutSeconds) {
        return Duration.ofSeconds(timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 设置默认请求头和业务自定义请求头
     * @param builder HTTP请求构建器
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: void
     * 返回值说明: 请求头会写入当前请求构建器
     */
    private static void setRequestHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        builder.header("Accept", "*/*");
        builder.header("Connection", "Keep-Alive");
        builder.header("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            builder.setHeader(key, entry.getValue() == null ? "" : entry.getValue());
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 执行HTTP请求并读取响应内容
     * @param request HTTP请求对象
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应正文，其它2xx状态返回null，异常时返回null
     */
    private static String send(HttpRequest request) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            if (statusCode >= 300) {
                throw new IOException("HTTP Request is not success, Response code is " + statusCode);
            }
            return statusCode == 200 ? response.body() : null;
        } catch (Exception e) {
            logger.error("send http request failed", e);
            return null;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 构建基础HTTP请求
     * @param httpUrl 请求地址
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: HttpRequest.Builder
     * 返回值说明: 已设置地址、请求头和超时时间的请求构建器
     */
    private static HttpRequest.Builder buildRequest(String httpUrl, Map<String, String> headers, int timeoutSeconds) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(httpUrl))
                .timeout(buildTimeout(timeoutSeconds));
        setRequestHeaders(builder, headers);
        return builder;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送无请求体POST请求
     * @param httpUrl 请求地址
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl) {
        return sendHttpPost(httpUrl, (String) null, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送无请求体POST请求，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, int timeoutSeconds) {
        return sendHttpPost(httpUrl, (String) null, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送GET请求
     * @param httpUrl 请求地址
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpGet(String httpUrl) {
        return sendHttpGet(httpUrl, null, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送GET请求，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpGet(String httpUrl, int timeoutSeconds) {
        return sendHttpGet(httpUrl, null, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送GET请求，将Map参数拼接到URL查询字符串中
     * @param httpUrl 请求地址
     * @param maps 查询参数Map，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpGet(String httpUrl, Map<String, String> maps) {
        return sendHttpGet(httpUrl, maps, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送GET请求，将Map参数拼接到URL查询字符串中，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param maps 查询参数Map，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpGet(String httpUrl, Map<String, String> maps, int timeoutSeconds) {
        return sendHttpGet(httpUrl, maps, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送GET请求，将Map参数拼接到URL查询字符串中，并支持自定义请求头
     * @param httpUrl 请求地址
     * @param maps 查询参数Map，可为空
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpGet(String httpUrl, Map<String, String> maps, Map<String, String> headers) {
        return sendHttpGet(httpUrl, maps, headers, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送GET请求，将Map参数拼接到URL查询字符串中，并支持自定义请求头和超时时间
     * @param httpUrl 请求地址
     * @param maps 查询参数Map，可为空
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpGet(String httpUrl, Map<String, String> maps, Map<String, String> headers,
            int timeoutSeconds) {
        String param = convertStringParameter(maps);
        String url = appendQueryString(httpUrl, param);
        HttpRequest request = buildRequest(url, headers, timeoutSeconds).GET().build();
        return send(request);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送multipart文件上传POST请求
     * @param httpUrl 请求地址
     * @param maps 表单参数，可为空
     * @param fileLists 上传文件列表，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists) {
        return sendHttpPost(httpUrl, maps, fileLists, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送multipart文件上传POST请求，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param maps 表单参数，可为空
     * @param fileLists 上传文件列表，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists,
            int timeoutSeconds) {
        return sendHttpPost(httpUrl, maps, fileLists, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送multipart文件上传POST请求，并支持自定义请求头
     * @param httpUrl 请求地址
     * @param maps 表单参数，可为空
     * @param fileLists 上传文件列表，可为空
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists,
            Map<String, String> headers) {
        return sendHttpPost(httpUrl, maps, fileLists, headers, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送multipart文件上传POST请求，并支持自定义请求头和超时时间
     * @param httpUrl 请求地址
     * @param maps 表单参数，可为空
     * @param fileLists 上传文件列表，可为空
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, List<File> fileLists,
            Map<String, String> headers, int timeoutSeconds) {
        try {
            String boundary = "----ZeroBoundary" + UUID.randomUUID();
            HttpRequest.BodyPublisher bodyPublisher = buildMultipartBody(maps, fileLists, boundary);
            HttpRequest.Builder builder = buildRequest(httpUrl, headers, timeoutSeconds)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(bodyPublisher);
            return send(builder.build());
        } catch (Exception e) {
            logger.error("build multipart request failed", e);
            return null;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，参数格式为key1=value1&key2=value2
     * @param httpUrl 请求地址
     * @param params 表单参数字符串，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, String params) {
        return sendHttpPost(httpUrl, params, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param params 表单参数字符串，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, String params, int timeoutSeconds) {
        return sendHttpPost(httpUrl, params, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，并支持自定义请求头
     * @param httpUrl 请求地址
     * @param params 表单参数字符串，可为空
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, String params, Map<String, String> headers) {
        return sendHttpPost(httpUrl, params, headers, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，并支持自定义请求头和超时时间
     * @param httpUrl 请求地址
     * @param params 表单参数字符串，可为空
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, String params, Map<String, String> headers, int timeoutSeconds) {
        return sendStringPost(httpUrl, params, CONTENT_TYPE_FORM_URL, headers, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，将Map参数拼接为key=value格式
     * @param httpUrl 请求地址
     * @param maps 表单参数Map，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps) {
        return sendHttpPost(httpUrl, convertStringParameter(maps));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，将Map参数拼接为key=value格式，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param maps 表单参数Map，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, int timeoutSeconds) {
        return sendHttpPost(httpUrl, convertStringParameter(maps), timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，将Map参数拼接为key=value格式，并支持自定义请求头
     * @param httpUrl 请求地址
     * @param maps 表单参数Map，可为空
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Map<String, String> headers) {
        return sendHttpPost(httpUrl, convertStringParameter(maps), headers);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送表单POST请求，将Map参数拼接为key=value格式，并支持自定义请求头和超时时间
     * @param httpUrl 请求地址
     * @param maps 表单参数Map，可为空
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Map<String, String> headers,
            int timeoutSeconds) {
        return sendHttpPost(httpUrl, convertStringParameter(maps), headers, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送JSON POST请求
     * @param httpUrl 请求地址
     * @param paramsJson JSON请求体，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostJson(String httpUrl, String paramsJson) {
        return sendHttpPostJson(httpUrl, paramsJson, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送JSON POST请求，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param paramsJson JSON请求体，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostJson(String httpUrl, String paramsJson, int timeoutSeconds) {
        return sendHttpPostJson(httpUrl, paramsJson, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送JSON POST请求，并支持自定义请求头
     * @param httpUrl 请求地址
     * @param paramsJson JSON请求体，可为空
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostJson(String httpUrl, String paramsJson, Map<String, String> headers) {
        return sendHttpPostJson(httpUrl, paramsJson, headers, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送JSON POST请求，并支持自定义请求头和超时时间
     * @param httpUrl 请求地址
     * @param paramsJson JSON请求体，可为空
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostJson(String httpUrl, String paramsJson, Map<String, String> headers,
            int timeoutSeconds) {
        return sendStringPost(httpUrl, paramsJson, CONTENT_TYPE_JSON_URL, headers, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送XML POST请求
     * @param httpUrl 请求地址
     * @param paramsXml XML请求体，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostXml(String httpUrl, String paramsXml) {
        return sendHttpPostXml(httpUrl, paramsXml, null, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送XML POST请求，并支持自定义超时时间
     * @param httpUrl 请求地址
     * @param paramsXml XML请求体，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostXml(String httpUrl, String paramsXml, int timeoutSeconds) {
        return sendHttpPostXml(httpUrl, paramsXml, null, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送XML POST请求，并支持自定义请求头
     * @param httpUrl 请求地址
     * @param paramsXml XML请求体，可为空
     * @param headers 自定义请求头，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostXml(String httpUrl, String paramsXml, Map<String, String> headers) {
        return sendHttpPostXml(httpUrl, paramsXml, headers, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送XML POST请求，并支持自定义请求头和超时时间
     * @param httpUrl 请求地址
     * @param paramsXml XML请求体，可为空
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    public static String sendHttpPostXml(String httpUrl, String paramsXml, Map<String, String> headers,
            int timeoutSeconds) {
        return sendStringPost(httpUrl, paramsXml, CONTENT_TYPE_TEXT_XML, headers, timeoutSeconds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 发送字符串请求体POST请求
     * @param httpUrl 请求地址
     * @param body 请求体内容，可为空
     * @param contentType 请求体类型
     * @param headers 自定义请求头，可为空
     * @param timeoutSeconds 超时时间，单位秒
     * @return
     * 返回值: String
     * 返回值说明: HTTP 200时返回响应内容，其它场景返回null
     */
    private static String sendStringPost(String httpUrl, String body, String contentType, Map<String, String> headers,
            int timeoutSeconds) {
        HttpRequest request = buildRequest(httpUrl, headers, timeoutSeconds)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 构建multipart请求体
     * @param maps 表单参数，可为空
     * @param fileLists 上传文件列表，可为空
     * @param boundary multipart边界
     * @return
     * 返回值: HttpRequest.BodyPublisher
     * 返回值说明: 可直接发送的multipart请求体
     * @throws IOException 文件读取失败时抛出
     */
    private static HttpRequest.BodyPublisher buildMultipartBody(Map<String, String> maps, List<File> fileLists,
            String boundary) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (maps != null) {
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                writeStringPart(output, boundary, entry.getKey(), entry.getValue());
            }
        }
        if (fileLists != null) {
            for (File file : fileLists) {
                if (file == null || !file.exists() || !file.isFile()) {
                    continue;
                }
                writeFilePart(output, boundary, "files", file);
            }
        }
        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArray(output.toByteArray());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 写入multipart普通表单字段
     * @param output multipart输出流
     * @param boundary multipart边界
     * @param name 表单字段名称
     * @param value 表单字段值，可为空
     * 返回值: void
     * 返回值说明: 表单字段被追加到multipart请求体中
     * @throws IOException 写入失败时抛出
     */
    private static void writeStringPart(ByteArrayOutputStream output, String boundary, String name, String value)
            throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 写入multipart文件字段，文件字段名沿用历史固定值files
     * @param output multipart输出流
     * @param boundary multipart边界
     * @param name 文件字段名称
     * @param file 上传文件
     * 返回值: void
     * 返回值说明: 文件被追加到multipart请求体中
     * @throws IOException 文件读取或写入失败时抛出
     */
    private static void writeFilePart(ByteArrayOutputStream output, String boundary, String name, File file)
            throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getName()
                + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(Files.readAllBytes(file.toPath()));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 将查询参数拼接到URL上
     * @param httpUrl 请求地址
     * @param queryString 查询字符串，可为空
     * @return
     * 返回值: String
     * 返回值说明: 已拼接查询参数的URL
     */
    private static String appendQueryString(String httpUrl, String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return httpUrl;
        }
        return httpUrl + (httpUrl.contains("?") ? "&" : "?") + queryString;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 00:08
     * 说明: 将Map参数拼接为key=value&key2=value2格式，保持历史实现不额外做URL编码
     * @param parameterMap 参数Map，value为空时按空字符串处理
     * @return
     * 返回值: String
     * 返回值说明: 拼接后的查询字符串或表单字符串
     */
    @SuppressWarnings("rawtypes")
    private static String convertStringParameter(Map parameterMap) {
        StringBuilder parameterBuffer = new StringBuilder();
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = parameterMap.get(key) == null ? "" : (String) parameterMap.get(key);
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }
}
