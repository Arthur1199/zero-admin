package com.zero.utils.notify.telegram;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作者: Jack
 * 时间: 2025年3月5日17:18:56
 * 说明: tele机器人发送消息注解
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TelegramMsgKey {
	//需要被转换的key的参数列表下表,下标从0开始
	String keyName();
}
