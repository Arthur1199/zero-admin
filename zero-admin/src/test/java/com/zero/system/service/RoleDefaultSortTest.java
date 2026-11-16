package com.zero.system.service;

import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.framework.aspectj.RequiresPermissionsAspect;
import com.zero.framework.web.service.PermissionService;
import com.zero.system.mapper.SysRoleMapper;
import com.zero.system.service.impl.SysRoleServiceImpl;
import com.zero.web.controller.system.SysRoleController;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 18:21
 * 说明: 隔离验证角色新增默认顺序计算、整数边界、接口权限及全量聚合SQL，不创建角色
 */
class RoleDefaultSortTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:21
     * 说明: 最大顺序加1，历史负数不产生负建议值，合法整数上界不溢出
     * @param current 当前最大顺序
     * @param expected 建议顺序
     * @return
     * 返回值: void
     * 返回值说明: 不调用任何写入方法
     */
    @ParameterizedTest
    @CsvSource({"2,3", "6,7", "-1,1", "0,1", "2147483646,2147483647"})
    void suggestsNextSort(long current, int expected)
    {
        SysRoleMapper mapper = mock(SysRoleMapper.class);
        SysRoleServiceImpl service = new SysRoleServiceImpl();
        ReflectionTestUtils.setField(service, "roleMapper", mapper);
        when(mapper.selectMaxRoleSort()).thenReturn(current);
        assertThat(service.selectNextRoleSort()).isEqualTo(expected);
        verify(mapper).selectMaxRoleSort();
        verifyNoMoreInteractions(mapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:21
     * 说明: 空表从1开始，超出整数范围时拒绝返回错误默认值
     * @return
     * 返回值: void
     * 返回值说明: 校验空值及溢出边界
     */
    @Test
    void handlesEmptyAndOverflow()
    {
        SysRoleMapper mapper = mock(SysRoleMapper.class);
        SysRoleServiceImpl service = new SysRoleServiceImpl();
        ReflectionTestUtils.setField(service, "roleMapper", mapper);
        assertThat(service.selectNextRoleSort()).isEqualTo(1);
        when(mapper.selectMaxRoleSort()).thenReturn((long) Integer.MAX_VALUE);
        assertThatThrownBy(service::selectNextRoleSort).isInstanceOf(ServiceException.class).hasMessageContaining("上限");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:21
     * 说明: 建议顺序接口只要求角色新增权限，普通查询权限不能代替新增授权
     * @return
     * 返回值: void
     * 返回值说明: 真实权限切面的隔离正反例
     */
    @Test
    void requiresCreationPermission()
    {
        ISysRoleService service = mock(ISysRoleService.class);
        when(service.selectNextRoleSort()).thenReturn(7);
        SysRoleController controller = new SysRoleController();
        ReflectionTestUtils.setField(controller, "roleService", service);
        RequiresPermissionsAspect aspect = new RequiresPermissionsAspect();
        ReflectionTestUtils.setField(aspect, "permissionService", new PermissionService());
        AspectJProxyFactory factory = new AspectJProxyFactory(controller); factory.addAspect(aspect);
        SysRoleController secured = factory.getProxy();
        LoginUser viewer = new LoginUser(new SysUser(), Set.of("system:role:add"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(viewer, null, List.of()));
        try
        {
            assertThat(secured.nextSort().get("data")).isEqualTo(7);
            viewer.setPermissions(Set.of("system:role:query"));
            assertThatThrownBy(secured::nextSort).isInstanceOf(AccessDeniedException.class);
        }
        finally { SecurityContextHolder.clearContext(); RequestContextHolder.resetRequestAttributes(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:21
     * 说明: 真实解析SQL确认从未删除角色聚合最大值，无分页或状态过滤
     * @return
     * 返回值: void
     * 返回值说明: 仅解析Mapper，不连接数据库
     */
    @Test
    void aggregatesAllUndeletedRoles() throws Exception
    {
        Configuration config = new Configuration(); config.getTypeAliasRegistry().registerAlias("SysRole", SysRole.class);
        String resource = "mapper/system/SysRoleMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        String sql = config.getMappedStatement(SysRoleMapper.class.getName() + ".selectMaxRoleSort").getBoundSql(null).getSql().replaceAll("\\s+", " ").trim();
        assertThat(sql).isEqualTo("select max(role_sort) from sys_role where del_flag = '0'");
        String listSql = config.getMappedStatement(SysRoleMapper.class.getName() + ".selectRoleList").getBoundSql(new SysRole()).getSql();
        assertThat(listSql).contains("r.create_by", "r.create_time", "r.update_by", "r.update_time");
    }
}
