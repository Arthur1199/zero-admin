package com.zero.framework.statistics;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicBoolean;
import com.zero.system.domain.statistics.StatEvent;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 请求属性内的统计上下文，不使用跨请求ThreadLocal。
 */
public class StatisticsContext
{
    /** 内部请求属性名。 */
    public static final String ATTRIBUTE = StatisticsContext.class.getName();
    /** 当前请求的唯一事件。 */
    public final StatEvent event = new StatEvent();
    /** 单调时钟起点。 */
    public final long started = System.nanoTime();
    /** 防止异步与错误派发重复完成。 */
    public final AtomicBoolean finished = new AtomicBoolean();
    /** 统一返回业务码，未识别为空。 */
    public volatile Integer code;
    /** MVC已检查统一响应的顶层code，即使缺失也不能误读data内的同名字段。 */
    public volatile boolean responseObserved;
    /** 系统异常证据，不存异常内容。 */
    public volatile boolean systemError;
    /** 已识别的失败类别。 */
    public volatile String failure;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 读取当前请求上下文，未采集时返回空。
     * @param request 当前HTTP请求
     * @return 请求统计上下文或null
     * 返回值: StatisticsContext
     * 返回值说明: 请求统计上下文或null
     */
    public static StatisticsContext get(HttpServletRequest request)
    {
        return (StatisticsContext) request.getAttribute(ATTRIBUTE);
    }
}
