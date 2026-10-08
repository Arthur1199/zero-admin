package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 02:25
 * 说明: 后台服务跨服务调用请求头常量，统一维护管理员上下文和调用来源
 */
public final class ForwardHeaderConstants
{
    /**
     * 登录令牌请求头名称，调用受保护的下游服务时原样透传
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * 服务来源请求头名称，便于下游服务识别调用方
     */
    public static final String SOURCE_SERVICE = "X-Source-Service";

    /**
     * 后台管理服务来源标识
     */
    public static final String ADMIN_SOURCE_SERVICE = "zero-admin";

    /**
     * 后台操作管理员ID请求头名称
     */
    public static final String ADMIN_ID = "adminId";

    /**
     * 后台操作管理员账号请求头名称
     */
    public static final String ADMIN_NAME = "adminName";

    /**
     * 后台操作管理员是否为超级管理员请求头名称
     */
    public static final String IS_ADMIN = "isAdmin";

    /**
     * 后台操作管理员昵称请求头名称
     */
    public static final String ADMIN_NICK_NAME = "adminNickName";

    /**
     * 后台操作管理员所属公司名称请求头名称
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
     * 时间: 2026/6/28 02:25
     * 说明: 常量类不允许实例化，避免被误当成业务组件使用
     */
    private ForwardHeaderConstants()
    {
    }
}
