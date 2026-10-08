package com.zero.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

@Configuration
public class NacosWatchAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.watch.enabled", matchIfMissing = true)
  public NacosWatch nacosWatchDeepBlue(NacosDiscoveryProperties nacosDiscoveryProperties) {
    return new NacosWatch(nacosDiscoveryProperties);
  }

}