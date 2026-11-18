package com.zero;

import com.zero.packageconfig.PackageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 15:50
 * 说明: 独立任务调度中心入口，保留XXL-JOB控制台和协议并注册到Nacos
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES)
public class ZeroJobAdminApplication {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 15:50
     * 说明: 使用上海时区启动调度中心，调度时间与数据库连接时区保持一致
     * @param args 环境提供的启动参数，可覆盖端口、环境及配置中心地址
     * @return
     * 返回值: void
     * 返回值说明: 启动独立调度服务及Nacos服务注册
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        ConfigurableApplicationContext context = SpringApplication.run(ZeroJobAdminApplication.class, args);
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
}
