package com.zero.config;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 保存 Boot Admin 注入的 Spring 应用上下文，供监控组件从静态入口查询上下文和 Bean。
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    //实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 记录 Spring 应用上下文以支持静态 Bean 查询。
     * @param applicationContext Spring 提供的应用上下文
     * @return
     * 返回值: void
     * 返回值说明: 更新静态上下文引用
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext;
    }

    //取得存储在静态变量中的ApplicationContext.
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取已注入的 Spring 应用上下文。
     * @return
     * 返回值: ApplicationContext
     * 返回值说明: 当前应用上下文，未注入时抛出 IllegalStateException
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    //从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从已注入的上下文按名称获取 Bean。
     * @param <T> 调用方期望的 Bean 类型
     * @param name Spring Bean 名称
     * @return
     * 返回值: T
     * 返回值说明: 指定名称的 Bean，名称不存在时由 Spring 报错
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    //从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
    //如果有多个Bean符合Class, 取出第一个.
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从已注入的上下文获取指定类型的 Bean。
     * @param <T> Bean 类型
     * @param clazz 用于按类型查询的 Class
     * @return
     * 返回值: T
     * 返回值说明: 首个匹配的 Bean，无匹配项时返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        @SuppressWarnings("rawtypes")
        Map beanMaps = applicationContext.getBeansOfType(clazz);
        if (beanMaps != null && !beanMaps.isEmpty()) {
            return (T) beanMaps.values().iterator().next();
        } else {
            return null;
        }
    }

    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }

}
