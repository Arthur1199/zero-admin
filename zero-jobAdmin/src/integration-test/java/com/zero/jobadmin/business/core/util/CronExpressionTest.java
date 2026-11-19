package com.zero.jobadmin.business.core.util;

import com.zero.jobadmin.business.scheduler.cron.CronExpression;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 展示 Cron 表达式计算出的连续触发时间，便于通过输出观察调度结果。
 */
@org.junit.jupiter.api.Tag("job-integration")
public class CronExpressionTest {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从同一 Cron 表达式依次计算并输出后续触发时间。
     * @return
     * 返回值: void
     * 返回值说明: 输出演示结果，供人工核对触发序列
     */
    @Test
    public void shouldWriteValueAsString() throws ParseException {
        CronExpression cronExpression = new CronExpression("0 0 0 ? * 1");
        Date lastTriggerTime = new Date();
        for (int i = 0; i < 5; i++) {
            Date nextTriggerTime = cronExpression.getNextValidTimeAfter(lastTriggerTime);
            System.out.println(DateTool.formatDateTime(nextTriggerTime));

            lastTriggerTime = nextTriggerTime;
        }
    }
}
