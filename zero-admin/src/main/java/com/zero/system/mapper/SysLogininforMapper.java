package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysLogininfor;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 系统访问日志情况信息 数据层
 */
public interface SysLogininforMapper
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增系统登录日志
     * @param logininfor 访问日志对象
     * @return
     * 返回值: void
     * 返回值说明：新增系统登录日志
     */
    public void insertLogininfor(SysLogininfor logininfor);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询系统登录日志集合
     * @param logininfor 访问日志对象
     * @return
     * 返回值: 登录记录集合
     * 返回值说明：查询系统登录日志集合
     */
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除系统登录日志
     * @param infoIds 需要删除的登录日志ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除系统登录日志
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空系统登录日志
     * @return
     * 返回值: 结果
     * 返回值说明：清空系统登录日志
     */
    public int cleanLogininfor();
}
