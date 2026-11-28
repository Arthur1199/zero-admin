package com.zero.config;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.ZeroAiApplication;
import com.zero.base.AiBaseService;
import com.zero.controller.AiBizDictTestController;
import com.zero.domain.DictItem;
import com.zero.intercept.MybatisSqlLogInterceptor;
import com.zero.mapper.AiRoutingProbeMapper;
import com.zero.model.AjaxResult;
import com.zero.util.DictConfigUtil;
import com.zero.util.RedissonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 22:49
 * 说明: 验证任务数据源装配、Redis 可选、数据源路由、Mapper XML 及分页。
 */
class AiInfrastructureTest {

    /** 使用实际启动配置，外部服务由测试覆盖参数隔离。 */
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ZeroAiApplication.class)
            .withPropertyValues("mybatis-plus.mapper-locations=classpath*:mapper/*Mapper.xml",
                    "spring.cloud.nacos.config.enabled=false", "spring.cloud.nacos.discovery.enabled=false",
                    "spring.cloud.discovery.enabled=false", "xxl.job.executor.enabled=false");

    /** 只验证 Redis 自身的装配条件。 */
    private final ApplicationContextRunner redisRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiRedisConfig.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 22:49
     * 说明: 仅配置业务库时，验证任务服务能启动并通过默认数据源查询。
     * @return
     * 返回值: void
     * 返回值说明: 上下文启动成功且内存业务库查询成功
     */
    @Test
    void startsWithBusinessDataSourceOnly() {
        runner.withPropertyValues("spring.datasource.dynamic.primary=zeroBiz",
                        "spring.datasource.dynamic.datasource.zeroBiz.url=jdbc:h2:mem:task_biz_only;MODE=MySQL",
                        "spring.datasource.dynamic.datasource.zeroBiz.username=sa")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(JdbcTemplate.class).queryForObject("SELECT 1", Integer.class))
                            .isEqualTo(1);
                });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 18:37
     * 说明: 未传入字典查询服务时保留参数短路和有效查询的配置缺失提示。
     */
    @Test
    void keepsDictionaryLookupOptional() {
        var caller = new DictionaryCaller();
        assertThat(caller.item("invalid")).isNull();
        assertThatIllegalStateException().isThrownBy(() -> caller.item("task_status.paid"))
                .withMessage("查询业务字典需要启用 zero-ai 业务数据源");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:08
     * 说明: 校验空 Redis 主机，兼容旧部署显式关闭 Redis。
     */
    @Test
    void reportsMissingRedisConnectionProperties() {
        redisRunner.withPropertyValues("spring.data.redis.host=")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasMessageContaining("spring.data.redis.host"));
        redisRunner.withPropertyValues("spring.data.redis.host=127.0.0.1", "zero.ai.redis.enabled=false")
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
            assertThat(context).hasSingleBean(MybatisSqlLogInterceptor.class);
            assertThat(context).hasSingleBean(DictConfigUtil.class);
            var mapper = context.getBean(AiRoutingProbeMapper.class);
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
     * 时间: 2026/11/19 23:15
     * 说明: 验证开发环境记录数据源路由、分页 SQL、SQLID、实际参数、查询结果和耗时。
     * @param output 测试期间捕获的日志输出
     */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void logsExecutedSqlWithParametersAndResultsInDev(CapturedOutput output) throws Exception {
        databaseRunner().withInitializer(context -> context.getEnvironment().setActiveProfiles("dev"))
                .run(context -> {
                    assertThat(context).hasSingleBean(MybatisSqlLogInterceptor.class);
                    var mapper = context.getBean(AiRoutingProbeMapper.class);
                    var page = mapper.selectOrigin(new Page<>(2, 1));
                    assertThat(page.getRecords()).containsExactly("biz-2");
                    assertThat(page.getTotal()).isEqualTo(2);
                    assertThat(context.getBean(DictConfigUtil.class).findActiveItem("task_status", "paid"))
                            .isNotNull();
                    assertThat(context.getBean(RoutingProbe.class).adminOrigin()).isEqualTo("admin-1");

                    String sqlLog = output.getAll();
                    assertThat(sqlLog)
                            .contains("开始执行 SQL数据源 [zeroBiz]，SQLID [com.zero.mapper.AiRoutingProbeMapper.selectOrigin]")
                            .contains("开始执行 SQL数据源 [zeroAdmin]，SQLID [com.zero.mapper.AiRoutingProbeMapper.selectOrigin]")
                            .containsPattern("执行 SQL数据源 \\[zeroBiz\\]，SQLID \\[com\\.zero\\.mapper\\.AiRoutingProbeMapper\\.selectOrigin\\]，耗时=\\d+ms")
                            .containsPattern("(?i)LIMIT\\s+\\?\\s*,\\s*\\?")
                            .contains("开始执行 SQL数据源 [zeroBiz]，SQLID [com.zero.mapper.AiDictItemMapper.selectActiveByCodes]")
                            .contains("g.group_code = ?", "i.item_code = ?")
                            .contains("执行 SQL数据源 [zeroBiz]，SQLID [com.zero.mapper.AiRoutingProbeMapper.selectOrigin]，执行参数=[1, 1]")
                            .contains("执行 SQL数据源 [zeroAdmin]，SQLID [com.zero.mapper.AiRoutingProbeMapper.selectOrigin]，查询结果=[admin-1]")
                            .contains("selectActiveByCodes]，执行参数=[task_status, paid]", "itemCode=paid")
                            .doesNotContain("Mapper 资源", "AiRoutingProbeMapper.xml", "AiDictItemMapper.xml",
                                    "Parameters:");
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
                "zero.ai.redis.connection-pool-size=8", "zero.ai.redis.connection-minimum-idle-size=0"};
        redisRunner.withPropertyValues(redisProperties).run(context -> {
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
     * 说明: 显式指定临时 Redis 端口后，校验字符串及对象读写、键过期和共享限流规则更新。
     */
    @Test
    @EnabledIfSystemProperty(named = "zero.ai.test.redis-port", matches = "[0-9]+")
    void readsAndExpiresKeysOnIsolatedRedis() {
        redisRunner.withPropertyValues("spring.data.redis.host=127.0.0.1",
                "spring.data.redis.password=", "spring.data.redis.port=" + System.getProperty("zero.ai.test.redis-port"))
                .run(context -> {
            var redis = context.getBean(StringRedisTemplate.class);
            var namespace = "zero-ai:test:" + UUID.randomUUID();
            var stringKey = namespace + ":string";
            var objectKey = namespace + ":object";
            var nativeKey = namespace + ":native";
            var rateKey = namespace + ":rate";
            var nativeRedis = context.getBean(RedissonUtil.class);
            var limiter = context.getBean(RedissonClient.class).getRateLimiter(rateKey);
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

                Duration window = Duration.ofMinutes(1);
                assertThat(nativeRedis.tryInitRateLimiter(rateKey, 2, window)).isTrue();
                assertThat(nativeRedis.tryAcquirePermits(rateKey, 2)).isTrue();
                assertThat(nativeRedis.updateRateLimiter(rateKey, 3, window)).isTrue();
                assertThat(nativeRedis.updateRateLimiter(rateKey, 3, window)).isFalse();
                assertThat(nativeRedis.tryAcquirePermits(rateKey, 1)).isTrue();
                assertThat(nativeRedis.tryAcquirePermits(rateKey, 1)).isFalse();

                org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(5))
                        .until(() -> Boolean.FALSE.equals(redis.hasKey(stringKey)));
            } finally {
                redis.delete(stringKey);
                objects.delete(objectKey);
                nativeRedis.remove(nativeKey);
                limiter.delete();
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
     * 时间: 2026/11/19 18:43
     * 说明: 验证测试入口在配置好数据源时可以查询字典。
     */
    @Test
    void exposesDictionaryEndpointsWithoutProfileRestriction() throws Exception {
        databaseRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("test"))
                .run(context -> {
                    var controller = context.getBean(AiBizDictTestController.class);
                    var value = controller.getBizDictValue("task_status.paid");
                    assertThat(value.isSuccess()).isTrue();
                    assertThat(value).containsEntry(AjaxResult.DATA_TAG, "paid");

                    var item = controller.findActiveItem("task_status.paid");
                    assertThat(item.isSuccess()).isTrue();
                    assertThat(item.get(AjaxResult.DATA_TAG)).isInstanceOfSatisfying(DictItem.class,
                            dictItem -> assertThat(dictItem.getItemCode()).isEqualTo("paid"));

                    var items = controller.findActiveItems("task_status");
                    assertThat(items.isSuccess()).isTrue();
                    assertThat(items.get(AjaxResult.DATA_TAG)).asInstanceOf(list(DictItem.class))
                            .extracting(DictItem::getItemCode).containsExactly("pending", "paid");
                });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:59
     * 说明: 在隔离 Redis 验证任务缓存命中，以及共享缓存清理后的回源。
     */
    @Test
    @EnabledIfSystemProperty(named = "zero.ai.test.redis-port", matches = "[0-9]+")
    void reloadsAfterSharedBusinessDictionaryEviction() throws Exception {
        databaseRunner().withBean(DictionaryCaller.class).withPropertyValues(
                "spring.data.redis.host=127.0.0.1",
                "spring.data.redis.port=" + System.getProperty("zero.ai.test.redis-port")).run(context -> {
                    var caller = context.getBean(DictionaryCaller.class);
                    var jdbc = context.getBean(JdbcTemplate.class);
                    var redis = context.getBean(RedissonClient.class);
                    assertThat(caller.valueFromAdminTransaction()).isEqualTo("paid");
                    assertThat(caller.items("task_status")).hasSize(2);
                    jdbc.update("UPDATE biz_dict_item SET item_value = 'updated' WHERE id = 10");
                    assertThat(caller.valueFromAdminTransaction()).isEqualTo("paid");
                    // 模拟 zero-report 在提交后清理同一组固定键。
                    assertThat(redis.getKeys().remainTimeToLive("zero:dict:config:item:task_status:paid")).isEqualTo(-1L);
                    redis.getKeys().delete("zero:dict:config:item:task_status:paid",
                            "zero:dict:config:group:task_status");
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
                .withPropertyValues("spring.datasource.dynamic.primary=zeroBiz", "spring.datasource.dynamic.strict=true",
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
    static class DictionaryCaller extends AiBaseService<AiRoutingProbeMapper, DictItem> {

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
            return getDictValue("task_status.paid");
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
            return getDictItem(key);
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
            return getDictItems(groupCode);
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
        private final AiRoutingProbeMapper mapper;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:08
         * 说明: 注入 MyBatis 创建的代理，查询使用调用方法所在的数据源。
         * @param mapper 读取测试来源标签的 Mapper
         */
        public RoutingProbe(AiRoutingProbeMapper mapper) {
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
