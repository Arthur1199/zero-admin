package com.zero;

import com.zero.packageconfig.PackageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 13:19
 * 说明: 零号报表服务启动入口，后续报表查询、统计和导出能力从该模块扩展
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = PackageProperties.FEIGN_CLIENTS_BASE_PACKAGES)
@MapperScan(PackageProperties.MAPPER_SCAN)
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES)
public class ZeroReportApplication {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 13:19
     * 说明: 启动报表服务
     * @param args 启动参数，由运行环境传入
     * @return
     * 返回值: void
     * 返回值说明: 完成 Spring Boot 报表服务启动
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        PackageProperties.applySystemProperties();
        ConfigurableApplicationContext context = SpringApplication.run(ZeroReportApplication.class, args);
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
