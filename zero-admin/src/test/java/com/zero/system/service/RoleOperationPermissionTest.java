package com.zero.system.service;

import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.framework.aspectj.RequiresPermissionsAspect;
import com.zero.framework.web.service.PermissionService;
import com.zero.system.domain.SysUserRole;
import com.zero.system.mapper.SysRoleDeptMapper;
import com.zero.system.mapper.SysRoleMapper;
import com.zero.system.service.impl.SysRoleServiceImpl;
import com.zero.web.controller.system.SysRoleController;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 17:30
 * 说明: 通过真实权限切面验证独立角色操作的正反边界，全部服务使用替身，不写真实授权
 */
class RoleOperationPermissionTest
{
    private SysRoleController secured;
    private LoginUser viewer;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 构造带真实权限切面的控制器，服务入口抛标记异常表示已通过权限边界
     * @return
     * 返回值: void
     * 返回值说明: 初始化隔离安全上下文，不执行数据库调用
     */
    @BeforeEach
    void prepare()
    {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SysRoleController controller = new SysRoleController();
        ReflectionTestUtils.setField(controller, "roleService", mock(ISysRoleService.class, call -> {
            throw new IllegalStateException("reached-service");
        }));
        PermissionService permissions = new PermissionService();
        ReflectionTestUtils.setField(controller, "operationPermissionService", permissions);
        RequiresPermissionsAspect aspect = new RequiresPermissionsAspect();
        ReflectionTestUtils.setField(aspect, "permissionService", permissions);
        AspectJProxyFactory factory = new AspectJProxyFactory(controller);
        factory.addAspect(aspect);
        secured = factory.getProxy();
        viewer = new LoginUser(new SysUser(), Set.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(viewer, null, List.of()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 为每个独立操作生成只有旧权限和只有正确新权限的两个场景
     * @return
     * 返回值: Stream
     * 返回值说明: 不包含真实账号信息的测试参数
     */
    static Stream<Arguments> boundaries()
    {
        return Stream.of("deptTree", "dataScope", "allocatedList", "unallocatedList", "cancelAuthUser", "cancelAuthUserAll", "selectAuthUserAll")
            .flatMap(method -> Stream.of(Arguments.of(method, false), Arguments.of(method, true)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 旧角色修改/新增/删除/查询权限不能调用独立操作，新权限单独具备时可通过校验
     * @param name 控制器方法
     * @param allowed 是否提供对应独立权限
     * @return
     * 返回值: void
     * 返回值说明: 检查权限拒绝或到达隔离服务标记，不执行真实操作
     */
    @ParameterizedTest
    @MethodSource("boundaries")
    void independentBoundary(String name, boolean allowed) throws Exception
    {
        String required = name.equals("deptTree") || name.equals("dataScope") ? "system:role:dataScope" : "system:role:authUser";
        viewer.setPermissions(allowed ? Set.of(required) : Set.of("system:role:edit", "system:role:query", "system:role:list", "system:role:add", "system:role:remove"));
        Class<?>[] types;
        Object[] args;
        if (name.equals("dataScope")) { types = new Class<?>[] {SysRole.class}; args = new Object[] {new SysRole()}; }
        else if (name.endsWith("List")) { types = new Class<?>[] {SysUser.class}; SysUser user = new SysUser(); user.setRoleId(2L); args = new Object[] {user}; }
        else if (name.equals("cancelAuthUser")) { types = new Class<?>[] {SysUserRole.class}; SysUserRole relation = new SysUserRole(); relation.setRoleId(2L); args = new Object[] {relation}; }
        else if (name.endsWith("All")) { types = new Class<?>[] {Long.class, Long[].class}; args = new Object[] {2L, new Long[] {3L}}; }
        else { types = new Class<?>[] {Long.class}; args = new Object[] {2L}; }
        Throwable failure = catchThrowable(() -> {
            try { SysRoleController.class.getMethod(name, types).invoke(secured, args); }
            catch (InvocationTargetException ex) { throw ex.getCause(); }
        });
        if (allowed) assertThat(failure).isInstanceOf(IllegalStateException.class).hasMessage("reached-service");
        else assertThat(failure).isInstanceOf(AccessDeniedException.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 普通修改接口不能提交数据权限；独立数据权限也不能调用基础修改接口
     * @return
     * 返回值: void
     * 返回值说明: 两种绕过路径均在写入前拒绝
     */
    @Test
    void rejectsEditingBypass()
    {
        SysRole role = new SysRole(); role.setRoleId(2L); role.setDataScope("1");
        viewer.setPermissions(Set.of("system:role:edit"));
        assertThatThrownBy(() -> secured.edit(role)).isInstanceOf(AccessDeniedException.class);
        viewer.setPermissions(Set.of("system:role:dataScope"));
        assertThatThrownBy(() -> secured.edit(role)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> secured.getInfo(2L)).isInstanceOf(IllegalStateException.class).hasMessage("reached-service");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 数据权限保存必须使用限定字段的Mapper，不能调用基础角色更新
     * @return
     * 返回值: void
     * 返回值说明: 验证服务调用隔离，不连接真实数据库
     */
    @Test
    void scopeUsesRestrictedMapper()
    {
        SysRoleServiceImpl service = new SysRoleServiceImpl();
        SysRoleMapper roles = mock(SysRoleMapper.class);
        SysRoleDeptMapper departments = mock(SysRoleDeptMapper.class);
        ReflectionTestUtils.setField(service, "roleMapper", roles);
        ReflectionTestUtils.setField(service, "roleDeptMapper", departments);
        SysRole role = new SysRole(); role.setRoleId(2L); role.setDataScope("1"); role.setRoleKey("forged");
        when(roles.updateRoleDataScope(role)).thenReturn(1);
        assertThat(service.authDataScope(role)).isEqualTo(1);
        verify(roles).updateRoleDataScope(role);
        verify(roles, never()).updateRole(any());
        verify(departments).deleteRoleDeptByRoleId(2L);
        verify(departments, never()).batchRoleDept(anyList());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 释放测试线程的登录上下文，避免影响其它测试
     * @return
     * 返回值: void
     * 返回值说明: 清理当前线程状态
     */
    @AfterEach
    void clear()
    {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }
}
