package com.zero;

import com.zero.packageconfig.PackageProperties;
import java.util.Arrays;
import java.util.TimeZone;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 01:49
 * 说明: 零号Boot Admin监控服务启动入口，负责启动监控控制台并注册到Nacos
 */
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES)
@EnableAdminServer
@EnableDiscoveryClient
public class ZeroBootAdminApplication {

	private static final Logger logger = LoggerFactory.getLogger(ZeroBootAdminApplication.class);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 01:49
	 * 说明: 启动Boot Admin服务，统一使用上海时区记录监控事件
	 * @param args 启动参数
	 * @return
	 * 返回值: void
	 * 返回值说明: 启动Spring Boot应用
	 */
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		PackageProperties.applySystemProperties();
		ConfigurableApplicationContext context = SpringApplication.run(ZeroBootAdminApplication.class, args);
        logger.info("\n" +
                "  ______  ______  _____    ____  \n" +
                " |___  / |  ____| |  __ \\  / __ \\ \n" +
                "    / /  | |__    | |__) || |  | |\n" +
                "   / /   |  __|   |  _  / | |  | |\n" +
                "  / /__  | |____  | | \\ \\ | |__| |\n" +
                " /_____| |______| |_|  \\_\\ \\____/ \n" +
                " " + PackageProperties.START_SUCCESS_MESSAGE + "  ");
        String[] profiles = context.getEnvironment().getActiveProfiles();
		logger.info("当前应用启动环境：{}", Arrays.asList(profiles));
	}
}
