package com.zero.task.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:08
 * 说明: 启用任务业务库后校验双数据源并装配 MyBatis-Plus Mapper 与分页。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true")
@MapperScan("com.zero.task.mapper")
public class TaskMybatisConfig {

    /** 两个数据源在 Nacos 和 @DS 中使用的固定名称。 */
    private static final String[] DATA_SOURCES = {"zeroBiz", "zeroAdmin"};

    /** 单页查询上限，与 zero-report 一致。 */
    private static final long MAX_PAGE_LIMIT = 10000L;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 连接池初始化前检查两个数据源，缺配置时只提示属性名。
     * @param environment 当前环境配置，包含 Nacos 导入的属性
     * @return
     * 返回值: BeanFactoryPostProcessor
     * 返回值说明: 在业务 Bean 实例化前校验数据源配置
     */
    @Bean
    static BeanFactoryPostProcessor validateTaskDataSources(Environment environment) {
        return beanFactory -> {
            for (String dataSource : DATA_SOURCES) {
                String prefix = "spring.datasource.dynamic.datasource." + dataSource + ".";
                for (String field : new String[]{"url", "username"}) {
                    String key = prefix + field;
                    if (!StringUtils.hasText(environment.getProperty(key))) {
                        throw new IllegalStateException("启用 zero-task MySQL 后必须配置 " + key);
                    }
                }
            }
        };
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 为标准 CRUD 及自定义 Mapper 查询提供 MySQL 分页。
     * @return
     * 返回值: MybatisPlusInterceptor
     * 返回值说明: 限制单页条数并保留请求的页码
     */
    @Bean
    MybatisPlusInterceptor mybatisPlusInterceptor() {
        var interceptor = new MybatisPlusInterceptor();
        var pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setOverflow(false);
        pagination.setMaxLimit(MAX_PAGE_LIMIT);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
