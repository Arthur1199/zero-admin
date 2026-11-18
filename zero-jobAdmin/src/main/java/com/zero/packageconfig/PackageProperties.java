package com.zero.packageconfig;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 15:50
 * 说明: 调度中心组件扫描边界，避免依赖中的执行器或其它服务组件进入容器
 */
public final class PackageProperties {

    /** 调度中心启动入口、配置及迁入源码的扫描根包。 */
    public static final String SCAN_BASE_PACKAGES = "com.zero";

    private PackageProperties() {
    }
}
