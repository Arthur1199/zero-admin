package com.zero.utils.sms;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.zero.utils.common.StringHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 封装 PaaSoo 的短信发送、记录查询、账户余额和号码状态接口，集中处理查询参数编码及 HTTP 响应。
 */
public class PaasooSmsUtil {

    /**
     * 默认请求超时时间，单位秒。
     */
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Paasoo短信发送接口地址。
     */
    private static final String URI_GET_SEND_SMS = "https://api.paasoo.cn/json";

    /**
     * Paasoo短信发送记录查询接口地址。
     */
    private static final String URI_GET_SEND_RECORD = "https://api.paasoo.cn/dlr";

    /**
     * Paasoo账户余额查询接口地址。
     */
    private static final String URI_GET_USER_BALANCE = "https://api.paasoo.cn/balance";

    /**
     * Paasoo号码状态查询接口地址。
     */
    private static final String URI_GET_VALID_NUMBER = "https://api.paasoo.cn/lookup";

    /**
     * 复用JDK17内置HTTP客户端，避免继续引入Apache HttpClient依赖。
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用本地配置提交一条短信，并读取第一条返回记录的业务状态。
     * @param to 含国家区号的收件号码
     * @param text 要发送的短信正文
     * @return
     * 返回值: boolean
     * 返回值说明: 仅当第一条响应记录的状态可解析为 0 时返回 true，空响应或无记录时返回 false
     * @throws Exception 配置读取、HTTP 请求失败或响应无法解析时向上传递
     */
    public static boolean sendSms(String to, String text) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("key", PaasooSmsConfig.getKey());
        params.put("secret", PaasooSmsConfig.getSecret());
        params.put("from", PaasooSmsConfig.getFrom());
        params.put("to", to);
        params.put("text", text);
        String result = get(URI_GET_SEND_SMS, params);
        if (StringHelper.isNotEmpty(result)) {
            System.out.println("发送短信接口返回参数：" + result);
            PaaSooSmsSingleResult singleResult = JSON.parseObject(result, PaaSooSmsSingleResult.class);
            if (singleResult != null) {
                String status = singleResult.getStatus();
                return StringHelper.isNotEmpty(status) && Long.parseLong(status) == 0;
            }
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用调用方提供的账号和发送方信息提交短信。
     * @param key PaaSoo API 账号
     * @param secret PaaSoo API 凭据，禁止记录或展示
     * @param from 供应商识别的发送方标识
     * @param to 含国家区号的接收号码
     * @param text 短信正文
     * @return
     * 返回值: String
     * 返回值说明: HTTP 请求成功后的原始响应正文
     * @throws Exception HTTP 请求失败或供应商返回非成功 HTTP 状态时向上传递
     */
    public static String getSendSms(String key, String secret, String from, String to, String text) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("secret", secret);
        params.put("from", from);
        params.put("to", to);
        params.put("text", text);
        return get(URI_GET_SEND_SMS, params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按供应商消息 ID 查询指定短信的发送记录。
     * @param key PaaSoo API 账号
     * @param secret PaaSoo API 凭据，禁止记录或展示
     * @param messageid 供应商返回的消息标识
     * @return
     * 返回值: String
     * 返回值说明: 包含发送记录的原始响应正文
     * @throws Exception HTTP 请求失败或供应商返回非成功 HTTP 状态时向上传递
     */
    public static String getSendRecord(String key, String secret, String messageid) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("secret", secret);
        params.put("messageid", messageid);
        return get(URI_GET_SEND_RECORD, params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询指定 PaaSoo 账号的可用余额。
     * @param key PaaSoo API 账号
     * @param secret PaaSoo API 凭据，禁止记录或展示
     * @return
     * 返回值: String
     * 返回值说明: 余额接口的原始响应正文
     * @throws Exception HTTP 请求失败或供应商返回非成功 HTTP 状态时向上传递
     */
    public static String getUserBalance(String key, String secret) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("secret", secret);
        return get(URI_GET_USER_BALANCE, params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 提交国家区号和本地号码，查询号码状态。
     * @param key PaaSoo API 账号
     * @param secret PaaSoo API 凭据，禁止记录或展示
     * @param countryCode 号码所在国家或地区的区号
     * @param nationalNumber 不带国家区号的本地号码
     * @return
     * 返回值: String
     * 返回值说明: 号码状态接口的原始响应正文
     * @throws Exception HTTP 请求失败或供应商返回非成功 HTTP 状态时向上传递
     */
    public static String getValidNumber(String key, String secret, String countryCode, String nationalNumber)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("secret", secret);
        params.put("countryCode", countryCode);
        params.put("nationalNumber", nationalNumber);
        return get(URI_GET_VALID_NUMBER, params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 构造带查询参数的 GET 请求，按已配置的超时时间等待响应。
     * @param url PaaSoo 接口地址
     * @param paramsMap 待附加的查询参数，可为空
     * @return
     * 返回值: String
     * 返回值说明: HTTP 2xx 响应的正文
     * @throws IOException 请求读取失败或 HTTP 状态码非 2xx 时抛出
     * @throws InterruptedException 调用线程等待响应时被中断
     */
    public static String get(String url, Map<String, String> paramsMap) throws IOException, InterruptedException {
        String queryString = buildQueryString(paramsMap);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + (queryString.isEmpty() ? "" : "?" + queryString)))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("User-Agent", "JDK-HttpClient/17")
                .GET()
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return response.body();
        }
        throw new IOException("HTTP request failed with status code: " + statusCode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 逐项对查询参数名和值编码，并用分隔符拼接。
     * @param paramsMap 查询参数，参数值为 null 时按空字符串拼接
     * @return
     * 返回值: String
     * 返回值说明: 不带问号的查询字符串，缺少参数时返回空字符串
     */
    private static String buildQueryString(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> param : paramsMap.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(encode(param.getKey())).append("=").append(encode(param.getValue()));
        }
        return builder.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 UTF-8 对查询参数名或值进行表单 URL 编码。
     * @param value 待编码的参数名或值
     * @return
     * 返回值: String
     * 返回值说明: 编码后的参数片段，null 按空字符串处理
     */
    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 演示 PaaSoo 的短信发送、记录、余额和号码查询。运行时可向供应商发出实际请求。
     * @param args 命令行参数，示例未读取
     * @return
     * 返回值: void
     * 返回值说明: 向控制台输出示例调用结果
     */
    public static void main(String[] args) {
        try {
            String key = "********";
            String secret = "********";
            String from = "********";
            String to = "********";
            String text = "********";
            String messageid = "********";
            String countryCode = "********";
            String nationalNumber = "********";

            System.out.println(PaasooSmsUtil.getSendSms(key, secret, from, to, text));
            System.out.println(PaasooSmsUtil.getSendRecord(key, secret, messageid));
            System.out.println(PaasooSmsUtil.getUserBalance(key, secret));
            System.out.println(PaasooSmsUtil.getValidNumber(key, secret, countryCode, nationalNumber));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
