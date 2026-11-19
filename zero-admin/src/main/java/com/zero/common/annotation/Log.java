package com.zero.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.OperatorType;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 自定义操作日志记录注解
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 模块
     * @return
     * 返回值: String
     * 返回值说明: 模块
     */
    public String title() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 功能
     * @return
     * 返回值: BusinessType
     * 返回值说明: 功能
     */
    public BusinessType businessType() default BusinessType.OTHER;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 操作人类别
     * @return
     * 返回值: OperatorType
     * 返回值说明: 操作人类别
     */
    public OperatorType operatorType() default OperatorType.MANAGE;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否保存请求的参数
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean isSaveRequestData() default true;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否保存响应的参数
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean isSaveResponseData() default true;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 排除指定的请求参数
     * @return
     * 返回值: String[]
     * 返回值说明: 排除指定的请求参数
     */
    public String[] excludeParamNames() default {};
}
