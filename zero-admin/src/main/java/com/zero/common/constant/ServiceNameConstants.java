package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 微服务注册名称常量，集中维护 Feign 调用和服务发现使用的服务名
 */
public class ServiceNameConstants
{
    /**
     * 后台管理服务名，必须与 zero-admin 的 spring.application.name 保持一致
     */
    public static final String ZERO_ADMIN = "zeroAdmin";

    /**
     * 报表服务名，必须与 zero-report 的 spring.application.name 保持一致
     */
    public static final String ZERO_REPORT = "zero-report";

    /**
     * 文件服务名，必须与 zero-file 的 spring.application.name 保持一致
     */
    public static final String ZERO_FILE = "zero-file";
}
