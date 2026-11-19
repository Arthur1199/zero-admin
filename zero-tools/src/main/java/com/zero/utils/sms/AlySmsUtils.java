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
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 使用 JDK HTTP 客户端提交阿里云验证码短信，并根据供应商的响应记录发送结果或提示限流。
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取阿里云短信配置，提交签名后的验证码消息请求并处理业务响应。
     * @param telephone 接收短信的手机号
     * @param code 写入模板变量 code 的验证码
     * @param signName 已配置的短信发送签名
     * @param templateCode 验证码对应的短信模板编码
     * @return
     * 返回值: void
     * 返回值说明: 将结果写到控制台，返回类型为 void
     * @throws Exception 配置或签名错误、网络调用失败、响应解析失败或遇到小时级限流时向上传递
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 组装签名前的参数，包含本次请求的时间、随机标识和验证码模板参数。
     * @param accessKeyId 短信接口的访问账号标识
     * @param telephone 接收短信的手机号
     * @param code 写入模板变量的验证码
     * @param signName 已配置的短信签名
     * @param templateCode 待使用的模板编码
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 按键排序且尚未包含签名的请求参数
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将规范化后的参数交给阿里云签名流程，生成可附加到请求的签名值。
     * @param accessKeySecret 阿里云请求签名密钥，禁止记录或展示
     * @param params 已按名称排序的不含签名的请求参数
     * @return
     * 返回值: String
     * 返回值说明: Base64 编码的请求签名
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按参数映射的迭代顺序编码并拼接查询项，调用方提供的有序映射用于保持签名和请求参数顺序一致。
     * @param params 待拼接的请求参数，按调用方决定的顺序遍历
     * @return
     * 返回值: String
     * 返回值说明: 不含前缀问号的查询字符串
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按阿里云查询签名约定编码字符串，修正表单编码对空格、星号和波浪号的处理。
     * @param value 待编码的参数名或参数值
     * @return
     * 返回值: String
     * 返回值说明: 用于签名和查询的参数文本，null 按空字符串编码
     */
    private static String percentEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解读供应商响应，区分成功、小时级限流和其他结果。
     * @param responseBody 阿里云短信接口提供的 JSON 响应文本
     * @return
     * 返回值: void
     * 返回值说明: 输出对应提示，普通失败或空响应处理后直接返回
     * @throws Exception 小时级限流或 JSON 解析异常时向上传递
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
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按字段名读取已解析的短信响应。
     * @param body 已解析的短信响应，可为空
     * @param key 响应字段名
     * @return
     * 返回值: String
     * 返回值说明: 字段值的字符串表示，响应对象或字段缺失时返回 null
     */
    private static String getString(JSONObject body, String key) {
        if (body == null || !body.containsKey(key)) {
            return null;
        }
        return body.getString(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用类内示例签名和模板提交一条验证码短信。
     * @param telephone 测试短信接收号码
     * @param code 测试模板中使用的验证码
     * @return
     * 返回值: void
     * 返回值说明: 调用配置指向的阿里云短信服务
     * @throws Exception 请求或响应处理失败时向上传递
     */
    public static void smsToTest(String telephone, String code) throws Exception {
        String templateCode = "SMS_123456789";
        String signName = "测试模板";
        sendSms(telephone, code, signName, templateCode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 执行本地短信演示流程，运行时可能向供应商提交发送请求。
     * @param args 命令行参数，演示流程不读取
     * @return
     * 返回值: void
     * 返回值说明: 打印发送提示或异常
     */
    public static void main(String[] args) {
        try {
            AlySmsUtils.smsToTest("17004754322", "123456");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
