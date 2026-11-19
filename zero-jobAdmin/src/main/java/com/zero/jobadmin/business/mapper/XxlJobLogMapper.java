package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.model.XxlJobLogReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 管理 xxl_job_log 中的调度触发、执行回调、失败状态和清理记录。
 */
@Mapper
public interface XxlJobLogMapper {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查询指定执行器组的调度日志，按日志 ID 倒序分页。
	 * @param offset 跳过的记录数
	 * @param pagesize 每页条数
	 * @param jobGroup 已校验权限的执行器组 ID
	 * @param jobId 任务 ID，0 表示组内全部任务
	 * @param triggerTimeStart 起始时刻，含该时刻，可为空
	 * @param triggerTimeEndExclusive 结束边界，不含该时刻，可为空
	 * @param logStatus 日志状态筛选值
	 * @return
	 * 返回值: List<XxlJobLog>
	 * 返回值说明: 指定页的调度日志记录
	 */
	public List<XxlJobLog> pageList(@Param("offset") int offset,
									@Param("pagesize") int pagesize,
									@Param("jobGroup") int jobGroup,
									@Param("jobId") int jobId,
									@Param("triggerTimeStart") Date triggerTimeStart,
									@Param("triggerTimeEndExclusive") Date triggerTimeEndExclusive,
									@Param("logStatus") int logStatus);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 统计与分页列表匹配的总记录数。
	 * @param offset 调用方的分页偏移量，计数时不使用
	 * @param pagesize 调用方的分页条数，计数时不使用
	 * @param jobGroup 已校验权限的执行器组 ID
	 * @param jobId 任务 ID，0 表示组内全部任务
	 * @param triggerTimeStart 起始时刻，含该时刻，可为空
	 * @param triggerTimeEndExclusive 结束边界，不含该时刻，可为空
	 * @param logStatus 日志状态筛选值
	 * @return
	 * 返回值: int
	 * 返回值说明: 对应执行器组、任务和时间范围内的日志总数
	 */
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("jobGroup") int jobGroup,
							 @Param("jobId") int jobId,
							 @Param("triggerTimeStart") Date triggerTimeStart,
							 @Param("triggerTimeEndExclusive") Date triggerTimeEndExclusive,
							 @Param("logStatus") int logStatus);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键读取一次调度的触发和处理结果。
	 * @param id 调度日志主键
	 * @return
	 * 返回值: XxlJobLog
	 * 返回值说明: 调度日志，未找到时返回 null
	 */
	public XxlJobLog load(@Param("id") long id);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 插入调度日志并在原对象上回填日志主键。
	 * @param xxlJobLog 待写入的调度记录
	 * @return
	 * 返回值: long
	 * 返回值说明: 插入影响行数
	 */
	public long save(XxlJobLog xxlJobLog);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:58
	 * 说明: 按日志 ID 保存派发结果，并保留派发返回前写入的重试备注。
	 * @param xxlJobLog 调度日志，包含执行器地址、调度状态及备注
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的日志行数
	 */
	public int updateTriggerInfo(XxlJobLog xxlJobLog);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:13
	 * 说明: 为已认领的失败日志补充重试说明，历史备注空间不足时保留原内容。
	 * @param id 调度日志 ID
	 * @param suffix 要追加的重试备注
	 * @return
	 * 返回值: int
	 * 返回值说明: 更新的日志条数
	 */
	int appendRetryMessage(@Param("id") long id, @Param("suffix") String suffix);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:08
	 * 说明: 仅允许还未结束的调度记录写入执行结果。
	 * @param xxlJobLog 日志 ID、完成时间、结果和备注
	 * @return
	 * 返回值: int
	 * 返回值说明: 当前调用成功写入终态时返回 1
	 */
	int updateHandleInfo(XxlJobLog xxlJobLog);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:17
	 * 说明: 原子写入人工终止结果并清除重试次数，避免失败扫描重新派发。
	 * @param log 执行器组、日志 ID、终止结果及备注
	 * @return
	 * 返回值: int
	 * 返回值说明: 仅仍在运行的日志成功写入时返回 1
	 */
	int markManuallyTerminated(XxlJobLog log);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:08
	 * 说明: 补充已成功完成任务的子任务提交备注。
	 * @param id 调度日志 ID
	 * @param handleMsg 已截断至存储限制的完整备注
	 * @return
	 * 返回值: int
	 * 返回值说明: 更新的日志条数
	 */
	int updateHandleMessage(@Param("id") long id, @Param("handleMsg") String handleMsg);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 删除指定任务的全部调度日志。
	 * @param jobId 任务主键
	 * @return
	 * 返回值: int
	 * 返回值说明: 删除行数
	 */
	public int delete(@Param("jobId") int jobId);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按调度时间统计运行中、成功及失败记录，沿用历史报表口径。
	 * @param from 开始时间，包含该时刻
	 * @param to 结束时间，不包含该时刻
	 * @return
	 * 返回值: XxlJobLogReport
	 * 返回值说明: 三类汇总计数，空区间均为零
	 */
	XxlJobLogReport findLogReport(@Param("from") Date from, @Param("to") Date to);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 21:25
	 * 说明: 冻结本轮清理的最大日志 ID。
	 * @param jobGroup 执行器组 ID，0 表示全部
	 * @param jobId 任务 ID，0 表示全部
	 * @return
	 * 返回值: Long
	 * 返回值说明: 清理范围内的最大日志 ID，无记录时为空
	 */
	Long findMaxClearLogId(@Param("jobGroup") int jobGroup, @Param("jobId") int jobId);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按日志 ID 升序读取本轮仍需清理的一批记录。
	 * @param jobGroup 执行器组 ID，0 表示全部
	 * @param jobId 任务 ID，0 表示全部
	 * @param clearBeforeTime 清理时间上限，可为空
	 * @param clearBeforeNum 保留最新的记录数，0 表示不限数量
	 * @param pagesize 单批记录数上限
	 * @param afterId 已处理的最后一个日志 ID，不包含该 ID
	 * @param maxId 本轮清理上限，包含该 ID
	 * @return
	 * 返回值: List<Long>
	 * 返回值说明: 需要清理的日志 ID，按升序返回
	 */
	public List<Long> findClearLogIds(@Param("jobGroup") int jobGroup,
									  @Param("jobId") int jobId,
									  @Param("clearBeforeTime") Date clearBeforeTime,
									  @Param("clearBeforeNum") int clearBeforeNum,
									  @Param("pagesize") int pagesize,
									  @Param("afterId") long afterId,
									  @Param("maxId") long maxId);
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按已筛选出的主键分批删除调度日志。
	 * @param logIds 非空的日志主键批次
	 * @return
	 * 返回值: int
	 * 返回值说明: 本批次删除行数
	 */
	public int clearLog(@Param("logIds") List<Long> logIds);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键升序挑选尚未认领且符合重试或告警处理条件的失败日志。
	 * @param pagesize 本次扫描的最大数量
	 * @return
	 * 返回值: List<Long>
	 * 返回值说明: 供失败监控线程处理的日志主键列表
	 */
	public List<Long> findFailJobLogIds(@Param("pagesize") int pagesize);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:34
	 * 说明: 按预期状态变更告警处理进度，认领时再次确认日志仍符合失败处理条件。
	 * @param logId 失败监控扫描到的日志 ID
	 * @param oldAlarmStatus 状态更新前的预期值，0 表示待处理
	 * @param newAlarmStatus 目标状态，-1 表示已认领
	 * @return
	 * 返回值: int
	 * 返回值说明: 更新成功返回 1，日志状态已变化或已删除时返回 0
	 */
	public int updateAlarmStatus(@Param("logId") long logId,
								 @Param("oldAlarmStatus") int oldAlarmStatus,
								 @Param("newAlarmStatus") int newAlarmStatus);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:16
	 * 说明: 查找自动注册组内长时间未完成、且对应执行器心跳已失效的调度日志。
	 * @param triggerBefore 开始检查的最晚调度时间，含该时刻
	 * @param registryGroup 执行器注册类别
	 * @param heartbeatSince 心跳仍有效的最早时间，含该时刻
	 * @param pageSize 每轮处理上限
	 * @return
	 * 返回值: List<Long>
	 * 返回值说明: 候选日志 ID，按调度时间和 ID 排序
	 */
	List<Long> findLostJobIds(@Param("triggerBefore") Date triggerBefore, @Param("registryGroup") String registryGroup,
							  @Param("heartbeatSince") Date heartbeatSince, @Param("pageSize") int pageSize);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 20:16
	 * 说明: 标记离线任务失败，并在写入时复查状态、组模式和最新心跳。
	 * @param log 日志 ID 和离线失败结果
	 * @param triggerBefore 开始检查的最晚调度时间，含该时刻
	 * @param registryGroup 执行器注册类别
	 * @param heartbeatSince 心跳仍有效的最早时间，含该时刻
	 * @return
	 * 返回值: int
	 * 返回值说明: 仍满足离线条件且成功写入时为 1
	 */
	int markLostJob(@Param("log") XxlJobLog log, @Param("triggerBefore") Date triggerBefore,
					@Param("registryGroup") String registryGroup, @Param("heartbeatSince") Date heartbeatSince);

}
