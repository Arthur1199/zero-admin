package com.zero.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.domain.DictGroup;
import com.zero.queryDto.DictGroupQuery;
import com.zero.dto.TableDataInfo;
import com.zero.model.AjaxResult;
import com.zero.service.IDictGroupService;
import jakarta.validation.Valid;
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

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:36
 * 说明: 业务字典组接口，供 zero-admin 通过 ReportRestService 调用并间接维护 zero_biz.biz_dict_group
 */
@RestController
@RequestMapping("/reportSystem/bizDict/group")
public class DictGroupController
{
    /**
     * 业务字典组服务，负责查询、校验、审计和软删除逻辑
     */
    @Autowired
    private IDictGroupService dictGroupService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 分页查询业务字典组，支持按状态、删除标志和创建时间筛选，返回管理后台表格需要的 rows、total、code 和 msg
     * @param query 查询条件和分页参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页后的业务字典组列表
     */
    @GetMapping("/list")
    public TableDataInfo list(DictGroupQuery query)
    {
        IPage<DictGroup> page = dictGroupService.selectGroupPage(query);
        return TableDataInfo.success(page.getRecords(), page.getTotal());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 09:36
     * 说明: 查询业务字典组下拉列表，不按状态和删除标志过滤，供管理后台进入页面后一次性加载并在前端本地过滤
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为全量业务字典组下拉数据
     */
    @GetMapping("/options")
    public AjaxResult options()
    {
        return AjaxResult.success(dictGroupService.selectGroupOptions());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 根据ID获取业务字典组详情，用于管理后台修改弹窗回显
     * @param id 字典组ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为业务字典组详情，不存在时data为null
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(dictGroupService.selectGroupById(id));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 新增业务字典组，创建人从 admin 透传请求头中获取
     * @param dictGroup 字典组信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增操作结果
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DictGroup dictGroup)
    {
        return dictGroupService.insertGroup(dictGroup) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 修改业务字典组，更新人从 admin 透传请求头中获取
     * @param dictGroup 字典组信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改操作结果
     */
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody DictGroup dictGroup)
    {
        return dictGroupService.updateGroup(dictGroup) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 批量软删除业务字典组，不再要求先删除字典项
     * @param ids 字典组ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除操作结果
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return dictGroupService.deleteGroupByIds(ids) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:42
     * 说明: 批量恢复已软删除的业务字典组，更新人从 admin 透传请求头中获取
     * @param ids 字典组ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 恢复操作结果
     */
    @PutMapping("/restore/{ids}")
    public AjaxResult restore(@PathVariable Long[] ids)
    {
        return dictGroupService.restoreGroupByIds(ids) ? AjaxResult.success() : AjaxResult.error();
    }
}
