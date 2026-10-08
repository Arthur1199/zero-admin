package com.zero.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:05
 * 说明: 报表服务跨服务调用请求头常量，统一维护后台管理员上下文和服务来源标识
 */
public final class ReportHeaderConstants
{
    /**
     * 登录令牌请求头名称，跨服务调用受保护接口时原样透传
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * 服务来源请求头名称，用于下游服务识别调用方
     */
    public static final String SOURCE_SERVICE = "X-Source-Service";

    /**
     * 报表服务自身来源标识，写入 X-Source-Service 请求头
     */
    public static final String REPORT_SOURCE_SERVICE = "zero-report";

    /**
     * 后台操作管理员ID请求头名称，通常由管理后台或网关写入
     */
    public static final String ADMIN_ID = "adminId";

    /**
     * 后台操作管理员账号请求头名称，通常由管理后台或网关写入
     */
    public static final String ADMIN_NAME = "adminName";

    /**
     * 后台操作管理员是否为超级管理员请求头名称，支持 true 或 1 表示管理员
     */
    public static final String IS_ADMIN = "isAdmin";

    /**
     * 后台操作管理员昵称请求头名称，可能包含中文，需要按 UTF-8 解码
     */
    public static final String ADMIN_NICK_NAME = "adminNickName";

    /**
     * 后台操作管理员所属公司名称请求头名称，可能包含中文，需要按 UTF-8 解码
     */
    public static final String ADMIN_COMPANY_NAME = "adminCompanyName";

    /**
     * 后台操作管理员所有角色ID请求头名称，多个角色ID使用英文逗号分隔
     */
    public static final String ADMIN_ROLE_IDS = "adminRoleIds";

    /**
     * 客户端真实IP请求头名称，用于 report 记录后台用户来源IP
     */
    public static final String CLIENT_IP = "clientIp";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 02:05
     * 说明: 常量类不允许实例化，避免被误当作业务组件注入
     */
    private ReportHeaderConstants()
    {
    }
}
