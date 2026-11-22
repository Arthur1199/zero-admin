package com.zero.web.controller.system;

import lombok.extern.slf4j.Slf4j;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.feign.RemoteReportHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 报表服务调用探活接口，登录后的后台用户可通过该接口验证 admin 到 report 的跨服务链路
 */
@Slf4j
@RestController
@RequestMapping("/system/report/health")
@SystemModule(SystemModuleEnum.REPORT)
public class ReportHealthController extends BaseController
{
    /**
     * 日志记录器，避免跨服务调用失败时吞掉关键排查信息
     */
    /**
     * 报表服务远程健康检查客户端
     */
    private final RemoteReportHealthService remoteReportHealthService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 构造报表健康检查控制器，通过构造注入保证依赖不可变
     * @param remoteReportHealthService 报表服务 Feign 客户端，负责按服务名调用 zero-report
     */
    public ReportHealthController(RemoteReportHealthService remoteReportHealthService)
    {
        this.remoteReportHealthService = remoteReportHealthService;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 调用 report 内部探活接口，验证 Nacos 服务发现、负载均衡和 OpenFeign 编解码是否正常
     * @return
     * 返回值: AjaxResult
     * 返回值说明: report 可访问时透传其返回数据；调用失败时返回统一错误提示，避免前端收到异常堆栈
     */
    @GetMapping("/ping")
    public AjaxResult ping()
    {
        try
        {
            return remoteReportHealthService.ping();
        }
        catch (Exception e)
        {
            log.error("调用报表服务健康检查失败，请确认 zero-report 已启动并注册到 Nacos", e);
            return error("报表服务暂不可用，请确认服务已启动并注册到注册中心");
        }
    }
}
