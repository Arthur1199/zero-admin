package com.zero.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.zero.common.annotation.RequiresPermissions;
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
import com.zero.common.annotation.SystemModule;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.system.domain.SysNotice;
import com.zero.system.service.ISysNoticeService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 公告 信息操作处理
 */
@RestController
@RequestMapping("/system/notice")
@SystemModule(SystemModuleEnum.SYSTEM)
public class SysNoticeController extends BaseController
{
    /**
     * 通知公告服务字段
     */
    @Autowired
    private ISysNoticeService noticeService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取通知公告列表
     * @param notice 通知公告参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：获取通知公告列表
     */
    @RequiresPermissions("system:notice:list")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice)
    {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据通知公告编号获取详细信息
     * @param noticeId 通知公告ID参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：根据通知公告编号获取详细信息
     */
    @RequiresPermissions("system:notice:query")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId)
    {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增通知公告
     * @param notice 通知公告参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：新增通知公告
     */
    @RequiresPermissions("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice)
    {
        notice.setCreateBy(getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改通知公告
     * @param notice 通知公告参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：修改通知公告
     */
    @RequiresPermissions("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice)
    {
        notice.setUpdateBy(getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除通知公告
     * @param noticeIds 通知公告ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：删除通知公告
     */
    @RequiresPermissions("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
