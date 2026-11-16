package com.zero.system.service.impl;

import com.zero.system.domain.SysConfig;
import com.zero.system.mapper.SysConfigMapper;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.IStatisticsSwitchService;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 21:34
 * 说明: 复用后台参数缓存读取统计开关，隔离配置存储故障，避免影响原业务及Redis失败时的事件回退。
 */
@Service
public class StatisticsSwitchServiceImpl implements IStatisticsSwitchService
{
    /** 仅输出固定故障描述，不记录参数内容、连接信息或底层异常正文。 */
    private static final Logger log = LoggerFactory.getLogger(StatisticsSwitchServiceImpl.class);
    /** 配置故障日志最短间隔，毫秒，避免每个请求重复刷日志。 */
    private static final long LOG_INTERVAL_MILLIS = 30000;
    /** 后台参数服务，后台修改和刷新缓存后，下一次读取可获取最新值。 */
    private final ISysConfigService configService;
    /** Redis故障时直接查询配置表，不重复实现参数缓存写入。 */
    private final SysConfigMapper configMapper;
    /** 最近一次成功读取的开关值，首次读取失败时默认关闭。 */
    private volatile boolean lastEnabled;
    /** 最近读取状态，未成功读取前不能视为正常配置。 */
    private volatile boolean available;
    /** 限频日志时间，支持请求及后台任务并发读取。 */
    private final AtomicLong loggedAt = new AtomicLong();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 注入现有参数服务及MySQL回退读取能力。
     * @param configService 后台参数服务
     * @param configMapper 参数数据访问
     */
    public StatisticsSwitchServiceImpl(ISysConfigService configService, SysConfigMapper configMapper)
    {
        this.configService = configService;
        this.configMapper = configMapper;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 每次复用现有参数缓存，不另设本地缓存延迟后台修改；Redis异常时仍允许从MySQL判断开关。
     * @return 是否开启统计
     * 返回值: boolean
     * 返回值说明: 存储同时不可用时返回最近有效值，并将读取状态标为异常
     */
    @Override
    public boolean isEnabled()
    {
        String value;
        try
        {
            value = configService.selectConfigByKey(CONFIG_KEY);
        }
        catch (Exception cacheFailure)
        {
            try
            {
                SysConfig query = new SysConfig();
                query.setConfigKey(CONFIG_KEY);
                SysConfig config = configMapper.selectConfig(query);
                value = config == null ? null : config.getConfigValue();
            }
            catch (Exception databaseFailure)
            {
                available = false;
                long now = System.currentTimeMillis();
                long previous = loggedAt.get();
                if (now - previous >= LOG_INTERVAL_MILLIS && loggedAt.compareAndSet(previous, now))
                {
                    log.warn("运行统计开关读取失败，暂按本实例最近有效值运行");
                }
                return lastEnabled;
            }
        }
        lastEnabled = value != null && "true".equalsIgnoreCase(value.trim());
        available = true;
        return lastEnabled;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 读取最近开关查询状态，不触发额外Redis或MySQL访问。
     * @return 最近读取是否成功
     * 返回值: boolean
     * 返回值说明: true表示已读取参数，false表示使用故障兜底值
     */
    @Override
    public boolean isAvailable()
    {
        return available;
    }
}
