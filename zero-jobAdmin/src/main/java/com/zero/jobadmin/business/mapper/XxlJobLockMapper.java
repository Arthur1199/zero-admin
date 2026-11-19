package com.zero.jobadmin.business.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 访问 xxl_job_lock 中的调度行锁，用于串行化调度扫描事务。
 */
@Mapper
public interface XxlJobLockMapper {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在当前事务内锁定调度锁记录，使不同调度中心实例串行推进任务游标。
     * @return
     * 返回值: String
     * 返回值说明: 锁记录名称，初始化记录缺失时返回 null
     */
    String scheduleLock();

}