package com.zero.config;

import lombok.extern.slf4j.Slf4j;
import com.zero.utils.xml.config.XmlConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * XML 配置加载器。
 */
@Slf4j
@Component
public class XmlLoadConfig implements ApplicationListener<ApplicationReadyEvent>
{
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        log.info("文件服务初始化完成，开始加载 [configuration.xml] 配置文件");
        XmlConfig.loadConfig();
    }
}
