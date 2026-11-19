package com.zero.framework.statistics;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson2.JSON;
import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.system.domain.statistics.StatEvent;
import com.zero.system.domain.statistics.StatInboxEntry;
import com.zero.system.mapper.StatisticsMapper;
import com.zero.system.service.impl.StatisticsStore;
import com.zero.system.service.IStatisticsSwitchService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 可靠事件管道：同步投递、MySQL回退、消费确认与重启接管，不阻断原业务。
 */
@Slf4j
@Component
public class StatisticsPipeline
{
    /** 仅记录固定错误类别，不输出事件正文或凭证。 */
    /** 配置及数据库访问。 */
    private final StatisticsProperties properties;
    /** 字符串Redis连接，独立于登录缓存序列化格式。 */
    private final StringRedisTemplate redis;
    /** 登录缓存读取，仅用于人数采样。 */
    private final RedisCache cache;
    /** 事务汇总服务。 */
    private final StatisticsStore store;
    /** 查询和回退队列访问。 */
    private final StatisticsMapper mapper;
    /** 统一后台参数开关，采集、消费、采样及页面状态均使用同一来源。 */
    private final IStatisticsSwitchService switchService;
    /** 当前实例消费标识，重启通过pending接管恢复。 */
    private final String consumer = UUID.randomUUID().toString();
    /** 独立生命周期执行器。 */
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, task -> {
        Thread thread = new Thread(task, "admin-statistics"); thread.setDaemon(true); return thread;
    });
    /** 本进程无法持久化的事件数，重启后不保留。 */
    private final AtomicLong lost = new AtomicLong();
    /** 限频日志及故障状态。 */
    private final AtomicLong loggedAt = new AtomicLong();
    /** 最近投递或消费失败时间，Unix毫秒。 */
    private volatile long lastErrorAt;
    /** 最近成功消费轮次时间，Unix毫秒。 */
    private volatile long lastSuccessAt;
    /** Redis消费组是否完成初始化。 */
    private volatile boolean groupReady;
    /** 近期活跃数据库查询是否可用，响应保留原字段名以兼容页面。 */
    private volatile boolean activeIndexAvailable = true;
    /** 单实例消费互斥，防止并发轮次覆盖扫描游标。 */
    private final AtomicBoolean consuming = new AtomicBoolean();
    /** Redis待确认队列扫描位置，扫描到尾部后回到起点。 */
    private String pendingCursor;
    /** MySQL待处理队列创建时间游标，使用数据库原始精度。 */
    private LocalDateTime inboxCursorTime;
    /** 与创建时间共同组成稳定游标的事件编号。 */
    private String inboxCursorId;
    /** 只释放属于本采样实例的租约。 */
    private static final DefaultRedisScript<Long> UNLOCK = new DefaultRedisScript<>(
        "if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]); end; return 0", Long.class);
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 注入统计持久化依赖。
     * @param properties 采集配置
     * @param redis 字符串Redis连接
     * @param cache 登录缓存
     * @param store 事务汇总
     * @param mapper 统计查询
     * @param switchService 后台参数开关
     */
    public StatisticsPipeline(StatisticsProperties properties, StringRedisTemplate redis, RedisCache cache, StatisticsStore store, StatisticsMapper mapper, IStatisticsSwitchService switchService)
    { this.properties=properties; this.redis=redis; this.cache=cache; this.store=store; this.mapper=mapper; this.switchService=switchService; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 启动独立消费者及在线采样，不要求业务Quartz任务额外配置。
     * @return 注册可恢复后台消费
     * 返回值: void
     * 返回值说明: 注册可恢复后台消费
     */
    @PostConstruct
    public void start()
    {
        executor.scheduleWithFixedDelay(this::consume, 1000, Math.max(1000,properties.getPollMillis()), TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(this::sample, 2000, Math.max(30000,properties.getSampleMillis()), TimeUnit.MILLISECONDS);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 停止后台调度，未确认事件留在持久化队列中等待恢复。
     * @return 释放独立执行器
     * 返回值: void
     * 返回值说明: 释放独立执行器
     */
    @PreDestroy
    public void stop()
    {
        executor.shutdown();
        try { if (!executor.awaitTermination(10,TimeUnit.SECONDS)) executor.shutdownNow(); }
        catch (InterruptedException e) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 同步投递事件，Redis异常或结果不确定时以同一ID回退MySQL。
     * @param event 不含敏感信息的统计事件
     * @return 事件持久化或记录明确缺口
     * 返回值: void
     * 返回值说明: 事件持久化或记录明确缺口
     */
    public void publish(StatEvent event)
    {
        try
        {
            RecordId id=redis.opsForStream().add(properties.getStreamKey(),Map.of("payload",event.getPayload()));
            if (id == null) throw new IllegalStateException("统计队列未确认写入");
        }
        catch (Exception redisFailure)
        {
            problem("Redis投递失败，回退数据库");
            try { store.fallback(event); }
            catch (Exception databaseFailure) { lost.incrementAndGet(); problem("双通道不可用，存在统计缺口"); }
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 采集准备或结果提取失败时报告缺口，不把统计异常抛回业务过滤链。
     * @return
     * 返回值: void
     * 返回值说明: 累加本实例缺口数并限频输出固定错误说明，不记录请求内容
     */
    public void captureFailure()
    {
        lost.incrementAndGet();
        problem("统计采集失败，原业务继续执行，存在统计缺口");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 初始化消费组，保留全部历史消息，不裁剪未消费数据。
     * @return 确保Stream和消费组存在
     * 返回值: void
     * 返回值说明: 确保Stream和消费组存在
     */
    private void ensureGroup()
    {
        if (groupReady) return;
        try
        {
            redis.execute((RedisCallback<Object>) connection -> connection.execute("XGROUP",
                bytes("CREATE"),bytes(properties.getStreamKey()),bytes(properties.getGroup()),bytes("0"),bytes("MKSTREAM")));
        }
        catch (Exception e)
        {
            Throwable cause=e; boolean exists=false;
            while (cause != null) { if (cause.getMessage()!=null && cause.getMessage().contains("BUSYGROUP")) exists=true; cause=cause.getCause(); }
            if (!exists) throw e;
        }
        groupReady=true;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 消费Stream及回退队列，单事件失败保留待确认状态，不影响其它事件。
     * @return 事务提交后确认消息
     * 返回值: void
     * 返回值说明: 事务提交后确认消息
     */
    public void consume()
    {
        if (!consuming.compareAndSet(false, true)) return;
        try { consumeRound(); }
        catch (Exception error) { problem("统计消费轮次失败，后续轮次继续重试"); }
        finally { consuming.set(false); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 分页扫描两种队列，坏事件保留重试但不持续占据每轮首批位置。
     * @return
     * 返回值: void
     * 返回值说明: 成功事件提交并确认，异常状态由外层保护避免终止定时消费
     */
    private void consumeRound()
    {
        if (!switchService.isEnabled()) return;
        int limit=Math.max(1,Math.min(500,properties.getBatchSize()));
        boolean healthy=true;
        try
        {
            ensureGroup();
            Range<String> range = pendingCursor == null ? Range.unbounded() : Range.rightUnbounded(Range.Bound.exclusive(pendingCursor));
            var pending=redis.opsForStream().pending(properties.getStreamKey(),properties.getGroup(),range,limit);
            if (pending != null)
            {
                List<RecordId> ids=new ArrayList<>();
                int seen = 0;
                for (var message : pending)
                {
                    seen++;
                    pendingCursor = message.getId().getValue();
                    if (message.getElapsedTimeSinceLastDelivery().toMillis() >= Math.max(60000, properties.getClaimIdleMillis())) ids.add(message.getId());
                }
                if (seen < limit) pendingCursor = null;
                if (!ids.isEmpty()) healthy=process(redis.opsForStream().claim(properties.getStreamKey(),properties.getGroup(),consumer,
                    Duration.ofMillis(Math.max(60000,properties.getClaimIdleMillis())),ids.toArray(RecordId[]::new))) && healthy;
            }
            healthy=process(redis.opsForStream().read(Consumer.from(properties.getGroup(),consumer),StreamReadOptions.empty().count(limit),
                StreamOffset.create(properties.getStreamKey(),ReadOffset.lastConsumed()))) && healthy;
        }
        catch (Exception e) { healthy=false; groupReady=false; problem("Stream消费暂不可用"); }
        try
        {
            Map<String, Object> cursor = new HashMap<>();
            cursor.put("limit", limit); cursor.put("afterTime", inboxCursorTime); cursor.put("afterId", inboxCursorId);
            List<StatInboxEntry> entries = mapper.pendingInbox(cursor);
            for (StatInboxEntry entry : entries)
            {
                inboxCursorTime = entry.getCreatedAt(); inboxCursorId = entry.getEventId();
                try
                {
                    StatEvent event = JSON.parseObject(entry.getPayload(), StatEvent.class);
                    if (event == null || !entry.getEventId().equals(event.getEventId())) throw new IllegalArgumentException("回退事件编号不一致");
                    store.accept(event);
                }
                catch (Exception e) { healthy=false; problem("回退事件消费失败，等待后续扫描重试"); }
            }
            if (entries.size() < limit) { inboxCursorTime = null; inboxCursorId = null; }
        }
        catch (Exception e) { healthy=false; problem("回退队列读取失败"); }
        if (healthy) lastSuccessAt=System.currentTimeMillis();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 处理已读取事件，数据库提交后才确认，重复投递由数据库去重。
     * @param records 新消息或被接管的待确认消息
     * @return 全部消息是否成功处理
     * 返回值: boolean
     * 返回值说明: 全部消息是否成功处理
     */
    private boolean process(List<MapRecord<String,Object,Object>> records)
    {
        boolean healthy=true;
        if (records == null) return true;
        for (var record:records)
        {
            try
            {
                StatEvent event=JSON.parseObject(String.valueOf(record.getValue().get("payload")),StatEvent.class);
                store.accept(event);
                // 持久化成功后再确认消息，失败时保留记录供重新投递。
                redis.opsForStream().acknowledge(properties.getStreamKey(),properties.getGroup(),record.getId());
            }
            catch (Exception e) { healthy=false; problem("事件消费未完成，保留待确认记录"); }
        }
        return healthy;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 分批扫描登录会话并保存采样，不读取或还原会话令牌，不续期。
     * @return 保存在线及活跃快照，失败时保留旧采样
     * 返回值: void
     * 返回值说明: 保存在线及活跃快照，失败时保留旧采样
     */
    public void sample()
    {
        String lock=properties.getStreamKey()+StatisticsProperties.SAMPLE_LOCK_SUFFIX, owner=UUID.randomUUID().toString();
        boolean acquired = false;
        try
        {
            if (!switchService.isEnabled()) return;
            if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lock,owner,Duration.ofSeconds(120)))) return;
            acquired = true;
            Set<Long> users=new HashSet<>(); Set<String> sessions=new HashSet<>();
            redis.execute((RedisCallback<Void>) connection -> {
                try (Cursor<byte[]> cursor=connection.scan(ScanOptions.scanOptions().match(CacheConstants.LOGIN_TOKEN_KEY+"*").count(200).build()))
                {
                    while (cursor.hasNext())
                    {
                        String key=new String(cursor.next(),StandardCharsets.UTF_8);
                        LoginUser user=cache.getCacheObject(key);
                        if (user!=null && user.getUser()!=null && user.getUserId()!=null && user.getExpireTime()>System.currentTimeMillis())
                        { sessions.add(key); users.add(user.getUserId()); }
                    }
                }
                return null;
            });
            if (!owner.equals(redis.opsForValue().get(lock))) return;
            long now=System.currentTimeMillis();
            LocalDateTime at=Instant.ofEpochMilli(now).atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            LocalDateTime begin = at.minusMinutes(5);
            long active;
            try
            {
                active = mapper.countRecentActive(Map.of("beginDay", begin.toLocalDate(), "endDay", at.toLocalDate(), "beginAt", begin, "endAt", at));
                activeIndexAvailable = true;
            }
            catch (RuntimeException error) { activeIndexAvailable = false; throw error; }
            mapper.online(Map.of("minute",at.withSecond(0).withNano(0),"at",at,"users",users.size(),"sessions",sessions.size(),"active",active));
        }
        catch (Exception e) { problem("在线人数采样失败，保留上次采样"); }
        finally { if (acquired) try { redis.execute(UNLOCK,List.of(lock),owner); } catch (Exception ignored) { /* 租约自动到期。 */ } }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 读取当前实例状态与Stream积压，Redis异常返回不可用状态。
     * @return 采集状态，不含事件内容
     * 返回值: Map<String, Object>
     * 返回值说明: 采集状态，不含事件内容
     */
    public Map<String,Object> status()
    {
        Map<String,Object> result=new HashMap<>();
        boolean enabled = switchService.isEnabled();
        result.put("switchAvailable", switchService.isAvailable());
        result.put("activeIndexAvailable",activeIndexAvailable);
        result.put("enabled",enabled); result.put("lostSinceStart",lost.get());
        result.put("lastErrorAt",lastErrorAt); result.put("lastSuccessAt",lastSuccessAt);
        result.put("slowMillis",properties.getSlowMillis()); result.put("sampleMillis",Math.max(30000,properties.getSampleMillis()));
        if (enabled) try
        {
            var info=redis.opsForStream().groups(properties.getStreamKey());
            boolean found = false;
            for (var group:info) if (properties.getGroup().equals(group.groupName()))
            { found = true; result.put("streamPending",group.pendingCount()); result.put("streamLag",group.getRaw().get("lag")); }
            result.put("consumerGroupAvailable", found);
            result.put("redisAvailable",true);
        }
        catch (Exception e) { result.put("redisAvailable",false); }
        return result;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 记录限频固定错误说明，日志不包含事件正文及底层连接信息。
     * @param message 固定错误描述
     * @return 更新故障时间与限频日志
     * 返回值: void
     * 返回值说明: 更新故障时间与限频日志
     */
    private void problem(String message)
    {
        long now=System.currentTimeMillis(); lastErrorAt=now;
        long old=loggedAt.get();
        if (now-old>30000 && loggedAt.compareAndSet(old,now)) log.warn("运行统计：{}",message);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 编码Redis命令参数。
     * @param value 固定命令参数或配置键
     * @return UTF-8命令参数
     * 返回值: byte[]
     * 返回值说明: UTF-8命令参数
     */
    private static byte[] bytes(String value) { return value.getBytes(StandardCharsets.UTF_8); }
}
