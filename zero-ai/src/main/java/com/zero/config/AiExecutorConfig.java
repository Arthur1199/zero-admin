package com.zero.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:19
 * 说明: 从 Spring 配置创建官方执行器，供 ZERO-JOB 注册、派发、查询日志和接收回调。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AiExecutorProperties.class)
public class AiExecutorConfig {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:19
     * 说明: 启动前校验执行器配置，仅允许运行已部署的 BEAN 任务。
     * @param properties 执行器配置，启用时要求填写凭据和注册地址
     * @return
     * 返回值: XxlJobSpringExecutor
     * 返回值说明: 由 Spring 启停并扫描任务方法的官方执行器
     */
    @Bean
    @ConditionalOnProperty(prefix = "xxl.job.executor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public XxlJobSpringExecutor aiExecutor(AiExecutorProperties properties) {
        if (!StringUtils.hasText(properties.accessToken())) {
            throw new IllegalArgumentException("必须配置 xxl.job.executor.access-token");
        }
        if (!StringUtils.hasText(properties.appName()) || !StringUtils.hasText(properties.logPath())) {
            throw new IllegalArgumentException("必须配置执行器 AppName 和执行日志目录");
        }
        if (properties.port() < 1 || properties.port() > 65535 || properties.timeout() < 1 || properties.timeout() > 10) {
            throw new IllegalArgumentException("执行器端口须在 1 到 65535 之间，通信超时须在 1 到 10 秒之间");
        }
        requireHttpAddress(properties.address(), "xxl.job.executor.address");
        if (!StringUtils.hasText(properties.adminAddresses())) {
            throw new IllegalArgumentException("必须配置 xxl.job.executor.admin-addresses");
        }
        for (String adminAddress : properties.adminAddresses().split(",", -1)) {
            requireHttpAddress(adminAddress, "xxl.job.executor.admin-addresses");
        }
        var executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.adminAddresses());
        executor.setAppname(properties.appName().strip());
        executor.setAccessToken(properties.accessToken());
        executor.setAddress(properties.address().strip());
        executor.setPort(properties.port());
        executor.setTimeout(properties.timeout());
        executor.setLogPath(properties.logPath());
        executor.setLogRetentionDays(properties.logRetentionDays());
        executor.setGlueEnabled(false);
        return executor;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:19
     * 说明: 检查 HTTP 基址，避免凭据或查询参数随地址进入注册记录和日志。
     * @param address 调度中心基址或执行器注册地址
     * @param key 对应配置项，供失败提示定位
     * @return
     * 返回值: void
     * 返回值说明: 格式不正确时只提示配置项
     */
    private void requireHttpAddress(String address, String key) {
        if (StringUtils.hasText(address)) {
            try {
                URI uri = URI.create(address.strip());
                if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        && uri.getHost() != null && uri.getPort() != 0 && uri.getPort() <= 65535
                        && uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null) {
                    return;
                }
            } catch (IllegalArgumentException ignored) {
                // 地址误填凭据时也不将原值包含在错误中。
            }
        }
        throw new IllegalArgumentException(key + " 必须填写有效的 HTTP 或 HTTPS 基址");
    }
}
