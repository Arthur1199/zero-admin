package com.zero.jobadmin;

import com.zero.jobadmin.business.scheduler.openapi.OpenApiController;
import com.zero.jobadmin.business.service.XxlJobService;
import com.zero.jobadmin.framework.controller.IndexController;
import com.zero.jobadmin.framework.controller.LoginController;
import com.zero.jobadmin.framework.mapper.XxlJobUserMapper;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.web.xxlsso.SimpleLoginStore;
import com.zero.jobadmin.framework.web.xxlsso.XxlSsoConfig;
import com.xxl.job.core.openapi.admin.AdminBiz;
import com.xxl.job.core.openapi.admin.AdminJobBiz;
import freemarker.template.Configuration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 15:50
 * 说明: 验证Boot 3下官方Web链路与迁移资源，不启动调度线程、不连接数据库或Nacos
 */
@SpringBootTest(classes = JobAdminCompatibilityTest.WebConfiguration.class, properties = {
        "spring.config.name=job-test",
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.nacos.config.import-check.enabled=false",
        "spring.application.name=zero-job-admin-test",
        "spring.freemarker.suffix=.ftl",
        "spring.freemarker.request-context-attribute=request",
        "spring.mvc.static-path-pattern=/static/**",
        "spring.web.resources.static-locations=classpath:/static/",
        "xxl.job.i18n=zh_CN",
        "xxl-sso.token.key=job_test_cookie",
        "xxl-sso.token.timeout=604800000",
        "xxl-sso.client.excluded.paths=/actuator/health,/actuator/info",
        "xxl-sso.client.login.path=/auth/login",
        "management.endpoints.web.exposure.include=health,info",
        "management.endpoint.health.show-details=never"
})
@AutoConfigureMockMvc
class JobAdminCompatibilityTest {

    /** 测试Web入口，所有请求仅在内存中执行。 */
    @Autowired
    private MockMvc mvc;

    /** Boot创建的官方模板引擎。 */
    @Autowired
    private Configuration freemarker;

    /** 数据访问替身，阻止测试读写真实调度数据库。 */
    @MockitoBean
    private XxlJobUserMapper userMapper;

    /** 首页统计替身，不启动调度业务。 */
    @MockitoBean
    private XxlJobService jobService;

    /** 执行器回调替身，禁止实际执行任务。 */
    @MockitoBean
    private AdminBiz adminBiz;

