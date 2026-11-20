package com.zero.framework.websocket;

import java.io.IOException;
import java.util.ArrayDeque;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.framework.web.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 本节点连接、首帧认证及有效性管理，客户端不得指定身份或转发目标，不记录消息正文
 */
@Slf4j
@Component
public class AdminWebSocketHandler extends TextWebSocketHandler
{
    /** 会话失效关闭码，浏览器收到后停止重连并清理本地登录态。 */
    private static final CloseStatus UNAUTHORIZED = new CloseStatus(4401, "登录已失效");
    /** 基础设施异常可重试，不能误判为登录失效。 */
    private static final CloseStatus RETRY = new CloseStatus(1013, "通信暂不可用");
    /** 连接限额关闭码，客户端退避重试，不强制退出其它登录会话。 */
    private static final CloseStatus LIMIT = new CloseStatus(4429, "连接数达到上限");
    private final AdminWebSocketProperties properties;
    private final TokenService tokens;
    private final AdminWebSocketLease leases;
    private final ObjectMapper mapper;
    private final TaskExecutor sender;
    /** 连接表包含待认证连接，以容器连接ID定位，移除时必须同时匹配实例。 */
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    /** 用户ID→登录会话→连接ID，注册和移除由registryLock保护，读取允许并发快照。 */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ConcurrentHashMap<String, Connection>>> users = new ConcurrentHashMap<>();
    private final Object registryLock = new Object();
    /** 应用停止后拒绝新握手，避免关闭扫描结束后又加入连接。 */
    private volatile boolean stopping;
    private final ScheduledExecutorService maintenance = Executors.newScheduledThreadPool(2, task -> {
        Thread thread = new Thread(task, "admin-ws-maintenance");
        thread.setDaemon(true);
        return thread;
    });

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 注入现有登录能力及专用发送线程池，避免引入另一套登录逻辑
     * @param properties 连接限制
     * @param tokens JWT及登录缓存服务
     * @param leases 跨节点连接名额
     * @param mapper JSON映射器
     * @param sender 有界发送线程池
     */
    public AdminWebSocketHandler(AdminWebSocketProperties properties, TokenService tokens, AdminWebSocketLease leases,
            ObjectMapper mapper, @Qualifier("adminWebSocketSender") TaskExecutor sender)
    {
        this.properties = properties;
        this.tokens = tokens;
        this.leases = leases;
        this.mapper = mapper;
        this.sender = sender;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 独立清理首帧超时和批量检查会话，避免Redis阻塞导致待认证连接永久保留
     * @return
     * 返回值: void
     * 返回值说明: 启动两个独立维护任务，不使用业务Quartz任务
     */
    @PostConstruct
    public void start()
    {
        maintenance.scheduleWithFixedDelay(this::sweep, 1, 1, TimeUnit.SECONDS);
        maintenance.scheduleWithFixedDelay(this::checkSessions, properties.sessionCheckInterval(),
                properties.sessionCheckInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 握手成功仅进入待认证集合，限制未认证连接数，不信任握手路径或请求头中的用户信息
     * @param session 新建立的容器会话
     * @return
     * 返回值: void
     * 返回值说明: 接受待认证连接或在达到限额时关闭
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception
    {
        session.setTextMessageSizeLimit(properties.maxTextBytes());
        // Tomcat的阻塞发送期限需写入原生Session，普通握手attributes不会传到底层。
        if (session instanceof org.springframework.web.socket.adapter.standard.StandardWebSocketSession standard) {
            standard.getNativeSession();
            standard.getNativeSession().getUserProperties().put(
                    "org.apache.tomcat.websocket.BLOCKING_SEND_TIMEOUT", (long) properties.sendTimeout());
        }
        Connection connection = new Connection(new ConcurrentWebSocketSessionDecorator(session,
                properties.sendTimeout(), properties.sendBufferBytes()));
        boolean admitted;
        synchronized (registryLock)
        {
            admitted = !stopping && connections.values().stream().filter(item -> !item.authenticated).count()
                    < properties.maxPendingConnections();
            if (admitted)
            {
                connections.put(session.getId(), connection);
            }
        }
        if (!admitted)
        {
            session.close(stopping ? CloseStatus.GOING_AWAY : LIMIT);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 首帧仅接受Token认证，认证后仅接受心跳，不允许客户端转发消息或冒充收件人
     * @param session 容器会话
     * @param message 文本信封，认证凭证禁止记录日志
     * @return
     * 返回值: void
     * 返回值说明: 响应认证和心跳；畸形或越权协议关闭连接
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
    {
        Connection connection = connections.get(session.getId());
        if (connection == null)
        {
            return;
        }
        try
        {
            if (message.asBytes().length > properties.maxTextBytes())
            {
                close(connection, CloseStatus.TOO_BIG_TO_PROCESS);
                return;
            }
            JsonNode frame;
            try
            {
                frame = mapper.readTree(message.getPayload());
            }
            catch (IOException e)
            {
                close(connection, CloseStatus.BAD_DATA);
                return;
            }
            if (frame == null || !frame.isObject())
            {
                close(connection, CloseStatus.PROTOCOL_ERROR);
                return;
            }
            if (!connection.authenticated)
            {
                // 首帧只携带Token；认证成功前不接受业务消息，也不刷新HTTP登录有效期。
                if (System.currentTimeMillis() - connection.created >= properties.authTimeout())
                {
                    close(connection, RETRY);
                    return;
                }
                JsonNode token = frame.path("token");
                if (!token.isTextual() || token.asText().isBlank())
                {
                    close(connection, UNAUTHORIZED);
                    return;
                }
                authenticate(connection, token.textValue());
                return;
            }
            JsonNode messageType = frame.path("msgType").path("messageType");
            if (!messageType.isIntegralNumber() || !messageType.canConvertToInt())
            {
                close(connection, CloseStatus.PROTOCOL_ERROR);
                return;
            }
            WebSocketMessageType info;
            try
            {
                info = WebSocketMessageType.fromJson(messageType.intValue());
            }
            catch (IllegalArgumentException e)
            {
                close(connection, CloseStatus.PROTOCOL_ERROR);
                return;
            }
            if (info == WebSocketMessageType.PING_PONG)
            {
                connection.lastActivity = System.currentTimeMillis();
                queue(connection, 0, () -> send(connection, WebSocketMessageVo.create(WebSocketMessageType.PING_PONG, Map.of())));
            }
            else
            {
                close(connection, CloseStatus.POLICY_VIOLATION);
            }
        }
        catch (Exception e)
        {
            // 第三方解析/Redis异常可能携带原文，只记录固定提示。
            log.warn("WebSocket认证或消息处理失败");
            close(connection, RETRY);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 验证JWT后申请全局连接名额，申请后再次核对会话和期限；同一连接不能并发切换身份
     * @param connection 待认证连接
     * @param token 首帧JWT，仅在当前调用使用
     * @return
     * 返回值: void
     * 返回值说明: 建立服务端身份索引或关闭无效连接，不续期登录缓存
     */
    private void authenticate(Connection connection, String token) throws IOException
    {
        if (token == null || token.isBlank())
        {
            close(connection, UNAUTHORIZED);
            return;
        }
        LoginUser user = tokens.authenticateWebSocketToken(token);
        if (user == null)
        {
            close(connection, UNAUTHORIZED);
            return;
        }
        synchronized (connection)
        {
            if (connection.closed)
            {
                return;
            }
            if (connection.authenticated)
            {
                close(connection, CloseStatus.POLICY_VIOLATION);
                return;
            }
            connection.userId = user.getUserId();
            connection.sessionId = user.getToken();
            if (!leases.acquire(connection.sessionId, connection.leaseId))
            {
                close(connection, LIMIT);
                return;
            }
            connection.leased.set(true);
            // 申请Redis名额期间连接可能已被超时任务关闭，补偿释放，避免占用到租约过期。
            if (connection.closed)
            {
                releaseLease(connection);
                return;
            }
            if (System.currentTimeMillis() - connection.created >= properties.authTimeout())
            {
                close(connection, RETRY);
                return;
            }
            // 先登记目标连接，再核对登录缓存。登记前发生的强退由缓存检查捕获，登记后到达的通知进入队列。
            // 发送任务会等当前连接锁释放，认证成功前不会投递已入队的消息。
            synchronized (registryLock)
            {
                if (connection.closed || !connection.socket.isOpen())
                {
                    return;
                }
                users.computeIfAbsent(connection.userId, key -> new ConcurrentHashMap<>())
                        .computeIfAbsent(connection.sessionId, key -> new ConcurrentHashMap<>())
                        .put(connection.socket.getId(), connection);
            }
            LoginUser current = tokens.getWebSocketLoginUser(connection.sessionId);
            // Redis读取可能阻塞，校验完成后继续遵守认证期限和关闭状态。
            if (connection.closed || System.currentTimeMillis() - connection.created >= properties.authTimeout())
            {
                close(connection, RETRY);
                return;
            }
            if (current == null || !connection.userId.equals(current.getUserId()))
            {
                close(connection, UNAUTHORIZED);
                return;
            }
            send(connection, WebSocketMessageVo.create(WebSocketMessageType.CREATE_CONNECTION,
                    Map.of("heartbeatInterval", properties.heartbeatInterval(), "idleTimeout", properties.idleTimeout())));
            synchronized (registryLock)
            {
                if (!connection.closed && connection.socket.isOpen())
                {
                    connection.lastActivity = System.currentTimeMillis();
                    connection.authenticated = true;
                }
            }
            leases.markOnline(connection.sessionId, connection.leaseId);
            if (connection.closed) leases.release(connection.sessionId, connection.leaseId);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: Redis订阅回调只提交发送任务，任务执行时重新核对登录缓存；目标集合不下发浏览器
     * @param broadcast 是否向本节点全部有效连接发送
     * @param userIds 经过业务层过滤的用户ID集合
     * @param body 已序列化且经过大小限制的浏览器信封
     * @return
     * 返回值: void
     * 返回值说明: 异步投递本节点连接，不进行跨节点直接调用或Redis重复发布
     */
    public void dispatch(boolean broadcast, Set<Long> userIds, String body)
    {
        int bytes = body.getBytes(StandardCharsets.UTF_8).length;
        users.forEach((userId, sessions) -> {
            if (broadcast || userIds.contains(userId))
            {
                sessions.values().forEach(items -> items.values().forEach(connection -> queue(connection, bytes, () -> {
                    if (valid(connection))
                    {
                        connection.socket.sendMessage(new TextMessage(body));
                    }
                })));
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 仅向首帧认证绑定过的连接发送退出通知，允许登录缓存已被撤销，不放宽普通业务投递校验
     * @param broadcast 是否全部用户
     * @param userIds 服务端确定的用户范围
     * @param sessionId 强退指定会话，null表示目标用户的全部会话
     * @param body 已由订阅端确认类型为LOGIN_OUT的消息
     * @return
     * 返回值: void
     * 返回值说明: 尽力发送后关闭连接，客户端也可通过4401兜底退出
     */
    public void dispatchLogout(boolean broadcast, Set<Long> userIds, String sessionId, String body)
    {
        int bytes = body.getBytes(StandardCharsets.UTF_8).length;
        users.forEach((userId, sessions) -> {
            if (broadcast || userIds.contains(userId))
            {
                sessions.forEach((id, items) -> {
                    if (sessionId == null || sessionId.equals(id))
                    {
                        items.values().forEach(connection -> queue(connection, bytes, () -> {
                            if (connection.authenticated && !connection.closed && connection.socket.isOpen())
                            {
                                connection.socket.sendMessage(new TextMessage(body));
                                close(connection, UNAUTHORIZED);
                            }
                        }));
                    }
                });
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 实际发送前读取最新登录缓存，不使用认证时缓存的旧登录信息
     * @param connection 已认证连接
     * @return
     * 返回值: boolean
     * 返回值说明: 有效且仍打开时返回true，失效时关闭并返回false
     */
    private boolean valid(Connection connection)
    {
        if (connection.closed || !connection.authenticated || !connection.socket.isOpen())
        {
            return false;
        }
        LoginUser current = tokens.getWebSocketLoginUser(connection.sessionId);
        if (current == null || !connection.userId.equals(current.getUserId()))
        {
            close(connection, UNAUTHORIZED);
            return false;
        }
        if (!leases.renew(connection.sessionId, connection.leaseId))
        {
            close(connection, RETRY);
            return false;
        }
        return true;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 每个登录会话只读取一次Redis，核对失败关闭该会话连接，异常不会终止后续维护周期
     * @return
     * 返回值: void
     * 返回值说明: 核对并续租活跃连接名额，不续期登录状态
     */
    void checkSessions()
    {
        Map<String, LoginUser> checked = new HashMap<>();
        for (Connection connection : connections.values())
        {
            if (!connection.authenticated || connection.closed)
            {
                continue;
            }
            try
            {
                if (!checked.containsKey(connection.sessionId))
                {
                    checked.put(connection.sessionId, tokens.getWebSocketLoginUser(connection.sessionId));
                }
                LoginUser current = checked.get(connection.sessionId);
                if (current == null || !connection.userId.equals(current.getUserId()))
                {
                    close(connection, UNAUTHORIZED);
                }
                else if (!leases.renew(connection.sessionId, connection.leaseId))
                {
                    close(connection, RETRY);
                }
            }
            catch (Exception e)
            {
                close(connection, RETRY);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 按首帧认证时间、客户端有效消息时间及正在发送时长清理连接，超时不等同于登录失效
     * @return
     * 返回值: void
     * 返回值说明: 超时关闭连接；业务消息持续推送不能代替客户端心跳
     */
    void sweep()
    {
        long now = System.currentTimeMillis();
        connections.values().forEach(connection -> {
            if (!connection.authenticated && now - connection.created >= properties.authTimeout())
            {
                close(connection, RETRY);
            }
            else if (connection.authenticated && now - connection.lastActivity >= properties.idleTimeout())
            {
                close(connection, CloseStatus.SESSION_NOT_RELIABLE);
            }
            else if (connection.socket.getTimeSinceSendStarted() > properties.sendTimeout())
            {
                close(connection, CloseStatus.SESSION_NOT_RELIABLE);
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 按本节点入队顺序排队，每连接仅提交一个消费任务；条数或字节超限时关闭慢连接
     * @param connection 发送目标
     * @param messageBytes 业务信封UTF-8字节数，心跳为0但仍受待发送条数限制
     * @param action 不包含日志输出的发送动作
     * @return
     * 返回值: void
     * 返回值说明: 异步发送或关闭连接，失败不泄露消息正文
     */
    private void queue(Connection connection, int messageBytes, SendAction action)
    {
        boolean submit = false;
        boolean overflow;
        // 队列锁与连接写锁分离，慢连接不能阻塞Redis订阅线程入队。
        synchronized (connection.sendLock)
        {
            if (connection.closed) return;
            overflow = connection.pending.size() >= properties.sendQueueCapacity()
                    || messageBytes > properties.sendBufferBytes() - connection.pendingBytes;
            if (!overflow)
            {
                connection.pending.addLast(new PendingSend(messageBytes, action));
                connection.pendingBytes += messageBytes;
                if (!connection.draining)
                {
                    connection.draining = true;
                    submit = true;
                }
            }
        }
        if (overflow)
        {
            close(connection, RETRY);
            return;
        }
        if (submit)
        {
            try { sender.execute(() -> drain(connection)); }
            catch (RuntimeException e) { close(connection, RETRY); }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 唯一消费任务按FIFO发送，避免多个线程竞争写锁造成消息乱序；认证完成后才执行
     * @param connection 本节点连接，关闭后不再投递排队消息
     * @return
     * 返回值: void
     * 返回值说明: 队列为空时释放消费标记，异常关闭当前连接
     */
    private void drain(Connection connection)
    {
        while (true)
        {
            PendingSend next;
            synchronized (connection.sendLock)
            {
                if (connection.closed || connection.pending.isEmpty())
                {
                    connection.draining = false;
                    return;
                }
                next = connection.pending.removeFirst();
                connection.pendingBytes -= next.bytes();
            }
            try
            {
                synchronized (connection)
                {
                    if (!connection.closed) next.action().run();
                }
            }
            catch (Exception e)
            {
                close(connection, RETRY);
                return;
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 通过并发保护的连接发送协议响应，禁止记录信封内容
     * @param connection 目标连接
     * @param message 服务端协议响应
     * @return
     * 返回值: void
     * 返回值说明: 写入连接，失败交给调用方关闭
     */
    private void send(Connection connection, WebSocketMessageVo message) throws IOException
    {
        message.setSenderIp(com.zero.common.utils.ip.IpUtils.getLocalIpAddress());
        connection.socket.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 幂等移除指定实例，先摘除索引再关闭，不能误删同账号其它连接
     * @param connection 需要关闭的连接
     * @param status 不包含凭证和业务数据的关闭原因
     * @return
     * 返回值: void
     * 返回值说明: 释放本地连接及Redis租约；释放失败由租约到期回收
     */
    private void close(Connection connection, CloseStatus status)
    {
        synchronized (registryLock)
        {
            if (connection.closed)
            {
                return;
            }
            connection.closed = true;
            connections.remove(connection.socket.getId(), connection);
            if (connection.userId != null && connection.sessionId != null)
            {
                var sessions = users.get(connection.userId);
                if (sessions != null)
                {
                    var items = sessions.get(connection.sessionId);
                    if (items != null)
                    {
                        items.remove(connection.socket.getId(), connection);
                        if (items.isEmpty())
                        {
                            sessions.remove(connection.sessionId, items);
                        }
                    }
                    if (sessions.isEmpty())
                    {
                        users.remove(connection.userId, sessions);
                    }
                }
            }
        }
        synchronized (connection.sendLock)
        {
            connection.pending.clear();
            connection.pendingBytes = 0;
        }
        try
        {
            connection.socket.close(status);
        }
        catch (Exception e)
        {
            log.debug("WebSocket连接已关闭或无法写入关闭帧");
        }
        releaseLease(connection);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 并发关闭和认证超时补偿只释放一次租约，Redis失败时等待自动到期
     * @param connection 需要释放名额的连接
     * @return
     * 返回值: void
     * 返回值说明: 不影响其它连接，也不记录会话编号
     */
    private void releaseLease(Connection connection)
    {
        if (connection.leased.getAndSet(false))
        {
            try
            {
                leases.release(connection.sessionId, connection.leaseId);
            }
            catch (Exception e)
            {
                log.debug("WebSocket连接名额等待租约到期回收");
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 本端协议仅支持文本消息，二进制直接关闭
     * @param session 容器会话
     * @param message 二进制载荷，不解析或记录
     * @return
     * 返回值: void
     * 返回值说明: 关闭不支持的连接并清理索引
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message)
    {
        Connection connection = connections.get(session.getId());
        if (connection != null)
        {
            close(connection, CloseStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 传输错误仅清理对应连接，不记录可能含消息原文的异常
     * @param session 出错的容器会话
     * @param exception 传输异常，不输出原文
     * @return
     * 返回值: void
     * 返回值说明: 关闭并释放该连接
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
    {
        Connection connection = connections.get(session.getId());
        if (connection != null)
        {
            close(connection, RETRY);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 容器关闭回调幂等清理，旧连接不能移除新连接
     * @param session 已关闭会话
     * @param status 容器关闭状态
     * @return
     * 返回值: void
     * 返回值说明: 清理索引及租约
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
    {
        Connection connection = connections.get(session.getId());
        if (connection != null)
        {
            close(connection, status);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 应用停止时释放连接与维护线程，浏览器可重连到其它健康节点
     * @return
     * 返回值: void
     * 返回值说明: 本节点停止接收和维护连接
     */
    @PreDestroy
    public void stop()
    {
        synchronized (registryLock)
        {
            stopping = true;
        }
        maintenance.shutdownNow();
        connections.values().forEach(connection -> close(connection, CloseStatus.GOING_AWAY));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 服务端连接状态，不保存JWT，不共享到Redis
     */
    private static final class Connection
    {
        /** 待发队列独立加锁，写连接时不占用该锁。 */
        private final Object sendLock = new Object();
        /** 当前连接已入队的发送动作，按FIFO消费。 */
        private final ArrayDeque<PendingSend> pending = new ArrayDeque<>();
        /** 等待发送的业务载荷字节数，由sendLock保护。 */
        private int pendingBytes;
        /** 是否已有消费任务，由sendLock保护。 */
        private boolean draining;
        private final ConcurrentWebSocketSessionDecorator socket;
        private final long created = System.currentTimeMillis();
        private final String leaseId = UUID.randomUUID().toString();
        private volatile long lastActivity = created;
        private volatile Long userId;
        private volatile String sessionId;
        private volatile boolean authenticated;
        private final java.util.concurrent.atomic.AtomicBoolean leased = new java.util.concurrent.atomic.AtomicBoolean();
        private volatile boolean closed;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 创建待认证连接状态，所有身份字段必须在JWT验证后赋值
         * @param socket 已包装并发发送保护的连接
         */
        private Connection(ConcurrentWebSocketSessionDecorator socket)
        {
            this.socket = socket;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 本地待发送动作及体积，不持有JWT，不进入Redis
     * @param bytes 信封UTF-8字节数，心跳为0
     * @param action 已完成范围筛选的发送动作
     */
    private record PendingSend(int bytes, SendAction action) {}

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 发送线程执行的可失败动作，统一关闭异常连接
     */
    @FunctionalInterface
    private interface SendAction
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 执行一次受连接状态保护的发送
         * @return
         * 返回值: void
         * 返回值说明: 发送失败抛出异常交给队列包装层处理
         */
        void run() throws Exception;
    }
}
