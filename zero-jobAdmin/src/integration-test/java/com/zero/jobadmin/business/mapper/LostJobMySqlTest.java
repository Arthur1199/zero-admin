package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.zero.jobadmin.business.model.XxlJobLogReport;
import com.xxl.job.core.constant.RegistTypeEnum;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 20:17
 * 说明: 在显式指定的测试 MySQL 中验证调度日志状态和备注更新，测试数据仅保存在当前连接。
 */
@Tag("job-integration")
class LostJobMySqlTest {

    /** 临时容器专用的空测试库。 */
    private static final String DATABASE = "zero_job_test";
    /** 临时容器专用账号，只对测试库授权。 */
    private static final String USERNAME = "zero_job_fixture";
    /** 从项目建表脚本中提取调度日志、任务、执行器组、心跳和日报表。 */
    private static final Pattern TABLE_DDL = Pattern.compile("(?s)CREATE TABLE `xxl_job_(?:group|registry|log|info|log_report)`.*?;");
    /** 每个用例独享的会话，其连接关闭时自动清除临时表。 */
    private SqlSession session;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:04
     * 说明: 仅在显式指定测试实例时连接 MySQL，使用实际表定义建立会话级临时表。
     * @return
     * 返回值: void
     * 返回值说明: Mapper 与临时表使用同一连接，测试记录不会进入持久表
     */
    @BeforeEach
    void openIsolatedSession() throws Exception {
        String port = System.getenv("ZERO_JOB_TEST_MYSQL_PORT");
        String password = System.getenv("ZERO_JOB_TEST_MYSQL_PASSWORD");
        assumeTrue(port != null && port.matches("[0-9]{1,5}") && password != null && !password.isBlank(),
                "本用例只在提供独立容器端口及临时凭据时运行");
        var dataSource = new DriverManagerDataSource(
                "jdbc:mysql://127.0.0.1:" + port + "/" + DATABASE + "?sslMode=DISABLED&connectionTimeZone=UTC",
                USERNAME, password);
        String schema = Files.readString(Path.of("..", "sql", "zero_job.sql"));
        var config = new Configuration(new Environment("isolated-mysql", new JdbcTransactionFactory(), dataSource));
        for (Class<?> mapper : new Class<?>[]{XxlJobGroupMapper.class, XxlJobRegistryMapper.class, XxlJobLogMapper.class,
                JobDashboardMapper.class, XxlJobLogReportMapper.class}) {
            String xml = "mapper/business/" + mapper.getSimpleName() + ".xml";
            try (var stream = getClass().getClassLoader().getResourceAsStream(xml)) {
                new XMLMapperBuilder(stream, config, xml, config.getSqlFragments()).parse();
            }
        }
        session = new SqlSessionFactoryBuilder().build(config).openSession(true);
        try (var statement = session.getConnection().createStatement()) {
            var tables = TABLE_DDL.matcher(schema);
            int created = 0;
            while (tables.find()) {
                statement.execute(tables.group().replaceFirst("^CREATE TABLE", "CREATE TEMPORARY TABLE"));
                created++;
            }
            assertThat(created).isEqualTo(5);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:04
     * 说明: 关闭测试连接并清除其临时表。
     * @return
     * 返回值: void
     * 返回值说明: 未启用集成测试时没有需要关闭的会话
     */
    @AfterEach
    void closeIsolatedSession() {
        if (session != null) {
            session.close();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:17
     * 说明: 验证离线判定遵循执行器组和心跳边界，并在写入前复查心跳及回调结果。
     * @return
     * 返回值: void
     * 返回值说明: 仅失效心跳对应的自动注册组日志标记为失败
     */
    @Test
    void lostJobUpdateRechecksCurrentHeartbeatAndResult() {
        var groups = session.getMapper(XxlJobGroupMapper.class);
        var registry = session.getMapper(XxlJobRegistryMapper.class);
        var logs = session.getMapper(XxlJobLogMapper.class);
        int automatic = createGroup(groups, "offline-fixture", 0);
        int manual = createGroup(groups, "manual-fixture", 1);
        String registryGroup = RegistTypeEnum.EXECUTOR.name();
        Date now = Date.from(Instant.parse("2026-11-18T12:00:00Z"));
        Date triggerBefore = Date.from(now.toInstant().minusSeconds(600));
        Date heartbeatSince = Date.from(now.toInstant().minusSeconds(90));
        Date expired = Date.from(now.toInstant().minusSeconds(91));
        String lostAddress = "http://offline.example:9999";
        String recoveredAddress = "http://recovered.example:9999";
        String onlineAddress = "http://online.example:9999";
        long lost = createLog(logs, automatic, lostAddress, triggerBefore);
        long recovered = createLog(logs, automatic, recoveredAddress, triggerBefore);
        long finished = createLog(logs, automatic, "http://finished.example:9999", triggerBefore);
        long manualLog = createLog(logs, manual, "http://manual.example:9999", triggerBefore);
        long onlineLog = createLog(logs, automatic, onlineAddress, triggerBefore);
        registry.registrySaveOrUpdate(registryGroup, "offline-fixture", lostAddress, expired);
        registry.registrySaveOrUpdate(registryGroup, "different-app", lostAddress, now);
        registry.registrySaveOrUpdate(registryGroup, "offline-fixture", onlineAddress, heartbeatSince);
        assertThat(logs.findLostJobIds(triggerBefore, registryGroup, heartbeatSince, 1000))
                .containsExactly(lost, recovered, finished);

        registry.registrySaveOrUpdate(registryGroup, "offline-fixture", recoveredAddress, now);
        XxlJobLog callback = completion(finished, now, 200);
        assertThat(logs.updateHandleInfo(callback)).isEqualTo(1);
        assertThat(logs.markLostJob(completion(recovered, now, 500), triggerBefore, registryGroup, heartbeatSince)).isZero();
        assertThat(logs.markLostJob(completion(finished, now, 500), triggerBefore, registryGroup, heartbeatSince)).isZero();
        assertThat(logs.markLostJob(completion(manualLog, now, 500), triggerBefore, registryGroup, heartbeatSince)).isZero();
        assertThat(logs.markLostJob(completion(onlineLog, now, 500), triggerBefore, registryGroup, heartbeatSince)).isZero();
        assertThat(logs.markLostJob(completion(lost, now, 500), triggerBefore, registryGroup, heartbeatSince)).isEqualTo(1);
        assertThat(logs.load(lost).getHandleCode()).isEqualTo(500);
        assertThat(logs.load(finished).getHandleCode()).isEqualTo(200);
        assertThat(logs.findLostJobIds(triggerBefore, registryGroup, heartbeatSince, 1000)).isEmpty();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:04
     * 说明: 在派发结果写回前模拟失败回调及重试备注，验证 SQL 合并顺序。
     * @return
     * 返回值: void
     * 返回值说明: 回调结果、重试备注和派发结果均写入同一条日志
     */
    @Test
    void triggerResultPreservesRetryRemarkWrittenAfterEarlyCallback() {
        var logs = session.getMapper(XxlJobLogMapper.class);
        Date now = Date.from(Instant.parse("2026-11-18T12:00:00Z"));
        var dispatch = new XxlJobLog();
        dispatch.setJobId(42);
        dispatch.setJobGroup(1);
        dispatch.setTriggerTime(now);
        dispatch.setExecutorParam("原始参数");
        dispatch.setExecutorShardingParam("1/2");
        dispatch.setExecutorFailRetryCount(2);
        assertThat(logs.save(dispatch)).isEqualTo(1);

        long id = dispatch.getId();
        assertThat(logs.updateHandleInfo(completion(id, now, 500))).isEqualTo(1);
        assertThat(logs.findFailJobLogIds(10)).containsExactly(id);
        assertThat(logs.updateAlarmStatus(id, 0, -1)).isEqualTo(1);
        var earlyCallback = logs.load(id);
        assertThat(earlyCallback.getExecutorParam()).isEqualTo("原始参数");
        assertThat(earlyCallback.getExecutorShardingParam()).isEqualTo("1/2");
        assertThat(earlyCallback.getExecutorFailRetryCount()).isEqualTo(2);
        assertThat(logs.appendRetryMessage(id, "<br>已提交重试")).isEqualTo(1);

        dispatch.setTriggerCode(200);
        dispatch.setExecutorAddress("http://127.0.0.1:18080");
        dispatch.setTriggerMsg("已派发");
        assertThat(logs.updateTriggerInfo(dispatch)).isEqualTo(1);
        var saved = logs.load(id);
        assertThat(saved.getTriggerCode()).isEqualTo(200);
        assertThat(saved.getHandleCode()).isEqualTo(500);
        assertThat(saved.getTriggerMsg()).isEqualTo("已派发<br>已提交重试");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:04
     * 说明: 验证多字节备注正好占满 TEXT 时，追加重试备注会保留历史内容。
     * @return
     * 返回值: void
     * 返回值说明: 不写入超限备注，告警状态仍可正常更新
     */
    @Test
    void fullHistoricalRemarkDoesNotPreventAlarmFinalization() {
        var logs = session.getMapper(XxlJobLogMapper.class);
        var log = new XxlJobLog();
        log.setJobId(42);
        log.setJobGroup(1);
        log.setTriggerTime(Date.from(Instant.parse("2026-11-18T12:00:00Z")));
        log.setTriggerCode(500);
        String fullRemark = "界".repeat(65_535 / 3);
        log.setTriggerMsg(fullRemark);
        assertThat(logs.save(log)).isEqualTo(1);
        assertThat(logs.updateAlarmStatus(log.getId(), 0, -1)).isEqualTo(1);
        assertThat(logs.appendRetryMessage(log.getId(), "<br>重试")).isZero();
        assertThat(logs.updateAlarmStatus(log.getId(), -1, 2)).isEqualTo(1);
        assertThat(logs.load(log.getId()).getTriggerMsg()).isEqualTo(fullRemark);
        assertThat(logs.load(log.getId()).getAlarmStatus()).isEqualTo(2);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:18
     * 说明: 验证报表和日志筛选使用同一终态口径，派发超时后成功回调不计为失败。
     * @return
     * 返回值: void
     * 返回值说明: 趋势四类计数互斥，失败榜次数等于对应日志筛选结果
     */
    @Test
    void dashboardAndLogFiltersUseCallbackOutcomeWhenAvailable() {
        var logs = session.getMapper(XxlJobLogMapper.class);
        var dashboard = session.getMapper(JobDashboardMapper.class);
        Date now = Date.from(Instant.parse("2026-11-18T12:00:00Z"));
        int group = 2;
        int job = 42;
        var statusPairs = new int[][]{{500, 200}, {500, 500}, {500, 0}, {200, 0}, {0, 0}};
        for (int[] pair : statusPairs) {
            var log = new XxlJobLog();
            log.setJobGroup(group);
            log.setJobId(job);
            log.setTriggerTime(now);
            log.setTriggerCode(pair[0]);
            log.setHandleCode(pair[1]);
            assertThat(logs.save(log)).isEqualTo(1);
        }

        var from = LocalDate.of(2026, 11, 18).atStartOfDay();
        var until = from.plusDays(1);
        var trend = dashboard.trend(from, until, List.of(group));
        assertThat(trend).hasSize(1);
        assertThat(trend.get(0).succeeded()).isEqualTo(1);
        assertThat(trend.get(0).dispatchFailed()).isEqualTo(1);
        assertThat(trend.get(0).executionFailed()).isEqualTo(1);
        assertThat(trend.get(0).pending()).isEqualTo(1);
        assertThat(logs.pageListCount(0, 10, group, job, null, null, 1)).isEqualTo(1);
        int failures = logs.pageListCount(0, 10, group, job, null, null, 2);
        assertThat(failures).isEqualTo(2);
        var failuresByJob = dashboard.topFailures(from, until, List.of(group));
        assertThat(failuresByJob).hasSize(1);
        assertThat(failuresByJob.get(0).failed()).isEqualTo(failures);
        assertThat(failuresByJob.get(0).attempts()).isEqualTo(4);
        assertThat(dashboard.pending(from, until, List.of(group), 0L, 10)).hasSize(1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 23:36
     * 说明: 验证历史补算只选取保留期内的待回调日期，并保存最新的日汇总。
     * @return
     * 返回值: void
     * 返回值说明: 游标和保留期边界有效，旧快照不能把已完成日期改回进行中
     */
    @Test
    void historicalReportRefreshPreservesNewestSnapshot() {
        var reports = session.getMapper(XxlJobLogReportMapper.class);
        var recent = Instant.parse("2026-11-18T00:00:00Z");
        var started = Date.from(recent.plusSeconds(3600));
        var retentionStart = Date.from(recent.minusSeconds(7 * 86400L));
        var recentStart = Date.from(recent.minusSeconds(2 * 86400L));
        var oldestPending = Date.from(recent.minusSeconds(6 * 86400L));
        var laterPending = Date.from(recent.minusSeconds(4 * 86400L));
        for (int offset : List.of(8, 6, 4, 2)) {
            var row = new XxlJobLogReport();
            row.setTriggerDay(Date.from(recent.minusSeconds(offset * 86400L)));
            row.setRunningCount(1);
            row.setUpdateTime(started);
            reports.saveOrUpdate(row);
        }
        assertThat(reports.findPendingReportDays(retentionStart, recentStart, null, 1)).containsExactly(oldestPending);
        assertThat(reports.findPendingReportDays(retentionStart, recentStart, oldestPending, 1))
                .containsExactly(laterPending);

        var finished = new XxlJobLogReport();
        finished.setTriggerDay(oldestPending);
        finished.setSucCount(1);
        finished.setUpdateTime(Date.from(started.toInstant().plusSeconds(60)));
        reports.saveOrUpdate(finished);
        var stale = new XxlJobLogReport();
        stale.setTriggerDay(oldestPending);
        stale.setRunningCount(1);
        stale.setUpdateTime(started);
        reports.saveOrUpdate(stale);
        assertThat(reports.findPendingReportDays(retentionStart, recentStart, null, 10)).containsExactly(laterPending);
        assertThat(reports.queryLogReport(oldestPending, oldestPending)).singleElement().satisfies(report -> {
            assertThat(report.getRunningCount()).isZero();
            assertThat(report.getSucCount()).isEqualTo(1);
        });
    }

    private int createGroup(XxlJobGroupMapper mapper, String appName, int addressType) {
        var group = new XxlJobGroup();
        group.setAppname(appName);
        group.setName(appName);
        group.setAddressType(addressType);
        mapper.save(group);
        return group.getId();
    }

    private long createLog(XxlJobLogMapper mapper, int groupId, String address, Date triggerTime) {
        var log = new XxlJobLog();
        log.setJobId(groupId);
        log.setJobGroup(groupId);
        log.setTriggerTime(triggerTime);
        log.setTriggerCode(200);
        mapper.save(log);
        log.setExecutorAddress(address);
        mapper.updateTriggerInfo(log);
        return log.getId();
    }

    private XxlJobLog completion(long id, Date finishTime, int handleCode) {
        var log = new XxlJobLog();
        log.setId(id);
        log.setHandleTime(finishTime);
        log.setHandleCode(handleCode);
        return log;
    }
}
