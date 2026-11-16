package com.zero.framework.realtimelog;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 22:34
 * 说明: 实时输出读取入口，配置控制台文件时覆盖整个进程输出，否则采集root日志；通过JDK接口跨DevTools类加载器读取实际来源。
 */
public class RealtimeLogAppender extends AppenderBase<ILoggingEvent> implements Function<Map<String, Object>, Map<String, Object>>
{
    /** 控制台格式由XML传入，自动补齐异常堆栈。 */
    private PatternLayout layout;
    /** 完整进程输出文件；为空时兼容原有Logback采集。 */
    private String consoleFile;
    /** 与启动器或IDEA输出文件一致的编码。 */
    private String consoleCharset = "UTF-8";
    /** 完整模式唯一读取来源，防止Logback内容重复入队。 */
    private ConsoleFileReader consoleReader;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 配置完整进程控制台文件，只从服务启动配置读取路径。
     * @param value 控制台文件，空值保留Logback模式
     * @return 保存来源配置
     * 返回值: void
     * 返回值说明: 启动时创建只读文件读取器
     */
    public void setConsoleFile(String value) { consoleFile = value; }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 23:05
     * 说明: 设置控制台文件编码，避免Windows默认编码导致中文乱码。
     * @param value Java支持的字符集名称
     * @return 保存编码配置
     * 返回值: void
     * 返回值说明: 默认UTF-8
     */
    public void setConsoleCharset(String value) { consoleCharset = value; }
    /** 默认格式与现有控制台一致。 */
    private String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{20} - [%method,%line] - %msg%n";
    /** XML可覆盖的内存限制。 */
    private int maxEvents = RealtimeLogBuffer.DEFAULT_EVENTS,
            maxBytes = RealtimeLogBuffer.DEFAULT_BYTES, maxEventBytes = RealtimeLogBuffer.DEFAULT_EVENT_BYTES;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 复用控制台日志格式。
     * @param value Logback格式，空值沿用默认
     * @return 设置格式
     * 返回值: void
     * 返回值说明: 设置启动格式
     */
    public void setPattern(String value) { if (value != null && !value.isBlank()) pattern = value; }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 接收事件容量配置。
     * @param value 最大事件数
     * @return 设置启动配置
     * 返回值: void
     * 返回值说明: 启动时统一校验
     */
    public void setMaxEvents(int value) { maxEvents = value; }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 接收总容量配置。
     * @param value 总UTF-8字节数
     * @return 设置启动配置
     * 返回值: void
     * 返回值说明: 启动时统一校验
     */
    public void setMaxBytes(int value) { maxBytes = value; }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 接收单条容量配置。
     * @param value 单条UTF-8字节数
     * @return 设置启动配置
     * 返回值: void
     * 返回值说明: 启动时统一校验
     */
    public void setMaxEventBytes(int value) { maxEventBytes = value; }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 在容器创建前准备采集，配置完整文件时只读该来源，应用模式重新加载沿用缓冲。
     * @return 启用采集器
     * 返回值: void
     * 返回值说明: 创建布局并校验容量
     */
    @Override public void start()
    {
        layout = new PatternLayout();
        layout.setContext(getContext());
        layout.setPattern(pattern);
        layout.start();
        RealtimeLogBuffer.INSTANCE.configure(maxEvents, maxBytes, maxEventBytes);
        consoleReader = consoleFile == null || consoleFile.isBlank() ? null
                : new ConsoleFileReader(consoleFile.trim(), consoleCharset, maxBytes, maxEventBytes);
        super.start();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 应用模式接收root日志事件；完整模式由控制台文件提供同一事件，跳过入队避免重复。
     * @param event 已通过原日志级别过滤的事件
     * @return 有界保存，采集异常静默隔离
     * 返回值: void
     * 返回值说明: 不输出采集异常，防止递归
     */
    @Override protected void append(ILoggingEvent event)
    {
        if (event == null || consoleReader != null) return;
        try
        {
            RealtimeLogBuffer.INSTANCE.append(event.getTimeStamp(), event.getLevel().toString(),
                    event.getThreadName(), event.getLoggerName(), layout.doLayout(event));
        }
        catch (RuntimeException ignored)
        {
            // 采集失败不再调用日志框架，原控制台及文件Appender继续运行。
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:55
     * 说明: 由实际Appender选择控制台文件或自身缓冲，JDK Function和Map不受DevTools双类加载器影响。
     * @param query 内部查询参数processId、after、limit，不接受文件路径
     * @return 实际采集缓冲快照
     * 返回值: Map<String, Object>
     * 返回值说明: 与增量接口data字段结构一致
     */
    @Override public Map<String, Object> apply(Map<String, Object> query)
    {
        if (query == null) throw new IllegalArgumentException("日志读取参数不能为空");
        if (consoleReader != null) return consoleReader.read((String) query.get("processId"),
                (Long) query.get("after"), (Integer) query.getOrDefault("limit", RealtimeLogBuffer.BATCH_SIZE));
        Map<String, Object> result = new java.util.HashMap<>(RealtimeLogBuffer.INSTANCE.read((String) query.get("processId"),
                (Long) query.get("after"), (Integer) query.getOrDefault("limit", RealtimeLogBuffer.BATCH_SIZE)));
        result.put("sourceMode", "logback");
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:34
     * 说明: 重新配置或关闭时释放旧布局。
     * @return 停止采集
     * 返回值: void
     * 返回值说明: 不清空进程缓冲
     */
    @Override public void stop()
    {
        super.stop();
        if (layout != null) layout.stop();
    }
}
