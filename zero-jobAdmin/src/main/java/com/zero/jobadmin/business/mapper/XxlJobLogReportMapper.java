package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLogReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * job log
 * @author xuxueli 2019-11-22
 */
@Mapper
public interface XxlJobLogReportMapper {

	/*public int save(XxlJobLogReport xxlJobLogReport);

	public int update(XxlJobLogReport xxlJobLogReport);*/

	/**
	 *
	 * 作者: xuxueli
	 * 时间: 2019/11/22
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

	public List<XxlJobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
												@Param("triggerDayTo") Date triggerDayTo);

	public XxlJobLogReport queryLogReportTotal();

}
