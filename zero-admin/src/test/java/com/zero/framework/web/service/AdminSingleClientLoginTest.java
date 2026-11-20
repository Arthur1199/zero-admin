package com.zero.framework.web.service;

import com.zero.common.constant.CacheConstants;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.framework.config.RedisConfig;
import com.zero.framework.websocket.AdminWebSocketService;
import com.zero.system.service.AdminUserLogoutService;
import com.zero.system.service.ISysConfigService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 01:11
 * 说明: 在隔离 Redis 验证后台会话替换、历史会话兼容、并发登录和续期保护。
 */
@EnabledIfEnvironmentVariable(named = "ZERO_ADMIN_LOGIN_TEST_REDIS_PORT", matches = "[0-9]+")
class AdminSingleClientLoginTest
{
    private LettuceConnectionFactory connectionFactory;
    private RedisCache cache;
    private TokenService tokens;
    private ISysConfigService config;
    private AdminWebSocketService webSocket;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 创建隔离的真实登录缓存，退出消息交由 Mockito 核对目标。
     * @return
     * 返回值: void
     * 返回值说明: 初始化测试使用的会话存储和 JWT 服务
     */
    @BeforeEach
    void start()
    {
        int port = Integer.parseInt(System.getenv("ZERO_ADMIN_LOGIN_TEST_REDIS_PORT"));
        var redisConfig = new RedisStandaloneConfiguration("127.0.0.1", port);
        redisConfig.setDatabase(1);
        connectionFactory = new LettuceConnectionFactory(redisConfig);
        connectionFactory.afterPropertiesSet();
        var redis = new RedisConfig().redisTemplate(connectionFactory);
        cache = new RedisCache();
        cache.redisTemplate = redis;
        config = mock(ISysConfigService.class);
        webSocket = mock(AdminWebSocketService.class);
        tokens = new TokenService();
        ReflectionTestUtils.setField(tokens, "redisCache", cache);
        ReflectionTestUtils.setField(tokens, "configService", config);
        ReflectionTestUtils.setField(tokens, "loginSessions", new AdminLoginSessionStore(redis));
        ReflectionTestUtils.setField(tokens, "userLogoutService", new AdminUserLogoutService(cache, webSocket));
        ReflectionTestUtils.setField(tokens, "header", "Authorization");
        ReflectionTestUtils.setField(tokens, "secret", "isolated-login-replacement-test-key");
        ReflectionTestUtils.setField(tokens, "expireTime", 30);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 释放测试 Redis 连接资源。
     * @return
     * 返回值: void
     * 返回值说明: 关闭本测试创建的 Lettuce 连接工厂
     */
    @AfterEach
    void stop()
    {
        connectionFactory.destroy();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 管理员先允许多端，再切换为单端，验证历史会话撤销及续期保护。
     * @return
     * 返回值: void
     * 返回值说明: 只通知管理员的旧会话，其他账号与新会话仍可使用
     */
    @Test
    void replacesOnlyOldSessionsOfTheSameAdminWhenDisabled()
    {
        when(config.selectConfigByKey(UserConstants.MULTI_CLIENT_LOGIN_CONFIG)).thenReturn("true");
        var legacy = user(1L);
        legacy.setToken("legacy-admin-session");
        legacy.setExpireTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
        cache.setCacheObject(CacheConstants.LOGIN_TOKEN_KEY + legacy.getToken(), legacy, 30, TimeUnit.MINUTES);
        var chrome = user(1L);
        var secondBrowser = user(1L);
        String chromeJwt = login(chrome);
        String secondJwt = login(secondBrowser);
        String otherJwt = login(user(7L));
        assertThat(httpUser(chromeJwt)).isNotNull();
        assertThat(httpUser(secondJwt)).isNotNull();
        verifyNoInteractions(webSocket);

        when(config.selectConfigByKey(UserConstants.MULTI_CLIENT_LOGIN_CONFIG)).thenReturn("false");
        var current = user(1L);
        String currentJwt = login(current);
        verify(webSocket).logoutSession(1L, legacy.getToken());
        verify(webSocket).logoutSession(1L, chrome.getToken());
        verify(webSocket).logoutSession(1L, secondBrowser.getToken());
        verifyNoMoreInteractions(webSocket);

        tokens.refreshToken(chrome);
        tokens.setLoginUser(legacy);
        assertThat(httpUser(chromeJwt)).isNull();
        assertThat(httpUser(secondJwt)).isNull();
        assertThat(tokens.getWebSocketLoginUser(legacy.getToken())).isNull();
        assertThat(httpUser(currentJwt)).isNotNull();
        assertThat(httpUser(otherJwt)).isNotNull();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 验证多个并发请求共享 Redis 时只保留最后写入的单端会话。
     * @throws Exception 并发任务失败或等待超时
     * @return
     * 返回值: void
     * 返回值说明: 并发签发的令牌中仅一个仍有有效会话
     */
    @Test
    void leavesOneSessionWhenLoginsRace() throws Exception
    {
        when(config.selectConfigByKey(UserConstants.MULTI_CLIENT_LOGIN_CONFIG)).thenReturn("false");
        var gate = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(3);
        try
        {
            Callable<String> request = () -> {
                gate.await();
                return login(user(2L));
            };
            var pending = List.of(pool.submit(request), pool.submit(request), pool.submit(request));
            gate.countDown();
            List<String> issued = new java.util.ArrayList<>();
            for (var future : pending)
            {
                issued.add(future.get(10, TimeUnit.SECONDS));
            }
            // 所有登录完成后再核对，避免把随后会被替换的中间会话计入最终结果。
            assertThat(issued.stream().filter(token -> httpUser(token) != null).count()).isEqualTo(1);
        }
        finally
        {
            pool.shutdownNow();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 为已通过身份验证的测试用户签发登录 JWT。
     * @param user 测试登录快照
     * @return 登录 JWT
     * 返回值: String
     * 返回值说明: 当前测试线程请求上下文中签发的令牌
     */
    private String login(LoginUser user)
    {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try
        {
            return tokens.createToken(user);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 通过管理后台的 HTTP 令牌读取入口验证会话。
     * @param jwt 待验证的测试登录 JWT
     * @return 验证后的登录快照
     * 返回值: LoginUser
     * 返回值说明: 会话已撤销时返回 null
     */
    private LoginUser httpUser(String jwt)
    {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt);
        return tokens.getLoginUser(request);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:11
     * 说明: 构造指定用户 ID 的已验证登录快照。
     * @param id 测试账号 ID
     * @return 已验证用户快照
     * 返回值: LoginUser
     * 返回值说明: 测试账号 ID 为 1 时代表超级管理员
     */
    private LoginUser user(Long id)
    {
        var account = new SysUser();
        account.setUserId(id);
        account.setUserName("login-test-" + id);
        return new LoginUser(id, 1L, account, Set.of());
    }
}
