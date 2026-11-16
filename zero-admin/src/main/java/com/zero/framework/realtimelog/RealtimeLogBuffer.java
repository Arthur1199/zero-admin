package com.zero.framework.realtimelog;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.function.Function;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.zero.common.exception.ServiceException;
import org.slf4j.LoggerFactory;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 22:34
 * 说明: 当前进程的有界实时日志缓冲，不持久化；容量、序号和快照共用同一把锁。
 */
public final class RealtimeLogBuffer
{
    /** 实时日志路径，用于排除轮询自身统计。 */
    public static final String EVENTS_PATH = "/monitor/realtimeLog/events";
    /** 菜单和接口共用的只读权限。 */
    public static final String PERMISSION = "monitor:realtimeLog:list";
    /** Logback XML中的采集器名称，从正在工作的root日志链路定位读取入口。 */
    public static final String APPENDER_NAME = "realtime";
    /** 默认事件数、总UTF-8字节、单条字节及单批上限。 */
    public static final int DEFAULT_EVENTS = 5000, DEFAULT_BYTES = 10 * 1024 * 1024,
            DEFAULT_EVENT_BYTES = 64 * 1024, BATCH_SIZE = 500;
    /** Logback早于Spring启动；该实例属于采集器的类加载器，接口必须通过实际Appender读取。 */
    public static final RealtimeLogBuffer INSTANCE = new RealtimeLogBuffer();
    /** 明确的敏感字段只在新增查看出口脱敏，不改变原日志。 */
    private static final Pattern CREDENTIAL = Pattern.compile(
            "(?i)([\"']?(?:password|passwd|pwd|token|authorizationToken|authorization|secret|api[-_]?key|(?:x-)?google[-_]?code|captcha(?:Code)?|验证码|密码|密钥)[\"']?\\s*[:=]\\s*)(?:\"[^\"]*\"|'[^']*'|[^\\s,;}]+)");
    /** 未以字段形式记录的Bearer或Basic凭证。 */
    private static final Pattern BEARER = Pattern.compile("(?i)(?:Bearer|Basic)\\s+[A-Za-z0-9._~+/=-]+");
    /** 截断说明计入正文容量。 */
    private static final String TRUNCATED = "\n[日志过长，已截断]";
    /** 重启后变化，避免旧页面错误沿用原游标。 */
    private final String processId = UUID.randomUUID().toString();
    /** 已脱敏的不可变事件快照。 */
    private final Deque<Entry> entries = new ArrayDeque<>();
    /** 当前容量限制与已用字节。 */
    private int maxEvents = DEFAULT_EVENTS, maxBytes = DEFAULT_BYTES,
            maxEventBytes = DEFAULT_EVENT_BYTES, bytes;
    /** 当前进程最后分配的序号。 */
    private long sequence;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:55
     * 说明: 从当前日志上下文定位实际采集器，禁止接口直接读取自身类加载器的静态缓冲。
     * @param processId 上次采集进程标识，首次为空
     * @param after 上次游标，首次为空
     * @param limit 每批条数，最大500
     * @return 实际日志缓冲快照
     * 返回值: Map<String, Object>
     * 返回值说明: 采集器缺失或旧版本尚未重启时明确报错，不返回误导性的空列表
     */
    public static Map<String, Object> readActive(String processId, Long after, int limit)
    {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context))
            throw new ServiceException("当前日志框架不是Logback，无法读取实时日志");
        return readActive(context, processId, after, limit);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:55
     * 说明: 只通过JDK公共接口访问Appender，避免对不同类加载器中的同名业务类做类型转换。
     * @param context 实际或隔离测试的Logback上下文
     * @param processId 上次进程标识
     * @param after 上次游标
     * @param limit 每批条数
     * @return 实际采集结果
     * 返回值: Map<String, Object>
     * 返回值说明: 不回退到接口所属类加载器的空缓冲
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> readActive(LoggerContext context, String processId, Long after, int limit)
    {
        var appender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender(APPENDER_NAME);
        if (appender == null || !appender.isStarted() || !(appender instanceof Function<?, ?>))
            throw new ServiceException("实时日志采集器未就绪，请完整重启zero-admin并检查实际Logback配置");
        Map<String, Object> query = new HashMap<>();
        query.put("processId", processId);
        query.put("after", after);
        query.put("limit", limit);
        return ((Function<Map<String, Object>, Map<String, Object>>) appender).apply(query);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 配置有界容量，Logback重新加载不清空已有日志。
     * @param events 事件数，限制1至50000
     * @param totalBytes 总字节数，限制1KB至100MB
     * @param eventBytes 单条字节数，限制512B至1MB且不超过总容量
     * @return 更新限制并淘汰超限事件
     * 返回值: void
     * 返回值说明: 更新限制并淘汰超限事件
     */
    public synchronized void configure(int events, int totalBytes, int eventBytes)
    {
        maxEvents = Math.max(1, Math.min(events, 50000));
        maxBytes = Math.max(1024, Math.min(totalBytes, 100 * 1024 * 1024));
        maxEventBytes = Math.min(maxBytes, Math.max(512, Math.min(eventBytes, 1024 * 1024)));
        trim();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 脱敏、按UTF-8边界截断后保存事件，异常堆栈随正文保留。
     * @param timestamp 日志产生时间，毫秒
     * @param level 原始级别
     * @param thread 原始线程名
     * @param logger 原始Logger名
     * @param text 已格式化正文和异常堆栈
     * @return 保存快照并淘汰最旧事件
     * 返回值: void
     * 返回值说明: 保存快照并淘汰最旧事件
     */
    public synchronized void append(long timestamp, String level, String thread, String logger, String text)
    {
        String safe = sanitize(text);
        String safeLevel = prefix(sanitize(level), 128), safeThread = prefix(sanitize(thread), 128),
                safeLogger = prefix(sanitize(logger), 128);
        int overhead = utf8(safeLevel) + utf8(safeThread) + utf8(safeLogger);
        int available = maxEventBytes - overhead;
        boolean truncated = utf8(safe) > available;
        if (truncated) safe = prefix(safe, available - utf8(TRUNCATED)) + TRUNCATED;
        Map<String, Object> event = Map.of("sequence", ++sequence, "timestamp", timestamp,
                "level", safeLevel, "thread", safeThread, "logger", safeLogger, "text", safe, "truncated", truncated);
        int size = utf8(safe) + overhead;
        entries.addLast(new Entry(event, size));
        bytes += size;
        trim();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 读取有界快照，首次返回最近日志，游标失效和重启明确告知前端。
     * @param requestedProcess 上次进程标识，首次为空
     * @param after 上次已读序号，首次为空
     * @param limit 单批条数，限制1至500
     * @return 事件及续读状态
     * 返回值: Map<String, Object>
     * 返回值说明: events、processId、nextCursor、hasMore、gap、reset
     */
    public synchronized Map<String, Object> read(String requestedProcess, Long after, int limit)
    {
        int count = Math.max(1, Math.min(limit, BATCH_SIZE));
        boolean reset = requestedProcess != null && !requestedProcess.isBlank() && !processId.equals(requestedProcess);
        boolean initial = after == null || requestedProcess == null || requestedProcess.isBlank() || reset;
        long oldest = entries.isEmpty() ? sequence + 1 : (long) entries.peekFirst().event().get("sequence");
        long cursor = initial ? Math.max(oldest - 1, sequence - count) : Math.max(0, after);
        boolean gap = !initial && cursor < oldest - 1;
        if (cursor > sequence) { gap = true; cursor = Math.max(oldest - 1, sequence - count); }
        List<Map<String, Object>> events = new ArrayList<>();
        for (Entry entry : entries)
        {
            if ((long) entry.event().get("sequence") > cursor)
            {
                events.add(entry.event());
                if (events.size() == count) break;
            }
        }
        long next = events.isEmpty() ? sequence : (long) events.get(events.size() - 1).get("sequence");
        return Map.of("events", events, "processId", processId, "nextCursor", next,
                "hasMore", next < sequence, "gap", gap, "reset", reset);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 容量超限时只淘汰最旧事件，不回退序号。
     * @return 释放超限事件
     * 返回值: void
     * 返回值说明: 释放超限事件
     */
    private void trim()
    {
        while (!entries.isEmpty() && (entries.size() > maxEvents || bytes > maxBytes))
            bytes -= entries.removeFirst().bytes();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 同时保护日志正文及可控线程名中的明确凭证。
     * @param value 可空原始文本
     * @return 安全文本
     * 返回值: String
     * 返回值说明: 不承诺识别任意自由文本中的敏感内容
     */
    private static String sanitize(String value)
    {
        String safe = BEARER.matcher(value == null ? "" : value).replaceAll("Bearer [已脱敏]");
        return CREDENTIAL.matcher(safe).replaceAll("$1[已脱敏]");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 统一使用UTF-8字节计费。
     * @param value 非空文本
     * @return 字节数
     * 返回值: int
     * 返回值说明: UTF-8编码长度
     */
    private static int utf8(String value) { return value.getBytes(StandardCharsets.UTF_8).length; }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 在完整UTF-8字符边界截断，避免中文和emoji乱码。
     * @param value 原文本
     * @param budget 最大字节数
     * @return 有界前缀
     * 返回值: String
     * 返回值说明: 不超过预算的完整字符文本
     */
    private static String prefix(String value, int budget)
    {
        byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
        if (encoded.length <= budget) return value;
        int end = Math.max(0, budget);
        while (end > 0 && (encoded[end] & 0xC0) == 0x80) end--;
        return new String(encoded, 0, end, StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 内部不可变日志快照和字节计费。
     * @param event 已脱敏快照
     * @param bytes UTF-8计费字节
     */
    private record Entry(Map<String, Object> event, int bytes) { }
}
