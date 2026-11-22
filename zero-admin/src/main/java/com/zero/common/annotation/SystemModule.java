package com.zero.common.annotation;

import com.zero.common.enums.SystemModuleEnum;

import java.lang.annotation.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 19:22
 * 说明: 声明控制器所属统计模块，优先于按路由首段推断的兜底规则
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemModule
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 19:24
     * @return
     * 返回值:SystemModuleEnum
     * 返回值说明：系统模块枚举
     */
    SystemModuleEnum value();
}
