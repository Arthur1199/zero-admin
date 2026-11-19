package com.zero.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 接收并保存管理后台的 Spring 应用上下文，为静态调用方提供按名称和类型查询 Bean 的入口。
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    /** Spring 容器初始化时注入的应用上下文，供静态 Bean 查询方法使用。 */
    @Getter
    private static ApplicationContext applicationContext;

    @SuppressWarnings("static-access")
	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取Spring上下文容器的Bean名称
     * @return
     * 返回值: String[]
     * 返回值说明: 符合上述查询条件的记录或值集合
     */
    public static String[] getBeanDefinitionNames(){
        return applicationContext.getBeanDefinitionNames();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据名称和期望类型查询当前 Spring 上下文中的 Bean。
     * @param <T> 期望的 Bean 类型
     * @param name Spring Bean 名称
     * @param clazz Bean 的目标 Java 类型
     * @return
     * 返回值: T
     * 返回值说明: 匹配的 Bean，名称缺失或类型不符时由 Spring 抛出异常
     */
    public static <T> T getBean(String name,Class<T> clazz){
        return applicationContext.getBean(name,clazz);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据名称从当前 Spring 上下文获取 Bean。
     * @param name Spring Bean 名称
     * @return
     * 返回值: Object
     * 返回值说明: 该名称对应的 Bean，找不到时由 Spring 抛出异常
     */
    public static Object getBean(String name){
        return applicationContext.getBean(name);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据类型从当前 Spring 上下文获取 Bean。
     * @param <T> 期望的 Bean 类型
     * @param clazz 用于查询的 Java 类型
     * @return
     * 返回值: T
     * 返回值说明: 匹配的 Bean，未找到或有多个候选时由 Spring 抛出异常
     */
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取指定 Bean 的 Java 类型。
     * @param name 要查询的 Spring Bean 名称
     * @return
     * 返回值: Class<?>
     * 返回值说明: Bean 类型，无法确定类型时返回 null
     */
    public static Class<?> getType(String name){
        return applicationContext.getType(name);
    }
}
