package com.zero.framework.websocket;

import lombok.extern.slf4j.Slf4j;
import com.zero.common.constant.AdminWebSocketConstants;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 注册管理后台原生WebSocket及Redis广播订阅，不修改其它服务的通信和权限配置
 */
@Slf4j
@Configuration
@EnableWebSocket
@EnableConfigurationProperties(AdminWebSocketProperties.class)
public class AdminWebSocketConfig implements WebSocketConfigurer
{
    private final AdminWebSocketHandler handler;
    private final AdminWebSocketProperties properties;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 复用Spring管理的处理器及经过校验的连接配置
     * @param handler 本节点连接处理器
     * @param properties 连接保护参数
     */
    public AdminWebSocketConfig(AdminWebSocketHandler handler, AdminWebSocketProperties properties)
    {
        this.handler = handler;
        this.properties = properties;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 默认保持同源校验，跨域仅接受明确列出的HTTP或HTTPS来源
     * @param registry 服务端握手端点注册表
     * @return
     * 返回值: void
     * 返回值说明: 注册唯一握手路径，不开启SockJS或任意来源通配
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        String[] origins = properties.allowedOrigins().toArray(String[]::new);
        for (String origin : origins)
        {
            java.net.URI uri = java.net.URI.create(origin);
            if (origin.contains("*") || uri.getHost() == null
                    || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty()))
            {
                throw new IllegalArgumentException("WebSocket允许来源必须是明确的HTTP/HTTPS源，不含路径或通配符");
            }
        }
        registry.addHandler(handler, AdminWebSocketConstants.PATH).setAllowedOrigins(origins);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 在容器层限制消息大小，避免超大首帧进入JSON解析
     * @param properties 连接配置
     * @return
     * 返回值: ServletServerContainerFactoryBean
     * 返回值说明: 当前Servlet容器的WebSocket文本及空闲保护
     */
    @Bean
    public static ServletServerContainerFactoryBean adminWebSocketContainer(AdminWebSocketProperties properties)
    {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(properties.maxTextBytes());
        container.setMaxBinaryMessageBufferSize(properties.maxTextBytes());
        container.setMaxSessionIdleTimeout((long) properties.idleTimeout());
        container.setAsyncSendTimeout((long) properties.sendTimeout());
        return container;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 使用有界线程池隔离Redis订阅线程和慢客户端，队列满时拒绝任务而非回到订阅线程发送
     * @param properties 线程数及队列保护配置
     * @return
     * 返回值: ThreadPoolTaskExecutor
     * 返回值说明: 专用发送线程池，由Spring在关闭时清理
     */
    @Bean("adminWebSocketSender")
    public static ThreadPoolTaskExecutor adminWebSocketSender(AdminWebSocketProperties properties)
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.sendThreads());
        executor.setMaxPoolSize(properties.sendThreads());
        executor.setQueueCapacity(properties.sendQueueCapacity());
        executor.setThreadNamePrefix("admin-ws-send-");
        return executor;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 所有节点订阅同一环境频道，发布节点也仅通过订阅回调投递
     * @param factory 现有Redis连接工厂
     * @param service 消息解码及分发入口
     * @param properties 环境频道配置
     * @return
     * 返回值: RedisMessageListenerContainer
     * 返回值说明: 支持Redis断线后重新订阅，不补发离线消息
     */
    @Bean
    public static RedisMessageListenerContainer adminWebSocketRedisListener(RedisConnectionFactory factory,
            AdminWebSocketService service, AdminWebSocketProperties properties)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(service, new ChannelTopic(properties.channel()));
        // 禁止框架默认异常日志打印可能包含业务内容的Redis原始消息。
        container.setErrorHandler(error -> log
                .warn("WebSocket订阅异常，等待恢复"));
        return container;
    }
}
