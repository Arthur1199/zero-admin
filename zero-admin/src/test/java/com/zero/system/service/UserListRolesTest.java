package com.zero.system.service;

import com.github.pagehelper.Page;
import com.zero.common.core.domain.entity.SysDept;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.service.impl.SysUserServiceImpl;
import com.zero.common.exception.ServiceException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 18:00
 * 说明: 隔离验证用户列表批量角色回填、分页总数及Mapper参数绑定，不连接真实数据库
 */
class UserListRolesTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:00
     * 说明: 多角色仅追加到当前页用户，分页实例及total保持，角色读取仅执行一次
     * @return
     * 返回值: void
     * 返回值说明: 验证多角色、无角色与空页边界
     */
    @Test
    void enrichesCurrentPageWithoutChangingPagination()
    {
        SysUserMapper mapper = mock(SysUserMapper.class);
        SysUserServiceImpl service = new SysUserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        SysUser first = new SysUser(); first.setUserId(2L);
        SysUser second = new SysUser(); second.setUserId(3L);
        Page<SysUser> page = new Page<>(1, 2); page.setTotal(17); page.addAll(List.of(first, second));
        SysUser enriched = new SysUser(); enriched.setUserId(2L);
        SysRole common = new SysRole(); common.setRoleId(2L); common.setRoleName("普通角色");
        SysRole finance = new SysRole(); finance.setRoleId(3L); finance.setRoleName("财务角色");
        enriched.setRoles(List.of(common, finance));
        when(mapper.selectUserList(any())).thenReturn(page);
        when(mapper.selectUserRolesByUserIds(any())).thenReturn(List.of(enriched));
        assertThat(service.selectUserList(new SysUser())).isSameAs(page).hasSize(2);
        assertThat(page.getTotal()).isEqualTo(17);
        assertThat(first.getRoles()).extracting(SysRole::getRoleName).containsExactly("普通角色", "财务角色");
        assertThat(second.getRoles()).isEmpty();
        verify(mapper).selectUserRolesByUserIds(new Long[] {2L, 3L});
        clearInvocations(mapper);
        when(mapper.selectUserList(any())).thenReturn(List.of());
        assertThat(service.selectUserList(new SysUser())).isEmpty();
        verify(mapper, never()).selectUserRolesByUserIds(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:00
     * 说明: 真实解析Mapper，验证ID参数绑定、未删除角色过滤及稳定排序，避免聚合截断
     * @return
     * 返回值: void
     * 返回值说明: 仅解析SQL，不执行数据库查询
     */
    @Test
    void roleMapperBindsVisibleUserIds() throws Exception
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
        var statement = config.getMappedStatement(SysUserMapper.class.getName() + ".selectUserRolesByUserIds");
        var bound = statement.getBoundSql(Map.of("userIds", new Long[] {2L, 3L}));
        String sql = bound.getSql().replaceAll("\\s+", " ");
        assertThat(bound.getParameterMappings()).hasSize(2);
        assertThat(sql).contains("r.del_flag = '0'", "order by ur.user_id, r.role_sort, r.role_id");
        assertThat(sql.toLowerCase()).doesNotContain("group_concat", "r.status =");
        String userSql = config.getMappedStatement(SysUserMapper.class.getName() + ".selectUserList").getBoundSql(new SysUser()).getSql();
        assertThat(userSql).contains("u.create_by", "u.create_time", "u.update_by", "u.update_time");
        assertThat(userSql.replaceAll("\\s+", " ")).contains("u.del_flag = '0'");
        SysUser deletedQuery = new SysUser(); deletedQuery.setDelFlag("2");
        var deleted = config.getMappedStatement(SysUserMapper.class.getName() + ".selectUserList").getBoundSql(deletedQuery);
        assertThat(deleted.getSql().replaceAll("\\s+", " ")).contains("u.del_flag = ?");
        assertThat(deleted.getParameterMappings()).extracting(mapping -> mapping.getProperty()).contains("delFlag");
        var department = config.getMappedStatement(SysUserMapper.class.getName() + ".selectCurrentDeptIdByUserId").getBoundSql(2L);
        assertThat(department.getSql().replaceAll("\\s+", " ").trim()).isEqualTo("select dept_id from sys_user where user_id = ? and del_flag = '0'");
        assertThat(department.getParameterMappings()).hasSize(1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 19:51
     * 说明: 缺省查询未删除账号，允许显式查询逻辑删除账号，非法标志在Mapper读取前拒绝
     * @return
     * 返回值: void
     * 返回值说明: 不执行删除、恢复或真实数据库访问
     */
    @Test
    void validatesDeletionFilterWithoutChangingDefault()
    {
        SysUserMapper mapper = mock(SysUserMapper.class);
        SysUserServiceImpl service = new SysUserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        when(mapper.selectUserList(any())).thenReturn(List.of());
        SysUser normal = new SysUser();
        assertThat(service.selectUserList(normal)).isEmpty();
        assertThat(normal.getDelFlag()).isEqualTo("0");
        verify(mapper).selectUserList(normal);
        SysUser deleted = new SysUser(); deleted.setDelFlag("2");
        assertThat(service.selectUserList(deleted)).isEmpty();
        assertThat(deleted.getDelFlag()).isEqualTo("2");
        verify(mapper).selectUserList(deleted);
        clearInvocations(mapper);
        SysUser invalid = new SysUser(); invalid.setDelFlag("all");
        assertThatThrownBy(() -> service.selectUserList(invalid)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.selectUserList(null)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(mapper);
    }
}
