package com.zero.web.controller.monitor;

import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.system.domain.statistics.StatisticsQuery;
import com.zero.system.service.impl.StatisticsQueryService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 管理后台运行统计只读接口，统一菜单权限，不新增匿名访问。
 */
@RestController
@RequestMapping("/monitor/statistics")
public class StatisticsController extends BaseController
{
    /** 统计查询服务。 */
    private final StatisticsQueryService service;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 注入统计查询。
     * @param service 运行统计查询服务
     */
    public StatisticsController(StatisticsQueryService service) { this.service=service; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询今日指标、当前在线和采集状态，要求登录且具备monitor:statistics:list权限，不返回敏感凭证。
     * @return data承载统计结果
     * 返回值: AjaxResult
     * 返回值说明: data承载统计结果
     */
    @RequiresPermissions("monitor:statistics:list")
    @GetMapping("/overview")
    public AjaxResult overview()
    { return AjaxResult.success(service.overview()); }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询历史趋势，要求登录且具备monitor:statistics:list权限，不返回敏感凭证。
     * @param query 日期、接口或分页条件
     * @return data承载统计结果
     * 返回值: AjaxResult
     * 返回值说明: data承载统计结果
     */
    @RequiresPermissions("monitor:statistics:list")
    @GetMapping("/trend")
    public AjaxResult trend(StatisticsQuery query)
    { return AjaxResult.success(service.trend(query)); }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询模块与失败分类分布，要求登录且具备monitor:statistics:list权限，不返回敏感凭证。
     * @param query 日期、接口或分页条件
     * @return data承载统计结果
     * 返回值: AjaxResult
     * 返回值说明: data承载统计结果
     */
    @RequiresPermissions("monitor:statistics:list")
    @GetMapping("/distribution")
    public AjaxResult distribution(StatisticsQuery query)
    { return AjaxResult.success(service.distribution(query)); }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询接口排行，要求登录且具备monitor:statistics:list权限，不返回敏感凭证。
     * @param query 日期、接口或分页条件
     * @return data承载统计结果
     * 返回值: AjaxResult
     * 返回值说明: data承载统计结果
     */
    @RequiresPermissions("monitor:statistics:list")
    @GetMapping("/ranking")
    public AjaxResult ranking(StatisticsQuery query)
    { return AjaxResult.success(service.ranking(query)); }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 分页查询接口统计，要求登录且具备monitor:statistics:list权限，不返回敏感凭证。
     * @param query 日期、接口或分页条件
     * @return 分页统计明细
     * 返回值: TableDataInfo
     * 返回值说明: 分页统计明细
     */
    @RequiresPermissions("monitor:statistics:list")
    @GetMapping("/list")
    public TableDataInfo list(StatisticsQuery query)
    { return service.list(query); }

}
