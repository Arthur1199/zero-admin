package com.zero.feign.admin;

import com.zero.constant.ServiceNameConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 后台管理服务健康检查远程调用接口，用于验证 report 主动调用其它 Java 微服务的能力
 */
@FeignClient(contextId = "remoteAdminHealthService", name = ServiceNameConstant.ZERO_ADMIN)
public interface RemoteAdminHealthService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 调用 admin 已开放的 Actuator 健康检查端点，避免为了探活额外暴露匿名业务接口
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: admin Actuator 健康检查响应，通常包含 status 等基础状态字段
     */
    @GetMapping("/actuator/health")
    Map<String, Object> health();
}
