package com.zero.system.service;

import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.framework.aspectj.RequiresPermissionsAspect;
import com.zero.framework.web.service.PermissionService;
import com.zero.web.controller.system.SysRoleController;
import com.zero.system.domain.SysRoleDept;
import com.zero.system.mapper.SysRoleDeptMapper;
import com.zero.system.mapper.SysRoleMapper;
import com.zero.system.mapper.SysRoleMenuMapper;
import com.zero.system.service.impl.SysRoleServiceImpl;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 16:37
 * 说明: 隔离验证新增角色的数据范围与部门关系，不连接数据库或执行真实授权
 */
class RoleCreationDataScopeTest
{
    private SysRoleServiceImpl service;
    private SysRoleMapper roles;
    private SysRoleMenuMapper menus;
    private SysRoleDeptMapper departments;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:37
     * 说明: 创建隔离Mapper替身，模拟角色插入后主键回填
     * @return
     * 返回值: void
     * 返回值说明: 每项测试使用独立Service及Mapper，不写任何真实角色
     */
    @BeforeEach
    void setUp()
    {
        service = new SysRoleServiceImpl();
        roles = mock(SysRoleMapper.class);
        menus = mock(SysRoleMenuMapper.class);
        departments = mock(SysRoleDeptMapper.class);
        ReflectionTestUtils.setField(service, "roleMapper", roles);
        ReflectionTestUtils.setField(service, "roleMenuMapper", menus);
        ReflectionTestUtils.setField(service, "roleDeptMapper", departments);
        when(roles.insertRole(any())).thenAnswer(invocation -> {
            invocation.<SysRole>getArgument(0).setRoleId(77L);
            return 1;
        });
        when(menus.batchRoleMenu(anyList())).thenReturn(1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:37
     * 说明: 五类范围均明确保存，自定范围的去重部门必须使用新角色ID，其余范围不写部门关系
     * @param scope 数据范围1至5
     * @return
     * 返回值: void
     * 返回值说明: 验证真实Service的Mapper调用与顺序
     */
    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3", "4", "5"})
    void savesChosenScopeAndOnlyCustomDepartments(String scope)
    {
        SysRole role = new SysRole();
        role.setDataScope(scope);
        role.setMenuIds(new Long[] {11L});
        role.setDeptIds(new Long[] {101L, 101L, 102L});
        assertThat(service.insertRole(role)).isEqualTo(1);
        assertThat(role.getDataScope()).isEqualTo(scope);
        var order = inOrder(roles, menus, departments);
        order.verify(roles).insertRole(role);
        order.verify(menus).batchRoleMenu(anyList());
        if (scope.equals("2"))
        {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SysRoleDept>> captor = ArgumentCaptor.forClass(List.class);
            order.verify(departments).batchRoleDept(captor.capture());
            assertThat(captor.getValue()).extracting(SysRoleDept::getRoleId).containsExactly(77L, 77L);
            assertThat(captor.getValue()).extracting(SysRoleDept::getDeptId).containsExactly(101L, 102L);
        }
        else
        {
            verifyNoInteractions(departments);
            assertThat(role.getDeptIds()).isEmpty();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:37
     * 说明: 非法范围及空自定部门在角色写入前被拒绝，旧调用缺省范围继续采用全部数据
     * @return
     * 返回值: void
     * 返回值说明: 验证缺省兼容及异常边界
     */
    @Test
    void validatesBeforeWritingAndKeepsLegacyDefault()
    {
        assertThatThrownBy(() -> service.insertRole(null)).isInstanceOf(ServiceException.class);
        SysRole role = new SysRole();
        role.setDataScope("unknown");
        assertThatThrownBy(() -> service.insertRole(role)).isInstanceOf(ServiceException.class);
        role.setDataScope("2");
        assertThatThrownBy(() -> service.insertRole(role)).isInstanceOf(ServiceException.class);
        role.setDeptIds(new Long[] {null});
        assertThatThrownBy(() -> service.insertRole(role)).isInstanceOf(ServiceException.class);
        role.setDeptIds(new Long[] {0L});
        assertThatThrownBy(() -> service.insertRole(role)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(roles, menus, departments);
        SysRole legacy = new SysRole();
        assertThat(service.insertRole(legacy)).isEqualTo(1);
        assertThat(legacy.getDataScope()).isEqualTo("1");
        assertThat(legacy.getMenuIds()).isEmpty();
        verifyNoInteractions(departments);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:37
     * 说明: 部门写入异常必须向外传播，由既有Spring事务边界处理，不能吞错返回新增成功
     * @return
     * 返回值: void
     * 返回值说明: 检查事务声明及异常传播，不将替身测试宣称为真实数据库回滚验证
     */
    @Test
    void propagatesDepartmentFailureWithinDeclaredTransaction() throws Exception
    {
        SysRole role = new SysRole();
        role.setDataScope("2");
        role.setDeptIds(new Long[] {101L});
        doThrow(new IllegalStateException("isolated failure")).when(departments).batchRoleDept(anyList());
        assertThatThrownBy(() -> service.insertRole(role)).isInstanceOf(IllegalStateException.class);
        assertThat(SysRoleServiceImpl.class.getMethod("insertRole", SysRole.class).getAnnotation(Transactional.class)).isNotNull();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:37
     * 说明: 新增部门树只依赖角色新增权限，没有角色查询或用户管理权限也可读取；无新增权限时必须拒绝
     * @return
     * 返回值: void
     * 返回值说明: 使用真实权限切面与隔离部门服务验证，不创建角色或变更授权
     */
    @Test
    void departmentTreeUsesCreationPermissionOnly()
    {
        SysRoleController controller = new SysRoleController();
        ISysDeptService deptService = mock(ISysDeptService.class);
        when(deptService.selectDeptTreeList(any())).thenReturn(List.of());
        ReflectionTestUtils.setField(controller, "deptService", deptService);
        RequiresPermissionsAspect aspect = new RequiresPermissionsAspect();
        ReflectionTestUtils.setField(aspect, "permissionService", new PermissionService());
        AspectJProxyFactory factory = new AspectJProxyFactory(controller);
        factory.addAspect(aspect);
        SysRoleController secured = factory.getProxy();
        LoginUser viewer = new LoginUser(new SysUser(), Set.of("system:role:add"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(viewer, null, List.of()));
        try
        {
            assertThat(secured.creationDeptTree().get("data")).isEqualTo(List.of());
            clearInvocations(deptService);
            viewer.setPermissions(Set.of("system:role:query"));
            assertThatThrownBy(secured::creationDeptTree).isInstanceOf(AccessDeniedException.class);
            verifyNoInteractions(deptService);
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
     * 时间: 2026/11/16 16:56
     * 说明: 隔离验证修改角色时替换部门范围或清理旧关联
     * @param scope 五种数据范围
     * @return
     * 返回值: void
     * 返回值说明: 不执行真实授权
     */
    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3", "4", "5"})
    void updateReplacesDepartmentScope(String scope)
    {
        SysRole role = new SysRole();
        role.setRoleId(77L);
        role.setDataScope(scope);
        role.setMenuIds(new Long[] {11L});
        role.setDeptIds(new Long[] {101L, 101L});
        service.updateRole(role);
        verify(roles).updateRole(role);
        verify(departments).deleteRoleDeptByRoleId(77L);
        if ("2".equals(scope))
        {
            ArgumentCaptor<List<SysRoleDept>> captor = ArgumentCaptor.forClass(List.class);
            verify(departments).batchRoleDept(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getRoleId()).isEqualTo(77L);
            assertThat(captor.getValue().get(0).getDeptId()).isEqualTo(101L);
        }
        else verify(departments, never()).batchRoleDept(anyList());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:56
     * 说明: 未传范围的旧调用不清部门，无效范围及空部门在写入前拒绝
     * @return
     * 返回值: void
     * 返回值说明: 仅验证隔离调用
     */
    @Test
    void updateValidatesBeforeWritesAndPreservesLegacyScope()
    {
        SysRole role = new SysRole();
        role.setRoleId(77L);
        role.setMenuIds(new Long[0]);
        service.updateRole(role);
        verifyNoInteractions(departments);
        clearInvocations(roles, menus);
        role.setDataScope("invalid");
        assertThatThrownBy(() -> service.updateRole(role)).isInstanceOf(ServiceException.class);
        role.setDataScope("2");
        assertThatThrownBy(() -> service.updateRole(role)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(roles, menus, departments);
    }
}
