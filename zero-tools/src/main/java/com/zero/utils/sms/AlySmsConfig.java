package com.zero.utils.sms;

import com.zero.utils.common.StringHelper;
import com.zero.utils.xml.config.XmlConfig;

/**
 * 作者: Jack
 * 时间: 2022年10月25日下午4:25:34
 * 说明: 阿里云配置加载工具
 *
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
	 * 作者:Jack
	 * 日期:2022年10月25日下午4:32:29
	 * @return
	 * 返回值:String
	 * 返回值说明:获取accessKeyId
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
	 * 作者:Jack
	 * 日期:2022年10月25日下午4:37:22
	 * @return
	 * 返回值:String
	 * 返回值说明:accessKeySecre
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
	 * 作者:Jack
	 * 日期:2022年10月25日下午4:33:31
	 * @return
	 * 返回值:String
	 * 返回值说明:获取registSignName
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
	 * 作者:Jack
	 * 日期:2022年10月25日下午4:35:57
	 * @return
	 * 返回值:String
	 * 返回值说明:获取registTemplateCode
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
	 * 作者:Jack
	 * 日期:2022年10月25日下午4:36:27
	 * @return
	 * 返回值:String
	 * 返回值说明:获取forgetPwdSignName
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
	 * 作者:Jack
	 * 日期:2022年10月25日下午4:37:19
	 * @return
	 * 返回值:String
	 * 返回值说明:
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
