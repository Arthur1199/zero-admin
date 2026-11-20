package com.zero.system.service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import javax.sql.DataSource;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.common.utils.SecurityUtils;
import com.zero.framework.security.filter.JwtAuthenticationTokenFilter;
import com.zero.framework.web.service.PermissionService;
import com.zero.framework.web.service.SysPermissionService;
import com.zero.framework.web.service.TokenService;
import com.zero.framework.websocket.AdminWebSocketService;
import com.zero.system.mapper.SysRoleDeptMapper;
import com.zero.system.mapper.SysRoleMapper;
import com.zero.system.mapper.SysRoleMenuMapper;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.mapper.SysUserPostMapper;
import com.zero.system.mapper.SysUserRoleMapper;
import com.zero.system.service.impl.SysRoleServiceImpl;
import com.zero.system.service.impl.SysUserServiceImpl;
import com.zero.web.controller.system.SysLoginController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
     * 时间: 2026/11/21 03:06
     * 说明: 验证角色实际授权变化时提交后定向刷新，菜单顺序和角色基本信息变化不刷新。
     */
    @Test
    void roleAuthorizationNotifiesMembersOnlyAfterCommit() throws Exception
    {
        var ws = mock(AdminWebSocketService.class);
        var service = new SysRoleServiceImpl();
        var roles = mock(SysRoleMapper.class);
        var menus = mock(SysRoleMenuMapper.class);
        var departments = mock(SysRoleDeptMapper.class);
        var members = mock(SysUserRoleMapper.class);
        ReflectionTestUtils.setField(service, "userRefreshService", new AdminUserRefreshService(ws, members));
        ReflectionTestUtils.setField(service, "roleMapper", roles);
        ReflectionTestUtils.setField(service, "roleMenuMapper", menus);
        ReflectionTestUtils.setField(service, "roleDeptMapper", departments);
        var saved = authorizationRole(2L, "2", Set.of());
        var edited = authorizationRole(2L, "2", Set.of());
        edited.setDeptIds(new Long[] {4L, 3L});
        edited.setMenuIds(new Long[] {16L, 15L});
        edited.setRoleName("新角色名称");
        edited.setRoleSort(7);
        edited.setRemark("新备注");
        when(roles.selectRoleForAuthorizationUpdate(2L)).thenReturn(saved);
        when(menus.selectMenuIdsForUpdate(2L)).thenReturn(List.of(15L, 16L));
        when(menus.batchRoleMenu(anyList())).thenReturn(2);
        when(departments.selectDeptIdsForUpdate(2L)).thenReturn(List.of(3L, 4L));
        when(members.selectMembersByRoleId(2L)).thenReturn(List.of(8L, 9L));

        transaction(mock(Connection.class)).executeWithoutResult(status -> service.updateRole(edited));
        verifyNoInteractions(ws, members);

        edited.setMenuIds(new Long[] {15L});
        var connection = mock(Connection.class);
        transaction(connection).executeWithoutResult(status -> {
            service.updateRole(edited);
            verifyNoInteractions(ws, members);
        });
        var order = inOrder(connection, members, ws);
        order.verify(connection).commit();
        order.verify(members).selectMembersByRoleId(2L);
        order.verify(ws).sendToUsers(eq(Set.of(8L, 9L)), argThat(message ->
                message.getMsgType() == WebSocketMessageType.REFRESH_BROWSER));

        clearInvocations(ws, members);
        edited.setDeptIds(new Long[] {5L});
        transaction(mock(Connection.class)).executeWithoutResult(status -> {
            assertThat(service.authDataScope(edited)).isEqualTo(1);
            status.setRollbackOnly();
        });
        verifyNoInteractions(ws, members);

        transaction(mock(Connection.class)).executeWithoutResult(status -> service.authDataScope(edited));
        verify(ws).sendToUsers(eq(Set.of(8L, 9L)), argThat(message ->
                message.getMsgType() == WebSocketMessageType.REFRESH_BROWSER));
        verify(ws, never()).broadcast(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:06
     * 说明: 模拟普通业务请求带着旧会话到达，接口鉴权先采用最新菜单权限和各角色的数据范围。
     */
    @Test
    void nextHttpRequestRechecksRoleAuthorizationWithoutWebSocket() throws Exception
    {
        var menus = mock(ISysMenuService.class);
        var permissions = new SysPermissionService();
        ReflectionTestUtils.setField(permissions, "menuService", menus);
        var tokens = mock(TokenService.class);
        var filter = new JwtAuthenticationTokenFilter();
        ReflectionTestUtils.setField(filter, "permissionService", permissions);
        ReflectionTestUtils.setField(filter, "tokenService", tokens);

        String shared = "system:user:list";
        String revoked = "system:user:edit";
        var request = new MockHttpServletRequest("GET", "/system/user/list");
        var response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SysUser user = user(3L, 2L);
        user.setRoles(List.of(authorizationRole(2L, "1", Set.of(shared, revoked)), authorizationRole(3L, "5", Set.of(shared))));
        var login = new LoginUser(2L, 3L, user, Set.of(shared, revoked));
        var latest = List.of(authorizationRole(2L, "5", Set.of()), authorizationRole(3L, "5", Set.of(shared)));
        when(tokens.getLoginUser(request)).thenReturn(login);
        when(menus.selectRoleAuthorizationsByUserId(2L)).thenReturn(latest);
        var seen = new AtomicReference<LoginUser>();

        filter.doFilter(request, response, (req, res) -> {
            var operationPermissions = new PermissionService();
            assertThat(operationPermissions.hasPermi(revoked)).isFalse();
            assertThat(operationPermissions.hasPermi(shared)).isTrue();
            seen.set(SecurityUtils.getLoginUser());
        });

        assertThat(seen.get()).isSameAs(login);
        assertThat(login.getUser().getRoles()).isEqualTo(latest);
        assertThat(login.getUser().getRoles().get(0).getDataScope()).isEqualTo("5");
        assertThat(login.getPermissions()).containsExactly(shared);
        verify(tokens).refreshToken(login);

        SecurityContextHolder.clearContext();
        clearInvocations(tokens);
        var scopeOnly = List.of(authorizationRole(2L, "5", Set.of()), authorizationRole(3L, "3", Set.of(shared)));
        when(menus.selectRoleAuthorizationsByUserId(2L)).thenReturn(scopeOnly);
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertThat(SecurityUtils.getLoginUser().getPermissions()).containsExactly(shared);
            assertThat(SecurityUtils.getLoginUser().getUser().getRoles().get(1).getDataScope()).isEqualTo("3");
        });
        verify(tokens).refreshToken(login);

        SecurityContextHolder.clearContext();
        when(menus.selectRoleAuthorizationsByUserId(2L)).thenThrow(new DataAccessResourceFailureException("isolated"));
        var unavailable = new MockHttpServletResponse();
        filter.doFilter(request, unavailable, (req, res) -> fail("权限读取失败时不得进入业务接口"));
        assertThat(unavailable.getStatus()).isEqualTo(503);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:06
     * 说明: 生成带有效状态、范围和权限的角色，用于比较保存结果和请求权限。
     */
    private SysRole authorizationRole(Long roleId, String scope, Set<String> permissions)
    {
        var role = new SysRole();
        role.setRoleId(roleId);
        role.setRoleKey("test-role-" + roleId);
        role.setStatus("0");
        role.setDataScope(scope);
        role.setPermissions(permissions);
        return role;
    }

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
        RequestContextHolder.resetRequestAttributes();
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
        var service = new AdminUserRefreshService(ws, mock(SysUserRoleMapper.class));
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
        var service = new AdminUserRefreshService(ws, mock(SysUserRoleMapper.class));
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
            new AdminUserRefreshService(ws, mock(SysUserRoleMapper.class)).afterChanged(List.of(2L));
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
        var service = new AdminUserRefreshService(ws, mock(SysUserRoleMapper.class));
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
        ReflectionTestUtils.setField(service, "clientRefreshService", new AdminUserRefreshService(ws, mock(SysUserRoleMapper.class)));
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
