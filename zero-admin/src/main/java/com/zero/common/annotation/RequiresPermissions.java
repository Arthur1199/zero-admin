package com.zero.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 声明访问接口所需的后台权限标识及匹配方式，供统一权限切面校验。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequiresPermissions
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 声明当前操作需要的权限集合。
     * @return
     * 返回值: String[]
     * 返回值说明: 操作所需的权限标识数组
     */
    String[] value() default {};

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 指定多个权限标识之间的匹配方式。
     * @return
     * 返回值: Logical
     * 返回值说明: 权限校验使用的逻辑，默认要求全部匹配
     */
    Logical logical() default Logical.AND;
}
