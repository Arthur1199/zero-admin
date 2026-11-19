package com.zero.jobadmin.business.scheduler.misfire.strategy;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.misfire.MisfireHandler;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 调度超期时跳过本次触发，仅记录日志。
 */
@Slf4j
public class MisfireDoNothing extends MisfireHandler {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对已过期的触发只记录跳过日志。
     * @param jobId 已过期的任务主键
     * @return
     * 返回值: void
     * 返回值说明: 本次计划不向执行器派发
     */
    @Override
    public void handle(int jobId) {
        log.warn(">>>>>>>>>>> xxl-job, schedule MisfireDoNothing: jobId = " + jobId );
    }

}
