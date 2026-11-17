package com.zero.framework.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.common.constant.CacheConstants;
import com.zero.common.enums.webSocket.WebSocketDisplayType;
import com.zero.common.enums.webSocket.WebSocketMessageLevel;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.framework.web.service.TokenService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 隔离验证首帧认证、多连接精确移除、过期和基础设施异常，不接触真实用户
 */
class AdminWebSocketTest
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenService tokens = mock(TokenService.class);
    private final AdminWebSocketLease leases = mock(AdminWebSocketLease.class);
    private final AdminWebSocketHandler handler = new AdminWebSocketHandler(properties(), tokens, leases, mapper, new SyncTaskExecutor());

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 释放测试连接和维护线程
     * @return
     * 返回值: void
     * 返回值说明: 不遗留测试线程
     */
    @AfterEach
    void cleanup()
    {
        handler.stop();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 已撤销会话仍能收到退出事件，单会话强退不能影响同用户其它会话或其它用户
     * @return
     * 返回值: void
     * 返回值说明: 普通事件仍不能发送给已失效会话
     */
    @Test
    void logoutTargetsOnlyBoundSessionAfterRevocation() throws Exception
    {
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        when(leases.renew(anyString(), anyString())).thenReturn(true);
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        List<String> other = new ArrayList<>();
        var sockets = List.of(socket("logout-first", first), socket("logout-second", second), socket("logout-other", other));
        var users = List.of(user(2L, "logout-a"), user(2L, "logout-b"), user(3L, "logout-c"));
        for (int i = 0; i < sockets.size(); i++)
        {
            when(tokens.authenticateWebSocketToken("isolated-test-token")).thenReturn(users.get(i));
            when(tokens.getWebSocketLoginUser(users.get(i).getToken())).thenReturn(users.get(i));
            handler.afterConnectionEstablished(sockets.get(i));
            auth(sockets.get(i));
        }
        when(tokens.getWebSocketLoginUser("logout-a")).thenReturn(null);
        handler.dispatchLogout(false, Set.of(2L), "logout-a", "logout-event");
        assertThat(first).hasSize(2).last().isEqualTo("logout-event");
        assertThat(second).hasSize(1);
        assertThat(other).hasSize(1);
        verify(sockets.get(0)).close(argThat(status -> status.getCode() == 4401));
        handler.dispatch(false, Set.of(2L), "ordinary-event");
        assertThat(first).hasSize(2);
        assertThat(second).hasSize(2);
        when(tokens.getWebSocketLoginUser("logout-b")).thenReturn(null);
        handler.dispatchLogout(false, Set.of(2L), null, "logout-all-user-sessions");
        assertThat(second).hasSize(3);
        assertThat(other).hasSize(1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 同账号多个连接均接收，关闭旧连接后新连接继续接收，客户端身份字段无效
     * @return
     * 返回值: void
     * 返回值说明: 断言精确移除和服务端身份绑定
     */
    @Test
    void multipleConnectionsAndExactRemoval() throws Exception
    {
        LoginUser user = user(2L, "test-session");
        when(tokens.authenticateWebSocketToken("isolated-test-token")).thenReturn(user);
        when(tokens.getWebSocketLoginUser("test-session")).thenReturn(user);
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        when(leases.renew(anyString(), anyString())).thenReturn(true);
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        WebSocketSession a = socket("first", first);
        WebSocketSession b = socket("second", second);
        handler.afterConnectionEstablished(a);
        auth(a);
        assertThat(mapper.readTree(first.get(0)).has("version")).isFalse();
        handler.afterConnectionEstablished(b);
        auth(b);
        handler.dispatch(false, Set.of(2L), "event-one");
        assertThat(first).hasSize(2);
        assertThat(second).hasSize(2);
        handler.afterConnectionClosed(a, CloseStatus.NORMAL);
        handler.dispatch(false, Set.of(2L), "event-two");
        assertThat(first).hasSize(2);
        assertThat(second).hasSize(3);
        verify(tokens, never()).verifyToken(any());
        verify(leases, times(1)).release(eq("test-session"), anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 首帧未认证时无法接收广播，错误凭证直接关闭且不发送消息
     * @return
     * 返回值: void
     * 返回值说明: 无效凭证关闭码为4401
     */
    @Test
    void unauthenticatedConnectionsCannotReceiveEvents() throws Exception
    {
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("pending", messages);
        handler.afterConnectionEstablished(socket);
        handler.dispatch(true, Set.of(), "private-event");
        assertThat(messages).isEmpty();
        auth(socket);
        assertThat(messages).isEmpty();
        verify(socket).close(argThat(status -> status.getCode() == 4401));
        verifyNoInteractions(leases);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 会话失效后发送前再次核对，不投递排队中的业务消息
     * @return
     * 返回值: void
     * 返回值说明: 失效连接关闭且只有认证成功响应
     */
    @Test
    void expiredSessionIsCheckedBeforeDelivery() throws Exception
    {
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "expired"));
        when(tokens.getWebSocketLoginUser("expired")).thenReturn(user(2L, "expired"), (LoginUser) null);
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("expired", messages);
        handler.afterConnectionEstablished(socket);
        auth(socket);
        handler.dispatch(true, Set.of(), "private-event");
        assertThat(messages).hasSize(1);
        verify(socket).close(argThat(status -> status.getCode() == 4401));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: Redis异常不能被当作有效身份，也不能误判为凭证错误导致前端强制退出
     * @return
     * 返回值: void
     * 返回值说明: 临时异常使用可重试关闭码1013
     */
    @Test
    void redisFailureFailsClosedWithoutLeakingException() throws Exception
    {
        when(tokens.authenticateWebSocketToken(anyString())).thenThrow(new IllegalStateException("sensitive-test-detail"));
        WebSocketSession socket = socket("unavailable", new ArrayList<>());
        handler.afterConnectionEstablished(socket);
        auth(socket);
        verify(socket).close(argThat(status -> status.getCode() == 1013 && !status.getReason().contains("sensitive")));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 待认证连接限额、认证期限、二进制及超大文本均有独立边界保护
     * @return
     * 返回值: void
     * 返回值说明: 拒绝额外待认证连接并清理异常连接
     */
    @Test
    void pendingLimitAndInvalidPayloads() throws Exception
    {
        WebSocketSession first = socket("first", new ArrayList<>());
        WebSocketSession extra = socket("extra", new ArrayList<>());
        handler.afterConnectionEstablished(first);
        handler.afterConnectionEstablished(extra);
        verify(extra).close(argThat(status -> status.getCode() == 4429));
        handler.handleBinaryMessage(first, new BinaryMessage(new byte[] {1}));
        verify(first).close(CloseStatus.NOT_ACCEPTABLE);
        WebSocketSession huge = socket("huge", new ArrayList<>());
        handler.afterConnectionEstablished(huge);
        handler.handleTextMessage(huge, new TextMessage("中".repeat(24000)));
        verify(huge).close(CloseStatus.TOO_BIG_TO_PROCESS);
        WebSocketSession malformed = socket("malformed", new ArrayList<>());
        handler.afterConnectionEstablished(malformed);
        handler.handleTextMessage(malformed, new TextMessage("{broken"));
        verify(malformed).close(CloseStatus.BAD_DATA);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 全局租约拒绝时不建立身份索引，不能接收广播
     * @return
     * 返回值: void
     * 返回值说明: 达到连接上限使用4429并且不发送AUTH_OK
     */
    @Test
    void globalConnectionLimitRejectsAdmission() throws Exception
    {
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "full-session"));
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("full", messages);
        handler.afterConnectionEstablished(socket);
        auth(socket);
        handler.dispatch(true, Set.of(), "event");
        assertThat(messages).isEmpty();
        verify(socket).close(argThat(status -> status.getCode() == 4429));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 排队期间登录可能过期，实际出队时必须重新核对，不能按提交任务时的身份发送
     * @return
     * 返回值: void
     * 返回值说明: 过期后排队事件不写入连接
     */
    @Test
    void queuedDeliveryRechecksSessionAtExecution() throws Exception
    {
        List<Runnable> tasks = new ArrayList<>();
        var queued = new AdminWebSocketHandler(properties(), tokens, leases, mapper, tasks::add);
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "queued-session"));
        when(tokens.getWebSocketLoginUser("queued-session")).thenReturn(user(2L, "queued-session"), (LoginUser) null);
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("queued", messages);
        try
        {
            queued.afterConnectionEstablished(socket);
            queued.handleTextMessage(socket, new TextMessage("{\"token\":\"test\"}"));
            queued.dispatch(true, Set.of(), "must-not-deliver");
            assertThat(tasks).hasSize(1);
            tasks.get(0).run();
            assertThat(messages).hasSize(1);
            verify(socket).close(argThat(status -> status.getCode() == 4401));
        }
        finally
        {
            queued.stop();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 有界发送队列满时不得回到订阅线程发送，必须关闭对应连接且不抛出载荷
     * @return
     * 返回值: void
     * 返回值说明: 拒绝任务使用可重试关闭码，未发送业务事件
     */
    @Test
    void rejectedSenderDoesNotFallBackToSynchronousDelivery() throws Exception
    {
        var rejected = new AdminWebSocketHandler(properties(), tokens, leases, mapper,
                task -> { throw new org.springframework.core.task.TaskRejectedException("isolated-test"); });
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "rejected-session"));
        when(tokens.getWebSocketLoginUser("rejected-session")).thenReturn(user(2L, "rejected-session"));
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("rejected", messages);
        try
        {
            rejected.afterConnectionEstablished(socket);
            rejected.handleTextMessage(socket, new TextMessage("{\"token\":\"test\"}"));
            rejected.dispatch(true, Set.of(), "must-not-deliver");
            assertThat(messages).hasSize(1);
            verify(socket).close(argThat(status -> status.getCode() == 1013));
        }
        finally
        {
            rejected.stop();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 申请连接名额期间发生会话失效时，不允许发送认证成功或建立用户索引
     * @return
     * 返回值: void
     * 返回值说明: 无业务消息且精确释放已申请名额
     */
    @Test
    void revokedSessionDuringAdmissionCannotAuthenticate() throws Exception
    {
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "revoked"));
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("revoked", messages);
        handler.afterConnectionEstablished(socket);
        auth(socket);
        handler.dispatch(true, Set.of(), "must-not-deliver");
        assertThat(messages).isEmpty();
        verify(socket).close(argThat(status -> status.getCode() == 4401));
        verify(leases).release(eq("revoked"), anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 认证超时代表通信延迟，只允许重试，不得向浏览器返回登录失效关闭码
     * @return
     * 返回值: void
     * 返回值说明: 清扫超时和迟到首帧均返回1013，不调用JWT认证
     */
    @Test
    void authenticationTimeoutDoesNotInvalidateLogin() throws Exception
    {
        WebSocketSession pending = socket("timeout", new ArrayList<>());
        handler.afterConnectionEstablished(pending);
        var entries = (java.util.Map<?, ?>) ReflectionTestUtils.getField(handler, "connections");
        ReflectionTestUtils.setField(entries.get("timeout"), "created", System.currentTimeMillis() - 10000);
        handler.sweep();
        verify(pending).close(argThat(status -> status.getCode() == 1013));
        WebSocketSession late = socket("late", new ArrayList<>());
        handler.afterConnectionEstablished(late);
        ReflectionTestUtils.setField(entries.get("late"), "created", System.currentTimeMillis() - 10000);
        auth(late);
        verify(late).close(argThat(status -> status.getCode() == 1013));
        verifyNoInteractions(tokens);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 申请Redis名额返回时再次检查认证期限，避免延迟结果重新激活超时连接
     * @return
     * 返回值: void
     * 返回值说明: 超时名额得到释放，未发送AUTH_OK
     */
    @Test
    void delayedLeaseResultCannotCompleteExpiredAuthentication() throws Exception
    {
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "delayed"));
        when(leases.acquire(anyString(), anyString())).thenAnswer(invocation -> {
            var entries = (java.util.Map<?, ?>) ReflectionTestUtils.getField(handler, "connections");
            ReflectionTestUtils.setField(entries.get("delayed"), "created", System.currentTimeMillis() - 10000);
            return true;
        });
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("delayed", messages);
        handler.afterConnectionEstablished(socket);
        auth(socket);
        assertThat(messages).isEmpty();
        verify(socket).close(argThat(status -> status.getCode() == 1013));
        verify(leases).release(eq("delayed"), anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 最后一次会话读取发生延迟时，也不能越过首帧认证期限后返回AUTH_OK
     * @return
     * 返回值: void
     * 返回值说明: 按临时超时关闭并释放名额，不强制清理浏览器登录态
     */
    @Test
    void delayedFinalSessionReadCannotCompleteExpiredAuthentication() throws Exception
    {
        LoginUser user = user(2L, "slow-session");
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user);
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        when(tokens.getWebSocketLoginUser("slow-session")).thenAnswer(invocation -> {
            var entries = (java.util.Map<?, ?>) ReflectionTestUtils.getField(handler, "connections");
            ReflectionTestUtils.setField(entries.get("slow-session"), "created", System.currentTimeMillis() - 10000);
            return user;
        });
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("slow-session", messages);
        handler.afterConnectionEstablished(socket);
        auth(socket);
        assertThat(messages).isEmpty();
        verify(socket).close(argThat(status -> status.getCode() == 1013));
        verify(leases).release(eq("slow-session"), anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 两个并发AUTH即使都已开始验签，也不能让同一连接重复占名额或同时注册两个身份
     * @return
     * 返回值: void
     * 返回值说明: 仅一次认证成功，重复认证关闭并清空用户索引
     */
    @Test
    void concurrentAuthenticationCannotSwitchIdentity() throws Exception
    {
        var barrier = new java.util.concurrent.CyclicBarrier(2);
        when(tokens.authenticateWebSocketToken(anyString())).thenAnswer(invocation -> {
            String sessionId = invocation.getArgument(0);
            barrier.await(5, java.util.concurrent.TimeUnit.SECONDS);
            return user("first-token".equals(sessionId) ? 2L : 3L, sessionId);
        });
        when(tokens.getWebSocketLoginUser(anyString())).thenAnswer(invocation -> {
            String sessionId = invocation.getArgument(0);
            return user("first-token".equals(sessionId) ? 2L : 3L, sessionId);
        });
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        List<String> messages = new ArrayList<>();
        WebSocketSession socket = socket("parallel", messages);
        handler.afterConnectionEstablished(socket);
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try
        {
            List<java.util.concurrent.Future<?>> results = new ArrayList<>();
            for (String token : List.of("first-token", "second-token"))
            {
                results.add(executor.submit(() -> handler.handleTextMessage(socket,
                        new TextMessage("{\"token\":\"" + token + "\"}"))));
            }
            for (var result : results)
            {
                result.get(5, java.util.concurrent.TimeUnit.SECONDS);
            }
            assertThat(messages.stream().filter(body -> body.contains("创建连接成功")).count()).isEqualTo(1);
            assertThat((java.util.Map<?, ?>) ReflectionTestUtils.getField(handler, "users")).isEmpty();
            verify(leases, times(1)).acquire(anyString(), anyString());
            verify(socket).close(CloseStatus.POLICY_VIOLATION);
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 停止期间并发握手不能在连接清理完成后重新进入本节点连接表
     * @return
     * 返回值: void
     * 返回值说明: 直接返回节点关闭状态，不申请名额
     */
    @Test
    void stoppingNodeRejectsNewConnections() throws Exception
    {
        handler.stop();
        WebSocketSession socket = socket("after-stop", new ArrayList<>());
        handler.afterConnectionEstablished(socket);
        verify(socket).close(CloseStatus.GOING_AWAY);
        assertThat((java.util.Map<?, ?>) ReflectionTestUtils.getField(handler, "connections")).isEmpty();
        verifyNoInteractions(leases, tokens);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 新增TokenService入口使用真实验签和缓存校验，不刷新会话，也不吞掉Redis异常
     * @return
     * 返回值: void
     * 返回值说明: 伪造、过期及空过期时间会话均无效
     */
    @Test
    void tokenValidationDoesNotRefreshLoginCache()
    {
        RedisCache cache = mock(RedisCache.class);
        TokenService service = new TokenService();
        ReflectionTestUtils.setField(service, "redisCache", cache);
        ReflectionTestUtils.setField(service, "secret", "isolated-websocket-test-secret");
        LoginUser user = user(2L, "token-session");
        String jwt = service.getExistingToken(user);
        when(cache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + user.getToken())).thenReturn(user);
        assertThat(service.authenticateWebSocketToken(jwt)).isSameAs(user);
        verify(cache, never()).setCacheObject(anyString(), any(), anyInt(), any());
        assertThat(service.authenticateWebSocketToken("forged-test-token")).isNull();
        user.setExpireTime(null);
        assertThat(service.authenticateWebSocketToken(jwt)).isNull();
        user.setExpireTime(System.currentTimeMillis() - 1);
        assertThat(service.authenticateWebSocketToken(jwt)).isNull();
        when(cache.getCacheObject(anyString())).thenThrow(new IllegalStateException("redis unavailable"));
        assertThatThrownBy(() -> service.authenticateWebSocketToken(jwt)).isInstanceOf(IllegalStateException.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 统一消息携带枚举元信息，Redis往返保留业务编号，不输出旧嵌套结构
     * @return
     * 返回值: void
     * 返回值说明: 统一消息在浏览器及Redis中均使用新协议
     */
    @Test
    void unifiedMessagesRoundTripThroughRedisEnvelope() throws Exception
    {
        var message = WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER,
                mapper.readTree("{\"orderId\":\"10001\"}"));
        String json = mapper.writeValueAsString(message);
        var body = mapper.readTree(json);
        assertThat(body.path("msgType").path("messageType").asInt()).isEqualTo(2);
        assertThat(body.path("msgType").path("desc").asText()).isEqualTo("刷新浏览器");
        assertThat(body.path("msgData").path("orderId").asText()).isEqualTo("10001");
        assertThat(body.has("type")).isFalse();
        assertThat(body.has("data")).isFalse();
        assertThat(body.path("sendTime").asLong()).isPositive();
        assertThat(body.has("version")).isFalse();
        assertThat(body.path("msgType").has("msgWeight")).isFalse();
        assertThat(body.path("msgType").has("openSendMsg")).isFalse();
        assertThat(mapper.readValue(json, WebSocketMessageVo.class).getMsgType()).isEqualTo(WebSocketMessageType.REFRESH_BROWSER);
        var dispatch = new AdminWebSocketService.Dispatch(false, Set.of(2L), message);
        var decoded = mapper.readValue(mapper.writeValueAsString(dispatch), AdminWebSocketService.Dispatch.class);
        assertThat(decoded.message().getMsgType()).isEqualTo(WebSocketMessageType.REFRESH_BROWSER);
        assertThat(decoded.userIds()).containsExactly(2L);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 空枚举与未知标识必须在发送前拒绝，解析错误不能携带原始输入
     * @return
     * 返回值: void
     * 返回值说明: 无效参数不访问Redis，不输出输入值
     */
    @Test
    void invalidEnumsFailBeforeRedisPublication()
    {
        var factory = mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        var service = new AdminWebSocketService(factory, mapper, handler, properties());
        assertThatThrownBy(() -> service.sendToUser(2L, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.sendToUsers(Set.of(2L), null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.broadcast(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.broadcast(WebSocketMessageVo.builder().build())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebSocketMessageVo.create(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebSocketMessageType.fromJson(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebSocketMessageType.fromJson(99999)).isInstanceOf(IllegalArgumentException.class);
        verify(factory, never()).getConnection();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 外部只能提供事件编号，伪造的说明、展示形式和级别不能修改服务端枚举
     * @return
     * 返回值: void
     * 返回值说明: 缺失和未知编号均拒绝，合法编号还原服务端元信息
     */
    @Test
    void businessMetadataCannotBeOverridden() throws Exception
    {
        for (WebSocketMessageType info : WebSocketMessageType.values())
        {
            assertThat(mapper.readValue(mapper.writeValueAsString(info), WebSocketMessageType.class)).isSameAs(info);
        }
        var info = mapper.readValue("{\"messageType\":2,\"desc\":\"forged\",\"displayType\":\"MESSAGE_BOX\",\"messageLevel\":\"ERROR\"}", WebSocketMessageType.class);
        assertThat(info).isSameAs(WebSocketMessageType.REFRESH_BROWSER);
        assertThat(info.getDesc()).isEqualTo("刷新浏览器");
        assertThat(info.getDisplayType()).isEqualTo(WebSocketMessageType.REFRESH_BROWSER.getDisplayType());
        assertThat(info.getMessageLevel()).isEqualTo(WebSocketMessageType.REFRESH_BROWSER.getMessageLevel());
        assertThatThrownBy(() -> mapper.readValue("{}", WebSocketMessageType.class)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> WebSocketMessageType.fromJson(99999)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebSocketMessageVo.create(null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 业务发送不能伪造认证、心跳及错误，也不能携带外站跳转地址
     * @return
     * 返回值: void
     * 返回值说明: 非法消息不发布Redis
     */
    @Test
    void businessPublicationRejectsControlMessagesAndUnsafeRoutes()
    {
        var factory = mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        var service = new AdminWebSocketService(factory, mapper, handler, properties());
        for (WebSocketMessageType info : WebSocketMessageType.values())
        {
            if (!info.isBusinessEvent())
            {
                assertThatThrownBy(() -> service.broadcast(WebSocketMessageVo.create(info, null)))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
        for (String url : List.of("https://example.test", "//example.test", "/\\example.test", "/path\nnext"))
        {
            var message = WebSocketMessageVo.builder().msgType(WebSocketMessageType.REFRESH_BROWSER).forwardUrl(url).build();
            assertThatThrownBy(() -> service.sendToUser(2L, message)).isInstanceOf(IllegalArgumentException.class);
        }
        verify(factory, never()).getConnection();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 首帧要求文本Token，旧协议及缺失凭证不能进入登录缓存校验
     * @return
     * 返回值: void
     * 返回值说明: 非法协议关闭，空凭证直接关闭，不发送认证失败消息
     */
    @Test
    void invalidProtocolAndEmptyCredentialsAreRejected() throws Exception
    {
        int index = 0;
        for (String body : List.of("{}", "{\"type\":\"AUTH\"}",
                "{\"msgType\":{\"messageType\":\"3\"}}",
                "{\"msgType\":{\"messageType\":3.1}}",
                "{\"msgType\":{\"messageType\":99999}}"))
        {
            var socket = socket("invalid-" + index++, new ArrayList<>());
            handler.afterConnectionEstablished(socket);
            handler.handleTextMessage(socket, new TextMessage(body));
            verify(socket).close(argThat(status -> status.getCode() == 4401));
        }
        List<String> messages = new ArrayList<>();
        var empty = socket("empty-credentials", messages);
        handler.afterConnectionEstablished(empty);
        handler.handleTextMessage(empty, new TextMessage("{}"));
        assertThat(messages).isEmpty();
        verify(empty).close(argThat(status -> status.getCode() == 4401));
        verifyNoInteractions(tokens, leases);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: builder消息按接收人路由，新增字段经Redis快照保留，不修改调用方对象
     * @return
     * 返回值: void
     * 返回值说明: 定向及广播正确，非法或矛盾的接收人不发布
     */
    @Test
    void builderMessagesRouteAndPreserveMetadata() throws Exception
    {
        var factory = mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        var service = new AdminWebSocketService(factory, mapper, handler, properties());
        var redis = mock(org.springframework.data.redis.core.StringRedisTemplate.class);
        ReflectionTestUtils.setField(service, "redis", redis);
        var source = WebSocketMessageVo.builder().msgType(WebSocketMessageType.REFRESH_BROWSER)
                .msgData("你好色彩").senderIp("test-node").fromAdminId(3L).toAdminId(2L).forwardUrl("").build();
        String id = service.sendMsg(source);
        var body = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(redis).convertAndSend(eq(properties().channel()), body.capture());
        var dispatch = mapper.readValue((String) body.getValue(), AdminWebSocketService.Dispatch.class);
        assertThat(dispatch.broadcast()).isFalse();
        assertThat(dispatch.userIds()).containsExactly(2L);
        assertThat(dispatch.message().getToAdminId()).isEqualTo(2L);
        assertThat(dispatch.message().getFromAdminId()).isEqualTo(3L);
        assertThat(dispatch.message().getSenderIp()).isEqualTo(com.zero.common.utils.ip.IpUtils.getLocalIpAddress());
        assertThat(dispatch.message().getMsgData()).isEqualTo("你好色彩");
        assertThat(dispatch.message().getId()).isEqualTo(id);
        assertThat(source.getId()).isNull();
        assertThat(source.getSenderIp()).isEqualTo("test-node");
        assertThatThrownBy(() -> service.broadcast(source)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.sendToUser(3L, source)).isInstanceOf(IllegalArgumentException.class);
        for (Long target : List.of(0L, -1L))
        {
            source.setToAdminId(target);
            assertThatThrownBy(() -> service.sendMsg(source)).isInstanceOf(IllegalArgumentException.class);
        }
        source.setToAdminId(null);
        source.setFromAdminId(0L);
        assertThatThrownBy(() -> service.sendMsg(source)).isInstanceOf(IllegalArgumentException.class);
        source.setFromAdminId(-1L);
        assertThatThrownBy(() -> service.sendMsg(source)).isInstanceOf(IllegalArgumentException.class);
        source.setFromAdminId(null);
        verifyNoMoreInteractions(redis);
        source.setToAdminId(null);
        service.sendMsg(source);
        verify(redis, times(2)).convertAndSend(eq(properties().channel()), body.capture());
        var broadcast = mapper.readValue((String) body.getValue(), AdminWebSocketService.Dispatch.class);
        assertThat(broadcast.broadcast()).isTrue();
        assertThat(broadcast.message().getToAdminId()).isNull();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 认证成功后收到重复Token或不支持的协议帧时直接关闭，不能发送错误类型或重做认证
     * @return
     * 返回值: void
     * 返回值说明: 仅保留首次连接成功响应，协议错误不会继续投递业务
     */
    @Test
    void authenticatedProtocolErrorsCloseWithoutResponse() throws Exception
    {
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "protocol-session"));
        when(tokens.getWebSocketLoginUser("protocol-session")).thenReturn(user(2L, "protocol-session"));
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        int index = 0;
        for (String body : List.of("{\"token\":\"repeat-test\"}",
                "{\"msgType\":{\"messageType\":99999}}",
                "{\"msgType\":{\"messageType\":2}}"))
        {
            List<String> messages = new ArrayList<>();
            var socket = socket("protocol-" + index++, messages);
            handler.afterConnectionEstablished(socket);
            auth(socket);
            handler.handleTextMessage(socket, new TextMessage(body));
            assertThat(messages).hasSize(1);
            assertThat(mapper.readTree(messages.get(0)).path("msgType").path("messageType").asInt()).isZero();
            verify(socket).close(argThat(status -> status.getCode() == 1002 || status.getCode() == 1008));
        }
        verify(tokens, times(3)).authenticateWebSocketToken(anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 成功响应写入期间到达的Redis事件必须保留，并等待成功响应完成后才写入连接
     * @return
     * 返回值: void
     * 返回值说明: 不丢失注册窗口事件，业务报文始终晚于认证成功报文
     */
    @Test
    void eventDuringAuthenticationResponseWaitsAndIsDelivered() throws Exception
    {
        var senderPool = java.util.concurrent.Executors.newSingleThreadExecutor();
        var authPool = java.util.concurrent.Executors.newSingleThreadExecutor();
        var ackEntered = new java.util.concurrent.CountDownLatch(1);
        var allowAck = new java.util.concurrent.CountDownLatch(1);
        var taskEntered = new java.util.concurrent.CountDownLatch(1);
        var tasks = new java.util.concurrent.CopyOnWriteArrayList<java.util.concurrent.Future<?>>();
        var guarded = new AdminWebSocketHandler(properties(), tokens, leases, mapper,
                task -> tasks.add(senderPool.submit(() -> { taskEntered.countDown(); task.run(); })));
        when(tokens.authenticateWebSocketToken(anyString())).thenReturn(user(2L, "ready-window"));
        when(tokens.getWebSocketLoginUser("ready-window")).thenReturn(user(2L, "ready-window"));
        when(leases.acquire(anyString(), anyString())).thenReturn(true);
        when(leases.renew(anyString(), anyString())).thenReturn(true);
        var messages = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var socket = socket("ready-window", messages);
        doAnswer(invocation -> {
            String body = ((TextMessage) invocation.getArgument(0)).getPayload();
            if (!"event-after-ack".equals(body))
            {
                ackEntered.countDown();
                assertThat(allowAck.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            }
            messages.add(body);
            return null;
        }).when(socket).sendMessage(any());
        try
        {
            guarded.afterConnectionEstablished(socket);
            var auth = authPool.submit(() -> guarded.handleTextMessage(socket, new TextMessage("{\"token\":\"test-only\"}")));
            assertThat(ackEntered.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            guarded.dispatch(false, Set.of(2L), "event-after-ack");
            assertThat(taskEntered.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(messages).isEmpty();
            allowAck.countDown();
            auth.get(5, java.util.concurrent.TimeUnit.SECONDS);
            for (var task : tasks) task.get(5, java.util.concurrent.TimeUnit.SECONDS);
            assertThat(messages).hasSize(2);
            assertThat(mapper.readTree(messages.get(0)).path("msgType").path("messageType").asInt()).isZero();
            assertThat(messages.get(1)).isEqualTo("event-after-ack");
        }
        finally
        {
            allowAck.countDown();
            guarded.stop();
            senderPool.shutdownNow();
            authPool.shutdownNow();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: Redis信封的广播标记、用户集合和VO接收人必须一致，异常信封不能扩大投递范围
     * @return
     * 返回值: void
     * 返回值说明: 无效信封忽略，合法定向信封只交给本节点目标连接
     */
    @Test
    void redisEnvelopeScopeMismatchIsIgnored() throws Exception
    {
        var targetHandler = mock(AdminWebSocketHandler.class);
        var factory = mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        var service = new AdminWebSocketService(factory, mapper, targetHandler, properties());
        var message = WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, "test-only");
        var mismatched = WebSocketMessageVo.create(WebSocketMessageType.REFRESH_BROWSER, null);
        mismatched.setToAdminId(3L);
        for (var dispatch : List.of(new AdminWebSocketService.Dispatch(true, Set.of(2L), message),
                new AdminWebSocketService.Dispatch(false, Set.of(), message),
                new AdminWebSocketService.Dispatch(false, Set.of(2L), mismatched),
                new AdminWebSocketService.Dispatch(false, Set.of(2L), message, "not-a-logout-event"),
                new AdminWebSocketService.Dispatch(true, Set.of(), WebSocketMessageVo.create(WebSocketMessageType.LOGIN_OUT, null), "session")))
        {
            var incoming = mock(org.springframework.data.redis.connection.Message.class);
            when(incoming.getBody()).thenReturn(mapper.writeValueAsBytes(dispatch));
            service.onMessage(incoming, null);
        }
        verifyNoInteractions(targetHandler);
        var incoming = mock(org.springframework.data.redis.connection.Message.class);
        when(incoming.getBody()).thenReturn(mapper.writeValueAsBytes(
                new AdminWebSocketService.Dispatch(false, Set.of(2L), message)));
        service.onMessage(incoming, null);
        verify(targetHandler).dispatch(eq(false), eq(Set.of(2L)), anyString());
        when(incoming.getBody()).thenReturn(mapper.writeValueAsBytes(
                new AdminWebSocketService.Dispatch(false, Set.of(2L),
                        WebSocketMessageVo.create(WebSocketMessageType.LOGIN_OUT, null), "selected")));
        service.onMessage(incoming, null);
        verify(targetHandler).dispatchLogout(eq(false), eq(Set.of(2L)), eq("selected"), anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 构造仅限当前测试的连接配置
     * @return
     * 返回值: AdminWebSocketProperties
     * 返回值说明: 待认证连接上限设为1以验证拒绝边界
     */
    private static AdminWebSocketProperties properties()
    {
        return new AdminWebSocketProperties("isolated:test:ws", List.of(), 5000, 30000, 90000,
                30000, 65536, 10, 1, 10000, 262144, 2, 100);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 创建不含真实个人信息的有效会话
     * @param id 测试用户ID
     * @param sessionId 测试会话编号
     * @return
     * 返回值: LoginUser
     * 返回值说明: 五分钟内有效的测试用户
     */
    static LoginUser user(Long id, String sessionId)
    {
        SysUser account = new SysUser();
        account.setUserId(id);
        account.setUserName("websocket-test");
        LoginUser user = new LoginUser(id, 1L, account, Set.of());
        user.setToken(sessionId);
        user.setExpireTime(System.currentTimeMillis() + 300000);
        return user;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 模拟容器连接并捕获输出，不使用真实网络
     * @param id 测试连接ID
     * @param messages 输出收集器
     * @return
     * 返回值: WebSocketSession
     * 返回值说明: 始终可写的隔离会话替身
     */
    private WebSocketSession socket(String id, List<String> messages) throws Exception
    {
        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.getId()).thenReturn(id);
        when(socket.isOpen()).thenReturn(true);
        when(socket.getAttributes()).thenReturn(new HashMap<>());
        doAnswer(invocation -> { messages.add(((TextMessage) invocation.getArgument(0)).getPayload()); return null; })
                .when(socket).sendMessage(any());
        return socket;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 发送首帧认证，夹带的userId不能覆盖后端解析结果
     * @param socket 待认证会话
     * @return
     * 返回值: void
     * 返回值说明: 触发处理器身份校验
     */
    private void auth(WebSocketSession socket)
    {
        handler.handleTextMessage(socket, new TextMessage("{\"token\":\"isolated-test-token\",\"userId\":999}"));
    }
}
