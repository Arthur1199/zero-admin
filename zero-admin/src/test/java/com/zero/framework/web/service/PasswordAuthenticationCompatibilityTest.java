package com.zero.framework.web.service;

import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.framework.config.FastJson2JsonRedisSerializer;
import com.zero.framework.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/23 21:52
 * 说明: 验证后台用户使用真实密码认证器时能够登录，并保留 Redis 会话格式。
 */
class PasswordAuthenticationCompatibilityTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:52
     * 说明: 使用后台实际配置的认证管理器验证密码，菜单权限继续由登录用户快照持有。
     * @return
     * 返回值: void
     * 返回值说明: 登录用户认证成功，权限集合可读取，Redis 会话保留菜单权限。
     */
    @Test
    void authenticatesLoginUserWithApplicationPermissions()
    {
        String password = "local-test-password";
        SecurityConfig config = new SecurityConfig();
        SysUser user = new SysUser(2L);
        user.setUserName("login-test");
        user.setPassword(config.bCryptPasswordEncoder().encode(password));
        String permission = "system:user:list";
        LoginUser principal = new LoginUser(2L, 10L, user, Set.of(permission));
        ReflectionTestUtils.setField(config, "userDetailsService", (UserDetailsService) name -> principal);

        var authentication = config.authenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(user.getUserName(), password));

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(principal);
        assertThat(principal.getAuthorities()).isEmpty();
        var serializer = new FastJson2JsonRedisSerializer<>(LoginUser.class);
        byte[] session = serializer.serialize(principal);
        assertThat(new String(session, StandardCharsets.UTF_8)).doesNotContain("\"authorities\"");
        assertThat(serializer.deserialize(session).getPermissions()).containsExactly(permission);
    }
}
