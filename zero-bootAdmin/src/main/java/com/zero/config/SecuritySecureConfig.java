package com.zero.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2025/5/22 19:46
 * 说明: 安全配置类
 */
@Configuration
public class SecuritySecureConfig {

    private final String adminContextPath;

    public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(adminContextPath + "/assets/**").permitAll()
                        .requestMatchers(adminContextPath + "/actuator/**").permitAll()
                        .requestMatchers(adminContextPath + "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage(adminContextPath + "/login")
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl(adminContextPath + "/logout")
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable); // 禁用CSRF（注意安全风险）

        return http.build();
    }
}