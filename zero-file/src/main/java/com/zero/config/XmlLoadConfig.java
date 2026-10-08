package com.zero.config;

import com.zero.utils.xml.config.XmlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * XML 配置加载器。
 */
@Component
public class XmlLoadConfig implements ApplicationListener<ApplicationReadyEvent>
{
    private static final Logger log = LoggerFactory.getLogger(XmlLoadConfig.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        log.info("文件服务初始化完成，开始加载 [configuration.xml] 配置文件");
        XmlConfig.loadConfig();
    }
}
