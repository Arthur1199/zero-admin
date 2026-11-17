package com.zero.common.enums.webSocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 统一管理WebSocket连接成功、心跳及业务事件，消息携带编号、中文说明、展示形式与级别
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum WebSocketMessageType
{
    /** 服务端认证完成；msgData包含心跳参数。 */
    CREATE_CONNECTION(0, "创建连接成功", WebSocketDisplayType.NONE, WebSocketMessageLevel.NORMAL),
    /** 客户端心跳请求和服务端心跳响应共用该编号，不刷新登录有效期。 */
    PING_PONG(1, "心跳检测", WebSocketDisplayType.NONE, WebSocketMessageLevel.NORMAL),
    /** 浏览器刷新事件，由全局连接工具刷新当前页面，重新获取最新用户资料及权限。 */
    REFRESH_BROWSER(2, "刷新浏览器", WebSocketDisplayType.NONE, WebSocketMessageLevel.NORMAL),
    /** 退出登录,由全局连接工具调用客户端退出登录接口 */
    LOGIN_OUT(3, "退出登录", WebSocketDisplayType.NONE, WebSocketMessageLevel.NORMAL),


    ;
    /** 稳定的事件编号，前端按该值订阅，不使用枚举序号。 */
    private final int messageType;
    /** 业务中文说明，由服务端枚举确定，不信任客户端传入的说明。 */
    private final String desc;
    /** 展示形式：NONE不展示、NOTIFICATION右侧卡片、MESSAGE顶部提示、MESSAGE_BOX居中弹窗。 */
    private final WebSocketDisplayType displayType;
    /** 消息级别：NORMAL普通、WARNING警告、ERROR错误。 */
    private final WebSocketMessageLevel messageLevel;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 定义事件编号及只读元信息，不允许运行时修改共享枚举
     * @param messageType 唯一事件编号
     * @param desc 中文说明
     * @param displayType 前端展示形式，通信控制消息必须为NONE
     * @param messageLevel 消息级别，普通、警告或错误
     */
    WebSocketMessageType(int messageType, String desc, WebSocketDisplayType displayType, WebSocketMessageLevel messageLevel)
    {
        this.messageType = messageType;
        this.desc = desc;
        this.displayType = displayType;
        this.messageLevel = messageLevel;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 只按编号还原服务端枚举，忽略外部伪造的说明、展示形式与级别
     * @param messageType JSON中明确提供的整数编号，不能为空
     * @return
     * 返回值: WebSocketMessageType
     * 返回值说明: 对应事件；缺失或未知编号拒绝，不回退为连接成功
     */
    @JsonCreator
    public static WebSocketMessageType fromJson(@JsonProperty("messageType") Integer messageType)
    {
        if (messageType != null)
        {
            for (WebSocketMessageType info : values())
            {
                if (info.messageType == messageType)
                {
                    return info;
                }
            }
        }
        throw new IllegalArgumentException("不支持的WebSocket业务类型");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 限制公开发送方法只能发送业务事件，不能向其它用户伪造连接成功或心跳响应
     * @return
     * 返回值: boolean
     * 返回值说明: 除明确列出的通信控制类型外均视为业务事件，不限制事件编号范围
     */
    @JsonIgnore
    public boolean isBusinessEvent()
    {
        return switch (this)
        {
            case CREATE_CONNECTION, PING_PONG -> false;
            default -> true;
        };
    }
}
