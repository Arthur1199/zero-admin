package com.zero.common.core.domain.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 菜单权限表 sys_menu
 */
public class SysMenu extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 菜单名称
     */
    private String menuName;

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
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 路由名称，默认和路由地址相同的驼峰格式（注意：因为vue3版本的router会删除名称相同路由，为避免名字的冲突，特殊情况可以自定义）
     */
    private String routeName;

    /**
     * 是否为外链（0是 1否）
     */
    private String isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    private String isCache;

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     */
    private String visible;

    /**
     * 菜单状态（0正常 1停用）
     */
    private String status;

    /**
     * 权限字符串
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /** 子菜单 */
    private List<SysMenu> children = new ArrayList<SysMenu>();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取菜单ID
     * @return
     * 返回值: Long
     * 返回值说明：获取菜单ID
     */

    public Long getMenuId()
    {
        return menuId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置菜单ID
     * @param menuId 菜单ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置菜单ID
     */

    public void setMenuId(Long menuId)
    {
        this.menuId = menuId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取菜单名称
     * @return
     * 返回值: String
     * 返回值说明：获取菜单名称
     */

    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 0, max = 50, message = "菜单名称长度不能超过50个字符")
    public String getMenuName()
    {
        return menuName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置菜单名称
     * @param menuName 菜单名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置菜单名称
     */

    public void setMenuName(String menuName)
    {
        this.menuName = menuName;
    }
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

    @NotNull(message = "显示顺序不能为空")
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
     * 说明: 获取路径
     * @return
     * 返回值: String
     * 返回值说明：获取路径
     */

    @Size(min = 0, max = 200, message = "路由地址不能超过200个字符")
    public String getPath()
    {
        return path;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置路径
     * @param path 路径参数
     * @return
     * 返回值: void
     * 返回值说明：设置路径
     */

    public void setPath(String path)
    {
        this.path = path;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Component
     * @return
     * 返回值: String
     * 返回值说明：获取Component
     */

    @Size(min = 0, max = 200, message = "组件路径不能超过255个字符")
    public String getComponent()
    {
        return component;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Component
     * @param component component参数
     * @return
     * 返回值: void
     * 返回值说明：设置Component
     */

    public void setComponent(String component)
    {
        this.component = component;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取查询
     * @return
     * 返回值: String
     * 返回值说明：获取查询
     */

    public String getQuery()
    {
        return query;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置查询
     * @param query 查询参数
     * @return
     * 返回值: void
     * 返回值说明：设置查询
     */

    public void setQuery(String query)
    {
        this.query = query;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Route名称
     * @return
     * 返回值: String
     * 返回值说明：获取Route名称
     */

    public String getRouteName()
    {
        return routeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Route名称
     * @param routeName route名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Route名称
     */

    public void setRouteName(String routeName)
    {
        this.routeName = routeName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取IsFrame
     * @return
     * 返回值: String
     * 返回值说明：获取IsFrame
     */

    public String getIsFrame()
    {
        return isFrame;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置IsFrame
     * @param isFrame isFrame参数
     * @return
     * 返回值: void
     * 返回值说明：设置IsFrame
     */

    public void setIsFrame(String isFrame)
    {
        this.isFrame = isFrame;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Is缓存
     * @return
     * 返回值: String
     * 返回值说明：获取Is缓存
     */

    public String getIsCache()
    {
        return isCache;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Is缓存
     * @param isCache is缓存参数
     * @return
     * 返回值: void
     * 返回值说明：设置Is缓存
     */

    public void setIsCache(String isCache)
    {
        this.isCache = isCache;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取菜单类型
     * @return
     * 返回值: String
     * 返回值说明：获取菜单类型
     */

    @NotBlank(message = "菜单类型不能为空")
    public String getMenuType()
    {
        return menuType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置菜单类型
     * @param menuType 菜单类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置菜单类型
     */

    public void setMenuType(String menuType)
    {
        this.menuType = menuType;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取显示状态
     * @return
     * 返回值: String
     * 返回值说明：获取显示状态
     */

    public String getVisible()
    {
        return visible;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置显示状态
     * @param visible 显示状态参数
     * @return
     * 返回值: void
     * 返回值说明：设置显示状态
     */

    public void setVisible(String visible)
    {
        this.visible = visible;
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
     * 说明: 获取权限标识
     * @return
     * 返回值: String
     * 返回值说明：获取权限标识
     */

    @Size(min = 0, max = 100, message = "权限标识长度不能超过100个字符")
    public String getPerms()
    {
        return perms;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置权限标识
     * @param perms 权限标识参数
     * @return
     * 返回值: void
     * 返回值说明：设置权限标识
     */

    public void setPerms(String perms)
    {
        this.perms = perms;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Icon
     * @return
     * 返回值: String
     * 返回值说明：获取Icon
     */

    public String getIcon()
    {
        return icon;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Icon
     * @param icon icon参数
     * @return
     * 返回值: void
     * 返回值说明：设置Icon
     */

    public void setIcon(String icon)
    {
        this.icon = icon;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取子节点
     * @return
     * 返回值: List<SysMenu>
     * 返回值说明：获取子节点
     */

    public List<SysMenu> getChildren()
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

    public void setChildren(List<SysMenu> children)
    {
        this.children = children;
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
            .append("menuId", getMenuId())
            .append("menuName", getMenuName())
            .append("parentId", getParentId())
            .append("orderNum", getOrderNum())
            .append("path", getPath())
            .append("component", getComponent())
            .append("query", getQuery())
            .append("routeName", getRouteName())
            .append("isFrame", getIsFrame())
            .append("IsCache", getIsCache())
            .append("menuType", getMenuType())
            .append("visible", getVisible())
            .append("status ", getStatus())
            .append("perms", getPerms())
            .append("icon", getIcon())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
