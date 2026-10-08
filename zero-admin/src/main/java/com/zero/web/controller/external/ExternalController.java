package com.zero.web.controller.external;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.annotation.Anonymous;
import com.zero.common.constant.ExternalApiConstants;
import com.zero.common.constant.IpWhiteConstants;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.utils.ip.AddressUtils;
import com.zero.system.domain.SysIpWhite;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.ISysIpWhiteService;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:51
 * 说明: 对外提供的JSON接口，所有接口均受sys.api.externalEnable总开关控制
 */
@Anonymous
@RestController
@RequestMapping("/external")
public class ExternalController extends BaseController
{
    /**
     * 系统参数服务，用于读取外部接口总开关，关闭时匿名接口不返回业务数据。
     */
    @Autowired
    private ISysConfigService configService;

    /**
     * 登录IP白名单服务，用于对外提供当前启用的白名单规则。
     */
    @Autowired
    private ISysIpWhiteService ipWhiteService;

    /**
     * 说明: 查询启用状态的登录IP白名单地址列表，供外部JSON调用方同步允许访问的IP规则
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为启用白名单IP地址集合，外部接口开关关闭时返回错误信息
     */
    @GetMapping("/ipWhite/list")
    public AjaxResult listIpWhite()
    {
        if (!isExternalEnabled())
        {
            return error("外部接口访问能力未开启");
        }
        SysIpWhite ipWhite = new SysIpWhite();
        ipWhite.setStatus(IpWhiteConstants.STATUS_ENABLED);
        List<String> ipAddrs = ipWhiteService.selectIpWhiteList(ipWhite).stream()
                .map(SysIpWhite::getIpAddr)
                .collect(Collectors.toList());
        return success(ipAddrs);
    }

    /**
     * 说明: 解析IP归属地，供外部JSON调用方按IP获取完整地区结构
     * @param ipAddr IP地址，仅精确IPv4地址会触发解析
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为IpAddressInfo完整结构，非法IP或无法解析时返回unknown结构
     */
    @GetMapping("/ip/region")
    public AjaxResult getIpRegion(String ipAddr)
    {
        if (!isExternalEnabled())
        {
            return error("外部接口访问能力未开启");
        }
        return success(AddressUtils.getRealAddressInfoByIP(ipAddr));
    }

    /**
     * 说明: 判断外部JSON接口总开关是否开启，未配置或非true时按关闭处理，避免误暴露匿名接口数据
     * @return
     * 返回值: boolean
     * 返回值说明: true-允许返回外部接口数据，false-拒绝返回业务数据
     */
    private boolean isExternalEnabled()
    {
        return Boolean.parseBoolean(configService.selectConfigByKey(ExternalApiConstants.EXTERNAL_ENABLE));
    }
}
