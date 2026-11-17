package com.zero.framework.websocket;

import java.util.UUID;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 统一WebSocket消息对象，连接成功响应、心跳和业务消息使用同一结构；收发用户信息仅为业务元数据，不作为客户端登录身份依据
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageVo
{
    /** 消息唯一编号，由发送服务生成，不能用客户端提供的编号替代。 */
    private String id;
    /** 服务端发送时间，Unix毫秒时间戳。 */
    private long sendTime;
    /** 发送节点IP，由发送服务自动获取本机网卡地址并覆盖调用方输入；仅用于排查问题，不查询公网出口。 */
    @Builder.Default
    private String senderIp = "";
    /** 发送用户ID，对应sys_user.user_id；可空，非空必须大于0，仅表示业务来源，不用于认证或授权。 */
    private Long fromAdminId;
    /** 接收用户ID，对应sys_user.user_id；sendMsg中null表示广播，非空必须大于0。 */
    private Long toAdminId;
    /** 事件编号、中文说明、展示形式和消息级别，统一由服务端枚举维护。 */
    private WebSocketMessageType msgType;
    /** 业务数据，支持文本、对象、列表或null，禁止包含Token等凭证；首帧认证使用独立的token字段。 */
    private Object msgData;
    /** 可选站内跳转路由，空字符串表示无跳转，不自动导航。 */
    @Builder.Default
    private String forwardUrl = "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 创建统一消息，自动设置编号和发送时间，可选来源及接收人默认null，登录会话不写入消息
     * @param msgType 连接成功响应、心跳或业务事件，不能为空
     * @param msgData 对应数据，可为空
     * @return
     * 返回值: WebSocketMessageVo
     * 返回值说明: 包含msgType、msgData及可选跳转路由的消息
     */
    public static WebSocketMessageVo create(WebSocketMessageType msgType, Object msgData)
    {
        if (msgType == null)
        {
            throw new IllegalArgumentException("WebSocket业务类型不能为空");
        }
        return builder().id(UUID.randomUUID().toString()).sendTime(System.currentTimeMillis())
                .msgType(msgType).msgData(msgData).build();
    }
}
