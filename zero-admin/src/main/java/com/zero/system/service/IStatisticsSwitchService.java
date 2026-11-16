package com.zero.system.service;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 21:34
 * 说明: 运行统计开关统一入口，由后台参数sys_config维护，不接受YAML开关覆盖。
 */
public interface IStatisticsSwitchService
{
    /** 运行统计总开关，true开启，false、缺失、空值及非法值关闭。 */
    String CONFIG_KEY = "sys.statistics.enabled";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 优先复用参数缓存；缓存异常时读取MySQL，两者均失败时保留本实例最近有效值。
     * @return 是否开启运行统计
     * 返回值: boolean
     * 返回值说明: true开启；首次读取即失败时默认关闭，不阻断业务请求
     */
    boolean isEnabled();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 获取最近一次开关读取状态，供统计页面区分关闭与配置读取故障。
     * @return 最近一次是否成功读取参数
     * 返回值: boolean
     * 返回值说明: false表示使用最近有效值或首次失败默认值，不表示管理员主动关闭
     */
    boolean isAvailable();
}
