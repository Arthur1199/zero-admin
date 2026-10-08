package com.zero.enums;

import com.zero.utils.common.StringHelper;
import com.zero.utils.xml.config.XmlConfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:45
 * 说明: 多语言枚举，提供系统默认语言读取能力
 */
public enum LanguageEnum {
	
	CN("cn","中文"),
	EN("en","English"),
	TH("th","ภาษาไทย"),
	MY("my","မြန်မာ"),
	
	;
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 21:45
	 * 说明: 从XML配置中读取系统默认语言，配置缺失或无法匹配时默认返回中文
	 * @return
	 * 返回值: LanguageEnum
	 * 返回值说明: 系统默认语言枚举
	 */
	public static LanguageEnum getDefaultLanguage() {
		String languageCode = XmlConfig.getString("serverConfig.defaultLanguage");
		if(StringHelper.isNotEmpty(languageCode)) {
			LanguageEnum[] values = LanguageEnum.values();
			for(LanguageEnum item : values) {
				if(languageCode.equals(item.getLanguage())) {
					return item;
				}
			}
		}
		return LanguageEnum.CN;
	}
	
	private String language;
	private String desc;
	
	private LanguageEnum(String language, String desc) {
		this.language = language;
		this.desc = desc;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
