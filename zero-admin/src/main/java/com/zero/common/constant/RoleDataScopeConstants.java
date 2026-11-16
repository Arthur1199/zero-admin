package com.zero.common.constant;

import java.util.Set;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 16:37
 * 说明: 角色创建时的数据范围取值，与sys_role.data_scope及数据过滤切面保持一致
 */
public final class RoleDataScopeConstants
{
    /** 全部数据权限，也是兼容旧新增调用的默认值 */
    public static final String ALL = "1";
    /** 自定义数据权限，部门关系写入sys_role_dept */
    public static final String CUSTOM = "2";
    /** 当前登录人所在部门 */
    public static final String DEPARTMENT = "3";
    /** 当前登录人所在部门及下级部门 */
    public static final String DEPARTMENT_AND_CHILDREN = "4";
    /** 仅当前登录人 */
    public static final String SELF = "5";
    /** 可接受的数据范围值，拒绝无效范围导致不可预期的数据过滤 */
    public static final Set<String> VALUES = Set.of(ALL, CUSTOM, DEPARTMENT, DEPARTMENT_AND_CHILDREN, SELF);
}
