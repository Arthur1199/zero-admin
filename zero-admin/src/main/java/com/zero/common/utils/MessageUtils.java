package com.zero.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import com.zero.common.utils.spring.SpringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 获取i18n资源文件
 */
public class MessageUtils
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据消息键和参数 获取消息 委托给spring messageSource
     * @param code 消息键
     * @param args 参数
     * @return
     * 返回值: 获取国际化翻译值
     * 返回值说明：根据消息键和参数 获取消息 委托给spring messageSource
     */
    public static String message(String code, Object... args)
    {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
