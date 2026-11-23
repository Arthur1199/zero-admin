package com.zero;

import lombok.extern.slf4j.Slf4j;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpServer;
import com.xxl.job.core.constant.Const;
import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.openapi.executor.ExecutorBiz;
import com.xxl.job.core.openapi.executor.dto.LogRequest;
import com.xxl.job.core.openapi.executor.dto.TriggerRequest;
import com.xxl.tool.http.HttpTool;
import com.zero.ZeroTaskApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:22
 * 说明: 在回环网络验证官方客户端、真实执行器和模拟调度中心之间的 HTTP 协议。
 */
@Slf4j
@EnabledIfSystemProperty(named = "zero.task.http-test", matches = "true")
class TaskExecutorHttpTest {

    /** 示例执行器在注册请求和派发请求中使用的 AppName。 */
    private static final String APP_NAME = "zero-task";

    /** 示例任务的注册名称。 */
    private static final String HANDLER = "zeroTaskDemoHandler";

    /** 执行器测试日志的临时目录。 */
    @TempDir
    Path logPath;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:22
     * 说明: 验证任务注册、派发、回调及注销，同时验证 Boot Admin 监控接口和应用日志分流。
     */
    @Test
    void shouldExecuteBeanTaskAndReportResultOverHttp() throws Exception {
        var loopback = InetAddress.getByName("127.0.0.1");
        int executorPort;
        try (var socket = new ServerSocket(0, 1, loopback)) {
            executorPort = socket.getLocalPort();
        }

        var token = UUID.randomUUID().toString();
        var executorAddress = "http://127.0.0.1:" + executorPort;
        var admin = HttpServer.create(new InetSocketAddress(loopback, 0), 0);
        var json = JsonMapper.builder().build();
        var registrations = new LinkedBlockingQueue<JsonNode>();
        var callbacks = new LinkedBlockingQueue<JsonNode>();
        var removals = new LinkedBlockingQueue<JsonNode>();
        admin.createContext("/api/", exchange -> {
            try (exchange) {
                if (!APP_NAME.equals(exchange.getRequestHeaders().getFirst(Const.XXL_JOB_APPNAME))
                        || !token.equals(exchange.getRequestHeaders().getFirst(Const.XXL_JOB_ACCESS_TOKEN))) {
                    exchange.sendResponseHeaders(403, -1);
                    return;
                }
                var requests = switch (exchange.getRequestURI().getPath()) {
                    case "/api/registry" -> registrations;
                    case "/api/callback" -> callbacks;
                    case "/api/registryRemove" -> removals;
                    default -> null;
                };
                if (requests == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                requests.add(json.readTree(exchange.getRequestBody()));
                byte[] response = "{\"code\":200,\"msg\":\"Success\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
        });
        admin.start();
        try {
            try (var context = new SpringApplicationBuilder(ZeroTaskApplication.class).run(
                    "--spring.profiles.active=isolated-test",
                    "--spring.cloud.nacos.config.enabled=false",
                    "--spring.cloud.nacos.discovery.enabled=false",
                    "--spring.cloud.discovery.enabled=false",
                    "--spring.datasource.dynamic.primary=zeroBiz",
                    "--spring.datasource.dynamic.datasource.zeroBiz.url=jdbc:h2:mem:task_executor_biz;MODE=MySQL",
                    "--spring.datasource.dynamic.datasource.zeroBiz.username=sa",
                    "--spring.datasource.dynamic.datasource.zeroAdmin.url=jdbc:h2:mem:task_executor_admin;MODE=MySQL",
                    "--spring.datasource.dynamic.datasource.zeroAdmin.username=sa",
                    "--server.address=127.0.0.1",
                    "--server.port=0",
                    "--logging.file.path=" + logPath,
                    "--xxl.job.executor.admin-addresses=http://127.0.0.1:" + admin.getAddress().getPort(),
                    "--xxl.job.executor.address=" + executorAddress,
                    "--xxl.job.executor.port=" + executorPort,
                    "--xxl.job.executor.access-token=" + token,
                    "--xxl.job.executor.log-path=" + logPath)) {
                log.info("zero-task test info");
                log.warn("zero-task test warning");
                log.error("zero-task test error");
                assertThat(Files.readString(logPath.resolve("sys-info.log")))
                        .contains(" - [shouldExecuteBeanTaskAndReportResultOverHttp,", " - zero-task test info")
                        .doesNotContain("zero-task test warning", "zero-task test error");
                assertThat(Files.readString(logPath.resolve("sys-error.log")))
                        .contains(" - zero-task test error")
                        .doesNotContain("zero-task test info", "zero-task test warning");

                int managementPort = ((WebServerApplicationContext) context).getWebServer().getPort();
                var healthRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:" + managementPort + "/actuator/health"))
                        .timeout(Duration.ofSeconds(5)).build();
                var http = HttpClient.newHttpClient();
                var health = http.send(healthRequest, HttpResponse.BodyHandlers.ofString());
                assertThat(health.statusCode()).isEqualTo(200);
                var healthDetails = json.readTree(health.body());
                assertThat(healthDetails.path("status").asText()).isEqualTo("UP");
                assertThat(healthDetails.path("components").has("diskSpace")).isTrue();
                for (String endpoint : new String[]{"info", "metrics/jvm.memory.used", "threaddump",
                        "loggers", "env/xxl.job.executor.access-token", "configprops/xxl.job.executor", "logfile"}) {
                    var monitoringRequest = HttpRequest.newBuilder()
                            .uri(URI.create("http://127.0.0.1:" + managementPort + "/actuator/" + endpoint))
                            .timeout(Duration.ofSeconds(5)).build();
                    var monitoring = http.send(monitoringRequest, HttpResponse.BodyHandlers.ofString());
                    assertThat(monitoring.statusCode()).as(endpoint).isEqualTo(200);
                    assertThat(monitoring.body().contains(token)).as(endpoint + " 不应返回执行器凭据").isFalse();
                    if ("logfile".equals(endpoint)) {
                        assertThat(monitoring.body()).contains(
                                "zero-task test info", "zero-task test warning", "zero-task test error");
                    }
                }

                var registration = registrations.poll(15, TimeUnit.SECONDS);
                assertThat(registration).isNotNull();
                assertThat(registration.path("registryKey").asText()).isEqualTo(APP_NAME);
                assertThat(registration.path("registryValue").asText()).isEqualTo(executorAddress);

                var executor = client(executorAddress, token);
                assertThat(executor.beat().getCode()).isEqualTo(200);
                assertThat(client(executorAddress, "incorrect").beat().getCode()).isEqualTo(500);
                var unknown = trigger(1000, null);
                unknown.setExecutorHandler("unknownHandler");
                assertThat(executor.trigger(unknown).getCode()).isEqualTo(500);
                var glue = trigger(1003, null);
                glue.setGlueType(GlueTypeEnum.GLUE_GROOVY.name());
                assertThat(executor.trigger(glue).getCode()).isEqualTo(500);

                var success = trigger(1001, null);
                assertThat(executor.trigger(success).getCode()).isEqualTo(200);
                verifyCallback(callbacks.poll(15, TimeUnit.SECONDS), success, 200);
                var failed = trigger(1002, " fail ");
                assertThat(executor.trigger(failed).getCode()).isEqualTo(200);
                verifyCallback(callbacks.poll(15, TimeUnit.SECONDS), failed, 500);

                var log = executor.log(new LogRequest(success.getLogId(), success.getLogDateTime(), 1));
                assertThat(log.getCode()).isEqualTo(200);
                assertThat(log.getData().getLogContent()).contains("参数长度=0", "分片索引=0", "分片总数=1");
            }
            var removal = removals.poll(10, TimeUnit.SECONDS);
            assertThat(removal).isNotNull();
            assertThat(removal.path("registryValue").asText()).isEqualTo(executorAddress);
        } finally {
            admin.stop(0);
            // Spring Boot 的日志上下文由 JVM 管理，测试结束时释放临时文件供 Windows 清理。
            var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            var rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.iteratorForAppenders().forEachRemaining(appender -> {
                if (appender instanceof FileAppender<?>) {
                    rootLogger.detachAppender(appender);
                    appender.stop();
                }
            });
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:22
     * 说明: 构造带原生鉴权头的执行器客户端。
     * @param address 回环执行器地址
     * @param token 本次测试的临时凭据
     * @return
     * 返回值: ExecutorBiz
     * 返回值说明: 发送真实 HTTP 请求的执行器代理
     */
    private ExecutorBiz client(String address, String token) {
        return HttpTool.createClient().url(address)
                .connectTimeout(3000).readTimeout(3000)
                .header(Const.XXL_JOB_ACCESS_TOKEN, token)
                .header(Const.XXL_JOB_APPNAME, APP_NAME)
                .proxy(ExecutorBiz.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:22
     * 说明: 构造同一 BEAN 任务不同运行批次的派发请求。
     * @param logId 运行批次的日志 ID
     * @param parameter 任务参数，可为空
     * @return
     * 返回值: TriggerRequest
     * 返回值说明: 包含任务、日志及分片元数据的请求
     */
    private TriggerRequest trigger(long logId, String parameter) {
        var request = new TriggerRequest();
        request.setJobId(42);
        request.setExecutorHandler(HANDLER);
        request.setExecutorParams(parameter);
        request.setExecutorBlockStrategy(ExecutorBlockStrategyEnum.SERIAL_EXECUTION.name());
        request.setGlueType(GlueTypeEnum.BEAN.name());
        request.setLogId(logId);
        request.setLogDateTime(System.currentTimeMillis());
        request.setBroadcastIndex(0);
        request.setBroadcastTotal(1);
        return request;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:22
     * 说明: 检查回调与已派发批次的对应关系及任务执行状态码。
     * @param response 模拟调度中心收到的回调
     * @param trigger 原派发请求
     * @param code 预期的任务执行状态码
     */
    private void verifyCallback(JsonNode response, TriggerRequest trigger, int code) {
        assertThat(response).isNotNull();
        var results = response.path("callbackList");
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).path("logId").asLong()).isEqualTo(trigger.getLogId());
        assertThat(results.get(0).path("handleCode").asInt()).isEqualTo(code);
    }
}
