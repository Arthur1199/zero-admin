package com.zero.system.service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.framework.web.service.SysPermissionService;
import com.zero.framework.web.service.TokenService;
import com.zero.framework.websocket.AdminWebSocketService;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.mapper.SysUserPostMapper;
import com.zero.system.mapper.SysUserRoleMapper;
import com.zero.system.service.impl.SysUserServiceImpl;
import com.zero.web.controller.system.SysLoginController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 隔离验证用户变更提交后推送、回滚不推送及刷新时获取最新权限，不操作真实用户
 */
class AdminUserRefreshTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 清理测试登录身份，避免污染其它安全回归
     * @return
     * 返回值: void
     * 返回值说明: 当前线程无残留身份
     */
    @AfterEach
    void cleanup()
    {
        SecurityContextHolder.clearContext();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 事务提交前不发布，提交完成后才通知去重后的目标用户
     * @return
     * 返回值: void
     * 返回值说明: 推送发生在数据库提交之后，绝不退化为广播
     */
    @Test
    void sendsOnlyAfterCommit() throws Exception
    {
        var ws = mock(AdminWebSocketService.class);
        var service = new AdminUserRefreshService(ws);
        var connection = mock(Connection.class);
        transaction(connection).executeWithoutResult(status -> {
            service.afterChanged(Arrays.asList(2L, 2L, null, -1L));
            verifyNoInteractions(ws);
        });
        var order = inOrder(connection, ws);
        order.verify(connection).commit();
        order.verify(ws).sendToUsers(eq(Set.of(2L)), argThat(message ->
                message.getMsgType() == WebSocketMessageType.REFRESH_BROWSER && message.getToAdminId() == null));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 回滚和无效接收人不发布任何消息
     * @return
     * 返回值: void
     * 返回值说明: 修改未生效时浏览器不会刷新
     */
    @Test
    void rollbackAndEmptyTargetsDoNotSend() throws Exception
    {
        var ws = mock(AdminWebSocketService.class);
        var service = new AdminUserRefreshService(ws);
        transaction(mock(Connection.class)).executeWithoutResult(status -> {
            service.afterChanged(List.of(2L));
            status.setRollbackOnly();
        });
        service.afterChanged(Arrays.asList(null, 0L));
        service.afterChanged(null);
        verifyNoInteractions(ws);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 特殊事务管理器禁用提交同步时不能退化为提交前发送
     * @return
     * 返回值: void
     * 返回值说明: 未知提交时机下跳过通知，不影响业务事务
     */
    @Test
    void activeTransactionWithoutSynchronizationCannotSendEarly()
    {
        var ws = mock(AdminWebSocketService.class);
        org.springframework.transaction.support.TransactionSynchronizationManager.setActualTransactionActive(true);
        try
        {
            new AdminUserRefreshService(ws).afterChanged(List.of(2L));
            verifyNoInteractions(ws);
        }
        finally
        {
            org.springframework.transaction.support.TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: Redis失败不能使已提交的用户修改显示为事务失败
     * @return
     * 返回值: void
     * 返回值说明: 提交成功且不透传推送异常
     */
    @Test
    void pushFailureDoesNotFailCommittedChange() throws Exception
    {
        var ws = mock(AdminWebSocketService.class);
        when(ws.sendToUsers(anyCollection(), any())).thenThrow(new IllegalStateException("isolated failure"));
        var service = new AdminUserRefreshService(ws);
        var connection = mock(Connection.class);
        var template = transaction(connection);
        assertThatCode(() -> template.executeWithoutResult(status -> service.afterChanged(List.of(2L))))
                .doesNotThrowAnyException();
        verify(connection).commit();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 用户编辑仅在部门或角色集合实际变化且更新成功时通知，忽略角色顺序
     * @return
     * 返回值: void
     * 返回值说明: 普通资料修改、重复保存及更新失败不发送刷新
     */
    @Test
    void userEditDetectsDepartmentAndRoleChanges()
    {
        var service = new SysUserServiceImpl();
        var mapper = mock(SysUserMapper.class);
        var refresh = mock(AdminUserRefreshService.class);
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        ReflectionTestUtils.setField(service, "userRoleMapper", mock(SysUserRoleMapper.class));
        ReflectionTestUtils.setField(service, "userPostMapper", mock(SysUserPostMapper.class));
        ReflectionTestUtils.setField(service, "userRefreshService", refresh);
        when(mapper.selectUserById(2L)).thenReturn(user(1L, 3L));
        when(mapper.updateUser(any())).thenReturn(1);
        var update = user(1L, 3L);
        update.setRoleIds(new Long[] {3L});
        service.updateUser(update);
        verifyNoInteractions(refresh);
        update.setDeptId(9L);
        service.updateUser(update);
        verify(refresh).afterChanged(List.of(2L));
        clearInvocations(refresh);
        update.setDeptId(1L);
        update.setRoleIds(new Long[] {4L});
        service.updateUser(update);
        verify(refresh).afterChanged(List.of(2L));
        clearInvocations(refresh);
        when(mapper.updateUser(any())).thenReturn(0);
        service.updateUser(update);
        verifyNoInteractions(refresh);
        service.insertUserAuth(2L, new Long[] {3L, 3L});
        verifyNoInteractions(refresh);
        service.insertUserAuth(2L, new Long[0]);
        verify(refresh).afterChanged(List.of(2L));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 角色管理的添加用户、取消单用户和批量取消也通知刷新，未修改记录时不通知
     * @return
     * 返回值: void
     * 返回值说明: 不遗漏角色管理入口，不扩大目标用户范围
     */
    @Test
    void roleAssignmentEntriesNotifyOnlySuccessfulWrites()
    {
        var service = new com.zero.system.service.impl.SysRoleServiceImpl();
        var mapper = mock(SysUserRoleMapper.class);
        var refresh = mock(AdminUserRefreshService.class);
        ReflectionTestUtils.setField(service, "userRoleMapper", mapper);
        ReflectionTestUtils.setField(service, "userRefreshService", refresh);
        var relation = new com.zero.system.domain.SysUserRole();
        relation.setUserId(2L);
        relation.setRoleId(3L);
        service.deleteAuthUser(relation);
        verifyNoInteractions(refresh);
        when(mapper.deleteUserRoleInfo(relation)).thenReturn(1);
        service.deleteAuthUser(relation);
        verify(refresh).afterChanged(List.of(2L));
        clearInvocations(refresh);
        Long[] ids = {2L, 4L};
        when(mapper.selectUserIdsByRoleId(3L, ids)).thenReturn(List.of(2L));
        when(mapper.deleteUserRoleInfos(3L, ids)).thenReturn(1);
        service.deleteAuthUsers(3L, ids);
        verify(refresh).afterChanged(List.of(2L));
        clearInvocations(refresh);
        when(mapper.batchUserRole(anyList())).thenReturn(2);
        service.insertAuthUsers(3L, ids);
        verify(refresh).afterChanged(List.of(2L, 4L));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 即使菜单权限相同，部门或角色变化后getInfo也必须更新当前登录快照
     * @return
     * 返回值: void
     * 返回值说明: 后续路由与业务请求读取最新部门、角色，更新来自HTTP请求而非心跳
     */
    @Test
    void getInfoRefreshesDepartmentAndRolesEvenWithSamePermissions()
    {
        for (boolean departmentOnly : List.of(true, false))
        {
            var controller = new SysLoginController();
            var users = mock(ISysUserService.class);
            var permissions = mock(SysPermissionService.class);
            var tokens = mock(TokenService.class);
            var latest = user(departmentOnly ? 9L : 1L, departmentOnly ? 3L : 4L);
            var login = new LoginUser(2L, 1L, user(1L, 3L), Set.of("same:permission"));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, List.of()));
            when(users.selectUserById(2L)).thenReturn(latest);
            when(permissions.getRolePermission(latest)).thenReturn(Set.of("new-role"));
            when(permissions.getMenuPermission(latest)).thenReturn(Set.of("same:permission"));
            ReflectionTestUtils.setField(controller, "userService", users);
            ReflectionTestUtils.setField(controller, "permissionService", permissions);
            ReflectionTestUtils.setField(controller, "tokenService", tokens);
            ReflectionTestUtils.setField(controller, "configService", mock(ISysConfigService.class));
            assertThat(controller.getInfo().get("user")).isSameAs(latest);
            assertThat(login.getDeptId()).isEqualTo(latest.getDeptId());
            assertThat(login.getUser()).isSameAs(latest);
            verify(tokens).refreshToken(login);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: API谷歌验证修改提交后才广播，回滚、写入失败和无效配置均不广播
     * @return
     * 返回值: void
     * 返回值说明: 广播覆盖所有用户且只在提交成功后发出，不携带收件人或敏感配置
     */
    @Test
    void apiAuthUpdateBroadcastsOnlyAfterSuccessfulCommit() throws Exception
    {
        var ws = mock(AdminWebSocketService.class);
        var service = new com.zero.system.service.impl.SysApiAuthServiceImpl();
        var mapper = mock(com.zero.system.mapper.SysApiAuthMapper.class);
        var rule = new com.zero.system.domain.SysApiAuth();
        rule.setId(9L);
        rule.setApiMethod("PUT");
        rule.setApiUrl("/isolated/api");
        rule.setApiStatus(1);
        rule.setIsNeedGoogle(1);
        when(mapper.selectApiAuthById(9L)).thenReturn(rule);
        ReflectionTestUtils.setField(service, "apiAuthMapper", mapper);
        ReflectionTestUtils.setField(service, "clientRefreshService", new AdminUserRefreshService(ws));
        assertThat(service.updateApiAuth(rule)).isZero();
        when(mapper.updateApiAuth(any())).thenReturn(1);
        transaction(mock(Connection.class)).executeWithoutResult(status -> {
            service.updateApiAuth(rule);
            status.setRollbackOnly();
        });
        assertThatThrownBy(() -> service.updateApiAuth(null)).isInstanceOf(com.zero.common.exception.ServiceException.class);
        rule.setApiMethod("INVALID");
        assertThatThrownBy(() -> service.updateApiAuth(rule)).isInstanceOf(com.zero.common.exception.ServiceException.class);
        verifyNoInteractions(ws);
        rule.setApiMethod("PUT");
        var connection = mock(Connection.class);
        transaction(connection).executeWithoutResult(status -> {
            assertThat(service.updateApiAuth(rule)).isEqualTo(1);
            verifyNoInteractions(ws);
        });
        var order = inOrder(connection, ws);
        order.verify(connection).commit();
        order.verify(ws).broadcast(argThat(message -> message.getMsgType() == WebSocketMessageType.REFRESH_BROWSER
                && message.getToAdminId() == null));
        verify(ws, never()).sendToUsers(anyCollection(), any());
        when(ws.broadcast(any())).thenThrow(new IllegalStateException("isolated unavailable"));
        var template = transaction(mock(Connection.class));
        assertThatCode(() -> template.executeWithoutResult(status ->
                assertThat(service.updateApiAuth(rule)).isEqualTo(1))).doesNotThrowAnyException();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 使用模拟JDBC连接驱动真实Spring事务回调，不连接数据库
     * @param connection 当前测试的连接替身
     * @return
     * 返回值: TransactionTemplate
     * 返回值说明: 可验证提交和回滚行为的事务模板
     */
    private TransactionTemplate transaction(Connection connection) throws Exception
    {
        var source = mock(DataSource.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        return new TransactionTemplate(new DataSourceTransactionManager(source));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 构造无真实个人资料的测试用户及角色快照
     * @param department 部门ID
     * @param roleId 角色ID
     * @return
     * 返回值: SysUser
     * 返回值说明: 用户ID固定为测试值2
     */
    private SysUser user(Long department, Long roleId)
    {
        var user = new SysUser(2L);
        user.setDeptId(department);
        user.setUserName("isolated-test");
        var role = new SysRole();
        role.setRoleId(roleId);
        user.setRoles(List.of(role));
        return user;
    }
}
