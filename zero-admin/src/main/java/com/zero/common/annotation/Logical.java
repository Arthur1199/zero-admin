package com.zero.common.annotation;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义多个权限标识之间的匹配方式，供 RequiresPermissions 注解选择全部满足或任一满足。
 */
public enum Logical
{
    /**
     * All permissions must match.
     */
    AND,

    /**
     * Any one permission can match.
     */
    OR
}
