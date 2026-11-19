package com.zero.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 作者: Jack
 * 时间: 2021年10月31日下午2:11:05
 * 说明: 上下文工具类
 *
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    /**
     * -- GETTER --
     *  获取Spring的上下文
     *
     * @return
     */
    @Getter
    private static ApplicationContext applicationContext;

    @SuppressWarnings("static-access")
	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取Spring上下文容器的Bean名称
     * @return
     */
    public static String[] getBeanDefinitionNames(){
        return applicationContext.getBeanDefinitionNames();
    }

    /**
     * 根据Bean的名称获取Bean
     * @param name Bean名称
     * @param clazz 需要获取的实体
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name,Class<T> clazz){
        return applicationContext.getBean(name,clazz);
    }

    /**
     * 根据Bean的名称获取Bean
     * @param name
     * @return
     */
    public static Object getBean(String name){
        return applicationContext.getBean(name);
    }

    /**
     * 根据class获取Bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据Bean名称获取Class
     * @param name
     * @return
     */
    public static Class<?> getType(String name){
        return applicationContext.getType(name);
    }
}
