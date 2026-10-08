package com.zero.system.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 03:38
 * 说明: 微服务实例运行状态，承载从Nacos实例信息和Actuator健康、指标端点聚合后的列表展示数据
 */
public class MicroserviceInstanceStatus
{
    /**
     * 服务名称，对应Nacos中的serviceId
     */
    private String serviceName;

    /**
     * 实例ID，优先使用Nacos实例元数据中的实例标识，缺失时由服务名、IP和端口拼接
     */
    private String instanceId;

    /**
     * 注册IP，来自Nacos服务实例
     */
    private String ip;

    /**
     * 注册端口，来自Nacos服务实例
     */
    private Integer port;

    /**
     * 访问协议，http或https
     */
    private String scheme;

    /**
     * 服务注册地址，来自Nacos服务实例URI
     */
    private String serviceUrl;

    /**
     * Actuator基础地址，用于读取health和metrics
     */
    private String actuatorUrl;

    /**
     * 健康状态，UP表示Actuator健康检查通过，DOWN或UNKNOWN表示不可用或无法读取
     */
    private String status;

    /**
     * Nacos元数据，列表展示时只返回注册元数据，不主动读取敏感Actuator配置
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * 运行时长，单位秒，来自process.uptime指标
     */
    private Double uptimeSeconds;

    /**
     * 进程CPU使用率，百分比
     */
    private Double processCpuUsage;

    /**
     * 系统CPU使用率，百分比
     */
    private Double systemCpuUsage;

    /**
     * CPU核心数
     */
    private Double cpuCount;

    /**
     * 堆内存已用，单位MB
     */
    private Double heapUsedMb;

    /**
     * 堆内存已申请，单位MB，表示JVM已经从系统申请到手、当前可直接使用的堆内存
     */
    private Double heapCommittedMb;

    /**
     * 堆内存最大值，单位MB
     */
    private Double heapMaxMb;

    /**
     * 当前活动线程数
     */
    private Double threadLive;

    /**
     * 当前守护线程数
     */
    private Double threadDaemon;

    /**
     * 线程峰值
     */
    private Double threadPeak;

    /**
     * 垃圾回收总次数
     */
    private Double gcCount;

    /**
     * 垃圾回收总耗时，单位秒
     */
    private Double gcTotalTimeSeconds;

    /**
     * 单次垃圾回收最大耗时，单位秒
     */
    private Double gcMaxTimeSeconds;

    /**
     * 微服务负载状态，NORMAL-正常，BUSY-繁忙，OVERLOAD-负载过高
     */
    private String loadStatus;

    /**
     * 微服务负载状态中文名称
     */
    private String loadStatusName;

    /**
     * 微服务负载状态说明，面向页面展示
     */
    private String loadStatusDesc;

    /**
     * 微服务负载状态判断依据，说明服务端按哪些指标计算
     */
    private String loadStatusReason;

    /**
     * 最近检测时间，格式yyyy-MM-dd HH:mm:ss
     */
    private String checkTime;

    /**
     * 读取失败原因，健康端点不可访问或指标缺失时用于页面提示
     */
    private String errorMsg;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 获取服务名称
     * @return
     * 返回值: String
     * 返回值说明: Nacos服务名称
     */
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 设置服务名称
     * @param serviceName Nacos服务名称
     * @return
     * 返回值: void
     * 返回值说明: 写入服务名称
     */
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 获取实例ID
     * @return
     * 返回值: String
     * 返回值说明: 实例唯一标识
     */
    public String getInstanceId()
    {
        return instanceId;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 设置实例ID
     * @param instanceId 实例唯一标识
     * @return
     * 返回值: void
     * 返回值说明: 写入实例唯一标识
     */
    public void setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }

