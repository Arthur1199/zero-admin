package com.zero.system.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.framework.websocket.AdminWebSocketService;
import com.zero.framework.websocket.WebSocketMessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 01:11
 * 说明: 处理指定会话强退、账号禁用或删除后的会话撤销，以及旧会话的跨节点退出通知。
 */
@Slf4j
@Service
public class AdminUserLogoutService
{
    /** 登录缓存SCAN每批建议数量，避免新增阻塞式全量KEYS查询。 */
    private static final int LOGIN_SCAN_COUNT = 200;
    /** 现有登录缓存，不保存或分发JWT。 */
    private final RedisCache cache;
    /** 统一跨节点在线消息入口。 */
    private final AdminWebSocketService webSocket;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 复用登录缓存和Redis消息分发
     * @param cache 登录缓存
     * @param webSocket 在线通信入口
     */
    public AdminUserLogoutService(RedisCache cache, AdminWebSocketService webSocket)
    {
        this.cache = cache;
        this.webSocket = webSocket;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 服务端先撤销指定会话，浏览器未收到通知也不能继续使用该会话
     * @param sessionId 在线列表选中的会话编号，不是JWT
     * @return
     * 返回值: void
     * 返回值说明: 不影响同账号其它独立登录会话；通知失败不回退会话撤销
     */
    public void forceLogout(String sessionId)
    {
        if (sessionId == null || sessionId.isBlank())
        {
            throw new IllegalArgumentException("会话编号不能为空");
        }
        LoginUser login = cache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + sessionId);
        cache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + sessionId);
        if (login != null)
        {
            notifySessionLogout(login.getUserId(), sessionId);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 对已经撤销的会话定向发送退出事件，保留同账号其它会话。
     * @param userId 已确认会话归属的用户 ID
     * @param sessionId 已撤销的会话编号
     * @return
     * 返回值: void
     * 返回值说明: 通知失败时记录告警，会话维持撤销状态
     */
    public void notifySessionLogout(Long userId, String sessionId)
    {
        if (userId == null || userId <= 0 || sessionId == null || sessionId.isBlank())
        {
            return;
        }
        try
        {
            webSocket.logoutSession(userId, sessionId);
        }
        catch (RuntimeException e)
        {
            log.warn("登录会话已强退，客户端退出通知发送失败");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 用户禁用、删除或谷歌解绑提交后撤销目标账号的全部会话，回滚时不执行。
     * @param userIds 已完成权限和写入结果校验的用户ID
     * @return
     * 返回值: void
     * 返回值说明: 使用SCAN分批读取登录缓存，推送或缓存异常只记录固定告警，不掩盖已提交结果
     */
    public void afterUsersUnavailable(Collection<Long> userIds)
    {
        Set<Long> targets = userIds == null ? Set.of() : userIds.stream()
                .filter(id -> id != null && id > 0).collect(Collectors.toSet());
        if (targets.isEmpty()) return;
        Runnable revoke = () -> {
            try
            {
                // SCAN避免新增阻塞Redis的全量KEYS；不续期、不输出会话或凭证。
                try (Cursor<?> cursor = cache.redisTemplate.scan(ScanOptions.scanOptions()
                        .match(CacheConstants.LOGIN_TOKEN_KEY + "*").count(LOGIN_SCAN_COUNT).build()))
                {
                    while (cursor.hasNext())
                    {
                        String key = (String) cursor.next();
                        LoginUser login = cache.getCacheObject(key);
                        if (login != null && targets.contains(login.getUserId())) cache.deleteObject(key);
                    }
                }
            }
            catch (Exception e)
            {
                log.error("账号变更已提交，登录会话撤销失败，请检查Redis");
            }
            try
            {
                webSocket.sendToUsers(targets, WebSocketMessageVo.builder()
                        .msgType(WebSocketMessageType.LOGIN_OUT).build());
            }
            catch (Exception e)
            {
                log.warn("账号变更已提交，客户端退出通知发送失败");
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive())
        {
            if (!TransactionSynchronizationManager.isSynchronizationActive())
            {
                log.error("当前事务不支持提交回调，已跳过登录会话撤销及退出通知");
                return;
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                /**
                 *
                 * 作者: Arthur 亚瑟
                 * 时间: 2026/11/17
                 * 说明: 数据提交后才撤销登录，避免回滚导致正常账号被退出
                 * @return
                 * 返回值: void
                 * 返回值说明: 撤销目标账号会话并尽力通知客户端
                 */
                @Override
                public void afterCommit() { revoke.run(); }
            });
        }
        else revoke.run();
    }
}
