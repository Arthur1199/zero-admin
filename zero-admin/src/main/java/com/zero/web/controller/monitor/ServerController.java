package com.zero.web.controller.monitor;

import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.framework.web.domain.Server;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 服务器监控
 */
@RestController
@RequestMapping("/monitor/server")
@SystemModule(SystemModuleEnum.MONITOR)
public class ServerController
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取信息
     */
    @RequiresPermissions("monitor:server:list")
    @GetMapping()
    public AjaxResult getInfo() throws Exception
    {
        Server server = new Server();
        server.copyTo();
        return AjaxResult.success(server);
    }
}
