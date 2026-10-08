package com.zero.packageconfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 00:36
 * 说明: Boot Admin监控服务包路径配置，统一维护启动扫描、应用名称、日志目录和启动提示文案
 */
public class PackageProperties
{
    /**
     * Boot Admin监控服务基础包名，重命名根包时优先修改该常量
     */
    public static final String BASE_PACKAGE = "com.zero";

    /**
     * Spring Boot组件扫描基础包
     */
    public static final String SCAN_BASE_PACKAGES = BASE_PACKAGE;

    /**
     * Boot Admin服务注册到Nacos和展示时使用的应用名
     */
    public static final String APPLICATION_NAME = "zero-boot-admin";

    /**
     * Boot Admin服务日志目录，避免重命名服务后日志仍写入旧目录
     */
    public static final String LOG_PATH = "logs/zeroBootAdmin";

    /**
     * 启动成功提示文案
     */
    public static final String START_SUCCESS_MESSAGE = "ZERO Boot Admin 启动成功";

    /**
     * 配置文件读取基础包名的属性key
     */
    public static final String BASE_PACKAGE_PROPERTY = "zero.package.base-package";

    /**
     * 配置文件读取应用名的属性key
     */
    public static final String APPLICATION_NAME_PROPERTY = "zero.boot-admin.application-name";

    /**
     * 配置文件读取日志目录的属性key
     */
    public static final String LOG_PATH_PROPERTY = "zero.boot-admin.log-path";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 00:36
     * 说明: 启动Spring容器前写入服务级系统属性，让YAML和logback等配置统一从本类取值
     * @return
     * 返回值: void
     * 返回值说明: 写入缺省系统属性，不覆盖启动参数或环境变量已传入的同名配置
     */
    public static void applySystemProperties()
    {
        setPropertyIfAbsent(BASE_PACKAGE_PROPERTY, BASE_PACKAGE);
        setPropertyIfAbsent(APPLICATION_NAME_PROPERTY, APPLICATION_NAME);
        setPropertyIfAbsent(LOG_PATH_PROPERTY, LOG_PATH);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 00:36
     * 说明: 设置系统属性默认值，允许外部启动参数覆盖服务内置配置
     * @param key 系统属性key
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
