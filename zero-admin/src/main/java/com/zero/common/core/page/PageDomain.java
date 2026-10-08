package com.zero.common.core.page;

import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 分页数据
 */
public class PageDomain
{
    /**
     * 当前记录起始索引
     */
    private Integer pageNum;

    /**
     * 每页显示记录数
     */
    private Integer pageSize;

    /**
     * 排序列
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    private String isAsc = "asc";

    /**
     * 分页参数合理化
     */
    private Boolean reasonable = true;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取OrderBy
     * @return
     * 返回值: String
     * 返回值说明：获取OrderBy
     */

    public String getOrderBy()
    {
        if (StringUtils.isEmpty(orderByColumn))
        {
            return "";
        }
        return StringUtils.toUnderScoreCase(orderByColumn) + " " + isAsc;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取分页Num
     * @return
     * 返回值: Integer
     * 返回值说明：获取分页Num
     */

    public Integer getPageNum()
    {
        return pageNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置分页Num
     * @param pageNum 分页Num参数
     * @return
     * 返回值: void
     * 返回值说明：设置分页Num
     */

    public void setPageNum(Integer pageNum)
    {
        this.pageNum = pageNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取分页大小
     * @return
     * 返回值: Integer
     * 返回值说明：获取分页大小
     */

    public Integer getPageSize()
    {
        return pageSize;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置分页大小
     * @param pageSize 分页大小参数
     * @return
     * 返回值: void
     * 返回值说明：设置分页大小
     */

    public void setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取OrderBy字段
     * @return
     * 返回值: String
     * 返回值说明：获取OrderBy字段
     */

    public String getOrderByColumn()
    {
        return orderByColumn;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置OrderBy字段
     * @param orderByColumn orderBy字段参数
     * @return
     * 返回值: void
     * 返回值说明：设置OrderBy字段
     */

    public void setOrderByColumn(String orderByColumn)
    {
        this.orderByColumn = orderByColumn;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IsAsc
     * @return
     * 返回值: String
     * 返回值说明：获取IsAsc
     */

    public String getIsAsc()
    {
        return isAsc;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IsAsc
     * @param isAsc isAsc参数
     * @return
     * 返回值: void
     * 返回值说明：设置IsAsc
     */

    public void setIsAsc(String isAsc)
    {
        if (StringUtils.isNotEmpty(isAsc))
        {
            // 兼容前端排序类型
            if ("ascending".equals(isAsc))
            {
                isAsc = "asc";
            }
            else if ("descending".equals(isAsc))
            {
                isAsc = "desc";
            }
            this.isAsc = isAsc;
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Reasonable
     * @return
     * 返回值: Boolean
     * 返回值说明：获取Reasonable
     */

    public Boolean getReasonable()
    {
        if (StringUtils.isNull(reasonable))
        {
            return Boolean.TRUE;
        }
        return reasonable;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Reasonable
     * @param reasonable reasonable参数
     * @return
     * 返回值: void
     * 返回值说明：设置Reasonable
     */

    public void setReasonable(Boolean reasonable)
    {
        this.reasonable = reasonable;
    }
}
