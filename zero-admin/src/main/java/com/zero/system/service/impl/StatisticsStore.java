package com.zero.system.service.impl;

import com.zero.system.domain.statistics.StatisticsOutcome;
import com.zero.system.domain.statistics.StatEvent;
import com.zero.system.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 事件登记与全部汇总在同一事务提交，保证重复投递不重复累计。
 */
@Service
public class StatisticsStore
{
    /** 统计数据库访问。 */
    private final StatisticsMapper mapper;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 注入统计Mapper。
     * @param mapper 统计数据访问
     */
    public StatisticsStore(StatisticsMapper mapper) { this.mapper = mapper; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: Redis失败时同步保存回退事件。
     * @param event 精简统计事件
     * @return 事件写入待处理表
     * 返回值: void
     * 返回值说明: 事件写入待处理表
     */
    public void fallback(StatEvent event) { validate(event); mapper.insertInbox(event); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 事务消费事件，重试只完成回退状态，不重复汇总。
     * @param event 持久化事件
     * @return 原子更新各统计维度
     * 返回值: void
     * 返回值说明: 原子更新各统计维度
     */
    @Transactional(rollbackFor = Exception.class)
    public void accept(StatEvent event)
    {
        LocalDateTime at = validate(event);
        if (mapper.consume(event.getEventId(), LocalDateTime.now(ZoneId.of("Asia/Shanghai"))) == 0) { mapper.markInbox(event.getEventId()); return; }
        Map<String, Object> row = new HashMap<>();
        row.put("endpointId", endpointId(event.getMethod(), event.getRoute()));
        row.put("method", event.getMethod()); row.put("route", event.getRoute()); row.put("name", event.getName()); row.put("module", event.getModule());
        row.put("minute", at.withSecond(0).withNano(0)); row.put("day", at.toLocalDate()); row.put("at", at);
        row.put("durationUs", Math.max(0, event.getDurationUs())); row.put("slow", event.isSlow() ? 1 : 0);
        for (StatisticsOutcome category : StatisticsOutcome.values())
            row.put(category.name().toLowerCase(Locale.ROOT), category.name().equals(event.getOutcome()) ? 1 : 0);
        row.put("loginSuccess", event.isLoginSuccess() ? 1 : 0);
        row.put("loginSystem", event.isLogin() && StatisticsOutcome.SYSTEM.name().equals(event.getOutcome()) ? 1 : 0);
        row.put("userId", event.getUserId());
        mapper.endpoint(row); mapper.apiMinute(row); mapper.apiDay(row);
        if (event.isLogin()) mapper.loginDay(row);
        if (event.getUserId() != null) mapper.userDay(row);
        mapper.markInbox(event.getEventId());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 入库前验证事件边界，避免INSERT IGNORE截断非法编号后误去重，或MySQL静默截断维度造成脏统计。
     * @param event 内部请求采集事件或从持久化队列恢复的事件
     * @return 完成时间对应的北京时间
     * 返回值: LocalDateTime
     * 返回值说明: 仅完整且符合存储长度和登录语义的事件可继续写入，非法事件交回队列报告故障
     */
    private LocalDateTime validate(StatEvent event)
    {
        if (event == null || event.getEventId() == null || event.getOutcome() == null
                || event.getCompletedAt() <= 0 || event.getDurationUs() < 0)
            throw new IllegalArgumentException("统计事件字段不完整");
        if (!UUID.fromString(event.getEventId()).toString().equals(event.getEventId()))
            throw new IllegalArgumentException("统计事件编号格式错误");
        if (!hasLength(event.getMethod(), 16) || !hasLength(event.getRoute(), 512) || !event.getRoute().startsWith("/")
                || !hasLength(event.getName(), 200) || !hasLength(event.getModule(), 80))
            throw new IllegalArgumentException("统计接口维度不符合存储约束");
        StatisticsOutcome outcome = StatisticsOutcome.valueOf(event.getOutcome());
        if (event.getUserId() != null && event.getUserId() <= 0)
            throw new IllegalArgumentException("统计用户编号错误");
        if (event.isLoginSuccess() && (!event.isLogin() || event.getUserId() == null || outcome != StatisticsOutcome.SUCCESS))
            throw new IllegalArgumentException("统计登录结果不一致");
        LocalDateTime at = Instant.ofEpochMilli(event.getCompletedAt()).atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        if (at.getYear() > 9999) throw new IllegalArgumentException("统计完成时间超出数据库范围");
        return at;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 校验必填维度，禁止空白及超过表字段长度的事件进入汇总事务。
     * @param value 事件维度
     * @param maximum 数据库字段最大字符数
     * @return 维度是否有效
     * 返回值: boolean
     * 返回值说明: true表示非空白且不超过长度限制
     */
    private boolean hasLength(String value, int maximum)
    {
        return value != null && !value.isBlank() && value.length() <= maximum;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 按方式与模板生成定长接口标识。
     * @param method HTTP方式
     * @param route 接口模板
     * @return SHA-256十六进制标识
     * 返回值: String
     * 返回值说明: SHA-256十六进制标识
     */
    public static String endpointId(String method, String route)
    {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((method + " " + route).getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("无法生成统计接口标识", e); }
    }
}
