package com.zero.framework.websocket;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.ip.IpUtils;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 管理后台在线消息发送入口，所有节点均经Redis订阅投递，不提供公网推送接口
 */
@Slf4j
@Service
public class AdminWebSocketService implements MessageListener
{
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final AdminWebSocketHandler handler;
    private final AdminWebSocketProperties properties;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 复用现有Redis连接，频道内容使用普通JSON，不改变现有缓存序列化
     * @param factory Redis连接工厂
     * @param mapper 项目JSON映射器
     * @param handler 本节点连接管理
     * @param properties 环境及大小限制
     */
    public AdminWebSocketService(RedisConnectionFactory factory, ObjectMapper mapper,
            AdminWebSocketHandler handler, AdminWebSocketProperties properties)
    {
        this.redis = new StringRedisTemplate(factory);
        this.mapper = mapper;
        this.handler = handler;
        this.properties = properties;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 发送builder组装的业务消息，接收人为空时广播，否则向指定用户所有有效连接发送
     * @param message 统一业务消息，接收人由可信业务代码确定，不接受浏览器任意指定
     * @return
     * 返回值: String
     * 返回值说明: 发布消息ID；非法接收人拒绝，不能回退广播
     */
    public String sendMsg(WebSocketMessageVo message)
    {
        if (message == null)
        {
            throw new IllegalArgumentException("WebSocket消息不能为空");
        }
        Long target = message.getToAdminId();
        if (target == null)
        {
            return broadcast(message);
        }
        return sendToUser(target, message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 向指定用户所有有效登录会话及标签页发送在线事件，调用方先完成业务权限校验
     * @param userId 接收用户ID，必须为正数
     * @param message 统一业务消息，不能为空；数据须完成权限过滤，禁止包含凭证
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID，不代表浏览器已收到
     */
    public String sendToUser(Long userId, WebSocketMessageVo message)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("接收用户不能为空");
        }
        return sendToUsers(Set.of(userId), message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 合并重复用户后跨节点发送，不为离线用户保存消息
     * @param userIds 接收用户集合，不能为空且各ID必须为正数
     * @param message 统一业务消息，不能为空；数据须完成权限过滤，禁止包含凭证
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID，失败不记录正文
     */
    public String sendToUsers(Collection<Long> userIds, WebSocketMessageVo message)
    {
        if (userIds == null || userIds.isEmpty() || userIds.stream().anyMatch(id -> id == null || id <= 0))
        {
            throw new IllegalArgumentException("接收用户必须是有效用户ID");
        }
        return publish(false, new LinkedHashSet<>(userIds), message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 向所有节点的有效连接广播，业务调用方必须确认载荷适用于所有登录用户
     * @param message 统一业务消息，不能为空；数据须完成权限过滤，禁止包含凭证
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID，不承诺补发或可靠送达
     */
    public String broadcast(WebSocketMessageVo message)
    {
        return publish(true, Set.of(), message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 强退指定登录会话，目标会话仅在服务端Redis信封内传递，不下发浏览器
     * @param userId 已从登录缓存确认的用户ID
     * @param sessionId 被强退的登录会话编号，不是JWT
     * @return
     * 返回值: String
     * 返回值说明: 已发布的退出消息ID，不承诺在线送达
     */
    public String logoutSession(Long userId, String sessionId)
    {
        if (userId == null || userId <= 0 || sessionId == null || sessionId.isBlank())
        {
            throw new IllegalArgumentException("强退目标不能为空");
        }
        return publish(false, Set.of(userId), WebSocketMessageVo.builder()
                .msgType(WebSocketMessageType.LOGIN_OUT).build(), sessionId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 创建唯一信封后只发布Redis，避免发布节点重复发送；成功仅表示Redis接受发布
     * @param broadcast 是否全部连接
     * @param userIds 已校验的接收用户集合
     * @param source 统一消息，发送时重新生成编号、时间并冻结数据快照
     * @return
     * 返回值: String
     * 返回值说明: 消息ID；Redis或序列化失败转为不含载荷的通用异常
     */
    private String publish(boolean broadcast, Set<Long> userIds, WebSocketMessageVo source)
    {
        return publish(broadcast, userIds, source, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 在统一信封内附加可选的强退会话范围，其余发送校验与普通消息一致
     * @param broadcast 是否广播
     * @param userIds 目标用户集合
     * @param source 浏览器消息
     * @param sessionId 强退目标会话，其它消息必须为空
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID
     */
    private String publish(boolean broadcast, Set<Long> userIds, WebSocketMessageVo source, String sessionId)
    {
        if (source == null || source.getMsgType() == null || !source.getMsgType().isBusinessEvent())
        {
            throw new IllegalArgumentException("只能推送有效业务事件");
        }
        // 显式发送方法与VO接收人不能矛盾，避免把定向消息误广播或发送给其它用户。
        Long target = source.getToAdminId();
        if (target != null)
        {
            if (broadcast || userIds.size() != 1 || !userIds.contains(target))
            {
                throw new IllegalArgumentException("消息接收人与发送范围不一致");
            }
        }
        if (source.getFromAdminId() != null && source.getFromAdminId() <= 0)
        {
            throw new IllegalArgumentException("发送用户必须是有效用户ID");
        }
        // 系统通知没有枚举展示默认值，缺少单条配置必须在发布前明确拒绝，不能静默丢失。
        if (source.getMsgType() == WebSocketMessageType.SYSTEM_NOTICE && source.getDisplayOptions() == null)
        {
            throw new IllegalArgumentException("系统通知必须配置展示参数");
        }
        if (source.getDisplayOptions() != null) {
            if (source.getMsgType() != WebSocketMessageType.SYSTEM_NOTICE)
                throw new IllegalArgumentException("仅系统通知允许配置展示形式");
            source.getDisplayOptions().validate();
        }
        WebSocketMessageType info = source.getMsgType();
        String forwardUrl = source.getForwardUrl() == null ? "" : source.getForwardUrl().trim();
        if (!forwardUrl.isEmpty() && (!forwardUrl.startsWith("/") || forwardUrl.startsWith("//")
                || forwardUrl.contains("\\") || forwardUrl.chars().anyMatch(Character::isISOControl)))
        {
            throw new IllegalArgumentException("跳转地址必须是站内路由");
        }
        try
        {
            WebSocketMessageVo message = WebSocketMessageVo.builder()
                    .id(java.util.UUID.randomUUID().toString()).sendTime(System.currentTimeMillis())
                    .msgType(info).msgData(mapper.valueToTree(source.getMsgData())).displayOptions(source.getDisplayOptions())
                    .senderIp(IpUtils.getLocalIpAddress())
                    .fromAdminId(source.getFromAdminId())
                    .toAdminId(!broadcast && userIds.size() == 1 ? userIds.iterator().next() : null)
                    .forwardUrl(forwardUrl).build();
            if (mapper.writeValueAsBytes(message).length > properties.maxTextBytes())
            {
                throw new IllegalArgumentException("WebSocket消息超过大小限制");
            }
            redis.convertAndSend(properties.channel(), mapper.writeValueAsString(new Dispatch(broadcast, userIds, message, sessionId)));
            return message.getId();
        }
        catch (IllegalArgumentException e)
        {
            // 序列化异常可能含业务数据，不能透传第三方异常原文。
            if ("WebSocket消息超过大小限制".equals(e.getMessage()))
            {
                throw e;
            }
            throw new ServiceException("WebSocket消息发送失败");
        }
        catch (Exception e)
        {
            log.warn("WebSocket消息发布失败");
            throw new ServiceException("WebSocket消息发送失败");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 订阅线程仅解析和提交有界任务，不直接向慢连接发送，不再次发布Redis
     * @param message Redis环境频道消息
     * @param pattern Redis订阅匹配信息，不用于业务身份识别
     * @return
     * 返回值: void
     * 返回值说明: 无效消息忽略且不打印原文，合法消息交给本节点连接管理器
     */
    @Override
    public void onMessage(Message message, byte[] pattern)
    {
        try
        {
            Dispatch dispatch = mapper.readValue(message.getBody(), Dispatch.class);
            if (dispatch.message() == null || dispatch.userIds() == null
                    || dispatch.message().getMsgType() == null
                    || !dispatch.message().getMsgType().isBusinessEvent()
                    || dispatch.message().getId() == null || dispatch.message().getId().isBlank()
                    || (dispatch.broadcast() ? !dispatch.userIds().isEmpty() : dispatch.userIds().isEmpty())
                    || (dispatch.message().getToAdminId() != null && (dispatch.broadcast()
                            || dispatch.userIds().size() != 1 || !dispatch.userIds().contains(dispatch.message().getToAdminId())))
                    || dispatch.userIds().stream().anyMatch(id -> id == null || id <= 0))
            {
                return;
            }
            if (dispatch.message().getMsgType() == WebSocketMessageType.SYSTEM_NOTICE
                    && dispatch.message().getDisplayOptions() == null) return;
            if (dispatch.message().getDisplayOptions() != null) {
                if (dispatch.message().getMsgType() != WebSocketMessageType.SYSTEM_NOTICE) return;
                dispatch.message().getDisplayOptions().validate();
            }
            String body = mapper.writeValueAsString(dispatch.message());
            if (dispatch.sessionId() != null && (dispatch.sessionId().isBlank() || dispatch.broadcast()
                    || dispatch.userIds().size() != 1 || dispatch.message().getMsgType() != WebSocketMessageType.LOGIN_OUT))
            {
                return;
            }
            if (body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= properties.maxTextBytes())
            {
                if (dispatch.message().getMsgType() == WebSocketMessageType.LOGIN_OUT)
                {
                    handler.dispatchLogout(dispatch.broadcast(), dispatch.userIds(), dispatch.sessionId(), body);
                }
                else
                {
                    handler.dispatch(dispatch.broadcast(), dispatch.userIds(), body);
                }
            }
        }
        catch (Exception e)
        {
            log.warn("WebSocket订阅消息处理失败，已忽略该消息");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 仅用于服务端Redis分发，接收人字段不得作为浏览器信封返回
     * @param broadcast 是否广播
     * @param userIds 目标用户，广播时为空集合
     * @param message 浏览器信封
     * @param sessionId 可选强退目标会话，仅服务端使用
     */
    public record Dispatch(boolean broadcast, Set<Long> userIds, WebSocketMessageVo message, String sessionId)
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/17
         * 说明: 普通业务消息不限定登录会话，兼容既有分发构造方式
         * @param broadcast 是否广播
         * @param userIds 用户范围
         * @param message 浏览器信封
         */
        public Dispatch(boolean broadcast, Set<Long> userIds, WebSocketMessageVo message)
        {
            this(broadcast, userIds, message, null);
        }
    }
}
