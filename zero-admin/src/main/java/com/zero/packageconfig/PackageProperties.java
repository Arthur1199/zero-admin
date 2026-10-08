package com.zero.packageconfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 00:24
 * 说明: 管理后台服务包路径配置，统一维护启动扫描、Mapper扫描、类型别名和代码生成默认包名
 */
public class PackageProperties
{
    /**
     * 管理后台服务基础包名，重命名根包时优先修改该常量
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
     * MyBatis 实体类型别名扫描路径
     */
    public static final String TYPE_ALIASES_PACKAGE = BASE_PACKAGE + ".**.domain";

    /**
     * 代码生成默认业务包名
     */
    public static final String GENERATOR_PACKAGE_NAME = BASE_PACKAGE + ".system";

    /**
     * 日志级别控制使用的业务包名
     */
    public static final String LOG_PACKAGE = BASE_PACKAGE;

    /**
     * 数据源注解切点表达式，随基础包名自动调整注解完整路径
     */
    public static final String DATA_SOURCE_POINTCUT_SIGN = "@annotation(" + BASE_PACKAGE + ".common.annotation.DataSource)"
            + "|| @within(" + BASE_PACKAGE + ".common.annotation.DataSource)";

    /**
     * 配置文件读取基础包名的属性 key
     */
    public static final String BASE_PACKAGE_PROPERTY = "zero.package.base-package";

    /**
     * 配置文件读取类型别名包的属性 key
     */
    public static final String TYPE_ALIASES_PACKAGE_PROPERTY = "zero.package.type-aliases-package";

    /**
     * 配置文件读取代码生成默认包的属性 key
     */
    public static final String GENERATOR_PACKAGE_NAME_PROPERTY = "zero.package.generator-package-name";

    /**
     * 默认业务包日志级别，保持与 application.yml 中的开发期日志策略一致
     */
    private static final String DEFAULT_LOG_LEVEL = "debug";

    /**
     * Spring Boot 日志级别属性前缀
     */
    private static final String LOGGING_LEVEL_PREFIX = "logging.level.";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 00:24
     * 说明: 启动 Spring 容器前写入包路径系统属性，让 YAML、logback 等非 Java 注解配置也统一从本类取值
     * @return
     * 返回值: void
     * 返回值说明: 写入缺省系统属性，不覆盖启动参数或环境变量已传入的同名配置
     */
    public static void applySystemProperties()
    {
        setPropertyIfAbsent(BASE_PACKAGE_PROPERTY, BASE_PACKAGE);
        setPropertyIfAbsent(TYPE_ALIASES_PACKAGE_PROPERTY, TYPE_ALIASES_PACKAGE);
        setPropertyIfAbsent(GENERATOR_PACKAGE_NAME_PROPERTY, GENERATOR_PACKAGE_NAME);
        setPropertyIfAbsent(LOGGING_LEVEL_PREFIX + LOG_PACKAGE, DEFAULT_LOG_LEVEL);
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
