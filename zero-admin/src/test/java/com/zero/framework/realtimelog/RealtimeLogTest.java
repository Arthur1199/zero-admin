package com.zero.framework.realtimelog;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.zero.framework.aspectj.RequiresPermissionsAspect;
import com.zero.framework.statistics.*;
import com.zero.framework.web.exception.GlobalExceptionHandler;
import com.zero.framework.web.service.PermissionService;
import com.zero.system.service.IStatisticsSwitchService;
import com.zero.web.controller.monitor.RealtimeLogController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.boot.devtools.restart.classloader.RestartClassLoader;
import java.net.URL;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 22:34
 * 说明: 实时日志隔离验证，覆盖并发、容量、续读、脱敏、原日志保留与权限边界。
 */
class RealtimeLogTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:55
     * 说明: 复现DevTools同名Buffer被加载两次的现场，读取必须来自实际Appender而不是接口侧静态实例。
     * @return 断言双类加载器下DEBUG SQL和增量均可读取
     * 返回值: void
     * 返回值说明: 使用真实RestartClassLoader，不连接业务服务
     */
    @Test void devtoolsReadsActualAppenderBuffer() throws Exception
    {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new ch.qos.logback.classic.util.LogbackMDCAdapter());
        URL classes = RealtimeLogAppender.class.getProtectionDomain().getCodeSource().getLocation();
        try (RestartClassLoader loader = new RestartClassLoader(getClass().getClassLoader(), new URL[] { classes }))
        {
            Object actual = loader.loadClass(RealtimeLogAppender.class.getName()).getConstructor().newInstance();
            assertThat(actual.getClass().getClassLoader()).isSameAs(loader);
            assertThat(loader.loadClass(RealtimeLogBuffer.class.getName()).getField("INSTANCE").get(null))
                    .isNotSameAs(RealtimeLogBuffer.INSTANCE);
            @SuppressWarnings("unchecked") Appender<ILoggingEvent> appender = (Appender<ILoggingEvent>) actual;
            appender.setContext(context); appender.setName(RealtimeLogBuffer.APPENDER_NAME); appender.start();
            context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
            Logger sql = context.getLogger("com.zero.system.mapper.StatisticsMapper.pendingInbox");
            sql.setLevel(Level.DEBUG);
            sql.debug("==> Preparing: SELECT event_id FROM sys_stat_event_inbox");
            Map<String, Object> snapshot = RealtimeLogBuffer.readActive(context, null, null, 500);
            assertThat(events(snapshot)).hasSize(1);
            assertThat(events(snapshot).get(0)).containsEntry("level", "DEBUG");
            assertThat((String) events(snapshot).get(0).get("text")).contains("==> Preparing:", "sys_stat_event_inbox");
            sql.debug("==> Parameters: 500(Integer)");
            Map<String, Object> next = RealtimeLogBuffer.readActive(context,
                    (String) snapshot.get("processId"), (Long) snapshot.get("nextCursor"), 500);
            assertThat(events(next)).hasSize(1);
            assertThat((String) events(next).get(0).get("text")).contains("Parameters: 500(Integer)");
        }
        finally { context.stop(); }
        assertThatThrownBy(() -> RealtimeLogBuffer.readActive(new LoggerContext(), null, null, 500))
                .hasMessageContaining("采集器未就绪");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 用Boot实际Logback解析器装载当前XML，验证Appender连接及原INFO/ERROR落盘；仅替换测试日志目录。
     * @param directory JUnit临时目录，不写业务日志路径
     * @return 断言配置及日志输出
     * 返回值: void
     * 返回值说明: 独立LoggerContext，不连接Nacos、Redis或数据库
     */
    @Test void actualLogbackConfiguration(@TempDir Path directory) throws Exception
    {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new ch.qos.logback.classic.util.LogbackMDCAdapter());
        String xml = Files.readString(Path.of("src/main/resources/logback-spring.xml"), StandardCharsets.UTF_8)
                .replace("value=\"logs/${app.name}\"", "value=\"" + directory.toString().replace('\\', '/') + "\"");
        Path configuration = directory.resolve("logback-test.xml");
        Files.writeString(configuration, xml, StandardCharsets.UTF_8);
        // Boot的扩展解析器非public；测试通过反射验证springProperty，与运行时保持同一实现。
        var constructor = Class.forName("org.springframework.boot.logging.logback.SpringBootJoranConfigurator")
                .getDeclaredConstructor(LoggingInitializationContext.class);
        constructor.setAccessible(true);
        var configurator = (ch.qos.logback.classic.joran.JoranConfigurator) constructor.newInstance(
                new LoggingInitializationContext(new StandardEnvironment()));
        configurator.setContext(context);
        Map<String, Object> before = RealtimeLogBuffer.INSTANCE.read(null, null, 500);
        try
        {
            configurator.doConfigure(configuration.toFile());
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
            assertThat(root.getAppender("console").isStarted()).isTrue();
            assertThat(root.getAppender("realtime")).isInstanceOf(RealtimeLogAppender.class);
            assertThat(root.getAppender("realtime").isStarted()).isTrue();
            Logger logger = context.getLogger("com.zero.test.realtime.configuration");
            logger.info("configuration-info-marker");
            logger.warn("configuration-warn-marker");
            logger.error("configuration-error-marker", new IllegalStateException("configuration-stack-marker"));
            var captured = events(RealtimeLogBuffer.INSTANCE.read((String) before.get("processId"), (Long) before.get("nextCursor"), 500));
            assertThat(captured).extracting(e -> e.get("level")).containsExactly("INFO", "WARN", "ERROR");
            assertThat(Files.readString(directory.resolve("sys-info.log"))).contains("configuration-info-marker").doesNotContain("configuration-warn-marker");
            assertThat(Files.readString(directory.resolve("sys-error.log"))).contains("configuration-error-marker", "configuration-stack-marker");
            assertThat(context.getStatusManager().getCopyOfStatusList()).noneMatch(status -> status.getLevel() == ch.qos.logback.core.status.Status.ERROR);
        }
        finally { context.stop(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 验证首次最近日志、容量覆盖、分批续读和进程重启。
     * @return 断言游标及缺口
     * 返回值: void
     * 返回值说明: 不需要外部服务
     */
    @Test void capacityAndCursor()
    {
        RealtimeLogBuffer buffer = new RealtimeLogBuffer();
        buffer.configure(3, 1024, 512);
        for (int i = 0; i < 5; i++) buffer.append(i, "WARN", "main", "test", "event-" + i);
        Map<String, Object> first = buffer.read(null, null, 2);
        assertThat(events(first)).extracting(e -> e.get("sequence")).containsExactly(4L, 5L);
        String process = (String) first.get("processId");
        Map<String, Object> missing = buffer.read(process, 1L, 2);
        assertThat(missing).containsEntry("gap", true).containsEntry("hasMore", true).containsEntry("nextCursor", 4L);
        assertThat(buffer.read(process, 4L, 2)).containsEntry("hasMore", false).containsEntry("nextCursor", 5L);
        assertThat(events(buffer.read(process, 5L, 500))).isEmpty();
        assertThat(buffer.read("previous-process", 999L, 2)).containsEntry("reset", true).containsEntry("nextCursor", 5L);
        assertThat(buffer.read(process, Long.MAX_VALUE, 2)).containsEntry("gap", true);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 验证明确凭证脱敏、中文截断和总字节淘汰，原始输入不落测试产物。
     * @return 断言脱敏和容量
     * 返回值: void
     * 返回值说明: 仅使用虚构凭证
     */
    @Test void redactionAndByteLimit()
    {
        RealtimeLogBuffer buffer = new RealtimeLogBuffer();
        buffer.configure(100, 1024, 512);
        buffer.append(1, "ERROR", "main", "test", "password=example-password token=example-token Authorization: Bearer example-jwt\n{\"api_key\":\"example-key\"}");
        String text = (String) events(buffer.read(null, null, 500)).get(0).get("text");
        assertThat(text).doesNotContain("example-password", "example-token", "example-jwt", "example-key").contains("已脱敏");
        for (int i = 0; i < 4; i++) buffer.append(i, "ERROR", "main", "test", "中文😀".repeat(300));
        List<Map<String, Object>> values = events(buffer.read(null, null, 500));
        assertThat(values).hasSize(2);
        for (Map<String, Object> event : values)
        {
            assertThat(event).containsEntry("truncated", true);
            assertThat((String) event.get("text")).contains("已截断").doesNotContain("�");
            assertThat(((String) event.get("text")).getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(512);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 并发生产日志时序号不重复且读取顺序稳定。
     * @return 断言并发事件无遗漏
     * 返回值: void
     * 返回值说明: 执行器始终释放
     */
    @Test void concurrentCapture() throws Exception
    {
        RealtimeLogBuffer buffer = new RealtimeLogBuffer();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try
        {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 4; i++) tasks.add(executor.submit(() -> {
                for (int j = 0; j < 100; j++) buffer.append(j, "INFO", "worker", "test", "concurrent");
            }));
            for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
            assertThat(events(buffer.read(null, null, 500))).hasSize(400)
                    .extracting(e -> (Long) e.get("sequence")).containsExactlyElementsOf(java.util.stream.LongStream.rangeClosed(1, 400).boxed().toList());
        }
        finally { executor.shutdownNow(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 验证各级别与堆栈采集、原Appender保留、采集异常隔离，轮询请求中的日志同样保留。
     * @return 断言日志链路
     * 返回值: void
     * 返回值说明: 独立LoggerContext，结束后释放
     */
    @Test void appenderPreservesOriginalAndStacktrace()
    {
        LoggerContext context = new LoggerContext();
        context.setName("realtime-test");
        RealtimeLogAppender appender = new RealtimeLogAppender();
        appender.setContext(context); appender.start();
        ListAppender<ILoggingEvent> original = new ListAppender<>();
        original.setContext(context); original.start();
        Logger logger = context.getLogger("test.realtime.capture");
        logger.setLevel(Level.TRACE); logger.addAppender(original); logger.addAppender(appender);
        Map<String, Object> before = RealtimeLogBuffer.INSTANCE.read(null, null, 500);
        String process = (String) before.get("processId");
        long cursor = (long) before.get("nextCursor");
        try
        {
            logger.trace("trace-event"); logger.debug("debug-event"); logger.info("info-event");
            logger.warn("warn-event"); logger.error("error-event", new IllegalStateException("stack-marker"));
            List<Map<String, Object>> captured = events(RealtimeLogBuffer.INSTANCE.read(process, cursor, 500));
            assertThat(captured).extracting(e -> e.get("level")).containsExactly("TRACE", "DEBUG", "INFO", "WARN", "ERROR");
            assertThat((String) captured.get(4).get("text")).contains("IllegalStateException", "stack-marker", "RealtimeLogTest");
            MockHttpServletRequest request = new MockHttpServletRequest("GET", RealtimeLogBuffer.EVENTS_PATH);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
            logger.info("poll-event-retained");
            RequestContextHolder.resetRequestAttributes();
            // 格式化失败只影响采集器；同一事件仍交给原Appender。
            ILoggingEvent broken = mock(ILoggingEvent.class);
            when(broken.getLoggerName()).thenThrow(new IllegalStateException("format-failed"));
            logger.callAppenders(broken);
            assertThat(original.list).hasSize(7);
            assertThat(events(RealtimeLogBuffer.INSTANCE.read(process, cursor, 500))).hasSize(6);
        }
        finally { RequestContextHolder.resetRequestAttributes(); context.stop(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 真实权限切面拒绝无权限访问，有权限返回统一结构并禁止缓存；轮询不进入统计。
     * @return 断言权限和统计隔离
     * 返回值: void
     * 返回值说明: MockMvc隔离验证，不代表真实登录联调
     */
    @Test void permissionCacheAndStatisticsExclusion() throws Exception
    {
        LoggerContext live = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = live.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> previous = root.getAppender(RealtimeLogBuffer.APPENDER_NAME);
        root.detachAppender(RealtimeLogBuffer.APPENDER_NAME);
        RealtimeLogAppender active = new RealtimeLogAppender();
        active.setName(RealtimeLogBuffer.APPENDER_NAME); active.setContext(live); active.start();
        root.addAppender(active);
        try
        {
        PermissionService permissions = mock(PermissionService.class);
        RequiresPermissionsAspect aspect = new RequiresPermissionsAspect();
        ReflectionTestUtils.setField(aspect, "permissionService", permissions);
        AspectJProxyFactory factory = new AspectJProxyFactory(new RealtimeLogController());
        factory.setProxyTargetClass(true); factory.addAspect(aspect);
        RealtimeLogController proxy = factory.getProxy();
        var mvc = MockMvcBuilders.standaloneSetup(proxy).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get(RealtimeLogBuffer.EVENTS_PATH)).andExpect(jsonPath("$.code").value(403));
        when(permissions.hasPermi(RealtimeLogBuffer.PERMISSION)).thenReturn(true);
        mvc.perform(get(RealtimeLogBuffer.EVENTS_PATH)).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.events").isArray()).andExpect(header().string("Cache-Control", "no-store, private"));
        StatisticsPipeline pipeline = mock(StatisticsPipeline.class);
        IStatisticsSwitchService switches = mock(IStatisticsSwitchService.class);
        new StatisticsFilter(new StatisticsProperties(), pipeline, switches).doFilter(
                new MockHttpServletRequest("GET", RealtimeLogBuffer.EVENTS_PATH), new MockHttpServletResponse(), (req, res) -> { });
        verifyNoInteractions(pipeline, switches);
        }
        finally
        {
            root.detachAppender(active); active.stop();
            if (previous != null) root.addAppender(previous);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 提取测试快照事件。
     * @param snapshot 缓冲响应
     * @return 事件集合
     * 返回值: List<Map<String, Object>>
     * 返回值说明: 测试内只读访问
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> events(Map<String, Object> snapshot)
    { return (List<Map<String, Object>>) snapshot.get("events"); }
}
