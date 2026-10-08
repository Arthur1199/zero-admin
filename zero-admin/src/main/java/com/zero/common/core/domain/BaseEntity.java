package com.zero.common.core.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: Entity基类
 */
public class BaseEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 搜索值
     */
    @JsonIgnore
    private String searchValue;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Search值
     * @return
     * 返回值: String
     * 返回值说明：获取Search值
     */

    public String getSearchValue()
    {
        return searchValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Search值
     * @param searchValue search值参数
     * @return
     * 返回值: void
     * 返回值说明：设置Search值
     */

    public void setSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取创建By
     * @return
     * 返回值: String
     * 返回值说明：获取创建By
     */

    public String getCreateBy()
    {
        return createBy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置创建By
     * @param createBy 创建By参数
     * @return
     * 返回值: void
     * 返回值说明：设置创建By
     */

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取创建时间
     * @return
     * 返回值: Date
     * 返回值说明：获取创建时间
     */

    public Date getCreateTime()
    {
        return createTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置创建时间
     * @param createTime 创建时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置创建时间
     */

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取修改By
     * @return
     * 返回值: String
     * 返回值说明：获取修改By
     */

    public String getUpdateBy()
    {
        return updateBy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置修改By
     * @param updateBy 修改By参数
     * @return
     * 返回值: void
     * 返回值说明：设置修改By
     */

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取修改时间
     * @return
     * 返回值: Date
     * 返回值说明：获取修改时间
     */

    public Date getUpdateTime()
    {
        return updateTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置修改时间
     * @param updateTime 修改时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置修改时间
     */

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取备注
     * @return
     * 返回值: String
     * 返回值说明：获取备注
     */

    public String getRemark()
    {
        return remark;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置备注
     * @param remark 备注参数
     * @return
     * 返回值: void
     * 返回值说明：设置备注
     */

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取参数集合
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明：获取参数集合
     */

    public Map<String, Object> getParams()
    {
        if (params == null)
        {
            params = new HashMap<>();
        }
        return params;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置参数集合
     * @param params 参数集合参数
     * @return
     * 返回值: void
     * 返回值说明：设置参数集合
     */

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }
}
