package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysUserPost;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户与岗位关联表 数据层
 */
public interface SysUserPostMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过用户ID删除用户和岗位关联
     * @param userId 用户ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过用户ID删除用户和岗位关联
     */
    public int deleteUserPostByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过岗位ID查询岗位使用数量
     * @param postId 岗位ID
     * @return
     * 返回值: 结果
     * 返回值说明：通过岗位ID查询岗位使用数量
     */
    public int countUserPostById(Long postId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除用户和岗位关联
     * @param ids 需要删除的数据ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除用户和岗位关联
     */
    public int deleteUserPost(Long[] ids);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量新增用户岗位信息
     * @param userPostList 用户岗位列表
     * @return
     * 返回值: 结果
     * 返回值说明：批量新增用户岗位信息
     */
    public int batchUserPost(List<SysUserPost> userPostList);
}
