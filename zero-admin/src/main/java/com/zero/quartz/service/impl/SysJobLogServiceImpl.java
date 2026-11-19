package com.zero.quartz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zero.quartz.domain.SysJobLog;
import com.zero.quartz.mapper.SysJobLogMapper;
import com.zero.quartz.service.ISysJobLogService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 定时任务调度日志信息 服务层
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService
{
    /**
     * 定时任务日志数据访问字段
     */
    @Autowired
    private SysJobLogMapper jobLogMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取quartz调度器日志的计划任务
     * @param jobLog 调度日志信息
     * @return
     * 返回值: 调度任务日志集合
     * 返回值说明：获取quartz调度器日志的计划任务
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog)
    {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过调度任务日志ID查询调度信息
     * @param jobLogId 调度任务日志ID
     * @return
     * 返回值: 调度任务日志对象信息
     * 返回值说明：通过调度任务日志ID查询调度信息
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId)
    {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 新增任务日志
     * @param jobLog 调度日志信息
     * @return
     * 返回值: void
     * 返回值说明：新增任务日志
     */
    @Override
    public void addJobLog(SysJobLog jobLog)
    {
        jobLogMapper.insertJobLog(jobLog);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 批量删除调度日志信息
     * @param logIds 需要删除的数据ID
     * @return
     * 返回值: int
     * 返回值说明：批量删除调度日志信息
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds)
    {
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除任务日志
     * @param jobId 调度日志ID
     * @return
     * 返回值: int
     * 返回值说明：删除任务日志
     */
    @Override
    public int deleteJobLogById(Long jobId)
    {
        return jobLogMapper.deleteJobLogById(jobId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空任务日志
     * @return
     * 返回值: void
     * 返回值说明：清空任务日志
     */
    @Override
    public void cleanJobLog()
    {
        jobLogMapper.cleanJobLog();
    }
}
