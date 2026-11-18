package com.zero.jobadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.jobadmin.business.controller.JobCodeController;
import com.zero.jobadmin.business.controller.JobGroupController;
import com.zero.jobadmin.business.controller.JobLogController;
import com.zero.jobadmin.business.controller.JobInfoController;
import com.zero.jobadmin.business.mapper.JobDashboardMapper;
import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.business.mapper.XxlJobInfoMapper;
import com.zero.jobadmin.business.mapper.XxlJobLockMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogGlueMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogMapper;
import com.zero.jobadmin.business.mapper.XxlJobLogReportMapper;
import com.zero.jobadmin.business.mapper.XxlJobRegistryMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.model.XxlJobLogReport;
import com.zero.jobadmin.business.model.XxlJobRegistry;
import com.zero.jobadmin.business.model.dto.JobDashboardReport.DayResult;
import com.zero.jobadmin.business.model.dto.JobScheduleUpdate;
import com.zero.jobadmin.business.scheduler.alarm.JobAlarmer;
import com.zero.jobadmin.business.scheduler.complete.JobCompleter;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.openapi.OpenApiController;
import com.zero.jobadmin.business.scheduler.openapi.impl.AdminBizImpl;
import com.zero.jobadmin.business.scheduler.openapi.impl.AdminJobBizImpl;
import com.zero.jobadmin.business.scheduler.thread.JobCompleteHelper;
import com.zero.jobadmin.business.scheduler.thread.JobLogReportHelper;
import com.zero.jobadmin.business.scheduler.thread.JobFailAlarmMonitorHelper;
import com.zero.jobadmin.business.scheduler.thread.JobRegistryHelper;
import com.zero.jobadmin.business.scheduler.thread.JobScheduleHelper;
import com.zero.jobadmin.business.scheduler.thread.JobTriggerPoolHelper;
import com.zero.jobadmin.business.scheduler.trigger.JobTrigger;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import com.zero.jobadmin.business.service.JobDashboardService;
import com.zero.jobadmin.business.service.JobLogCleanup;
import com.zero.jobadmin.business.service.XxlJobService;
import com.zero.jobadmin.business.service.impl.XxlJobServiceImpl;
import com.zero.jobadmin.framework.controller.IndexController;
import com.zero.jobadmin.framework.controller.JobUserController;
import com.zero.jobadmin.framework.controller.LoginController;
import com.zero.jobadmin.framework.mapper.XxlJobUserMapper;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.JobGroupPermissionUtil;
import com.zero.jobadmin.framework.web.xxlsso.SimpleLoginStore;
import com.zero.jobadmin.framework.web.xxlsso.XxlSsoConfig;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.openapi.admin.dto.CallbackData;
import com.xxl.job.core.openapi.admin.dto.CallbackRequest;
import com.xxl.job.core.openapi.admin.dto.RegistryRequest;
import com.xxl.sso.core.model.LoginInfo;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
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
        "xxl-sso.client.excluded.paths=/actuator,/actuator/**",
        "xxl-sso.client.login.path=/auth/login",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.health.show-details=always"
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

    /** 用内存数据验证报表的组权限、留存日期范围和分页。 */
    @MockitoBean
    private JobDashboardMapper dashboardMapper;

    /** 模拟执行器组与任务查询，避免访问真实数据库。 */
    @MockitoBean
    private XxlJobGroupMapper groupMapper;
    @MockitoBean
    private XxlJobInfoMapper jobInfoMapper;
    @MockitoBean
    private XxlJobLogMapper jobLogMapper;
    @MockitoBean
    private XxlJobLogGlueMapper jobLogGlueMapper;
    @MockitoBean
    private XxlJobRegistryMapper registryMapper;

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
        mvc.perform(get("/static/zero-job-favicon.png")).andExpect(status().isOk());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:08
     * 说明: 用普通账号验证全组查询仍受授权限制、日期补齐与失败榜下钻
     * @return
     * 返回值: void
     * 返回值说明: 未登录无法查询，未授权组不查询明细，下钻保留日期和历史任务编号
     */
    @Test
    void dashboardUsesVisibleGroupAndKeepsDrilldownFilters() throws Exception {
        var user = new com.zero.jobadmin.framework.model.XxlJobUser();
        user.setId(42);
        user.setUsername("report-fixture");
        user.setPassword(com.xxl.tool.crypto.Sha256Tool.sha256("test-fixture-password"));
        user.setPermission("2");
        when(userMapper.loadByUserName("report-fixture")).thenReturn(user);
        when(userMapper.loadById(42)).thenReturn(user);
        when(userMapper.updateToken(eq(42), anyString())).thenAnswer(invocation -> {
            user.setToken(invocation.getArgument(1));
            return 1;
        });

        assertThatThrownBy(() -> mvc.perform(get("/dashboard/report")
                .param("startDate", "2026-01-01").param("endDate", "2026-01-02")))
                .hasRootCauseInstanceOf(com.xxl.sso.core.exception.XxlSsoException.class);
        var login = mvc.perform(post("/auth/doLogin").param("userName", "report-fixture")
                        .param("password", "test-fixture-password"))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getCookie("job_test_cookie");
        assertThat(login).isNotNull();

        var allowed = new XxlJobGroup();
        allowed.setId(2);
        allowed.setName("A < B");
        var denied = new XxlJobGroup();
        denied.setId(7);
        denied.setName("Hidden group");
        when(groupMapper.findAll()).thenReturn(List.of(allowed, denied));
        when(jobService.dashboardInfo()).thenReturn(java.util.Map.of(
                "jobInfoCount", 3, "jobLogCount", 4, "executorCount", 1));
        when(jobService.chartInfo(any(), any())).thenReturn(com.xxl.tool.response.Response.ofSuccess(
                java.util.Map.of("triggerDayList", List.of("2026-01-01"))));
        var first = LocalDate.of(2026, 1, 1);
        var last = first.plusDays(1);
        var until = last.plusDays(1).atStartOfDay();
        when(dashboardMapper.trend(first.atStartOfDay(), until, List.of(2)))
                .thenReturn(List.of(new DayResult(last, 1, 2, 3, 12)));

        mvc.perform(get("/dashboard").cookie(login))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("A &lt; B")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"lineChart\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"pieChart\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"reportLineChart\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden group"))));
        mvc.perform(get("/jobinfo").cookie(login).param("jobGroup", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("A &lt; B")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden group"))));
        mvc.perform(post("/chartInfo").cookie(login).param("startDate", "2026-01-01 00:00:00")
                        .param("endDate", "2026-01-02 23:59:59"))
                .andExpect(jsonPath("$.data.triggerDayList[0]").value("2026-01-01"));
        mvc.perform(get("/dashboard/report").cookie(login).param("startDate", first.toString())
                        .param("endDate", last.toString()).param("jobGroup", "7"))
                .andExpect(jsonPath("$.code").value(500));
        mvc.perform(get("/dashboard/report").cookie(login).param("startDate", first.toString())
                        .param("endDate", "2026-02-01"))
                .andExpect(jsonPath("$.code").value(500));
        verifyNoInteractions(dashboardMapper);

        mvc.perform(get("/dashboard/report").cookie(login).param("startDate", first.toString())
                        .param("endDate", last.toString()).param("page", "99"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trend[0].day").value(first.toString()))
                .andExpect(jsonPath("$.data.trend[0].pending").value(0))
                .andExpect(jsonPath("$.data.trend[1].dispatchFailed").value(2))
                .andExpect(jsonPath("$.data.pendingTotal").value(12))
                .andExpect(jsonPath("$.data.page").value(2));
        verify(dashboardMapper).topFailures(first.atStartOfDay(), until, List.of(2));
        verify(dashboardMapper).pending(first.atStartOfDay(), until, List.of(2), 10L, 10);

        mvc.perform(get("/joblog").cookie(login).param("jobGroup", "2").param("jobId", "123")
                        .param("failedOnly", "true").param("startDate", first.toString()).param("endDate", last.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("jobId", 123))
                .andExpect(model().attribute("reportHistoricalJob", true))
                .andExpect(model().attribute("reportToday", org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}")))
                .andExpect(model().attribute("reportStart", first.toString()))
                .andExpect(model().attribute("reportEnd", last.toString()));
        verifyNoInteractions(jobLogMapper);

        for (String invalidTime : List.of("invalid", "2026-02-30 00:00:00 - 2026-03-01 23:59:59",
                "2026-01-02 00:00:00 - 2026-01-01 23:59:59")) {
            mvc.perform(post("/joblog/pageList").cookie(login).param("jobGroup", "2").param("jobId", "123")
                            .param("logStatus", "2").param("filterTime", invalidTime))
                    .andExpect(jsonPath("$.code").value(500));
        }
        mvc.perform(post("/joblog/pageList").cookie(login).param("jobGroup", "2").param("jobId", "123")
                        .param("logStatus", "2").param("pagesize", "501").param("filterTime", ""))
                .andExpect(jsonPath("$.code").value(500));
        verifyNoInteractions(jobLogMapper);

        for (String action : List.of("logKill", "clearLog")) {
            mvc.perform(get("/joblog/" + action).cookie(login).param("id", "20")
                            .param("jobGroup", "2").param("jobId", "123").param("type", "9"))
                    .andExpect(status().isMethodNotAllowed());
            mvc.perform(head("/joblog/" + action).cookie(login))
                    .andExpect(status().isMethodNotAllowed());
        }
        verifyNoInteractions(jobLogMapper);
        mvc.perform(post("/joblog/clearLog").cookie(login).param("jobGroup", "2")
                        .param("jobId", "123").param("type", "1"))
                .andExpect(jsonPath("$.code").value(200));
        verify(jobLogMapper).findMaxClearLogId(2, 123);
        verify(jobLogMapper, never()).clearLog(anyList());

        var firstSecond = java.util.Date.from(java.time.Instant.parse("2025-12-31T16:00:00Z"));
        var nextDayStart = java.util.Date.from(java.time.Instant.parse("2026-01-02T16:00:00Z"));
        long longLogId = 9_007_199_254_740_993L;
        var longLog = new XxlJobLog();
        longLog.setId(longLogId);
        longLog.setJobGroup(2);
        longLog.setJobId(123);
        longLog.setTriggerTime(firstSecond);
        longLog.setHandleTime(nextDayStart);
        when(jobLogMapper.pageList(0, 10, 2, 123, firstSecond, nextDayStart, 2)).thenReturn(List.of(longLog));
        when(jobLogMapper.load(longLogId)).thenReturn(longLog);
        mvc.perform(post("/joblog/pageList").cookie(login).param("jobGroup", "2").param("jobId", "123")
                        .param("logStatus", "2").param("filterTime", "2026-01-01 00:00:00 - 2026-01-02 23:59:59"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.data[0].id").isString())
                .andExpect(jsonPath("$.data.data[0].id").value(Long.toString(longLogId)))
                .andExpect(jsonPath("$.data.data[0].triggerTime").value("2026-01-01 00:00:00"))
                .andExpect(jsonPath("$.data.data[0].handleTime").value("2026-01-03 00:00:00"));
        verify(jobLogMapper).pageList(0, 10, 2, 123, firstSecond, nextDayStart, 2);
        verify(jobLogMapper).pageListCount(0, 10, 2, 123, firstSecond, nextDayStart, 2);
        mvc.perform(get("/joblog/logDetailPage").cookie(login).param("id", Long.toString(longLogId)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("var logId = '" + longLogId + "';")));
        verify(jobLogMapper).load(longLogId);

        var historicalLog = new XxlJobLog();
        historicalLog.setId(20L);
        historicalLog.setJobGroup(2);
        historicalLog.setJobId(123);
        when(jobLogMapper.load(20L)).thenReturn(historicalLog);
        mvc.perform(get("/joblog/logDetailPage").cookie(login).param("id", "20"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("jobName", "#123"));

        mvc.perform(post("/joblog/logKill").cookie(login).param("id", "999"))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("joblog_logid_invalid")));
        var job = new com.zero.jobadmin.business.model.XxlJobInfo();
        job.setJobGroup(7);
        when(jobInfoMapper.loadById(123)).thenReturn(job);
        mvc.perform(post("/joblog/logKill").cookie(login).param("id", "20"))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("jobinfo_glue_jobid_invalid")));
        job.setJobGroup(2);
        historicalLog.setTriggerCode(200);
        historicalLog.setHandleCode(200);
        mvc.perform(post("/joblog/logKill").cookie(login).param("id", "20"))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("joblog_kill_finished")));
        mvc.perform(post("/joblog/logDetailCat").cookie(login).param("logId", "20").param("fromLineNum", "0"))
                .andExpect(jsonPath("$.code").value(500));
        historicalLog.setTriggerTime(new java.util.Date());
        mvc.perform(post("/joblog/logDetailCat").cookie(login).param("logId", "20").param("fromLineNum", "1"))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("jobgroup_empty")));

        job.setId(123);
        historicalLog.setHandleCode(0);
        historicalLog.setExecutorAddress("http://fixture.invalid");
        when(groupMapper.load(2)).thenReturn(allowed);
        var executor = mock(com.xxl.job.core.openapi.executor.ExecutorBiz.class);
        when(executor.kill(argThat(kill -> kill.getJobId() == 123)))
                .thenReturn(com.xxl.tool.response.Response.ofSuccess());
        String rawLog = "  <node> & <b>值</b> ###TAG_BR###\r\n";
        when(executor.log(any())).thenAnswer(invocation -> com.xxl.tool.response.Response.ofSuccess(
                new com.xxl.job.core.openapi.executor.dto.LogData(1, 1, rawLog, false)));
        var completer = new JobCompleter();
        ReflectionTestUtils.setField(completer, "xxlJobLogMapper", jobLogMapper);
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        when(bootstrap.getJobCompleter()).thenReturn(completer);
        when(jobLogMapper.markManuallyTerminated(any())).thenReturn(1, 0)
                .thenThrow(new IllegalStateException("fixture persistence error"));
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            bootstrapStatic.when(() -> XxlJobAdminBootstrap.getExecutorBiz(historicalLog.getExecutorAddress(), allowed))
                    .thenReturn(executor);
            mvc.perform(post("/joblog/logKill").cookie(login).param("id", "20"))
                    .andExpect(jsonPath("$.code").value(200));
            mvc.perform(post("/joblog/logKill").cookie(login).param("id", "20"))
                    .andExpect(jsonPath("$.msg").value(I18nUtil.getString("joblog_kill_state_changed")));
            mvc.perform(post("/joblog/logKill").cookie(login).param("id", "20"))
                    .andExpect(jsonPath("$.msg").value(I18nUtil.getString("joblog_kill_store_failed")));
            mvc.perform(post("/joblog/logDetailCat").cookie(login).param("logId", "20")
                            .param("fromLineNum", "1").param("plainText", "true"))
                    .andExpect(jsonPath("$.data.logContent").value(rawLog));
            mvc.perform(post("/joblog/logDetailCat").cookie(login).param("logId", "20").param("fromLineNum", "1"))
                    .andExpect(jsonPath("$.data.logContent").value("  &lt;node&gt; &amp; <b>值</b> ###TAG_BR###\r\n"));
        }
        verify(jobLogMapper, times(3)).markManuallyTerminated(argThat(log -> log.getId() == 20
                && log.getJobGroup() == 2 && log.getHandleCode() == 500));
        verify(jobLogMapper, never()).updateHandleInfo(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:45
     * 说明: 验证任务配置更新同时校验原组和目标组，并处理 GLUE 可空字段和非法子任务编号
     * @return
     * 返回值: void
     * 返回值说明: 无权移组和非法输入不会更新任务，正常更新保留调度进度
     */
    @Test
    void taskUpdateChecksBothGroupsAndValidatesOptionalFields() {
        var service = new XxlJobServiceImpl();
        ReflectionTestUtils.setField(service, "xxlJobInfoMapper", jobInfoMapper);
        ReflectionTestUtils.setField(service, "xxlJobGroupMapper", groupMapper);
        var login = new LoginInfo("1", "test-signature");
        login.setUserName("fixture");
        login.setExtraInfo(java.util.Map.of("jobGroups", "2"));

        var stored = new XxlJobInfo();
        stored.setId(42);
        stored.setJobGroup(7);
        stored.setGlueType("GLUE_SHELL");
        stored.setScheduleType("NONE");
        when(jobInfoMapper.loadById(42)).thenReturn(stored);
        when(groupMapper.load(2)).thenReturn(new XxlJobGroup());

        var edit = new XxlJobInfo();
        edit.setId(42);
        edit.setJobGroup(2);
        edit.setName("Example");
        edit.setAuthor("Fixture");
        edit.setScheduleType("NONE");
        edit.setMisfireStrategy("DO_NOTHING");
        edit.setExecutorRouteStrategy("FIRST");
        edit.setExecutorBlockStrategy("SERIAL_EXECUTION");
        assertThat(service.update(edit, login).getMsg()).isEqualTo(I18nUtil.getString("system_permission_limit"));
        stored.setJobGroup(2);
        edit.setJobGroup(7);
        assertThat(service.update(edit, login).getMsg()).isEqualTo(I18nUtil.getString("system_permission_limit"));

        edit.setJobGroup(2);
        edit.setChildJobId("999999999999999");
        assertThat(service.update(edit, login).isSuccess()).isFalse();
        verify(jobInfoMapper, never()).updateInGroup(any(), anyInt(), anyBoolean());

        edit.setChildJobId(null);
        when(jobInfoMapper.updateInGroup(stored, 2, false)).thenReturn(1, 0);
        assertThat(service.update(edit, login).isSuccess()).isTrue();
        assertThat(stored.getExecutorHandler()).isEmpty();
        assertThat(service.update(edit, login).isSuccess()).isFalse();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:41
     * 说明: 验证新增和编辑遵循数据库的字符及字节容量。
     * @return
     * 返回值: void
     * 返回值说明: 超长字段不会写库，达到合法上限时仍可新增
     */
    @Test
    void taskConfigurationChecksColumnSizesBeforeWrite() {
        var service = new XxlJobServiceImpl();
        ReflectionTestUtils.setField(service, "xxlJobInfoMapper", jobInfoMapper);
        ReflectionTestUtils.setField(service, "xxlJobGroupMapper", groupMapper);
        var login = new LoginInfo("1", "test-signature");
        login.setUserName("fixture");
        login.setExtraInfo(java.util.Map.of("jobGroups", "2"));
        var job = new XxlJobInfo();
        job.setId(42);
        job.setJobGroup(2);
        job.setName("任务");
        job.setAuthor("负".repeat(65));
        var existing = new XxlJobInfo();
        existing.setJobGroup(2);
        when(jobInfoMapper.loadById(42)).thenReturn(existing);

        assertThat(service.add(job, login).getMsg()).contains(I18nUtil.getString("jobinfo_field_author"), "64");
        assertThat(service.update(job, login).getMsg()).contains(I18nUtil.getString("jobinfo_field_author"), "64");
        job.setAuthor("😀".repeat(64));
        job.setExecutorParam("参".repeat(21846));
        assertThat(service.update(job, login).isSuccess()).isFalse();
        assertThat(service.add(job, login).isSuccess()).isFalse();
        verify(jobInfoMapper, never()).save(any());
        verify(jobInfoMapper, never()).updateInGroup(any(), anyInt(), anyBoolean());

        job.setExecutorParam("参".repeat(21845));
        job.setName("任".repeat(255));
        job.setScheduleType("NONE");
        job.setGlueType("BEAN");
        job.setExecutorHandler("fixtureHandler");
        job.setExecutorRouteStrategy("FIRST");
        job.setExecutorBlockStrategy("SERIAL_EXECUTION");
        job.setMisfireStrategy("DO_NOTHING");
        when(groupMapper.load(2)).thenReturn(new XxlJobGroup());
        String oversizedNoneConfig = "x".repeat(129);
        job.setScheduleConf(oversizedNoneConfig);
        var preview = new JobInfoController().nextTriggerTime(null, null, "NONE", oversizedNoneConfig);
        assertThat(preview.isSuccess()).isFalse();
        assertThat(service.add(job, login).getMsg()).isEqualTo(preview.getMsg());
        assertThat(service.update(job, login).getMsg()).isEqualTo(preview.getMsg());
        verify(jobInfoMapper, never()).save(any());
        verify(jobInfoMapper, never()).updateInGroup(any(), anyInt(), anyBoolean());
        job.setScheduleConf(null);
        job.setId(0);
        when(jobInfoMapper.save(job)).thenAnswer(invocation -> {
            job.setId(42);
            return 1;
        });
        assertThat(service.add(job, login).isSuccess()).isTrue();
        verify(jobInfoMapper).save(job);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:30
     * 说明: 验证任务写入入口的请求方法、任务预览的执行器组权限和固定间隔游标。
     * @return
     * 返回值: void
     * 返回值说明: 任务写入只接受 POST，预览首个时间与已保存的游标一致
     */
    @Test
    void taskMutationsRequirePostAndKeepCurrentFormContract() throws Exception {
        var user = new com.zero.jobadmin.framework.model.XxlJobUser();
        user.setId(62);
        user.setUsername("task-action-fixture");
        user.setPassword(com.xxl.tool.crypto.Sha256Tool.sha256("test-fixture-password"));
        user.setPermission("2");
        when(userMapper.loadByUserName(user.getUsername())).thenReturn(user);
        when(userMapper.loadById(user.getId())).thenReturn(user);
        when(userMapper.updateToken(eq(user.getId()), anyString())).thenAnswer(invocation -> {
            user.setToken(invocation.getArgument(1));
            return 1;
        });
        var login = mvc.perform(post("/auth/doLogin").param("userName", user.getUsername())
                        .param("password", "test-fixture-password"))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getCookie("job_test_cookie");
        assertThat(login).isNotNull();

        for (String action : List.of("insert", "update", "delete", "start", "stop", "trigger")) {
            mvc.perform(get("/jobinfo/" + action).cookie(login)).andExpect(status().isMethodNotAllowed());
            mvc.perform(head("/jobinfo/" + action).cookie(login)).andExpect(status().isMethodNotAllowed());
        }
        verifyNoInteractions(jobService);

        var success = com.xxl.tool.response.Response.<String>ofSuccess();
        when(jobService.add(any(), any())).thenReturn(success);
        when(jobService.update(any(), any())).thenReturn(success);
        when(jobService.remove(eq(42), any())).thenReturn(success);
        when(jobService.start(eq(42), any())).thenReturn(success);
        when(jobService.stop(eq(42), any())).thenReturn(success);
        when(jobService.trigger(any(), eq(42), eq("fixture"), eq(""))).thenReturn(success);
        for (String action : List.of("insert", "update")) {
            mvc.perform(post("/jobinfo/" + action).cookie(login).param("jobGroup", "2").param("id", "42"))
                    .andExpect(jsonPath("$.code").value(200));
        }
        for (String action : List.of("delete", "start", "stop")) {
            mvc.perform(post("/jobinfo/" + action).cookie(login).param("ids[]", "42"))
                    .andExpect(jsonPath("$.code").value(200));
        }
        mvc.perform(post("/jobinfo/trigger").cookie(login).param("id", "42")
                        .param("executorParam", "fixture").param("addressList", ""))
                .andExpect(jsonPath("$.code").value(200));

        var runningJob = new XxlJobInfo();
        runningJob.setId(42);
        runningJob.setJobGroup(2);
        runningJob.setTriggerStatus(com.zero.jobadmin.business.constant.TriggerStatus.RUNNING.getValue());
        runningJob.setScheduleType("FIX_RATE");
        runningJob.setScheduleConf("7");
        long cursor = java.time.Instant.now().plusSeconds(300).truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toEpochMilli();
        runningJob.setTriggerNextTime(cursor);
        when(jobInfoMapper.loadById(42)).thenReturn(runningJob);

        mvc.perform(post("/jobinfo/nextTriggerTime").cookie(login).param("id", "42").param("scheduleType", "NONE"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0]").value(com.xxl.tool.core.DateTool.formatDateTime(new java.util.Date(cursor))))
                .andExpect(jsonPath("$.data[1]").value(com.xxl.tool.core.DateTool.formatDateTime(new java.util.Date(cursor + 7000))));
        runningJob.setJobGroup(3);
        mvc.perform(post("/jobinfo/nextTriggerTime").cookie(login).param("id", "42"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("system_permission_limit")));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:50
     * 说明: 验证重复启停不重置调度时间，条件更新落空时返回失败。
     * @return
     * 返回值: void
     * 返回值说明: 真实状态切换才写入调度字段
     */
    @Test
    void taskTriggerStateOnlyUpdatesOnTransition() {
        var service = new XxlJobServiceImpl();
        ReflectionTestUtils.setField(service, "xxlJobInfoMapper", jobInfoMapper);
        var login = new LoginInfo("1", "test-signature");
        login.setUserName("fixture");
        login.setExtraInfo(java.util.Map.of("jobGroups", "2"));
        var job = new XxlJobInfo();
        job.setId(42);
        job.setJobGroup(2);
        job.setScheduleType("FIX_RATE");
        job.setScheduleConf("30");
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        when(jobInfoMapper.updateTriggerState(eq(job), eq(1), longThat(time -> time > 0))).thenReturn(1);
        when(jobInfoMapper.updateTriggerState(job, 0, 0)).thenReturn(0, 1);

        assertThat(service.stop(42, login).isSuccess()).isTrue();
        verify(jobInfoMapper, never()).updateTriggerState(any(), anyInt(), anyLong());
        assertThat(service.start(42, login).isSuccess()).isTrue();
        job.setTriggerStatus(1);
        assertThat(service.start(42, login).isSuccess()).isTrue();
        verify(jobInfoMapper, times(1)).updateTriggerState(eq(job), eq(1), anyLong());
        assertThat(service.stop(42, login).isSuccess()).isFalse();
        assertThat(service.stop(42, login).isSuccess()).isTrue();
        verify(jobInfoMapper, times(2)).updateTriggerState(job, 0, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:34
     * 说明: 验证手动派发在入队前校验临时执行器地址。
     * @return
     * 返回值: void
     * 返回值说明: 无效地址不会入队，合法地址保留顺序，空输入使用组地址
     */
    @Test
    void manualTriggerValidatesExecutorOverridesBeforeDispatch() {
        var service = new XxlJobServiceImpl();
        ReflectionTestUtils.setField(service, "xxlJobInfoMapper", jobInfoMapper);
        var login = new LoginInfo("1", "test-signature");
        login.setUserName("fixture");
        login.setExtraInfo(java.util.Map.of("jobGroups", "2"));
        var job = new XxlJobInfo();
        job.setJobGroup(2);
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        var pool = mock(JobTriggerPoolHelper.class);
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        when(bootstrap.getJobTriggerPoolHelper()).thenReturn(pool);
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            for (String address : List.of("https://", "ftp://fixture.invalid", "http://fixture.invalid:0",
                    "http://fixture.invalid:70000", "http://fixture.invalid,")) {
                var response = service.trigger(login, 42, "fixture", address);
                assertThat(response.isSuccess()).isFalse();
                assertThat(response.getMsg()).isEqualTo(I18nUtil.getString("jobgroup_field_registryList_invalid"));
            }
            verifyNoInteractions(pool);
            assertThat(service.trigger(login, 42, "fixture",
                    " http://fixture.invalid:9999 , https://[::1]:9443 ").isSuccess()).isTrue();
            assertThat(service.trigger(login, 42, "fixture", "   ").isSuccess()).isTrue();
        }
        verify(pool).trigger(42, TriggerTypeEnum.MANUAL, -1, null, "fixture",
                "http://fixture.invalid:9999,https://[::1]:9443");
        verify(pool).trigger(42, TriggerTypeEnum.MANUAL, -1, null, "fixture", null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:53
     * 说明: 验证派发线程池满时手动触发返回失败，自动重试保留分片和剩余次数。
     * @return
     * 返回值: void
     * 返回值说明: 两次拒绝均写入调度失败日志，无执行器调用
     */
    @Test
    void saturatedDispatchPoolRecordsRejectedAttemptsAndReportsManualFailure() {
        var service = new XxlJobServiceImpl();
        ReflectionTestUtils.setField(service, "xxlJobInfoMapper", jobInfoMapper);
        var login = new LoginInfo("1", "test-signature");
        login.setExtraInfo(java.util.Map.of("jobGroups", "2"));
        var job = new XxlJobInfo();
        job.setId(42);
        job.setJobGroup(2);
        job.setExecutorFailRetryCount(4);
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        when(jobLogMapper.save(any())).thenReturn(1L);

        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var trigger = new JobTrigger();
        ReflectionTestUtils.setField(trigger, "xxlJobInfoMapper", jobInfoMapper);
        ReflectionTestUtils.setField(trigger, "xxlJobLogMapper", jobLogMapper);
        var helper = new JobTriggerPoolHelper();
        when(bootstrap.getJobTrigger()).thenReturn(trigger);
        when(bootstrap.getJobTriggerPoolHelper()).thenReturn(helper);
        var pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
        var blocker = new CountDownLatch(1);
        ReflectionTestUtils.setField(helper, "fastTriggerPool", pool);
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            pool.execute(() -> {
                try {
                    blocker.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            pool.execute(() -> {});

            assertThat(service.trigger(login, 42, "manual-fixture", null).isSuccess()).isFalse();
            assertThatThrownBy(() -> helper.trigger(42, TriggerTypeEnum.RETRY, 2, "1/2", "retry-fixture", null))
                    .isInstanceOf(RejectedExecutionException.class);
        } finally {
            pool.shutdownNow();
        }

        var logs = org.mockito.ArgumentCaptor.forClass(XxlJobLog.class);
        verify(jobLogMapper, times(2)).save(logs.capture());
        var rejected = logs.getAllValues();
        assertThat(rejected).allSatisfy(log -> {
            assertThat(log.getTriggerCode()).isEqualTo(com.xxl.tool.response.Response.ofFail("fixture").getCode());
            assertThat(log.getTriggerMsg()).contains(I18nUtil.getString("jobconf_trigger_pool_rejected"));
        });
        assertThat(rejected.get(0).getExecutorFailRetryCount()).isZero();
        assertThat(rejected.get(0).getExecutorParam()).isEqualTo("manual-fixture");
        assertThat(rejected.get(1).getExecutorFailRetryCount()).isEqualTo(2);
        assertThat(rejected.get(1).getExecutorShardingParam()).isEqualTo("1/2");
        assertThat(rejected.get(1).getExecutorParam()).isEqualTo("retry-fixture");
        verifyNoInteractions(groupMapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:55
     * 说明: 验证正常停止时线程池排空已接收的待派发任务。
     * @return
     * 返回值: void
     * 返回值说明: 停止入口等到队列任务执行完毕
     */
    @Test
    void stoppingDispatchPoolLetsQueuedWorkFinish() {
        var helper = new JobTriggerPoolHelper();
        var releaseWorker = new CountDownLatch(1);
        var finished = new CountDownLatch(1);
        var pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1)) {
            @Override
            public void shutdown() {
                super.shutdown();
                releaseWorker.countDown();
            }
        };
        ReflectionTestUtils.setField(helper, "fastTriggerPool", pool);
        try {
            pool.execute(() -> {
                try {
                    releaseWorker.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            pool.execute(finished::countDown);
            helper.stop();
            assertThat(finished.getCount()).isZero();
            assertThat(pool.isTerminated()).isTrue();
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:55
     * 说明: 验证强制停止仅标记还在队列的任务并保留线程中断状态。
     * @return
     * 返回值: void
     * 返回值说明: 被取消的派发带原任务参数进入失败日志处理
     */
    @Test
    void interruptedDispatchShutdownRecordsOnlyUnstartedJobs() {
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var trigger = mock(JobTrigger.class);
        when(bootstrap.getJobTrigger()).thenReturn(trigger);
        var helper = new JobTriggerPoolHelper();
        var blocker = new CountDownLatch(1);
        var pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
        ReflectionTestUtils.setField(helper, "fastTriggerPool", pool);
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            pool.execute(() -> {
                try {
                    blocker.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            helper.trigger(42, TriggerTypeEnum.RETRY, 2, "1/2", "fixture", null);
            try {
                Thread.currentThread().interrupt();
                helper.stop();
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
            } finally {
                Thread.interrupted();
            }
            verify(trigger).recordUndispatchedTrigger(42, TriggerTypeEnum.RETRY, 2, "1/2", "fixture", true);
            verifyNoMoreInteractions(trigger);
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:08
     * 说明: 验证首次完成才提交子任务，并兼容历史异常子任务编号。
     * @return
     * 返回值: void
     * 返回值说明: 重复完成不会再次提交子任务或补写备注
     */
    @Test
    void completingJobSubmitsChildrenOnlyAfterFirstResultWrite() {
        var completer = new JobCompleter();
        ReflectionTestUtils.setField(completer, "xxlJobInfoMapper", jobInfoMapper);
        ReflectionTestUtils.setField(completer, "xxlJobLogMapper", jobLogMapper);
        var log = new XxlJobLog();
        log.setId(501L);
        log.setJobId(42);
        log.setHandleCode(200);
        var job = new XxlJobInfo();
        job.setChildJobId("99999999999999,43");
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        when(jobLogMapper.updateHandleInfo(log)).thenReturn(1, 0);
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var pool = mock(JobTriggerPoolHelper.class);
        when(bootstrap.getJobTriggerPoolHelper()).thenReturn(pool);
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            assertThat(completer.complete(log)).isEqualTo(1);
            assertThat(completer.complete(log)).isZero();
        }
        var operation = inOrder(jobLogMapper, pool);
        operation.verify(jobLogMapper).updateHandleInfo(log);
        operation.verify(pool).trigger(43, TriggerTypeEnum.PARENT, -1, null, null, null);
        operation.verify(jobLogMapper).updateHandleMessage(eq(501L),
                argThat(message -> message.contains("99999999999999") && message.contains("43")));
        operation.verify(jobLogMapper).updateHandleInfo(log);
        operation.verifyNoMoreInteractions();
        verify(jobInfoMapper, times(1)).loadById(42);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:32
     * 说明: 用执行器协议验证写入范围、局部写入失败、重试和停机响应。
     * @return
     * 返回值: void
     * 返回值说明: 失败批次可重试，已完成日志只写回一次，其他执行器组的日志不受影响
     */
    @Test
    void executorCallbackUsesAuthenticatedGroupAndCompletesOnlyOnce() throws Exception {
        var group = new XxlJobGroup();
        group.setId(2);
        group.setAppname("callback-fixture");
        group.setAccessToken("fixture-callback-token");
        when(groupMapper.loadByAppname(group.getAppname())).thenReturn(group);
        var helper = new JobCompleteHelper();
        var completer = new JobCompleter();
        ReflectionTestUtils.setField(completer, "xxlJobInfoMapper", jobInfoMapper);
        ReflectionTestUtils.setField(completer, "xxlJobLogMapper", jobLogMapper);
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        when(bootstrap.getJobCompleteHelper()).thenReturn(helper);
        when(bootstrap.getXxlJobLogMapper()).thenReturn(jobLogMapper);
        when(bootstrap.getJobCompleter()).thenReturn(completer);
        var matching = new XxlJobLog();
        matching.setId(701);
        matching.setJobGroup(2);
        var otherGroup = new XxlJobLog();
        otherGroup.setId(702);
        otherGroup.setJobGroup(3);
        var toRetry = new XxlJobLog();
        toRetry.setId(703);
        toRetry.setJobGroup(2);
        when(jobLogMapper.load(701)).thenReturn(matching);
        when(jobLogMapper.load(702)).thenReturn(otherGroup);
        when(jobLogMapper.load(703)).thenReturn(toRetry);
        var failFirstWrite = new AtomicBoolean(true);
        when(jobLogMapper.updateHandleInfo(any())).thenAnswer(invocation -> {
            XxlJobLog result = invocation.getArgument(0);
            if (result.getId() == 703 && failFirstWrite.getAndSet(false)) {
                throw new IllegalStateException("fixture persistence error");
            }
            XxlJobLog stored = result.getId() == 701 ? matching : toRetry;
            stored.setHandleCode(result.getHandleCode());
            stored.setHandleMsg(result.getHandleMsg());
            return 1;
        });
        var payload = new CallbackRequest(List.of(new CallbackData(703, 1, 500, "retried result"),
                new CallbackData(701, 1, 500, "fixture result"),
                new CallbackData(702, 1, 500, "fixture result")));
        String body = new ObjectMapper().writeValueAsString(payload);

        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            var request = post("/api/callback").header(Const.XXL_JOB_APPNAME, group.getAppname())
                    .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                    .contentType("application/json").content(body);
            mvc.perform(request).andExpect(jsonPath("$.code").value(500));
            assertThat(matching.getHandleCode()).isEqualTo(500);
            assertThat(toRetry.getHandleCode()).isZero();
            mvc.perform(request).andExpect(jsonPath("$.code").value(200));
            helper.stop();
            mvc.perform(request).andExpect(jsonPath("$.code").value(500));
        }
        verify(jobLogMapper).updateHandleInfo(argThat(log -> log.getId() == 701));
        verify(jobLogMapper, times(2)).updateHandleInfo(argThat(log -> log.getId() == 703));
        verify(jobLogMapper, never()).updateHandleInfo(argThat(log -> log.getId() == 702));
        verify(jobLogMapper, times(2)).load(703);
        assertThat(matching.getHandleCode()).isEqualTo(500);
        assertThat(matching.getHandleMsg()).isEqualTo("fixture result");
        assertThat(otherGroup.getHandleCode()).isZero();
        assertThat(toRetry.getHandleMsg()).isEqualTo("retried result");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:33
     * 说明: 验证执行器注册操作使用认证范围，并在持久化失败时返回失败。
     * @return
     * 返回值: void
     * 返回值说明: 本组注册和任务操作可到达服务，跨应用请求不会写入
     */
    @Test
    void executorManagementPassesAuthenticatedScopeToServices() throws Exception {
        var group = new XxlJobGroup();
        group.setId(2);
        group.setAppname("manage-fixture");
        group.setAccessToken("fixture-manage-token");
        var registry = new JobRegistryHelper();
        when(groupMapper.loadByAppname(group.getAppname())).thenReturn(group);
        String address = "http://fixture.invalid";
        when(registryMapper.registrySaveOrUpdate(eq("EXECUTOR"), eq(group.getAppname()), eq(address), any()))
                .thenReturn(0).thenThrow(new IllegalStateException("fixture persistence error"));
        when(registryMapper.registryDelete("EXECUTOR", group.getAppname(), address))
                .thenReturn(0).thenThrow(new IllegalStateException("fixture persistence error"));
        when(jobService.start(eq(42), any())).thenReturn(com.xxl.tool.response.Response.ofSuccess());
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        when(bootstrap.getJobRegistryHelper()).thenReturn(registry);
        when(bootstrap.getXxlJobRegistryMapper()).thenReturn(registryMapper);
        var json = new ObjectMapper();
        String online = json.writeValueAsString(new RegistryRequest("EXECUTOR", group.getAppname(), address));
        String unrelated = json.writeValueAsString(new RegistryRequest("EXECUTOR", "different-fixture", address));

        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            for (String action : List.of("registry", "registryRemove")) {
                mvc.perform(post("/api/" + action).header(Const.XXL_JOB_APPNAME, group.getAppname())
                                .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                                .contentType("application/json").content(online))
                        .andExpect(jsonPath("$.code").value(200));
                mvc.perform(post("/api/" + action).header(Const.XXL_JOB_APPNAME, group.getAppname())
                                .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                                .contentType("application/json").content(unrelated))
                        .andExpect(jsonPath("$.code").value(500));
                mvc.perform(post("/api/" + action).header(Const.XXL_JOB_APPNAME, group.getAppname())
                                .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                                .contentType("application/json").content(online))
                        .andExpect(jsonPath("$.code").value(500))
                        .andExpect(jsonPath("$.msg").value(action.equals("registry")
                                ? "Failed to save executor heartbeat." : "Failed to remove executor registry."));
            }
            mvc.perform(post("/api/startJob").header(Const.XXL_JOB_APPNAME, group.getAppname())
                            .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                            .contentType("application/json").content("{\"id\":42}"))
                    .andExpect(jsonPath("$.code").value(200));
        }
        verify(registryMapper, times(2)).registrySaveOrUpdate(eq("EXECUTOR"), eq(group.getAppname()), eq(address), any());
        verify(registryMapper, times(2)).registryDelete("EXECUTOR", group.getAppname(), address);
        verifyNoMoreInteractions(registryMapper);
        verify(jobService).start(eq(42), argThat(login -> JobGroupPermissionUtil.hasJobGroupPermission(login, 2)
                && !JobGroupPermissionUtil.hasJobGroupPermission(login, 3)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:02
     * 说明: 验证 OpenAPI 每次请求使用当前执行器组，令牌修改和分组移除立即生效。
     * @return
     * 返回值: void
     * 返回值说明: 只有当前有效令牌可调用任务操作
     */
    @Test
    void executorAuthenticationUsesCurrentGroupCredentialsOnEveryRequest() throws Exception {
        var original = new XxlJobGroup();
        original.setId(2);
        original.setAppname("auth-fixture");
        original.setAccessToken("fixture-before-rotation");
        var updated = new XxlJobGroup();
        updated.setId(2);
        updated.setAppname(original.getAppname());
        updated.setAccessToken("fixture-after-rotation");
        when(groupMapper.loadByAppname(original.getAppname())).thenReturn(original, updated, updated, null);
        when(jobService.start(eq(42), any())).thenReturn(com.xxl.tool.response.Response.ofSuccess());

        for (int i = 0; i < 4; i++) {
            String supplied = i < 2 ? original.getAccessToken() : updated.getAccessToken();
            mvc.perform(post("/api/startJob").header(Const.XXL_JOB_APPNAME, original.getAppname())
                            .header(Const.XXL_JOB_ACCESS_TOKEN, supplied)
                            .contentType("application/json").content("{\"id\":42}"))
                    .andExpect(jsonPath("$.code").value(i % 2 == 0 ? 200 : 500));
        }

        verify(groupMapper, times(4)).loadByAppname(original.getAppname());
        verify(jobService, times(2)).start(eq(42), any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:39
     * 说明: 验证 OpenAPI 区分无效 JSON 和内部故障，并保留标准失败响应。
     * @return
     * 返回值: void
     * 返回值说明: 无效 JSON 不调用服务，内部异常的原始消息不会返回执行器
     */
    @Test
    void executorOpenApiReturnsSafeFailureDetails() throws Exception {
        var group = new XxlJobGroup();
        group.setId(2);
        group.setAppname("error-fixture");
        group.setAccessToken("fixture-error-token");
        when(groupMapper.loadByAppname(group.getAppname())).thenReturn(group);

        mvc.perform(post("/api/startJob").header(Const.XXL_JOB_APPNAME, group.getAppname())
                        .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                        .contentType("application/json").content("{\"id\":"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("Invalid request body."));
        verifyNoInteractions(jobService);

        when(jobService.start(eq(42), any())).thenThrow(new IllegalStateException("fixture internal error details"));
        mvc.perform(post("/api/startJob").header(Const.XXL_JOB_APPNAME, group.getAppname())
                        .header(Const.XXL_JOB_ACCESS_TOKEN, group.getAccessToken())
                        .contentType("application/json").content("{\"id\":42}"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("OpenAPI request could not be processed."));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:13
     * 说明: 验证已删除任务跳过重试，单条异常释放已认领状态。
     * @return
     * 返回值: void
     * 返回值说明: 随后的日志仍可完成处理，告警使用模拟实现
     */
    @Test
    void failedJobMonitorFinalizesEachClaimedLogIndependently() {
        var monitor = new JobFailAlarmMonitorHelper();
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var pool = mock(JobTriggerPoolHelper.class);
        var alarmer = mock(JobAlarmer.class);
        when(bootstrap.getXxlJobInfoMapper()).thenReturn(jobInfoMapper);
        when(bootstrap.getJobTriggerPoolHelper()).thenReturn(pool);
        when(bootstrap.getJobAlarmer()).thenReturn(alarmer);
        var deleted = new XxlJobLog();
        deleted.setJobId(41);
        deleted.setExecutorFailRetryCount(2);
        var available = new XxlJobLog();
        available.setJobId(42);
        available.setExecutorFailRetryCount(2);
        var job = new XxlJobInfo();
        when(jobLogMapper.updateAlarmStatus(anyLong(), eq(0), eq(-1))).thenReturn(1);
        when(jobLogMapper.load(10L)).thenReturn(deleted);
        when(jobLogMapper.load(11L)).thenThrow(new IllegalStateException("fixture load failure"));
        when(jobLogMapper.load(12L)).thenReturn(available);
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        when(alarmer.alarm(job, available)).thenReturn(true);

        for (long id : List.of(10L, 11L, 12L)) {
            ReflectionTestUtils.invokeMethod(monitor, "processFailure", bootstrap, jobLogMapper, id);
        }
        verify(jobLogMapper).updateAlarmStatus(10L, -1, 1);
        verify(jobLogMapper).updateAlarmStatus(11L, -1, 3);
        verify(jobLogMapper).updateAlarmStatus(12L, -1, 2);
        verify(pool).trigger(42, TriggerTypeEnum.RETRY, 1, null, null, null);
        verifyNoMoreInteractions(pool);
        verify(jobLogMapper).appendRetryMessage(eq(12L), anyString());
        verify(jobLogMapper, never()).updateTriggerInfo(any());
        verify(alarmer).alarm(job, available);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:16
     * 说明: 验证离线检查的查询与状态更新共用时间边界。
     * @return
     * 返回值: void
     * 返回值说明: 更新时再次传入执行器心跳有效期和调度时间上限
     */
    @Test
    void lostJobMonitorRechecksWithSameHeartbeatWindow() {
        var now = new java.util.Date(1_800_000_000_000L);
        var triggerBefore = new java.util.Date(now.getTime() - java.util.concurrent.TimeUnit.MINUTES.toMillis(10));
        var heartbeatSince = new java.util.Date(now.getTime()
                - java.util.concurrent.TimeUnit.SECONDS.toMillis(com.xxl.job.core.constant.Const.DEAD_TIMEOUT));
        when(jobLogMapper.findLostJobIds(triggerBefore, "EXECUTOR", heartbeatSince, 1000)).thenReturn(List.of(501L));

        ReflectionTestUtils.invokeMethod(new JobCompleteHelper(), "markLostJobs", jobLogMapper, now);

        verify(jobLogMapper).markLostJob(argThat(log -> log.getId() == 501L
                        && log.getHandleCode() == 500 && now.equals(log.getHandleTime())),
                eq(triggerBefore), eq("EXECUTOR"), eq(heartbeatSince));
        verify(jobLogMapper, never()).updateHandleInfo(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:25
     * 说明: 验证计划预览区分没有计划、无效配置和可执行的未来计划。
     * @return
     * 返回值: void
     * 返回值说明: 无计划返回空列表，有效预览最多返回五个递增时间
     */
    @Test
    void schedulePreviewDistinguishesEmptyAndInvalidSchedules() {
        var controller = new JobInfoController();
        assertThat(controller.nextTriggerTime(null, null, "NONE", null).getData()).isEmpty();
        var expired = controller.nextTriggerTime(null, null, "CRON", "0 0 0 1 1 ? 2000");
        assertThat(expired.isSuccess()).isTrue();
        assertThat(expired.getData()).isEmpty();
        assertThat(controller.nextTriggerTime(null, null, "UNKNOWN", "5").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "CRON", null).isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "CRON", "   ").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "CRON", "invalid").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "FIX_RATE", "-1").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "FIX_RATE", "0").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "FIX_RATE", "2147483648").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "FIX_RATE", "9223372036854775807").isSuccess()).isFalse();
        assertThat(controller.nextTriggerTime(null, null, "FIX_RATE", "7").getData()).hasSize(5).isSorted().doesNotHaveDuplicates();
        assertThat(controller.nextTriggerTime(null, null, "CRON", "0/10 * * * * ?").getData()).hasSize(5).isSorted();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:36
     * 说明: 验证立即触发后能预读短间隔计划，历史非法配置停止继续计算。
     * @return
     * 返回值: void
     * 返回值说明: 时间轮保存下一次触发，非法配置不会占用后续扫描
     */
    @Test
    void schedulerPrefetchesShortIntervalsAndStopsInvalidLegacySchedules() {
        var scheduler = new JobScheduleHelper();
        var pool = mock(JobTriggerPoolHelper.class);
        long now = 1_800_000_000_000L;
        var shortInterval = new XxlJobInfo();
        shortInterval.setId(51);
        shortInterval.setTriggerStatus(1);
        shortInterval.setTriggerNextTime(now);
        shortInterval.setScheduleType("FIX_RATE");
        shortInterval.setScheduleConf("2");
        var invalid = new XxlJobInfo();
        invalid.setId(52);
        invalid.setTriggerStatus(1);
        invalid.setTriggerNextTime(now - 6_000L);
        invalid.setScheduleType("FIX_RATE");
        invalid.setScheduleConf("0");

        List<Runnable> dispatches = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(scheduler, "scheduleJob", invalid, now, pool, dispatches);
        ReflectionTestUtils.invokeMethod(scheduler, "scheduleJob", shortInterval, now, pool, dispatches);
        verifyNoInteractions(pool);
        dispatches.forEach(Runnable::run);
        verify(pool).trigger(51, TriggerTypeEnum.CRON, -1, null, null, null);
        verifyNoMoreInteractions(pool);
        assertThat(ReflectionTestUtils.getField(scheduler, "ringData"))
                .isEqualTo(java.util.Map.of((now + 2000L) / 1000, List.of(51)));
        List<Integer> lateJobs = ReflectionTestUtils.invokeMethod(scheduler, "drainTimeRing", now / 1000 + 9);
        assertThat(lateJobs).containsExactly(51);
        assertThat(shortInterval.getTriggerLastTime()).isEqualTo(now + 2000L);
        assertThat(shortInterval.getTriggerNextTime()).isEqualTo(now + 4000L);
        assertThat(invalid.getTriggerStatus()).isZero();
        assertThat(invalid.getTriggerNextTime()).isZero();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:44
     * 说明: 验证调度中心取得数据库锁记录后才查询任务。
     * @return
     * 返回值: void
     * 返回值说明: 缺少锁记录时跳过本轮，有锁时按原上限查询
     */
    @Test
    void schedulerScansOnlyAfterAcquiringTheLockRow() {
        var scheduler = new JobScheduleHelper();
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var lockMapper = mock(XxlJobLockMapper.class);
        when(bootstrap.getXxlJobLockMapper()).thenReturn(lockMapper);
        when(bootstrap.getXxlJobInfoMapper()).thenReturn(jobInfoMapper);
        when(lockMapper.scheduleLock()).thenReturn(null, "schedule_lock");

        List<XxlJobInfo> missingLock = ReflectionTestUtils.invokeMethod(scheduler, "readScheduleBatch", bootstrap, 20);
        assertThat(missingLock).isEmpty();
        verifyNoInteractions(jobInfoMapper);

        var expected = List.of(new XxlJobInfo());
        when(jobInfoMapper.scheduleJobQuery(anyLong(), eq(20))).thenReturn(expected);
        List<XxlJobInfo> locked = ReflectionTestUtils.invokeMethod(scheduler, "readScheduleBatch", bootstrap, 20);
        assertThat(locked).isEqualTo(expected);
        var operations = inOrder(lockMapper, jobInfoMapper);
        operations.verify(lockMapper, times(2)).scheduleLock();
        operations.verify(jobInfoMapper).scheduleJobQuery(anyLong(), eq(20));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:46
     * 说明: 验证较后的游标批次发现计划并发修改时会回滚，只有提交成功的扫描才派发。
     * @return
     * 返回值: void
     * 返回值说明: 失败没有派发或入轮，成功先提交事务再提交任务
     */
    @Test
    void schedulerDispatchesOnlyAfterTheEntireCursorTransactionCommits() {
        var scheduler = new JobScheduleHelper();
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var lockMapper = mock(XxlJobLockMapper.class);
        var transactionManager = mock(PlatformTransactionManager.class);
        var transaction = mock(TransactionStatus.class);
        var pool = mock(JobTriggerPoolHelper.class);
        var first = new XxlJobInfo();
        first.setId(51);
        first.setTriggerStatus(1);
        long originalNextTime = System.currentTimeMillis();
        first.setTriggerNextTime(originalNextTime);
        first.setScheduleType("FIX_RATE");
        first.setScheduleConf("2");
        var second = new XxlJobInfo();
        second.setId(52);
        second.setTriggerStatus(1);
        second.setTriggerNextTime(System.currentTimeMillis() + 4000L);
        second.setScheduleType("FIX_RATE");
        second.setScheduleConf("10");
        when(bootstrap.getTransactionManager()).thenReturn(transactionManager);
        when(transactionManager.getTransaction(any())).thenReturn(transaction);
        when(bootstrap.getXxlJobLockMapper()).thenReturn(lockMapper);
        when(lockMapper.scheduleLock()).thenReturn("schedule_lock");
        when(bootstrap.getXxlJobInfoMapper()).thenReturn(jobInfoMapper);
        when(bootstrap.getJobTriggerPoolHelper()).thenReturn(pool);
        when(bootstrap.getScheduleBatchSize()).thenReturn(1);
        when(jobInfoMapper.scheduleJobQuery(anyLong(), eq(20))).thenReturn(List.of(first, second));
        when(jobInfoMapper.scheduleBatchUpdate(anyList())).thenAnswer(invocation -> {
            List<JobScheduleUpdate> batch = invocation.getArgument(0);
            assertThat(batch).hasSize(1);
            JobScheduleUpdate update = batch.get(0);
            return update.job().getId() == first.getId() ? 1 : 0;
        });

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(scheduler, "runScheduleScan", bootstrap, 20))
                .isInstanceOf(OptimisticLockingFailureException.class).hasMessageContaining("expected=1, updated=0");
        verify(jobInfoMapper).scheduleBatchUpdate(argThat(batch -> batch.get(0).job().getId() == 51
                && batch.get(0).expectedNextTime() == originalNextTime));
        verify(transactionManager).rollback(transaction);
        verify(transactionManager, never()).commit(any());
        verifyNoInteractions(pool);
        assertThat(ReflectionTestUtils.getField(scheduler, "ringData")).isEqualTo(java.util.Map.of());

        first.setTriggerStatus(1);
        first.setTriggerNextTime(System.currentTimeMillis());
        when(jobInfoMapper.scheduleJobQuery(anyLong(), eq(20))).thenReturn(List.of(first));
        Boolean updated = ReflectionTestUtils.invokeMethod(scheduler, "runScheduleScan", bootstrap, 20);
        assertThat(updated).isTrue();
        var operations = inOrder(transactionManager, pool);
        operations.verify(transactionManager).commit(transaction);
        operations.verify(pool).trigger(51, TriggerTypeEnum.CRON, -1, null, null, null);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:11
     * 说明: 验证无效分片、缺失分组和无效策略落入调度失败日志，历史空 GLUE 时间支持正常提交。
     * @return
     * 返回值: void
     * 返回值说明: 无效配置不联系执行器，合法任务使用兼容时间发起调度
     */
    @Test
    void triggeringJobRecordsConfigurationFailuresAndSupportsLegacyGlueTime() throws Exception {
        var trigger = new JobTrigger();
        ReflectionTestUtils.setField(trigger, "xxlJobInfoMapper", jobInfoMapper);
        ReflectionTestUtils.setField(trigger, "xxlJobGroupMapper", groupMapper);
        ReflectionTestUtils.setField(trigger, "xxlJobLogMapper", jobLogMapper);
        var job = new XxlJobInfo();
        job.setId(42);
        job.setJobGroup(2);
        job.setGlueType("BEAN");
        job.setExecutorFailRetryCount(2);
        job.setExecutorRouteStrategy("SHARDING_BROADCAST");
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        when(jobLogMapper.save(any())).thenAnswer(invocation -> {
            XxlJobLog entry = invocation.getArgument(0);
            entry.setId(501L);
            return 1L;
        });
        var group = new XxlJobGroup();
        group.setAddressList("http://executor.example:9999,http://executor-two.example:9999");
        when(groupMapper.load(2)).thenReturn(null, group, group, group);
        var executor = mock(com.xxl.job.core.openapi.executor.ExecutorBiz.class);
        when(executor.trigger(any())).thenReturn(com.xxl.tool.response.Response.ofSuccess());
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, null, null);
            trigger.trigger(42, TriggerTypeEnum.RETRY, -1, "999999999999/5", null, null);
            job.setExecutorRouteStrategy("UNKNOWN");
            trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, null, null);
            bootstrapStatic.verifyNoInteractions();

            job.setExecutorRouteStrategy("FIRST");
            bootstrapStatic.when(() -> XxlJobAdminBootstrap.getExecutorBiz("http://executor.example:9999", group))
                    .thenReturn(executor);
            trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, null, null);
        }
        var logs = org.mockito.ArgumentCaptor.forClass(XxlJobLog.class);
        verify(jobLogMapper, times(4)).updateTriggerInfo(logs.capture());
        var outcomes = logs.getAllValues();
        assertThat(outcomes.get(0).getTriggerMsg()).contains(I18nUtil.getString("jobgroup_empty"));
        assertThat(outcomes.get(1).getTriggerMsg()).contains(I18nUtil.getString("jobconf_trigger_sharding_invalid"));
        assertThat(outcomes.get(1).getExecutorFailRetryCount()).isZero();
        assertThat(outcomes.get(2).getTriggerMsg()).contains(I18nUtil.getString("jobinfo_field_executorRouteStrategy")
                + I18nUtil.getString("system_invalid"));
        assertThat(outcomes).extracting(XxlJobLog::getTriggerCode).containsExactly(500, 500, 500, 200);
        verify(executor).trigger(argThat(request -> request.getGlueUpdatetime() == 0L));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:28
     * 说明: 使用真实执行器客户端验证调度协议、令牌更新、拒绝日志及 GLUE 版本传输。
     * @return
     * 返回值: void
     * 返回值说明: 分别记录派发成功和失败，同一秒内发布不同源码也会发送不同缓存版本
     */
    @Test
    void triggerUsesRealHttpProtocolAgainstLoopbackExecutor() throws Exception {
        record Dispatch(String path, String method, String token, String appName, String body) {}
        try (var server = new ServerSocket()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            server.setSoTimeout(5000);
            // Windows 的 JDK HttpServer 依赖当前环境不可用的本地 NIO 管道。
            var receiver = new FutureTask<>(() -> {
                var requests = new ArrayList<Dispatch>();
                for (int requestIndex = 0; requestIndex < 5; requestIndex++) {
                    try (var socket = server.accept()) {
                        socket.setSoTimeout(5000);
                        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                        String[] requestLine = reader.readLine().split(" ");
                        var headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
                        for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
                            int separator = line.indexOf(':');
                            headers.put(line.substring(0, separator), line.substring(separator + 1).trim());
                        }
                        char[] requestBody = new char[Integer.parseInt(headers.get("Content-Length"))];
                        for (int read = 0; read < requestBody.length;) {
                            int count = reader.read(requestBody, read, requestBody.length - read);
                            if (count < 0) {
                                throw new IOException("Incomplete fixture request");
                            }
                            read += count;
                        }
                        requests.add(new Dispatch(requestLine[1], requestLine[0], headers.get(Const.XXL_JOB_ACCESS_TOKEN),
                                headers.get(Const.XXL_JOB_APPNAME), new String(requestBody)));
                        String reply = requestIndex == 1
                                ? "{\"code\":500,\"msg\":\"fixture rejected\"}"
                                : "{\"code\":200,\"msg\":\"fixture accepted\"}";
                        byte[] body = reply.getBytes(StandardCharsets.UTF_8);
                        String responseHeaders = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "
                                + body.length + "\r\nConnection: close\r\n\r\n";
                        var output = socket.getOutputStream();
                        output.write(responseHeaders.getBytes(StandardCharsets.US_ASCII));
                        output.write(body);
                        output.flush();
                    }
                }
                return requests;
            });
            var receiverThread = new Thread(receiver, "zero-job-http-fixture");
            receiverThread.setDaemon(true);
            receiverThread.start();

            var trigger = new JobTrigger();
            ReflectionTestUtils.setField(trigger, "xxlJobInfoMapper", jobInfoMapper);
            ReflectionTestUtils.setField(trigger, "xxlJobGroupMapper", groupMapper);
            ReflectionTestUtils.setField(trigger, "xxlJobLogMapper", jobLogMapper);
            var group = new XxlJobGroup();
            group.setAppname("loopback-fixture");
            String originalToken = "fixture-dispatch-token";
            group.setAccessToken(originalToken);
            group.setAddressList("http://127.0.0.1:" + server.getLocalPort());
            var job = new XxlJobInfo();
            job.setId(42);
            job.setJobGroup(2);
            job.setGlueType("BEAN");
            job.setExecutorHandler("fixtureHandler");
            job.setExecutorBlockStrategy("SERIAL_EXECUTION");
            job.setExecutorRouteStrategy("FIRST");
            when(groupMapper.load(2)).thenReturn(group);
            when(jobInfoMapper.loadById(42)).thenReturn(job);
            var logIds = new AtomicLong(700);
            when(jobLogMapper.save(any())).thenAnswer(invocation -> {
                invocation.getArgument(0, XxlJobLog.class).setId(logIds.incrementAndGet());
                return 1L;
            });
            var bootstrap = mock(XxlJobAdminBootstrap.class);
            when(bootstrap.getTimeout()).thenReturn(3);
            try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class, CALLS_REAL_METHODS)) {
                bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
                trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, "first payload", null);
                group.setAccessToken("fixture-updated-dispatch-token");
                trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, "second payload", null);
                job.setGlueType("GLUE_SHELL");
                job.setGlueUpdatetime(java.util.Date.from(java.time.Instant.parse("2026-01-01T00:00:00Z")));
                job.setGlueSource("echo first version");
                trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, "", null);
                job.setGlueSource("echo second version");
                trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, "", null);
                trigger.trigger(42, TriggerTypeEnum.MANUAL, -1, null, "", null);
            }
            List<Dispatch> requests = receiver.get(5, TimeUnit.SECONDS);
            assertThat(requests).hasSize(5);
            Dispatch first = requests.get(0);
            assertThat(first.path()).isEqualTo("/trigger");
            assertThat(first.method()).isEqualTo("POST");
            assertThat(first.token()).isEqualTo(originalToken);
            assertThat(requests.get(1).token()).isEqualTo(group.getAccessToken());
            assertThat(first.appName()).isEqualTo("loopback-fixture");
            var payload = new ObjectMapper().readTree(first.body());
            assertThat(payload.path("jobId").asInt()).isEqualTo(42);
            assertThat(payload.path("logId").asLong()).isEqualTo(701L);
            assertThat(payload.path("executorHandler").asText()).isEqualTo("fixtureHandler");
            assertThat(payload.path("executorParams").asText()).isEqualTo("first payload");
            assertThat(payload.path("glueUpdatetime").asLong()).isZero();
            var mapper = new ObjectMapper();
            var firstGlue = mapper.readTree(requests.get(2).body());
            var secondGlue = mapper.readTree(requests.get(3).body());
            var sameGlue = mapper.readTree(requests.get(4).body());
            long firstVersion = firstGlue.path("glueUpdatetime").asLong();
            long secondVersion = secondGlue.path("glueUpdatetime").asLong();
            assertThat(firstGlue.path("glueSource").asText()).isEqualTo("echo first version");
            assertThat(secondGlue.path("glueSource").asText()).isEqualTo("echo second version");
            assertThat(firstVersion).isNegative().isNotEqualTo(secondVersion);
            assertThat(secondVersion).isNegative().isEqualTo(sameGlue.path("glueUpdatetime").asLong());
            verify(jobLogMapper).updateTriggerInfo(argThat(log -> log.getId() == 701 && log.getTriggerCode() == 200));
            verify(jobLogMapper).updateTriggerInfo(argThat(log -> log.getId() == 702 && log.getTriggerCode() == 500
                    && log.getTriggerMsg().contains("fixture rejected")));
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:04
     * 说明: 验证执行器客户端按应用隔离并在达到上限时保留最近使用的客户端。
     * @return
     * 返回值: void
     * 返回值说明: 构建代理且不发网络请求，只淘汰较早访问的客户端
     */
    @Test
    void executorClientCacheSeparatesAppsAndEvictsOldestEntry() throws Exception {
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        when(bootstrap.getTimeout()).thenReturn(3);
        var primary = new XxlJobGroup();
        primary.setAppname("cache-primary-fixture");
        primary.setAccessToken("fixture-cache-token");
        var secondary = new XxlJobGroup();
        secondary.setAppname("cache-secondary-fixture");
        secondary.setAccessToken(primary.getAccessToken());
        String sharedEndpoint = "http://127.0.0.1:9998";
        String recentEndpoint = "http://127.0.0.1:9999";
        int capacity = (int) ReflectionTestUtils.getField(XxlJobAdminBootstrap.class, "EXECUTOR_BIZ_CACHE_MAX_SIZE");

        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class, CALLS_REAL_METHODS)) {
            bootstrapStatic.when(XxlJobAdminBootstrap::getInstance).thenReturn(bootstrap);
            var cold = XxlJobAdminBootstrap.getExecutorBiz(sharedEndpoint, primary);
            var recent = XxlJobAdminBootstrap.getExecutorBiz(recentEndpoint, primary);
            var otherApp = XxlJobAdminBootstrap.getExecutorBiz(sharedEndpoint, secondary);
            assertThat(otherApp).isNotSameAs(cold);
            assertThat(XxlJobAdminBootstrap.getExecutorBiz(recentEndpoint, primary)).isSameAs(recent);
            for (int index = 0; index < capacity - 1; index++) {
                XxlJobAdminBootstrap.getExecutorBiz("http://127.0.0.1:" + (10000 + index), primary);
            }
            assertThat(XxlJobAdminBootstrap.getExecutorBiz(recentEndpoint, primary)).isSameAs(recent);
            assertThat(XxlJobAdminBootstrap.getExecutorBiz(sharedEndpoint, primary)).isNotSameAs(cold);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:06
     * 说明: 验证历史报表按当地日期汇总，日期之间写入失败相互隔离。
     * @return
     * 返回值: void
     * 返回值说明: 失败日保留旧数据，其他日期更新，其中无记录日期统计为零
     */
    @Test
    void historicalReportUsesDayBoundariesAndContinuesAfterOneDayFails() {
        var bootstrap = mock(XxlJobAdminBootstrap.class);
        var reports = mock(XxlJobLogReportMapper.class);
        when(bootstrap.getXxlJobLogMapper()).thenReturn(jobLogMapper);
        when(bootstrap.getXxlJobLogReportMapper()).thenReturn(reports);
        var zone = ZoneId.of("America/New_York");
        var today = LocalDate.of(2026, 11, 1);
        var nextDayStart = java.util.Date.from(today.plusDays(1).atStartOfDay(zone).toInstant());
        var todayStart = java.util.Date.from(today.atStartOfDay(zone).toInstant());
        var yesterdayStart = java.util.Date.from(today.minusDays(1).atStartOfDay(zone).toInstant());
        var olderStart = java.util.Date.from(today.minusDays(2).atStartOfDay(zone).toInstant());
        var todayReport = new XxlJobLogReport();
        todayReport.setSucCount(2);
        var emptyReport = new XxlJobLogReport();
        when(jobLogMapper.findLogReport(todayStart, nextDayStart)).thenReturn(todayReport);
        when(jobLogMapper.findLogReport(yesterdayStart, todayStart)).thenThrow(new IllegalStateException("fixture query error"));
        when(jobLogMapper.findLogReport(olderStart, yesterdayStart)).thenReturn(emptyReport);

        ReflectionTestUtils.invokeMethod(new JobLogReportHelper(), "refreshRecentReports", bootstrap, today, zone);

        assertThat(nextDayStart.getTime() - todayStart.getTime()).isEqualTo(TimeUnit.HOURS.toMillis(25));
        verify(reports).saveOrUpdate(todayReport);
        verify(reports).saveOrUpdate(emptyReport);
        verifyNoMoreInteractions(reports);
        assertThat(todayReport.getTriggerDay()).isEqualTo(todayStart);
        assertThat(todayReport.getSucCount()).isEqualTo(2);
        assertThat(todayReport.getUpdateTime()).isNotNull();
        assertThat(emptyReport.getTriggerDay()).isEqualTo(olderStart);
        assertThat(emptyReport.getRunningCount() + emptyReport.getSucCount() + emptyReport.getFailCount()).isZero();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:26
     * 说明: 验证清理固定上限，其他实例先删除一批时仍能继续推进。
     * @return
     * 返回值: void
     * 返回值说明: 按已读批次的末尾 ID 继续，不查询或删除上限之外的日志
     */
    @Test
    void logCleanupAdvancesAfterConcurrentDeletionWithinFixedUpperBound() {
        when(jobLogMapper.findMaxClearLogId(2, 123)).thenReturn(9L);
        when(jobLogMapper.findClearLogIds(2, 123, null, 0, 1000, 0, 9)).thenReturn(List.of(4L, 6L));
        when(jobLogMapper.findClearLogIds(2, 123, null, 0, 1000, 6, 9)).thenReturn(List.of(9L));
        when(jobLogMapper.clearLog(List.of(4L, 6L))).thenReturn(0);
        JobLogCleanup.clear(jobLogMapper, 2, 123, null, 0);

        var operations = inOrder(jobLogMapper);
        operations.verify(jobLogMapper).findMaxClearLogId(2, 123);
        operations.verify(jobLogMapper).findClearLogIds(2, 123, null, 0, 1000, 0, 9);
        operations.verify(jobLogMapper).clearLog(List.of(4L, 6L));
        operations.verify(jobLogMapper).findClearLogIds(2, 123, null, 0, 1000, 6, 9);
        operations.verify(jobLogMapper).clearLog(List.of(9L));
        operations.verifyNoMoreInteractions();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:00
     * 说明: 验证手动地址校验和自动注册切换。
     * @return
     * 返回值: void
     * 返回值说明: 无效地址拒绝保存，自动注册读取当前执行器组的心跳
     */
    @Test
    void executorGroupValidatesManualAddressesAndUsesStoredAppName() {
        var controller = new JobGroupController();
        ReflectionTestUtils.setField(controller, "xxlJobGroupMapper", groupMapper);
        ReflectionTestUtils.setField(controller, "xxlJobRegistryMapper", registryMapper);
        var submitted = new XxlJobGroup();
        submitted.setAppname("another-app");
        submitted.setName("Example executor");
        submitted.setAddressType(1);
        submitted.setAddressList("https://");
        assertThat(controller.insert(submitted).isSuccess()).isFalse();
        submitted.setAddressList("http://executor.example:9999,");
        assertThat(controller.insert(submitted).isSuccess()).isFalse();
        verify(groupMapper, never()).save(any());

        when(groupMapper.save(submitted)).thenReturn(1);
        submitted.setAddressList(" http://executor_one:9999 , https://executor.example/proxy ");
        assertThat(controller.insert(submitted).isSuccess()).isTrue();
        assertThat(submitted.getAddressList()).isEqualTo("http://executor_one:9999,https://executor.example/proxy");

        var stored = new XxlJobGroup();
        stored.setId(2);
        stored.setAppname("existing-app");
        stored.setAddressType(1);
        submitted.setId(2);
        submitted.setAddressType(0);
        when(groupMapper.load(2)).thenReturn(stored);
        var ownNode = new XxlJobRegistry();
        ownNode.setRegistryGroup("EXECUTOR");
        ownNode.setRegistryKey("existing-app");
        ownNode.setRegistryValue("http://own.example:9999");
        var otherNode = new XxlJobRegistry();
        otherNode.setRegistryGroup("EXECUTOR");
        otherNode.setRegistryKey("another-app");
        otherNode.setRegistryValue("http://other.example:9999");
        when(registryMapper.findAll(anyInt(), any())).thenReturn(List.of(otherNode, ownNode));
        when(groupMapper.update(submitted, 1)).thenReturn(1, 0);
        assertThat(controller.update(submitted).isSuccess()).isTrue();
        assertThat(submitted.getAddressList()).isEqualTo("http://own.example:9999");
        assertThat(controller.update(submitted).isSuccess()).isFalse();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:54
     * 说明: 验证普通用户查看注册节点时只得到已授权组的地址列表。
     * @return
     * 返回值: void
     * 返回值说明: 未授权组不查询，成功结果不包含执行器认证字段
     */
    @Test
    void registryNodesOnlyExposeAddressesOfAuthorizedGroups() throws Exception {
        var user = new com.zero.jobadmin.framework.model.XxlJobUser();
        user.setId(42);
        user.setUsername("nodes-fixture");
        user.setPassword(com.xxl.tool.crypto.Sha256Tool.sha256("test-fixture-password"));
        user.setPermission("2");
        when(userMapper.loadByUserName("nodes-fixture")).thenReturn(user);
        when(userMapper.loadById(42)).thenReturn(user);
        when(userMapper.updateToken(eq(42), anyString())).thenAnswer(invocation -> {
            user.setToken(invocation.getArgument(1));
            return 1;
        });
        var login = mvc.perform(post("/auth/doLogin").param("userName", "nodes-fixture")
                        .param("password", "test-fixture-password"))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getCookie("job_test_cookie");

        var group = new XxlJobGroup();
        group.setAddressList("http://executor.example:9999");
        group.setAccessToken("fixture-value");
        when(groupMapper.load(2)).thenReturn(group);
        mvc.perform(post("/jobgroup/loadById").param("id", "2").cookie(login))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.registryList[0]").value("http://executor.example:9999"))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist());
        when(groupMapper.load(2)).thenReturn(new XxlJobGroup());
        mvc.perform(post("/jobgroup/loadById").param("id", "2").cookie(login))
                .andExpect(jsonPath("$.data.registryList").isEmpty());
        assertThatThrownBy(() -> mvc.perform(post("/jobgroup/loadById").param("id", "7").cookie(login)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        verify(groupMapper, never()).load(7);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:06
     * 说明: 验证用户列表不会返回登录凭据，用户编辑以目标 ID 校验本人限制及更新结果。
     * @return
     * 返回值: void
     * 返回值说明: 本人和不存在的目标返回失败，空密码编辑沿用已有密码
     */
    @Test
    void userManagementFiltersCredentialsAndReportsUpdateOutcome() throws Exception {
        var admin = new com.zero.jobadmin.framework.model.XxlJobUser();
        admin.setId(42);
        admin.setRole(1);
        admin.setUsername("admin-fixture");
        admin.setPassword(com.xxl.tool.crypto.Sha256Tool.sha256("test-fixture-password"));
        when(userMapper.loadByUserName("admin-fixture")).thenReturn(admin);
        when(userMapper.loadById(42)).thenReturn(admin);
        when(userMapper.updateToken(eq(42), anyString())).thenAnswer(invocation -> {
            admin.setToken(invocation.getArgument(1));
            return 1;
        });
        var login = mvc.perform(post("/auth/doLogin").param("userName", "admin-fixture")
                        .param("password", "test-fixture-password"))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getCookie("job_test_cookie");

        var visibleUser = new com.zero.jobadmin.framework.model.XxlJobUser();
        visibleUser.setId(43);
        visibleUser.setUsername("normal-fixture");
        visibleUser.setPassword("test-only-password-digest");
        visibleUser.setToken("test-only-session-token");
        when(userMapper.pageList(0, 10, "", -1)).thenReturn(List.of(visibleUser));
        when(userMapper.pageListCount(0, 10, "", -1)).thenReturn(1);
        mvc.perform(get("/user/pageList").cookie(login).param("username", "").param("role", "-1"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("normal-fixture")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("test-only-"))));

        mvc.perform(post("/user/update").cookie(login).param("id", "42").param("role", "0"))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("user_update_loginuser_limit")));
        verify(userMapper, never()).update(any());

        when(userMapper.update(any())).thenReturn(1, 0);
        mvc.perform(post("/user/update").cookie(login).param("id", "43").param("role", "0")
                        .param("password", "").param("permission", "2"))
                .andExpect(jsonPath("$.code").value(200));
        verify(userMapper).update(argThat(update -> update.getId() == 43
                && update.getPassword() == null && "2".equals(update.getPermission())));
        mvc.perform(post("/user/update").cookie(login).param("id", "99").param("role", "0"))
                .andExpect(jsonPath("$.code").value(500));

        mvc.perform(get("/auth/updatePwd").cookie(login)).andExpect(status().isMethodNotAllowed());
        String oldHash = admin.getPassword();
        String newHash = com.xxl.tool.crypto.Sha256Tool.sha256("new-fixture-password");
        when(userMapper.changePassword(42, oldHash, newHash)).thenReturn(0, 1);
        for (int expected : List.of(500, 200)) {
            mvc.perform(post("/auth/updatePwd").cookie(login)
                            .param("oldPassword", "test-fixture-password").param("password", "new-fixture-password"))
                    .andExpect(jsonPath("$.code").value(expected));
        }
        verify(userMapper, times(2)).changePassword(42, oldHash, newHash);
        verify(userMapper, never()).update(argThat(update -> update.getId() == 42));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:50
     * 说明: 验证 GLUE 页面文本转义，并验证版本回退至相同内容后仍能拒绝旧窗口保存。
     * @return
     * 返回值: void
     * 返回值说明: 两次有效保存记录历史，过期窗口无法覆盖内容
     */
    @Test
    void glueEditorRejectsStaleSaveAndPreservesSourceText() throws Exception {
        var user = new com.zero.jobadmin.framework.model.XxlJobUser();
        user.setId(42);
        user.setUsername("glue-fixture");
        user.setPassword(com.xxl.tool.crypto.Sha256Tool.sha256("test-fixture-password"));
        user.setPermission("2");
        when(userMapper.loadByUserName("glue-fixture")).thenReturn(user);
        when(userMapper.loadById(42)).thenReturn(user);
        when(userMapper.updateToken(eq(42), anyString())).thenAnswer(invocation -> {
            user.setToken(invocation.getArgument(1));
            return 1;
        });
        var login = mvc.perform(post("/auth/doLogin").param("userName", "glue-fixture")
                        .param("password", "test-fixture-password"))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getCookie("job_test_cookie");

        var job = new XxlJobInfo();
        job.setId(42);
        job.setJobGroup(2);
        job.setName("Example <job>");
        job.setGlueType("GLUE_SHELL");
        job.setGlueSource("echo '</textarea>'");
        job.setGlueRemark("Original");
        when(jobInfoMapper.loadById(42)).thenReturn(job);
        when(jobInfoMapper.loadByIdForUpdate(42)).thenReturn(job);
        when(jobLogGlueMapper.findLatestVersionId(42)).thenReturn(null);
        var view = mvc.perform(get("/jobcode").cookie(login).param("jobId", "42"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;/textarea&gt;")))
                .andReturn().getModelAndView();
        assertThat(view).isNotNull();
        String version = (String) view.getModel().get("glueVersion");
        assertThat(version).isNotBlank();
        mvc.perform(post("/jobcode/save").cookie(login).param("id", "42")
                        .param("glueSource", "echo updated").param("glueRemark", "Updated")
                        .param("glueVersion", "stale"))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("jobinfo_glue_conflict")));
        verify(jobInfoMapper, never()).updateGlue(any());
        verify(jobLogGlueMapper, never()).save(any());
        when(jobInfoMapper.updateGlue(job)).thenReturn(1);
        when(jobLogGlueMapper.save(any())).thenReturn(1);
        mvc.perform(post("/jobcode/save").cookie(login).param("id", "42")
                        .param("glueSource", "echo updated").param("glueRemark", "Updated")
                        .param("glueVersion", version))
                .andExpect(jsonPath("$.code").value(200));
        verify(jobLogGlueMapper).save(argThat(history -> history.getJobId() == 42
                && history.getGlueSource().equals("echo updated")));

        var updated = new com.zero.jobadmin.business.model.XxlJobLogGlue();
        updated.setId(100);
        updated.setAddTime(new java.util.Date());
        updated.setGlueSource("echo updated");
        updated.setGlueRemark("Updated");
        when(jobLogGlueMapper.findByJobId(42)).thenReturn(List.of(updated));
        when(jobLogGlueMapper.findLatestVersionId(42)).thenReturn(100);
        var updatedView = mvc.perform(get("/jobcode").cookie(login).param("jobId", "42"))
                .andExpect(status().isOk()).andReturn().getModelAndView();
        assertThat(updatedView).isNotNull();
        String updatedVersion = (String) updatedView.getModel().get("glueVersion");
        mvc.perform(post("/jobcode/save").cookie(login).param("id", "42")
                        .param("glueSource", "echo '</textarea>'").param("glueRemark", "Original")
                        .param("glueVersion", updatedVersion))
                .andExpect(jsonPath("$.code").value(200));

        when(jobLogGlueMapper.findLatestVersionId(42)).thenReturn(101);
        mvc.perform(post("/jobcode/save").cookie(login).param("id", "42")
                        .param("glueSource", "echo stale edit").param("glueRemark", "Old window")
                        .param("glueVersion", version))
                .andExpect(jsonPath("$.msg").value(I18nUtil.getString("jobinfo_glue_conflict")));
        verify(jobInfoMapper, times(2)).updateGlue(job);
        verify(jobLogGlueMapper, times(2)).save(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 验证控制台仍需登录，Boot Admin可读取全部已启用监控端点及健康详情
     * @return
     * 返回值: void
     * 返回值说明: 首页跳转登录，health返回组件详情，指标、线程转储及日志级别接口可读
     */
    @Test
    void consoleRequiresLoginAndHealthIsAvailable() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").exists());
        mvc.perform(get("/actuator")).andExpect(status().isOk())
                .andExpect(jsonPath("$._links.beans").exists())
                .andExpect(jsonPath("$._links.env").exists());
        mvc.perform(get("/actuator/threaddump")).andExpect(status().isOk())
                .andExpect(jsonPath("$.threads").isArray());
        mvc.perform(get("/actuator/loggers")).andExpect(status().isOk())
                .andExpect(jsonPath("$.loggers").exists());
        mvc.perform(get("/actuator/metrics/process.uptime")).andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").isNumber());
        mvc.perform(get("/actuator/metrics/jvm.memory.used").param("tag", "area:heap"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.measurements[0].value").isNumber());
        mvc.perform(get("/actuator/metrics/jvm.threads.live"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.measurements[0].value").isNumber());
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
        try (var bootstrapStatic = mockStatic(XxlJobAdminBootstrap.class)) {
            mvc.perform(post("/api/registry").contentType("application/json").content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
            bootstrapStatic.verifyNoInteractions();
        }
        verifyNoInteractions(jobService);
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
        assertThat(base.getProperty("management.endpoints.web.exposure.include")).isEqualTo("*");
        assertThat(base.getProperty("management.endpoint.health.show-details")).isEqualTo("always");
        for (String profile : new String[]{"dev", "test", "prod"}) {
            var source = loader.load(profile, resolver.getResource("classpath:application-" + profile + ".yml")).get(0);
            assertThat(source.getProperty("spring.config.import[0]").toString())
                    .contains("zero-job-admin-mysql.yml?group=${spring.profiles.active}&refreshEnabled=false");
            assertThat(source.getProperty("spring.config.import[1]").toString())
                    .contains("zero-job-admin-common.yml?group=${spring.profiles.active}&refreshEnabled=false");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 解析全部官方Mapper和模板，发现包迁移遗漏和资源语法不兼容
     * @return
     * 返回值: void
     * 返回值说明: 全部 Mapper XML 和 FreeMarker 模板均可解析；不执行 SQL
     */
    @Test
    void migratedMappersAndTemplatesLoad() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.apache.ibatis.session.Configuration mybatis = new org.apache.ibatis.session.Configuration();
        Resource[] mappers = resolver.getResources("classpath:/mapper/**/*Mapper.xml");
        assertThat(mappers).hasSize(9);
        for (Resource mapper : mappers) {
            try (var stream = mapper.getInputStream()) {
                new XMLMapperBuilder(stream, mybatis, mapper.toString(), mybatis.getSqlFragments()).parse();
            }
        }
        assertThat(mybatis.getMappedStatementNames()).isNotEmpty();
        var scheduleUpdate = mybatis.getMappedStatement(XxlJobInfoMapper.class.getName() + ".scheduleBatchUpdate");
        var scannedJob = new XxlJobInfo();
        scannedJob.setTriggerNextTime(1000);
        var observedSchedule = JobScheduleUpdate.capture(scannedJob);
        scannedJob.setTriggerNextTime(2000);
        var scheduleSql = scheduleUpdate.getBoundSql(java.util.Map.of("list", List.of(observedSchedule)));
        assertThat(scheduleSql.getSql()).contains("WHERE trigger_status = 1", "AND job_group = ?",
                "AND trigger_last_time = ?", "AND trigger_next_time = ?", "AND schedule_type = ?",
                "AND schedule_conf <=> ?", "AND misfire_strategy = ?");
        assertThat(observedSchedule.expectedNextTime()).isEqualTo(1000);
        assertThat(observedSchedule.job().getTriggerNextTime()).isEqualTo(2000);
        var completeLog = mybatis.getMappedStatement(XxlJobLogMapper.class.getName() + ".updateHandleInfo");
        assertThat(completeLog.getBoundSql(new XxlJobLog()).getSql()).contains("AND `handle_code` = 0");
        var terminateLog = mybatis.getMappedStatement(XxlJobLogMapper.class.getName() + ".markManuallyTerminated");
        assertThat(terminateLog.getBoundSql(new XxlJobLog()).getSql())
                .contains("`executor_fail_retry_count` = 0", "AND `job_group` = ?", "AND `handle_code` = 0");
        var offlineWindow = java.util.Map.of("triggerBefore", new java.util.Date(), "heartbeatSince", new java.util.Date(),
                "registryGroup", "EXECUTOR", "pageSize", 1000, "log", new XxlJobLog());
        for (String statement : List.of("findLostJobIds", "markLostJob")) {
            assertThat(mybatis.getMappedStatement(XxlJobLogMapper.class.getName() + "." + statement)
                    .getBoundSql(offlineWindow).getSql()).contains("t.handle_code = 0", "g.address_type = 0",
                    "r.registry_key = g.app_name", "r.registry_value = t.executor_address", "r.update_time", "NOT EXISTS");
        }
        var userList = mybatis.getMappedStatement(XxlJobUserMapper.class.getName() + ".pageList");
        assertThat(userList.getBoundSql(java.util.Map.of("offset", 0, "pagesize", 10, "username", "", "role", -1))
                .getSql()).doesNotContain("password", "token");
        var removeDead = mybatis.getMappedStatement(XxlJobRegistryMapper.class.getName() + ".removeDead");
        var now = new java.util.Date();
        var aggregate = mybatis.getMappedStatement(XxlJobLogMapper.class.getName() + ".findLogReport");
        assertThat(aggregate.getBoundSql(java.util.Map.of("from", now, "to", now)).getSql())
                .contains("AS runningCount", "AS sucCount", "AS failCount", "trigger_time >= ? AND trigger_time < ?");
        var logFilters = java.util.Map.of("offset", 0, "pagesize", 10, "jobGroup", 2, "jobId", 123, "logStatus", 2,
                "triggerTimeStart", now, "triggerTimeEndExclusive", now);
        for (String statement : List.of("pageList", "pageListCount")) {
            var logQuery = mybatis.getMappedStatement(XxlJobLogMapper.class.getName() + "." + statement);
            assertThat(logQuery.getBoundSql(logFilters).getSql().replaceAll("\\s+", " "))
                    .contains("t.trigger_time >= ?", "t.trigger_time < ?", "t.trigger_code NOT IN (0, 200)");
        }
        var cleanupQuery = mybatis.getMappedStatement(XxlJobLogMapper.class.getName() + ".findClearLogIds");
        assertThat(cleanupQuery.getBoundSql(java.util.Map.of("jobGroup", 2, "jobId", 123, "clearBeforeNum", 1000,
                        "pagesize", 1000, "afterId", 0L, "maxId", 999L)).getSql())
                .contains("id > ? AND id <= ?", "t.id <= ?", "t.trigger_time DESC, t.id DESC");
        var saveReport = mybatis.getMappedStatement(XxlJobLogReportMapper.class.getName() + ".saveOrUpdate");
        assertThat(saveReport.getBoundSql(new XxlJobLogReport()).getSql())
                .contains("`update_time` IS NULL OR `update_time` < ?");
        var registryUpsert = mybatis.getMappedStatement(XxlJobRegistryMapper.class.getName() + ".registrySaveOrUpdate");
        assertThat(registryUpsert.getBoundSql(java.util.Map.of("registryGroup", "EXECUTOR",
                        "registryKey", "fixture", "registryValue", "http://fixture.invalid", "updateTime", now)).getSql())
                .contains("CASE WHEN `update_time` > ? THEN `update_time` ELSE ? END");
        assertThat(removeDead.getBoundSql(java.util.Map.of("nowTime", now, "timeout", 60, "ids", List.of(1L)))
                .getSql()).contains("WHERE update_time", "AND id IN");
        assertThat(removeDead.getBoundSql(java.util.Map.of("nowTime", now, "timeout", 60, "ids", List.of()))
                .getSql()).contains("AND 1 = 0");
        var refreshAddresses = mybatis.getMappedStatement(XxlJobGroupMapper.class.getName()
                + ".refreshAutoRegisteredAddresses");
        assertThat(refreshAddresses.getBoundSql(java.util.Map.of("id", 2, "nowTime", now, "addressList", ""))
                .getSql()).contains("SET address_list", "AND address_type = 0").doesNotContain("access_token");
        var updateGroup = mybatis.getMappedStatement(XxlJobGroupMapper.class.getName() + ".update");
        var automaticGroup = new XxlJobGroup();
        assertThat(updateGroup.getBoundSql(java.util.Map.of("group", automaticGroup, "previousAddressType", 0))
                .getSql()).contains("AND address_type =").doesNotContain("address_list");
        assertThat(updateGroup.getBoundSql(java.util.Map.of("group", automaticGroup, "previousAddressType", 1))
                .getSql()).contains("address_list");
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
    @Import({LoginController.class, IndexController.class, JobInfoController.class, JobLogController.class,
            JobGroupController.class, JobCodeController.class, JobUserController.class, JobDashboardService.class,
            OpenApiController.class, AdminBizImpl.class, AdminJobBizImpl.class,
            XxlSsoConfig.class, SimpleLoginStore.class, I18nUtil.class})
    static class WebConfiguration {
    }
}
