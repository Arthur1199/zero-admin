package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLog;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 演练调度日志分页和触发、回调、清理相关的数据库操作。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class XxlJobLogMapperTest {

    @Resource
    private XxlJobLogMapper xxlJobLogMapper;

    @Test
    public void test(){
        List<XxlJobLog> list = xxlJobLogMapper.pageList(0, 10, 1, 1, null, null, 1);
        int list_count = xxlJobLogMapper.pageListCount(0, 10, 1, 1, null, null, 1);

        XxlJobLog log = new XxlJobLog();
        log.setJobGroup(1);
        log.setJobId(1);

        long ret1 = xxlJobLogMapper.save(log);
        XxlJobLog dto = xxlJobLogMapper.load(log.getId());

        log.setTriggerTime(new Date());
        log.setTriggerCode(1);
        log.setTriggerMsg("1");
        log.setExecutorAddress("1");
        log.setExecutorHandler("1");
        log.setExecutorParam("1");
        ret1 = xxlJobLogMapper.updateTriggerInfo(log);
        dto = xxlJobLogMapper.load(log.getId());


        log.setHandleTime(new Date());
        log.setHandleCode(2);
        log.setHandleMsg("2");
        ret1 = xxlJobLogMapper.updateHandleInfo(log);
        dto = xxlJobLogMapper.load(log.getId());


        List<Long> ret4 = xxlJobLogMapper.findClearLogIds(1, 1, new Date(), 100, 100, 0, Long.MAX_VALUE);

        int ret2 = xxlJobLogMapper.delete(log.getJobId());

    }

}
