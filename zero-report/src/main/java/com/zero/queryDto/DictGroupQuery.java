package com.zero.queryDto;

import com.zero.base.BaseQuery;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:31
 * 说明: 业务字典组分页查询条件，承载管理后台业务参数设置页面的筛选项和分页参数
 */
@Getter
@Setter
public class DictGroupQuery extends BaseQuery
{
    /**
     * 字典组编码，支持模糊查询
     */
    private String groupCode;

    /**
     * 字典组名称，支持模糊查询
     */
    private String groupName;

    /**
     * 状态：1正常，0停用
     */
    private String status;

    /**
     * 删除标志：0未删除，1已删除
     */
    private String delFlag;

}
