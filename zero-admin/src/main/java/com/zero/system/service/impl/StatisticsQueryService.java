package com.zero.system.service.impl;

import com.zero.common.exception.ServiceException;
import com.zero.common.core.page.TableDataInfo;
import com.zero.framework.statistics.*;
import com.zero.system.domain.statistics.StatisticsQuery;
import com.zero.system.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 运行统计只读查询，限制时间跨度和分页，结果按统一data语义返回。
 */
@Service
public class StatisticsQueryService
{
    /** 统计数据访问。 */
    private final StatisticsMapper mapper;
    /** 当前实例管道状态。 */
    private final StatisticsPipeline pipeline;
    /** 半开日期区间的安全下限，与MySQL DATE支持范围一致。 */
    private static final LocalDate MIN_DATE = LocalDate.of(1000, 1, 1);
    /** 保证结束日期加一天后仍能作为合法MySQL DATETIME查询边界。 */
    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 30);
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 注入查询依赖。
     * @param mapper 统计Mapper
     * @param pipeline 采集状态
     */
    public StatisticsQueryService(StatisticsMapper mapper,StatisticsPipeline pipeline) { this.mapper=mapper; this.pipeline=pipeline; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 校验时间、分页及排序白名单，不接受任意SQL排序字段。
     * @param input 页面查询条件
     * @return SQL查询参数
     * 返回值: Map<String,Object>
     * 返回值说明: SQL查询参数
     */
    public Map<String,Object> parameters(StatisticsQuery input)
    {
        if (input == null) input = new StatisticsQuery();
        try
        {
            LocalDate begin=input.getBeginTime()==null || input.getBeginTime().isBlank()?LocalDate.now(ZoneId.of("Asia/Shanghai")):LocalDate.parse(input.getBeginTime().trim());
            LocalDate end=input.getEndTime()==null || input.getEndTime().isBlank()?begin:LocalDate.parse(input.getEndTime().trim());
            if (begin.isBefore(MIN_DATE) || end.isAfter(MAX_DATE)) throw new ServiceException("统计日期超出数据库支持范围");
            long days=ChronoUnit.DAYS.between(begin,end)+1;
            if (days<1 || days>366) throw new ServiceException("统计日期范围必须为1至366天");
            if (input.getKeyword()!=null && input.getKeyword().length()>100) throw new ServiceException("接口关键字不能超过100字符");
            if (!Set.of("calls","slow","failed").contains(input.getRanking()==null?"calls":input.getRanking())) throw new ServiceException("不支持的排行类型");
            int size=input.getPageSize()==null?20:Math.max(1,Math.min(100,input.getPageSize()));
            int page=input.getPageNum()==null?1:Math.max(1,Math.min(100000,input.getPageNum()));
            Map<String,Object> query=new HashMap<>();
            query.put("beginDay",begin); query.put("endDay",end.plusDays(1));
            query.put("beginAt",begin.atStartOfDay()); query.put("endAt",end.plusDays(1).atStartOfDay());
            query.put("minute",days<=7); query.put("keyword",input.getKeyword()==null?null:input.getKeyword().trim());
            query.put("ranking",input.getRanking()); query.put("minCalls",input.getMinCalls()==null?10:Math.max(1,input.getMinCalls()));
            query.put("limit",size); query.put("offset",(page-1)*size); return query;
        }
        catch (java.time.format.DateTimeParseException e) { throw new ServiceException("日期格式必须为yyyy-MM-dd"); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询今日概览及持久化、采样和实例级故障状态。
     * @return 今日指标及采集状态
     * 返回值: Map<String,Object>
     * 返回值说明: 今日指标及采集状态
     */
    public Map<String,Object> overview()
    {
        Map<String,Object> result=new HashMap<>();
        result.put("status",pipeline.status());
        try { result.put("metrics",mapper.overview(parameters(new StatisticsQuery()))); result.put("health",mapper.health()); result.put("databaseAvailable",true); }
        catch (Exception e) { result.put("databaseAvailable",false); }
        result.put("serverTime",System.currentTimeMillis()); return result;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询API、在线分钟或日趋势及登录日趋势。
     * @param query 日期范围
     * @return 带kind的趋势数据，在线缺口保持缺失
     * 返回值: List<Map<String,Object>>
     * 返回值说明: 带kind的趋势数据，在线缺口保持缺失
     */
    public List<Map<String,Object>> trend(StatisticsQuery query) { return mapper.trend(parameters(query)); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询模块调用及失败类别分布。
     * @param query 日期范围
     * @return 模块分布及失败分类计数
     * 返回值: List<Map<String,Object>>
     * 返回值说明: 模块分布及失败分类计数
     */
    public List<Map<String,Object>> distribution(StatisticsQuery query) { return mapper.distribution(parameters(query)); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 查询固定十条排行，不提供动态SQL排序。
     * @param query 日期及排行条件
     * @return 接口TOP10
     * 返回值: List<Map<String,Object>>
     * 返回值说明: 接口TOP10
     */
    public List<Map<String,Object>> ranking(StatisticsQuery query)
    {
        Map<String,Object> params=parameters(query); params.put("limit",10); params.put("offset",0); return mapper.list(params);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 分页查询接口汇总，独立计数避免PageHelper影响其他统计查询。
     * @param query 页面日期和分页条件
     * @return code、msg、rows和total
     * 返回值: TableDataInfo
     * 返回值说明: code、msg、rows和total
     */
    public TableDataInfo list(StatisticsQuery query)
    {
        Map<String,Object> params=parameters(query); params.put("ranking","calls");
        TableDataInfo result=new TableDataInfo(mapper.list(params),mapper.countEndpoints(params)); result.setCode(200); result.setMsg("查询成功"); return result;
    }
}
