package com.zero.utils.bean;

import cn.hutool.core.bean.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:16
 * 说明: Bean属性复制统一入口，默认使用Spring BeanUtils，复杂场景可显式调用Hutool BeanUtil
 */
public class BeanCopyHelper {

    private static final Logger logger = LoggerFactory.getLogger(BeanCopyHelper.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 使用Spring BeanUtils复制同名属性，适合普通DTO、VO之间的浅拷贝
     * @param source 源对象，不能为空
     * @param target 目标对象，不能为空
     * 返回值: void
     * 返回值说明: 将源对象同名属性复制到目标对象
     */
    public static void copyBySpring(Object source, Object target) {
        checkCopyArgs(source, target);
        try {
            BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            logger.error("错误：Spring属性复制失败", e);
            throw e;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 使用Spring BeanUtils复制属性并创建目标对象
     * @param source 源对象，不能为空
     * @param targetClass 目标类型，必须有可实例化构造方式
     * @return
     * 返回值: T
     * 返回值说明: 已复制属性的新目标对象
     */
    public static <T> T copyBySpring(Object source, Class<T> targetClass) {
        checkSourceAndClass(source, targetClass);
        T target = BeanUtils.instantiateClass(targetClass);
        copyBySpring(source, target);
        return target;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 使用Spring BeanUtils批量复制集合元素
     * @param sourceList 源对象集合，可为空
     * @param targetClass 目标类型，必须有可实例化构造方式
     * @return
     * 返回值: List<T>
     * 返回值说明: 复制后的目标对象集合；源集合为空时返回空集合
     */
    public static <T> List<T> copyListBySpring(Collection<?> sourceList, Class<T> targetClass) {
        List<T> targetList = new ArrayList<>();
        if (sourceList == null || sourceList.isEmpty()) {
            return targetList;
        }
        for (Object source : sourceList) {
            targetList.add(copyBySpring(source, targetClass));
        }
        return targetList;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 使用hutool BeanUtil复制同名属性，适合需要hutool类型转换能力的浅拷贝场景
     * @param source 源对象，不能为空
     * @param target 目标对象，不能为空
     * 返回值: void
     * 返回值说明: 将源对象同名属性复制到目标对象
     */
    public static void copyByHutool(Object source, Object target) {
        checkCopyArgs(source, target);
        try {
            BeanUtil.copyProperties(source, target);
        } catch (Exception e) {
            logger.error("错误：hutool属性复制失败", e);
            throw e;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 使用Hutool BeanUtil复制属性并创建目标对象
     * @param source 源对象，不能为空
     * @param targetClass 目标类型，必须有可实例化构造方式
     * @return
     * 返回值: T
     * 返回值说明: 已复制属性的新目标对象
     */
    public static <T> T copyByHutool(Object source, Class<T> targetClass) {
        checkSourceAndClass(source, targetClass);
        try {
            return BeanUtil.copyProperties(source, targetClass);
        } catch (Exception e) {
            logger.error("错误：Hutool属性复制并创建对象失败", e);
            throw e;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 使用Hutool BeanUtil批量复制集合元素
     * @param sourceList 源对象集合，可为空
     * @param targetClass 目标类型，必须有可实例化构造方式
     * @return
     * 返回值: List<T>
     * 返回值说明: 复制后的目标对象集合；源集合为空时返回空集合
     */
    public static <T> List<T> copyListByHutool(Collection<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(sourceList, targetClass);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 校验源对象和目标对象不能为空
     * @param source 源对象
     * @param target 目标对象
     * 返回值: void
     * 返回值说明: 参数为空时抛出IllegalArgumentException
     */
    private static void checkCopyArgs(Object source, Object target) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:16
     * 说明: 校验源对象和目标类型不能为空
     * @param source 源对象
     * @param targetClass 目标类型
     * 返回值: void
     * 返回值说明: 参数为空时抛出IllegalArgumentException
     */
    private static void checkSourceAndClass(Object source, Class<?> targetClass) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("targetClass must not be null");
        }
    }
}
