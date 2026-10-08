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
 * 作者: Jack
 * 时间: 2022年10月17日上午12:06:01
 * 说明: 国内短信宝发送平台工具类，负责组装账号、密钥、手机号和短信内容后调用发送接口
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
     * 作者: Jack
     * 时间: 2022年10月17日上午12:08:22
     * 说明: 发送短信宝短信，接口返回0视为成功，其它响应码视为失败
     * @param phoneNum 接收短信的手机号
     * @param content 短信内容
     * @return
     * 返回值: boolean
     * 返回值说明: true-短信宝返回成功，false-配置缺失、接口异常或返回失败码
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
