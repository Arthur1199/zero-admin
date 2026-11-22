package com.zero.common.enums;

import lombok.Getter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/22 19:10
 * 说明: 后台接口模块分类，作为运行统计模块归属、明细筛选与下拉展示的唯一来源
 */
@Getter
public enum SystemModuleEnum
{
    /**
     * 文件模块，含文件管理及通用上传下载
     */
    FILE("file", "文件"),

    /**
     * 系统模块，含参数、字典、菜单、角色、部门、岗位、公告、IP白名单、接口验证配置及对外接口
     */
    SYSTEM("system", "系统"),

    /**
     * 用户模块，含用户管理、个人中心及谷歌动态码绑定管理
     */
    USER("user", "用户"),

    /**
     * 报表模块，含报表业务代理及报表服务探活
     */
    REPORT("report", "报表"),

    /**
     * 监控模块，含在线用户、操作日志、登录日志、缓存、服务器、微服务及运行统计自身
     */
    MONITOR("monitor", "监控"),

    /**
     * 工具模块，含备忘录、系统通知及接口文档演示
     */
    TOOLS("tools", "工具"),

    /**
     * 认证模块，含登录、退出、验证码及登录页谷歌动态码绑定
     */
    AUTH("auth", "认证"),

    /**
     * 未识别模块，未进入MVC或未声明注解时的兜底归类
     */
    OTHER("other", "其他");

    /**
     * 入库及接口返回的模块编码，与常量名解耦，避免常量改名影响已入库数据
     * -- GETTER --
     *  作者: Arthur 亚瑟
     * 时间: 2026/11/22 19:10
     * 说明: 获取模块编码
     *
     * @return
     * 返回值: String
     * 返回值说明: 入库及接口返回使用的英文编码

     */
    private final String code;

    /**
     * 模块中文名称，用于明细筛选下拉和页面展示
     * -- GETTER --
     *  作者: Arthur 亚瑟
     * 时间: 2026/11/22 19:10
     * 说明: 获取模块中文名称
     *
     * @return
     * 返回值: String
     * 返回值说明: 明细筛选下拉和表格展示使用的中文名称

     */
    private final String label;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/22 19:10
     * 说明: 绑定模块编码和中文名称
     * @param code 模块编码，入库及接口返回使用
     * @param label 模块中文名称，页面展示使用
     */
    SystemModuleEnum(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

}
