package com.zero.system.service.impl;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.zero.common.utils.DateUtils;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.MicroserviceInstanceStatus;
import com.zero.system.service.IMicroserviceClusterService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 03:38
 * 说明: 微服务集群监控服务实现，通过Nacos发现服务实例，并直连实例Actuator端点读取基础运行指标
 */
@Service
public class MicroserviceClusterServiceImpl implements IMicroserviceClusterService
{
    /**
     * Actuator默认基础路径
     */
    private static final String DEFAULT_ACTUATOR_PATH = "/actuator";

    /**
     * Actuator健康状态未知
     */
    private static final String STATUS_UNKNOWN = "UNKNOWN";

    /**
     * Actuator健康状态正常
     */
    private static final String STATUS_UP = "UP";

    /**
     * 负载状态：正常
     */
    private static final String LOAD_STATUS_NORMAL = "NORMAL";

    /**
     * 负载状态：繁忙
     */
    private static final String LOAD_STATUS_BUSY = "BUSY";

    /**
     * 负载状态：负载过高
     */
    private static final String LOAD_STATUS_OVERLOAD = "OVERLOAD";

    /**
     * 字节转换为MB的除数
     */
    private static final double BYTES_PER_MB = 1024D * 1024D;

    /**
     * Nacos服务发现客户端
     */
    private final DiscoveryClient discoveryClient;

