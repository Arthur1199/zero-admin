package com.zero.feign;

import com.zero.common.constant.ServiceNameConstants;
import com.zero.common.core.domain.AjaxResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 报表服务健康检查远程调用接口，用于验证 admin 到 report 的服务发现和 Feign 调用链路
 */
@FeignClient(contextId = "remoteReportHealthService", name = ServiceNameConstants.ZERO_REPORT)
public interface RemoteReportHealthService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 调用报表服务内部探活接口，确认 report 服务已注册并能响应 admin 的跨服务请求
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code=200 表示 report 可访问，data 返回 report 服务基础状态
     */
    @GetMapping("/internal/report/health/ping")
    AjaxResult ping();
}
