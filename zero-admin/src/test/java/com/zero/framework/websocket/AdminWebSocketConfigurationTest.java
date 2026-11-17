package com.zero.framework.websocket;

import java.util.Map;
import com.zero.system.domain.SysUserRole;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 隔离验证频道必填及批量取消角色SQL边界，不连接业务数据库或Redis
 */
class AdminWebSocketConfigurationTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 配置缺失时禁止回退固定dev频道，避免不同环境混收消息
     * @return
     * 返回值: void
     * 返回值说明: 频道缺失或空白均拒绝配置绑定
     */
    @Test
    void missingChannelFailsInsteadOfUsingSharedDefault()
    {
        new ApplicationContextRunner().withUserConfiguration(PropertiesOnly.class)
                .run(context -> assertThat(context).hasFailed());
        new ApplicationContextRunner().withUserConfiguration(PropertiesOnly.class)
                .withPropertyValues("zero.websocket.channel= ")
                .run(context -> assertThat(context).hasFailed());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 已配置的环境频道正常绑定，不更改其它保护参数默认值
     * @return
     * 返回值: void
     * 返回值说明: 环境配置保持可用，心跳和空闲期限正确
     */
    @Test
    void explicitChannelBindsNormally()
    {
        new ApplicationContextRunner().withUserConfiguration(PropertiesOnly.class)
                .withPropertyValues("zero.websocket.channel=isolated:config:ws")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var properties = context.getBean(AdminWebSocketProperties.class);
                    assertThat(properties.channel()).isEqualTo("isolated:config:ws");
                    assertThat(properties.heartbeatInterval()).isEqualTo(30000);
                });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 使用真实MyBatis解析器核对新增关系查询，空用户集合不能退化为全角色查询
     * @return
     * 返回值: void
     * 返回值说明: 用户及角色参数使用占位符，空集合包含恒假条件；不代表真实MySQL执行
     */
    @Test
    void roleRelationQueryIsBoundedAndParameterized() throws Exception
    {
        var configuration = new org.apache.ibatis.session.Configuration();
        configuration.getTypeAliasRegistry().registerAlias("SysUserRole", SysUserRole.class);
        String resource = "mapper/system/SysUserRoleMapper.xml";
        try (var input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        var query = configuration.getMappedStatement("com.zero.system.mapper.SysUserRoleMapper.selectUserIdsByRoleId");
        var bound = query.getBoundSql(Map.of("roleId", 3L, "userIds", new Long[] {2L, 4L}));
        assertThat(bound.getSql()).contains("role_id = ?", "user_id in", "for update");
        assertThat(bound.getParameterMappings()).hasSize(3);
        assertThat(query.getBoundSql(Map.of("roleId", 3L, "userIds", new Long[0])).getSql()).contains("and 1 = 0");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 仅装配属性绑定，不装配实际服务连接
     */
    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AdminWebSocketProperties.class)
    static class PropertiesOnly
    {
    }
}
