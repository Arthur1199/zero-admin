package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysPost;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 岗位信息 数据层
 */
public interface SysPostMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询岗位数据集合
     * @param post 岗位信息
     * @return
     * 返回值: 岗位数据集合
     * 返回值说明：查询岗位数据集合
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
     * 时间: 2026/11/20 01:36
     * 说明: 查询用户所属岗位组
     * @param userName 用户名
     * @return
     * 返回值: List<SysPost>
     * 返回值说明：查询用户所属岗位组
     */
    public List<SysPost> selectPostsByUserName(String userName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除岗位信息
     * @param postId 岗位ID
     * @return
     * 返回值: int
     * 返回值说明：删除岗位信息
     */
    public int deletePostById(Long postId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除岗位信息
     * @param postIds 需要删除的岗位ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除岗位信息
     */
    public int deletePostByIds(Long[] postIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 修改岗位信息
     * @param post 岗位信息
     * @return
     * 返回值: int
     * 返回值说明：修改岗位信息
     */
    public int updatePost(SysPost post);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增岗位信息
     * @param post 岗位信息
     * @return
     * 返回值: int
     * 返回值说明：新增岗位信息
     */
    public int insertPost(SysPost post);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验岗位名称
     * @param postName 岗位名称
     * @return
     * 返回值: SysPost
     * 返回值说明：校验岗位名称
     */
    public SysPost checkPostNameUnique(String postName);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验岗位编码
     * @param postCode 岗位编码
     * @return
     * 返回值: SysPost
     * 返回值说明：校验岗位编码
     */
    public SysPost checkPostCodeUnique(String postCode);
}
