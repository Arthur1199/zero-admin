package com.zero.system.service;

import java.util.List;
import java.util.Set;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.system.service.impl.SysProfileStatisticsService;
import com.zero.web.controller.system.SysProfileController;
import eu.bitwalker.useragentutils.UserAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 20:12
 * 说明: 验证当前会话接口的网络、设备、在线时长及旧会话兼容边界
 */
class SysProfileSessionStatisticsTest
{
    /** 模拟浏览器请求头，使用与登录日志相同的解析器校验结果。 */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    @AfterEach
    void clearLogin()
    {
        SecurityContextHolder.clearContext();
    }

    /** 转发地址取自当前请求，在线时长取首次登录时间。 */
    @Test
    void readsCurrentRequestAndOriginalSessionStart() throws Exception
    {
        var userAgent = UserAgent.parseUserAgentString(USER_AGENT);
        long now = System.currentTimeMillis();
        loginWithStart(now - 90000).perform(get("/system/user/profile/statistics/session")
                .header("X-Forwarded-For", "192.168.10.24, 10.0.0.3")
                .header("User-Agent", USER_AGENT))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.onlineSeconds").value(greaterThanOrEqualTo(90)))
                .andExpect(jsonPath("$.data.ip").value("192.168.10.24"))
                .andExpect(jsonPath("$.data.location").value("内网IP"))
                .andExpect(jsonPath("$.data.browser").value(userAgent.getBrowser().getName()))
                .andExpect(jsonPath("$.data.os").value(userAgent.getOperatingSystem().getName()));
    }

    /** 没有首次登录时间的旧会话仍可看到当前直连 IP。 */
    @Test
    void handlesLegacySessionAndMissingUserAgent() throws Exception
    {
        loginWithStart(null).perform(get("/system/user/profile/statistics/session").with(request -> {
            request.setRemoteAddr("::1");
            return request;
        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onlineSeconds").doesNotExist())
                .andExpect(jsonPath("$.data.ip").value("127.0.0.1"))
                .andExpect(jsonPath("$.data.location").value("内网IP"));
    }

    /** 地区库无法解析 IPv6 时仍返回当前地址。 */
    @Test
    void keepsIpWhenRegionIsUnavailable() throws Exception
    {
        loginWithStart(null).perform(get("/system/user/profile/statistics/session").with(request -> {
            request.setRemoteAddr("2001:db8::123");
            return request;
        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ip").value("2001:db8::123"))
                .andExpect(jsonPath("$.data.location").doesNotExist());
    }

    /** 使用已认证的隔离会话调用接口，统计网络环境时不使用 Mapper。 */
    private MockMvc loginWithStart(Long startedAt)
    {
        SysUser user = new SysUser();
        user.setUserId(8L);
        LoginUser loginUser = new LoginUser(8L, null, user, Set.of());
        loginUser.setSessionStartedAt(startedAt);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setIpaddr("127.0.0.2");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
        SysProfileController controller = new SysProfileController();
        ReflectionTestUtils.setField(controller, "profileStatisticsService", new SysProfileStatisticsService(null));
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
