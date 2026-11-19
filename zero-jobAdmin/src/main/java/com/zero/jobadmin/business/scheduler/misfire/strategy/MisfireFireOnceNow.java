package com.zero.jobadmin.business.scheduler.misfire.strategy;

import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.zero.jobadmin.business.scheduler.misfire.MisfireHandler;
import com.zero.jobadmin.business.scheduler.trigger.TriggerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 对错过计划时间的任务补发一次 MISFIRE 类型的触发。
 */
public class MisfireFireOnceNow extends MisfireHandler {
    protected static Logger logger = LoggerFactory.getLogger(MisfireFireOnceNow.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 向触发线程池提交一次补偿触发。
     * @param jobId 需要补偿执行的任务主键
     * @return
     * 返回值: void
     * 返回值说明: 按任务配置尝试执行一次
     */
    @Override
    public void handle(int jobId) {
        // FIRE_ONCE_NOW 》 trigger
        XxlJobAdminBootstrap.getInstance().getJobTriggerPoolHelper().trigger(jobId, TriggerTypeEnum.MISFIRE, -1, null, null, null);
        logger.warn(">>>>>>>>>>> xxl-job, schedule MisfireFireOnceNow: jobId = " + jobId );
    }

}
