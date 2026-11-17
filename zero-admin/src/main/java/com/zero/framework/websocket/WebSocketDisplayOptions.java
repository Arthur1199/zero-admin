package com.zero.framework.websocket;
import com.zero.common.enums.webSocket.WebSocketDisplayType;
import com.zero.common.enums.webSocket.WebSocketMessageLevel;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 单条系统通知展示配置，不修改共享枚举，不适用于控制事件
 */

public record WebSocketDisplayOptions(
    /** 弹窗形式，不允许NONE。 */ WebSocketDisplayType displayType,
    /** 消息级别。 */ WebSocketMessageLevel messageLevel,
    /** 展示毫秒数，0手工关闭，否则1000至3600000且为整数秒。 */ long duration)
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 校验单条展示选项
     * @return
     * 返回值: void
     * 返回值说明: 完成操作或拒绝非法参数
     */

    public void validate() {
        if (displayType == null || displayType == WebSocketDisplayType.NONE || messageLevel == null
            || duration < 0 || duration > 3600000 || duration % 1000 != 0)
            throw new IllegalArgumentException("通知展示配置无效");
    }
}
