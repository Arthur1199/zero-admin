package com.zero.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 岗位表 sys_post
 */
public class SysPost extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 岗位序号
     */
    @Excel(name = "岗位序号", cellType = ColumnType.NUMERIC)
    private Long postId;

    /**
     * 岗位编码
     */
    @Excel(name = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @Excel(name = "岗位名称")
    private String postName;

    /**
     * 岗位排序
     */
    @Excel(name = "岗位排序")
    private Integer postSort;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 用户是否存在此岗位标识 默认不存在
     */
    private boolean flag = false;
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
     * 说明: 获取岗位编码
     * @return
     * 返回值: String
     * 返回值说明：获取岗位编码
     */

    @NotBlank(message = "岗位编码不能为空")
    @Size(min = 0, max = 64, message = "岗位编码长度不能超过64个字符")
    public String getPostCode()
    {
        return postCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置岗位编码
     * @param postCode 岗位编码参数
     * @return
     * 返回值: void
     * 返回值说明：设置岗位编码
     */

    public void setPostCode(String postCode)
    {
        this.postCode = postCode;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取岗位名称
     * @return
     * 返回值: String
     * 返回值说明：获取岗位名称
     */

    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 0, max = 50, message = "岗位名称长度不能超过50个字符")
    public String getPostName()
    {
        return postName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置岗位名称
     * @param postName 岗位名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置岗位名称
     */

    public void setPostName(String postName)
    {
        this.postName = postName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取岗位排序
     * @return
     * 返回值: Integer
     * 返回值说明：获取岗位排序
     */

    @NotNull(message = "显示顺序不能为空")
    public Integer getPostSort()
    {
        return postSort;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置岗位排序
     * @param postSort 岗位排序参数
     * @return
     * 返回值: void
     * 返回值说明：设置岗位排序
     */

    public void setPostSort(Integer postSort)
    {
        this.postSort = postSort;
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
     * 说明: 判断Flag
     * @return
     * 返回值: boolean
     * 返回值说明：判断Flag
     */

    public boolean isFlag()
    {
        return flag;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Flag
     * @param flag flag参数
     * @return
     * 返回值: void
     * 返回值说明：设置Flag
     */

    public void setFlag(boolean flag)
    {
        this.flag = flag;
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
            .append("postId", getPostId())
            .append("postCode", getPostCode())
            .append("postName", getPostName())
            .append("postSort", getPostSort())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
