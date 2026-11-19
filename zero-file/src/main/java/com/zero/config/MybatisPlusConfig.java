package com.zero.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: MyBatis-Plus 分页配置。
 */
@Configuration
public class MybatisPlusConfig
{
    private static final long MAX_PAGE_LIMIT = 10000L;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为文件服务装配 MyBatis-Plus 分页拦截器。
     * @return
     * 返回值: MybatisPlusInterceptor
     * 返回值说明: 包含分页规则的拦截器
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
