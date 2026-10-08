package com.zero.packageconfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 00:24
 * 说明: 报表服务包路径配置，统一维护启动扫描、Feign扫描、Mapper扫描和类型别名包名
 */
public class PackageProperties
{
    /**
     * 报表服务基础包名，重命名根包时优先修改该常量
     */
    public static final String BASE_PACKAGE = "com.zero";

    /**
     * Spring Boot 组件扫描基础包
     */
    public static final String SCAN_BASE_PACKAGES = BASE_PACKAGE;

    /**
     * OpenFeign 客户端扫描基础包
     */
    public static final String FEIGN_CLIENTS_BASE_PACKAGES = BASE_PACKAGE;

    /**
     * MyBatis Mapper 接口扫描路径
     */
    public static final String MAPPER_SCAN = BASE_PACKAGE + ".**.mapper";

    /**
     * MyBatis-Plus 实体类型别名扫描路径
     */
    public static final String TYPE_ALIASES_PACKAGE = BASE_PACKAGE + ".**.domain";

    /**
     * 配置文件读取基础包名的属性 key
     */
    public static final String BASE_PACKAGE_PROPERTY = "zero.package.base-package";

    /**
     * 配置文件读取类型别名包的属性 key
     */
    public static final String TYPE_ALIASES_PACKAGE_PROPERTY = "zero.package.type-aliases-package";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 00:24
     * 说明: 启动 Spring 容器前写入包路径系统属性，让 YAML 等非 Java 注解配置也统一从本类取值
     * @return
     * 返回值: void
     * 返回值说明: 写入缺省系统属性，不覆盖启动参数或环境变量已传入的同名配置
     */
    public static void applySystemProperties()
    {
        setPropertyIfAbsent(BASE_PACKAGE_PROPERTY, BASE_PACKAGE);
        setPropertyIfAbsent(TYPE_ALIASES_PACKAGE_PROPERTY, TYPE_ALIASES_PACKAGE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 00:24
     * 说明: 设置系统属性默认值，允许外部启动参数覆盖服务内置包路径配置
     * @param key 系统属性 key
     * @param value 系统属性默认值
     * @return
     * 返回值: void
     * 返回值说明: 仅在属性不存在时写入默认值
     */
    private static void setPropertyIfAbsent(String key, String value)
    {
        if (System.getProperty(key) == null)
        {
            System.setProperty(key, value);
        }
    }
}
