package com.zero.system.service;

import java.util.List;
import java.util.Set;
import com.zero.common.core.domain.entity.SysRole;
import com.zero.system.domain.SysUserRole;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 角色业务层
 */
public interface ISysRoleService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 18:10
     * 说明: 为新增角色提供可手动修改的默认顺序，不预占编号或写入角色
     * @return
     * 返回值: int
     * 返回值说明: 全部未删除角色最大顺序加1，无记录时为1，超出整数范围时提示错误
     */
    int selectNextRoleSort();

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
     * 说明: 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return
     * 返回值: 角色列表
     * 返回值说明：根据用户ID查询角色列表
     */
    public List<SysRole> selectRolesByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID查询角色权限
     * @param userId 用户ID
     * @return
     * 返回值: 权限列表
     * 返回值说明：根据用户ID查询角色权限
     */
    public Set<String> selectRolePermissionByUserId(Long userId);

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
     * 时间: 2026/11/20 01:36
     * 说明: 校验角色名称是否唯一
     * @param role 角色信息
     * @return
     * 返回值: boolean
     * 返回值说明：校验角色名称是否唯一
     */
    public boolean checkRoleNameUnique(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验角色权限是否唯一
     * @param role 角色信息
     * @return
     * 返回值: boolean
     * 返回值说明：校验角色权限是否唯一
     */
    public boolean checkRoleKeyUnique(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色是否允许操作
     * @param role 角色信息
     * @return
     * 返回值: void
     * 返回值说明：校验角色是否允许操作
     */
    public void checkRoleAllowed(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验角色是否有数据权限
     * @param roleIds 角色id
     * @return
     * 返回值: void
     * 返回值说明：校验角色是否有数据权限
     */
    public void checkRoleDataScope(Long... roleIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过角色ID查询角色使用数量
     * @param roleId 角色ID
     * @return
     * 返回值: int
     * 返回值说明：通过角色ID查询角色使用数量
     */
    public int countUserRoleByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增保存角色信息
     * @param role 角色信息
     * @return
     * 返回值: int
     * 返回值说明：新增保存角色信息
     */
    public int insertRole(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改保存角色信息
     * @param role 角色信息
     * @return
     * 返回值: int
     * 返回值说明：修改保存角色信息
     */
    public int updateRole(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改角色状态
     * @param role 角色信息
     * @return
     * 返回值: int
     * 返回值说明：修改角色状态
     */
    public int updateRoleStatus(SysRole role);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改数据权限信息
     * @param role 角色信息
     * @return
     * 返回值: int
     * 返回值说明：修改数据权限信息
     */
    public int authDataScope(SysRole role);

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

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 取消授权用户角色
     * @param userRole 用户和角色关联信息
     * @return
     * 返回值: int
     * 返回值说明：取消授权用户角色
     */
    public int deleteAuthUser(SysUserRole userRole);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量取消授权用户角色
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量取消授权用户角色
     */
    public int deleteAuthUsers(Long roleId, Long[] userIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量选择授权用户角色
     * @param roleId 角色ID
     * @param userIds 需要删除的用户数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量选择授权用户角色
     */
    public int insertAuthUsers(Long roleId, Long[] userIds);
}
