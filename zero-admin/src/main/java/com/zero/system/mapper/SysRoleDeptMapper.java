package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysRoleDept;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色与部门关联表 数据层
 */
public interface SysRoleDeptMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 03:01
     * 说明: 取得角色行锁后读取最新的部门关联，避免与同一角色的并发保存比较旧快照。
     * @param roleId 角色ID
     * @return 已保存的部门ID，无关联时为空列表
     */
    List<Long> selectDeptIdsForUpdate(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过角色ID删除角色和部门关联
     * @param roleId 角色ID
     * @return
     * 返回值: int
     * 返回值说明：通过角色ID删除角色和部门关联
     */
    public int deleteRoleDeptByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除角色部门关联信息
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除角色部门关联信息
     */
    public int deleteRoleDept(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询部门使用数量
     * @param deptId 部门ID
     * @return
     * 返回值: int
     * 返回值说明：查询部门使用数量
     */
    public int selectCountRoleDeptByDeptId(Long deptId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量新增角色部门信息
     * @param roleDeptList 角色部门列表
     * @return
     * 返回值: int
     * 返回值说明：批量新增角色部门信息
     */
    public int batchRoleDept(List<SysRoleDept> roleDeptList);
}
