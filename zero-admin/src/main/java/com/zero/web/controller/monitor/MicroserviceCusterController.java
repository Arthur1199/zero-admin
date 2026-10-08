package com.zero.web.controller.monitor;

import java.util.List;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.page.TableDataInfo;
import com.zero.system.domain.MicroserviceInstanceStatus;
import com.zero.system.service.IMicroserviceClusterService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 03:36
 * 说明: 微服务状态实时监控控制器
 */
@RestController
@RequestMapping("/monitor/microService")
public class MicroserviceCusterController extends BaseController
{
    /**
     * 微服务集群监控服务字段
     */
    private final IMicroserviceClusterService microserviceClusterService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 初始化微服务状态监控控制器
     * @param microserviceClusterService 微服务集群监控服务
     * @return
     * 返回值: MicroserviceCusterController
     * 返回值说明: 可查询微服务状态列表的控制器实例
     */
    public MicroserviceCusterController(IMicroserviceClusterService microserviceClusterService)
    {
        this.microserviceClusterService = microserviceClusterService;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 查询微服务状态列表，数据来自Nacos服务实例和各实例Actuator health、metrics基础端点
     * @param serviceName 服务名称，可选；为空时查询全部已注册服务
     * @param status 健康状态，可选；支持UP、DOWN、UNKNOWN
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 微服务状态分页列表，rows承载实例状态数据，total承载总数
     */
    @RequiresPermissions("monitor:microService:list")
    @GetMapping("/list")
    public TableDataInfo list(String serviceName, String status)
    {
        List<MicroserviceInstanceStatus> list = microserviceClusterService.selectServiceList(serviceName, status);
        return getDataTable(list);
    }
}
