package com.zero.system.domain.statistics;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 运行统计最小事件，不包含请求参数、响应正文和会话凭证。
 */
@Getter
@Setter
public class StatEvent
{
    /** 唯一事件编号，重试及双通道投递保持不变。 */
    private String eventId;
    /** 完成时间，Unix毫秒。 */
    private long completedAt;
    /** 服务端处理耗时，微秒。 */
    private long durationUs;
    /** HTTP请求方式。 */
    private String method;
    /** 已匹配接口使用映射模板，其余使用最长512字符的实际路径，均不含查询字符串。 */
    private String route;
    /** 接口名称，来源于映射方法。 */
    private String name;
    /** 业务模块，来源于接口模板首段。 */
    private String module;
    /** SUCCESS、BUSINESS、AUTH、SYSTEM、HTTP、UNKNOWN。 */
    private String outcome;
    /** 已认证用户ID，匿名为空。 */
    private Long userId;
    /** 是否为POST /login尝试。 */
    private boolean login;
    /** 登录最终成功生成令牌。 */
    private boolean loginSuccess;
    /** 是否达到采集时的慢请求阈值。 */
    private boolean slow;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 序列化MySQL回退事件，不包含敏感请求数据。
     * @return 可恢复的精简事件JSON
     * 返回值: String
     * 返回值说明: 可恢复的精简事件JSON
     */
    @com.alibaba.fastjson2.annotation.JSONField(serialize = false)
    public String getPayload() { return com.alibaba.fastjson2.JSON.toJSONString(this); }
}
