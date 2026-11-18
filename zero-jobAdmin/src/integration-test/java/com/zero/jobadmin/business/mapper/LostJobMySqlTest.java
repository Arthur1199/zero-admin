package com.zero.jobadmin.business.mapper;

import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.business.model.XxlJobLog;
import com.xxl.job.core.constant.RegistTypeEnum;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18 20:17
 * 说明: 在显式指定的临时 MySQL 实例验证待回调日志离线判定，不加载服务配置或启动调度线程。
 */
@Tag("job-integration")
class LostJobMySqlTest {

    /** 临时容器专用的空测试库。 */
    private static final String DATABASE = "zero_job_test";
    /** 临时容器专用账号，只对测试库授权。 */
    private static final String USERNAME = "zero_job_fixture";
    /** 从现有建表脚本中提取本用例所需的三张表。 */
    private static final Pattern TABLE_DDL = Pattern.compile("(?s)CREATE TABLE `xxl_job_(?:group|registry|log)`.*?;");

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
    void lostJobUpdateRechecksCurrentHeartbeatAndResult() throws Exception {
        String port = System.getenv("ZERO_JOB_TEST_MYSQL_PORT");
        String password = System.getenv("ZERO_JOB_TEST_MYSQL_PASSWORD");
        assumeTrue(port != null && port.matches("[0-9]{1,5}") && password != null && !password.isBlank(),
                "本用例只在提供独立容器端口及临时凭据时运行");
        var dataSource = new DriverManagerDataSource(
                "jdbc:mysql://127.0.0.1:" + port + "/" + DATABASE + "?sslMode=DISABLED&connectionTimeZone=UTC",
                USERNAME, password);
        String schema = Files.readString(Path.of("upstream", "tables_xxl_job-3.5.0.sql"));
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            var tables = TABLE_DDL.matcher(schema);
            while (tables.find()) {
                statement.execute(tables.group());
            }
        }

        var config = new Configuration(new Environment("isolated-mysql", new JdbcTransactionFactory(), dataSource));
        for (Class<?> mapper : new Class<?>[]{XxlJobGroupMapper.class, XxlJobRegistryMapper.class, XxlJobLogMapper.class}) {
            String xml = "mapper/business/" + mapper.getSimpleName() + ".xml";
            try (var stream = getClass().getClassLoader().getResourceAsStream(xml)) {
                new XMLMapperBuilder(stream, config, xml, config.getSqlFragments()).parse();
            }
        }

        try (SqlSession session = new SqlSessionFactoryBuilder().build(config).openSession(true)) {
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
