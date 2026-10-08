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
     * 时间: 2026/5/14 00:58
     * 说明: 修改角色信息
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改角色信息
     */
    public int updateRole(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增角色信息
     * @param role 角色信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增角色信息
     */
    public int insertRole(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过角色ID删除角色
     * @param roleId 角色ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过角色ID删除角色
     */
    public int deleteRoleById(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除角色信息
     * @param roleIds 需要删除的角色ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除角色信息
     */
    public int deleteRoleByIds(Long[] roleIds);
}
