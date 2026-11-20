package com.zero.system.service;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.system.domain.SysGoogle;
import com.zero.system.mapper.SysGoogleMapper;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.mapper.SysUserPostMapper;
import com.zero.system.mapper.SysUserRoleMapper;
import com.zero.system.service.impl.SysUserServiceImpl;
import com.zero.system.service.impl.SysGoogleServiceImpl;
import com.zero.framework.websocket.AdminWebSocketService;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 隔离验证强退精确会话、禁用删除提交后的会话撤销以及回滚保护，不操作真实用户
 */
class AdminUserLogoutTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 先撤销选中的缓存，再发布会话定向事件；通知失败不能复活缓存
     * @return
     * 返回值: void
     * 返回值说明: 不进行用户级广播或扫描
     */
    @Test
    void forceLogoutRevokesOnlySelectedSession()
    {
        var cache = mock(RedisCache.class);
        var ws = mock(AdminWebSocketService.class);
        when(cache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "selected")).thenReturn(login(2L));
        when(ws.logoutSession(2L, "selected")).thenThrow(new IllegalStateException("isolated-failure"));
        new AdminUserLogoutService(cache, ws).forceLogout("selected");
        var order = inOrder(cache, ws);
        order.verify(cache).getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "selected");
        order.verify(cache).deleteObject(CacheConstants.LOGIN_TOKEN_KEY + "selected");
        order.verify(ws).logoutSession(2L, "selected");
        verifyNoMoreInteractions(cache, ws);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 回滚和无效用户集合不撤销登录，也不发布退出消息
     * @return
     * 返回值: void
     * 返回值说明: 不影响正常用户会话
     */
    @Test
    void rollbackAndEmptyTargetsDoNothing() throws Exception
    {
        var cache = mock(RedisCache.class);
        var ws = mock(AdminWebSocketService.class);
        var service = new AdminUserLogoutService(cache, ws);
        transaction(mock(Connection.class)).executeWithoutResult(status -> {
            service.afterUsersUnavailable(List.of(2L));
            verifyNoInteractions(cache, ws);
            status.setRollbackOnly();
        });
        service.afterUsersUnavailable(List.of());
        service.afterUsersUnavailable(null);
        verifyNoInteractions(cache, ws);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 撤销缓存或发送通知异常均不掩盖已提交结果，缓存扫描失败仍尝试通知客户端退出
     * @return
     * 返回值: void
     * 返回值说明: 故障记录固定告警，不向外暴露缓存或消息正文
     */
    @Test
    @SuppressWarnings("rawtypes")
    void infrastructureFailureDoesNotUndoCommittedOperation()
    {
        var cache = mock(RedisCache.class);
        cache.redisTemplate = mock(RedisTemplate.class);
        when(cache.redisTemplate.scan(any(ScanOptions.class))).thenThrow(new IllegalStateException("isolated-cache-failure"));
        var ws = mock(AdminWebSocketService.class);
        when(ws.sendToUsers(anyCollection(), any())).thenThrow(new IllegalStateException("isolated-publish-failure"));
        assertThatCode(() -> new AdminUserLogoutService(cache, ws).afterUsersUnavailable(List.of(2L))).doesNotThrowAnyException();
        verify(ws).sendToUsers(eq(Set.of(2L)), argThat(message -> message.getMsgType() == WebSocketMessageType.LOGIN_OUT));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 提交之后分批读取登录缓存，仅删除目标用户全部会话，其它用户和过期记录跳过
     * @return
     * 返回值: void
     * 返回值说明: 撤销后发送全会话退出，游标正确关闭
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void commitRevokesAllTargetSessionsOnly() throws Exception
    {
        var cache = mock(RedisCache.class);
        var ws = mock(AdminWebSocketService.class);
        var connection = mock(Connection.class);
        cache.redisTemplate = mock(RedisTemplate.class);
        Cursor cursor = mock(Cursor.class);
        when(cache.redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, true, false);
        when(cursor.next()).thenReturn("session-a", "session-b", "other", "expired");
        when(cache.getCacheObject("session-a")).thenReturn(login(2L));
        when(cache.getCacheObject("session-b")).thenReturn(login(2L));
        when(cache.getCacheObject("other")).thenReturn(login(3L));
        transaction(connection).executeWithoutResult(status -> {
            new AdminUserLogoutService(cache, ws).afterUsersUnavailable(List.of(2L, 2L));
            verifyNoInteractions(cache, ws);
        });
        var order = inOrder(connection, cache, ws);
        order.verify(connection).commit();
        order.verify(cache).deleteObject("session-a");
        order.verify(cache).deleteObject("session-b");
        order.verify(ws).sendToUsers(eq(Set.of(2L)), argThat(message -> message.getMsgType() == WebSocketMessageType.LOGIN_OUT));
        verify(cache, never()).deleteObject("other");
        verify(cursor).close();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 02:02
     * 说明: 谷歌解绑只在成功提交后撤销目标用户的会话，零行更新和事务回滚不强退。
     * @return
     * 返回值: void
     * 返回值说明: 验证数据库提交先于会话删除和定向退出通知
     */
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void googleUnbindRevokesSessionsOnlyAfterSuccessfulCommit() throws Exception
    {
        var cache = mock(RedisCache.class);
        cache.redisTemplate = mock(RedisTemplate.class);
        var cursor = mock(Cursor.class);
        var ws = mock(AdminWebSocketService.class);
        var mapper = mock(SysGoogleMapper.class);
        var connection = mock(Connection.class);
        var service = new SysGoogleServiceImpl();
        ReflectionTestUtils.setField(service, "googleMapper", mapper);
        ReflectionTestUtils.setField(service, "userLogoutService", new AdminUserLogoutService(cache, ws));
        var binding = new SysGoogle();
        binding.setGoogleAuthKey("isolated-test-key");
        when(mapper.selectGoogleByUserId(2L)).thenReturn(binding);
        when(mapper.updateGoogleAuthKey(any(SysGoogle.class))).thenReturn(0, 1, 1);

        assertThat(service.unbindGoogle(2L, "operator")).isZero();
        transaction(connection).executeWithoutResult(status -> {
            assertThat(service.unbindGoogle(2L, "operator")).isOne();
            status.setRollbackOnly();
        });
        verifyNoInteractions(cache, cache.redisTemplate, ws);

        when(cache.redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("target-session", "other-session");
        when(cache.getCacheObject("target-session")).thenReturn(login(2L));
        when(cache.getCacheObject("other-session")).thenReturn(login(3L));
        transaction(connection).executeWithoutResult(status -> {
            assertThat(service.unbindGoogle(2L, "operator")).isOne();
            verifyNoInteractions(cache, ws);
        });
        var order = inOrder(connection, cache, ws);
        order.verify(connection).commit();
        order.verify(cache).deleteObject("target-session");
        order.verify(ws).sendToUsers(eq(Set.of(2L)), argThat(message -> message.getMsgType() == WebSocketMessageType.LOGIN_OUT));
        verify(cache, never()).deleteObject("other-session");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 02:02
     * 说明: 仅解绑写入检查原绑定仍存在，正常绑定可以更新空密钥的记录。
     * @return
     * 返回值: void
     * 返回值说明: 验证 MyBatis 为不同操作生成的 SQL 条件
     */
    @Test
    void googleUnbindUpdatesOnlyAnExistingBinding() throws Exception
    {
        var config = new Configuration();
        config.getTypeAliasRegistry().registerAlias(SysGoogle.class);
        String resource = "mapper/system/SysGoogleMapper.xml";
        try (var input = org.apache.ibatis.io.Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        var update = config.getMappedStatement(SysGoogleMapper.class.getName() + ".updateGoogleAuthKey");
        var record = new SysGoogle();
        record.setGoogleAuthKey("");
        assertThat(update.getBoundSql(record).getSql()).contains("google_auth_key is not null and google_auth_key <> ''");
        record.setGoogleAuthKey("isolated-test-key");
        assertThat(update.getBoundSql(record).getSql()).doesNotContain("google_auth_key is not null");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 禁用写入成功和单用户删除成功才注册退出，启用和零行更新保持原行为
     * @return
     * 返回值: void
     * 返回值说明: 不改变原Mapper调用与删除关联流程
     */
    @Test
    void userOperationsRegisterLogoutOnlyOnSuccess()
    {
        var mapper = mock(SysUserMapper.class);
        var logout = mock(AdminUserLogoutService.class);
        var service = new SysUserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        ReflectionTestUtils.setField(service, "userLogoutService", logout);
        ReflectionTestUtils.setField(service, "userRoleMapper", mock(SysUserRoleMapper.class));
        ReflectionTestUtils.setField(service, "userPostMapper", mock(SysUserPostMapper.class));
        SysUser user = new SysUser(2L);
        user.setStatus("1");
        when(mapper.updateUser(user)).thenReturn(0, 1, 1);
        assertThat(service.updateUserStatus(user)).isZero();
        verifyNoInteractions(logout);
        assertThat(service.updateUserStatus(user)).isEqualTo(1);
        verify(logout).afterUsersUnavailable(List.of(2L));
        clearInvocations(logout);
        user.setStatus("0");
        service.updateUserStatus(user);
        verifyNoInteractions(logout);
        when(mapper.deleteUserById(2L)).thenReturn(1);
        service.deleteUserById(2L);
        verify(logout).afterUsersUnavailable(List.of(2L));
        clearInvocations(logout);
        var batch = spy(service);
        doNothing().when(batch).checkUserAllowed(any(SysUser.class));
        doNothing().when(batch).checkUserDataScope(anyLong());
        Long[] ids = {2L, 3L};
        when(mapper.deleteUserByIds(ids)).thenReturn(0, 2);
        batch.deleteUserByIds(ids);
        verifyNoInteractions(logout);
        batch.deleteUserByIds(ids);
        verify(logout).afterUsersUnavailable(List.of(2L, 3L));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 编辑用户同时禁用时，退出优先于部门角色刷新，避免浏览器刷新抢先中断退出流程
     * @return
     * 返回值: void
     * 返回值说明: 保留用户编辑Mapper操作，不重复发送刷新
     */
    @Test
    void editedDisabledUserLogsOutInsteadOfRefreshing()
    {
        var mapper = mock(SysUserMapper.class);
        var logout = mock(AdminUserLogoutService.class);
        var refresh = mock(AdminUserRefreshService.class);
        var service = new SysUserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        ReflectionTestUtils.setField(service, "userLogoutService", logout);
        ReflectionTestUtils.setField(service, "userRefreshService", refresh);
        ReflectionTestUtils.setField(service, "userRoleMapper", mock(SysUserRoleMapper.class));
        ReflectionTestUtils.setField(service, "userPostMapper", mock(SysUserPostMapper.class));
        SysUser previous = new SysUser(2L);
        previous.setDeptId(1L);
        SysUser edited = new SysUser(2L);
        edited.setDeptId(2L);
        edited.setStatus("1");
        when(mapper.selectUserById(2L)).thenReturn(previous);
        when(mapper.updateUser(edited)).thenReturn(1);
        assertThat(service.updateUser(edited)).isEqualTo(1);
        verify(logout).afterUsersUnavailable(List.of(2L));
        verifyNoInteractions(refresh);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 已撤销的会话调用既有退出处理器仍返回成功，不能依赖缓存存在才能完成退出
     * @return
     * 返回值: void
     * 返回值说明: 使用模拟Servlet，不访问业务数据或记录真实退出日志
     */
    @Test
    void revokedSessionCanStillCompleteLogoutEndpoint() throws Exception
    {
        var tokens = mock(com.zero.framework.web.service.TokenService.class);
        var handler = new com.zero.framework.security.handle.LogoutSuccessHandlerImpl();
        ReflectionTestUtils.setField(handler, "tokenService", tokens);
        var response = new org.springframework.mock.web.MockHttpServletResponse();
        try (var messages = mockStatic(com.zero.common.utils.MessageUtils.class))
        {
            messages.when(() -> com.zero.common.utils.MessageUtils.message("user.logout.success")).thenReturn("退出成功");
            handler.onLogoutSuccess(new org.springframework.mock.web.MockHttpServletRequest(), response, null);
        }
        assertThat(new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.getContentAsString())
                .path("code").asInt()).isEqualTo(200);
        verify(tokens, never()).delLoginUser(anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 生成不含凭证的隔离登录缓存
     * @param id 测试用户ID
     * @return
     * 返回值: LoginUser
     * 返回值说明: 仅供验证目标匹配
     */
    private LoginUser login(Long id)
    {
        return new LoginUser(id, 1L, new SysUser(id), Set.of());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 使用模拟JDBC连接测试真实Spring事务同步生命周期
     * @param connection 隔离测试连接
     * @return
     * 返回值: TransactionTemplate
     * 返回值说明: 不连接真实数据库
     */
    private TransactionTemplate transaction(Connection connection) throws Exception
    {
        var source = mock(DataSource.class);
        when(source.getConnection()).thenReturn(connection);
        return new TransactionTemplate(new DataSourceTransactionManager(source));
    }
}
