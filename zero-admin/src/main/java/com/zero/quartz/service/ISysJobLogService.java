package com.zero.quartz.service;

import java.util.List;
import com.zero.quartz.domain.SysJobLog;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 定时任务调度日志信息信息 服务层
 */
public interface ISysJobLogService
{
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
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

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
    public SysJobLog selectJobLogById(Long jobLogId);

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
    public void addJobLog(SysJobLog jobLog);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 批量删除调度日志信息
     * @param logIds 需要删除的日志ID
     * @return
     * 返回值: 结果
     * 返回值说明：批量删除调度日志信息
     */
    public int deleteJobLogByIds(Long[] logIds);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除任务日志
     * @param jobId 调度日志ID
     * @return
     * 返回值: 结果
     * 返回值说明：删除任务日志
     */
    public int deleteJobLogById(Long jobId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空任务日志
     * @return
     * 返回值: void
     * 返回值说明：清空任务日志
     */
    public void cleanJobLog();
}
