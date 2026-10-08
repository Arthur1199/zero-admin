package com.zero.common.constant;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:06
 * 说明: 登录IP白名单常量，集中维护配置key和状态值，避免登录校验、管理接口和SQL初始化分散硬编码
 *
 * 登录IP白名单常量
 */
public class IpWhiteConstants
{
    /**
     * 登录IP白名单开关配置key，true-开启，false或未配置-关闭；开启后普通账号必须命中启用白名单，admin账号绕过白名单
     */
    public static final String CHEK_IP_WHITE = "sys.account.chekIpWhite";

    /**
     * 白名单启用状态，只有该状态的IP规则才参与登录校验
     */
    public static final String STATUS_ENABLED = "1";

    /**
     * 白名单关闭状态，保留记录但不参与登录校验
     */
    public static final String STATUS_DISABLED = "0";
}
