package com.zero.utils.sms;

import com.zero.utils.common.StringHelper;
import com.zero.utils.xml.config.XmlConfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:45
 * 说明: PaaSoo国际短信配置读取工具，从XML配置缓存中读取平台鉴权和发件人配置
 */
public class PaasooSmsConfig {

	/**
	 * XML配置层级分隔符
	 */
	private static final String COMMA = ".";

	/**
	 * PaaSoo国际短信配置模块名称
	 */
	private static final String PAASOOSMSCONFIG = "paaSooSmsConfig";
	
	/**
	 * 访问密钥标识配置项名称
	 */
	private static final String KEY = "key";
	
	/**
	 * 访问密钥配置项名称
	 */
	private static final String SECRET = "secret";
	
	/**
	 * 短信发件人配置项名称，部分国家支持自定义短信发件人，平台接口也要求必须提供
	 */
	private static final String FROM = "from";
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 21:45
	 * 说明: 获取PaaSoo访问密钥标识
	 * @return
	 * 返回值: String
	 * 返回值说明: 访问密钥标识，配置为空时返回null
	 */
	public static String getKey(){
		String key = XmlConfig.getString(PAASOOSMSCONFIG + COMMA + KEY);
		if(StringHelper.isNotEmpty(key)) {
			return key;
		}
		System.out.println("无法获取key配置");
		return null;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 21:45
	 * 说明: 获取PaaSoo访问密钥
	 * @return
	 * 返回值: String
	 * 返回值说明: 访问密钥，配置为空时返回null
	 */
	public static String getSecret(){
		String secret = XmlConfig.getString(PAASOOSMSCONFIG + COMMA + SECRET);
		if(StringHelper.isNotEmpty(secret)) {
			return secret;
		}
		System.out.println("无法获取secret配置");
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 21:45
	 * 说明: 获取短信发件人配置
	 * @return
	 * 返回值: String
	 * 返回值说明: 短信发件人，配置为空时返回null
	 */
	public static String getFrom(){
		String from = XmlConfig.getString(PAASOOSMSCONFIG + COMMA + FROM);
		if(StringHelper.isNotEmpty(from)) {
			return from;
		}
		System.out.println("无法获取from配置");
		return null;
	}
	
}
