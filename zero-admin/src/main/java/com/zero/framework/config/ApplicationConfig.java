package com.zero.framework.config;

import com.zero.packageconfig.PackageProperties;
import java.util.TimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 装配管理后台的 Spring AOP、MyBatis Mapper 扫描以及 JSON 序列化配置。
 */
@Configuration
// 暴露当前 AOP 代理，供使用 AopContext 的内部调用继续经过切面。
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(PackageProperties.MAPPER_SCAN)
public class ApplicationConfig
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 时区配置
     * @return
     * 返回值: Jackson2ObjectMapperBuilderCustomizer
     * 返回值说明：时区配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization()
    {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }
}
