package com.zero.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.domain.DictItem;
import com.zero.queryDto.DictItemQuery;
import com.zero.dto.TableDataInfo;
import com.zero.model.AjaxResult;
import com.zero.service.IDictItemService;
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
 * 说明: 业务字典项接口，供 zero-admin 通过 ReportRestService 调用并间接维护 zero_biz.biz_dict_item
 */
@RestController
@RequestMapping("/reportSystem/bizDict/item")
public class DictItemController
{
    /**
     * 业务字典项服务，负责查询、组内唯一校验、审计和软删除逻辑
     */
    @Autowired
    private IDictItemService dictItemService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 分页查询业务字典项，展开字典组时按 groupId 获取对应明细
     * @param query 查询条件和分页参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页后的业务字典项列表
     */
    @GetMapping("/list")
    public TableDataInfo list(DictItemQuery query)
    {
        IPage<DictItem> page = dictItemService.selectItemPage(query);
        return TableDataInfo.success(page.getRecords(), page.getTotal());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 根据ID获取业务字典项详情，用于管理后台修改弹窗回显
     * @param id 字典项ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为业务字典项详情，不存在时data为null
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(dictItemService.selectItemById(id));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 新增业务字典项，创建人从 admin 透传请求头中获取
     * @param dictItem 字典项信息，itemCode、itemName、itemValue和groupId不能为空
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增操作结果
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DictItem dictItem)
    {
        return dictItemService.insertItem(dictItem) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 修改业务字典项，更新人从 admin 透传请求头中获取
     * @param dictItem 字典项信息，itemCode、itemName、itemValue和groupId不能为空
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改操作结果
     */
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody DictItem dictItem)
    {
        return dictItemService.updateItem(dictItem) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:36
     * 说明: 批量软删除业务字典项，删除后不再出现在展开明细列表
     * @param ids 字典项ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除操作结果
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return dictItemService.deleteItemByIds(ids) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:42
     * 说明: 批量恢复已软删除的业务字典项，所属字典组必须先恢复
     * @param ids 字典项ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 恢复操作结果
     */
    @PutMapping("/restore/{ids}")
    public AjaxResult restore(@PathVariable Long[] ids)
    {
        return dictItemService.restoreItemByIds(ids) ? AjaxResult.success() : AjaxResult.error();
    }
}
