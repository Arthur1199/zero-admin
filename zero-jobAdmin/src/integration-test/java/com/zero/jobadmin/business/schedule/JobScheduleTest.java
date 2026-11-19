package com.zero.jobadmin.business.schedule;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.config.XxlJobAdminBootstrap;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 启动多个线程争用数据库调度行锁，通过日志观察事务间的等待及释放顺序。
 */
@Slf4j
@SpringBootTest
// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
@org.junit.jupiter.api.Tag("job-integration")
public class JobScheduleTest {
    @Test
    public void test() throws InterruptedException {

        // thread
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                lockTest("threadName-" + finalI);
            }).start();
        }

        TimeUnit.MINUTES.sleep(10);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在独立事务内竞争同一数据库行锁，持锁一段时间并记录取得与释放的时间。
     * @param threadName 用于区分并发调用的日志标识
     * @return
     * 返回值: void
     * 返回值说明: 提交当前事务并释放调度锁
     */
    private void lockTest(String threadName) {

        TransactionStatus transactionStatus = XxlJobAdminBootstrap.getInstance().getTransactionManager().getTransaction(new DefaultTransactionDefinition());
        try {
            String lockedRecord = XxlJobAdminBootstrap.getInstance().getXxlJobLockMapper().scheduleLock(); // for update

            log.info(threadName + " : start at " + DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS") );
            TimeUnit.MILLISECONDS.sleep(500);
            log.info(threadName + " : end at " + DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS") );
        } catch (Throwable e) {
            log.error("error: ",  e);
        } finally {
            log.info(threadName + " : commit at " + DateTool.format(new Date(), "yyyy-MM-dd HH:mm:ss SSS") );
            XxlJobAdminBootstrap.getInstance().getTransactionManager().commit(transactionStatus);
        }
    }
}
