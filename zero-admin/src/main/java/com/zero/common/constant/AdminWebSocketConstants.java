package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 管理后台WebSocket握手路径，消息类型和业务事件分别由独立枚举维护
 */
public final class AdminWebSocketConstants
{
    /** 服务内握手路径，代理仅移除现有API前缀。 */
    public static final String PATH = "/ws/admin";

    private AdminWebSocketConstants()
    {
    }
}
