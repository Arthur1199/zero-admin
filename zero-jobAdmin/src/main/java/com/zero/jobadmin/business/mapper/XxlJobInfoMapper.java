package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.model.dto.JobScheduleUpdate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读写 xxl_job_info 的任务定义、启停状态与调度游标。
 */
@Mapper
public interface XxlJobInfoMapper {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:17
	 * 说明: 分页查询任务管理所需的配置，GLUE 源码由编辑器单独读取。
	 * @param offset 分页偏移
	 * @param pagesize 每页条数
	 * @param jobGroup 由调用方校验的执行器组 ID
	 * @param triggerStatus 调度状态，负数表示不筛选
	 * @param name 可选的任务名称筛选
	 * @param executorHandler 可选的 JobHandler 筛选
	 * @param author 可选的负责人筛选
	 * @return
	 * 返回值: List<XxlJobInfo>
	 * 返回值说明: 按 ID 降序返回的任务记录，GLUE 源码为空
	 */
	public List<XxlJobInfo> pageList(@Param("offset") int offset,
									 @Param("pagesize") int pagesize,
									 @Param("jobGroup") int jobGroup,
									 @Param("triggerStatus") int triggerStatus,
									 @Param("name") String name,
									 @Param("executorHandler") String executorHandler,
									 @Param("author") String author);
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按任务列表的执行器、状态等条件统计总数。
	 * @param offset 分页偏移量，从 0 开始
	 * @param pagesize 每页最多记录数
	 * @param jobGroup 执行器组 ID，小于等于 0 时不限
	 * @param triggerStatus 触发状态，小于 0 时不限
	 * @param name 任务名称筛选
	 * @param executorHandler 执行器处理器筛选
	 * @param author 任务负责人筛选
	 * @return
	 * 返回值: int
	 * 返回值说明: 匹配任务数，统计不分页
	 */
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("jobGroup") int jobGroup,
							 @Param("triggerStatus") int triggerStatus,
							 @Param("name") String name,
							 @Param("executorHandler") String executorHandler,
							 @Param("author") String author);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 新增任务配置并回填数据库生成的 ID。
	 * @param info 待保存的任务配置
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的记录数
	 */
	public int save(XxlJobInfo info);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键读取任务定义及当前调度时间。
	 * @param id 任务主键
	 * @return
	 * 返回值: XxlJobInfo
	 * 返回值说明: 任务记录，未找到时返回 null
	 */
	public XxlJobInfo loadById(@Param("id") int id);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:49
	 * 说明: 在保存 GLUE 的事务内锁定任务，串行处理同一任务的脚本版本更新
	 * @param id 任务 ID
	 * @return
	 * 返回值: XxlJobInfo
	 * 返回值说明: 任务当前配置，记录不存在时为空
	 */
	XxlJobInfo loadByIdForUpdate(@Param("id") int id);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:49
	 * 说明: 仅更新 GLUE 字段和维护时间，保留并发的任务配置和调度状态
	 * @param job 已在同一事务内加锁的任务及新脚本
	 * @return
	 * 返回值: int
	 * 返回值说明: 匹配的任务记录数
	 */
	int updateGlue(XxlJobInfo job);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:49
	 * 说明: 任务所属组、调度配置和原状态均匹配时，更新启停状态及触发时间
	 * @param job 校验权限及计算调度时间时的任务快照
	 * @param status 目标启停状态
	 * @param nextTriggerTime 下次调度时间，停止时为 0
	 * @return
	 * 返回值: int
	 * 返回值说明: 匹配并更新的任务记录数，0 表示任务已变更
	 */
	int updateTriggerState(@Param("job") XxlJobInfo job, @Param("status") int status,
			@Param("nextTriggerTime") long nextTriggerTime);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键更新任务记录，供直接维护任务配置的调用方使用。
	 * @param xxlJobInfo 携带任务主键和待保存的字段
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的记录数
	 */
	public int update(XxlJobInfo xxlJobInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 19:45
	 * 说明: 原组与启停状态未变化时更新任务配置，避免覆盖调度进度和 GLUE 内容
	 * @param job 待保存的任务配置
	 * @param originalGroup 权限校验时的执行器组
	 * @param refreshNextTime 调度配置变化且任务运行时是否更新下次触发时间
	 * @return
	 * 返回值: int
	 * 返回值说明: 成功匹配并更新的任务记录数
	 */
	int updateInGroup(@Param("job") XxlJobInfo job, @Param("originalGroup") int originalGroup,
			@Param("refreshNextTime") boolean refreshNextTime);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按主键物理删除任务定义。
	 * @param id 任务主键
	 * @return
	 * 返回值: int
	 * 返回值说明: 受影响的记录数
	 */
	public int delete(@Param("id") long id);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:17
	 * 说明: 查询调度日志的任务下拉选项。
	 * @param jobGroup 经过调用方权限校验的执行器组 ID
	 * @return
	 * 返回值: List<XxlJobInfo>
	 * 返回值说明: 按任务 ID 升序返回，仅填充任务 ID 和名称
	 */
	public List<XxlJobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 统计任务表总数，供调度控制台概览使用。
	 * @return
	 * 返回值: int
	 * 返回值说明: 任务总数
	 */
	public int findAllCount();

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 预读已启用且下一次触发时间落在扫描上界以内的任务。
	 * @param maxNextTime 扫描窗口上界，单位为 Unix 时间戳毫秒
	 * @param pagesize 本批任务上限
	 * @return
	 * 返回值: List<XxlJobInfo>
	 * 返回值说明: 本批次需要计算调度游标的任务列表
	 */
	public List<XxlJobInfo> scheduleJobQuery(@Param("maxNextTime") long maxNextTime, @Param("pagesize") int pagesize );

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 在任务保持启用时更新触发游标，按传入状态决定是否更新触发状态。
	 * @param xxlJobInfo 包含任务主键、触发游标和状态的内存对象
	 * @return
	 * 返回值: int
	 * 返回值说明: 数据库更新的行数
	 */
	public int scheduleUpdate(XxlJobInfo xxlJobInfo);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 批量推进仍启用且与预读计划一致的调度游标。
	 * @param updates 包含旧计划和新游标的任务批次
	 * @return
	 * 返回值: int
	 * 返回值说明: 匹配并更新的任务数，调用方用它判断是否发生并发修改
	 */
	public int scheduleBatchUpdate(@Param("list") List<JobScheduleUpdate> updates);

}
