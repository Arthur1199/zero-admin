package com.zero.jobadmin.business.schedule.route;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.route.strategy.ExecutorRouteConsistentHash;
import org.junit.jupiter.api.Test;

import java.util.List;

// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@Slf4j
@org.junit.jupiter.api.Tag("job-integration")
public class ExecutorRouteConsistentHashTest {
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
