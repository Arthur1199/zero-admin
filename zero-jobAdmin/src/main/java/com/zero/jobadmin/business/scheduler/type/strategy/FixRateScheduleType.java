package com.zero.jobadmin.business.scheduler.type.strategy;

import com.zero.jobadmin.business.model.XxlJobInfo;
import com.zero.jobadmin.business.scheduler.type.ScheduleType;
import com.xxl.tool.core.DateTool;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按配置的固定秒数间隔计算下一次任务触发。
 */
public class FixRateScheduleType extends ScheduleType {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:36
     * 说明: 按合法的正整数秒间隔计算下一个调度时间。
     * @param jobInfo 使用固定速度调度的任务
     * @param fromTime 本次计算的起点
     * @return
     * 返回值: Date
     * 返回值说明: 向上对齐整秒的未来时间
     */
    @Override
    public Date generateNextTriggerTime(XxlJobInfo jobInfo, Date fromTime) throws Exception {
        int seconds = parseIntervalSeconds(jobInfo.getScheduleConf());
        Date nextTriggerTime = new Date(Math.addExact(fromTime.getTime(), seconds * 1000L));

        // assign second:
        if (nextTriggerTime.getTime() % 1000 != 0) {
            nextTriggerTime = DateTool.addSeconds(DateTool.setMilliseconds(nextTriggerTime, 0), 1);
        }

        return nextTriggerTime;

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 21:44
     * 说明: 解析固定间隔配置，保存、预览及调度共用正整数规则。
     * @param config 正整数秒的字符串配置
     * @return
     * 返回值: int
     * 返回值说明: 大于零且不超过 int 上限的秒数
     * @throws IllegalArgumentException 缺少配置、格式错误或数值越界
     */
    public static int parseIntervalSeconds(String config) {
        int seconds = Integer.parseInt(config);
        if (seconds < 1) {
            throw new IllegalArgumentException("Fixed-rate interval must be positive.");
        }
        return seconds;
    }

}
