package com.zero.config;

import com.zero.utils.xml.config.XmlConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/28 20:31
 * 说明: zero-ai 启动后加载 resources/configuration.xml，兼容既有 category.item 格式配置。
 */
@Component
@Slf4j
public class XmlLoadConfig implements ApplicationListener<ApplicationReadyEvent>
{

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 01:27
     * 说明: 应用启动完成后加载XML配置；配置文件可能包含机器人令牌等敏感值，加载过程只记录状态不输出配置内容
     * @param event 应用启动完成事件
     * @return
     * 返回值: void
     * 返回值说明: 加载resources/configuration.xml到XmlConfig缓存
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        log.info("zero-ai 初始化完成，开始加载 [configuration.xml] 配置文件");
        XmlConfig.loadConfig();
    }
}