    /**
     * 访问微服务Actuator端点的HTTP客户端，设置短超时避免单个实例异常拖慢整个列表
     */
    private final RestTemplate restTemplate;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 初始化微服务集群监控服务
     * @param discoveryClient Nacos服务发现客户端
     * @return
     * 返回值: MicroserviceClusterServiceImpl
     * 返回值说明: 可查询Nacos实例并读取Actuator基础指标的服务实例
     */
    public MicroserviceClusterServiceImpl(DiscoveryClient discoveryClient)
    {
        this.discoveryClient = discoveryClient;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 查询微服务实例状态列表，先从Nacos取实例，再从实例Actuator读取health和metrics基础指标
     * @param serviceName 服务名称，可选；为空时查询全部服务
     * @param status 健康状态，可选；支持UP、DOWN、UNKNOWN
     * @return
     * 返回值: List<MicroserviceInstanceStatus>
     * 返回值说明: 微服务实例状态列表；单个实例Actuator不可访问时保留注册信息并记录失败原因
     */
    @Override
    public List<MicroserviceInstanceStatus> selectServiceList(String serviceName, String status)
    {
        List<String> serviceNames = StringUtils.isNotEmpty(serviceName)
                ? Collections.singletonList(serviceName) : discoveryClient.getServices();
        List<MicroserviceInstanceStatus> result = new ArrayList<>();
        for (String item : serviceNames)
        {
            List<ServiceInstance> instances = discoveryClient.getInstances(item);
            for (ServiceInstance instance : instances)
            {
                MicroserviceInstanceStatus instanceStatus = buildInstanceStatus(item, instance);
                if (StringUtils.isEmpty(status) || status.equalsIgnoreCase(instanceStatus.getStatus()))
                {
                    result.add(instanceStatus);
                }
            }
        }
        result.sort(Comparator.comparing(MicroserviceInstanceStatus::getServiceName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(MicroserviceInstanceStatus::getIp, Comparator.nullsLast(String::compareTo))
                .thenComparing(MicroserviceInstanceStatus::getPort, Comparator.nullsLast(Integer::compareTo)));
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 聚合单个服务实例的注册信息、健康状态和基础指标
     * @param serviceName 服务名称
     * @param instance Nacos服务实例
     * @return
     * 返回值: MicroserviceInstanceStatus
     * 返回值说明: 单个服务实例的列表展示数据
     */
    private MicroserviceInstanceStatus buildInstanceStatus(String serviceName, ServiceInstance instance)
    {
        MicroserviceInstanceStatus status = new MicroserviceInstanceStatus();
        Map<String, String> metadata = new LinkedHashMap<>(instance.getMetadata());
        String actuatorUrl = buildActuatorUrl(instance, metadata);
        status.setServiceName(serviceName);
        status.setInstanceId(resolveInstanceId(serviceName, instance, metadata));
        status.setIp(instance.getHost());
        status.setPort(instance.getPort());
        status.setScheme(instance.isSecure() ? "https" : "http");
        status.setServiceUrl(instance.getUri().toString());
        status.setActuatorUrl(actuatorUrl);
        status.setStatus(STATUS_UNKNOWN);
        status.setMetadata(metadata);
        status.setCheckTime(DateUtils.getTime());
        fillHealth(status, actuatorUrl);
        fillMetrics(status, actuatorUrl);
        fillLoadStatus(status);
        return status;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 读取Actuator健康状态，失败时不抛出异常，避免影响其它实例展示
     * @param status 实例状态对象
     * @param actuatorUrl Actuator基础地址
     * @return
     * 返回值: void
     * 返回值说明: 将健康状态和失败原因写入实例状态对象
     */
    @SuppressWarnings("unchecked")
    private void fillHealth(MicroserviceInstanceStatus status, String actuatorUrl)
    {
        try
        {
            Map<String, Object> health = restTemplate.getForObject(actuatorUrl + "/health", Map.class);
            Object healthStatus = health == null ? null : health.get("status");
            status.setStatus(healthStatus == null ? STATUS_UNKNOWN : String.valueOf(healthStatus));
        }
        catch (RestClientException ex)
        {
            status.setStatus("DOWN");
            status.setErrorMsg("健康端点不可访问: " + ex.getMessage());
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 读取Actuator基础指标，缺失的指标保持为空，页面以--兜底展示
     * @param status 实例状态对象
     * @param actuatorUrl Actuator基础地址
     * @return
     * 返回值: void
     * 返回值说明: 将CPU、内存、线程和运行时长写入实例状态对象
     */
    private void fillMetrics(MicroserviceInstanceStatus status, String actuatorUrl)
    {
        status.setUptimeSeconds(readMetricValue(actuatorUrl, "process.uptime", null));
        status.setProcessCpuUsage(toPercent(readMetricValue(actuatorUrl, "process.cpu.usage", null)));
        status.setSystemCpuUsage(toPercent(readMetricValue(actuatorUrl, "system.cpu.usage", null)));
        status.setCpuCount(readMetricValue(actuatorUrl, "system.cpu.count", null));
        status.setHeapUsedMb(toMb(readMetricValue(actuatorUrl, "jvm.memory.used", "area:heap")));
        status.setHeapCommittedMb(toMb(readMetricValue(actuatorUrl, "jvm.memory.committed", "area:heap")));
        status.setHeapMaxMb(toMb(readMetricValue(actuatorUrl, "jvm.memory.max", "area:heap")));
        status.setThreadLive(readMetricValue(actuatorUrl, "jvm.threads.live", null));
        status.setThreadDaemon(readMetricValue(actuatorUrl, "jvm.threads.daemon", null));
        status.setThreadPeak(readMetricValue(actuatorUrl, "jvm.threads.peak", null));
        status.setGcCount(readMetricValue(actuatorUrl, "jvm.gc.pause", null, "COUNT"));
        status.setGcTotalTimeSeconds(round(readMetricValue(actuatorUrl, "jvm.gc.pause", null, "TOTAL_TIME")));
        status.setGcMaxTimeSeconds(round(readMetricValue(actuatorUrl, "jvm.gc.pause", null, "MAX")));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 根据服务端已采集的CPU、堆内存、线程和GC指标计算微服务负载状态，统一口径后前端只负责展示
     * @param status 实例状态对象
     * @return
     * 返回值: void
     * 返回值说明: 写入loadStatus、loadStatusName、loadStatusDesc和loadStatusReason
     */
    private void fillLoadStatus(MicroserviceInstanceStatus status)
    {
        Double cpuUsage = firstNotNull(status.getSystemCpuUsage(), status.getProcessCpuUsage());
        Double memoryUsage = percent(status.getHeapUsedMb(), status.getHeapMaxMb());
        Double threadPressure = threadPressure(status);
        Double gcMaxTime = status.getGcMaxTimeSeconds();
        String reason = buildLoadReason(cpuUsage, memoryUsage, threadPressure, gcMaxTime);

        if (!STATUS_UP.equalsIgnoreCase(status.getStatus())
                || greaterThan(cpuUsage, 80D)
                || greaterThan(memoryUsage, 85D)
                || greaterThan(threadPressure, 90D)
                || greaterThan(gcMaxTime, 3D))
        {
            status.setLoadStatus(LOAD_STATUS_OVERLOAD);
            status.setLoadStatusName("负载过高");
            status.setLoadStatusDesc("存在高风险指标或服务不可用，需要尽快关注。");
            status.setLoadStatusReason(reason);
            return;
        }

        if (greaterOrEqual(cpuUsage, 60D)
                || greaterOrEqual(memoryUsage, 70D)
                || greaterOrEqual(threadPressure, 75D)
                || greaterOrEqual(gcMaxTime, 1D))
        {
            status.setLoadStatus(LOAD_STATUS_BUSY);
            status.setLoadStatusName("繁忙");
            status.setLoadStatusDesc("服务正在承受较多压力，建议持续观察。");
            status.setLoadStatusReason(reason);
            return;
        }

        status.setLoadStatus(LOAD_STATUS_NORMAL);
        status.setLoadStatusName("正常");
        status.setLoadStatusDesc("服务资源使用处于平稳区间。");
        status.setLoadStatusReason(reason);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 生成微服务负载状态的页面展示依据，缺失指标用--展示
     * @param cpuUsage CPU使用率
     * @param memoryUsage 堆内存使用率
     * @param threadPressure 业务线程压力
     * @param gcMaxTime 单次最长GC耗时
     * @return
     * 返回值: String
     * 返回值说明: 面向页面展示的判断依据文案
     */
    private String buildLoadReason(Double cpuUsage, Double memoryUsage, Double threadPressure, Double gcMaxTime)
    {
        return "判断依据：CPU " + formatPercent(cpuUsage)
                + "，内存使用率 " + formatPercent(memoryUsage)
                + "，业务线程压力 " + formatPercent(threadPressure)
                + "，单次最长GC " + formatSeconds(gcMaxTime) + "。";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 估算业务线程压力，守护线程主要是后台辅助线程，不计入主要业务负载
     * @param status 实例状态对象
     * @return
     * 返回值: Double
     * 返回值说明: 业务线程压力百分比；指标缺失时返回null
     */
    private Double threadPressure(MicroserviceInstanceStatus status)
    {
        if (status.getThreadLive() == null || status.getThreadPeak() == null || status.getThreadPeak() <= 0)
        {
            return null;
        }
        double daemon = status.getThreadDaemon() == null ? 0D : status.getThreadDaemon();
        double businessThreads = Math.max(status.getThreadLive() - daemon, 0D);
        return percent(businessThreads, status.getThreadPeak());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 读取单个Actuator指标值，只取measurements中的第一个VALUE
     * @param actuatorUrl Actuator基础地址
     * @param metricName 指标名称
     * @param tag 指标标签，可选，例如area:heap
     * @return
     * 返回值: Double
     * 返回值说明: 指标数值；端点不可访问、指标不存在或格式异常时返回null
     */
    @SuppressWarnings("unchecked")
    private Double readMetricValue(String actuatorUrl, String metricName, String tag)
    {
        return readMetricValue(actuatorUrl, metricName, tag, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:16
     * 说明: 按统计类型读取Actuator指标值，适配jvm.gc.pause这类同时返回COUNT、TOTAL_TIME和MAX的指标
     * @param actuatorUrl Actuator基础地址
     * @param metricName 指标名称
     * @param tag 指标标签，可选，例如area:heap
     * @param statistic 统计类型，可选；为空时读取第一个统计值
     * @return
     * 返回值: Double
     * 返回值说明: 指标数值；端点不可访问、指标不存在或格式异常时返回null
     */
    @SuppressWarnings("unchecked")
    private Double readMetricValue(String actuatorUrl, String metricName, String tag, String statistic)
    {
        try
        {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(actuatorUrl + "/metrics/" + metricName);
            String scheme = builder.build().getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
            {
                return null;
            }
            if (StringUtils.isNotEmpty(tag))
            {
                builder.queryParam("tag", tag);
            }
            Map<String, Object> metric = restTemplate.getForObject(builder.toUriString(), Map.class);
            if (metric == null || !(metric.get("measurements") instanceof List))
            {
                return null;
            }
            List<Map<String, Object>> measurements = (List<Map<String, Object>>) metric.get("measurements");
            if (measurements.isEmpty())
            {
                return null;
            }
            Map<String, Object> target = measurements.get(0);
            if (StringUtils.isNotEmpty(statistic))
            {
                for (Map<String, Object> measurement : measurements)
                {
                    Object itemStatistic = measurement.get("statistic");
                    if (statistic.equals(String.valueOf(itemStatistic)))
                    {
                        target = measurement;
                        break;
                    }
                }
            }
            Object value = target.get("value");
            return value instanceof Number ? ((Number) value).doubleValue() : null;
        }
        catch (RuntimeException ex)
        {
            return null;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 组装Actuator基础地址，优先兼容Nacos元数据中的管理端口和Actuator路径
     * @param instance Nacos服务实例
     * @param metadata Nacos实例元数据
     * @return
     * 返回值: String
     * 返回值说明: 可访问health和metrics的Actuator基础地址
     */
    private String buildActuatorUrl(ServiceInstance instance, Map<String, String> metadata)
    {
        URI uri = instance.getUri();
        String scheme = instance.isSecure() ? "https" : uri.getScheme();
        String portText = metadata.get("management.port");
        int port = StringUtils.isNotEmpty(portText) && portText.matches("\\d+") ? Integer.parseInt(portText) : instance.getPort();
        String actuatorPath = metadata.get("management.endpoints.web.base-path");
        if (StringUtils.isEmpty(actuatorPath))
        {
            actuatorPath = metadata.get("management.context-path");
        }
        if (StringUtils.isEmpty(actuatorPath))
        {
            actuatorPath = DEFAULT_ACTUATOR_PATH;
        }
        if (!actuatorPath.startsWith("/"))
        {
            actuatorPath = "/" + actuatorPath;
        }
        return scheme + "://" + instance.getHost() + ":" + port + actuatorPath;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 解析实例ID，兼容不同注册方式写入metadata的实例标识差异
     * @param serviceName 服务名称
     * @param instance Nacos服务实例
     * @param metadata Nacos实例元数据
     * @return
     * 返回值: String
     * 返回值说明: 实例唯一标识，缺失时使用服务名、IP和端口拼接
     */
    private String resolveInstanceId(String serviceName, ServiceInstance instance, Map<String, String> metadata)
    {
        String instanceId = metadata.get("nacos.instanceId");
        if (StringUtils.isEmpty(instanceId))
        {
            instanceId = metadata.get("instanceId");
        }
        return StringUtils.isNotEmpty(instanceId) ? instanceId : serviceName + "@" + instance.getHost() + ":" + instance.getPort();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 从多个指标中取第一个有效值，用于CPU等有兜底来源的指标
     * @param values 候选指标值
     * @return
     * 返回值: Double
     * 返回值说明: 第一个非空指标；全部为空时返回null
     */
    private Double firstNotNull(Double... values)
    {
        for (Double value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 计算使用率百分比
     * @param used 已使用数量
     * @param max 最大数量
     * @return
     * 返回值: Double
     * 返回值说明: 百分比；分母缺失或小于等于0时返回null
     */
    private Double percent(Double used, Double max)
    {
        return used == null || max == null || max <= 0 ? null : round(used / max * 100D);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 空值安全的大于比较
     * @param value 指标值
     * @param threshold 阈值
     * @return
     * 返回值: boolean
     * 返回值说明: 指标存在且大于阈值时返回true
     */
    private boolean greaterThan(Double value, Double threshold)
    {
        return value != null && value > threshold;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 空值安全的大于等于比较
     * @param value 指标值
     * @param threshold 阈值
     * @return
     * 返回值: boolean
     * 返回值说明: 指标存在且大于等于阈值时返回true
     */
    private boolean greaterOrEqual(Double value, Double threshold)
    {
        return value != null && value >= threshold;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 格式化百分比文案
     * @param value 百分比数值
     * @return
     * 返回值: String
     * 返回值说明: 页面展示百分比；缺失时返回--
     */
    private String formatPercent(Double value)
    {
        return value == null ? "--" : value + "%";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 04:36
     * 说明: 格式化秒数文案
     * @param value 秒数
     * @return
     * 返回值: String
     * 返回值说明: 页面展示秒数；缺失时返回--
     */
    private String formatSeconds(Double value)
    {
        return value == null ? "--" : value + "秒";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 将0到1之间的小数转换为百分比并保留两位小数
     * @param value 指标原始值
     * @return
     * 返回值: Double
     * 返回值说明: 百分比数值；原始值为空时返回null
     */
    private Double toPercent(Double value)
    {
        return value == null ? null : round(value * 100D);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 将字节转换为MB并保留两位小数
     * @param value 字节数
     * @return
     * 返回值: Double
     * 返回值说明: MB数值；原始值为空或为负数时返回null
     */
    private Double toMb(Double value)
    {
        return value == null || value < 0 ? null : round(value / BYTES_PER_MB);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 03:38
     * 说明: 保留两位小数，避免列表中显示过长的小数
     * @param value 原始数值
     * @return
     * 返回值: Double
     * 返回值说明: 保留两位小数后的数值
     */
    private Double round(Double value)
    {
        return value == null ? null : Math.round(value * 100D) / 100D;
    }
}
