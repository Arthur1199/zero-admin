package com.zero.web.reportController;

import com.zero.common.annotation.Log;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.feign.ReportRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:38
 * 说明: 业务参数设置代理控制器，管理后台负责权限和操作日志，实际字典组、字典项增删改查通过 ReportRestService 调用 zero-report 完成
 */
@RestController
@RequestMapping("/reportSystem/bizDict")
public class BizDictController extends BaseController
{
    /**
     * report 字典组接口路径，两边路径保持一致，降低转发映射复杂度
     */
    private static final String GROUP_PATH = "/reportSystem/bizDict/group";

    /**
     * report 字典项接口路径，两边路径保持一致，展开字典组时按 groupId 查询明细
     */
    private static final String ITEM_PATH = "/reportSystem/bizDict/item";

    /**
     * 报表服务通用调用服务，统一透传登录令牌和管理员上下文
     */
    @Autowired
    private ReportRestService reportRestService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 查询业务字典组列表，分页结果由 report 服务返回，admin 只负责权限控制和透传
     * @param queryParams 查询条件，包含 pageNum、pageSize、groupCode、groupName、status、delFlag、beginTime、endTime
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页后的业务字典组列表
     */
    @RequiresPermissions("reportSystem:bizDict:list")
    @GetMapping("/group/list")
    public TableDataInfo groupList(@RequestParam Map<String, Object> queryParams)
    {
        return reportRestService.exchange(GROUP_PATH + "/list", HttpMethod.GET, null, queryParams,
                TableDataInfo.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 09:36
     * 说明: 查询业务字典组下拉列表，不做按钮权限控制，供业务参数设置页面初始化查询区选项
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为全量业务字典组下拉数据，由前端filterable本地过滤
     */
    @GetMapping("/group/options")
    public AjaxResult groupOptions()
    {
        return reportRestService.invokeGetByMap(GROUP_PATH + "/options", Map.of());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 获取业务字典组详情，用于列表页修改弹窗回显；能进入列表并具备修改按钮权限即可完成页面流程
     * @param id 字典组ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为业务字典组详情
     */
    @GetMapping("/group/{id}")
    public AjaxResult getGroupInfo(@PathVariable Long id)
    {
        return reportRestService.invokeGetByMap(GROUP_PATH + "/" + id, Map.of());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 新增业务字典组，权限和操作日志在 admin 记录，真实写入由 report 服务完成
     * @param body 字典组信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:add")
    @Log(title = "业务参数设置-字典组", businessType = BusinessType.INSERT)
    @PostMapping("/group")
    public AjaxResult addGroup(@RequestBody Map<String, Object> body)
    {
        return reportRestService.invokePostJson(GROUP_PATH, body);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 修改业务字典组，权限和操作日志在 admin 记录，真实写入由 report 服务完成
     * @param body 字典组信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:edit")
    @Log(title = "业务参数设置-字典组", businessType = BusinessType.UPDATE)
    @PutMapping("/group")
    public AjaxResult editGroup(@RequestBody Map<String, Object> body)
    {
        return reportRestService.exchange(GROUP_PATH, HttpMethod.PUT, body, Map.of(), AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 批量软删除业务字典组，不再要求先删除字典项，真实删除由 report 服务完成
     * @param ids 字典组ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:remove")
    @Log(title = "业务参数设置-字典组", businessType = BusinessType.DELETE)
    @DeleteMapping("/group/{ids}")
    public AjaxResult removeGroup(@PathVariable Long[] ids)
    {
        return reportRestService.exchange(GROUP_PATH + "/" + joinIds(ids), HttpMethod.DELETE, null, Map.of(),
                AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:42
     * 说明: 批量恢复已软删除的业务字典组，复用删除权限，真实恢复由 report 服务完成
     * @param ids 字典组ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 恢复操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:remove")
    @Log(title = "业务参数设置-字典组恢复", businessType = BusinessType.UPDATE)
    @PutMapping("/group/restore/{ids}")
    public AjaxResult restoreGroup(@PathVariable Long[] ids)
    {
        return reportRestService.exchange(GROUP_PATH + "/restore/" + joinIds(ids), HttpMethod.PUT, null, Map.of(),
                AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 查询业务字典项列表，展开字典组时按 groupId 加载明细
     * @param queryParams 查询条件，包含 groupId、itemCode、itemName、itemValue、status、pageNum、pageSize
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 分页后的业务字典项列表
     */
    @RequiresPermissions("reportSystem:bizDict:list")
    @GetMapping("/item/list")
    public TableDataInfo itemList(@RequestParam Map<String, Object> queryParams)
    {
        return reportRestService.exchange(ITEM_PATH + "/list", HttpMethod.GET, null, queryParams, TableDataInfo.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 获取业务字典项详情，用于展开明细列表的修改弹窗回显
     * @param id 字典项ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为业务字典项详情
     */
    @GetMapping("/item/{id}")
    public AjaxResult getItemInfo(@PathVariable Long id)
    {
        return reportRestService.invokeGetByMap(ITEM_PATH + "/" + id, Map.of());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 新增业务字典项，itemCode、itemName、itemValue和groupId由 report 服务再次校验
     * @param body 字典项信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:add")
    @Log(title = "业务参数设置-字典项", businessType = BusinessType.INSERT)
    @PostMapping("/item")
    public AjaxResult addItem(@RequestBody Map<String, Object> body)
    {
        return reportRestService.invokePostJson(ITEM_PATH, body);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 修改业务字典项，权限和操作日志在 admin 记录，真实写入由 report 服务完成
     * @param body 字典项信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:edit")
    @Log(title = "业务参数设置-字典项", businessType = BusinessType.UPDATE)
    @PutMapping("/item")
    public AjaxResult editItem(@RequestBody Map<String, Object> body)
    {
        return reportRestService.exchange(ITEM_PATH, HttpMethod.PUT, body, Map.of(), AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 批量软删除业务字典项，删除后展开明细列表不再展示
     * @param ids 字典项ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:remove")
    @Log(title = "业务参数设置-字典项", businessType = BusinessType.DELETE)
    @DeleteMapping("/item/{ids}")
    public AjaxResult removeItem(@PathVariable Long[] ids)
    {
        return reportRestService.exchange(ITEM_PATH + "/" + joinIds(ids), HttpMethod.DELETE, null, Map.of(),
                AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:42
     * 说明: 批量恢复已软删除的业务字典项，复用删除权限，真实恢复由 report 服务完成
     * @param ids 字典项ID集合
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 恢复操作结果
     */
    @RequiresPermissions("reportSystem:bizDict:remove")
    @Log(title = "业务参数设置-字典项恢复", businessType = BusinessType.UPDATE)
    @PutMapping("/item/restore/{ids}")
    public AjaxResult restoreItem(@PathVariable Long[] ids)
    {
        return reportRestService.exchange(ITEM_PATH + "/restore/" + joinIds(ids), HttpMethod.PUT, null, Map.of(),
                AjaxResult.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:38
     * 说明: 将路径变量ID数组转换为逗号分隔字符串，保持若依批量删除接口路径风格
     * @param ids ID数组，可包含一个或多个ID
     * @return
     * 返回值: String
     * 返回值说明: 逗号分隔ID字符串
     */
    private String joinIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Long id : ids)
        {
            if (id == null)
            {
                continue;
            }
            if (!builder.isEmpty())
            {
                builder.append(",");
            }
            builder.append(id);
        }
        return builder.toString();
    }
}
