package com.zero.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.zero.system.domain.SysUserRole;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户与角色关联表 数据层
 */
public interface SysUserRoleMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过用户ID删除用户和角色关联
     * @param userId 用户ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过用户ID删除用户和角色关联
     */
    public int deleteUserRoleByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除用户和角色关联
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除用户和角色关联
     */
    public int deleteUserRole(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过角色ID查询角色使用数量
     * @param roleId 角色ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过角色ID查询角色使用数量
     */
    public int countUserRoleByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量新增用户角色信息
     * @param userRoleList 用户角色列表
     * @return
     * 返回值: 结果
     * 返回值说明：批量新增用户角色信息
     */
    public int batchUserRole(List<SysUserRole> userRoleList);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除用户和角色关联信息
     * @param userRole 用户和角色关联信息
     * @return
     * 返回值: 结果
     * 返回值说明：删除用户和角色关联信息
     */
    public int deleteUserRoleInfo(SysUserRole userRole);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量取消授权用户角色
     * @param roleId 角色ID
     * @param userIds 需要删除的用户数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量取消授权用户角色
     */
    public int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
}
