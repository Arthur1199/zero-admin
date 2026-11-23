package com.zero.config;

import com.zero.common.core.domain.AjaxResult;
import com.zero.framework.config.SecurityConfig;
import com.zero.framework.config.properties.PermitAllUrlProperties;
import com.zero.framework.security.filter.JwtAuthenticationTokenFilter;
import com.zero.framework.web.service.SysPermissionService;
import com.zero.framework.security.handle.AuthenticationEntryPointImpl;
import com.zero.framework.security.handle.LogoutSuccessHandlerImpl;
import com.zero.framework.web.exception.GlobalExceptionHandler;
import com.zero.framework.web.service.TokenService;
import jakarta.servlet.Filter;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 14:07
 * 说明: 隔离验证SpringDoc与实际全局异常处理器及安全链兼容，不加载外部服务配置
 */
class SpringDocCompatibilityTest
{
    /** 仅装配Web、文档和安全组件，令牌等外部依赖使用空行为替身。 */
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class,
            JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
            WebMvcAutoConfiguration.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
            SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class, SwaggerConfig.class))
        .withUserConfiguration(SpringDocConfigProperties.class, SwaggerUiConfigProperties.class,
            SwaggerUiOAuthProperties.class, Swagger3Config.class, GlobalExceptionHandler.class,
            SecurityConfig.class, JwtAuthenticationTokenFilter.class,
            AuthenticationEntryPointImpl.class, ProbeController.class)
        .withBean(CorsFilter.class, () -> new CorsFilter(new UrlBasedCorsConfigurationSource()))
        .withInitializer(context -> {
            // 直接注册替身，避免对替身再次注入令牌配置、Redis及应用扫描生命周期。
            context.getBeanFactory().registerSingleton("tokenService", mock(TokenService.class));
            context.getBeanFactory().registerSingleton("sysPermissionService", mock(SysPermissionService.class));
            context.getBeanFactory().registerSingleton("userDetailsService", mock(UserDetailsService.class));
            context.getBeanFactory().registerSingleton("logoutSuccessHandler", mock(LogoutSuccessHandlerImpl.class));
            PermitAllUrlProperties properties = mock(PermitAllUrlProperties.class);
            when(properties.getUrls()).thenReturn(Collections.emptyList());
            context.getBeanFactory().registerSingleton("permitAllUrl", properties);
        });

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:07
     * 说明: 全局异常处理器存在时仍生成OpenAPI和Bearer授权定义，避免业务500或缺少认证配置
     * @return
     * 返回值: void
     * 返回值说明: 文档包含版本、操作响应及JWT安全方案，不返回业务错误包装或实际Token
     */
    @Test
    void apiDocsShouldGenerateWithGlobalExceptionHandler()
    {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
            mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.paths['/compatibility/probe'].get.responses").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.code").doesNotExist());
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:07
     * 说明: Swagger配置只读入口可匿名读取，业务入口及配置写请求继续要求登录
     * @return
     * 返回值: void
     * 返回值说明: GET配置返回文档地址，未登录业务及POST请求返回既有401业务码
     */
    @Test
    void swaggerConfigShouldBeReadableWithoutOpeningBusinessEndpoints()
    {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
            mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"))
                .andExpect(jsonPath("$.code").doesNotExist());
            mvc.perform(get("/compatibility/probe")).andExpect(jsonPath("$.code").value(401));
            mvc.perform(post("/v3/api-docs/swagger-config")).andExpect(jsonPath("$.code").value(401));
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:07
     * 说明: 仅供隔离测试生成文档，不参与应用扫描或真实业务操作
     */
    @RestController
    static class ProbeController
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 14:07
         * 说明: 提供无副作用的响应类型供SpringDoc解析
         * @return
         * 返回值: AjaxResult
         * 返回值说明: data中包含测试标志，不读取数据库或缓存
         */
        @GetMapping("/compatibility/probe")
        public AjaxResult probe()
        {
            return AjaxResult.success(Map.of("probe", true));
        }
    }
}