    public String getServiceUrl()
    {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    public String getActuatorUrl()
    {
        return actuatorUrl;
    }

    public void setActuatorUrl(String actuatorUrl)
    {
        this.actuatorUrl = actuatorUrl;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = metadata;
    }

    public Double getUptimeSeconds()
    {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Double uptimeSeconds)
    {
        this.uptimeSeconds = uptimeSeconds;
    }

    public Double getProcessCpuUsage()
    {
        return processCpuUsage;
    }

    public void setProcessCpuUsage(Double processCpuUsage)
    {
        this.processCpuUsage = processCpuUsage;
    }

    public Double getSystemCpuUsage()
    {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(Double systemCpuUsage)
    {
        this.systemCpuUsage = systemCpuUsage;
    }

    public Double getCpuCount()
    {
        return cpuCount;
    }

    public void setCpuCount(Double cpuCount)
    {
        this.cpuCount = cpuCount;
    }

    public Double getHeapUsedMb()
    {
        return heapUsedMb;
    }

    public void setHeapUsedMb(Double heapUsedMb)
    {
        this.heapUsedMb = heapUsedMb;
    }

    public Double getHeapCommittedMb()
    {
        return heapCommittedMb;
    }

    public void setHeapCommittedMb(Double heapCommittedMb)
    {
        this.heapCommittedMb = heapCommittedMb;
    }

    public Double getHeapMaxMb()
    {
        return heapMaxMb;
    }

    public void setHeapMaxMb(Double heapMaxMb)
    {
        this.heapMaxMb = heapMaxMb;
    }

    public Double getThreadLive()
    {
        return threadLive;
    }

    public void setThreadLive(Double threadLive)
    {
        this.threadLive = threadLive;
    }

    public Double getThreadDaemon()
    {
        return threadDaemon;
    }

    public void setThreadDaemon(Double threadDaemon)
    {
        this.threadDaemon = threadDaemon;
    }

    public Double getThreadPeak()
    {
        return threadPeak;
    }

    public void setThreadPeak(Double threadPeak)
    {
        this.threadPeak = threadPeak;
    }

    public Double getGcCount()
    {
        return gcCount;
    }

    public void setGcCount(Double gcCount)
    {
        this.gcCount = gcCount;
    }

    public Double getGcTotalTimeSeconds()
    {
        return gcTotalTimeSeconds;
    }

    public void setGcTotalTimeSeconds(Double gcTotalTimeSeconds)
    {
        this.gcTotalTimeSeconds = gcTotalTimeSeconds;
    }

    public Double getGcMaxTimeSeconds()
    {
        return gcMaxTimeSeconds;
    }

    public void setGcMaxTimeSeconds(Double gcMaxTimeSeconds)
    {
        this.gcMaxTimeSeconds = gcMaxTimeSeconds;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 获取微服务负载状态
     * @return
     * 返回值: String
     * 返回值说明: NORMAL-正常，BUSY-繁忙，OVERLOAD-负载过高
     */
    public String getLoadStatus()
    {
        return loadStatus;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 设置微服务负载状态
     * @param loadStatus 负载状态编码
     * @return
     * 返回值: void
     * 返回值说明: 写入负载状态编码
     */
    public void setLoadStatus(String loadStatus)
    {
        this.loadStatus = loadStatus;
    }

    public String getLoadStatusName()
    {
        return loadStatusName;
    }

    public void setLoadStatusName(String loadStatusName)
    {
        this.loadStatusName = loadStatusName;
    }

    public String getLoadStatusDesc()
    {
        return loadStatusDesc;
    }

    public void setLoadStatusDesc(String loadStatusDesc)
    {
        this.loadStatusDesc = loadStatusDesc;
    }

    public String getLoadStatusReason()
    {
        return loadStatusReason;
    }

    public void setLoadStatusReason(String loadStatusReason)
    {
        this.loadStatusReason = loadStatusReason;
    }

    public String getCheckTime()
    {
        return checkTime;
    }

    public void setCheckTime(String checkTime)
    {
        this.checkTime = checkTime;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }
}
