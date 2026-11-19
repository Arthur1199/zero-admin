package com.zero.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 装配 Boot Admin 使用的 Nacos 服务发现观察器。
 */
@Configuration
public class NacosWatchAutoConfiguration {
  /**
   *
   * 作者: Arthur 亚瑟
   * 时间: 2026/11/20 01:36
   * 说明: 在观察器开关开启且无现有 Bean 时，创建项目使用的 Nacos 观察器。
   * @param nacosDiscoveryProperties Nacos 服务发现配置
   * @return
   * 返回值: NacosWatch
   * 返回值说明: 用于轮询 Nacos 服务清单的观察器
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.watch.enabled", matchIfMissing = true)
  public NacosWatch nacosWatchDeepBlue(NacosDiscoveryProperties nacosDiscoveryProperties) {
    return new NacosWatch(nacosDiscoveryProperties);
  }

}