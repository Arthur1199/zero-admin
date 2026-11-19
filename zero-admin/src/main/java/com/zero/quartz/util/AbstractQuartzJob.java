package com.zero.quartz.util;

import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import com.zero.common.constant.Constants;
import com.zero.common.constant.ScheduleConstants;
import com.zero.common.utils.ExceptionUtil;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.bean.BeanUtils;
import com.zero.common.utils.spring.SpringUtils;
import com.zero.quartz.domain.SysJob;
import com.zero.quartz.domain.SysJobLog;
import com.zero.quartz.service.ISysJobLogService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 抽象quartz调用
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job
{
    /**
     * log字段
     */
    /**
     * 线程本地变量
     */
    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 执行
     * @param context context参数
     * @return
     * 返回值: void
     * 返回值说明：执行
     */

    @Override
    public void execute(JobExecutionContext context)
    {
        SysJob sysJob = new SysJob();
        BeanUtils.copyBeanProp(sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));
        try
        {
            before(context, sysJob);
            if (sysJob != null)
            {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        }
        catch (Exception e)
        {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 执行前
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @return
     * 返回值: void
     * 返回值说明：执行前
     */
    protected void before(JobExecutionContext context, SysJob sysJob)
    {
        threadLocal.set(new Date());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 执行后
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @param e e参数
     * @return
     * 返回值: void
     * 返回值说明：执行后
     */
    protected void after(JobExecutionContext context, SysJob sysJob, Exception e)
    {
        Date startTime = threadLocal.get();
        threadLocal.remove();

        final SysJobLog sysJobLog = new SysJobLog();
        sysJobLog.setJobName(sysJob.getJobName());
        sysJobLog.setJobGroup(sysJob.getJobGroup());
        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        sysJobLog.setStartTime(startTime);
        sysJobLog.setStopTime(new Date());
        long runMs = sysJobLog.getStopTime().getTime() - sysJobLog.getStartTime().getTime();
        sysJobLog.setJobMessage(sysJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
        if (e != null)
        {
            sysJobLog.setStatus(Constants.FAIL);
            String errorMsg = StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000);
            sysJobLog.setExceptionInfo(errorMsg);
        }
        else
        {
            sysJobLog.setStatus(Constants.SUCCESS);
        }

        // 写入数据库当中
        SpringUtils.getBean(ISysJobLogService.class).addJobLog(sysJobLog);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 由具体执行器实现同一 Quartz 任务的调用过程。
     * @param context Quartz 当前任务执行上下文
     * @param sysJob 本次调用的后台任务定义
     * @return
     * 返回值: void
     * 返回值说明: 执行已配置的任务方法
     */
    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;
}
