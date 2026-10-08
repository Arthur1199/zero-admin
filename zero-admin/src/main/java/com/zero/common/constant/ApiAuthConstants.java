package com.zero.common.constant;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 03:49
 * 说明: API谷歌验证配置常量，集中维护状态值和谷歌验证码开关值，避免管理接口、验证匹配和SQL初始化分散硬编码
 *
 * API谷歌验证配置常量
 */
public class ApiAuthConstants
{
    /**
     * API请求方式通配配置，表示同一接口路径下所有请求方式都需要按该配置处理
     */
    public static final String METHOD_ALL = "ALL";

    /**
     * API请求方式GET
     */
    public static final String METHOD_GET = "GET";

    /**
     * API请求方式POST
     */
    public static final String METHOD_POST = "POST";

    /**
     * API请求方式PUT
     */
    public static final String METHOD_PUT = "PUT";

    /**
     * API请求方式DELETE
     */
    public static final String METHOD_DELETE = "DELETE";

    /**
     * API请求方式PATCH
     */
    public static final String METHOD_PATCH = "PATCH";

    /**
     * API正常状态，只有该状态的配置才能参与后续谷歌验证匹配
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * API禁用状态，保留配置但不参与后续谷歌验证
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * API删除状态，后台删除时使用软删除，避免历史配置直接物理丢失
     */
    public static final Integer STATUS_DELETED = -1;

    /**
     * 需要谷歌验证码，后续接口调用方可据此增加二次校验
     */
    public static final Integer NEED_GOOGLE_YES = 1;

    /**
     * 不需要谷歌验证码，默认值，普通接口按常规权限控制处理
     */
    public static final Integer NEED_GOOGLE_NO = 0;
}
