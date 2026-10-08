package com.zero.framework.config;

import java.util.Map;

import com.zero.feign.AdminForwardHeaderProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: OpenFeign 请求头透传配置，保证 admin 调用其它 Java 服务时能延续登录态和链路标识
 */
@Configuration
public class FeignRequestInterceptorConfig
{
    /**
     * 后台跨服务请求头构建器
     */
    private final AdminForwardHeaderProvider adminForwardHeaderProvider;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 构造 Feign 请求头透传配置，通过构造注入复用统一请求头构建逻辑
     * @param adminForwardHeaderProvider 后台跨服务请求头构建器
     */
    public FeignRequestInterceptorConfig(AdminForwardHeaderProvider adminForwardHeaderProvider)
    {
        this.adminForwardHeaderProvider = adminForwardHeaderProvider;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 创建 Feign 请求拦截器，统一透传登录令牌、链路标识和管理员上下文
     * @return
     * 返回值: RequestInterceptor
     * 返回值说明: OpenFeign 发起请求前执行的统一请求头处理器
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor()
    {
        return template -> {
            Map<String, String> headers = adminForwardHeaderProvider.createForwardContext().toHeaders();
            headers.forEach(template::header);
        };
    }
}
