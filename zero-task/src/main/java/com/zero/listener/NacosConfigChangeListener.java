package com.zero.listener;

import com.alibaba.cloud.nacos.refresh.NacosConfigRefreshEvent;
import com.zero.ZeroTaskApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 22:43
 * 说明: 监听任务执行服务已导入且开启刷新的 Nacos 配置，收到变更事件后请求重启。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NacosConfigChangeListener
{
    /** 仅允许当前应用上下文触发重启。 */
    private final ConfigurableApplicationContext applicationContext;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:43
     * 说明: 复用 Spring Cloud Alibaba 的配置变更事件，监听范围随服务的 Nacos 导入配置变化。
     * @param event 携带发生变更的 Data ID 和配置分组
     * @return
     * 返回值: void
     * 返回值说明: 请求重启当前应用上下文，重启请求被接受时记录变更的配置分组和 Data ID
     */
    @EventListener
    public void onConfigChanged(NacosConfigRefreshEvent event)
    {
        if (ZeroTaskApplication.restart(applicationContext))
        {
            log.info("检测到Nacos配置变更，开始重启应用服务，group={}，dataId={}", event.getGroup(), event.getDataId());
        }
    }
}
