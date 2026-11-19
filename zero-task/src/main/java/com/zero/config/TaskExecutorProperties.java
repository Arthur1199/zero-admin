package com.zero.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:19
 * 说明: 绑定 ZERO-JOB 原生执行器配置。
 * @param adminAddresses 调度中心 HTTP 基址，多个地址用逗号分隔，SDK 追加协议路径
 * @param appName ZERO-JOB 执行器组 AppName
 * @param accessToken 执行器通信凭据，禁止输出到日志
 * @param address 上报给调度中心的可访问 HTTP 地址
 * @param port 实际监听的任务派发端口，默认 9998
 * @param timeout 调度中心通信超时，单位秒，默认 3
 * @param logPath 执行日志目录，默认 logs/zero-task/jobhandler
 * @param logRetentionDays 执行日志保留天数，默认 30
 */
@ConfigurationProperties("xxl.job.executor")
public record TaskExecutorProperties(String adminAddresses, @DefaultValue("zero-task") String appName,
                                     String accessToken, String address, @DefaultValue("9998") int port,
                                     @DefaultValue("3") int timeout, @DefaultValue("logs/zero-task/jobhandler") String logPath,
                                     @DefaultValue("30") int logRetentionDays) {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:19
     * 说明: 避免输出配置对象时泄露执行器凭据。
     * @return
     * 返回值: String
     * 返回值说明: 调试用执行器名称和监听端口
     */
    @Override
    public String toString() {
        return "TaskExecutorProperties[appName=" + appName + ", port=" + port + "]";
    }
}
