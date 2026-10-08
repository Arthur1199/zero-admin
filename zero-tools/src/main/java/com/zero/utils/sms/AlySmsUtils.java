package com.zero.utils.sms;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 *
 * 作者: Jack
 * 时间: 2022年10月15日下午3:33:26
 * 说明: 阿里云短信发送工具类，使用JDK17内置HTTP客户端直连短信OpenAPI，避免引入阿里云SDK传递依赖
 */
public class AlySmsUtils {

    /**
     * 阿里云短信OpenAPI访问地址。
     */
    private static final String API_URL = "https://dysmsapi.aliyuncs.com/";

    /**
     * 阿里云短信OpenAPI版本。
     */
    private static final String API_VERSION = "2017-05-25";

    /**
     * 阿里云短信发送接口名称。
     */
    private static final String ACTION_SEND_SMS = "SendSms";

    /**
     * 阿里云短信接口成功状态码。
     */
    private static final String SUCCESS_CODE = "OK";

    /**
     * 阿里云短信限流提示关键字，用于转换为业务可理解的错误信息。
     */
    private static final String RATE_LIMIT_MESSAGE = "触发小时级流控";

    /**
     * OpenAPI默认请求超时时间，单位秒。
     */
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * UTC时间格式，阿里云OpenAPI签名参数要求使用该格式。
     */
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    /**
     * JDK17内置HTTP客户端，可复用连接，避免每次请求重复创建。
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

    /**
	 *
	 * 作者: Jack
	 * 时间: 2022年10月15日下午3:45:01
	 * 说明: 发送阿里云验证码短信
	 *
	 * @param telephone    接收短信的手机号
	 * @param code         验证码，写入短信模板变量code
	 * @param signName     阿里云短信签名名称
	 * @param templateCode 阿里云短信模板编码
	 * @throws Exception 配置缺失、签名失败、请求异常或触发限流时抛出
	 */
    public static void sendSms(String telephone, String code, String signName, String templateCode) throws Exception {
        try {
            String accessKeyId = AlySmsConfig.getAccessKeyId();
            String accessKeySecret = AlySmsConfig.getAccessKeySecret();
            Map<String, String> params = buildRequestParams(accessKeyId, telephone, code, signName, templateCode);
            params.put("Signature", sign(accessKeySecret, params));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "?" + buildQueryString(params)))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			handleResponse(response.body());
		} catch (Exception e) {
            System.out.println("短信发送失败，系统异常");
            throw e;
        }
    }

    /**
     *
     * 作者: Jack
     * 时间: 2026/5/16 23:59
     * 说明: 构建阿里云短信OpenAPI请求参数
     * @param accessKeyId 阿里云AccessKeyId
     * @param telephone 接收短信的手机号
     * @param code 验证码，写入短信模板变量code
     * @param signName 阿里云短信签名名称
     * @param templateCode 阿里云短信模板编码
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 已包含公共参数和业务参数的有序参数集合
     */
    private static Map<String, String> buildRequestParams(String accessKeyId, String telephone, String code,
                                                          String signName, String templateCode) {
        Map<String, String> params = new TreeMap<>();
        params.put("AccessKeyId", accessKeyId);
        params.put("Action", ACTION_SEND_SMS);
        params.put("Format", "JSON");
        params.put("RegionId", "cn-hangzhou");
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("SignatureVersion", "1.0");
        params.put("Timestamp", UTC_FORMATTER.format(Instant.now()));
        params.put("Version", API_VERSION);
        params.put("PhoneNumbers", telephone);
        params.put("SignName", signName);
        params.put("TemplateCode", templateCode);
        params.put("TemplateParam", "{\"code\":\"" + code + "\"}");
        return params;
    }

    /**
     *
     * 作者: Jack
     * 时间: 2026/5/16 23:59
     * 说明: 生成阿里云OpenAPI签名
     * @param accessKeySecret 阿里云AccessKeySecret，调用方不得写入日志
     * @param params 参与签名的请求参数，不包含Signature参数
     * @return
     * 返回值: String
     * 返回值说明: Base64编码后的HMAC-SHA1签名
     * @throws Exception 签名算法初始化或计算失败时抛出
     */
    private static String sign(String accessKeySecret, Map<String, String> params) throws Exception {
        String canonicalizedQueryString = buildQueryString(params);
        String stringToSign = "GET&%2F&" + percentEncode(canonicalizedQueryString);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     *
     * 作者: Jack
     * 时间: 2026/5/16 23:59
     * 说明: 构建OpenAPI查询字符串
     * @param params 请求参数
     * @return
     * 返回值: String
     * 返回值说明: 按参数名排序并完成URL编码的查询字符串
     */
    private static String buildQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(percentEncode(entry.getKey())).append("=").append(percentEncode(entry.getValue()));
        }
        return builder.toString();
    }

    /**
     *
     * 作者: Jack
     * 时间: 2026/5/16 23:59
     * 说明: 按阿里云OpenAPI要求进行URL编码
     * @param value 待编码字符串
     * @return
     * 返回值: String
     * 返回值说明: 符合RFC3986要求的编码结果
     */
    private static String percentEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    /**
	 *
	 * 作者: Jack
	 * 时间: 2026/5/16 23:59
	 * 说明: 解析阿里云短信响应并转换为布尔结果
	 *
	 * @param responseBody 阿里云响应JSON
	 * @throws Exception 触发阿里云限流或响应解析失败时抛出
	 */
    private static void handleResponse(String responseBody) throws Exception {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            System.out.println("短信发送失败，阿里云短信未响应数据");
            return;
        }
        JSONObject body = JSON.parseObject(responseBody);
        String code = getString(body, "Code");
        String message = getString(body, "Message");
        if (SUCCESS_CODE.equals(code)) {
            System.out.println("短信发送成功");
            return;
        }
        if (message != null && message.startsWith(RATE_LIMIT_MESSAGE)) {
            System.err.println("请不要频繁发送短信，阿里云已触发小时级流控");
            throw new Exception("请不要频繁发送短信");
        }
        System.out.println("短信发送失败");
	}

    /**
     *
     * 作者: Jack
     * 时间: 2026/5/16 23:59
     * 说明: 从JSON对象中安全获取字符串字段
     * @param body 响应JSON对象
     * @param key 字段名称
     * @return
     * 返回值: String
     * 返回值说明: 字段不存在或为空时返回null
     */
    private static String getString(JSONObject body, String key) {
        if (body == null || !body.containsKey(key)) {
            return null;
        }
        return body.getString(key);
    }

    /**
     *
     * 作者: Jack
     * 时间: 2022年10月15日下午3:41:29
     * 说明: 发送测试短信，调用前需要确认配置文件中已存在有效阿里云AccessKey
     * @param telephone 接收短信的手机号
     * @param code 验证码
     * 返回值: void
     * 返回值说明: 测试短信发送失败时抛出异常
     * @throws Exception 阿里云短信发送失败时抛出
     */
    public static void smsToTest(String telephone, String code) throws Exception {
        String templateCode = "SMS_123456789";
        String signName = "测试模板";
        sendSms(telephone, code, signName, templateCode);
    }

    /**
     *
     * 作者: Jack
     * 时间: 2022年10月15日下午3:41:29
     * 说明: 本地测试入口，生产环境不要通过main方法发送短信
     * @param args 启动参数，当前未使用
     * 返回值: void
     * 返回值说明: 控制台输出测试发送结果
     */
    public static void main(String[] args) {
        try {
            AlySmsUtils.smsToTest("17004754322", "123456");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
