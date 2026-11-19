package com.zero.jobadmin.framework.web.error;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 使用Spring Boot 3错误页接口，将容器错误转入官方控制台错误页面
 */
@Component
public class WebErrorPageRegistrar implements ErrorPageRegistrar {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 保留官方错误页路由，兼容当前Boot 3容器
     * @param registry 当前Web容器的错误页注册器
     * @return
     * 返回值: void
     * 返回值说明: 注册统一错误页面，不改变官方错误处理流程
     */
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage errorPage = new ErrorPage("/errorpage");
        registry.addErrorPages(errorPage);
    }
}
