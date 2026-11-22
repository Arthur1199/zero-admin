package com.zero.framework.statistics;

import com.zero.system.domain.statistics.StatisticsOutcome;
import com.zero.system.domain.statistics.StatEvent;
import com.zero.system.service.IStatisticsSwitchService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.util.AntPathMatcher;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 全链路统计Filter，包含安全拒绝及异步请求，完成时同步持久化且只记录一次。
 */
public class StatisticsFilter implements Filter
{
    /** 统计配置。 */
    private final StatisticsProperties properties;
    /** 持久化投递管道。 */
    private final StatisticsPipeline pipeline;
    /** 后台参数中的统计总开关。 */
    private final IStatisticsSwitchService switchService;
    /** 有限路径匹配器。 */
    private final AntPathMatcher matcher=new AntPathMatcher();
    /** 安全链直接输出的JSON只做有界观察，不缓存完整响应或下载流。 */
    private static final int MAX_RESPONSE_PREFIX = 2048;
    /** 未匹配请求的路径最大长度，与统计接口表的route列长度一致。 */
    private static final int MAX_REQUEST_PATH_LENGTH = 512;
    /** 流式读取顶层业务码，禁止将data中的code误认为整个请求的结果。 */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    /** 登录入口，仅POST构成登录尝试。 */
    private static final String LOGIN_PATH = "/login";
    /** Spring Security直接处理的退出入口，不经过MVC映射识别。 */
    private static final String LOGOUT_PATH = "/logout";
    /** 允许单独归类的HTTP方法，其它值归入OTHER防止维度膨胀。 */
    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 初始化Filter。
     * @param properties 统计配置
     * @param pipeline 可靠投递管道
     * @param switchService 后台参数开关，修改后下次请求生效
     */
    public StatisticsFilter(StatisticsProperties properties, StatisticsPipeline pipeline, IStatisticsSwitchService switchService)
    {
        this.properties = properties;
        this.pipeline = pipeline;
        this.switchService = switchService;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 采集请求处理耗时与最终结果，统计异常不得改变业务响应。
     * @param request Servlet请求
     * @param response Servlet响应
     * @param chain 原过滤链
     * @return 继续原过滤链并提交一个统计事件
     * 返回值: void
     * 返回值说明: 继续原过滤链并提交一个统计事件
     */
    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException
    {
        if (!(request instanceof HttpServletRequest http) || !(response instanceof HttpServletResponse original))
        { chain.doFilter(request,response); return; }
        StatisticsContext context=StatisticsContext.get(http);
        if (context!=null) { chain.doFilter(request,response); return; }
        boolean collect;
        try { collect = included(http); }
        catch (RuntimeException error) { pipeline.captureFailure(); collect = false; }
        if (!collect) { chain.doFilter(request,response); return; }
        context=new StatisticsContext(); http.setAttribute(StatisticsContext.ATTRIBUTE,context);
        StatEvent event=context.event;
        event.setEventId(UUID.randomUUID().toString());
        event.setMethod(HTTP_METHODS.contains(http.getMethod())?http.getMethod():"OTHER");
        // requestURI不含查询字符串，避免查询参数中的凭据进入统计；MVC识别成功后会替换为接口模板。
        String path = http.getRequestURI().substring(http.getContextPath().length());
        event.setRoute(path.length()>MAX_REQUEST_PATH_LENGTH ? path.substring(0,MAX_REQUEST_PATH_LENGTH-3)+"..." : path);
        event.setName("未匹配接口或安全链路拒绝"); event.setModule("other");
        event.setLogin("POST".equals(http.getMethod()) && LOGIN_PATH.equals(path));
        if (event.isLogin()) { event.setRoute(LOGIN_PATH); event.setName("后台登录"); event.setModule("login"); }
        if (LOGOUT_PATH.equals(path)) { event.setRoute(LOGOUT_PATH); event.setName("退出登录"); event.setModule("login"); }
        ObservedResponse observed=new ObservedResponse(original,context);
        try { chain.doFilter(request,observed); }
        catch (IOException | ServletException | RuntimeException | Error error) { context.systemError=true; throw error; }
        finally
        {
            if (http.isAsyncStarted())
            {
                StatisticsContext captured=context;
                AsyncListener listener=new AsyncListener()
                {
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 异步处理结束时完成唯一事件。
                     * @param event 异步完成事件
                     * @return 持久化统计事件
                     * 返回值: void
                     * 返回值说明: 持久化统计事件
                     */
                    @Override public void onComplete(AsyncEvent event) { finish(captured,observed); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 异步超时按系统失败分类。
                     * @param event 超时事件
                     * @return 保存失败证据
                     * 返回值: void
                     * 返回值说明: 保存失败证据
                     */
                    @Override public void onTimeout(AsyncEvent event) { captured.systemError=true; }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 异步异常按系统失败分类。
                     * @param event 异常事件
                     * @return 保存失败证据
                     * 返回值: void
                     * 返回值说明: 保存失败证据
                     */
                    @Override public void onError(AsyncEvent event) { captured.systemError=true; }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 异步重启周期继续监听，避免提前结束。
                     * @param event 新异步周期
                     * @return 重新注册监听器
                     * 返回值: void
                     * 返回值说明: 重新注册监听器
                     */
                    @Override public void onStartAsync(AsyncEvent event) { event.getAsyncContext().addListener(this); }
                };
                try { http.getAsyncContext().addListener(listener); }
                catch (IllegalStateException completed) { finish(context,observed); }
            }
            else finish(context,observed);
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 先排除WebSocket握手、统计自身、实时日志轮询、OPTIONS及静态路径，再读取后台参数开关，避免无须采集的请求额外访问参数缓存。
     * @param request HTTP请求
     * @return 是否需要采集
     * 返回值: boolean
     * 返回值说明: 是否需要采集
     */
    private boolean included(HttpServletRequest request)
    {
        if ("OPTIONS".equals(request.getMethod())) return false;
        String route=request.getRequestURI().substring(request.getContextPath().length());
        // WebSocket升级成功返回101，不参与普通HTTP接口的成功和失败统计。
        if (com.zero.common.constant.AdminWebSocketConstants.PATH.equals(route)) return false;
        // 每秒日志轮询不触发统计持久化，避免产生轮询自身日志。
        if (com.zero.framework.realtimelog.RealtimeLogBuffer.EVENTS_PATH.equals(route)) return false;
        if (route.equals("/monitor/statistics") || route.startsWith("/monitor/statistics/")) return false;
        if (properties.getExcludes()!=null && properties.getExcludes().stream()
                .filter(pattern -> pattern != null && !pattern.isBlank()).anyMatch(pattern -> matcher.match(pattern,route))) return false;
        return switchService.isEnabled();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 按完成时间和单调耗时生成结果，只允许一次持久化。
     * @param context 请求上下文
     * @param response 最终响应
     * @return 提交精简事件
     * 返回值: void
     * 返回值说明: 提交精简事件
     */
    private void finish(StatisticsContext context,ObservedResponse response)
    {
        if (!context.finished.compareAndSet(false,true)) return;
        try
        {
            response.extractCode();
            StatEvent event=context.event;
            event.setCompletedAt(System.currentTimeMillis()); event.setDurationUs(Math.max(0,(System.nanoTime()-context.started)/1000));
            event.setSlow(event.getDurationUs()/1000>=Math.max(1,properties.getSlowMillis()));
            boolean json=response.getContentType()!=null && response.getContentType().toLowerCase(Locale.ROOT).contains("json");
            event.setOutcome(StatisticsResult.classify(response.getStatus(),context.code,context.systemError,context.failure,json));
            event.setLoginSuccess(event.isLoginSuccess() && StatisticsOutcome.SUCCESS.name().equals(event.getOutcome()));
            if (event.isLogin() && !event.isLoginSuccess()) event.setUserId(null);
            pipeline.publish(event);
        }
        catch (Exception ignored) { pipeline.captureFailure(); }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 有界观察安全链直接输出的JSON；透传响应，不缓存下载或完整正文。
     */
    private static class ObservedResponse extends HttpServletResponseWrapper
    {
        /** 当前请求上下文。 */
        private final StatisticsContext context;
        /** 有限前缀，提取后立即释放；不入库、不落日志。 */
        private final ByteArrayOutputStream prefix=new ByteArrayOutputStream();
        /** 包装后的输出流与字符流，不改变原响应的使用约束。 */
        private ServletOutputStream output;
        /** 原字符输出的透传Writer。 */
        private PrintWriter writer;
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 创建透传响应包装器。
         * @param response 原响应
         * @param context 统计上下文
         */
        ObservedResponse(HttpServletResponse response,StatisticsContext context) { super(response); this.context=context; }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 只观察未被MVC识别的JSON前缀。
         * @param bytes 本次输出字节
         * @param offset 起始位置
         * @param length 长度
         * @return 缓存有界观察前缀
         * 返回值: void
         * 返回值说明: 缓存有界观察前缀
         */
        private void observe(byte[] bytes,int offset,int length)
        {
            if (shouldObserve()) prefix.write(bytes,offset,Math.min(length,MAX_RESPONSE_PREFIX-prefix.size()));
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 22:02
         * 说明: 已由MVC识别的响应和非JSON输出不创建观察副本，避免业务大响应产生额外分配。
         * @return 是否需要继续观察JSON前缀
         * 返回值: boolean
         * 返回值说明: 达到上限或已识别业务码后立即停止观察
         */
        private boolean shouldObserve()
        {
            return !context.responseObserved && context.code == null && prefix.size() < MAX_RESPONSE_PREFIX
                    && getContentType() != null && getContentType().toLowerCase(Locale.ROOT).contains("json");
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 提取业务码并释放临时字节，不保存响应内容。
         * @return 仅更新业务码
         * 返回值: void
         * 返回值说明: 仅更新业务码
         */
        void extractCode()
        {
            if (!context.responseObserved && context.code==null)
            {
                try (JsonParser parser = JSON_FACTORY.createParser(prefix.toByteArray()))
                {
                    if (parser.nextToken() == JsonToken.START_OBJECT)
                    {
                        while (parser.nextToken() == JsonToken.FIELD_NAME)
                        {
                            String name = parser.currentName();
                            JsonToken token = parser.nextToken();
                            if ("code".equals(name))
                            {
                                if (token == JsonToken.VALUE_NUMBER_INT) context.code = parser.getIntValue();
                                break;
                            }
                            parser.skipChildren();
                        }
                    }
                }
                catch (IOException ignored) { /* 前缀被截断或响应不规范时保留未知，不修改实际响应。 */ }
            }
            prefix.reset();
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 透传字节流并观察有限JSON前缀。
         * @return 原流的透传包装
         * 返回值: ServletOutputStream
         * 返回值说明: 原流的透传包装
         */
        @Override public ServletOutputStream getOutputStream() throws IOException
        {
            if (writer!=null) throw new IllegalStateException("getWriter已调用");
            if (output==null)
            {
                ServletOutputStream delegate=super.getOutputStream();
                output=new ServletOutputStream()
                {
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 转发单字节输出。
                     * @param value 输出字节
                     * @return 不改变原输出
                     * 返回值: void
                     * 返回值说明: 不改变原输出
                     */
                    @Override public void write(int value) throws IOException { delegate.write(value); if (shouldObserve()) observe(new byte[]{(byte)value},0,1); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 转发批量输出。
                     * @param bytes 字节数组
                     * @param offset 偏移
                     * @param length 长度
                     * @return 不改变原输出
                     * 返回值: void
                     * 返回值说明: 不改变原输出
                     */
                    @Override public void write(byte[] bytes,int offset,int length) throws IOException { delegate.write(bytes,offset,length); observe(bytes,offset,length); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 透传刷新。
                     * @return 刷新原流
                     * 返回值: void
                     * 返回值说明: 刷新原流
                     */
                    @Override public void flush() throws IOException { delegate.flush(); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 透传关闭。
                     * @return 关闭原流
                     * 返回值: void
                     * 返回值说明: 关闭原流
                     */
                    @Override public void close() throws IOException { delegate.close(); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 读取原流就绪状态。
                     * @return 是否可写
                     * 返回值: boolean
                     * 返回值说明: 是否可写
                     */
                    @Override public boolean isReady() { return delegate.isReady(); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 透传异步写监听器。
                     * @param listener 原写监听器
                     * @return 保持异步输出语义
                     * 返回值: void
                     * 返回值说明: 保持异步输出语义
                     */
                    @Override public void setWriteListener(WriteListener listener) { delegate.setWriteListener(listener); }
                };
            }
            return output;
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 透传字符流，不额外刷新或提交业务响应。
         * @return 原字符流的透传包装
         * 返回值: PrintWriter
         * 返回值说明: 原字符流的透传包装
         */
        @Override public PrintWriter getWriter() throws IOException
        {
            if (output!=null) throw new IllegalStateException("getOutputStream已调用");
            if (writer==null)
            {
                PrintWriter delegate=super.getWriter();
                writer=new PrintWriter(new Writer()
                {
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 透传字符输出并观察JSON。
                     * @param chars 字符数组
                     * @param offset 偏移
                     * @param length 长度
                     * @return 写入原字符流
                     * 返回值: void
                     * 返回值说明: 写入原字符流
                     */
                    @Override public void write(char[] chars,int offset,int length) { delegate.write(chars,offset,length); if (shouldObserve()) { byte[] bytes=new String(chars,offset,Math.min(length,MAX_RESPONSE_PREFIX-prefix.size())).getBytes(StandardCharsets.UTF_8); observe(bytes,0,bytes.length); } }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 透传字符刷新。
                     * @return 刷新原字符流
                     * 返回值: void
                     * 返回值说明: 刷新原字符流
                     */
                    @Override public void flush() { delegate.flush(); }
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 20:46
                     * 说明: 透传字符关闭。
                     * @return 关闭原字符流
                     * 返回值: void
                     * 返回值说明: 关闭原字符流
                     */
                    @Override public void close() { delegate.close(); }
                })
                {
                    /**
                     *
                     * 作者: Arthur 亚瑟
                     * 时间: 2026/11/16 22:02
                     * 说明: 原PrintWriter会自行吞掉底层写异常，包装后仍须返回原始错误状态。
                     * @return 原响应字符流是否发生写入错误
                     * 返回值: boolean
                     * 返回值说明: 透传原流的checkError，不把失败误报为正常
                     */
                    @Override public boolean checkError() { return delegate.checkError(); }
                };
            }
            return writer;
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 响应重置时清理先前观察内容，避免识别旧错误码。
         * @return 重置原响应及临时观察
         * 返回值: void
         * 返回值说明: 重置原响应及临时观察
         */
        @Override public void resetBuffer() { super.resetBuffer(); prefix.reset(); context.code=null; context.responseObserved=false; }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/11/16 20:46
         * 说明: 完整重置响应时清理观察内容。
         * @return 重置原响应及临时观察
         * 返回值: void
         * 返回值说明: 重置原响应及临时观察
         */
        @Override public void reset() { super.reset(); prefix.reset(); context.code=null; context.responseObserved=false; writer=null; output=null; }
    }
}
