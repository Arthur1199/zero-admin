package com.zero.feign;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 装配访问文件服务的负载均衡客户端及管理员上下文透传拦截器。
 */
@Configuration
public class FileRestTemplateConfig
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:10
     * 说明: 创建按服务名解析文件服务地址的 RestTemplate，并配置管理员请求上下文拦截。
     * @param adminForwardHeaderProvider 管理员上下文请求头提供方
     * @param builder 注入统一的 Jackson 3 和文件流消息转换配置
     * @return
     * 返回值: RestTemplate
     * 返回值说明: 文件服务专用的负载均衡客户端
     */
    @Bean("fileRestTemplate")
    @LoadBalanced
    public RestTemplate fileRestTemplate(AdminForwardHeaderProvider adminForwardHeaderProvider, RestTemplateBuilder builder)
    {
        RestTemplate restTemplate = builder.build();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            adminForwardHeaderProvider.createForwardContext().toHeaders()
                    .forEach((name, value) -> request.getHeaders().set(name, value));
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
