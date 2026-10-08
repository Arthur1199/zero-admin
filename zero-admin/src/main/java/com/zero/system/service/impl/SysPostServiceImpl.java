package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.common.constant.UserConstants;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysPost;
import com.zero.system.mapper.SysPostMapper;
import com.zero.system.mapper.SysUserPostMapper;
import com.zero.system.service.ISysPostService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 岗位信息 服务层处理
 */
@Service
public class SysPostServiceImpl implements ISysPostService
{
    /**
     * 岗位数据访问字段
     */
    @Autowired
    private SysPostMapper postMapper;
    /**
     * 用户岗位数据访问字段
     */

    @Autowired
    private SysUserPostMapper userPostMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询岗位信息集合
     * @param post 岗位信息
     * @return
     * 返回值: 岗位信息集合
     * 返回值说明：查询岗位信息集合
     */
    @Override
    public List<SysPost> selectPostList(SysPost post)
    {
        return postMapper.selectPostList(post);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询所有岗位
     * @return
     * 返回值: 岗位列表
     * 返回值说明：查询所有岗位
     */
    @Override
    public List<SysPost> selectPostAll()
    {
        return postMapper.selectPostAll();
    }

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
    @Override
    public SysPost selectPostById(Long postId)
    {
        return postMapper.selectPostById(postId);
    }

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
    @Override
    public List<Long> selectPostListByUserId(Long userId)
    {
        return postMapper.selectPostListByUserId(userId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验岗位名称是否唯一
     * @param post 岗位信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验岗位名称是否唯一
     */
    @Override
    public boolean checkPostNameUnique(SysPost post)
    {
        Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();
        SysPost info = postMapper.checkPostNameUnique(post.getPostName());
        if (StringUtils.isNotNull(info) && info.getPostId().longValue() != postId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验岗位编码是否唯一
     * @param post 岗位信息
     * @return
     * 返回值: 结果
     * 返回值说明：校验岗位编码是否唯一
     */
    @Override
    public boolean checkPostCodeUnique(SysPost post)
    {
        Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();
        SysPost info = postMapper.checkPostCodeUnique(post.getPostCode());
        if (StringUtils.isNotNull(info) && info.getPostId().longValue() != postId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

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
    @Override
    public int countUserPostById(Long postId)
    {
        return userPostMapper.countUserPostById(postId);
    }

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
    @Override
    public int deletePostById(Long postId)
    {
        return postMapper.deletePostById(postId);
    }

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
    @Override
    public int deletePostByIds(Long[] postIds)
    {
        for (Long postId : postIds)
        {
            SysPost post = selectPostById(postId);
            if (countUserPostById(postId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", post.getPostName()));
            }
        }
        return postMapper.deletePostByIds(postIds);
    }

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
    @Override
    public int insertPost(SysPost post)
    {
        return postMapper.insertPost(post);
    }

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
    @Override
    public int updatePost(SysPost post)
    {
        return postMapper.updatePost(post);
    }
}
