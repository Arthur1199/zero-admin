package com.zero.utils.sms;

import com.zero.utils.common.StringHelper;
import com.zero.utils.xml.config.XmlConfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 从 XML 配置读取阿里云短信凭据及注册、找回密码场景使用的签名和模板。
 */
public class AlySmsConfig {

	//逗号标识
	private static final String COMMA = ".";
	/**
	 * 配置模块名称
	 */
	private static final String ALYSMSCONFIG = "alySmsConfig";
	//访问密钥标识
	private static final String ACCESSKEYID = "accessKeyId";
	//用户的密钥
	private static final String ACCESSKEYSECRET = "accessKeySecret";
	
	//------------------------------注册短信相关------------------------------
	//用户注册的短信签名名称
	private static final String REGISTSIGNNAME = "registSignName";
	//用户注册的短信模板
	private static final String REGISTTEMPLATECODE = "registTemplateCode";
	
	//------------------------------忘记登录密码短信相关------------------------------
	//忘记密码重置登录密码的短信签名名称
	private static final String FORGETPWDSIGNNAME = "forgetPwdSignName";
	//忘记密码重置登录密码的短信模板
	private static final String FORGETPWDTEMPLATECODE = "forgetPwdTemplateCode";
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 读取阿里云短信请求使用的账号标识。
	 * @return
	 * 返回值: String
	 * 返回值说明: 配置中的 AccessKey ID，缺失时返回 null
	 */
	public static final String getAccessKeyId() {
		String accessKeyId = XmlConfig.getString(ALYSMSCONFIG + COMMA + ACCESSKEYID);
		if(StringHelper.isNotEmpty(accessKeyId)) {
			return accessKeyId;
		}
		System.out.println("无法获取accessKeyId配置");
		return null;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 读取阿里云短信签名使用的凭据。
	 * @return
	 * 返回值: String
	 * 返回值说明: 配置中的 AccessKey Secret，缺失时返回 null，禁止输出凭据
	 */
	public static final String getAccessKeySecret() {
		String accessKeySecret = XmlConfig.getString(ALYSMSCONFIG + COMMA + ACCESSKEYSECRET);
		if(StringHelper.isNotEmpty(accessKeySecret)) {
			return accessKeySecret;
		}
		System.out.println("无法获取accessKeySecre配置");
		return null;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查找注册验证码短信的发送签名。
	 * @return
	 * 返回值: String
	 * 返回值说明: 注册场景的短信签名，未配置时返回 null
	 */
	public static final String getRegistSignName() {
		String registSignName = XmlConfig.getString(ALYSMSCONFIG + COMMA + REGISTSIGNNAME);
		if(StringHelper.isNotEmpty(registSignName)) {
			return registSignName;
		}
		System.out.println("无法获取registSignName配置");
		return null;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查找注册验证码短信使用的模板。
	 * @return
	 * 返回值: String
	 * 返回值说明: 注册场景的模板编码，未配置时返回 null
	 */
	public static final String getRegistTemplateCode() {
		String registTemplateCode = XmlConfig.getString(ALYSMSCONFIG + COMMA + REGISTTEMPLATECODE);
		if(StringHelper.isNotEmpty(registTemplateCode)) {
			return registTemplateCode;
		}
		System.out.println("无法获取registTemplateCode配置");
		return null;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查找找回密码短信的发送签名。
	 * @return
	 * 返回值: String
	 * 返回值说明: 找回密码场景的短信签名，未配置时返回 null
	 */
	public static final String getForgetPwdSignName() {
		String forgetPwdSignName = XmlConfig.getString(ALYSMSCONFIG + COMMA + FORGETPWDSIGNNAME);
		if(StringHelper.isNotEmpty(forgetPwdSignName)) {
			return forgetPwdSignName;
		}
		System.out.println("无法获取forgetPwdSignName配置");
		return null;
	}
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查找找回密码短信使用的模板。
	 * @return
	 * 返回值: String
	 * 返回值说明: 找回密码场景的模板编码，未配置时返回 null
	 */
	public static final String getForgetPwdTemplateCode() {
		String forgetPwdTemplateCode = XmlConfig.getString(ALYSMSCONFIG + COMMA + FORGETPWDTEMPLATECODE);
		if(StringHelper.isNotEmpty(forgetPwdTemplateCode)) {
			return forgetPwdTemplateCode;
		}
		System.out.println("无法获取forgetPwdTemplateCode配置");
		return null;
	}
	
}
