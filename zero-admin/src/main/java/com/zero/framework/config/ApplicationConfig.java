package com.zero.framework.config;

import com.zero.packageconfig.PackageProperties;
import java.util.TimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
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
     * 时间: 2026/11/23 21:10
     * 说明: 保留管理后台 JSON 在迁移前使用的 JVM 默认时区。
     * @return
     * 返回值: JsonMapperBuilderCustomizer
     * 返回值说明: 为 MVC、代理和 WebSocket 共用的 Jackson 3 配置时区。
     */
    @Bean
    public JsonMapperBuilderCustomizer jacksonObjectMapperCustomization()
    {
        return builder -> builder.defaultTimeZone(TimeZone.getDefault());
    }
}
