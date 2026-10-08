package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.MicroserviceInstanceStatus;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 03:38
 * 说明: 微服务集群监控服务，负责聚合Nacos注册实例和Actuator健康、指标数据
 */
public interface IMicroserviceClusterService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 查询微服务实例状态列表，仅读取health和metrics基础指标，不读取环境变量、配置属性和日志级别等敏感端点
     * @param serviceName 服务名称，可选；为空时查询Nacos中全部服务
     * @param status 健康状态，可选；支持按UP、DOWN、UNKNOWN过滤
     * @return
     * 返回值: List<MicroserviceInstanceStatus>
     * 返回值说明: 微服务实例状态列表，包含Nacos注册信息和Actuator基础指标
     */
    public List<MicroserviceInstanceStatus> selectServiceList(String serviceName, String status);
}
