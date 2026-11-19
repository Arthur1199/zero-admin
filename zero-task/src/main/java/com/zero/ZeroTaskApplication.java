package com.zero;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import lombok.extern.slf4j.Slf4j;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:19
 * 说明: 启动 ZERO-JOB 定时任务执行服务，由任务配置按开关装配数据源和 Redisson。
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.zero.task", exclude = {
        DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class, RedisAutoConfiguration.class,
        RedissonAutoConfigurationV2.class
})
public class ZeroTaskApplication {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:19
     * 说明: 按上海时区启动 Spring Boot 服务及任务执行器，启动成功后记录当前环境。
     * @param args Spring Boot 启动参数
     * @return
     * 返回值: void
     * 返回值说明: 启动成功后由 Spring 管理执行器生命周期
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        ConfigurableApplicationContext context = SpringApplication.run(ZeroTaskApplication.class, args);
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
}
