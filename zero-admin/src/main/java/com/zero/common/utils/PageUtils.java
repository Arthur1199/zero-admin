package com.zero.common.utils;

import com.github.pagehelper.PageHelper;
import com.zero.common.core.page.PageDomain;
import com.zero.common.core.page.TableSupport;
import com.zero.common.utils.sql.SqlUtil;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 分页工具类
 */
public class PageUtils extends PageHelper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置请求分页数据
     * @return
     * 返回值: void
     * 返回值说明：设置请求分页数据
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清理分页的线程变量
     * @return
     * 返回值: void
     * 返回值说明：清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
