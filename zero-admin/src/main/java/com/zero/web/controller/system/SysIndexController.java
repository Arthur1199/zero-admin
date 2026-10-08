package com.zero.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.config.ZeroConfig;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 首页
 */
@RestController
public class SysIndexController
{
    /**
     * 系统基础配置
     */
    @Autowired
    private ZeroConfig zeroConfig;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 访问首页，提示语
     * @return
     * 返回值: String
     * 返回值说明：访问首页，提示语
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", zeroConfig.getName(), zeroConfig.getVersion());
    }
}
