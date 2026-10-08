package com.zero.system.mapper;

import java.util.List;
import com.zero.system.domain.SysIpWhite;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:06
 * 说明: 系统登录IP白名单数据层，负责sys_ip_white表的增删改查和登录校验规则读取
 *
 */
public interface SysIpWhiteMapper
{
    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 根据白名单ID查询登录IP白名单详情
     * @param id 白名单ID
     * @return
     * 返回值: SysIpWhite
     * 返回值说明: 白名单信息，不存在时返回null
     */
    public SysIpWhite selectIpWhiteById(Long id);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 根据IP地址查询白名单，用于新增和修改时校验唯一性
     * @param ipAddr IP地址，按存储值精确匹配
     * @return
     * 返回值: SysIpWhite
     * 返回值说明: 白名单信息，不存在时返回null
     */
    public SysIpWhite selectIpWhiteByIpAddr(String ipAddr);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 查询登录IP白名单列表，支持IP地址、IP地区、状态和创建时间范围筛选
     * @param ipWhite 查询条件
     * @return
     * 返回值: List<SysIpWhite>
     * 返回值说明: 白名单集合
     */
    public List<SysIpWhite> selectIpWhiteList(SysIpWhite ipWhite);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 查询启用状态的IP白名单地址，登录校验只读取启用状态规则
     * @return
     * 返回值: List<String>
     * 返回值说明: 已启用的白名单IP地址集合
     */
    public List<String> selectEnabledIpAddrs();

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 新增登录IP白名单
     * @param ipWhite 白名单信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int insertIpWhite(SysIpWhite ipWhite);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 修改登录IP白名单
     * @param ipWhite 白名单信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int updateIpWhite(SysIpWhite ipWhite);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 删除登录IP白名单
     * @param id 白名单ID
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int deleteIpWhiteById(Long id);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 批量删除登录IP白名单
     * @param ids 白名单ID集合
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int deleteIpWhiteByIds(Long[] ids);
}
