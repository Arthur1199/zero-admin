package com.zero.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 启用文件存储，会自动根据配置文件进行加载
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({FileStorageAutoConfiguration.class,FileStorageProperties.class})
public @interface EnableFileStorage {
}

