package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobLogGlue;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 演练 GLUE 脚本历史的保存、查询、版本清理和删除。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class XxlJobLogGlueMapperTest {

    @Resource
    private XxlJobLogGlueMapper xxlJobLogGlueMapper;

    @Test
    public void test(){
        XxlJobLogGlue logGlue = new XxlJobLogGlue();
        logGlue.setJobId(1);
        logGlue.setGlueType("1");
        logGlue.setGlueSource("1");
        logGlue.setGlueRemark("1");

        logGlue.setAddTime(new Date());
        logGlue.setUpdateTime(new Date());
        int ret = xxlJobLogGlueMapper.save(logGlue);

        List<XxlJobLogGlue> list = xxlJobLogGlueMapper.findByJobId(1);

        int ret2 = xxlJobLogGlueMapper.removeOld(1, 1);

        int ret3 = xxlJobLogGlueMapper.deleteByJobId(1);
    }

}
