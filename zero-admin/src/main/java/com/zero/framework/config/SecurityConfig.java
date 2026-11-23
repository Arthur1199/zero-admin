package com.zero.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;
import com.zero.framework.config.properties.PermitAllUrlProperties;
import com.zero.framework.security.filter.JwtAuthenticationTokenFilter;
import com.zero.framework.security.handle.AuthenticationEntryPointImpl;
import com.zero.framework.security.handle.LogoutSuccessHandlerImpl;
import com.zero.common.constant.AdminWebSocketConstants;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: spring security配置
 */
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig
{
    /**
     * 自定义用户认证逻辑
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 认证失败处理类
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /**
     * 退出处理类
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * token认证过滤器
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 跨域过滤器
     */
    @Autowired
    private CorsFilter corsFilter;

    /**
     * 允许匿名访问的地址
     */
    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/23 21:10
     * 说明: 装配使用现有用户查询和 BCrypt 验证的认证管理器。
     * @return
     * 返回值: AuthenticationManager
     * 返回值说明：身份验证实现
     */
    @Bean
    public AuthenticationManager authenticationManager()
    {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 配置管理后台的无状态 JWT 安全链路，按路由和 HTTP 方法放行登录、资源、接口文档及 WebSocket 握手。
     * @param httpSecurity Spring 提供的 HTTP 安全配置器
     * @return
     * 返回值: SecurityFilterChain
     * 返回值说明: 用于管理后台请求的安全过滤器链
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
    {
        return httpSecurity
            // CSRF禁用，因为不使用session
            .csrf(csrf -> csrf.disable())
            // 关闭安全过滤器自动添加的缓存控制头，框架嵌入仍限同源。
            .headers((headersCustomizer) -> {
                headersCustomizer.cacheControl(cache -> cache.disable()).frameOptions(options -> options.sameOrigin());
            })
            // 认证失败处理类
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            // 基于token，所以不需要session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 注解标记允许匿名访问的url
            .authorizeHttpRequests((requests) -> {
                permitAllUrl.getUrls().forEach(url -> requests.requestMatchers(url).permitAll());
                // 登录、验证码和首次谷歌绑定接口允许匿名访问；真正的绑定校验在业务层完成。
                requests.requestMatchers("/login", "/captchaImage", "/google/generateGaImage", "/google/bind").permitAll()
                    // 仅允许WebSocket握手，连接必须在首条消息完成JWT认证后才能接收业务事件。
                    .requestMatchers(HttpMethod.GET, AdminWebSocketConstants.PATH).permitAll()
                    // 静态资源，可匿名访问
                    .requestMatchers(HttpMethod.GET, "/","/*.html", "/**.html", "/**.css", "/**.js", "/profile/**").permitAll()
                    // Swagger UI独立读取配置和分组文档，仅放行文档子路径的GET请求。
                    .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui/index.html", "/swagger-ui/**", "/webjars/**", "/*/api-docs", "/druid/**", "/actuator/**").permitAll()
//                    .requestMatchers(HttpMethod.POST, "/", "/*").permitAll()
                    // 除上面外的所有请求全部需要鉴权认证
                    .anyRequest().authenticated();
            })
            // 添加Logout filter
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
            // 添加JWT filter
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // 添加CORS filter
            .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
            .addFilterBefore(corsFilter, LogoutFilter.class)
            .build();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 强散列哈希加密实现
     * @return
     * 返回值: BCryptPasswordEncoder
     * 返回值说明：强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
