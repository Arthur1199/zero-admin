package com.zero.jobadmin.business.schedule.route;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.route.strategy.ExecutorRouteConsistentHash;
import org.junit.jupiter.api.Test;

import java.util.List;

// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 使用固定的演示执行器地址，输出一批任务在一致性哈希路由下的选择结果。
 */
@Slf4j
@org.junit.jupiter.api.Tag("job-integration")
public class ExecutorRouteConsistentHashTest {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将一组连续任务 ID 路由到固定的示例地址列表。
     * @return
     * 返回值: void
     * 返回值说明: 输出每个任务的路由地址，便于观察任务分布
     */
    @Test
    public void test() {

        List<String> addressList = List.of("192.168.0.1:1111", "192.168.0.2:2222", "192.168.0.3:3333");
        ExecutorRouteConsistentHash executorRouteConsistentHash = new ExecutorRouteConsistentHash();

        for (int i = 0; i < 100; i++) {
            int jobId = 1000 + i;
            String address = executorRouteConsistentHash.hashJob(jobId, addressList);
            log.info("jobId:{}, address:{}", jobId, address);
        }
    }
}
