package com.zero.web.controller.monitor;

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
import com.zero.common.annotation.SystemModule;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.common.utils.poi.ExcelUtil;
import com.zero.system.domain.SysOperLog;
import com.zero.system.service.ISysOperLogService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 操作日志记录
 */
@RestController
@RequestMapping("/monitor/operlog")
@SystemModule(SystemModuleEnum.MONITOR)
public class SysOperlogController extends BaseController
{
    /**
     * 操作日志服务字段
     */
    @Autowired
    private ISysOperLogService operLogService;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询
     * @param operLog 操作日志参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：查询
     */

    @RequiresPermissions("monitor:operlog:list")
    @GetMapping("/list")
    public TableDataInfo list(SysOperLog operLog)
    {
        startPage();
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        return getDataTable(list);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 导出
     * @param response 响应参数
     * @param operLog 操作日志参数
     * @return
     * 返回值: void
     * 返回值说明：导出
     */

    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("monitor:operlog:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysOperLog operLog)
    {
        List<SysOperLog> list = operLogService.selectOperLogList(operLog);
        ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
        util.exportExcel(response, list, "操作日志");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除
     * @param operIds 操作ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：删除
     */

    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @RequiresPermissions("monitor:operlog:remove")
    @DeleteMapping("/{operIds}")
    public AjaxResult remove(@PathVariable Long[] operIds)
    {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: clean
     * @return
     * 返回值: AjaxResult
     * 返回值说明：clean
     */

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @RequiresPermissions("monitor:operlog:remove")
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        operLogService.cleanOperLog();
        return success();
    }
}
