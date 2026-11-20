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
     * 时间: 2026/11/21 03:01
     * 说明: 根据角色关系定位权限变更的定向通知目标，不按操作者的数据范围过滤。
     * @param roleId 权限配置实际变更的角色ID
     * @return 所属用户ID列表，无用户时为空
     */
    List<Long> selectMembersByRoleId(Long roleId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过用户ID删除用户和角色关联
     * @param userId 用户ID
     * @return
     * 返回值: int
     * 返回值说明：通过用户ID删除用户和角色关联
     */
    public int deleteUserRoleByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除用户和角色关联
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除用户和角色关联
     */
    public int deleteUserRole(Long[] ids);

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
     * 说明: 批量新增用户角色信息
     * @param userRoleList 用户角色列表
     * @return
     * 返回值: int
     * 返回值说明：批量新增用户角色信息
     */
    public int batchUserRole(List<SysUserRole> userRoleList);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除用户和角色关联信息
     * @param userRole 用户和角色关联信息
     * @return
     * 返回值: int
     * 返回值说明：删除用户和角色关联信息
     */
    public int deleteUserRoleInfo(SysUserRole userRole);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量取消授权用户角色
     * @param roleId 角色ID
     * @param userIds 需要删除的用户数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量取消授权用户角色
     */
    public int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 在批量取消角色事务内锁定指定用户已存在的角色关系，限定刷新通知的实际目标，不改变操作人权限校验
     * @param roleId 本次取消的角色ID
     * @param userIds 本次操作的用户ID，空集合不返回记录
     * @return
     * 返回值: List<Long>
     * 返回值说明: 已拥有此角色的目标用户ID，无匹配时返回空列表；仅供事务内关系变更使用
     */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
}
