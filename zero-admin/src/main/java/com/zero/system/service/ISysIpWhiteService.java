package com.zero.system.service;

import java.util.List;
import com.zero.system.domain.SysIpWhite;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:06
 * 说明: 系统登录IP白名单服务层，封装白名单管理、IP规则校验和登录准入判断
 *
 */
public interface ISysIpWhiteService
{
    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 查询登录IP白名单详情
     * @param id 白名单ID
     * @return
     * 返回值: SysIpWhite
     * 返回值说明: 白名单信息，不存在时返回null
     */
    public SysIpWhite selectIpWhiteById(Long id);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 查询登录IP白名单列表
     * @param ipWhite 查询条件
     * @return
     * 返回值: List<SysIpWhite>
     * 返回值说明: 白名单集合
     */
    public List<SysIpWhite> selectIpWhiteList(SysIpWhite ipWhite);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 新增登录IP白名单，写入前会校验IP格式、唯一性并补充IP地区
     * @param ipWhite 白名单信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int insertIpWhite(SysIpWhite ipWhite);

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 修改登录IP白名单，写入前会校验IP格式、唯一性并补充IP地区
     * @param ipWhite 白名单信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    public int updateIpWhite(SysIpWhite ipWhite);

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

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 校验登录IP是否命中启用白名单，支持精确IP、通配符和IP段
     * @param ipAddr 当前登录IP
     * @return
     * 返回值: boolean
     * 返回值说明: true-允许登录，false-不允许登录
     */
    public boolean isLoginIpAllowed(String ipAddr);
}
