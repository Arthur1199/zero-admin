package com.zero.jobadmin.business.core.util;

import com.zero.jobadmin.business.scheduler.cron.CronExpression;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class CronExpressionTest {

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
