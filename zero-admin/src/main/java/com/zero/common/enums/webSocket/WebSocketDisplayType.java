package com.zero.common.enums.webSocket;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: WebSocket消息展示形式，不允许服务端消息指定任意组件或HTML
 */
public enum WebSocketDisplayType
{
    /** 不弹窗。 */
    NONE,
    /** 右侧通知卡片。 */
    NOTIFICATION,
    /** 顶部短提示。 */
    MESSAGE,
    /** 居中消息弹窗。 */
    MESSAGE_BOX
}
