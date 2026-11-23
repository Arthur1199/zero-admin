package com.zero.config;

import com.zero.common.annotation.Sensitive;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.enums.DesensitizedType;
import com.zero.framework.config.ApplicationConfig;
import com.zero.system.domain.SysMemo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/23 21:15
 * 说明: 使用 Spring Boot 装配的 Jackson 3 验证管理接口的字段脱敏和敏感字段隐藏契约。
 */
class JacksonWireContractTest
{
    /** 独立加载 JSON 配置，复用后台时区定制，无需数据库或 Redis。 */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withBean(org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer.class,
                    () -> new ApplicationConfig().jacksonObjectMapperCustomization());

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:15
     * 说明: 同一个共享 mapper 按当前登录身份处理两个不同脱敏规则的属性。
     * @return
     * 返回值: void
     * 返回值说明: 普通用户只收到脱敏值，管理员收到原值，无身份时按原规则脱敏。
     */
    @Test
    void preservesRedactionByFieldAndCurrentUser()
    {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(JsonMapper.class);
            JsonMapper mapper = context.getBean(JsonMapper.class);
            Contact contact = new Contact("13812348000", "alice@example.test");
            try
            {
                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                new LoginUser(new SysUser(2L), Set.of()), null, List.of()));
                assertThat(mapper.writeValueAsString(contact))
                        .contains("\"phone\":\"138****8000\"", "\"email\":\"a****@example.test\"");

                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                new LoginUser(new SysUser(1L), Set.of()), null, List.of()));
                assertThat(mapper.writeValueAsString(contact))
                        .contains("\"phone\":\"13812348000\"", "\"email\":\"alice@example.test\"");
            }
            finally
            {
                SecurityContextHolder.clearContext();
            }
            assertThat(mapper.writeValueAsString(contact))
                    .contains("\"phone\":\"138****8000\"", "\"email\":\"a****@example.test\"");
            SysMemo memo = new SysMemo();
            memo.setContentCipher("test-cipher-text");
            assertThat(mapper.writeValueAsString(memo)).doesNotContain("contentCipher", "test-cipher-text");
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:15
     * 说明: 使用两个独立注解验证 Jackson 根据属性选择正确的脱敏规则。
     * @param phone 测试手机号
     * @param email 测试邮箱
     */
    private record Contact(@Sensitive(desensitizedType = DesensitizedType.PHONE) String phone,
                           @Sensitive(desensitizedType = DesensitizedType.EMAIL) String email)
    {
    }
}
