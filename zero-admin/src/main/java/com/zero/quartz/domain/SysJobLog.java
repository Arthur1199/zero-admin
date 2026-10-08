package com.zero.quartz.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zero.common.annotation.Excel;
import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 定时任务调度日志表 sys_job_log
 */
public class SysJobLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Excel(name = "日志序号")
    private Long jobLogId;

    /**
     * 任务名称
     */
    @Excel(name = "任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @Excel(name = "任务组名")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @Excel(name = "调用目标字符串")
    private String invokeTarget;

    /**
     * 日志信息
     */
    @Excel(name = "日志信息")
    private String jobMessage;

    /**
     * 执行状态（0正常 1失败）
     */
    @Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")
    private String status;

    /**
     * 异常信息
     */
    @Excel(name = "异常信息")
    private String exceptionInfo;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 停止时间
     */
    private Date stopTime;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取定时任务日志ID
     * @return
     * 返回值: Long
     * 返回值说明：获取定时任务日志ID
     */

    public Long getJobLogId()
    {
        return jobLogId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置定时任务日志ID
     * @param jobLogId 定时任务日志ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置定时任务日志ID
     */

    public void setJobLogId(Long jobLogId)
    {
        this.jobLogId = jobLogId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取定时任务名称
     * @return
     * 返回值: String
     * 返回值说明：获取定时任务名称
     */

    public String getJobName()
    {
        return jobName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置定时任务名称
     * @param jobName 定时任务名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置定时任务名称
     */

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取定时任务Group
     * @return
     * 返回值: String
     * 返回值说明：获取定时任务Group
     */

    public String getJobGroup()
    {
        return jobGroup;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置定时任务Group
     * @param jobGroup 定时任务Group参数
     * @return
     * 返回值: void
     * 返回值说明：设置定时任务Group
     */

    public void setJobGroup(String jobGroup)
    {
        this.jobGroup = jobGroup;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取InvokeTarget
     * @return
     * 返回值: String
     * 返回值说明：获取InvokeTarget
     */

    public String getInvokeTarget()
    {
        return invokeTarget;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置InvokeTarget
     * @param invokeTarget invokeTarget参数
     * @return
     * 返回值: void
     * 返回值说明：设置InvokeTarget
     */

    public void setInvokeTarget(String invokeTarget)
    {
        this.invokeTarget = invokeTarget;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取定时任务消息
     * @return
     * 返回值: String
     * 返回值说明：获取定时任务消息
     */

    public String getJobMessage()
    {
        return jobMessage;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置定时任务消息
     * @param jobMessage 定时任务消息参数
     * @return
     * 返回值: void
     * 返回值说明：设置定时任务消息
     */

    public void setJobMessage(String jobMessage)
    {
        this.jobMessage = jobMessage;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取状态
     * @return
     * 返回值: String
     * 返回值说明：获取状态
     */

    public String getStatus()
    {
        return status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置状态
     * @param status 状态参数
     * @return
     * 返回值: void
     * 返回值说明：设置状态
     */

    public void setStatus(String status)
    {
        this.status = status;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取异常信息
     * @return
     * 返回值: String
     * 返回值说明：获取异常信息
     */

    public String getExceptionInfo()
    {
        return exceptionInfo;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置异常信息
     * @param exceptionInfo 异常信息参数
     * @return
     * 返回值: void
     * 返回值说明：设置异常信息
     */

    public void setExceptionInfo(String exceptionInfo)
    {
        this.exceptionInfo = exceptionInfo;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取开始时间
     * @return
     * 返回值: Date
     * 返回值说明：获取开始时间
     */

    public Date getStartTime()
    {
        return startTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置开始时间
     * @param startTime 开始时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置开始时间
     */

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Stop时间
     * @return
     * 返回值: Date
     * 返回值说明：获取Stop时间
     */

    public Date getStopTime()
    {
        return stopTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Stop时间
     * @param stopTime stop时间参数
     * @return
     * 返回值: void
     * 返回值说明：设置Stop时间
     */

    public void setStopTime(Date stopTime)
    {
        this.stopTime = stopTime;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明：toString
     */

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("jobLogId", getJobLogId())
            .append("jobName", getJobName())
            .append("jobGroup", getJobGroup())
            .append("jobMessage", getJobMessage())
            .append("status", getStatus())
            .append("exceptionInfo", getExceptionInfo())
            .append("startTime", getStartTime())
            .append("stopTime", getStopTime())
            .toString();
    }
}
