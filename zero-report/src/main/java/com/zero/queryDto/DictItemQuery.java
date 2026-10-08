package com.zero.queryDto;

import com.zero.base.BaseQuery;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:31
 * 说明: 业务字典项查询条件，承载展开字典组后加载明细项所需的分组、状态、编码和分页参数
 */
@Getter
@Setter
public class DictItemQuery extends BaseQuery
{
    /**
     * 展开字典组时默认一次加载500条明细
     */
    private static final long DEFAULT_ITEM_PAGE_SIZE = 500L;

    /**
     * 字典项最大每页条数，避免展开区域一次拉取过多明细
     */
    private static final long MAX_ITEM_PAGE_SIZE = 1000L;

    /**
     * 字典组ID，展开字典组时必须传入
     */
    private Long groupId;

    /**
     * 字典项编码，支持模糊查询
     */
    private String itemCode;

    /**
     * 字典项名称，支持模糊查询
     */
    private String itemName;

    /**
     * 字典项值，支持模糊查询
     */
    private String itemValue;

    /**
     * 状态：1正常，0停用
     */
    private String status;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:26
     * 说明: 创建字典项查询对象，展开字典组时默认一次加载较多明细，前端传入 pageSize 时仍可覆盖
     */
    public DictItemQuery()
    {
        setPageSize(DEFAULT_ITEM_PAGE_SIZE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:26
     * 说明: 获取字典项查询默认每页条数，pageSize 为空或非法时用于兜底
     * @return
     * 返回值: long
     * 返回值说明: 字典项展开明细默认每页条数
     */
    @Override
    protected long defaultPageSize()
    {
        return DEFAULT_ITEM_PAGE_SIZE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:43
     * 说明: 获取字典项查询允许的最大每页条数，避免展开区域一次拉取过多明细
     * @return
     * 返回值: long
     * 返回值说明: 字典项查询最大每页条数
     */
    @Override
    protected long maxPageSize()
    {
        return MAX_ITEM_PAGE_SIZE;
    }
}
