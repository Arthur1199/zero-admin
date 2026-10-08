package com.zero.config;

import com.zero.dto.ReportAdminContext;
import com.zero.constant.ReportHeaderConstants;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 报表服务 OpenFeign 请求头透传配置，支持 report 继续调用其它 Java 微服务
 */
@Configuration
public class FeignRequestInterceptorConfig
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 创建 Feign 请求拦截器，透传登录令牌、链路标识和后台用户上下文，避免 report 继续调用其它服务时丢失操作人信息
     * @return
     * 返回值: RequestInterceptor
     * 返回值说明: OpenFeign 发起请求前执行的统一请求头处理器
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor()
    {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes))
            {
                return;
            }
            ReportAdminContext.fromRequest(attributes.getRequest())
                    .toForwardHeaders(ReportHeaderConstants.REPORT_SOURCE_SERVICE)
                    .forEach(template::header);
        };
    }
}
