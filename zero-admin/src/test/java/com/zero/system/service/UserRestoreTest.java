package com.zero.system.service;

import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.domain.entity.SysDept;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.framework.aspectj.DataScopeAspect;
import com.zero.framework.security.context.PermissionContextHolder;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.service.impl.SysUserServiceImpl;
import com.zero.web.controller.system.SysUserController;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
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
 * 时间: 2026/11/16 20:21
 * 说明: 隔离验证用户恢复的数据范围、基础恢复、条件更新及接口权限，不写真实数据库
 */
class UserRestoreTest
{
    private SysUserMapper mapper;
    private SysUserServiceImpl service;
    private SysUser deleted;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 创建已删除账号和超级管理员安全上下文
     * @return
     * 返回值: void
     * 返回值说明: 每个场景独立构造模拟数据
     */
    @BeforeEach
    void prepare()
    {
        mapper = mock(SysUserMapper.class);
        service = new SysUserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        deleted = new SysUser(3L);
        deleted.setUserName("deleted-user"); deleted.setDelFlag("2"); deleted.setDeptId(105L);
        deleted.setStatus("1"); deleted.setPhonenumber("123"); deleted.setEmail("mock@example.test");
        when(mapper.selectUserById(3L)).thenReturn(deleted);
        when(mapper.restoreUser(any())).thenReturn(1);
        LoginUser login = new LoginUser(1L, 103L, new SysUser(1L), Set.of("*:*:*"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, List.of()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 清理安全及请求线程上下文
     * @return
     * 返回值: void
     * 返回值说明: 避免测试间共享身份
     */
    @AfterEach
    void cleanup()
    {
        SecurityContextHolder.clearContext(); RequestContextHolder.resetRequestAttributes();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 成功恢复保留停用状态，不依赖角色及岗位写入组件
     * @return
     * 返回值: void
     * 返回值说明: 仅调用账号条件更新，补充审计人
     */
    @Test
    void restoresWithoutGrantingRolesOrChangingStatus()
    {
        assertThat(service.restoreUser(3L, "operator")).isEqualTo(1);
        verify(mapper).restoreUser(argThat(user -> "operator".equals(user.getUpdateBy()) && "1".equals(user.getStatus()) && user.getDeptId() == 105L));
        verify(mapper, never()).selectUserList(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 恢复只处理删除标志，不额外执行账号、手机号和邮箱唯一性校验
     * @return
     * 返回值: void
     * 返回值说明: 保留简单恢复语义
     */
    @Test
    void doesNotAddUniquenessChecks()
    {
        assertThat(service.restoreUser(3L, "operator")).isEqualTo(1);
        verify(mapper, never()).checkUserNameUnique(any());
        verify(mapper, never()).checkPhoneUnique(any());
        verify(mapper, never()).checkEmailUnique(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 非法参数、记录状态、管理员保护和并发变化均拒绝
     * @return
     * 返回值: void
     * 返回值说明: 不把重复恢复当作成功
     */
    @Test
    void rejectsInvalidStateAndConcurrentChanges()
    {
        assertThatThrownBy(() -> service.restoreUser(null, "operator")).hasMessageContaining("参数无效");
        assertThatThrownBy(() -> service.restoreUser(3L, " ")).hasMessageContaining("参数无效");
        assertThatThrownBy(() -> service.restoreUser(4L, "operator")).hasMessage("账号不存在");
        when(mapper.selectUserById(1L)).thenReturn(new SysUser(1L));
        assertThatThrownBy(() -> service.restoreUser(1L, "operator")).hasMessageContaining("超级管理员");
        deleted.setDelFlag("0");
        assertThatThrownBy(() -> service.restoreUser(3L, "operator")).hasMessageContaining("未删除或已恢复");
        verify(mapper, never()).restoreUser(any());
        deleted.setDelFlag("2"); when(mapper.restoreUser(any())).thenReturn(0);
        assertThatThrownBy(() -> service.restoreUser(3L, "operator")).hasMessageContaining("状态或信息已变化");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 使用真实AOP验证恢复查已删除用户并使用最新归属部门，越权时拒绝
     * @return
     * 返回值: void
     * 返回值说明: 校验普通用户不能跳过数据过滤
     */
    @Test
    void restoresOnlyInsideCurrentDataScope()
    {
        SysUser current = new SysUser(2L); current.setDeptId(103L);
        SysRole role = new SysRole(); role.setRoleId(2L); role.setStatus("0"); role.setDataScope("4"); role.setPermissions(Set.of("system:user:remove"));
        current.setRoles(List.of(role));
        LoginUser login = new LoginUser(2L, 103L, current, Set.of("system:user:remove"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, List.of()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        PermissionContextHolder.setContext("system:user:remove");
        when(mapper.selectCurrentDeptIdByUserId(2L)).thenReturn(105L);
        when(mapper.selectUserList(any())).thenAnswer(call -> {
            SysUser query = call.getArgument(0);
            assertThat(query.getDelFlag()).isEqualTo("2");
            assertThat(query.getUserId()).isEqualTo(3L);
            assertThat(query.getParams().get("dataScope").toString()).contains("105").doesNotContain("103");
            return List.of(deleted);
        });
        DataScopeAspect aspect = new DataScopeAspect(); ReflectionTestUtils.setField(aspect, "userMapper", mapper);
        AspectJProxyFactory factory = new AspectJProxyFactory(service); factory.setProxyTargetClass(true); factory.setExposeProxy(true); factory.addAspect(aspect);
        SysUserServiceImpl proxy = factory.getProxy();
        assertThat(proxy.restoreUser(3L, "operator")).isEqualTo(1);
        clearInvocations(mapper);
        doReturn(List.of()).when(mapper).selectUserList(any());
        assertThatThrownBy(() -> proxy.restoreUser(3L, "operator")).hasMessageContaining("没有权限");
        verify(mapper, never()).restoreUser(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:21
     * 说明: 解析真实Mapper校验条件更新和控制器复用删除权限
     * @return
     * 返回值: void
     * 返回值说明: 静态验证参数绑定和更新边界，不执行SQL
     */
    @Test
    void mapperAndControllerKeepRestoreBoundary() throws Exception
    {
        Configuration config = new Configuration();
        config.getTypeAliasRegistry().registerAlias("SysUser", SysUser.class);
        config.getTypeAliasRegistry().registerAlias("SysRole", SysRole.class);
        config.getTypeAliasRegistry().registerAlias("SysDept", SysDept.class);
        String resource = "mapper/system/SysUserMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        var bound = config.getMappedStatement(SysUserMapper.class.getName() + ".restoreUser").getBoundSql(deleted);
        String sql = bound.getSql().replaceAll("\\s+", " ");
        assertThat(sql).contains("set del_flag = '0', update_by = ?, update_time = sysdate()", "del_flag = '2'", "dept_id <=> ?");
        assertThat(sql.substring(0, sql.indexOf("where"))).doesNotContain("status", "password", "dept_id");
        assertThat(bound.getParameterMappings()).hasSize(3);
        assertThat(SysUserController.class.getMethod("restore", Long.class).getAnnotation(RequiresPermissions.class).value()).containsExactly("system:user:remove");
    }
}
