package com.zero.feign;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * zero-file RestTemplate config.
 */
@Configuration
public class FileRestTemplateConfig
{
    @Bean("fileRestTemplate")
    @LoadBalanced
    public RestTemplate fileRestTemplate(AdminForwardHeaderProvider adminForwardHeaderProvider)
    {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            adminForwardHeaderProvider.createForwardContext().toHeaders()
                    .forEach((name, value) -> request.getHeaders().set(name, value));
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
