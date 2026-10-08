package com.zero.quartz.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.zero.common.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.annotation.Log;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.utils.poi.ExcelUtil;
import com.zero.quartz.domain.SysJobLog;
import com.zero.quartz.service.ISysJobLogService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 调度日志操作处理
 */
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController
{
    /**
     * 定时任务日志服务字段
     */
    @Autowired
    private ISysJobLogService jobLogService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询定时任务调度日志列表
     * @param sysJobLog 系统定时任务日志参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：查询定时任务调度日志列表
     */
    @RequiresPermissions("monitor:job:list")
    @GetMapping("/list")
    public TableDataInfo list(SysJobLog sysJobLog)
    {
        startPage();
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        return getDataTable(list);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 导出定时任务调度日志列表
     * @param response 响应参数
     * @param sysJobLog 系统定时任务日志参数
     * @return
     * 返回值: void
     * 返回值说明：导出定时任务调度日志列表
     */
    @RequiresPermissions("monitor:job:export")
    @Log(title = "任务调度日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysJobLog sysJobLog)
    {
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        ExcelUtil<SysJobLog> util = new ExcelUtil<SysJobLog>(SysJobLog.class);
        util.exportExcel(response, list, "调度日志");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 根据调度编号获取详细信息
     * @param jobLogId 定时任务日志ID参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：根据调度编号获取详细信息
     */
    @RequiresPermissions("monitor:job:query")
    @GetMapping(value = "/{jobLogId}")
    public AjaxResult getInfo(@PathVariable Long jobLogId)
    {
        return success(jobLogService.selectJobLogById(jobLogId));
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除定时任务调度日志
     * @param jobLogIds 定时任务日志ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：删除定时任务调度日志
     */
    @RequiresPermissions("monitor:job:remove")
    @Log(title = "定时任务调度日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobLogIds}")
    public AjaxResult remove(@PathVariable Long[] jobLogIds)
    {
        return toAjax(jobLogService.deleteJobLogByIds(jobLogIds));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空定时任务调度日志
     * @return
     * 返回值: AjaxResult
     * 返回值说明：清空定时任务调度日志
     */
    @RequiresPermissions("monitor:job:remove")
    @Log(title = "调度日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        jobLogService.cleanJobLog();
        return success();
    }
}
