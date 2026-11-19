package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLogReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 查询并写入 xxl_job_log_report 中的每日调度统计。
 */
@Mapper
public interface XxlJobLogReportMapper {

	/*public int save(XxlJobLogReport xxlJobLogReport);

	public int update(XxlJobLogReport xxlJobLogReport);*/

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 保存按日汇总，写入时保留统计开始时间更新的快照。
	 * @param xxlJobLogReport 某天的汇总计数和本次统计开始时间
	 * @return
	 * 返回值: int
	 * 返回值说明: 数据库返回的影响行数，旧快照被跳过时可能为零
	 */
	public int saveOrUpdate(XxlJobLogReport xxlJobLogReport);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 23:07
	 * 说明: 按日期分批找出最近三天之外仍含进行中任务的汇总。
	 * @param retentionStart 日志保留范围起点，未启用保留策略时为空
	 * @param recentStart 常规刷新窗口起点，不含该日
	 * @param afterDay 分批读取的日期下界，首次查询时为空
	 * @param limit 本轮最多返回的日期数
	 * @return
	 * 返回值: List<Date>
	 * 返回值说明: 可重新汇总的历史日期，按时间升序排列
	 */
	List<Date> findPendingReportDays(@Param("retentionStart") Date retentionStart,
									@Param("recentStart") Date recentStart,
									@Param("afterDay") Date afterDay,
									@Param("limit") int limit);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查询指定日期区间内的逐日调度汇总，区间包含两端。
	 * @param triggerDayFrom 起始统计日期
	 * @param triggerDayTo 结束统计日期
	 * @return
	 * 返回值: List<XxlJobLogReport>
	 * 返回值说明: 按统计日期升序的汇总记录
	 */
	public List<XxlJobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
												@Param("triggerDayTo") Date triggerDayTo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 对逐日报表的运行中、成功和失败数分别求和。
	 * @return
	 * 返回值: XxlJobLogReport
	 * 返回值说明: 调度概览使用的合计数据
	 */
	public XxlJobLogReport queryLogReportTotal();

}
