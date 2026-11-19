package com.zero.jobadmin.business.scheduler.misfire;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 定义任务错过计划触发时间后的处理行为，由实际策略实现。
 */
public abstract class MisfireHandler {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 处理扫描时超过允许迟到窗口的任务。
     * @param jobId 已错过计划时间的任务主键
     * @return
     * 返回值: void
     * 返回值说明: 根据所选策略记录跳过或重新提交触发
     */
    public abstract void handle(final int jobId);

}
