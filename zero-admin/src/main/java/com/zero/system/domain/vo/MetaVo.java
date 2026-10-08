package com.zero.system.domain.vo;

import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 路由显示信息
 */
public class MetaVo
{
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private boolean noCache;

    /**
     * 内链地址（http(s)://开头）
     */
    private String link;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 元信息Vo
     */

    public MetaVo()
    {
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 元信息Vo
     * @param title 标题参数
     * @param icon icon参数
     */

    public MetaVo(String title, String icon)
    {
        this.title = title;
        this.icon = icon;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 元信息Vo
     * @param title 标题参数
     * @param icon icon参数
     * @param noCache no缓存参数
     */

    public MetaVo(String title, String icon, boolean noCache)
    {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 元信息Vo
     * @param title 标题参数
     * @param icon icon参数
     * @param link link参数
     */

    public MetaVo(String title, String icon, String link)
    {
        this.title = title;
        this.icon = icon;
        this.link = link;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 元信息Vo
     * @param title 标题参数
     * @param icon icon参数
     * @param noCache no缓存参数
     * @param link link参数
     */

    public MetaVo(String title, String icon, boolean noCache, String link)
    {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        if (StringUtils.ishttp(link))
        {
            this.link = link;
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断No缓存
     * @return
     * 返回值: boolean
     * 返回值说明：判断No缓存
     */

    public boolean isNoCache()
    {
        return noCache;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置No缓存
     * @param noCache no缓存参数
     * @return
     * 返回值: void
     * 返回值说明：设置No缓存
     */

    public void setNoCache(boolean noCache)
    {
        this.noCache = noCache;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取标题
     * @return
     * 返回值: String
     * 返回值说明：获取标题
     */

    public String getTitle()
    {
        return title;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置标题
     * @param title 标题参数
     * @return
     * 返回值: void
     * 返回值说明：设置标题
     */

    public void setTitle(String title)
    {
        this.title = title;
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
     * 说明: 获取Link
     * @return
     * 返回值: String
     * 返回值说明：获取Link
     */

    public String getLink()
    {
        return link;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Link
     * @param link link参数
     * @return
     * 返回值: void
     * 返回值说明：设置Link
     */

    public void setLink(String link)
    {
        this.link = link;
    }
}
