package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 外部JSON接口常量
 *
 * 集中维护外部匿名接口的配置项，避免控制器、服务和初始化脚本分散硬编码。
 */
public class ExternalApiConstants
{
    /**
     * 外部JSON接口总开关配置key，true-开启匿名访问能力，false或未配置-关闭外部数据返回。
     */
    public static final String EXTERNAL_ENABLE = "sys.api.externalEnable";
}
