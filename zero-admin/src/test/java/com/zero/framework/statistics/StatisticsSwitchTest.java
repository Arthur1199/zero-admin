package com.zero.framework.statistics;

import com.zero.common.constant.CacheConstants;
import com.zero.common.core.redis.RedisCache;
import com.zero.system.domain.SysConfig;
import com.zero.system.mapper.SysConfigMapper;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.IStatisticsSwitchService;
import com.zero.system.service.impl.StatisticsSwitchServiceImpl;
import com.zero.system.service.impl.SysConfigServiceImpl;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 21:34
 * 说明: 后台参数驱动统计开关回归，验证即时缓存更新、非法值关闭及存储故障兜底。
 */
class StatisticsSwitchTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 使用原参数服务的修改逻辑验证开关下一次读取生效，不修改原参数业务。
     * @return
     * 返回值: void
     * 返回值说明: 后台修改缓存后不需要重启或额外本地缓存刷新
     */
    @Test void parameterUpdateImmediatelyChangesSwitch()
    {
        SysConfigMapper mapper = mock(SysConfigMapper.class);
        RedisCache redis = mock(RedisCache.class);
        Map<String, Object> cache = new ConcurrentHashMap<>();
        when(redis.getCacheObject(anyString())).thenAnswer(call -> cache.get(call.getArgument(0)));
        doAnswer(call -> { cache.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(redis).setCacheObject(anyString(), any());
        SysConfig saved = new SysConfig();
        saved.setConfigId(13L);
        saved.setConfigKey(IStatisticsSwitchService.CONFIG_KEY);
        saved.setConfigValue("true");
        when(mapper.selectConfig(any(SysConfig.class))).thenReturn(saved);
        when(mapper.selectConfigById(13L)).thenReturn(saved);
        when(mapper.updateConfig(any(SysConfig.class))).thenReturn(1);
        SysConfigServiceImpl configService = new SysConfigServiceImpl();
        ReflectionTestUtils.setField(configService, "configMapper", mapper);
        ReflectionTestUtils.setField(configService, "redisCache", redis);
        IStatisticsSwitchService service = new StatisticsSwitchServiceImpl(configService, mapper);
        assertThat(service.isEnabled()).isTrue();
        saved.setConfigValue("false");
        configService.updateConfig(saved);
        assertThat(cache).containsEntry(CacheConstants.SYS_CONFIG_KEY + IStatisticsSwitchService.CONFIG_KEY, "false");
        assertThat(service.isEnabled()).isFalse();
        saved.setConfigValue(" TRUE ");
        configService.updateConfig(saved);
        assertThat(service.isEnabled()).isTrue();
        verify(mapper, times(1)).selectConfig(any(SysConfig.class));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 缺失、空值及非true配置关闭统计，不扩展1、yes等未定义开关值。
     * @return
     * 返回值: void
     * 返回值说明: 非法值不会误开启统计
     */
    @Test void missingAndInvalidValuesDisable()
    {
        ISysConfigService config = mock(ISysConfigService.class);
        IStatisticsSwitchService service = new StatisticsSwitchServiceImpl(config, mock(SysConfigMapper.class));
        for (String value : new String[]{null, "", " ", "false", "1", "yes", "invalid"})
        {
            when(config.selectConfigByKey(IStatisticsSwitchService.CONFIG_KEY)).thenReturn(value);
            assertThat(service.isEnabled()).isFalse();
            assertThat(service.isAvailable()).isTrue();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: Redis读取失败时从MySQL读取，双通道失败保留最后有效值并报告状态，恢复后按新参数生效。
     * @return
     * 返回值: void
     * 返回值说明: 配置故障不阻断业务，也不掩盖异常状态
     */
    @Test void storageFailureFallsBackAndRecovers()
    {
        ISysConfigService config = mock(ISysConfigService.class);
        SysConfigMapper mapper = mock(SysConfigMapper.class);
        when(config.selectConfigByKey(IStatisticsSwitchService.CONFIG_KEY)).thenThrow(new IllegalStateException("isolated cache failure"));
        SysConfig saved = new SysConfig(); saved.setConfigValue("true");
        when(mapper.selectConfig(any(SysConfig.class))).thenReturn(saved);
        IStatisticsSwitchService service = new StatisticsSwitchServiceImpl(config, mapper);
        assertThat(service.isEnabled()).isTrue();
        assertThat(service.isAvailable()).isTrue();
        when(mapper.selectConfig(any(SysConfig.class))).thenThrow(new IllegalStateException("isolated database failure"));
        assertThat(service.isEnabled()).isTrue();
        assertThat(service.isAvailable()).isFalse();
        assertThat(new StatisticsSwitchServiceImpl(config, mapper).isEnabled()).isFalse();
        saved.setConfigValue("false");
        doReturn(saved).when(mapper).selectConfig(any(SysConfig.class));
        assertThat(service.isEnabled()).isFalse();
        assertThat(service.isAvailable()).isTrue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 旧YAML开关不再控制采集；同一Filter读取后台参数后即可关闭或开启，不影响正常响应。
     * @return
     * 返回值: void
     * 返回值说明: 动态切换不创建新的采集组件或重启应用
     */
    @Test void legacyYamlCannotOverrideRuntimeParameter() throws Exception
    {
        StatisticsProperties properties = new StatisticsProperties();
        new Binder(new MapConfigurationPropertySource(Map.of("zero.statistics.enabled", "true")))
                .bind("zero.statistics", Bindable.ofInstance(properties));
        ISysConfigService config = mock(ISysConfigService.class);
        when(config.selectConfigByKey(IStatisticsSwitchService.CONFIG_KEY)).thenReturn("false");
        StatisticsSwitchServiceImpl service = new StatisticsSwitchServiceImpl(config, mock(SysConfigMapper.class));
        StatisticsPipeline pipeline = mock(StatisticsPipeline.class);
        StatisticsFilter filter = new StatisticsFilter(properties, pipeline, service);
        filter.doFilter(new MockHttpServletRequest("GET", "/system/user/list"), new MockHttpServletResponse(), (req, res) -> {});
        verifyNoInteractions(pipeline);
        when(config.selectConfigByKey(IStatisticsSwitchService.CONFIG_KEY)).thenReturn("true");
        filter.doFilter(new MockHttpServletRequest("GET", "/system/user/list"), new MockHttpServletResponse(), (req, res) -> {});
        verify(pipeline).publish(any());
    }
}
