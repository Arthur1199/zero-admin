package com.zero.controller;

import com.zero.model.AjaxResult;
import com.zero.feign.admin.RemoteAdminHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 报表服务内部健康检查接口，承载 admin 调 report 和 report 调 admin 的最小可验证跨服务链路
 */
@Slf4j
@RestController
@RequestMapping("/internal/report/health")
public class ReportHealthController
{

    /**
     * 当前服务环境配置，用于读取服务名和端口，不直接硬编码运行信息
     */
    private final Environment environment;

    /**
     * 后台管理服务健康检查 Feign 客户端
     */
    private final RemoteAdminHealthService remoteAdminHealthService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 构造报表健康检查控制器，通过构造注入固定运行期依赖
     * @param environment 当前 Spring 环境，用于读取服务名和端口等非敏感运行信息
     * @param remoteAdminHealthService admin 服务 Feign 客户端，用于验证 report 主动调用其它 Java 服务
     */
    public ReportHealthController(Environment environment, RemoteAdminHealthService remoteAdminHealthService)
    {
        this.environment = environment;
        this.remoteAdminHealthService = remoteAdminHealthService;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 返回 report 服务基础状态，供 admin 通过 Feign 验证服务注册发现和 HTTP 编解码链路
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data 包含服务名、端口和响应时间，msg 只表示操作结果不承载业务数据
     */
    @GetMapping("/ping")
    public AjaxResult ping()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serviceName", environment.getProperty("spring.application.name", "zero-report"));
        data.put("serverPort", environment.getProperty("server.port", "9101"));
        data.put("status", "UP");
        data.put("time", LocalDateTime.now().toString());
        return AjaxResult.success("操作成功", data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 由 report 主动调用 admin 健康端点，验证 report 的 OpenFeign 客户端、负载均衡和服务发现能力
     * @return
     * 返回值: AjaxResult
     * 返回值说明: admin 可访问时 data 返回其健康检查响应；调用失败时返回统一错误提示
     */
    @GetMapping("/admin")
    public AjaxResult adminHealth()
    {
        try
        {
            return AjaxResult.success("操作成功", remoteAdminHealthService.health());
        }
        catch (Exception e)
        {
            log.error("调用后台管理服务健康检查失败，请确认 zero-admin 已启动并注册到 Nacos", e);
            return AjaxResult.error("后台管理服务暂不可用，请确认服务已启动并注册到注册中心");
        }
    }
}
