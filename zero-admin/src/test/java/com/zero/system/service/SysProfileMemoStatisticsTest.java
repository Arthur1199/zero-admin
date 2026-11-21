package com.zero.system.service;

import java.util.List;
import java.util.Set;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.framework.web.service.PermissionService;
import com.zero.system.domain.SysProfileStatistics.MemoCounts;
import com.zero.system.domain.SysProfileStatistics.Memos;
import com.zero.system.service.impl.SysProfileStatisticsService;
import com.zero.web.controller.system.SysProfileController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 19:50
 * 说明: 验证个人中心备忘录的服务端权限标识及数据访问范围
 */
class SysProfileMemoStatisticsTest
{
    /** 模拟统计查询，验证授权前不读取数据。 */
    private final SysProfileStatisticsService statisticsService = mock(SysProfileStatisticsService.class);

    @AfterEach
    void clearLogin()
    {
        SecurityContextHolder.clearContext();
    }

    /** 缺少查看权限时只返回标识，即使请求携带其他用户 ID 也不查询统计。 */
    @Test
    void reportsNoAccessWithoutQueryingStatistics() throws Exception
    {
        loginAs(8L, "systemTools:memo:add").perform(get("/system/user/profile/statistics/memos").param("userId", "9"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.canReadMemos").value(false))
                .andExpect(jsonPath("$.data.statistics").doesNotExist());
        verifyNoInteractions(statisticsService);
    }

    /** 菜单查看权限与管理员通配权限均只允许读取当前登录人的统计。 */
    @ParameterizedTest
    @ValueSource(strings = {"systemTools:memo:list", "*:*:*"})
    void returnsStatisticsForAuthenticatedUser(String permission) throws Exception
    {
        when(statisticsService.getMemos(8L)).thenReturn(new Memos(new MemoCounts(0, 0, 0, 0), List.of()));
        loginAs(8L, permission).perform(get("/system/user/profile/statistics/memos").param("userId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canReadMemos").value(true))
                .andExpect(jsonPath("$.data.statistics.counts.total").value(0));
        verify(statisticsService).getMemos(8L);
    }

    /** 在隔离的 MVC 请求中使用实际菜单权限校验。 */
    private MockMvc loginAs(Long userId, String permission)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        LoginUser loginUser = new LoginUser(userId, null, user, Set.of(permission));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
        SysProfileController controller = new SysProfileController();
        ReflectionTestUtils.setField(controller, "profileStatisticsService", statisticsService);
        ReflectionTestUtils.setField(controller, "permissionService", new PermissionService());
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
