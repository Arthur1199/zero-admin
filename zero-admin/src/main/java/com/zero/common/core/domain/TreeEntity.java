package com.zero.common.core.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: Tree基类
 */
public class TreeEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 父菜单名称
     */
    private String parentName;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 祖级列表
     */
    private String ancestors;

    /** 子部门 */
    private List<?> children = new ArrayList<>();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取父节点名称
     * @return
     * 返回值: String
     * 返回值说明：获取父节点名称
     */

    public String getParentName()
    {
        return parentName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置父节点名称
     * @param parentName 父节点名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置父节点名称
     */

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取父节点ID
     * @return
     * 返回值: Long
     * 返回值说明：获取父节点ID
     */

    public Long getParentId()
    {
        return parentId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置父节点ID
     * @param parentId 父节点ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置父节点ID
     */

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取OrderNum
     * @return
     * 返回值: Integer
     * 返回值说明：获取OrderNum
     */

    public Integer getOrderNum()
    {
        return orderNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置OrderNum
     * @param orderNum orderNum参数
     * @return
     * 返回值: void
     * 返回值说明：设置OrderNum
     */

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Ancestors
     * @return
     * 返回值: String
     * 返回值说明：获取Ancestors
     */

    public String getAncestors()
    {
        return ancestors;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Ancestors
     * @param ancestors ancestors参数
     * @return
     * 返回值: void
     * 返回值说明：设置Ancestors
     */

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取子节点
     * @return
     * 返回值: List<>
     * 返回值说明：获取子节点
     */

    public List<?> getChildren()
    {
        return children;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置子节点
     * @param children 子节点参数
     * @return
     * 返回值: void
     * 返回值说明：设置子节点
     */

    public void setChildren(List<?> children)
    {
        this.children = children;
    }
}
