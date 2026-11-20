package com.zero.system.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.framework.websocket.AdminWebSocketService;
import com.zero.framework.websocket.WebSocketMessageVo;
import com.zero.system.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 用户关系和角色授权变更提交后定向通知客户端刷新，API谷歌验证修改全员通知，不保存离线消息
 */
@Slf4j
@Service
public class AdminUserRefreshService
{
    /** 跨节点消息发布入口，接收人由服务端业务流程确定。 */
    private final AdminWebSocketService webSocketService;

    /** 提交后定位变更角色的当前成员，兼容并发分配用户角色。 */
    private final SysUserRoleMapper userRoleMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 复用统一Redis推送入口，不查询本节点在线用户，避免遗漏其它节点连接
     * @param webSocketService 管理后台在线通信服务
     * @param userRoleMapper 角色成员查询
     */
    public AdminUserRefreshService(AdminWebSocketService webSocketService, SysUserRoleMapper userRoleMapper)
    {
        this.webSocketService = webSocketService;
        this.userRoleMapper = userRoleMapper;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 为角色或部门已发生变更的用户注册提交后通知，回滚时不发送
     * @param userIds 接收用户ID集合，空集合跳过；过滤无效ID并去重，不允许退化为广播
     * @return
     * 返回值: void
     * 返回值说明: 提交后尽力发送，Redis失败只记固定提示，不影响已提交业务
     */
    public void afterChanged(Collection<Long> userIds)
    {
        Set<Long> targets = validTargets(userIds);
        if (!targets.isEmpty())
        {
            afterCommit(() -> sendRefresh(targets, "你的部门或角色已变更，正在刷新页面。"));
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:04
     * 说明: 角色菜单或数据范围保存成功后，读取该角色当前成员并定向通知。
     * @param roleId 菜单授权或数据范围实际变更的角色ID
     */
    public void afterRoleAuthorizationChanged(Long roleId)
    {
        if (roleId == null || roleId <= 0)
        {
            return;
        }
        afterCommit(() -> {
            try
            {
                Set<Long> targets = validTargets(userRoleMapper.selectMembersByRoleId(roleId));
                if (!targets.isEmpty())
                {
                    sendRefresh(targets, "你的角色权限已变更，正在刷新页面。");
                }
            }
            catch (Exception e)
            {
                log.warn("角色权限修改已提交，读取刷新通知目标失败，角色ID={}", roleId);
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:04
     * 说明: 过滤并去重定向目标，无有效用户时避免发送。
     * @param userIds 接收用户ID，可为空
     * @return 可定向通知的有效用户ID
     */
    private Set<Long> validTargets(Collection<Long> userIds)
    {
        Set<Long> targets = new LinkedHashSet<>();
        if (userIds != null)
        {
            userIds.stream().filter(id -> id != null && id > 0).forEach(targets::add);
        }
        return targets;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:08
     * 说明: 发送定向刷新事件，通信失败不影响已经提交的变更。
     * @param targets 经校验的非空接收者集合
     * @param content 刷新原因
     */
    private void sendRefresh(Set<Long> targets, String content)
    {
        try
        {
            webSocketService.sendToUsers(targets, WebSocketMessageVo.builder()
                    .msgType(WebSocketMessageType.REFRESH_BROWSER)
                    .msgData(Map.of("content", content))
                    .build());
        }
        catch (Exception e)
        {
            log.warn("用户部门、角色或角色权限修改已提交，定向刷新通知发送失败");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: API谷歌验证修改提交后通知所有在线客户端重载规则，不携带配置详情或验证码
     * @return
     * 返回值: void
     * 返回值说明: 各节点有效连接收到刷新事件；推送失败不影响已保存配置，离线不补发
     */
    public void afterApiAuthChanged()
    {
        afterCommit(() -> {
            try
            {
                webSocketService.broadcast(WebSocketMessageVo.builder()
                        .msgType(WebSocketMessageType.REFRESH_BROWSER)
                        .msgData(Map.of("content", "接口谷歌验证规则已更新，正在刷新页面。"))
                        .build());
            }
            catch (Exception e)
            {
                log.warn("API谷歌验证配置已提交，客户端刷新通知发送失败");
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 复用提交后发送规则，事务回滚或不支持同步时不能提前通知客户端
     * @param send 已完成目标范围校验且自行处理推送异常的通知动作
     * @return
     * 返回值: void
     * 返回值说明: 无事务时立即执行；有事务时仅在提交成功后执行
     */
    private void afterCommit(Runnable send)
    {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isSynchronizationActive())
        {
            // 特殊事务管理器关闭同步时不能提前推送，以免客户端读取未提交的数据。
            log.warn("当前事务不支持提交回调，已跳过客户端刷新通知");
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                /**
                 *
                 * 作者: Arthur 亚瑟
                 * 时间: 2026/11/17
                 * 说明: 数据库提交成功后才通知客户端，避免刷新时读取到未提交数据
                 * @return
                 * 返回值: void
                 * 返回值说明: 尽力通知，不让推送异常影响已提交的修改
                 */
                @Override
                public void afterCommit()
                {
                    send.run();
                }
            });
        }
        else
        {
            send.run();
        }
    }
}
