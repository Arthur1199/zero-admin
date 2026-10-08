package com.zero;

import com.zero.packageconfig.PackageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 启动程序
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = PackageProperties.FEIGN_CLIENTS_BASE_PACKAGES)
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES, exclude = { DataSourceAutoConfiguration.class })
public class ZeroAdminApplication
{
    /**
     * logger字段
     */
    private static final Logger logger = LoggerFactory.getLogger(ZeroAdminApplication.class);
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: main
     * @param args args参数
     * @return
     * 返回值: void
     * 返回值说明：main
     */

    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        PackageProperties.applySystemProperties();
        ConfigurableApplicationContext context = SpringApplication.run(ZeroAdminApplication.class, args);
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
