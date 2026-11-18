package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobRegistry;
import com.xxl.job.core.constant.RegistTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class XxlJobRegistryMapperTest {

    @Resource
    private XxlJobRegistryMapper xxlJobRegistryMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 19:56
     * 说明: 模拟清理候选查出后收到新心跳，仅使用事务内创建的测试节点。
     * @return
     * 返回值: void
     * 返回值说明: 已续约的候选节点保留，仍超时的测试节点可清理
     */
    @Test
    @Transactional
    public void expiredRegistryCleanupRechecksHeartbeat() {
        String group = RegistTypeEnum.EXECUTOR.name();
        String appName = "registry-test-" + UUID.randomUUID();
        String address = "http://executor.example:9999";
        Date now = new Date();
        xxlJobRegistryMapper.registrySaveOrUpdate(group, appName, address, now);
        XxlJobRegistry fixture = xxlJobRegistryMapper.findAll(60, now).stream()
                .filter(node -> appName.equals(node.getRegistryKey()))
                .findFirst().orElseThrow();

        Date expired = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(2));
        xxlJobRegistryMapper.registrySaveOrUpdate(group, appName, address, expired);
        List<Long> ids = List.of(fixture.getId());
        assertThat(xxlJobRegistryMapper.findDead(60, now, 1000)).contains(fixture.getId());
        xxlJobRegistryMapper.registrySaveOrUpdate(group, appName, address, now);
        assertThat(xxlJobRegistryMapper.removeDead(ids, 60, now)).isZero();

        xxlJobRegistryMapper.registrySaveOrUpdate(group, appName, address, expired);
        assertThat(xxlJobRegistryMapper.removeDead(ids, 60, now)).isEqualTo(1);
    }

    @Test
    public void test2() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new Thread(()->{
                int ret = xxlJobRegistryMapper.registrySaveOrUpdate("g1", "k1", "v1", new Date());
                System.out.println(ret);

                /*int ret = xxlJobRegistryDao.registryUpdate("g1", "k1", "v1", new Date());
                if (ret < 1) {
                    ret = xxlJobRegistryDao.registrySave("g1", "k1", "v1", new Date());
                }*/
            }).start();
        }

        TimeUnit.SECONDS.sleep(10);
    }

}
