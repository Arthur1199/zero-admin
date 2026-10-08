package com.zero;

import com.zero.packageconfig.PackageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@EnableDiscoveryClient
@EnableFeignClients(basePackages = PackageProperties.FEIGN_CLIENTS_BASE_PACKAGES)
@MapperScan(PackageProperties.MAPPER_SCAN)
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES)
public class ZeroReportApplication {

    /**
     * logger字段
     */
    private static final Logger logger = LoggerFactory.getLogger(ZeroReportApplication.class);

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
        logger.info("\n" +
                "  ______  ______  _____    ____  \n" +
                " |___  / |  ____| |  __ \\  / __ \\ \n" +
                "    / /  | |__    | |__) || |  | |\n" +
                "   / /   |  __|   |  _  / | |  | |\n" +
                "  / /__  | |____  | | \\ \\ | |__| |\n" +
                " /_____| |______| |_|  \\_\\ \\____/ \n" +
                " 启动成功  ");
        String[] property = context.getEnvironment().getActiveProfiles();
        logger.info("当前应用启动环境："+ Arrays.asList(property).toString());
    }
}
