package com.zero.jobadmin.framework.web.xxlsso;

import com.xxl.sso.core.auth.interceptor.XxlSsoWebInterceptor;
import com.xxl.sso.core.bootstrap.XxlSsoBootstrap;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 装配调度控制台的 XXL-SSO 登录组件、会话存储和请求拦截。
 */
@Configuration
public class XxlSsoConfig implements WebMvcConfigurer {


    @Value("${xxl-sso.token.key}")
    private String tokenKey;

    @Value("${xxl-sso.token.timeout}")
    private long tokenTimeout;

    @Value("${xxl-sso.client.excluded.paths}")
    private String excludedPaths;

    @Value("${xxl-sso.client.login.path}")
    private String loginPath;


    @Resource
    private SimpleLoginStore loginStore;


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 配置 XXL-SSO 启动组件的登录存储、令牌配置和有效期。
     * @return
     * 返回值: XxlSsoBootstrap
     * 返回值说明: 用于调度控制台的 SSO 启动组件
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public XxlSsoBootstrap xxlSsoBootstrap() {

        XxlSsoBootstrap bootstrap = new XxlSsoBootstrap();
        bootstrap.setLoginStore(loginStore);
        bootstrap.setTokenKey(tokenKey);
        bootstrap.setTokenTimeout(tokenTimeout);
        return bootstrap;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为调度控制台注册 SSO 拦截器，沿用配置中的排除路径。
     * @param registry Spring MVC 拦截器注册表
     * @return
     * 返回值: void
     * 返回值说明: 添加用于处理控制台请求的 SSO 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 2.1、build xxl-sso interceptor
        XxlSsoWebInterceptor webInterceptor = new XxlSsoWebInterceptor(excludedPaths, loginPath);

        // 2.2、add interceptor
        registry.addInterceptor(webInterceptor).addPathPatterns("/**");
    }

}
