package com.zero.framework.statistics;

import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.utils.SecurityUtils;
import jakarta.servlet.http.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 识别接口模板、认证用户及统一业务码，不改变响应内容。
 */
@Component
@RestControllerAdvice
public class StatisticsMvc implements HandlerInterceptor, ResponseBodyAdvice<Object>
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 在安全上下文被清理前读取接口与认证用户。
     * @param request 请求
     * @param response 响应
     * @param handler 映射处理器
     * @return 始终继续原业务链
     * 返回值: boolean
     * 返回值说明: 始终继续原业务链
     */
    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)
    {
        StatisticsContext context=StatisticsContext.get(request);
        if (context==null || context.finished.get() || request.getDispatcherType() == jakarta.servlet.DispatcherType.ERROR) return true;
        if (handler instanceof HandlerMethod method)
        {
            Object pattern=request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (pattern!=null)
            {
                String route=pattern.toString();
                context.event.setRoute(route.length()>512 ? "/__oversized_mapping__" : route);
                String name = method.getBeanType().getSimpleName()+"."+method.getMethod().getName();
                context.event.setName(name.length() > 200 ? name.substring(0, 200) : name);
                // 模块优先取控制器注解，未声明时才退回按路由首段归类，避免根路径接口把方法名当模块。
                String module=StatisticsModuleResolver.resolve(method);
                context.event.setModule(module!=null?module:StatisticsModuleResolver.fallback(route));
            }
        }
        try
        {
            if (SecurityUtils.getAuthentication()!=null && SecurityUtils.getAuthentication().getPrincipal() instanceof LoginUser user)
                context.event.setUserId(user.getUserId());
        }
        catch (RuntimeException ignored) { /* 匿名请求不归入任何账号。 */ }
        return true;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 统一响应读取业务码，不限制Controller返回类型。
     * @param returnType 映射方法返回类型
     * @param converterType 响应转换器
     * @return 允许观察全部MVC响应
     * 返回值: boolean
     * 返回值说明: 允许观察全部MVC响应
     */
    @Override
    public boolean supports(MethodParameter returnType,Class<? extends HttpMessageConverter<?>> converterType) { return true; }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 仅提取code字段，不保存响应正文。
     * @param body 原始响应
     * @param returnType 返回类型
     * @param contentType 媒体类型
     * @param converterType 转换器
     * @param request 请求
     * @param response 响应
     * @return 原始响应，不做变更
     * 返回值: Object
     * 返回值说明: 原始响应，不做变更
     */
    @Override
    public Object beforeBodyWrite(Object body,MethodParameter returnType,MediaType contentType,Class<? extends HttpMessageConverter<?>> converterType,ServerHttpRequest request,ServerHttpResponse response)
    {
        if (request instanceof ServletServerHttpRequest servlet)
        {
            StatisticsContext context=StatisticsContext.get(servlet.getServletRequest());
            if (context!=null)
            {
                context.responseObserved = body instanceof Map<?,?> || body instanceof com.zero.common.core.page.TableDataInfo;
                Object code=body instanceof Map<?,?> map?map.get("code"):body instanceof com.zero.common.core.page.TableDataInfo table?table.getCode():null;
                context.code = null;
                if (code instanceof Integer number) context.code = number;
                else if (code instanceof Number number)
                {
                    try { context.code = new java.math.BigDecimal(number.toString()).intValueExact(); }
                    catch (NumberFormatException | ArithmeticException ignored) { /* 非整数状态码保持未知，不能截断为200后误报成功。 */ }
                }
            }
        }
        return body;
    }
}
