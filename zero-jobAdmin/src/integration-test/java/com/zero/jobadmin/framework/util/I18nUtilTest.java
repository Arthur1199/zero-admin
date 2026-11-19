package com.zero.jobadmin.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 展示调度控制台国际化文案的单键和多键读取结果。
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class I18nUtilTest {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取指定国际化键并演示无参数时的多键查找结果。
     * @return
     * 返回值: void
     * 返回值说明: 将国际化文案写入测试日志
     */
    @Test
    public void test(){
        log.info(I18nUtil.getString("admin_name"));
        log.info(I18nUtil.getMultString("admin_name", "admin_name_full"));
        log.info(I18nUtil.getMultString());
    }

}
