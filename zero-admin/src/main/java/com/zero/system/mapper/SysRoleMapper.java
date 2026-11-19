package com.zero.system.mapper;

import java.util.List;
import com.zero.common.core.domain.entity.SysRole;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色表 数据层
 */
public interface SysRoleMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:10
     * 说明: 读取全部未删除角色的最大显示顺序，包含停用角色，不受页面分页或筛选影响
     * @return
     * 返回值: Long
     * 返回值说明: 最大顺序，没有角色时为空；仅供新增角色默认值参考
     */
    Long selectMaxRoleSort();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据条件分页查询角色数据
     * @param role 角色信息
     * @return
     * 返回值: 角色数据集合信息
     * 返回值说明：根据条件分页查询角色数据
     */
    public List<SysRole> selectRoleList(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询角色
     * @param userId 用户ID
     * @return
     * 返回值: 角色列表
     * 返回值说明：根据用户ID查询角色
     */
    public List<SysRole> selectRolePermissionByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询所有角色
     * @return
     * 返回值: 角色列表
     * 返回值说明：查询所有角色
     */
    public List<SysRole> selectRoleAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID获取角色选择框列表
     * @param userId 用户ID
     * @return
     * 返回值: 选中角色ID列表
     * 返回值说明：根据用户ID获取角色选择框列表
     */
    public List<Long> selectRoleListByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过角色ID查询角色
     * @param roleId 角色ID
     * @return
     * 返回值: 角色对象信息
     * 返回值说明：通过角色ID查询角色
     */
    public SysRole selectRoleById(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询角色
     * @param userName 用户名
     * @return
     * 返回值: 角色列表
     * 返回值说明：根据用户ID查询角色
     */
    public List<SysRole> selectRolesByUserName(String userName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色名称是否唯一
     * @param roleName 角色名称
     * @return
     * 返回值: 角色信息
     * 返回值说明：校验角色名称是否唯一
     */
    public SysRole checkRoleNameUnique(String roleName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色权限是否唯一
     * @param roleKey 角色权限
     * @return
     * 返回值: 角色信息
     * 返回值说明：校验角色权限是否唯一
     */
    public SysRole checkRoleKeyUnique(String roleKey);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改角色信息
     * @param role 角色信息
     * @return
     * 返回值: int
     * 返回值说明：修改角色信息
     */
    public int updateRole(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 17:30
     * 说明: 仅更新角色数据范围和部门联动，防止独立数据权限修改基础字段或菜单
     * @param role 已校验角色编号、范围、部门联动和审计信息
     * @return
     * 返回值: int
     * 返回值说明: 更新行数，调用方已校验角色数据范围
     */
    int updateRoleDataScope(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增角色信息
     * @param role 角色信息
     * @return
     * 返回值: int
     * 返回值说明：新增角色信息
     */
    public int insertRole(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过角色ID删除角色
     * @param roleId 角色ID
     * @return
     * 返回值: int
     * 返回值说明：通过角色ID删除角色
     */
    public int deleteRoleById(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除角色信息
     * @param roleIds 需要删除的角色ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除角色信息
     */
    public int deleteRoleByIds(Long[] roleIds);
}
