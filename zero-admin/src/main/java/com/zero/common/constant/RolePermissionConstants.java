package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 17:30
 * 说明: 角色管理独立授权操作权限，与sys_menu及前端按钮标识保持一致
 */
public final class RolePermissionConstants
{
    /** 修改已存在角色的数据范围及部门关联 */
    public static final String DATA_SCOPE = "system:role:dataScope";

    /** 查询分配用户列表、添加及取消角色用户关联 */
    public static final String AUTH_USER = "system:role:authUser";

    private RolePermissionConstants()
    {
    }
}
