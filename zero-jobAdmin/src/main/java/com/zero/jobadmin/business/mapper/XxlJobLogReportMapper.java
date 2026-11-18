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

	public List<XxlJobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
												@Param("triggerDayTo") Date triggerDayTo);

	public XxlJobLogReport queryLogReportTotal();

}
