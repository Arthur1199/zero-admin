package com.zero.common.filter;

import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 排除JSON敏感属性
 */
public class PropertyPreExcludeFilter extends SimplePropertyPreFilter
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: PropertyPreExclude过滤器
     */
    public PropertyPreExcludeFilter()
    {
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增Excludes
     * @param filters filters参数
     * @return
     * 返回值: PropertyPreExcludeFilter
     * 返回值说明：新增Excludes
     */

    public PropertyPreExcludeFilter addExcludes(String... filters)
    {
        for (int i = 0; i < filters.length; i++)
        {
            this.getExcludes().add(filters[i]);
        }
        return this;
    }
}
