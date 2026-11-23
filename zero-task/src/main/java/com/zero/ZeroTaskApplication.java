package com.zero;

import com.zero.packageconfig.PackageProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.redisson.spring.starter.RedissonAutoConfigurationV4;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 22:43
 * 说明: 任务执行服务启动入口，启用业务数据源及 Mapper 扫描，并在配置变更后重建应用上下文。
 */
@Slf4j
@EnableDiscoveryClient
@MapperScan(PackageProperties.MAPPER_SCAN)
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES, exclude = {
        DataRedisAutoConfiguration.class,
        RedissonAutoConfigurationV2.class,
        RedissonAutoConfigurationV4.class
})
public class ZeroTaskApplication {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:39
     * 说明: 使用应用启动时设置的 JVM 时区作为 Jackson 3 的默认时区，保留接口原有日期偏移量。
     * @return
     * 返回值: JsonMapperBuilderCustomizer
     * 返回值说明: 设置 JSON 默认时区，显式配置仍可覆盖。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public JsonMapperBuilderCustomizer jacksonTimeZoneCustomizer() {
        return builder -> builder.defaultTimeZone(TimeZone.getDefault());
    }

    /** 当前已完成启动的应用上下文，供配置监听线程识别有效的重启请求。 */
    private static volatile ConfigurableApplicationContext context;

    /** 当前负责关闭旧上下文并启动新上下文的线程，用于拦截并发重启。 */
    private static Thread restartThread;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:43
     * 说明: 初始化上海时区和包路径默认值，再启动服务并记录当前环境。
     * @param args Spring Boot 启动参数
     * @return
     * 返回值: void
     * 返回值说明: 启动任务执行服务并保存当前上下文，供配置变更后校验重启请求
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        PackageProperties.applySystemProperties();
        context = SpringApplication.run(ZeroTaskApplication.class, args);
        log.info("\n" +
                "  ______  ______  _____    ____  \n" +
                " |___  / |  ____| |  __ \\  / __ \\ \n" +
                "    / /  | |__    | |__) || |  | |\n" +
                "   / /   |  __|   |  _  / | |  | |\n" +
                "  / /__  | |____  | | \\ \\ | |__| |\n" +
                " /_____| |______| |_|  \\_\\ \\____/ \n" +
                " 启动成功  ");
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        log.info("当前应用启动环境：{}", Arrays.toString(activeProfiles));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:43
     * 说明: 接收当前应用上下文的重启请求，在后台关闭旧上下文，并沿用原启动参数重建应用。
     * @param eventContext 收到配置变更事件的上下文
     * @return 是否已启动重启线程
     * 返回值: boolean
     * 返回值说明: 成功启动重启线程时返回 true，已有重启在执行或上下文为空、已关闭、已替换时返回 false
     */
    public static synchronized boolean restart(ConfigurableApplicationContext eventContext)
    {
        if ((restartThread != null && restartThread.isAlive())
                || eventContext == null || context != eventContext || !eventContext.isActive())
        {
            return false;
        }
        restartThread = new Thread(() -> {
            try
            {
                ApplicationArguments args = eventContext.getBean(ApplicationArguments.class);
                // 关闭上下文时由 Spring Boot 按停机配置等待正在处理的请求结束。
                eventContext.close();
                context = SpringApplication.run(ZeroTaskApplication.class, args.getSourceArgs());
            }
            catch (RuntimeException e)
            {
                log.error("服务自动重启失败", e);
            }
        }, "nacos-change-restart-thread");
        restartThread.setDaemon(false);
        // 从 Nacos 回调线程触发重启时，继续使用任务执行服务的类加载器。
        restartThread.setContextClassLoader(ZeroTaskApplication.class.getClassLoader());
        restartThread.start();
        return true;
    }
}
