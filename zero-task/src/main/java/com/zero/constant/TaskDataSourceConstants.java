package com.zero.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 报表服务数据源名称常量，必须与 spring.datasource.dynamic.datasource 下的数据源 key 保持一致
 */
public class TaskDataSourceConstants
{
    /**
     * 报表主数据源，默认承载报表业务表、统计表和后续报表配置表
     */
    public static final String REPORT = "zeroBiz";

    /**
     * 后台管理数据源，仅在报表业务明确需要读取后台基础数据时使用
     */
    public static final String ADMIN = "zeroAdmin";
}
