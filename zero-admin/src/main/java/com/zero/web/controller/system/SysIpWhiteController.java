package com.zero.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.annotation.Log;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.annotation.SystemModule;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.AddressUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.system.domain.SysIpWhite;
import com.zero.system.service.ISysIpWhiteService;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 05:06
 * 说明: 登录白名单信息操作处理，提供后台管理页面使用的列表、详情、新增、修改和删除接口
 *
 */
@RestController
@RequestMapping("/system/ipWhite")
@SystemModule(SystemModuleEnum.SYSTEM)
public class SysIpWhiteController extends BaseController
{
    /**
     * 登录IP白名单服务，封装IP规则校验、唯一性校验和地区自动解析
     */
    @Autowired
    private ISysIpWhiteService ipWhiteService;

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 获取登录IP白名单列表
     * @param ipWhite 查询条件
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页后的白名单列表
     */
    @RequiresPermissions("system:ipWhite:list")
    @GetMapping("/list")
    public TableDataInfo list(SysIpWhite ipWhite)
    {
        startPage();
        List<SysIpWhite> list = ipWhiteService.selectIpWhiteList(ipWhite);
        return getDataTable(list);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 根据IP地址解析地区，用于新增或修改弹窗中自动回填IP地区
     * @param ipAddr IP地址，仅精确IPv4地址会触发解析
     * @return
     * 返回值: AjaxResult
     * 返回值说明: IP地区文本，无法解析时返回空字符串
     */
    @RequiresPermissions("system:ipWhite:list")
    @GetMapping("/region")
    public AjaxResult getRegion(String ipAddr)
    {
        if (!IpUtils.isIP(ipAddr))
        {
            return success((Object) StringUtils.EMPTY);
        }
        String region = AddressUtils.getRealAddressByIP(ipAddr);
        return success((Object) (AddressUtils.UNKNOWN.equals(region) ? StringUtils.EMPTY : region));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 根据白名单ID获取详细信息
     * @param id 白名单ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 白名单详情
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(ipWhiteService.selectIpWhiteById(id));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 新增登录IP白名单，新增后启用状态的规则会参与普通账号登录校验
     * @param ipWhite 白名单信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增结果
     */
    @RequiresPermissions("system:ipWhite:add")
    @Log(title = "登录白名单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysIpWhite ipWhite)
    {
        ipWhite.setCreateBy(getUsername());
        return toAjax(ipWhiteService.insertIpWhite(ipWhite));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 修改登录IP白名单，状态改为关闭后该规则不再参与登录校验
     * @param ipWhite 白名单信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改结果
     */
    @RequiresPermissions("system:ipWhite:edit")
    @Log(title = "登录白名单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysIpWhite ipWhite)
    {
        ipWhite.setUpdateBy(getUsername());
        return toAjax(ipWhiteService.updateIpWhite(ipWhite));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 删除登录IP白名单，删除后对应IP规则不再参与登录校验
     * @param ids 白名单ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除结果
     */
    @RequiresPermissions("system:ipWhite:remove")
    @Log(title = "登录白名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ipWhiteService.deleteIpWhiteByIds(ids));
    }
}
