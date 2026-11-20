package com.zero;

import com.zero.packageconfig.PackageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 22:43
 * 说明: 独立任务调度中心入口，初始化 XXL-JOB 调度组件，并在配置变更后重建应用上下文。
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES)
public class ZeroJobAdminApplication {

    /** 当前已完成启动的应用上下文，供配置监听线程识别有效的重启请求。 */
    private static volatile ConfigurableApplicationContext context;

    /** 当前负责关闭旧上下文并启动新上下文的线程，用于拦截并发重启。 */
    private static Thread restartThread;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:43
     * 说明: 使用上海时区启动调度中心，调度时间与数据库连接时区保持一致
     * @param args 环境提供的启动参数，可覆盖端口、环境及配置中心地址
     * @return
     * 返回值: void
     * 返回值说明: 启动调度中心并保存当前上下文，供配置变更后校验重启请求
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        context = SpringApplication.run(ZeroJobAdminApplication.class, args);
        log.info("\n" +
                "  ______  ______  _____    ____  \n" +
                " |___  / |  ____| |  __ \\  / __ \\ \n" +
                "    / /  | |__    | |__) || |  | |\n" +
                "   / /   |  __|   |  _  / | |  | |\n" +
                "  / /__  | |____  | | \\ \\ | |__| |\n" +
                " /_____| |______| |_|  \\_\\ \\____/ \n" +
                " 启动成功  ");
        String[] property = context.getEnvironment().getActiveProfiles();
        log.info("当前应用启动环境："+ Arrays.asList(property).toString());
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
                context = SpringApplication.run(ZeroJobAdminApplication.class, args.getSourceArgs());
            }
            catch (RuntimeException e)
            {
                log.error("服务自动重启失败", e);
            }
        }, "nacos-change-restart-thread");
        restartThread.setDaemon(false);
        // 从 Nacos 回调线程触发重启时，继续使用调度中心的类加载器。
        restartThread.setContextClassLoader(ZeroJobAdminApplication.class.getClassLoader());
        restartThread.start();
        return true;
    }
}
