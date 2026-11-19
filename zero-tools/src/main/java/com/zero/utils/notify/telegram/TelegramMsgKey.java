package com.zero.utils.notify.telegram;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 标注需要出现在 Telegram 消息中的字段，并提供其显示名称，供组装器在运行时读取。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TelegramMsgKey {
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 指定消息文本中显示在字段值之前的名称。
	 * @return
	 * 返回值: String
	 * 返回值说明: 通知组装时使用的字段展示名称
	 */
	String keyName();
}
