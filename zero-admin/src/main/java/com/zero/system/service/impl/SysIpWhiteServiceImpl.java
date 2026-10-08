package com.zero.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.common.constant.IpWhiteConstants;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.AddressUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.system.domain.SysIpWhite;
import com.zero.system.mapper.SysIpWhiteMapper;
import com.zero.system.service.ISysIpWhiteService;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:06
 * 说明: 系统登录IP白名单服务层实现，负责白名单规则维护、IP归属地补全和登录IP准入判断
 *
 */
@Service
public class SysIpWhiteServiceImpl implements ISysIpWhiteService
{
    /**
     * 登录IP白名单数据访问，读取管理列表和启用状态的登录校验规则
     */
    @Autowired
    private SysIpWhiteMapper ipWhiteMapper;

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 查询登录IP白名单详情
     * @param id 白名单ID
     * @return
     * 返回值: SysIpWhite
     * 返回值说明: 白名单信息，不存在时返回null
     */
    @Override
    public SysIpWhite selectIpWhiteById(Long id)
    {
        return ipWhiteMapper.selectIpWhiteById(id);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 查询登录IP白名单列表
     * @param ipWhite 查询条件
     * @return
     * 返回值: List<SysIpWhite>
     * 返回值说明: 白名单集合
     */
    @Override
    public List<SysIpWhite> selectIpWhiteList(SysIpWhite ipWhite)
    {
        return ipWhiteMapper.selectIpWhiteList(ipWhite);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 新增登录IP白名单，写入前会统一校验IP规则并补充精确IP的归属地
     * @param ipWhite 白名单信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    @Override
    public int insertIpWhite(SysIpWhite ipWhite)
    {
        prepareIpWhite(ipWhite);
        checkIpAddrUnique(ipWhite);
        return ipWhiteMapper.insertIpWhite(ipWhite);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 修改登录IP白名单；IP地址作为白名单唯一业务标识不允许修改，仅允许维护地区、状态和备注
     * @param ipWhite 白名单信息
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    @Override
    public int updateIpWhite(SysIpWhite ipWhite)
    {
        SysIpWhite oldIpWhite = ipWhiteMapper.selectIpWhiteById(ipWhite.getId());
        if (StringUtils.isNull(oldIpWhite))
        {
            throw new ServiceException("登录白名单不存在");
        }
        ipWhite.setIpAddr(oldIpWhite.getIpAddr());
        prepareIpWhite(ipWhite);
        return ipWhiteMapper.updateIpWhite(ipWhite);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 批量删除登录IP白名单
     * @param ids 白名单ID集合
     * @return
     * 返回值: int
     * 返回值说明: 影响行数
     */
    @Override
    public int deleteIpWhiteByIds(Long[] ids)
    {
        return ipWhiteMapper.deleteIpWhiteByIds(ids);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 校验登录IP是否命中启用白名单；无启用规则时返回false，避免开关开启后默认放行
     * @param ipAddr 当前登录IP
     * @return
     * 返回值: boolean
     * 返回值说明: true-允许登录，false-不允许登录
     */
    @Override
    public boolean isLoginIpAllowed(String ipAddr)
    {
        List<String> ipAddrs = ipWhiteMapper.selectEnabledIpAddrs();
        if (StringUtils.isEmpty(ipAddrs))
        {
            return false;
        }
        return IpUtils.isMatchedIp(String.join(";", ipAddrs), ipAddr);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 规范化白名单数据，避免无效IP规则进入登录校验链路；精确IP未填写地区时会调用AddressUtils自动解析
     * @param ipWhite 白名单信息
     * @return
     * 返回值: void
     * 返回值说明: 原对象会被补齐规范化后的IP、状态和IP地区
     */
    private void prepareIpWhite(SysIpWhite ipWhite)
    {
        String ipAddr = StringUtils.trim(ipWhite.getIpAddr());
        if (!IpUtils.isIP(ipAddr) && !IpUtils.isIpWildCard(ipAddr) && !IpUtils.isIPSegment(ipAddr))
        {
            throw new ServiceException("IP地址格式不正确，仅支持精确IP、通配符和IP段");
        }
        ipWhite.setIpAddr(ipAddr);
        if (StringUtils.isEmpty(ipWhite.getStatus()))
        {
            ipWhite.setStatus(IpWhiteConstants.STATUS_DISABLED);
        }
        if (IpUtils.isIP(ipAddr) && StringUtils.isEmpty(ipWhite.getIpRegion()))
        {
            ipWhite.setIpRegion(AddressUtils.getRealAddressByIP(ipAddr));
        }
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 校验IP地址唯一性，避免同一规则重复维护导致白名单判断混乱
     * @param ipWhite 白名单信息
     * @return
     * 返回值: void
     * 返回值说明: 校验不通过会抛出ServiceException
     */
    private void checkIpAddrUnique(SysIpWhite ipWhite)
    {
        Long id = StringUtils.isNull(ipWhite.getId()) ? -1L : ipWhite.getId();
        SysIpWhite info = ipWhiteMapper.selectIpWhiteByIpAddr(ipWhite.getIpAddr());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            throw new ServiceException("IP地址已存在");
        }
    }
}
