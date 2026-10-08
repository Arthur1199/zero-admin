package com.zero.common.core.domain;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.entity.SysDept;
import com.zero.common.core.domain.entity.SysMenu;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: Treeselect树结构实体类
 */
public class TreeSelect implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 节点禁用
     */
    private boolean disabled = false;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 树查询
     */

    public TreeSelect()
    {

    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 树查询
     * @param dept 部门参数
     */

    public TreeSelect(SysDept dept)
    {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());
        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 树查询
     * @param menu 菜单参数
     */

    public TreeSelect(SysMenu menu)
    {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取ID
     * @return
     * 返回值: Long
     * 返回值说明：获取ID
     */

    public Long getId()
    {
        return id;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置ID
     * @param id ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置ID
     */

    public void setId(Long id)
    {
        this.id = id;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Label
     * @return
     * 返回值: String
     * 返回值说明：获取Label
     */

    public String getLabel()
    {
        return label;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Label
     * @param label label参数
     * @return
     * 返回值: void
     * 返回值说明：设置Label
     */

    public void setLabel(String label)
    {
        this.label = label;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Disabled
     * @return
     * 返回值: boolean
     * 返回值说明：判断Disabled
     */

    public boolean isDisabled()
    {
        return disabled;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Disabled
     * @param disabled disabled参数
     * @return
     * 返回值: void
     * 返回值说明：设置Disabled
     */

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取子节点
     * @return
     * 返回值: List<TreeSelect>
     * 返回值说明：获取子节点
     */

    public List<TreeSelect> getChildren()
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

    public void setChildren(List<TreeSelect> children)
    {
        this.children = children;
    }
}
