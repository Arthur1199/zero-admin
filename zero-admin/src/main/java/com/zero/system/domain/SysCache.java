package com.zero.system.domain;

import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 缓存信息
 */
public class SysCache
{
    /**
     * 缓存名称
     */
    private String cacheName = "";

    /**
     * 缓存键名
     */
    private String cacheKey = "";

    /**
     * 缓存内容
     */
    private String cacheValue = "";

    /**
     * 备注
     */
    private String remark = "";
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统缓存
     */

    public SysCache()
    {

    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统缓存
     * @param cacheName 缓存名称参数
     * @param remark 备注参数
     */

    public SysCache(String cacheName, String remark)
    {
        this.cacheName = cacheName;
        this.remark = remark;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 系统缓存
     * @param cacheName 缓存名称参数
     * @param cacheKey 缓存键名参数
     * @param cacheValue 缓存值参数
     */

    public SysCache(String cacheName, String cacheKey, String cacheValue)
    {
        this.cacheName = StringUtils.replace(cacheName, ":", "");
        this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
        this.cacheValue = cacheValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取缓存名称
     * @return
     * 返回值: String
     * 返回值说明：获取缓存名称
     */

    public String getCacheName()
    {
        return cacheName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置缓存名称
     * @param cacheName 缓存名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置缓存名称
     */

    public void setCacheName(String cacheName)
    {
        this.cacheName = cacheName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取缓存键名
     * @return
     * 返回值: String
     * 返回值说明：获取缓存键名
     */

    public String getCacheKey()
    {
        return cacheKey;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置缓存键名
     * @param cacheKey 缓存键名参数
     * @return
     * 返回值: void
     * 返回值说明：设置缓存键名
     */

    public void setCacheKey(String cacheKey)
    {
        this.cacheKey = cacheKey;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取缓存值
     * @return
     * 返回值: String
     * 返回值说明：获取缓存值
     */

    public String getCacheValue()
    {
        return cacheValue;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置缓存值
     * @param cacheValue 缓存值参数
     * @return
     * 返回值: void
     * 返回值说明：设置缓存值
     */

    public void setCacheValue(String cacheValue)
    {
        this.cacheValue = cacheValue;
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
}
