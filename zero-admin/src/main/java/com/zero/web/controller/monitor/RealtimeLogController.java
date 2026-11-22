package com.zero.web.controller.monitor;

import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.framework.realtimelog.RealtimeLogBuffer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 22:34
 * 说明: 当前管理后台进程实时输出只读入口，完整模式可包含未脱敏控制台原文，来源仅由服务配置指定，不允许HTTP指定文件。
 */
@RestController
@RequestMapping("/monitor/realtimeLog")
@SystemModule(SystemModuleEnum.MONITOR)
public class RealtimeLogController extends BaseController
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 登录且具备日志菜单权限才能读取，通过实际Appender兼容DevTools；按配置读取原始控制台文件或应用日志缓冲，响应禁止缓存。
     * @param processId 上次返回的进程标识，首次为空
     * @param after 上次返回的游标，首次为空
     * @param limit 每批事件或原文块数，默认500、最大500
     * @param response 设置禁止缓存响应头
     * @return 日志及续读状态
     * 返回值: AjaxResult
     * 返回值说明: data包含来源模式、日志或未脱敏控制台原文、游标和重置/缺口提示；原始模式的游标是字节偏移
     */
    @RequiresPermissions(RealtimeLogBuffer.PERMISSION)
    @GetMapping("/events")
    public AjaxResult events(@RequestParam(required = false) String processId,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "500") int limit, HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store, private");
        response.setHeader("Pragma", "no-cache");
        return success(RealtimeLogBuffer.readActive(processId, after, limit));
    }
}
