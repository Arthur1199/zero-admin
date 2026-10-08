package com.zero.utils.sms;

import com.zero.utils.common.StringHelper;
import com.zero.utils.xml.config.XmlConfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:45
 * 说明: 短信宝服务端配置读取工具，从XML配置缓存中读取用户名和接口密钥
 */
public class SmsbaoConfig {

	/**
	 * XML配置层级分隔符
	 */
	private static final String COMMA = ".";

	/**
	 * 短信宝配置模块名称
	 */
	private static final String SMSBAOCONFIG = "smsBaoConfig";

	/**
	 * 用户名配置项名称
	 */
	private static final String USERNAME = "userName";

	/**
	 * 接口密钥配置项名称
	 */
	private static final String APIKEY = "apiKey";

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 21:45
	 * 说明: 获取短信宝用户名配置
	 * @return
	 * 返回值: String
	 * 返回值说明: 用户名，配置为空时返回null
	 */
	public static String getUserName() {
		String userName = XmlConfig.getString(SMSBAOCONFIG + COMMA + USERNAME);
		if(StringHelper.isNotEmpty(userName)) {
			return userName;
		}
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 21:45
	 * 说明: 获取短信宝接口密钥配置
	 * @return
	 * 返回值: String
	 * 返回值说明: 接口密钥，配置为空时返回null
	 */
	public static String getApiKey() {
		String apiKey = XmlConfig.getString(SMSBAOCONFIG + COMMA + APIKEY);
		if(StringHelper.isNotEmpty(apiKey)) {
			return apiKey;
		}
		return null;
	}

}
