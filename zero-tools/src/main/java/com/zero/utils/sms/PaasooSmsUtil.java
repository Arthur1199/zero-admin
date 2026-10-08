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
 * 作者: Jack
 * 时间: 2022年10月15日下午3:50:26
 * 说明: Paasoo短信HTTP接口工具类，基于JDK17内置HttpClient发送短信、查询发送记录、查询余额和校验号码状态
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
     * 作者: Jack
     * 时间: 2022年10月15日下午3:50:26
     * 说明: 从本地配置读取Paasoo账号并发送短信
     * @param to 接收手机号，需包含国家区号，例如英国号码格式为447883196637
     * @param text 短信内容
     * @return
     * 返回值: boolean
     * 返回值说明: true-发送成功，false-接口未返回成功状态
     * @throws Exception 配置读取、HTTP请求或响应解析失败时抛出
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
     * 作者: Jack
     * 时间: 2022年10月15日下午3:50:26
     * 说明: 调用Paasoo短信发送接口
     * @param key API账号
     * @param secret API密码，调用方不得写入日志
     * @param from SenderID
     * @param to 接收手机号，需包含国家区号
     * @param text 短信内容
     * @return
     * 返回值: String
     * 返回值说明: Paasoo返回的JSON字符串
     * @throws Exception HTTP请求失败时抛出
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
     * 作者: Jack
     * 时间: 2022年10月15日下午3:50:26
     * 说明: 查询Paasoo短信发送记录
     * @param key API账号
     * @param secret API密码，调用方不得写入日志
     * @param messageid 发送记录消息ID
     * @return
     * 返回值: String
     * 返回值说明: Paasoo返回的JSON字符串
     * @throws Exception HTTP请求失败时抛出
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
     * 作者: Jack
     * 时间: 2022年10月15日下午3:50:26
     * 说明: 查询Paasoo账户余额
     * @param key API账号
     * @param secret API密码，调用方不得写入日志
     * @return
     * 返回值: String
     * 返回值说明: Paasoo返回的JSON字符串
     * @throws Exception HTTP请求失败时抛出
     */
    public static String getUserBalance(String key, String secret) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("secret", secret);
        return get(URI_GET_USER_BALANCE, params);
    }

    /**
     *
     * 作者: Jack
     * 时间: 2022年10月15日下午3:50:26
     * 说明: 查询Paasoo号码状态
     * @param key API账号
     * @param secret API密码，调用方不得写入日志
     * @param countryCode 待查询手机号所属国家区号
     * @param nationalNumber 待校验手机号，不包含国家区号
     * @return
     * 返回值: String
     * 返回值说明: Paasoo返回的JSON字符串
     * @throws Exception HTTP请求失败时抛出
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
     * 作者: Jack
     * 时间: 2026/5/17 00:15
     * 说明: 基于JDK17 HttpClient执行通用GET请求
     * @param url 请求地址
     * @param paramsMap 查询参数Map
     * @return
     * 返回值: String
     * 返回值说明: HTTP 2xx时返回响应内容
     * @throws IOException HTTP状态码非2xx或读取响应失败时抛出
     * @throws InterruptedException 请求线程被中断时抛出
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
     * 作者: Jack
     * 时间: 2026/5/17 00:15
     * 说明: 构建URL查询字符串
     * @param paramsMap 查询参数Map，value为空时按空字符串处理
     * @return
     * 返回值: String
     * 返回值说明: 已按UTF-8编码的查询字符串
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
     * 作者: Jack
     * 时间: 2026/5/17 00:15
     * 说明: 对查询参数进行UTF-8编码
     * @param value 参数值
     * @return
     * 返回值: String
     * 返回值说明: URL安全的参数值
     */
    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Jack
     * 时间: 2022年10月15日下午3:50:26
     * 说明: 本地测试入口，生产环境不要通过main方法发送短信或查询账号信息
     * @param args 启动参数，当前未使用
     * @return
     * 返回值: void
     * 返回值说明: 控制台输出测试调用结果
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
