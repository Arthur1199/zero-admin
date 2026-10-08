package com.zero.feign;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:25
 * 说明: 报表服务通用 RestTemplate 配置，支持 admin 按服务名动态调用 zero-report 并统一透传管理员上下文
 */
@Configuration
public class ReportRestTemplateConfig
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 创建专用于调用报表服务的负载均衡 RestTemplate，避免影响集群探活等其它手动 RestTemplate
     * @param adminForwardHeaderProvider 后台跨服务请求头构建器
     * @return
     * 返回值: RestTemplate
     * 返回值说明: 支持通过 http://zero-report 访问注册中心实例的 RestTemplate
     */
    @Bean("reportRestTemplate")
    @LoadBalanced
    public RestTemplate reportRestTemplate(AdminForwardHeaderProvider adminForwardHeaderProvider)
    {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(reportHeaderInterceptor(adminForwardHeaderProvider));
        return restTemplate;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:25
     * 说明: 创建 RestTemplate 请求拦截器，将当前后台管理员上下文写入调用 report 的请求头
     * @param adminForwardHeaderProvider 后台跨服务请求头构建器
     * @return
     * 返回值: ClientHttpRequestInterceptor
     * 返回值说明: RestTemplate 执行请求前追加上下文请求头的拦截器
     */
    @Bean("reportHeaderInterceptor")
    public ClientHttpRequestInterceptor reportHeaderInterceptor(
            @Qualifier("adminForwardHeaderProvider") AdminForwardHeaderProvider adminForwardHeaderProvider)
    {
        return (request, body, execution) -> {
            adminForwardHeaderProvider.createForwardContext().toHeaders()
                    .forEach((name, value) -> request.getHeaders().set(name, value));
            return execution.execute(request, body);
        };
    }
}
