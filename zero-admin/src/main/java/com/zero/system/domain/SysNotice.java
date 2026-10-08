package com.zero.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.xss.Xss;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 通知公告表 sys_notice
 */
public class SysNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取通知公告ID
     * @return
     * 返回值: Long
     * 返回值说明：获取通知公告ID
     */

    public Long getNoticeId()
    {
        return noticeId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置通知公告ID
     * @param noticeId 通知公告ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置通知公告ID
     */

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置通知公告标题
     * @param noticeTitle 通知公告标题参数
     * @return
     * 返回值: void
     * 返回值说明：设置通知公告标题
     */

    public void setNoticeTitle(String noticeTitle)
    {
        this.noticeTitle = noticeTitle;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取通知公告标题
     * @return
     * 返回值: String
     * 返回值说明：获取通知公告标题
     */

    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过50个字符")
    public String getNoticeTitle()
    {
        return noticeTitle;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置通知公告类型
     * @param noticeType 通知公告类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置通知公告类型
     */

    public void setNoticeType(String noticeType)
    {
        this.noticeType = noticeType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取通知公告类型
     * @return
     * 返回值: String
     * 返回值说明：获取通知公告类型
     */

    public String getNoticeType()
    {
        return noticeType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置通知公告内容
     * @param noticeContent 通知公告内容参数
     * @return
     * 返回值: void
     * 返回值说明：设置通知公告内容
     */

    public void setNoticeContent(String noticeContent)
    {
        this.noticeContent = noticeContent;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取通知公告内容
     * @return
     * 返回值: String
     * 返回值说明：获取通知公告内容
     */

    public String getNoticeContent()
    {
        return noticeContent;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置状态
     * @param status 状态参数
     * @return
     * 返回值: void
     * 返回值说明：设置状态
     */

    public void setStatus(String status)
    {
        this.status = status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取状态
     * @return
     * 返回值: String
     * 返回值说明：获取状态
     */

    public String getStatus()
    {
        return status;
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
            .append("noticeId", getNoticeId())
            .append("noticeTitle", getNoticeTitle())
            .append("noticeType", getNoticeType())
            .append("noticeContent", getNoticeContent())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
