package com.zero.framework.websocket;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 管理后台连接及发送保护；时间单位毫秒，配置错误时拒绝启动
 * @param channel 必填的环境隔离Redis频道，各节点必须一致，不提供跨环境共用的默认频道
 * @param allowedOrigins 明确允许的浏览器来源，空列表表示同源，不允许通配符
 * @param authTimeout 首帧认证期限
 * @param heartbeatInterval 告知客户端的心跳间隔
 * @param idleTimeout 无有效消息的连接最大空闲时间
 * @param sessionCheckInterval 登录缓存集中核对间隔
 * @param maxTextBytes 单条文本消息最大UTF-8字节数
 * @param maxConnectionsPerSession 每个登录会话在所有节点合计的连接上限
 * @param maxPendingConnections 本节点未认证连接上限
 * @param sendTimeout 单连接发送超时
 * @param sendBufferBytes 单连接发送缓冲上限
 * @param sendThreads 有界发送线程数量
 * @param sendQueueCapacity 等待发送任务上限
 */
@Validated
@ConfigurationProperties("zero.websocket")
public record AdminWebSocketProperties(
        @NotBlank String channel,
        @DefaultValue List<String> allowedOrigins,
        @Min(1000) @DefaultValue("5000") int authTimeout,
        @Min(1000) @DefaultValue("30000") int heartbeatInterval,
        @Min(1000) @DefaultValue("90000") int idleTimeout,
        @Min(1000) @DefaultValue("30000") int sessionCheckInterval,
        @Min(1024) @DefaultValue("65536") int maxTextBytes,
        @Min(1) @DefaultValue("10") int maxConnectionsPerSession,
        @Min(1) @DefaultValue("100") int maxPendingConnections,
        @Min(1000) @DefaultValue("10000") int sendTimeout,
        @Min(1024) @DefaultValue("262144") int sendBufferBytes,
        @Min(1) @DefaultValue("4") int sendThreads,
        @Min(1) @DefaultValue("1000") int sendQueueCapacity)
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 心跳必须早于空闲断开时间，避免错误配置导致所有连接持续重连
     * @param channel Redis环境频道
     * @param allowedOrigins 明确的跨域来源列表，默认空列表
     * @param authTimeout 首帧期限，毫秒
     * @param heartbeatInterval 心跳间隔，毫秒
     * @param idleTimeout 空闲期限，毫秒
     * @param sessionCheckInterval 会话核对间隔，毫秒
     * @param maxTextBytes 文本消息字节上限
     * @param maxConnectionsPerSession 跨节点单会话连接上限
     * @param maxPendingConnections 本节点待认证连接上限
     * @param sendTimeout 发送期限，毫秒
     * @param sendBufferBytes 发送缓冲字节上限
     * @param sendThreads 发送线程数量
     * @param sendQueueCapacity 发送队列容量
     */
    public AdminWebSocketProperties
    {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        if (idleTimeout <= heartbeatInterval)
        {
            throw new IllegalArgumentException("WebSocket空闲期限必须大于心跳间隔");
        }
    }
}
