package com.zero.base;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.zero.constant.ReportDataSourceConstants;
import com.zero.domain.DictGroup;
import com.zero.domain.DictItem;
import com.zero.mapper.DictItemMapper;
import com.zero.service.BizDictLookup;
import com.zero.service.impl.DictGroupServiceImpl;
import com.zero.service.impl.DictItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 14:34
 * 说明: 通过两个隔离内存库验证报表父类的字典查询、数据源及事务边界。
 */
class ReportBizDictLookupTest {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 验证单项与分组列表的读取、有效项过滤和排序。
     */
    @Test
    void readsOnlyActiveItemsByGroupAndItemCode() throws Exception {
        dictionaryContext().run(context -> {
            assertThat(context).hasNotFailed();
            var service = context.getBean(DictionaryCaller.class);
            assertThat(service.value(" order_status . paid ")).isEqualTo(" 1 ");
            assertThat(service.value("refund_status.paid")).isEqualTo("2");
            assertThat(service.value("order_status.quote'd")).isEqualTo("bound");
            DictItem item = service.item("order_status.paid");
            assertThat(item).isNotNull();
            assertThat(item.getItemName()).isEqualTo("Paid");
            assertThat(item.getExtraJson()).isEqualTo("{\"source\":\"test\"}");
            assertThat(service.items(" order_status ")).extracting(DictItem::getItemCode)
                    .containsExactly("quote'd", "paid", "packed");
            assertThat(service.items("refund_status")).extracting(DictItem::getItemCode)
                    .containsExactly("paid");
            for (String groupCode : new String[]{null, "", "  ", "disabled_group", "deleted_group", "missing"}) {
                assertThat(service.items(groupCode)).as("字典分组 %s", groupCode).isEmpty();
            }

            for (String key : new String[]{null, "", "  ", "order_status", "a.", ".b", "a.b.c",
                    "order_status.missing", "order_status.disabled", "order_status.deleted",
                    "disabled_group.paid", "deleted_group.paid", "missing.paid"}) {
                assertThat(service.item(key)).as("字典项 %s", key).isNull();
                assertThat(service.value(key)).as("字典值 %s", key).isNull();
            }
            var page = PageHelper.startPage(2, 1);
            try {
                assertThat(service.value("order_status.paid")).isEqualTo(" 1 ");
                assertThat(service.items("order_status")).hasSize(3);
                assertThat(PageHelper.getLocalPage()).isSameAs(page);
            } finally {
                PageHelper.clearPage();
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 查询始终走业务库，挂起并恢复后台库事务，读取已提交的字典值。
     */
    @Test
    void readsCommittedDictionaryFromBusinessDataSource() throws Exception {
        dictionaryContext().run(context -> {
            assertThat(context).hasNotFailed();
            var service = context.getBean(DictionaryCaller.class);
            var jdbc = context.getBean(JdbcTemplate.class);
            var transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            assertThat(service.valueFromAdminTransaction()).isEqualTo(" 1 ");
            transaction.executeWithoutResult(status -> {
                jdbc.update("UPDATE biz_dict_item SET item_value = ? WHERE id = ?", "updated", 10);
                assertThat(service.value("order_status.paid")).isEqualTo(" 1 ");
            });
            assertThat(service.valueFromAdminTransaction()).isEqualTo("updated");
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:30
     * 说明: 使用独立 Redis 和两个应用上下文验证命中、提交失效、回滚及 Redis 不可用时回源。
     */
    @Test
    @EnabledIfSystemProperty(named = "zero.report.test.redis-port", matches = "[0-9]+")
    void cachesDictionaryAcrossInstancesAndInvalidatesCommittedChanges() throws Exception {
        var runner = dictionaryContext()
                .withUserConfiguration(DictGroupServiceImpl.class, DictItemServiceImpl.class)
                .withBean(RedissonClient.class, this::redisClient, bean -> bean.setDestroyMethodName("shutdown"));
        runner.run(writer -> runner.run(reader -> {
            assertThat(writer).hasNotFailed();
            assertThat(reader).hasNotFailed();
            var lookup = reader.getBean(DictionaryCaller.class);
            var jdbc = writer.getBean(JdbcTemplate.class);
            var items = writer.getBean(DictItemServiceImpl.class);
            var groups = writer.getBean(DictGroupServiceImpl.class);
            var transaction = new TransactionTemplate(writer.getBean(PlatformTransactionManager.class));

            assertThat(lookup.value("order_status.paid")).isEqualTo(" 1 ");
            assertThat(lookup.items("order_status")).extracting(DictItem::getItemCode)
                    .containsExactly("quote'd", "paid", "packed");
            assertThat(lookup.item("order_status.missing")).isNull();
            assertThat(lookup.items("new_group")).isEmpty();
            var readerClient = reader.getBean(RedissonClient.class);
            verifyCacheTtls(readerClient);
            jdbc.update("UPDATE biz_dict_item SET item_value = ? WHERE id = 10", "external");
            jdbc.update("UPDATE biz_dict_item SET status = '1', item_code = 'missing' WHERE id = 12");
            assertThat(lookup.value("order_status.paid")).isEqualTo(" 1 ");
            assertThat(lookup.items("order_status")).extracting(DictItem::getItemCode)
                    .containsExactly("quote'd", "paid", "packed");
            assertThat(lookup.item("order_status.missing")).isNull();

            transaction.executeWithoutResult(status -> {
                var item = items.selectItemById(10L);
                item.setItemValue("rolled back");
                assertThat(items.updateItem(item)).isTrue();
                assertThat(lookup.value("order_status.paid")).isEqualTo(" 1 ");
                status.setRollbackOnly();
            });
            assertThat(lookup.value("order_status.paid")).isEqualTo(" 1 ");
            transaction.executeWithoutResult(status -> {
                var item = items.selectItemById(10L);
                item.setItemValue("committed");
                assertThat(items.updateItem(item)).isTrue();
                assertThat(lookup.value("order_status.paid")).isEqualTo(" 1 ");
            });
            assertThat(lookup.value("order_status.paid")).isEqualTo("committed");
            assertThat(lookup.value("order_status.missing")).isEqualTo("3");
            assertThat(lookup.item("order_status.paid").getUpdateTime()).isNotNull();

            DictGroup group = new DictGroup();
            group.setGroupCode("new_group");
            group.setGroupName("New group");
            assertThat(groups.insertGroup(group)).isTrue();
            assertThat(lookup.items("new_group")).isEmpty();
            DictItem inserted = new DictItem();
            inserted.setGroupId(group.getId());
            inserted.setItemCode("new_item");
            inserted.setItemName("New item");
            inserted.setItemValue("new value");
            assertThat(items.insertItem(inserted)).isTrue();
            assertThat(lookup.items("new_group")).extracting(DictItem::getItemCode).containsExactly("new_item");

            group.setGroupCode("renamed_group");
            assertThat(groups.updateGroup(group)).isTrue();
            assertThat(lookup.items("new_group")).isEmpty();
            assertThat(lookup.value("renamed_group.new_item")).isEqualTo("new value");

            var moved = items.selectItemById(10L);
            moved.setGroupId(group.getId());
            assertThat(items.updateItem(moved)).isTrue();
            assertThat(lookup.item("order_status.paid")).isNull();
            assertThat(lookup.value("renamed_group.paid")).isEqualTo("committed");
            moved.setStatus("0");
            assertThat(items.updateItem(moved)).isTrue();
            assertThat(lookup.item("renamed_group.paid")).isNull();
            moved.setStatus("1");
            assertThat(items.updateItem(moved)).isTrue();
            assertThat(lookup.value("renamed_group.paid")).isEqualTo("committed");
            assertThat(items.deleteItemByIds(new Long[]{10L, inserted.getId()})).isTrue();
            assertThat(lookup.items("renamed_group")).isEmpty();
            assertThat(items.restoreItemByIds(new Long[]{10L, inserted.getId()})).isTrue();
            assertThat(lookup.items("renamed_group")).hasSize(2);
            group.setStatus("0");
            assertThat(groups.updateGroup(group)).isTrue();
            assertThat(lookup.items("renamed_group")).isEmpty();
            group.setStatus("1");
            assertThat(groups.updateGroup(group)).isTrue();
            assertThat(lookup.items("renamed_group")).hasSize(2);
            assertThat(groups.deleteGroupByIds(new Long[]{group.getId()})).isTrue();
            assertThat(lookup.items("renamed_group")).isEmpty();
            assertThat(groups.restoreGroupByIds(new Long[]{group.getId()})).isTrue();
            assertThat(lookup.items("renamed_group")).hasSize(2);

            verifyLateFill(reader.getBean(DictItemMapper.class), items, writer.getBean(RedissonClient.class),
                    reader.getBean(BizDictLookup.class), group.getId());
            readerClient.shutdown();
            writer.getBean(RedissonClient.class).shutdown();
            var changedWithoutRedis = items.selectItemById(10L);
            changedWithoutRedis.setItemValue("db fallback");
            assertThat(items.updateItem(changedWithoutRedis)).isTrue();
            assertThat(lookup.value("renamed_group.paid")).isEqualTo("db fallback");
        }));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:31
     * 说明: 检查独立 Redis 中单项和列表的正常结果与空结果均设置了预期有效期。
     * @param client 测试实例的 Redisson 客户端
     */
    private void verifyCacheTtls(RedissonClient client) {
        var encoder = Base64.getUrlEncoder().withoutPadding();
        Function<String, String> encode = value -> encoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
        String prefix = "zero:report:biz-dict:entry:*:";
        String groupCode = encode.apply("order_status");
        var keys = client.getKeys();
        assertThat(keys.getKeysByPattern(prefix + "item:" + groupCode + ":" + encode.apply("paid")))
                .singleElement().satisfies(key -> assertThat(keys.remainTimeToLive(key)).isBetween(1L, 60_000L));
        assertThat(keys.getKeysByPattern(prefix + "item:" + groupCode + ":" + encode.apply("missing")))
                .singleElement().satisfies(key -> assertThat(keys.remainTimeToLive(key)).isBetween(1L, 15_000L));
        assertThat(keys.getKeysByPattern(prefix + "group:" + groupCode))
                .singleElement().satisfies(key -> assertThat(keys.remainTimeToLive(key)).isBetween(1L, 60_000L));
        assertThat(keys.getKeysByPattern(prefix + "group:" + encode.apply("new_group")))
                .singleElement().satisfies(key -> assertThat(keys.remainTimeToLive(key)).isBetween(1L, 15_000L));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:30
     * 说明: 暂停已取得旧值的回源请求，提交更新后再放行，验证该旧值不能覆盖新版本缓存。
     * @param mapper 真实字典查询 Mapper
     * @param items 负责写入的字典项 Service
     * @param client 第一个实例的 Redisson 客户端
     * @param reader 带事务和数据源代理的第二个实例查询服务
     * @param groupId 并发测试时写入的字典组
     */
    private void verifyLateFill(DictItemMapper mapper, DictItemServiceImpl items, RedissonClient client,
                                BizDictLookup reader, Long groupId)
            throws Exception {
        var valuesRead = new CountDownLatch(1);
        var allowFill = new CountDownLatch(1);
        var delayedMapper = mock(DictItemMapper.class);
        when(delayedMapper.selectActiveByCodes("renamed_group", "paid")).thenAnswer(invocation -> {
            var old = mapper.selectActiveByCodes("renamed_group", "paid");
            valuesRead.countDown();
            assertThat(allowFill.await(10, TimeUnit.SECONDS)).isTrue();
            return old;
        });
        var factory = new StaticListableBeanFactory(Map.of("redis", client));
        var delayedLookup = new BizDictLookup(delayedMapper, factory.getBeanProvider(RedissonClient.class));
        var delayed = CompletableFuture.supplyAsync(() -> delayedLookup.findActiveItem("renamed_group", "paid"));
        try {
            assertThat(valuesRead.await(10, TimeUnit.SECONDS)).isTrue();
            var changed = items.selectItemById(10L);
            assertThat(changed.getGroupId()).isEqualTo(groupId);
            changed.setItemValue("latest");
            assertThat(items.updateItem(changed)).isTrue();
            assertThat(reader.findActiveItem("renamed_group", "paid").getItemValue()).isEqualTo("latest");
        } finally {
            allowFill.countDown();
        }
        assertThat(delayed.get(10, TimeUnit.SECONDS).getItemValue()).isEqualTo("committed");
        assertThat(reader.findActiveItem("renamed_group", "paid").getItemValue()).isEqualTo("latest");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 15:30
     * 说明: 只连接测试启动的本地独立 Redis。
     * @return 使用独立连接池的 Redisson 客户端
     * 返回值: RedissonClient
     * 返回值说明: 由测试应用上下文关闭
     */
    private RedissonClient redisClient() {
        var config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:" + System.getProperty("zero.report.test.redis-port"))
                .setConnectionMinimumIdleSize(1).setConnectionPoolSize(4);
        return Redisson.create(config);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 装配真实 MyBatis Mapper、动态路由和事务，业务字典只存在于业务库。
     * @return
     * 返回值: ApplicationContextRunner
     * 返回值说明: 只访问当前测试的 H2 内存库
     */
    private ApplicationContextRunner dictionaryContext() throws Exception {
        String id = UUID.randomUUID().toString();
        String biz = "jdbc:h2:mem:report_biz_" + id + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        String admin = "jdbc:h2:mem:report_admin_" + id + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(admin, "sa", "");
             var sql = connection.createStatement()) {
            sql.execute("CREATE TABLE admin_probe (id INT PRIMARY KEY)");
            sql.execute("INSERT INTO admin_probe VALUES (1)");
        }
        try (var connection = DriverManager.getConnection(biz, "sa", "");
             var sql = connection.createStatement()) {
            sql.execute("""
                    CREATE TABLE biz_dict_group (
                        id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 100) PRIMARY KEY,
                        group_code VARCHAR(100) UNIQUE, group_name VARCHAR(100), status CHAR(1), del_flag CHAR(1),
                        remark VARCHAR(500), create_by VARCHAR(64), create_time TIMESTAMP,
                        update_by VARCHAR(64), update_time TIMESTAMP
                    )
                    """);
            sql.execute("""
                    CREATE TABLE biz_dict_item (
                        id BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 100) PRIMARY KEY,
                        group_id BIGINT, item_code VARCHAR(100),
                        item_name VARCHAR(100), item_value VARCHAR(500), extra_json VARCHAR(500),
                        item_sort INT NOT NULL DEFAULT 20, status CHAR(1), del_flag CHAR(1), remark VARCHAR(500),
                        create_by VARCHAR(64), create_time TIMESTAMP, update_by VARCHAR(64), update_time TIMESTAMP,
                        UNIQUE (group_id, item_code)
                    )
                    """);
            sql.execute("""
                    INSERT INTO biz_dict_group (id, group_code, status, del_flag) VALUES
                        (1, 'order_status', '1', '0'), (2, 'refund_status', '1', '0'),
                        (3, 'disabled_group', '0', '0'), (4, 'deleted_group', '1', '1')
                    """);
            sql.execute("""
                    INSERT INTO biz_dict_item
                        (id, group_id, item_code, item_name, item_value, extra_json, status, del_flag)
                    VALUES
                        (10, 1, 'paid', 'Paid', ' 1 ', '{"source":"test"}', '1', '0'),
                        (11, 2, 'paid', 'Paid', '2', NULL, '1', '0'),
                        (12, 1, 'disabled', 'Disabled', '3', NULL, '0', '0'),
                        (13, 1, 'deleted', 'Deleted', '4', NULL, '1', '1'),
                        (14, 3, 'paid', 'Paid', '5', NULL, '1', '0'),
                        (15, 4, 'paid', 'Paid', '6', NULL, '1', '0'),
                        (17, 1, 'packed', 'Packed', '7', NULL, '1', '0')
                    """);
            try (var insert = connection.prepareStatement("""
                    INSERT INTO biz_dict_item (id, group_id, item_code, item_value, item_sort, status, del_flag)
                    VALUES (16, 1, ?, 'bound', 10, '1', '0')
                    """)) {
                insert.setString(1, "quote'd");
                insert.executeUpdate();
            }
        }
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class,
                        DynamicDataSourceAutoConfiguration.class, MybatisPlusAutoConfiguration.class,
                        PageHelperAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
                        DataSourceTransactionManagerAutoConfiguration.class,
                        TransactionAutoConfiguration.class))
                .withUserConfiguration(MapperConfiguration.class, BizDictLookup.class, DictionaryCaller.class)
                .withPropertyValues("mybatis-plus.mapper-locations=classpath*:mapper/**/*Mapper.xml",
                        "mybatis-plus.global-config.banner=false",
                        "pagehelper.helper-dialect=mysql",
                        "spring.datasource.dynamic.primary=zeroBiz", "spring.datasource.dynamic.strict=true",
                        "spring.datasource.dynamic.druid.validation-query=SELECT 1",
                        "spring.datasource.dynamic.datasource.zeroBiz.url=" + biz,
                        "spring.datasource.dynamic.datasource.zeroBiz.username=sa",
                        "spring.datasource.dynamic.datasource.zeroAdmin.url=" + admin,
                        "spring.datasource.dynamic.datasource.zeroAdmin.username=sa");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 加载报表模块现有 Mapper 接口和 XML。
     */
    @Configuration(proxyBeanMethods = false)
    @MapperScan(basePackageClasses = DictItemMapper.class)
    static class MapperConfiguration {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/19 14:34
     * 说明: 通过正常的 Service 子类调用父类字典方法，覆盖后台库事务场景。
     */
    @lombok.RequiredArgsConstructor
    static class DictionaryCaller extends ReportBaseService<DictItemMapper, DictItem> {

        /** 用于确认查询前后后台库事务仍绑定原数据源。 */
        private final JdbcTemplate jdbc;

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:34
         * 说明: 读取完整字典项。
         * @param key 分组编码.字典项编码
         * @return 完整字典项，无记录时为空
         */
        DictItem item(String key) {
            return getBizDictItem(key);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:41
         * 说明: 调用父类读取分组内的全部有效字典项。
         * @param groupCode 字典分组编码
         * @return
         * 返回值: List<DictItem>
         * 返回值说明: 分组内的有效字典项列表
         */
        List<DictItem> items(String groupCode) {
            return getBizDictItems(groupCode);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:34
         * 说明: 读取业务字典的字符串值。
         * @param key 分组编码.字典项编码
         * @return 原始字符串值，无记录时为空
         */
        String value(String key) {
            return getBizDictValue(key);
        }

        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/19 14:34
         * 说明: 在后台库事务中访问业务字典，调用完成后恢复后台库事务。
         * @return 业务库中的字典值
         */
        @DS(ReportDataSourceConstants.ADMIN)
        @Transactional
        public String valueFromAdminTransaction() {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM admin_probe", Integer.class)).isEqualTo(1);
            String value = getBizDictValue("order_status.paid");
            assertThat(getBizDictItems("order_status")).hasSize(3);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM admin_probe", Integer.class)).isEqualTo(1);
            return value;
        }
    }
}
