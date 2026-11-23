package com.zero.config;

import com.zero.config.RedissonAutoConfigCustomizer;
import com.zero.util.RedissonUtil;
import org.redisson.spring.starter.RedissonAutoConfigurationV4;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:08
 * 说明: 显式配置 Redis 主机后装配任务服务的 Redisson 客户端、工具和 Spring Redis 模板。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.data.redis", name = "host")
@ConditionalOnProperty(prefix = "zero.task.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({RedissonAutoConfigurationV4.class, DataRedisAutoConfiguration.class,
        RedissonAutoConfigCustomizer.class, RedissonUtil.class})
public class TaskRedisConfig {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 16:14
     * 说明: 拒绝空主机配置，避免误用 Spring Boot 的默认 Redis 地址。
     * @param environment 当前环境配置，包含 Nacos 导入的属性
     * @return
     * 返回值: BeanFactoryPostProcessor
     * 返回值说明: 创建连接工厂前校验 Redis 目标地址
     */
    @Bean
    static BeanFactoryPostProcessor validateTaskRedis(Environment environment) {
        return beanFactory -> {
            // 在实例化连接工厂前校验目标，避免启用 Redis 后静默连接默认地址。
            if (!StringUtils.hasText(environment.getProperty("spring.data.redis.host"))) {
                throw new IllegalStateException("启用 zero-task Redis 后必须配置 spring.data.redis.host");
            }
        };
    }
}