    /** 任务管理替身，禁止实际写入任务。 */
    @MockitoBean
    private AdminJobBiz adminJobBiz;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 验证官方登录模板和静态资源在Boot 3下正常响应
     * @return
     * 返回值: void
     * 返回值说明: 登录页完成实际FreeMarker渲染且静态资源可读取
     */
    @Test
    void loginPageAndStaticResourcesRender() throws Exception {
        mvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"loginForm\"")));
        mvc.perform(get("/static/favicon.ico")).andExpect(status().isOk());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 验证独立控制台的登录拦截保留，健康检查不要求浏览器登录
     * @return
     * 返回值: void
     * 返回值说明: 首页跳转登录且health不泄露组件详情
     */
    @Test
    void consoleRequiresLoginAndHealthIsAvailable() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 验证执行器接口沿用官方协议并拒绝缺少身份参数的请求
     * @return
     * 返回值: void
     * 返回值说明: 缺少执行器凭据时返回官方失败结构，不调用调度业务
     */
    @Test
    void executorProtocolRejectsMissingCredentials() throws Exception {
        mvc.perform(post("/api/registry").contentType("application/json").content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
        org.mockito.Mockito.verifyNoInteractions(adminBiz, adminJobBiz);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 使用内存账号验证官方登录协议及登录存储调用，不持久化会话凭据
     * @return
     * 返回值: void
     * 返回值说明: 正确密码可设置登录Cookie，错误密码不建立登录态
     */
    @Test
    void officialLoginAcceptsValidPasswordAndRejectsInvalidPassword() throws Exception {
        var user = new com.zero.jobadmin.framework.model.XxlJobUser();
        user.setId(41);
        user.setUsername("compatibility-user");
        user.setPassword(com.xxl.tool.crypto.Sha256Tool.sha256("test-fixture-password"));
        org.mockito.Mockito.when(userMapper.loadByUserName("compatibility-user")).thenReturn(user);
        org.mockito.Mockito.when(userMapper.updateToken(org.mockito.ArgumentMatchers.eq(41),
                org.mockito.ArgumentMatchers.anyString())).thenReturn(1);
        mvc.perform(post("/auth/doLogin").param("userName", "compatibility-user")
                        .param("password", "test-fixture-password"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(cookie().exists("job_test_cookie"));
        mvc.perform(post("/auth/doLogin").param("userName", "compatibility-user")
                        .param("password", "incorrect-fixture-password"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(500))
                .andExpect(cookie().doesNotExist("job_test_cookie"));
        org.mockito.Mockito.verify(userMapper, org.mockito.Mockito.times(1)).updateToken(
                org.mockito.ArgumentMatchers.eq(41), org.mockito.ArgumentMatchers.anyString());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 验证数据源、邮件及调度参数集中在Nacos配置源文件，本地不重复定义
     * @return
     * 返回值: void
     * 返回值说明: 配置文件语法有效，启动入口使用独立端口、库名及受限监控配置
     */
    @Test
    void serviceYamlPreservesDeploymentBoundaries() throws Exception {
        var loader = new org.springframework.boot.env.YamlPropertySourceLoader();
        var resolver = new PathMatchingResourcePatternResolver();
        var base = loader.load("base", resolver.getResource("classpath:application.yml")).get(0);
        assertThat(base.getProperty("spring.application.name")).isEqualTo("zero-job-admin");
        assertThat(base.getProperty("server.port")).isEqualTo(9102);
        assertThat(base.getProperty("spring.datasource.url")).isNull();
        assertThat(base.getProperty("spring.datasource.type")).isNull();
        var mysql = loader.load("mysql", resolver.getResource(
                "classpath:nacosConfig/zero-job-admin-mysql.yml")).get(0);
        assertThat(mysql.getProperty("spring.datasource.url").toString()).contains("/zero_job?").doesNotContain("${");
        assertThat(mysql.getProperty("spring.datasource.type")).isEqualTo("com.zaxxer.hikari.HikariDataSource");
        assertThat(mysql.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo(30);
        var common = loader.load("common", resolver.getResource(
                "classpath:nacosConfig/zero-job-admin-common.yml")).get(0);
        for (String key : ((org.springframework.core.env.EnumerablePropertySource<?>) common).getPropertyNames()) {
            assertThat(base.getProperty(key)).as("本地不重复定义Nacos common配置：%s", key).isNull();
            assertThat(String.valueOf(common.getProperty(key))).doesNotContain("${");
        }
        assertThat(common.getProperty("spring.mail.host")).isEqualTo("localhost");
        assertThat(common.getProperty("spring.mail.properties.mail.smtp.connectiontimeout")).isEqualTo(5000);
        assertThat(common.getProperty("xxl.job.triggerpool.fast.max")).isEqualTo(300);
        assertThat(base.getProperty("management.endpoints.web.exposure.include")).isEqualTo("health,info");
        for (String profile : new String[]{"dev", "test", "prod"}) {
            var source = loader.load(profile, resolver.getResource("classpath:application-" + profile + ".yml")).get(0);
            assertThat(source.getProperty("spring.config.import[0]").toString())
                    .contains("zero-job-admin-mysql.yml?group=${spring.profiles.active}&refreshEnabled=false");
            assertThat(source.getProperty("spring.config.import[1]").toString())
                    .contains("zero-job-admin-common.yml?group=${spring.profiles.active}&refreshEnabled=false");
            assertThat(source.getProperty("spring.cloud.nacos.config.group"))
                    .isEqualTo(profile);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 解析全部官方Mapper和模板，发现包迁移遗漏和资源语法不兼容
     * @return
     * 返回值: void
     * 返回值说明: 八个Mapper可建立映射，全部模板可加载解析；不执行SQL
     */
    @Test
    void migratedMappersAndTemplatesLoad() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.apache.ibatis.session.Configuration mybatis = new org.apache.ibatis.session.Configuration();
        Resource[] mappers = resolver.getResources("classpath:/mapper/**/*Mapper.xml");
        assertThat(mappers).hasSize(8);
        for (Resource mapper : mappers) {
            try (var stream = mapper.getInputStream()) {
                new XMLMapperBuilder(stream, mybatis, mapper.toString(), mybatis.getSqlFragments()).parse();
            }
        }
        assertThat(mybatis.getMappedStatementNames()).isNotEmpty();
        Resource[] templates = resolver.getResources("classpath:/templates/**/*.ftl");
        assertThat(templates).isNotEmpty();
        for (Resource template : templates) {
            String path = template.getURL().toString();
            freemarker.getTemplate(path.substring(path.indexOf("/templates/") + "/templates/".length()));
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:53
     * 说明: 验证执行器凭据不会进入对象日志，同时保留协议认证所需原始值
     * @return
     * 返回值: void
     * 返回值说明: 诊断输出不包含测试凭据，实际凭据字段仍可正常读取
     */
    @Test
    void executorDiagnosticsDoNotExposeCredentials() {
        var group = new com.zero.jobadmin.business.model.XxlJobGroup();
        group.setAccessToken("synthetic-test-credential");
        assertThat(group.toString()).doesNotContain("synthetic-test-credential", "accessToken");
        assertThat(group.getAccessToken()).isEqualTo("synthetic-test-credential");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 只导入Web兼容性测试所需组件，隔离数据源及调度线程生命周期
     */
    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    @Import({LoginController.class, IndexController.class, OpenApiController.class,
            XxlSsoConfig.class, SimpleLoginStore.class, I18nUtil.class})
    static class WebConfiguration {
    }
}
