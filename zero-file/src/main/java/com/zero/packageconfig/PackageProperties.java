package com.zero.packageconfig;

/**
 * 文件服务包扫描配置。
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
