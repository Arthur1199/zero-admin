package com.zero.framework.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.common.constant.CacheConstants;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.framework.config.RedisConfig;
import com.zero.framework.config.SecurityConfig;
import com.zero.framework.config.properties.PermitAllUrlProperties;
import com.zero.framework.security.filter.JwtAuthenticationTokenFilter;
import com.zero.framework.security.handle.AuthenticationEntryPointImpl;
import com.zero.framework.security.handle.LogoutSuccessHandlerImpl;
import com.zero.framework.web.service.TokenService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 显式指定隔离Redis端口后，启动两个独立Tomcat上下文验证真实网络、JWT、Pub/Sub和跨节点租约
 * 不启动业务服务、数据库或Nacos，不使用已有用户数据；测试键使用随机编号并自动到期
 */
@EnabledIfEnvironmentVariable(named = "ZERO_WS_TEST_REDIS_PORT", matches = "[0-9]+")
class AdminWebSocketIntegrationTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 跨节点强退仅命中选中会话的标签页，禁用或删除后的通知覆盖同账号剩余会话
     * @return
     * 返回值: void
     * 返回值说明: 真实Redis与网络验证，所有用户及会话均为随机隔离测试数据
     */
    @Test
    void logoutAcrossNodesRespectsSessionAndUserTargets() throws Exception
    {
        String channel = "isolated:ws:test:" + UUID.randomUUID();
        try (var a = node(channel); var b = node(channel))
        {
            LoginUser selected = AdminWebSocketTest.user(31L, UUID.randomUUID().toString());
            LoginUser independent = AdminWebSocketTest.user(31L, UUID.randomUUID().toString());
            LoginUser other = AdminWebSocketTest.user(32L, UUID.randomUUID().toString());
            String selectedToken = cacheUser(a, selected);
            String independentToken = cacheUser(a, independent);
            String otherToken = cacheUser(a, other);
            try (Client first = connect(a, selectedToken); Client tab = connect(b, selectedToken);
                    Client second = connect(b, independentToken); Client third = connect(a, otherToken))
            {
                for (Client client : List.of(first, tab, second, third)) client.next();
                var logout = new com.zero.system.service.AdminUserLogoutService(a.getBean(RedisCache.class),
                        a.getBean(AdminWebSocketService.class));
                logout.forceLogout(selected.getToken());
                for (Client client : List.of(first, tab))
                {
                    assertThat(client.next().path("msgType").path("messageType").asInt()).isEqualTo(3);
                    assertThat(client.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(4401);
                }
                assertThat(a.getBean(TokenService.class).getWebSocketLoginUser(selected.getToken())).isNull();
                assertThat(second.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                assertThat(third.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                assertThat(a.getBean(TokenService.class).getWebSocketLoginUser(independent.getToken())).isNotNull();
                logout.afterUsersUnavailable(List.of(31L));
                assertThat(second.next().path("msgType").path("messageType").asInt()).isEqualTo(3);
                assertThat(second.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(4401);
                assertThat(a.getBean(TokenService.class).getWebSocketLoginUser(independent.getToken())).isNull();
                assertThat(a.getBean(TokenService.class).getWebSocketLoginUser(other.getToken())).isNotNull();
                assertThat(third.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
            }
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 验证任意节点发送、目标过滤、多标签、跨节点连接上限、心跳不续期及节点重建
     * @return
     * 返回值: void
     * 返回值说明: 使用真实Redis和WebSocket收发，订阅中断消息不补发
     */
    @Test
    void twoNodesDeliverThroughRealRedisAndRecover() throws Exception
    {
        String channel = "isolated:ws:test:" + UUID.randomUUID();
        try (var a = node(channel); var b = node(channel))
        {
            LoginUser user = AdminWebSocketTest.user(21L, UUID.randomUUID().toString());
            LoginUser other = AdminWebSocketTest.user(22L, UUID.randomUUID().toString());
            String token = cacheUser(a, user);
            String otherToken = cacheUser(a, other);
            try (Client first = connect(a, token); Client second = connect(b, token); Client third = connect(b, otherToken))
            {
                assertThat(first.next().path("msgType").path("messageType").asInt()).isEqualTo(0);
                assertThat(second.next().path("msgType").path("messageType").asInt()).isEqualTo(0);
                assertThat(third.next().path("msgType").path("messageType").asInt()).isEqualTo(0);
                String id = a.getBean(AdminWebSocketService.class).sendMsg(WebSocketMessageVo.builder().msgType(WebSocketMessageType.REFRESH_BROWSER)
                        .toAdminId(21L).fromAdminId(22L).senderIp("isolated-node-a")
                        .msgData(Map.of("value", 1)).forwardUrl("/orders/detail").build());
                JsonNode directed = first.next();
                assertThat(directed.path("id").asText()).isEqualTo(id);
                assertThat(directed.path("toAdminId").asLong()).isEqualTo(21L);
                assertThat(directed.path("fromAdminId").asLong()).isEqualTo(22L);
                assertThat(directed.path("senderIp").asText()).isEqualTo(com.zero.common.utils.ip.IpUtils.getLocalIpAddress());
                assertThat(directed.path("forwardUrl").asText()).isEqualTo("/orders/detail");
                assertThat(directed.path("msgData").path("value").asInt()).isEqualTo(1);
                assertThat(second.next().path("id").asText()).isEqualTo(id);
                assertThat(third.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                assertThat(first.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();

                // 复用用户变更通知服务，确认刷新事件只投递到跨节点的目标用户连接。
                new com.zero.system.service.AdminUserRefreshService(a.getBean(AdminWebSocketService.class))
                        .afterChanged(List.of(21L));
                for (Client target : List.of(first, second))
                {
                    JsonNode refresh = target.next();
                    assertThat(refresh.path("msgType").path("messageType").asInt()).isEqualTo(2);
                    assertThat(refresh.path("msgData").path("content").asText()).contains("部门或角色");
                }
                assertThat(third.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();

                String broadcast = b.getBean(AdminWebSocketService.class).sendMsg(WebSocketMessageVo.builder().msgType(WebSocketMessageType.REFRESH_BROWSER).msgData(null).build());
                for (Client client : List.of(first, second, third))
                {
                    JsonNode message = client.next();
                    assertThat(message.path("id").asText()).isEqualTo(broadcast);
                    assertThat(message.has("userIds")).isFalse();
                    assertThat(message.has("version")).isFalse();
                    assertThat(message.has("type")).isFalse();
                    assertThat(message.has("data")).isFalse();
                    assertThat(message.path("msgType").path("messageType").asInt()).isEqualTo(2);
                    assertThat(message.path("msgData").isNull()).isTrue();
                }

                // 配置变更广播覆盖两个节点的所有用户，不局限于操作者或单节点。
                new com.zero.system.service.AdminUserRefreshService(b.getBean(AdminWebSocketService.class))
                        .afterApiAuthChanged();
                String configMessageId = null;
                for (Client client : List.of(first, second, third))
                {
                    JsonNode notice = client.next();
                    if (configMessageId == null) configMessageId = notice.path("id").asText();
                    assertThat(notice.path("id").asText()).isEqualTo(configMessageId);
                    assertThat(notice.path("msgType").path("messageType").asInt()).isEqualTo(2);
                    assertThat(notice.path("msgData").path("content").asText()).contains("谷歌验证");
                    assertThat(client.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                }

                String groupId = b.getBean(AdminWebSocketService.class).sendToUsers(List.of(21L, 22L, 21L),
                        WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, Map.of("taskId", "task-test")));
                for (Client client : List.of(first, second, third))
                {
                    JsonNode group = client.next();
                    assertThat(group.path("id").asText()).isEqualTo(groupId);
                    assertThat(group.path("msgType").path("messageType").asInt()).isEqualTo(2);
                    assertThat(client.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                }

                // 本测试将跨节点会话上限设为2，第三条连接必须被拒绝。
                try (Client limited = connect(a, token))
                {
                    assertThat(limited.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(4429);
                }
                long expires = a.getBean(RedisCache.class).<LoginUser>getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + user.getToken()).getExpireTime();
                first.send(WebSocketMessageType.PING_PONG, Map.of());
                assertThat(first.next().path("msgType").path("messageType").asInt()).isEqualTo(1);
                assertThat(a.getBean(RedisCache.class).<LoginUser>getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + user.getToken()).getExpireTime()).isEqualTo(expires);

                // 停订阅时丢失消息，恢复后只接收新消息，不补发旧消息。
                var subscriber = b.getBean("adminWebSocketRedisListener", RedisMessageListenerContainer.class);
                subscriber.stop();
                a.getBean(AdminWebSocketService.class).sendToUser(22L, WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, Map.of()));
                subscriber.start();
                String restored = a.getBean(AdminWebSocketService.class).sendToUser(22L, WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, Map.of()));
                assertThat(third.next().path("id").asText()).isEqualTo(restored);
                assertThat(third.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();

                // 模拟会话到期，不执行任何真实用户删除或强退。
                user.setExpireTime(System.currentTimeMillis() - 1);
                a.getBean(RedisCache.class).setCacheObject(CacheConstants.LOGIN_TOKEN_KEY + user.getToken(), user, 1, TimeUnit.MINUTES);
                a.getBean(AdminWebSocketHandler.class).checkSessions();
                b.getBean(AdminWebSocketHandler.class).checkSessions();
                assertThat(first.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(4401);
                assertThat(second.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(4401);
                String alive = a.getBean(AdminWebSocketService.class).sendToUser(22L, WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, Map.of()));
                assertThat(third.next().path("id").asText()).isEqualTo(alive);
            }
            b.close();
            try (var restarted = node(channel); Client client = connect(restarted, otherToken))
            {
                assertThat(client.next().path("msgType").path("messageType").asInt()).isEqualTo(0);
                String id = a.getBean(AdminWebSocketService.class).sendToUser(22L, WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, Map.of()));
                assertThat(client.next().path("id").asText()).isEqualTo(id);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 验证真实握手来源校验、无首帧认证超时以及伪造凭证拒绝
     * @return
     * 返回值: void
     * 返回值说明: 非可信来源无法升级，未认证连接不能接收业务广播
     */
    @Test
    void rejectsWrongOriginMissingAndForgedCredentials() throws Exception
    {
        try (var node = node("isolated:ws:test:" + UUID.randomUUID()))
        {
            int port = node.getWebServer().getPort();
            assertThatThrownBy(() -> HttpClient.newHttpClient().newWebSocketBuilder()
                    .header("Origin", "https://untrusted.example.test")
                    .buildAsync(URI.create("ws://localhost:" + port + "/ws/admin"), new Client()).join())
                    .hasCauseInstanceOf(java.net.http.WebSocketHandshakeException.class);
            try (Client pending = open(node))
            {
                node.getBean(AdminWebSocketService.class).broadcast(WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, Map.of()));
                assertThat(pending.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                assertThat(pending.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(1013);
            }
            try (Client forged = connect(node, "invalid-isolated-test-token"))
            {
                assertThat(forged.messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
                assertThat(forged.closes.poll(5, TimeUnit.SECONDS)).isEqualTo(4401);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 两节点并发申请同一测试会话名额不能突破全局上限，过期或释放的租约不能续租复活
     * @return
     * 返回值: void
     * 返回值说明: 验证真实Redis脚本的原子申请、跨节点续租及过期回收
     */
    @Test
    void sharedConnectionQuotaRemainsAtomicAcrossNodes() throws Exception
    {
        String channel = "isolated:ws:quota:" + UUID.randomUUID();
        try (var a = node(channel); var b = node(channel))
        {
            var first = a.getBean(AdminWebSocketLease.class);
            var second = b.getBean(AdminWebSocketLease.class);
            String session = UUID.randomUUID().toString();
            var executor = java.util.concurrent.Executors.newFixedThreadPool(8);
            var begin = new java.util.concurrent.CountDownLatch(1);
            var admitted = ConcurrentHashMap.<String>newKeySet();
            try
            {
                var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
                for (int index = 0; index < 20; index++)
                {
                    String id = "connection-" + index;
                    var lease = index % 2 == 0 ? first : second;
                    tasks.add(executor.submit(() -> {
                        try
                        {
                            begin.await(5, TimeUnit.SECONDS);
                            if (lease.acquire(session, id)) admitted.add(id);
                        }
                        catch (InterruptedException error)
                        {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("测试被中断");
                        }
                    }));
                }
                begin.countDown();
                for (var task : tasks) task.get(10, TimeUnit.SECONDS);
                assertThat(admitted).hasSize(2);
                String id = admitted.iterator().next();
                assertThat(second.renew(session, id)).isTrue();
                var redis = new org.springframework.data.redis.core.StringRedisTemplate(
                        a.getBean(org.springframework.data.redis.connection.RedisConnectionFactory.class));
                // 仅把随机测试键中的一个名额标为过期，验证旧连接不能再续租。
                redis.opsForZSet().add(channel + ":connections:" + session, id, 0);
                assertThat(first.renew(session, id)).isFalse();
                assertThat(second.acquire(session, "replacement")).isTrue();
                first.release(session, "replacement");
                assertThat(second.renew(session, "replacement")).isFalse();
            }
            finally
            {
                executor.shutdownNow();
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 构造独立Servlet应用，仅导入生产WebSocket、安全链及登录缓存组件
     * @param channel 随机隔离频道
     * @return
     * 返回值: AnnotationConfigServletWebServerApplicationContext
     * 返回值说明: 监听随机本地端口的测试节点，调用方负责关闭
     */
    private AnnotationConfigServletWebServerApplicationContext node(String channel)
    {
        var context = new AnnotationConfigServletWebServerApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("isolated-websocket-test", Map.of(
                "zero.websocket.channel", channel, "zero.websocket.auth-timeout", "1000",
                "zero.websocket.max-connections-per-session", "2", "token.header", "Authorization",
                "token.secret", "isolated-websocket-integration-secret", "token.expireTime", "30")));
        context.getBeanFactory().registerSingleton("userDetailsService", mock(UserDetailsService.class));
        context.getBeanFactory().registerSingleton("logoutSuccessHandler", mock(LogoutSuccessHandlerImpl.class));
        PermitAllUrlProperties permitted = mock(PermitAllUrlProperties.class);
        when(permitted.getUrls()).thenReturn(List.of());
        context.getBeanFactory().registerSingleton("permitAllUrl", permitted);
        context.register(Node.class);
        context.refresh();
        return context;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 写入随机编号的测试会话并签发测试JWT，不调用真实登录及数据库
     * @param node 测试节点
     * @param user 隔离测试用户
     * @return
     * 返回值: String
     * 返回值说明: 仅用于首帧的测试JWT，禁止输出
     */
    private String cacheUser(AnnotationConfigServletWebServerApplicationContext node, LoginUser user)
    {
        node.getBean(RedisCache.class).setCacheObject(CacheConstants.LOGIN_TOKEN_KEY + user.getToken(), user, 5, TimeUnit.MINUTES);
        return node.getBean(TokenService.class).getExistingToken(user);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 建立真实网络连接后发送认证帧，JWT不放URL
     * @param node 接收握手的节点
     * @param token 测试JWT
     * @return
     * 返回值: Client
     * 返回值说明: 收集服务端消息及关闭码的客户端
     */
    private Client connect(AnnotationConfigServletWebServerApplicationContext node, String token)
            throws com.fasterxml.jackson.core.JsonProcessingException
    {
        Client client = open(node);
        client.socket.sendText(new ObjectMapper().writeValueAsString(Map.of("token", token)), true).join();
        return client;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 同源握手并等待网络建立
     * @param node 测试节点
     * @return
     * 返回值: Client
     * 返回值说明: 尚未认证的客户端
     */
    private Client open(AnnotationConfigServletWebServerApplicationContext node)
    {
        int port = node.getWebServer().getPort();
        Client client = new Client();
        client.socket = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build().newWebSocketBuilder()
                .header("Origin", "http://localhost:" + port)
                .buildAsync(URI.create("ws://localhost:" + port + "/ws/admin"), client).join();
        return client;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 测试节点依赖，仅启用实际WebSocket、安全链、Redis和JSON，不加载application.yml中的外部资源
     */
    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    @Import({AdminWebSocketConfig.class, AdminWebSocketHandler.class, AdminWebSocketService.class,
            AdminWebSocketLease.class, TokenService.class, RedisConfig.class, RedisCache.class,
            SecurityConfig.class, JwtAuthenticationTokenFilter.class, AuthenticationEntryPointImpl.class})
    static class Node
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 满足现有RedisConfig的缓存基础设施依赖，测试不调用业务缓存注解
         * @return
         * 返回值: CacheManager
         * 返回值说明: 隔离的本地测试缓存管理器，登录缓存仍使用真实Redis
         */
        @Bean
        org.springframework.cache.CacheManager cacheManager()
        {
            return new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 在嵌入容器提前创建Filter前解析测试属性，不依赖完整Boot启动流程
         * @return
         * 返回值: PropertySourcesPlaceholderConfigurer
         * 返回值说明: 仅解析当前测试上下文属性
         */
        @Bean
        static org.springframework.context.support.PropertySourcesPlaceholderConfigurer placeholders()
        {
            return new org.springframework.context.support.PropertySourcesPlaceholderConfigurer();
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 测试容器绑定随机端口
         * @return
         * 返回值: TomcatServletWebServerFactory
         * 返回值说明: 独立Tomcat实例
         */
        @Bean
        TomcatServletWebServerFactory webServer()
        {
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(0);
            factory.addContextCustomizers(context -> context.addServletContainerInitializer(
                    new org.apache.tomcat.websocket.server.WsSci(), null));
            return factory;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 注册实际MVC握手路由，显式复用当前上下文，避免默认加载XML子上下文
         * @param context 当前节点的Web应用上下文
         * @return
         * 返回值: ServletRegistrationBean
         * 返回值说明: 根路径MVC分发器
         */
        @Bean
        ServletRegistrationBean<DispatcherServlet> dispatcher(org.springframework.web.context.WebApplicationContext context)
        {
            return new ServletRegistrationBean<>(new DispatcherServlet(context), "/");
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 注册项目实际SecurityFilterChain，验证握手仅放行GET精确路径
         * @return
         * 返回值: DelegatingFilterProxyRegistrationBean
         * 返回值说明: 安全链过滤器注册
         */
        @Bean
        DelegatingFilterProxyRegistrationBean securityFilter()
        {
            return new DelegatingFilterProxyRegistrationBean("springSecurityFilterChain");
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 使用明确指定的本地隔离Redis端口
         * @return
         * 返回值: LettuceConnectionFactory
         * 返回值说明: 无生产配置来源的测试连接工厂
         */
        @Bean
        LettuceConnectionFactory redisConnectionFactory()
        {
            return new LettuceConnectionFactory("127.0.0.1", Integer.parseInt(System.getenv("ZERO_WS_TEST_REDIS_PORT")));
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 使用真实Jackson进行协议解析
         * @return
         * 返回值: ObjectMapper
         * 返回值说明: 测试JSON映射器
         */
        @Bean
        ObjectMapper objectMapper()
        {
            return new ObjectMapper();
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 同源请求可通过，跨源默认拒绝
         * @return
         * 返回值: CorsFilter
         * 返回值说明: 测试安全链需要的CORS过滤器
         */
        @Bean
        CorsFilter corsFilter()
        {
            return new CorsFilter(new UrlBasedCorsConfigurationSource());
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 真实Java WebSocket测试客户端，不输出认证帧
     */
    private static class Client implements WebSocket.Listener, AutoCloseable
    {
        private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        private final BlockingQueue<Integer> closes = new LinkedBlockingQueue<>();
        private final StringBuilder fragments = new StringBuilder();
        private WebSocket socket;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 合并网络文本分片并继续请求下一帧
         * @param webSocket 网络连接
         * @param data 当前分片
         * @param last 是否最后一片
         * @return
         * 返回值: CompletionStage
         * 返回值说明: 已消费当前分片
         */
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
        {
            fragments.append(data);
            if (last)
            {
                messages.add(fragments.toString());
                fragments.setLength(0);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 捕获固定关闭码，不保留关闭原文
         * @param webSocket 已关闭连接
         * @param statusCode 关闭码
         * @param reason 关闭提示，不记录
         * @return
         * 返回值: CompletionStage
         * 返回值说明: 已完成关闭回调
         */
        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
        {
            closes.add(statusCode);
            return CompletableFuture.completedFuture(null);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 使用真实网络发送标准协议帧
         * @param messageType 认证或心跳枚举
         * @param data 认证或心跳数据
         * @return
         * 返回值: void
         * 返回值说明: 等待发送完成，不输出报文
         */
        void send(WebSocketMessageType messageType, Object data)
        {
            try
            {
                socket.sendText(new ObjectMapper().writeValueAsString(
                        WebSocketMessageVo.create(messageType, data)), true).join();
            }
            catch (Exception e)
            {
                throw new AssertionError("测试消息发送失败");
            }
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 最多等待五秒接收服务端消息，防止测试永久等待
         * @return
         * 返回值: JsonNode
         * 返回值说明: 已解析服务端信封
         */
        JsonNode next() throws Exception
        {
            String body = messages.poll(5, TimeUnit.SECONDS);
            assertThat(body).as("应收到服务端消息").isNotNull();
            return new ObjectMapper().readTree(body);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 清理测试客户端网络连接
         * @return
         * 返回值: void
         * 返回值说明: 已关闭客户端连接
         */
        @Override
        public void close()
        {
            if (socket != null)
            {
                socket.abort();
            }
        }
    }
}
