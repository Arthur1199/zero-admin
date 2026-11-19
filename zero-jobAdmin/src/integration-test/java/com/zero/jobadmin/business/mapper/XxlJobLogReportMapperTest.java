package com.zero.jobadmin.business.mapper;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.model.XxlJobLogReport;
import com.xxl.tool.core.DateTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 演练调度逐日报表记录新增或更新，通过日志观察数据库结果。
 */
@Slf4j(topic = "com.zero.jobadmin.business.mapper.XxlJobLogMapperTest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class XxlJobLogReportMapperTest {
    @Resource
    private XxlJobLogReportMapper xxlJobLogReportMapper;

    @Test
    public void test(){

        Date date = DateTool.parseDate("2025-10-01");

        XxlJobLogReport xxlJobLogReport = new XxlJobLogReport();
        xxlJobLogReport.setTriggerDay(date);
        xxlJobLogReport.setRunningCount(444);
        xxlJobLogReport.setSucCount(555);
        xxlJobLogReport.setFailCount(666);
        xxlJobLogReport.setUpdateTime(new Date());

        int ret = xxlJobLogReportMapper.saveOrUpdate(xxlJobLogReport);
        log.info("ret:{}", ret);
    }
}
