package com.zero.utils.sms;

import com.zero.utils.common.StringHelper;
import com.zero.utils.http.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读取短信宝账号配置、组装请求，并将供应商的数字状态码转换为发送结果。
 */
public class SmsbaoUtil {

    private static final Logger logger = LoggerFactory.getLogger(SmsbaoUtil.class);

    /**
     * 短信宝发送接口地址。
     */
    public static final String BASE_URL = "https://api.smsbao.com/sms";

    /**
     * 历史常量名兼容入口，旧代码使用baseUrl时仍可正常编译。
     */
    @Deprecated
    public static final String baseUrl = BASE_URL;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验号码、正文和账号配置后提交短信宝请求。
     * @param phoneNum 接收短信的手机号
     * @param content 要发送的短信正文
     * @return
     * 返回值: boolean
     * 返回值说明: 响应去除首尾空白后解析为 0 时返回 true，参数、配置或响应不符合要求时返回 false
     */
    public static boolean sendSms(String phoneNum, String content) {
        if (StringHelper.isEmpty(phoneNum) || StringHelper.isEmpty(content)) {
            return false;
        }

        String userName = SmsbaoConfig.getUserName();
        String apiKey = SmsbaoConfig.getApiKey();
        if (StringHelper.isEmpty(userName) || StringHelper.isEmpty(apiKey)) {
            return false;
        }

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("u", userName);
        paramMap.put("p", apiKey);
        paramMap.put("m", phoneNum);
        paramMap.put("c", URLEncoder.encode(content, StandardCharsets.UTF_8));

        String result = HttpClientHelper.sendHttpGet(BASE_URL, paramMap);
        logger.info("smsbao send sms finished, result={}", result);
        if (StringHelper.isEmpty(result)) {
            return false;
        }

        try {
            return Integer.parseInt(result.trim()) == 0;
        } catch (NumberFormatException e) {
            logger.warn("smsbao send sms returned invalid code");
            return false;
        }
    }
}
