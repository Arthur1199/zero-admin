package com.zero.quartz.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zero.common.annotation.Excel;
import com.zero.common.annotation.Excel.ColumnType;
import com.zero.common.constant.ScheduleConstants;
import com.zero.common.core.domain.BaseEntity;
import com.zero.common.utils.StringUtils;
import com.zero.quartz.util.CronUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 定时任务调度表 sys_job
 */
public class SysJob extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @Excel(name = "任务序号", cellType = ColumnType.NUMERIC)
    private Long jobId;

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
     * cron执行表达式
     */
    @Excel(name = "执行表达式 ")
    private String cronExpression;

    /**
     * cron计划策略
     */
    @Excel(name = "计划策略 ", readConverterExp = "0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行")
    private String misfirePolicy = ScheduleConstants.MISFIRE_DEFAULT;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    @Excel(name = "并发执行", readConverterExp = "0=允许,1=禁止")
    private String concurrent;

    /**
     * 任务状态（0正常 1暂停）
     */
    @Excel(name = "任务状态", readConverterExp = "0=正常,1=暂停")
    private String status;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取定时任务ID
     * @return
     * 返回值: Long
     * 返回值说明：获取定时任务ID
     */

    public Long getJobId()
    {
        return jobId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置定时任务ID
     * @param jobId 定时任务ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置定时任务ID
     */

    public void setJobId(Long jobId)
    {
        this.jobId = jobId;
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

    @NotBlank(message = "任务名称不能为空")
    @Size(min = 0, max = 64, message = "任务名称不能超过64个字符")
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

    @NotBlank(message = "调用目标字符串不能为空")
    @Size(min = 0, max = 500, message = "调用目标字符串长度不能超过500个字符")
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
     * 说明: 获取Cron表达式Expression
     * @return
     * 返回值: String
     * 返回值说明：获取Cron表达式Expression
     */

    @NotBlank(message = "Cron执行表达式不能为空")
    @Size(min = 0, max = 255, message = "Cron执行表达式不能超过255个字符")
    public String getCronExpression()
    {
        return cronExpression;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Cron表达式Expression
     * @param cronExpression Cron表达式Expression参数
     * @return
     * 返回值: void
     * 返回值说明：设置Cron表达式Expression
     */

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取NextValid时间
     * @return
     * 返回值: Date
     * 返回值说明：获取NextValid时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getNextValidTime()
    {
        if (StringUtils.isNotEmpty(cronExpression))
        {
            return CronUtils.getNextExecution(cronExpression);
        }
        return null;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取MisfirePolicy
     * @return
     * 返回值: String
     * 返回值说明：获取MisfirePolicy
     */

    public String getMisfirePolicy()
    {
        return misfirePolicy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置MisfirePolicy
     * @param misfirePolicy misfirePolicy参数
     * @return
     * 返回值: void
     * 返回值说明：设置MisfirePolicy
     */

    public void setMisfirePolicy(String misfirePolicy)
    {
        this.misfirePolicy = misfirePolicy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Concurrent
     * @return
     * 返回值: String
     * 返回值说明：获取Concurrent
     */

    public String getConcurrent()
    {
        return concurrent;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Concurrent
     * @param concurrent concurrent参数
     * @return
     * 返回值: void
     * 返回值说明：设置Concurrent
     */

    public void setConcurrent(String concurrent)
    {
        this.concurrent = concurrent;
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
     * 说明: toString
     * @return
     * 返回值: String
     * 返回值说明：toString
     */

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("jobId", getJobId())
            .append("jobName", getJobName())
            .append("jobGroup", getJobGroup())
            .append("cronExpression", getCronExpression())
            .append("nextValidTime", getNextValidTime())
            .append("misfirePolicy", getMisfirePolicy())
            .append("concurrent", getConcurrent())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
