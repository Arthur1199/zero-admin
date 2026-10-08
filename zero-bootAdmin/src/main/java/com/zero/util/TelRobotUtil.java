package com.zero.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 01:46
 * 说明: Telegram机器人消息发送工具，机器人凭据必须由配置或环境变量注入
 */
public class TelRobotUtil {

	private static final Log log = LogFactory.getLog(TelRobotUtil.class);

	private static final String ROBOT_API = "https://api.telegram.org/bot";

	private static String ROBOT_TOKEN = "";

	private static String ROBOT_CHAT_ID = "";

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 发送消息到Telegram机器人，未配置凭据时直接返回错误，避免请求异常和敏感信息落日志
	 * @param text 消息内容，不允许为空
	 * @return
	 * 返回值: String
	 * 返回值说明: Telegram接口响应内容，配置缺失或发送失败时返回错误说明
	 */
	public static String sendMessage(String text) {
		if (StringHelper.isEmpty(text)) {
			log.error("发送内容为空");
			return "ERROR: 发送内容不能为空";
		}
		if (StringHelper.isEmpty(ROBOT_TOKEN) || StringHelper.isEmpty(ROBOT_CHAT_ID)) {
			log.error("Telegram机器人配置未初始化");
			return "ERROR: Telegram机器人配置未初始化";
		}

		HttpClient client = HttpClients.createDefault();
		try {
			String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8)
					.replace("+", "%20")
					.replace("*", "%2A")
					.replace("%7E", "~");
			String url = ROBOT_API + ROBOT_TOKEN + "/sendMessage"
					+ "?chat_id=" + ROBOT_CHAT_ID
					+ "&text=" + encodedText
					+ "&parse_mode=HTML";

			HttpGet getMethod = new HttpGet(url);
			getMethod.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

			HttpResponse response = client.execute(getMethod);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String result = entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
			if (entity != null) {
				EntityUtils.consume(entity);
			}
			if (statusCode >= 200 && statusCode < 300) {
				log.info("Telegram消息发送成功");
				return result;
			}
			String error = "HTTP请求失败，状态码：" + statusCode;
			log.error(error);
			return error;
		} catch (IOException e) {
			log.error("发送消息时发生IO异常", e);
			return "ERROR: " + e.getMessage();
		} catch (Exception e) {
			log.error("发送消息时发生未知异常", e);
			return "ERROR: " + e.getMessage();
		}
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取当前Telegram机器人令牌
	 * @return
	 * 返回值: String
	 * 返回值说明: 机器人令牌，调用方不得写入日志
	 */
	public static String getROBOT_TOKEN() {
		return ROBOT_TOKEN;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 设置Telegram机器人令牌
	 * @param robotToken 机器人令牌，来自环境变量或外部安全配置
	 * @return
	 * 返回值: void
	 * 返回值说明: 更新内存中的机器人令牌
	 */
	public static void setROBOT_TOKEN(String robotToken) {
		ROBOT_TOKEN = robotToken;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 获取当前Telegram聊天ID
	 * @return
	 * 返回值: String
	 * 返回值说明: 聊天ID，调用方不得写入日志
	 */
	public static String getROBOT_CHAT_ID() {
		return ROBOT_CHAT_ID;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:46
	 * 说明: 设置Telegram聊天ID
	 * @param robotChatId 聊天ID，来自环境变量或外部安全配置
	 * @return
	 * 返回值: void
	 * 返回值说明: 更新内存中的聊天ID
	 */
	public static void setROBOT_CHAT_ID(String robotChatId) {
		ROBOT_CHAT_ID = robotChatId;
	}
}
