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
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.utils.poi.ExcelUtil;
import com.zero.framework.web.service.SysPasswordService;
import com.zero.system.domain.SysLogininfor;
import com.zero.system.service.ISysLogininforService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 系统访问记录
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController
{
    /**
     * 登录日志服务字段
     */
    @Autowired
    private ISysLogininforService logininforService;
    /**
     * 密码服务字段
     */

    @Autowired
    private SysPasswordService passwordService;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 查询
     * @param logininfor 登录日志参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：查询
     */

    @RequiresPermissions("monitor:logininfor:list")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor)
    {
        startPage();
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        return getDataTable(list);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 导出
     * @param response 响应参数
     * @param logininfor 登录日志参数
     * @return
     * 返回值: void
     * 返回值说明：导出
     */

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("monitor:logininfor:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLogininfor logininfor)
    {
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登录日志");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除
     * @param infoIds 信息ID集合参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：删除
     */

    @RequiresPermissions("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
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

    @RequiresPermissions("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        logininforService.cleanLogininfor();
        return success();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: unlock
     * @param userName 用户名称参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：unlock
     */

    @RequiresPermissions("monitor:logininfor:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName)
    {
        passwordService.clearLoginRecordCache(userName);
        return success();
    }
}
