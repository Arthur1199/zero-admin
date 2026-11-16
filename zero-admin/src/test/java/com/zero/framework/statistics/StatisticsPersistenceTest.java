package com.zero.framework.statistics;

import com.alibaba.fastjson2.JSON;
import com.zero.common.core.redis.RedisCache;
import com.zero.system.domain.statistics.*;
import com.zero.system.mapper.StatisticsMapper;
import com.zero.system.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 显式隔离MySQL与Redis集成回归，验证真实SQL、重投、事务回滚与消费接管。
 */
@EnabledIfEnvironmentVariable(named="ZERO_STAT_TEST_MYSQL_URL",matches=".*zero_stat_test.*")
class StatisticsPersistenceTest
{
    /** 隔离测试连接，不读取项目数据库配置。 */
    private static DriverManagerDataSource dataSource;
    /** 隔离库SQL访问。 */
    private static JdbcTemplate jdbc;
    /** 真实MyBatis Mapper。 */
    private static StatisticsMapper mapper;
    /** 真实后台参数Mapper，用于验证初始化参数与缓存联动。 */
    private static com.zero.system.mapper.SysConfigMapper configMapper;
    /** 启用事务代理的汇总服务。 */
    private static StatisticsStore store;
    /** 隔离Redis连接。 */
    private static LettuceConnectionFactory connection;
    /** 隔离字符串Redis模板。 */
    private static StringRedisTemplate redis;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 仅在显式指定隔离库时安装测试表和真实Mapper，不读取生产配置。
     * @return 初始化隔离存储
     * 返回值: void
     * 返回值说明: 初始化隔离存储
     */
    @BeforeAll static void setup() throws Exception
    {
        // 部分Windows JDK17环境的本地域套接字异常；仅显式测试参数下切换测试JVM的Pipe实现。
        if (Boolean.getBoolean("zero.statistics.test.forceTcpPipe"))
        {
            var field=Class.forName("sun.nio.ch.PipeImpl").getDeclaredField("noUnixDomainSockets");
            field.setAccessible(true); field.setBoolean(null,true);
        }
        String url=System.getenv("ZERO_STAT_TEST_MYSQL_URL");
        java.net.URI address=java.net.URI.create(url.substring(5));
        if (!Set.of("127.0.0.1","localhost").contains(address.getHost()) || !address.getPath().matches("/zero_stat_test(?:_[a-zA-Z0-9]+)?"))
            throw new IllegalArgumentException("持久化测试仅允许本机独立zero_stat_test库");
        dataSource=new DriverManagerDataSource(url,"root","");
        jdbc=new JdbcTemplate(dataSource);
        String init=Files.readString(Path.of("../sql/zero_admin.sql"));
        int start=init.indexOf("create table sys_menu ("); int end=init.indexOf(";",start);
        String menu=init.substring(start,end).replace("create table sys_menu","create table if not exists sys_menu"); jdbc.execute(menu);
        int configStart=init.indexOf("create table sys_config (");
        jdbc.execute(init.substring(configStart,init.indexOf(";",configStart)).replace("create table sys_config","create table if not exists sys_config"));
        jdbc.update("INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,path,menu_type) VALUES(2,'系统监控',0,'monitor','M'),(121,'管理后台',2,'admin','M')");
        ResourceDatabasePopulator scripts=new ResourceDatabasePopulator(new ByteArrayResource(init.substring(init.indexOf("-- 运行统计模块初始化")).getBytes(StandardCharsets.UTF_8)));
        scripts.setSqlScriptEncoding("UTF-8"); scripts.execute(dataSource);
        // 只读取初始化文件中的两条原始INSERT，在隔离库首次安装测试数据。
        for (String line : init.lines().filter(row -> row.toLowerCase(Locale.ROOT).startsWith("insert into")
                && (row.contains("'monitor:statistics:list'") || row.contains("'sys.statistics.enabled'"))).toList())
        {
            boolean configRow = line.contains("'sys.statistics.enabled'");
            int count = configRow
                    ? jdbc.queryForObject("SELECT COUNT(*) FROM sys_config WHERE config_key='sys.statistics.enabled'", Integer.class)
                    : jdbc.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE perms='monitor:statistics:list'", Integer.class);
            if (count == 0) jdbc.execute(line);
        }
        SqlSessionFactoryBean factory=new SqlSessionFactoryBean(); factory.setDataSource(dataSource);
        factory.setTypeAliases(new Class<?>[]{com.zero.system.domain.SysConfig.class});
        factory.setMapperLocations(new ClassPathResource("mapper/system/StatisticsMapper.xml"),new ClassPathResource("mapper/system/SysConfigMapper.xml"));
        mapper=new SqlSessionTemplate(factory.getObject()).getMapper(StatisticsMapper.class);
        configMapper=new SqlSessionTemplate(factory.getObject()).getMapper(com.zero.system.mapper.SysConfigMapper.class);
        TransactionInterceptor interceptor=new TransactionInterceptor(new DataSourceTransactionManager(dataSource),new AnnotationTransactionAttributeSource());
        ProxyFactory proxy=new ProxyFactory(new StatisticsStore(mapper)); proxy.setProxyTargetClass(true); proxy.addAdvice(interceptor); store=(StatisticsStore)proxy.getProxy();
        connection=new LettuceConnectionFactory("127.0.0.1",Integer.parseInt(System.getenv().getOrDefault("ZERO_STAT_TEST_REDIS_PORT","13379"))); connection.afterPropertiesSet();
        redis=new StringRedisTemplate(connection); redis.afterPropertiesSet();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 释放测试连接，不删除任何表和容器。
     * @return 关闭测试Redis连接
     * 返回值: void
     * 返回值说明: 关闭测试Redis连接
     */
    @AfterAll static void close() { if(connection!=null) connection.destroy(); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 创建唯一接口事件，统计按北京时间完成日期归属。
     * @param day 测试完成日期
     * @return 无敏感信息的测试事件
     * 返回值: StatEvent
     * 返回值说明: 无敏感信息的测试事件
     */
    private StatEvent event(String day)
    {
        StatEvent event=new StatEvent(); event.setEventId(UUID.randomUUID().toString()); event.setRoute("/test/"+event.getEventId()+"/{id}");
        event.setMethod("GET"); event.setName("test.method"); event.setModule("test"); event.setUserId(2L);
        event.setOutcome("SUCCESS"); event.setDurationUs(1200500); event.setSlow(true);
        event.setCompletedAt(LocalDate.parse(day).atStartOfDay().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()); return event;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 创建未启动调度的隔离事件管道。
     * @param p 独立Stream配置
     * @return 手动驱动的测试管道
     * 返回值: StatisticsPipeline
     * 返回值说明: 手动驱动的测试管道
     */
    private StatisticsPipeline pipeline(StatisticsProperties p)
    { return new StatisticsPipeline(p,redis,mock(RedisCache.class),store,mapper,StatisticsTestSwitch.enabled()); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证重复事件、真实查询、加权耗时、用户去重及初始化菜单。
     * @return 所有汇总只累计一次
     * 返回值: void
     * 返回值说明: 所有汇总只累计一次
     */
    @Test void duplicateAndSqlQueries()
    {
        StatEvent event=event("2031-12-01"); event.setLogin(true); event.setLoginSuccess(true); event.setMethod("POST");
        store.fallback(event); store.accept(event); store.accept(event);
        StatisticsQueryService service=new StatisticsQueryService(mapper,mock(StatisticsPipeline.class));
        StatisticsQuery q=new StatisticsQuery(); q.setBeginTime("2031-12-01");q.setEndTime("2031-12-01");q.setKeyword(event.getEventId());
        Map<String,Object> row=mapper.list(service.parameters(q)).get(0);
        assertThat(((Number)row.get("total")).longValue()).isEqualTo(1); assertThat(((Number)row.get("avgMs")).doubleValue()).isEqualTo(1200.5);
        assertThat(jdbc.queryForObject("SELECT processed FROM sys_stat_event_inbox WHERE event_id=?",Integer.class,event.getEventId())).isEqualTo(1);
        assertThat(mapper.overview(service.parameters(q))).containsKey("loginAttempts");
        assertThat(mapper.trend(service.parameters(q))).isNotEmpty(); assertThat(mapper.distribution(service.parameters(q))).isNotEmpty();
        q.setEndTime("2031-12-20"); assertThat(mapper.trend(service.parameters(q))).isNotEmpty();
        assertThat(mapper.countEndpoints(service.parameters(q))).isEqualTo(1);
        q.setRanking("failed"); assertThat(mapper.list(service.parameters(q))).isEmpty();
        q.setRanking("slow"); assertThat(mapper.list(service.parameters(q))).isEmpty();
        StatEvent second=JSON.parseObject(event.getPayload(),StatEvent.class); second.setEventId(UUID.randomUUID().toString());
        second.setDurationUs(399500); second.setUserId(3L); second.setLogin(false); second.setLoginSuccess(false); second.setCompletedAt(event.getCompletedAt()+60000);
        store.accept(second); q.setRanking("calls");
        Map<String,Object> weighted=mapper.list(service.parameters(q)).get(0);
        assertThat(((Number)weighted.get("total")).longValue()).isEqualTo(2);
        assertThat(((Number)weighted.get("avgMs")).doubleValue()).isEqualTo(800.0);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE perms='monitor:statistics:list'",Integer.class)).isEqualTo(1);
        assertThat(mapper.health()).containsKey("lastConsumedAt");
        assertThat(mapper.health().get("lastConsumedAt")).isInstanceOf(String.class);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证汇总失败时去重登记回滚，修复事件后仍能重试。
     * @return 数据库事务回滚全部统计写入
     * 返回值: void
     * 返回值说明: 数据库事务回滚全部统计写入
     */
    @Test void transactionRollback()
    {
        StatEvent event=event("2031-12-02");
        StatisticsMapper broken = spy(mapper);
        doThrow(new IllegalStateException("isolated summary failure")).when(broken).apiDay(anyMap());
        TransactionInterceptor interceptor = new TransactionInterceptor(new DataSourceTransactionManager(dataSource),new AnnotationTransactionAttributeSource());
        ProxyFactory proxy = new ProxyFactory(new StatisticsStore(broken)); proxy.setProxyTargetClass(true); proxy.addAdvice(interceptor);
        StatisticsStore failingStore = (StatisticsStore) proxy.getProxy();
        assertThatThrownBy(()->failingStore.accept(event)).isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_stat_api_minute WHERE endpoint_id=?",Integer.class,StatisticsStore.endpointId(event.getMethod(),event.getRoute()))).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_stat_event_consumed WHERE event_id=?",Integer.class,event.getEventId())).isZero();
        store.accept(event);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_stat_event_consumed WHERE event_id=?",Integer.class,event.getEventId())).isEqualTo(1);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 模拟旧实例入库后未确认即停止，新实例接管pending并保持只累计一次。
     * @return 验证真实Stream消费与崩溃恢复
     * 返回值: void
     * 返回值说明: 验证真实Stream消费与崩溃恢复
     */
    @Test void pendingRecoveryAfterCommit()
    {
        StatisticsProperties p=new StatisticsProperties(); p.setStreamKey("stat-test:"+UUID.randomUUID());
        StatisticsPipeline old=pipeline(p); old.consume();
        StatEvent event=event("2031-12-03"); old.publish(event);
        var read=redis.opsForStream().read(Consumer.from(p.getGroup(),"crashed"),StreamReadOptions.empty().count(1),StreamOffset.create(p.getStreamKey(),ReadOffset.lastConsumed()));
        assertThat(read).hasSize(1); store.accept(event); // 模拟此处进程崩溃，未发送ACK。
        String id=read.get(0).getId().getValue();
        redis.execute((RedisCallback<Object>) c->c.execute("XCLAIM",p.getStreamKey().getBytes(StandardCharsets.UTF_8),p.getGroup().getBytes(StandardCharsets.UTF_8),"crashed".getBytes(StandardCharsets.UTF_8),"0".getBytes(StandardCharsets.UTF_8),id.getBytes(StandardCharsets.UTF_8),"IDLE".getBytes(StandardCharsets.UTF_8),"65000".getBytes(StandardCharsets.UTF_8)));
        old.stop(); StatisticsPipeline restarted=pipeline(p);
        try
        {
            restarted.consume();
            assertThat(redis.opsForStream().pending(p.getStreamKey(),p.getGroup()).getTotalPendingMessages()).isZero();
            assertThat(jdbc.queryForObject("SELECT total_count FROM sys_stat_api_day WHERE endpoint_id=?",Long.class,StatisticsStore.endpointId(event.getMethod(),event.getRoute()))).isEqualTo(1);
            // 新实例还可继续处理新消息。
            StatEvent next=event("2031-12-03"); restarted.publish(next); restarted.consume();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_stat_event_consumed WHERE event_id=?",Integer.class,next.getEventId())).isEqualTo(1);
        }
        finally { restarted.stop(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证Redis故障回退MySQL，以及双通道失败不改变业务调用。
     * @return 保留回退事件并报告缺口
     * 返回值: void
     * 返回值说明: 保留回退事件并报告缺口
     */
    @Test void fallbackAndUnavailable()
    {
        StatisticsProperties p=new StatisticsProperties();
        StringRedisTemplate broken=mock(StringRedisTemplate.class);
        StatisticsPipeline pipeline=new StatisticsPipeline(p,broken,mock(RedisCache.class),store,mapper,StatisticsTestSwitch.enabled());
        StatEvent event=event("2031-12-04"); pipeline.publish(event);
        assertThat(jdbc.queryForObject("SELECT processed FROM sys_stat_event_inbox WHERE event_id=?",Integer.class,event.getEventId())).isZero();
        pipeline.consume();
        assertThat(jdbc.queryForObject("SELECT processed FROM sys_stat_event_inbox WHERE event_id=?",Integer.class,event.getEventId())).isEqualTo(1); pipeline.stop();
        StatisticsStore unavailable=mock(StatisticsStore.class); doThrow(new IllegalStateException()).when(unavailable).fallback(any());
        StatisticsPipeline both=new StatisticsPipeline(p,broken,mock(RedisCache.class),unavailable,mapper,StatisticsTestSwitch.enabled());
        assertThatCode(()->both.publish(event)).doesNotThrowAnyException(); assertThat(both.status()).containsEntry("lostSinceStart",1L); both.stop();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 验证在线按用户去重、会话分别计数，扫描遇到失效缓存时跳过且不续期。
     * @return 保存真实在线快照
     * 返回值: void
     * 返回值说明: 保存真实在线快照
     */
    @Test void onlineSampling()
    {
        StatisticsProperties p=new StatisticsProperties(); p.setStreamKey("stat-test:"+UUID.randomUUID());
        RedisCache cache=mock(RedisCache.class);
        var account=new com.zero.common.core.domain.entity.SysUser(); account.setUserId(2L);
        var user=new com.zero.common.core.domain.model.LoginUser(2L,1L,account,Set.of()); user.setExpireTime(System.currentTimeMillis()+60000);
        String prefix=com.zero.common.constant.CacheConstants.LOGIN_TOKEN_KEY+UUID.randomUUID();
        redis.opsForValue().set(prefix+"-a","placeholder",Duration.ofSeconds(30)); redis.opsForValue().set(prefix+"-b","placeholder",Duration.ofSeconds(30));
        redis.opsForValue().set(prefix+"-expired","placeholder",Duration.ofSeconds(30));
        when(cache.getCacheObject(prefix+"-a")).thenReturn(user); when(cache.getCacheObject(prefix+"-b")).thenReturn(user);
        StatisticsPipeline pipe=new StatisticsPipeline(p,redis,cache,store,mapper,StatisticsTestSwitch.enabled());
        try
        {
            StatEvent event=event(LocalDate.now(ZoneId.of("Asia/Shanghai")).toString()); event.setCompletedAt(System.currentTimeMillis()); pipe.publish(event); pipe.consume(); pipe.sample();
            Map<String,Object> health=mapper.health(); assertThat(((Number)health.get("onlineUsers")).longValue()).isEqualTo(1);
            assertThat(((Number)health.get("onlineSessions")).longValue()).isEqualTo(2); assertThat(((Number)health.get("recentActive")).longValue()).isEqualTo(1);
            verify(cache,never()).setCacheObject(anyString(),any());
        }
        finally { pipe.stop(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 活跃人数查询失败不得阻塞主事件确认，查询恢复后无需新请求即可重新采样。
     * @return
     * 返回值: void
     * 返回值说明: 验证持久化活动数据可恢复且故障不被误报为零
     */
    @Test void activeQueryRecoversWithoutNewRequests()
    {
        StatisticsProperties p=new StatisticsProperties(); p.setStreamKey("stat-test:"+UUID.randomUUID());
        StatisticsMapper faulty=spy(mapper);
        doThrow(new IllegalStateException("isolated active failure")).when(faulty).countRecentActive(anyMap());
        StatisticsPipeline pipe=new StatisticsPipeline(p,redis,mock(RedisCache.class),store,faulty,StatisticsTestSwitch.enabled());
        try
        {
            StatEvent event=event(LocalDate.now(ZoneId.of("Asia/Shanghai")).toString()); event.setCompletedAt(System.currentTimeMillis());
            pipe.publish(event); pipe.consume(); pipe.sample();
            assertThat(redis.opsForStream().pending(p.getStreamKey(),p.getGroup()).getTotalPendingMessages()).isZero();
            assertThat(pipe.status()).containsEntry("activeIndexAvailable",false);
            doAnswer(call -> mapper.countRecentActive(call.getArgument(0))).when(faulty).countRecentActive(anyMap());
            pipe.sample();
            assertThat(pipe.status()).containsEntry("activeIndexAvailable",true);
            assertThat(((Number)mapper.health().get("recentActive")).longValue()).isGreaterThanOrEqualTo(1);
        }
        finally { pipe.stop(); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 21:34
     * 说明: 使用统一初始化SQL中的参数和真实MySQL Mapper验证后台修改开关，无须重启。
     * @return
     * 返回值: void
     * 返回值说明: 从统一初始化文件读取参数，后台修改同步参数缓存
     */
    @Test void initializedDatabaseParameterControlsSwitch()
    {
        String key = com.zero.system.service.IStatisticsSwitchService.CONFIG_KEY;
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_config WHERE config_key=?", Integer.class, key)).isEqualTo(1);
        com.zero.system.domain.SysConfig query = new com.zero.system.domain.SysConfig(); query.setConfigKey(key);
        com.zero.system.domain.SysConfig saved = configMapper.selectConfig(query);
        String original = saved.getConfigValue();
        RedisCache cache = mock(RedisCache.class);
        Map<String,Object> values = new java.util.concurrent.ConcurrentHashMap<>();
        when(cache.getCacheObject(anyString())).thenAnswer(call -> values.get(call.getArgument(0)));
        doAnswer(call -> { values.put(call.getArgument(0), call.getArgument(1)); return null; }).when(cache).setCacheObject(anyString(), any());
        SysConfigServiceImpl configService = new SysConfigServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(configService,"configMapper",configMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(configService,"redisCache",cache);
        StatisticsSwitchServiceImpl control = new StatisticsSwitchServiceImpl(configService,configMapper);
        try
        {
            saved.setConfigValue("true"); configService.updateConfig(saved); assertThat(control.isEnabled()).isTrue();
            saved.setConfigValue("false"); configService.updateConfig(saved); assertThat(control.isEnabled()).isFalse();
            assertThat(jdbc.queryForObject("SELECT config_value FROM sys_config WHERE config_key=?",String.class,key)).isEqualTo("false");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_config WHERE config_key=?",Integer.class,key)).isEqualTo(1);
        }
        finally { saved.setConfigValue(original); configService.updateConfig(saved); }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 首批坏事件保留时，后续回退事件仍能在下一轮被读取和消费。
     * @return
     * 返回值: void
     * 返回值说明: 验证MySQL游标轮转不删除失败记录、不阻塞后续事件
     */
    @Test void inboxPoisonDoesNotStarveLaterEvents()
    {
        String badId=UUID.randomUUID().toString();
        jdbc.update("INSERT INTO sys_stat_event_inbox(event_id,payload,created_at) VALUES(?,?,'2000-01-01 00:00:00')",badId,"broken-json");
        StatEvent good=event("2032-02-01"); store.fallback(good);
        StatisticsProperties p=new StatisticsProperties(); p.setBatchSize(1);
        StatisticsPipeline pipe=new StatisticsPipeline(p,mock(StringRedisTemplate.class),mock(RedisCache.class),store,mapper,StatisticsTestSwitch.enabled());
        try
        {
            pipe.consume(); pipe.consume();
            assertThat(jdbc.queryForObject("SELECT processed FROM sys_stat_event_inbox WHERE event_id=?",Integer.class,badId)).isZero();
            assertThat(jdbc.queryForObject("SELECT processed FROM sys_stat_event_inbox WHERE event_id=?",Integer.class,good.getEventId())).isEqualTo(1);
        }
        finally { pipe.stop(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 待确认队列的首个坏消息不能持续遮挡后续可接管的正常消息。
     * @return
     * 返回值: void
     * 返回值说明: 下一轮恢复正常消息，坏消息继续保留pending
     */
    @Test void pendingPoisonDoesNotStarveLaterEvents()
    {
        StatisticsProperties p=new StatisticsProperties(); p.setStreamKey("stat-test:"+UUID.randomUUID()); p.setBatchSize(1);
        StatisticsPipeline pipe=pipeline(p);
        try
        {
            pipe.consume();
            redis.opsForStream().add(p.getStreamKey(),Map.of("payload","broken-json"));
            StatEvent good=event("2032-02-02"); pipe.publish(good);
            var records=redis.opsForStream().read(Consumer.from(p.getGroup(),"crashed"),StreamReadOptions.empty().count(2),StreamOffset.create(p.getStreamKey(),ReadOffset.lastConsumed()));
            for (var record:records)
            {
                redis.execute((RedisCallback<Object>) connection -> connection.execute("XCLAIM",p.getStreamKey().getBytes(StandardCharsets.UTF_8),p.getGroup().getBytes(StandardCharsets.UTF_8),"crashed".getBytes(StandardCharsets.UTF_8),"0".getBytes(StandardCharsets.UTF_8),record.getId().getValue().getBytes(StandardCharsets.UTF_8),"IDLE".getBytes(StandardCharsets.UTF_8),"65000".getBytes(StandardCharsets.UTF_8)));
            }
            pipe.consume(); pipe.consume();
            assertThat(redis.opsForStream().pending(p.getStreamKey(),p.getGroup()).getTotalPendingMessages()).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_stat_event_consumed WHERE event_id=?",Integer.class,good.getEventId())).isEqualTo(1);
        }
        finally { pipe.stop(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 近期活跃跨零点按用户去重，只包含五分钟窗口内且不晚于采样时间的活动。
     * @return
     * 返回值: void
     * 返回值说明: 验证半开起点、跨日去重和未来记录排除
     */
    @Test void activeUsersAcrossMidnight()
    {
        String[] moments={"2032-01-01T23:58:00","2032-01-02T00:01:00","2032-01-01T23:55:00","2032-01-02T00:03:00","2032-01-01T23:57:00"};
        long[] users={200L,200L,201L,202L,203L};
        for(int index=0;index<moments.length;index++)
        {
            StatEvent event=event("2032-01-02"); event.setUserId(users[index]);
            event.setCompletedAt(LocalDateTime.parse(moments[index]).atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()); store.accept(event);
        }
        LocalDateTime end=LocalDateTime.parse("2032-01-02T00:02:00"), begin=end.minusMinutes(5);
        assertThat(mapper.countRecentActive(Map.of("beginDay",begin.toLocalDate(),"endDay",end.toLocalDate(),"beginAt",begin,"endAt",end))).isEqualTo(1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: Redis可连接但消费组缺失时，状态接口不能报告消费链路正常。
     * @return
     * 返回值: void
     * 返回值说明: 缺失组可明确识别，消费恢复后状态也可恢复
     */
    @Test void missingConsumerGroupIsReported()
    {
        StatisticsProperties p=new StatisticsProperties(); p.setStreamKey("stat-test:"+UUID.randomUUID());
        StatEvent event=event("2032-03-01"); redis.opsForStream().add(p.getStreamKey(),Map.of("payload",event.getPayload()));
        StatisticsPipeline pipe=pipeline(p);
        try
        {
            assertThat(pipe.status()).containsEntry("redisAvailable",true).containsEntry("consumerGroupAvailable",false);
            pipe.consume();
            assertThat(pipe.status()).containsEntry("consumerGroupAvailable",true);
        }
        finally { pipe.stop(); }
    }

}
