package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLogGlue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * job log for glue
 * @author xuxueli 2016-5-19 18:04:56
 */
@Mapper
public interface XxlJobLogGlueMapper {
	
	public int save(XxlJobLogGlue xxlJobLogGlue);
	
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

	public int removeOld(@Param("jobId") int jobId, @Param("limit") int limit);

	public int deleteByJobId(@Param("jobId") int jobId);
	
}
