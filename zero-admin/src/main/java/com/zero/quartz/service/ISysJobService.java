package com.zero.quartz.service;

import java.util.List;
import org.quartz.SchedulerException;
import com.zero.common.exception.job.TaskException;
import com.zero.quartz.domain.SysJob;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 定时任务调度信息信息 服务层
 */
public interface ISysJobService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取quartz调度器的计划任务
     * @param job 调度信息
     * @return
     * 返回值: 调度任务集合
     * 返回值说明：获取quartz调度器的计划任务
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 通过调度任务ID查询调度信息
     * @param jobId 调度任务ID
     * @return
     * 返回值: 调度任务对象信息
     * 返回值说明：通过调度任务ID查询调度信息
     */
    public SysJob selectJobById(Long jobId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 暂停任务
     * @param job 调度信息
     * @return
     * 返回值: int
     * 返回值说明：暂停任务
     */
    public int pauseJob(SysJob job) throws SchedulerException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 恢复任务
     * @param job 调度信息
     * @return
     * 返回值: int
     * 返回值说明：恢复任务
     */
    public int resumeJob(SysJob job) throws SchedulerException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 删除任务后，所对应的trigger也将被删除
     * @param job 调度信息
     * @return
     * 返回值: int
     * 返回值说明：删除任务后，所对应的trigger也将被删除
     */
    public int deleteJob(SysJob job) throws SchedulerException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 逐个删除后台 Quartz 调度任务。
     * @param jobIds 待删除的调度任务主键
     * @return
     * 返回值: void
     * 返回值说明: 移除任务记录及对应的调度项
     */
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 任务调度状态修改
     * @param job 调度信息
     * @return
     * 返回值: int
     * 返回值说明：任务调度状态修改
     */
    public int changeStatus(SysJob job) throws SchedulerException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 立即运行任务
     * @param job 调度信息
     * @return
     * 返回值: boolean
     * 返回值说明：立即运行任务
     */
    public boolean run(SysJob job) throws SchedulerException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 新增任务
     * @param job 调度信息
     * @return
     * 返回值: int
     * 返回值说明：新增任务
     */
    public int insertJob(SysJob job) throws SchedulerException, TaskException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 更新任务
     * @param job 调度信息
     * @return
     * 返回值: int
     * 返回值说明：更新任务
     */
    public int updateJob(SysJob job) throws SchedulerException, TaskException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验cron表达式是否有效
     * @param cronExpression 表达式
     * @return
     * 返回值: boolean
     * 返回值说明：校验cron表达式是否有效
     */
    public boolean checkCronExpressionIsValid(String cronExpression);
}
