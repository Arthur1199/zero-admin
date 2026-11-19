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

    // 实例对象
    private static SlackRobotUtil slackRobotUtil;

    // 初始化配置
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 初始化 Slack 通知所需的请求地址、频道和认证信息。
     * @param requestApi Slack 请求地址
     * @param channel 接收消息的频道
     * @param authorization Slack 请求使用的授权信息
     * @return
     * 返回值: void
     * 返回值说明: 后续通知从已初始化的实例读取发送配置
     */
    public static void initConfig(String requestApi, String channel, String authorization) {
        if (slackRobotUtil == null) {
            slackRobotUtil = new SlackRobotUtil();
            slackRobotUtil.setRequestApi(requestApi);
            slackRobotUtil.setChannel(channel);
            slackRobotUtil.setAuthorization(authorization);
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
     * 时间: 2026/11/20 01:36
     * 说明: 等待发送配置初始化后，向 Slack 发送指定文本。
     * @param text 发送给已配置频道的通知文本
     * @return
     * 返回值: void
     * 返回值说明: 发送异常记录日志，等待配置时被中断则抛出异常
     */
    public static void sendMsg(String text) {
        while(slackRobotUtil == null){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.error("slackRobotUtil数据初始化成功：{}",slackRobotUtil.hashCode());
        // 创建 HttpClient 实例
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(slackRobotUtil.getRequestApi());

        try {
            // 设置请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("channel", slackRobotUtil.getChannel());
            paramJson.put("text", text);

            // 设置 JSON 请求体
            StringEntity entity = new StringEntity(paramJson.toString(), "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);

            // 设置请求头
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Authorization", "Bearer " + slackRobotUtil.getAuthorization());

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
