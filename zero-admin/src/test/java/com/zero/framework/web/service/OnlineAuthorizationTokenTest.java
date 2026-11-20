package com.zero.framework.web.service;

import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.system.service.ISysConfigService;
import com.zero.system.domain.SysUserOnline;
import com.zero.system.service.impl.SysUserOnlineServiceImpl;
import com.zero.web.controller.monitor.SysUserOnlineController;
import com.zero.framework.aspectj.RequiresPermissionsAspect;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 14:56
 * 说明: 使用隔离Redis与测试密钥验证在线授权令牌还原、生命周期及并发过期边界，不接触真实会话
 */
class OnlineAuthorizationTokenTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:56
     * 说明: 使用实际权限切面验证普通账号持有菜单权限即可读取全部会话，无权限时禁止查询缓存
     * @return
     * 返回值: void
     * 返回值说明: 不依赖超级管理员角色，不修改真实账号或权限
     */
    @Test
    void existingMenuPermissionControlsAllSessionTokens()
    {
        RedisCache cache = mock(RedisCache.class);
        LoginUser session = new LoginUser(new SysUser(), Set.of());
        when(cache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*")).thenReturn(List.of("other-session"));
        when(cache.getCacheObject("other-session")).thenReturn(session);
        SysUserOnlineServiceImpl service = mock(SysUserOnlineServiceImpl.class);
        SysUserOnline online = new SysUserOnline();
        online.setAuthorizationToken("isolated-other-session-token");
        when(service.loginUserToUserOnline(session)).thenReturn(online);
        SysUserOnlineController controller = new SysUserOnlineController();
        ReflectionTestUtils.setField(controller, "redisCache", cache);
        ReflectionTestUtils.setField(controller, "userOnlineService", service);
        RequiresPermissionsAspect aspect = new RequiresPermissionsAspect();
        ReflectionTestUtils.setField(aspect, "permissionService", new PermissionService());
        AspectJProxyFactory factory = new AspectJProxyFactory(controller);
        factory.addAspect(aspect);
        SysUserOnlineController secured = factory.getProxy();
        SysUser account = new SysUser();
        account.setUserId(2L);
        account.setUserName("ordinary-viewer");
        LoginUser viewer = new LoginUser(account, Set.of("monitor:online:list"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(viewer, null, List.of()));
        try
        {
            assertThat(secured.list(null, null, new MockHttpServletResponse()).getRows()).isEqualTo(List.of(online));
            clearInvocations(cache, service);
            viewer.setPermissions(Set.of("unrelated:permission"));
            assertThatThrownBy(() -> secured.list(null, null, new MockHttpServletResponse()))
                    .isInstanceOf(AccessDeniedException.class);
            verifyNoInteractions(cache, service);
        }
        finally
        {
            SecurityContextHolder.clearContext();
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:56
     * 说明: 完整JWT应与登录时签发值相同，展示不刷新缓存，缓存删除后该JWT不能恢复登录态
     * @return
     * 返回值: void
     * 返回值说明: 验证JWT签名、现有会话关联与无缓存写入
     */
    @Test
    void restoresOriginalJwtWithoutChangingSession()
    {
        RedisCache cache = mock(RedisCache.class);
        TokenService tokens = new TokenService();
        ReflectionTestUtils.setField(tokens, "redisCache", cache);
        ReflectionTestUtils.setField(tokens, "configService", mock(ISysConfigService.class));
        ReflectionTestUtils.setField(tokens, "loginSessions", mock(AdminLoginSessionStore.class));
        ReflectionTestUtils.setField(tokens, "header", "Authorization");
        ReflectionTestUtils.setField(tokens, "secret", "isolated-online-token-test-key");
        ReflectionTestUtils.setField(tokens, "expireTime", 30);
        SysUser account = new SysUser();
        account.setUserName("test-online-user");
        LoginUser user = new LoginUser(account, Set.of("monitor:online:list"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try
        {
            String original = tokens.createToken(user);
            String sessionId = user.getToken();
            Long loginTime = user.getLoginTime(), expireTime = user.getExpireTime();
            clearInvocations(cache);
            String restored = tokens.getExistingToken(user);
            assertThat(restored).isEqualTo(original);
            assertThat(user.getToken()).isEqualTo(sessionId);
            assertThat(user.getLoginTime()).isEqualTo(loginTime);
            assertThat(user.getExpireTime()).isEqualTo(expireTime);
            verifyNoInteractions(cache);
            when(cache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + sessionId)).thenReturn(user, null);
            request.addHeader("Authorization", "Bearer " + restored);
            assertThat(tokens.getLoginUser(request)).isSameAs(user);
            assertThat(tokens.getLoginUser(request)).isNull();

            SysUserOnlineServiceImpl service = new SysUserOnlineServiceImpl();
            ReflectionTestUtils.setField(service, "tokenService", tokens);
            SysUserOnline online = service.loginUserToUserOnline(user);
            assertThat(online.getAuthorizationToken()).isEqualTo("Bearer " + original);
            assertThat(online.getTokenId()).isEqualTo(sessionId);
            assertThat(tokens.getExistingToken(null)).isEmpty();
            assertThat(tokens.getExistingToken(new LoginUser())).isEmpty();
            user.setToken(null);
            assertThat(tokens.getExistingToken(user)).isEmpty();
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:56
     * 说明: Redis扫描后并发过期的会话不导致用户名筛选空指针，敏感响应禁止缓存
     * @return
     * 返回值: void
     * 返回值说明: 验证失效会话过滤及缓存响应头，不发起真实查询
     */
    @Test
    void expiredCacheEntryIsSkippedAndResponseIsNotCached()
    {
        RedisCache cache = mock(RedisCache.class);
        when(cache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*")).thenReturn(List.of("expired-key"));
        SysUserOnlineController controller = new SysUserOnlineController();
        ReflectionTestUtils.setField(controller, "redisCache", cache);
        ReflectionTestUtils.setField(controller, "userOnlineService", new SysUserOnlineServiceImpl());
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(controller.list(null, "test-online-user", response).getRows()).isEmpty();
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
    }
}
