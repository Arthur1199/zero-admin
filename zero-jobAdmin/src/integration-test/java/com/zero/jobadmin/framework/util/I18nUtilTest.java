package com.zero.jobadmin.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * email util test
 *
 * @author xuxueli 2017-12-22 17:16:23
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class I18nUtilTest {
    @Test
    public void test(){
        log.info(I18nUtil.getString("admin_name"));
        log.info(I18nUtil.getMultString("admin_name", "admin_name_full"));
        log.info(I18nUtil.getMultString());
    }

}
