package com.zero.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 17:47
 * 说明: 报表服务 MyBatis-Plus 配置，统一承载标准 CRUD 和简单分页查询能力
 */
@Configuration
public class MybatisPlusConfig
{
    /**
     * 单页最大查询条数，避免误传超大 pageSize 导致报表查询压垮数据库
     */
    private static final long MAX_PAGE_LIMIT = 10000L;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 17:47
     * 说明: 配置 MyBatis-Plus 分页拦截器；复杂 XML 报表 SQL 使用 PageHelper 时，不要同时使用 MyBatis-Plus Page 对象分页
     * @return
     * 返回值: MybatisPlusInterceptor
     * 返回值说明: 包含 MySQL 分页拦截器的 MyBatis-Plus 插件集合
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor()
    {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setOverflow(false);
        paginationInnerInterceptor.setMaxLimit(MAX_PAGE_LIMIT);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
