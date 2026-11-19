package com.zero.task.config;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.base.TaskBaseService;
import com.zero.task.domain.DictItem;
import com.zero.task.mapper.TaskRoutingProbeMapper;
import com.zero.task.service.TaskBizDictLookup;
import com.zero.util.RedissonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:08
 * 说明: 验证任务数据源和 Redis 独立装配、路由、Mapper XML 及分页。
 */
class TaskInfrastructureTest {

    /** 只装配任务基础设施。MySQL 兼容模式的内存库隔离业务数据库。 */
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, DynamicDataSourceAutoConfiguration.class,
                    MybatisPlusAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
                    JdbcTemplateAutoConfiguration.class, TransactionAutoConfiguration.class))
            .withUserConfiguration(TaskMybatisConfig.class, TaskRedisConfig.class, TaskBizDictLookup.class)
            .withPropertyValues("spring.datasource.dynamic.enabled=false",
                    "mybatis-plus.mapper-locations=classpath*:mapper/*Mapper.xml");

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 未提供 Redis 主机或启用数据库时，不创建相应的客户端。
     */
    @Test
    void leavesBothCapabilitiesDisabledByDefault() {
        runner.run(context -> assertThat(context)
                .doesNotHaveBean(DataSource.class).doesNotHaveBean(TaskRoutingProbeMapper.class)
                .doesNotHaveBean(TaskBizDictLookup.class).doesNotHaveBean(RedisConnectionFactory.class)
                .doesNotHaveBean(RedissonClient.class).doesNotHaveBean(RedissonUtil.class));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 校验数据库配置与空 Redis 主机，兼容旧部署显式关闭 Redis。
     */
    @Test
    void reportsMissingConnectionProperties() {
        runner.withPropertyValues("spring.datasource.dynamic.enabled=true")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasMessageContaining("spring.datasource.dynamic.datasource.zeroBiz.url"));
        runner.withPropertyValues("spring.data.redis.host=")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasMessageContaining("spring.data.redis.host"));
        runner.withPropertyValues("spring.data.redis.host=127.0.0.1", "zero.task.redis.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RedissonClient.class));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 默认访问业务库，显式切换后台库，离开方法后恢复默认库。
     */
    @Test
    void routesBothDataSourcesAndPaginatesXmlQuery() throws Exception {
        databaseRunner().run(context -> {
            assertThat(context).doesNotHaveBean(RedisConnectionFactory.class);
            var mapper = context.getBean(TaskRoutingProbeMapper.class);
            var page = mapper.selectOrigin(new Page<>(2, 1));
            assertThat(page.getTotal()).isEqualTo(2);
            assertThat(page.getRecords()).containsExactly("biz-2");
            var reader = context.getBean(RoutingProbe.class);
            assertThat(reader.adminOrigin()).isEqualTo("admin-1");
            assertThat(mapper.selectOrigin(new Page<>(1, 1)).getRecords()).containsExactly("biz-1");
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 提供 Redis 主机后自动装配客户端和模板，使用与报表服务一致的连接池参数。
     */
    @Test
    void enablesRedisIndependentlyAndAlongsideMySql() throws Exception {
        String[] redisProperties = {"spring.data.redis.host=127.0.0.1",
                "zero.redis.connection-pool-size=4", "zero.redis.connection-minimum-idle-size=1",
                "zero.task.redis.connection-pool-size=8", "zero.task.redis.connection-minimum-idle-size=0"};
        runner.withPropertyValues(redisProperties).run(context -> {
            assertThat(context)
                    .hasSingleBean(StringRedisTemplate.class).hasBean("redisTemplate").hasSingleBean(RedissonClient.class)
                    .hasSingleBean(RedissonUtil.class).hasSingleBean(RedissonConnectionFactory.class)
                    .doesNotHaveBean(DataSource.class);
            var config = context.getBean(RedissonClient.class).getConfig();
            assertThat(config.isLazyInitialization()).isTrue();
            assertThat(config.useSingleServer().getConnectionPoolSize()).isEqualTo(4);
            assertThat(config.useSingleServer().getConnectionMinimumIdleSize()).isEqualTo(1);
        });
        databaseRunner().withPropertyValues(redisProperties).run(context -> assertThat(context)
                .hasSingleBean(StringRedisTemplate.class).hasBean("redisTemplate").hasSingleBean(DataSource.class));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 显式指定临时 Redis 端口后，校验字符串及对象读写和键过期。
     */
    @Test
    @EnabledIfSystemProperty(named = "zero.task.test.redis-port", matches = "[0-9]+")
    void readsAndExpiresKeysOnIsolatedRedis() {
        runner.withPropertyValues("spring.data.redis.host=127.0.0.1",
                "spring.data.redis.password=", "spring.data.redis.port=" + System.getProperty("zero.task.test.redis-port"))
                .run(context -> {
            var redis = context.getBean(StringRedisTemplate.class);
            var namespace = "zero-task:test:" + UUID.randomUUID();
            var stringKey = namespace + ":string";
            var objectKey = namespace + ":object";
            var nativeKey = namespace + ":native";
            var nativeRedis = context.getBean(RedissonUtil.class);
            @SuppressWarnings("unchecked")
            var objects = (RedisTemplate<Object, Object>) context.getBean("redisTemplate");
            try {
                redis.opsForValue().set(stringKey, "task-value", Duration.ofSeconds(2));
                assertThat(redis.opsForValue().get(stringKey)).isEqualTo("task-value");
                assertThat(redis.getExpire(stringKey)).isPositive();
                objects.opsForValue().set(objectKey, 42, Duration.ofMinutes(1));
                assertThat(objects.opsForValue().get(objectKey)).isEqualTo(42);
                nativeRedis.setString(nativeKey, "native");
                assertThat(nativeRedis.<String>getString(nativeKey)).isEqualTo("native");
                org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(5))
                        .until(() -> Boolean.FALSE.equals(redis.hasKey(stringKey)));
            } finally {
                redis.delete(stringKey);
                objects.delete(objectKey);
                nativeRedis.remove(nativeKey);
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:59
     * 说明: 验证任务父类的三个字典 API、业务数据源路由和已启用字典过滤。
     */
    @Test
    void looksUpBusinessDictionaryWithoutRedis() throws Exception {
        databaseRunner().withBean(DictionaryCaller.class).run(context -> {
            var caller = context.getBean(DictionaryCaller.class);
            assertThat(caller.valueFromAdminTransaction()).isEqualTo("paid");
            assertThat(caller.item(" task_status . paid ").getItemName()).isEqualTo("Paid");
            assertThat(caller.items("task_status")).extracting(DictItem::getItemCode).containsExactly("pending", "paid");
            assertThat(caller.item("task_status.disabled")).isNull();
            assertThat(caller.item("disabled_group.paid")).isNull();
            assertThat(caller.item("task_status.paid.extra")).isNull();
            assertThat(caller.items("missing")).isEmpty();
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:59
     * 说明: 在隔离 Redis 验证任务缓存命中，以及报表服务切换共享字典版本后的回源。
     */
    @Test
    @EnabledIfSystemProperty(named = "zero.task.test.redis-port", matches = "[0-9]+")
    void observesSharedBusinessDictionaryVersion() throws Exception {
        databaseRunner().withBean(DictionaryCaller.class).withPropertyValues(
                "spring.data.redis.host=127.0.0.1",
                "spring.data.redis.port=" + System.getProperty("zero.task.test.redis-port")).run(context -> {
                    var caller = context.getBean(DictionaryCaller.class);
                    var jdbc = context.getBean(JdbcTemplate.class);
                    var redis = context.getBean(RedissonClient.class);
                    assertThat(caller.valueFromAdminTransaction()).isEqualTo("paid");
                    assertThat(caller.items("task_status")).hasSize(2);
                    jdbc.update("UPDATE biz_dict_item SET item_value = 'updated' WHERE id = 10");
                    assertThat(caller.valueFromAdminTransaction()).isEqualTo("paid");
                    // 模拟 zero-report 在提交后写入同一 Redis 版本键。
                    redis.getBucket("zero:report:biz-dict:version", StringCodec.INSTANCE).set(UUID.randomUUID().toString());
                    assertThat(caller.valueFromAdminTransaction()).isEqualTo("updated");
                    assertThat(caller.items("task_status")).extracting(DictItem::getItemValue)
                            .containsExactly("pending", "updated");
                });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 创建名称独立的测试库和 Mapper 调用入口，用于验证 @DS。
     * @return
     * 返回值: ApplicationContextRunner
     * 返回值说明: 双数据源已配置的测试上下文
     */
    private ApplicationContextRunner databaseRunner() throws Exception {
        String id = UUID.randomUUID().toString();
        String biz = "jdbc:h2:mem:task_biz_" + id + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        String admin = "jdbc:h2:mem:task_admin_" + id + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        createProbeRows(biz, "biz");
        createProbeRows(admin, "admin");
        createDictionaryRows(biz);
        return runner.withBean(RoutingProbe.class)
                .withPropertyValues("spring.datasource.dynamic.enabled=true",
                        "spring.datasource.dynamic.primary=zeroBiz", "spring.datasource.dynamic.strict=true",
                        "spring.datasource.dynamic.datasource.zeroBiz.url=" + biz,
                        "spring.datasource.dynamic.datasource.zeroBiz.username=sa",
                        "spring.datasource.dynamic.datasource.zeroAdmin.url=" + admin,
                        "spring.datasource.dynamic.datasource.zeroAdmin.username=sa");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 在内存库创建两条按主键排序的分页数据。
     * @param url 随机命名的内存 JDBC URL
     * @param origin 测试数据来源标签
     */
    private void createProbeRows(String url, String origin) throws Exception {
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE task_routing_probe (id INT PRIMARY KEY, origin VARCHAR(20))");
            try (var insert = connection.prepareStatement("INSERT INTO task_routing_probe VALUES (?, ?)")) {
                for (int row = 1; row <= 2; row++) {
                    insert.setInt(1, row);
                    insert.setString(2, origin + "-" + row);
                    insert.executeUpdate();
                }
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:59
     * 说明: 在隔离的业务库创建字典测试数据，覆盖有效、停用和列表排序。
     * @param url 隔离业务库 JDBC 地址
     */
    private void createDictionaryRows(String url) throws Exception {
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE biz_dict_group (id BIGINT PRIMARY KEY, group_code VARCHAR(100), status CHAR(1), del_flag CHAR(1))");
            statement.execute("""
                    CREATE TABLE biz_dict_item (id BIGINT PRIMARY KEY, group_id BIGINT, item_code VARCHAR(100),
                      item_name VARCHAR(100), item_value VARCHAR(500), extra_json VARCHAR(500),
                      item_sort INT, status CHAR(1), del_flag CHAR(1), remark VARCHAR(500),
                      create_by VARCHAR(64), create_time TIMESTAMP, update_by VARCHAR(64), update_time TIMESTAMP)
                    """);
            statement.execute("INSERT INTO biz_dict_group VALUES (1, 'task_status', '1', '0'), (2, 'disabled_group', '0', '0')");
            statement.execute("""
                    INSERT INTO biz_dict_item (id, group_id, item_code, item_name, item_value, item_sort, status, del_flag)
                    VALUES (10, 1, 'paid', 'Paid', 'paid', 20, '1', '0'),
                           (11, 1, 'pending', 'Pending', 'pending', 10, '1', '0'),
                           (12, 1, 'disabled', 'Disabled', 'disabled', 1, '0', '0'),
                           (13, 2, 'paid', 'Paid', 'paid', 1, '1', '0')
                    """);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:59
     * 说明: 通过基类公开测试入口，覆盖不同数据源的事务中调用业务字典。
     */
    static class DictionaryCaller extends TaskBaseService<TaskRoutingProbeMapper, DictItem> {

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 15:59
         * 说明: 在后台库事务中获取字典值，查询应挂起事务后访问业务库。
         * @return 任务状态字典值
         * 返回值: String
         * 返回值说明: 已支付的配置值
         */
        @DS("zeroAdmin")
        @Transactional
        public String valueFromAdminTransaction() {
            return getBizDictValue("task_status.paid");
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 15:59
         * 说明: 根据完整编码读取字典项。
         * @param key 分组编码.字典项编码
         * @return 字典项，无记录时为空
         * 返回值: DictItem
         * 返回值说明: 匹配的有效字典项
         */
        public DictItem item(String key) {
            return getBizDictItem(key);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 15:59
         * 说明: 按分组读取有效字典项。
         * @param groupCode 字典组编码
         * @return 排好序的字典项列表
         * 返回值: List<DictItem>
         * 返回值说明: 分组内所有有效字典项
         */
        public List<DictItem> items(String groupCode) {
            return getBizDictItems(groupCode);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 验证 Service 方法通过 @DS 切换到后台库。
     */
    public static class RoutingProbe {

        /** 供业务库和后台库共享的测试 Mapper。 */
        private final TaskRoutingProbeMapper mapper;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:08
         * 说明: 注入 MyBatis 创建的代理，查询使用调用方法所在的数据源。
         * @param mapper 读取测试来源标签的 Mapper
         */
        public RoutingProbe(TaskRoutingProbeMapper mapper) {
            this.mapper = mapper;
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:08
         * 说明: 通过后台数据源读取第一条来源标签。
         * @return
         * 返回值: String
         * 返回值说明: 后台库的测试标签
         */
        @DS("zeroAdmin")
        public String adminOrigin() {
            return mapper.selectOrigin(new Page<>(1, 1)).getRecords().get(0);
        }
    }
}
