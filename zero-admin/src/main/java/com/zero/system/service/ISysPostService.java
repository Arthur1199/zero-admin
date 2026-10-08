package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysPost;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 岗位信息 服务层
 */
public interface ISysPostService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询岗位信息集合
     * @param post 岗位信息
     * @return
     * 返回值: 岗位列表
     * 返回值说明：查询岗位信息集合
     */
    public List<SysPost> selectPostList(SysPost post);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询所有岗位
     * @return
     * 返回值: 岗位列表
     * 返回值说明：查询所有岗位
     */
    public List<SysPost> selectPostAll();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过岗位ID查询岗位信息
     * @param postId 岗位ID
     * @return
     * 返回值: 角色对象信息
     * 返回值说明：通过岗位ID查询岗位信息
     */
    public SysPost selectPostById(Long postId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据用户ID获取岗位选择框列表
     * @param userId 用户ID
     * @return
     * 返回值: 选中岗位ID列表
     * 返回值说明：根据用户ID获取岗位选择框列表
     */
    public List<Long> selectPostListByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验岗位名称
     * @param post 岗位信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验岗位名称
     */
    public boolean checkPostNameUnique(SysPost post);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验岗位编码
     * @param post 岗位信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验岗位编码
     */
    public boolean checkPostCodeUnique(SysPost post);

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
     * 说明: 删除岗位信息
     * @param postId 岗位ID
     * @return
     * 返回值: 结果
     * 返回值说明：删除岗位信息
     */
    public int deletePostById(Long postId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除岗位信息
     * @param postIds 需要删除的岗位ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除岗位信息
     */
    public int deletePostByIds(Long[] postIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增保存岗位信息
     * @param post 岗位信息
     * @return
     * 返回值: 结果
     * 返回值说明：新增保存岗位信息
     */
    public int insertPost(SysPost post);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改保存岗位信息
     * @param post 岗位信息
     * @return
     * 返回值: 结果
     * 返回值说明：修改保存岗位信息
     */
    public int updatePost(SysPost post);
}
