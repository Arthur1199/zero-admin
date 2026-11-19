package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLogGlue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 维护 xxl_job_logglue 中的 GLUE 脚本历史版本。
 */
@Mapper
public interface XxlJobLogGlueMapper {
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 保存任务 GLUE 脚本的历史版本。
	 * @param xxlJobLogGlue 待保存的脚本版本
	 * @return
	 * 返回值: int
	 * 返回值说明: 插入影响行数
	 */
	public int save(XxlJobLogGlue xxlJobLogGlue);
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 查询指定任务的 GLUE 版本历史。
	 * @param jobId 任务主键
	 * @return
	 * 返回值: List<XxlJobLogGlue>
	 * 返回值说明: 该任务的脚本历史列表
	 */
	public List<XxlJobLogGlue> findByJobId(@Param("jobId") int jobId);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/18 22:08
	 * 说明: 在持有且已授权任务行锁的事务中查询最近一次 GLUE 保存的标识。
	 * @param jobId 调用方已校验权限的任务 ID
	 * @return
	 * 返回值: Integer
	 * 返回值说明: 最新历史主键，从未保存过时为空
	 */
	Integer findLatestVersionId(@Param("jobId") int jobId);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 清理指定任务较旧的 GLUE 版本，保留主键最大的指定数量。
	 * @param jobId 任务主键
	 * @param limit 要保留的最近版本数
	 * @return
	 * 返回值: int
	 * 返回值说明: 删除行数
	 */
	public int removeOld(@Param("jobId") int jobId, @Param("limit") int limit);

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 删除指定任务的全部 GLUE 历史版本。
	 * @param jobId 任务主键
	 * @return
	 * 返回值: int
	 * 返回值说明: 删除行数
	 */
	public int deleteByJobId(@Param("jobId") int jobId);
	
}
