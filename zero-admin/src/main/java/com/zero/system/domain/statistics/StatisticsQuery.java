package com.zero.system.domain.statistics;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 运行统计查询参数，仅接收日期、接口关键字及受限分页排序。
 */
@Getter
@Setter
public class StatisticsQuery
{
    /** 起始日期，yyyy-MM-dd，默认北京时间今日。 */
    private String beginTime;
    /** 结束日期，yyyy-MM-dd，包含当天，默认与起始日期一致。 */
    private String endTime;
    /** 接口模板或名称关键字，最多100字符。 */
    private String keyword;
    /** 页码，默认1。 */
    private Integer pageNum=1;
    /** 每页条数，默认20，最大100。 */
    private Integer pageSize=20;
    /** 排行类型：calls、slow、failed。 */
    private String ranking="calls";
    /** 慢接口排行最低调用次数，默认10。 */
    private Integer minCalls=10;
}
