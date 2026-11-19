package com.zero;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.zero.constant.TaskDataSourceConstants;
import com.zero.packageconfig.PackageProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.TimeZone;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 12:19
 * 说明: 启动 ZERO-JOB 定时任务执行服务，启用业务数据源及 Mapper 扫描。
 */
@Slf4j
@EnableDiscoveryClient
@MapperScan(PackageProperties.MAPPER_SCAN)
@SpringBootApplication(scanBasePackages = PackageProperties.SCAN_BASE_PACKAGES, exclude = {
        RedisAutoConfiguration.class,
        RedissonAutoConfigurationV2.class
})
public class ZeroTaskApplication {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 12:19
     * 说明: 初始化上海时区和包路径默认值，再启动服务并记录当前环境。
     * @param args Spring Boot 启动参数
     * @return
     * 返回值: void
     * 返回值说明: 启动成功后由 Spring 管理执行器生命周期
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        PackageProperties.applySystemProperties();
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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 21:08
     * 说明: 启动时确认动态数据源已装配并验证业务库可以连接。
     * @param dataSource 由动态数据源自动配置提供的路由数据源
     * @return 启动校验 Bean
     * 返回值: InitializingBean
     * 返回值说明: 缺少业务库或连接失败时阻止应用启动
     */
    @Bean
    static InitializingBean requireTaskDataSource(DataSource dataSource) {
        return () -> {
            Assert.state(dataSource instanceof DynamicRoutingDataSource, "zero-task 必须启用动态数据源");
            var dataSources = ((DynamicRoutingDataSource) dataSource).getDataSources();
            var businessDataSource = dataSources.get(TaskDataSourceConstants.REPORT);
            Assert.state(businessDataSource != null, "zero-task 必须配置 zeroBiz 数据源");
            Assert.state(dataSources.containsKey(TaskDataSourceConstants.ADMIN), "zero-task 必须配置 zeroAdmin 数据源");
            try (var connection = businessDataSource.getConnection()) {
                Assert.state(connection.isValid(5), "zero-task 无法连接 zeroBiz 数据源");
            }
        };
    }
}
