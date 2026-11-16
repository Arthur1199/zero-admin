package com.zero.framework.statistics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 统计队列及调度参数，支持环境及Nacos覆盖；总开关独立由后台参数sys.statistics.enabled控制。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "zero.statistics")
public class StatisticsProperties
{
    /** 在线采样租约后缀，跨实例共用。 */
    public static final String SAMPLE_LOCK_SUFFIX = ":sample-lock";
    /** 持久化Stream键，按环境隔离。 */
    private String streamKey = "zero:admin:statistics:events";
    /** 同一集群共用的消费组名称。 */
    private String group = "admin-statistics";
    /** 慢请求阈值，毫秒。 */
    private long slowMillis = 1000;
    /** 单轮最大事件数量，运行时限制1至500。 */
    private int batchSize = 500;
    /** 消费间隔，毫秒，最小1000。 */
    private long pollMillis = 5000;
    /** 待确认消息接管等待时间，毫秒，最小60000。 */
    private long claimIdleMillis = 60000;
    /** 在线采样间隔，毫秒，最小30000。 */
    private long sampleMillis = 30000;
    /** Ant风格排除路径，统计自身接口始终排除。 */
    private List<String> excludes = List.of("/", "/error", "/profile/**", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/druid/**", "/actuator/**", "/**/*.js", "/**/*.css", "/**/*.html", "/favicon.ico");
}
