package com.zero.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zero.common.annotation.DataScope;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.framework.aspectj.DataScopeAspect;
import com.zero.system.mapper.SysUserMapper;
import java.util.List;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * 时间: 2026/11/16 19:23
 * 说明: 使用真实数据权限切面验证同一登录账号部门变更后的过滤及分页安全，不写真实账号数据
 */
class DepartmentDataScopeFreshnessTest
{
    private DataScopeAspect aspect;
    private SysUserMapper users;
    private SysUser cachedUser;
    private LoginUser loginUser;
    private SysRole role;
    private DataScope annotation;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 模拟登录快照仍处于旧部门103，数据库部门将由各场景指定
     * @return
     * 返回值: void
     * 返回值说明: 构造隔离请求及安全上下文
     */
    @BeforeEach
    void prepare() throws Exception
    {
        aspect = new DataScopeAspect(); users = mock(SysUserMapper.class);
        ReflectionTestUtils.setField(aspect, "userMapper", users);
        cachedUser = new SysUser(); cachedUser.setUserId(2L); cachedUser.setDeptId(103L);
        role = new SysRole(); role.setRoleId(2L); role.setStatus("0"); role.setDataScope("4"); role.setPermissions(Set.of("system:role:list"));
        cachedUser.setRoles(List.of(role));
        loginUser = new LoginUser(2L, 103L, cachedUser, Set.of("system:role:list"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        annotation = Fixture.class.getDeclaredMethod("query", BaseEntity.class).getAnnotation(DataScope.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 部门变更后下一请求用新部门，同请求只查一次，辅助读取不能消耗原分页或覆盖登录快照
     * @param scope 本部门或本部门及以下范围
     * @return
     * 返回值: void
     * 返回值说明: 验证真实切面生成的SQL条件及分页实例
     */
    @ParameterizedTest
    @ValueSource(strings = {"3", "4"})
    void nextRequestUsesCurrentDepartmentAndPreservesPage(String scope) throws Throwable
    {
        role.setDataScope(scope);
        when(users.selectCurrentDeptIdByUserId(2L)).thenAnswer(call -> {
            assertThat(PageHelper.getLocalPage()).isNull();
            return 105L;
        });
        Page<Object> page = PageHelper.startPage(2, 10, "r.role_sort"); page.setReasonable(true);
        assertThat(filter()).contains("105").doesNotContain("103");
        assertThat(PageHelper.getLocalPage()).isSameAs(page);
        assertThat(page.getPageNum()).isEqualTo(2);
        assertThat(page.getOrderBy()).isEqualTo("r.role_sort");
        assertThat(filter()).contains("105");
        verify(users, times(1)).selectCurrentDeptIdByUserId(2L);
        assertThat(cachedUser.getDeptId()).isEqualTo(103L);
        assertThat(loginUser.getDeptId()).isEqualTo(103L);
        PageHelper.clearPage();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        when(users.selectCurrentDeptIdByUserId(2L)).thenReturn(106L);
        assertThat(filter()).contains("106").doesNotContain("105", "103");
        verify(users, times(2)).selectCurrentDeptIdByUserId(2L);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 最新部门缺失或读取失败时不能回退旧部门，清理分页防止线程后续查询被错误分页
     * @return
     * 返回值: void
     * 返回值说明: 不生成旧权限条件或修改账号数据
     */
    @Test
    void failsClosedAndClearsPage() throws Throwable
    {
        when(users.selectCurrentDeptIdByUserId(2L)).thenReturn(null);
        PageHelper.startPage(1, 10);
        assertThatThrownBy(this::filter).isInstanceOf(ServiceException.class).hasMessageContaining("部门不存在");
        assertThat(PageHelper.getLocalPage()).isNull();
        when(users.selectCurrentDeptIdByUserId(2L)).thenThrow(new IllegalStateException("isolated database failure"));
        PageHelper.startPage(2, 10);
        assertThatThrownBy(this::filter).isInstanceOf(IllegalStateException.class);
        assertThat(PageHelper.getLocalPage()).isNull();
        assertThat(cachedUser.getDeptId()).isEqualTo(103L);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 全部、自定义和本人范围不额外查询部门，保留原有非部门规则
     * @param scope 非部门范围
     * @return
     * 返回值: void
     * 返回值说明: 保证本次修复不扩大到角色授权刷新
     */
    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "5"})
    void otherScopesKeepOriginalRules(String scope) throws Throwable
    {
        role.setDataScope(scope);
        String condition = filter();
        if ("1".equals(scope)) assertThat(condition).isEmpty();
        if ("2".equals(scope)) assertThat(condition).contains("sys_role_dept");
        if ("5".equals(scope)) assertThat(condition).contains("d.dept_id = 0");
        verifyNoInteractions(users);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 超级管理员仍绕过过滤，无关权限角色仍不能参与本次查询的数据范围
     * @return
     * 返回值: void
     * 返回值说明: 验证原授权边界未改变
     */
    @Test
    void preservesAdminAndPermissionBoundaries() throws Throwable
    {
        cachedUser.setUserId(1L);
        assertThat(filter()).isEmpty();
        cachedUser.setUserId(2L);
        role.setPermissions(Set.of("system:user:list"));
        assertThat(filter()).contains("d.dept_id = 0");
        verifyNoInteractions(users);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 执行真实切面入口并取得受控的数据范围片段
     * @return
     * 返回值: String
     * 返回值说明: 空字符串表示未限制，否则为服务端生成的SQL片段
     */
    private String filter() throws Throwable
    {
        BaseEntity query = new BaseEntity(); JoinPoint point = mock(JoinPoint.class);
        when(point.getArgs()).thenReturn(new Object[] {query});
        aspect.doBefore(point, annotation);
        return (String) query.getParams().get("dataScope");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 清理隔离请求的线程状态
     * @return
     * 返回值: void
     * 返回值说明: 不影响其它测试的登录、请求或分页
     */
    @AfterEach
    void clear()
    {
        PageHelper.clearPage(); SecurityContextHolder.clearContext(); RequestContextHolder.resetRequestAttributes();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:23
     * 说明: 提供与角色列表一致的部门数据权限注解，仅用于隔离测试
     */
    private static class Fixture
    {
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 19:23
         * 说明: 声明测试查询的过滤规则，方法体不执行数据库操作
         * @param query 数据范围参数
         * @return
         * 返回值: void
         * 返回值说明: 无副作用
         */
        @DataScope(deptAlias = "d", permission = "system:role:list")
        public void query(BaseEntity query) { }
    }
}
