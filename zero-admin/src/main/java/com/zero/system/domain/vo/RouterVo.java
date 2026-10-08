package com.zero.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 路由配置信息
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo
{
    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 路由名字
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 路由参数：如 {"id": 1, "name": "ry"}
     */
    private String query;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaVo meta;

    /**
     * 子路由
     */
    private List<RouterVo> children;
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
     * 说明: 获取名称
     * @return
     * 返回值: String
     * 返回值说明：获取名称
     */

    public String getName()
    {
        return name;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置名称
     * @param name 名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置名称
     */

    public void setName(String name)
    {
        this.name = name;
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
     * 说明: 获取Hidden
     * @return
     * 返回值: boolean
     * 返回值说明：获取Hidden
     */

    public boolean getHidden()
    {
        return hidden;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Hidden
     * @param hidden hidden参数
     * @return
     * 返回值: void
     * 返回值说明：设置Hidden
     */

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Redirect
     * @return
     * 返回值: String
     * 返回值说明：获取Redirect
     */

    public String getRedirect()
    {
        return redirect;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Redirect
     * @param redirect redirect参数
     * @return
     * 返回值: void
     * 返回值说明：设置Redirect
     */

    public void setRedirect(String redirect)
    {
        this.redirect = redirect;
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
     * 说明: 获取AlwaysShow
     * @return
     * 返回值: Boolean
     * 返回值说明：获取AlwaysShow
     */

    public Boolean getAlwaysShow()
    {
        return alwaysShow;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置AlwaysShow
     * @param alwaysShow alwaysShow参数
     * @return
     * 返回值: void
     * 返回值说明：设置AlwaysShow
     */

    public void setAlwaysShow(Boolean alwaysShow)
    {
        this.alwaysShow = alwaysShow;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取元信息
     * @return
     * 返回值: MetaVo
     * 返回值说明：获取元信息
     */

    public MetaVo getMeta()
    {
        return meta;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置元信息
     * @param meta 元信息参数
     * @return
     * 返回值: void
     * 返回值说明：设置元信息
     */

    public void setMeta(MetaVo meta)
    {
        this.meta = meta;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取子节点
     * @return
     * 返回值: List<RouterVo>
     * 返回值说明：获取子节点
     */

    public List<RouterVo> getChildren()
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

    public void setChildren(List<RouterVo> children)
    {
        this.children = children;
    }
}
