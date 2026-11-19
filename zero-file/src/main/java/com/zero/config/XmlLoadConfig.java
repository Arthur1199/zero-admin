package com.zero.config;

import lombok.extern.slf4j.Slf4j;
import com.zero.utils.xml.config.XmlConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: XML 配置加载器。
 */
@Slf4j
@Component
public class XmlLoadConfig implements ApplicationListener<ApplicationReadyEvent>
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 文件服务就绪后初始化 XML 配置。
     * @param event Spring 应用就绪事件
     * @return
     * 返回值: void
     * 返回值说明: 加载 configuration.xml 供后续业务读取
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        log.info("文件服务初始化完成，开始加载 [configuration.xml] 配置文件");
        XmlConfig.loadConfig();
    }
}
