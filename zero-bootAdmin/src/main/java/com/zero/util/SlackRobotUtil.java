package com.zero.util;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2025/5/22 17:13
 * 说明: Slac机器人消息工具类
 */
@Slf4j
public class SlackRobotUtil {

    //http请求地址
    private String requestApi;

    // 房间号
    private String channel;

    // 授权令牌
    private String authorization;

    // 私有构造器
    private SlackRobotUtil() {};

    /** 仅发布完整的机器人配置，供通知线程安全读取。 */
    private static volatile SlackRobotUtil slackRobotUtil;

    // 初始化配置
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:36
     * 说明: 初始化 Slack 通知所需的请求地址、频道和认证信息。
     * @param requestApi Slack 请求地址
     * @param channel 接收消息的频道
     * @param authorization Slack 请求使用的授权信息
     * @return
     * 返回值: void
     * 返回值说明: 配置齐全后一次性发布，后续通知读取同一份配置。
     */
    public static synchronized void initConfig(String requestApi, String channel, String authorization) {
        if (slackRobotUtil == null && requestApi != null && !requestApi.isBlank()
                && channel != null && !channel.isBlank() && authorization != null && !authorization.isBlank()) {
            SlackRobotUtil config = new SlackRobotUtil();
            config.setRequestApi(requestApi);
            config.setChannel(channel);
            config.setAuthorization(authorization);
            slackRobotUtil = config;
        }
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getRequestApi() {
        return requestApi;
    }

    public void setRequestApi(String requestApi) {
        this.requestApi = requestApi;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:36
     * 说明: 配置可用时向 Slack 发送文本。启动期间或未配置的环境跳过通知。
     * @param text 发送给已配置频道的通知文本
     * @return
     * 返回值: void
     * 返回值说明: 缺少配置时直接返回，发送异常记录日志。
     */
    public static void sendMsg(String text) {
        SlackRobotUtil config = slackRobotUtil;
        if (config == null || text == null || text.isBlank()) {
            return;
        }
        // 创建 HttpClient 实例
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(config.getRequestApi());

        try {
            // 设置请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("channel", config.getChannel());
            paramJson.put("text", text);

            // 设置 JSON 请求体
            StringEntity entity = new StringEntity(paramJson.toString(), "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);

            // 设置请求头
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Authorization", "Bearer " + config.getAuthorization());

            // 执行请求
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                // 读取响应内容
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity, "UTF-8");
                log.info("发送消息成功，响应内容：{}", responseBody);
                EntityUtils.consume(responseEntity); // 当前仅在 HTTP 200 分支显式消费响应实体
            } else {
                log.error("HTTP 请求失败，状态码：{}", statusCode);
            }
        } catch (Exception e) {
            log.error("发送消息失败，系统异常：", e);
        } finally {
        }
    }
}
