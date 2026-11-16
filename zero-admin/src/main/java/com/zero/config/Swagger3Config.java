package com.zero.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 配置接口文档信息及JWT Bearer调试授权，不改变业务接口的安全校验
 */

@Configuration
public class Swagger3Config {
    /** Swagger授权方案标识，Authorize输入原始JWT，由客户端添加Bearer前缀。 */
    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 声明文档信息和Bearer授权入口，Swagger调试需独立授权，不自动继承后台Axios请求头
     * @return
     * 返回值: OpenAPI
     * 返回值说明：包含JWT安全方案的OpenAPI定义，不保存实际Token或登录凭据
     */

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BEARER_AUTH_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("输入当前登录Token，不包含Bearer前缀。后台页面登录不会自动授权Swagger。")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .info(new Info().title("Home API")
                        .description("Swagger3 Spring Boot 3.0 application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("home-ui")
                        .url("https://home-ui.com"));

    }
}
