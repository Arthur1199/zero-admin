package com.zero.web.controller.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.zero.common.constant.GoogleAuthConstants;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.system.domain.SysApiAuth;
import com.zero.system.service.ISysApiAuthService;
import com.zero.system.service.ISysConfigService;

/**
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/27 03:49
 * 说明: API谷歌验证配置信息操作处理，提供后台管理页面使用的列表、详情、新增、修改和软删除接口
 *
 */
@RestController
@RequestMapping("/system/apiAuth")
@SystemModule(SystemModuleEnum.SYSTEM)
public class SysApiAuthController extends BaseController
{
    /**
     * API谷歌验证配置服务，封装配置规范化、唯一性校验和软删除逻辑
     */
    @Autowired
    private ISysApiAuthService apiAuthService;

    /**
     * 参数配置服务，用于读取接口级谷歌二次验证总开关
     */
    @Autowired
    private ISysConfigService configService;

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 获取API谷歌验证配置列表，分页口径沿用后台列表规范，包含正常、禁用和删除状态记录
     * @param apiAuth 查询条件
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页后的API谷歌验证配置列表
     */
    @RequiresPermissions("system:apiAuth:list")
    @GetMapping("/list")
    public TableDataInfo list(SysApiAuth apiAuth)
    {
        startPage();
        List<SysApiAuth> list = apiAuthService.selectApiAuthList(apiAuth);
        return getDataTable(list);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 04:49
     * 说明: 获取需要接口谷歌二次验证的API列表，登录用户进入系统后前端缓存该列表用于请求前弹窗提示
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data.isNeedGoogleAuth表示总开关，data.authApis表示需要验证的API规则集合
     */
    @GetMapping("/googleAuthApis")
    public AjaxResult googleAuthApis()
    {
        Map<String, Object> data = new HashMap<>();
        boolean isNeedGoogleAuth = Boolean.parseBoolean(configService.selectConfigByKey(GoogleAuthConstants.API_GOOGLE_AUTH_ENABLED));
        data.put("isNeedGoogleAuth", isNeedGoogleAuth);
        data.put("authApis", isNeedGoogleAuth ? apiAuthService.selectGoogleAuthApis() : List.of());
        return success(data);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 根据配置ID获取API谷歌验证配置详情，用于列表页修改弹窗回显
     * @param id API谷歌验证配置ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为API谷歌验证配置详情，不存在时data为null，已删除记录仍返回用于后台展示状态
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(apiAuthService.selectApiAuthById(id));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 新增API谷歌验证配置，新增后的正常状态配置可按请求方式、路径和可选权限标识参与接口谷歌验证匹配
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增结果
     */
    @RequiresPermissions("system:apiAuth:add")
    @Log(title = "API谷歌验证", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysApiAuth apiAuth)
    {
        apiAuth.setCreateBy(getUsername());
        return toAjax(apiAuthService.insertApiAuth(apiAuth));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 修改API谷歌验证配置，状态改为禁用后后续接口不再触发谷歌二次验证
     * @param apiAuth API谷歌验证配置信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改结果
     */
    @RequiresPermissions("system:apiAuth:edit")
    @Log(title = "API谷歌验证", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysApiAuth apiAuth)
    {
        apiAuth.setUpdateBy(getUsername());
        return toAjax(apiAuthService.updateApiAuth(apiAuth));
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/27 03:49
     * 说明: 软删除API谷歌验证配置，删除后仍可在列表筛选展示，但不再参与后续谷歌验证匹配
     * @param ids API谷歌验证配置ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除结果
     */
    @RequiresPermissions("system:apiAuth:remove")
    @Log(title = "API谷歌验证", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(apiAuthService.deleteApiAuthByIds(ids, getUsername()));
    }
}
