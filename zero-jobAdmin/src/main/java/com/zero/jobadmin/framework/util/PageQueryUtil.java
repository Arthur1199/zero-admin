package com.zero.jobadmin.framework.util;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 22:19
 * 说明: 管理端分页列表的请求边界，避免未经限制的查询进入数据库。
 */
public final class PageQueryUtil {

    /** 管理列表单页允许读取的最大记录数。 */
    private static final int MAX_PAGE_SIZE = 500;

    private PageQueryUtil() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 22:19
     * 说明: 检查可直接传给分页查询的偏移和条数。
     * @param offset 从 0 开始的记录偏移
     * @param pageSize 每页条数，允许 1 至 500
     * @return
     * 返回值: boolean
     * 返回值说明: 偏移或条数越界时返回 true
     */
    public static boolean isInvalid(int offset, int pageSize) {
        return offset < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE;
    }
}
