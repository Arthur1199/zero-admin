package com.zero.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户和岗位关联 sys_user_post
 */
public class SysUserPost
{
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户ID
     * @return
     * 返回值: Long
     * 返回值说明：获取用户ID
     */

    public Long getUserId()
    {
        return userId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户ID
     * @param userId 用户ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户ID
     */

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取岗位ID
     * @return
     * 返回值: Long
     * 返回值说明：获取岗位ID
     */

    public Long getPostId()
    {
        return postId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置岗位ID
     * @param postId 岗位ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置岗位ID
     */

    public void setPostId(Long postId)
    {
        this.postId = postId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明：toString
     */

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("postId", getPostId())
            .toString();
    }
}
