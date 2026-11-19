package com.zero.packageconfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件服务包扫描配置。
 */
public class PackageProperties
{
    public static final String BASE_PACKAGE = "com.zero";

    public static final String SCAN_BASE_PACKAGES = BASE_PACKAGE;

    public static final String FEIGN_CLIENTS_BASE_PACKAGES = BASE_PACKAGE;

    public static final String MAPPER_SCAN = BASE_PACKAGE + ".**.mapper";

    public static final String TYPE_ALIASES_PACKAGE = BASE_PACKAGE + ".**.domain";

    public static final String BASE_PACKAGE_PROPERTY = "zero.package.base-package";

    public static final String TYPE_ALIASES_PACKAGE_PROPERTY = "zero.package.type-aliases-package";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在文件服务启动阶段写入包扫描相关系统属性。
     * @return
     * 返回值: void
     * 返回值说明: 设置后续扫描所需的系统属性
     */
    public static void applySystemProperties()
    {
        setPropertyIfAbsent(BASE_PACKAGE_PROPERTY, BASE_PACKAGE);
        setPropertyIfAbsent(TYPE_ALIASES_PACKAGE_PROPERTY, TYPE_ALIASES_PACKAGE);
    }

    private static void setPropertyIfAbsent(String key, String value)
    {
        if (System.getProperty(key) == null)
        {
            System.setProperty(key, value);
        }
    }
}
