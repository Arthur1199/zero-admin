package com.zero.framework.statistics;

import com.zero.system.service.IStatisticsSwitchService;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 21:34
 * 说明: 统计框架隔离测试的开关桩，不读取或修改真实后台参数。
 */
final class StatisticsTestSwitch
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 为已有采集及持久化用例提供正常开启的独立开关。
     * @return 正常开启的测试开关
     * 返回值: IStatisticsSwitchService
     * 返回值说明: 仅在测试中模拟读取成功及开启状态
     */
    static IStatisticsSwitchService enabled()
    {
        IStatisticsSwitchService service = mock(IStatisticsSwitchService.class);
        when(service.isEnabled()).thenReturn(true);
        when(service.isAvailable()).thenReturn(true);
        return service;
    }
}
