package com.zero.config;

import lombok.extern.slf4j.Slf4j;
import com.zero.dto.TableDataInfo;
import com.zero.model.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;

import java.util.stream.Collectors;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 04:37
 * 说明: 报表服务统一异常处理器，按接口返回类型转换为 AjaxResult 或 TableDataInfo，便于 admin 通过 ReportRestService 原样返回前端
 */
@Slf4j
@RestControllerAdvice
public class ReportExceptionHandler
{
    /**
     * 日志对象，仅记录接口路径和异常类型，不输出请求体、令牌等敏感内容
     */
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:37
     * 说明: 处理服务层主动抛出的业务异常，例如重复编码、无效分组、禁止删除含明细的字典组
     * @param e 业务异常
     * @param request 当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 列表接口返回TableDataInfo失败结构，其它接口返回AjaxResult失败结构
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request)
    {
        return error(request, e.getMessage());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:37
     * 说明: 处理 JSON 请求体参数校验异常，合并字段错误并返回给管理后台表单
     * @param e 请求体参数校验异常
     * @param request 当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 列表接口返回TableDataInfo失败结构，其它接口返回AjaxResult失败结构
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request)
    {
        return error(request, e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；")));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:37
     * 说明: 处理普通表单或查询参数绑定异常，避免下游直接返回 Spring 默认错误结构
     * @param e 参数绑定异常
     * @param request 当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 列表接口返回TableDataInfo失败结构，其它接口返回AjaxResult失败结构
     */
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e, HttpServletRequest request)
    {
        return error(request, e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；")));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:37
     * 说明: 处理请求方式不支持异常，提示调用方检查 admin 与 report 的转发方法是否一致
     * @param e 请求方式异常
     * @param request 当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 列表接口返回TableDataInfo失败结构，其它接口返回AjaxResult失败结构
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request)
    {
        return error(request, "请求方式不支持：" + e.getMethod());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 04:37
     * 说明: 处理未预期异常，日志只记录路径和异常栈，不向前端暴露数据库或服务内部细节
     * @param e 未预期异常
     * @param request 当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 列表接口返回TableDataInfo失败结构，其它接口返回AjaxResult失败结构
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request)
    {
        String uri = request == null ? "" : request.getRequestURI();
        log.error("报表服务接口处理异常，uri={}", uri, e);
        return error(request, "报表服务处理异常，请联系管理员");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:02
     * 说明: 根据原始 Controller 方法返回类型构建错误响应，保证列表接口和普通接口的响应结构不串型
     * @param request 当前请求，用于读取 Spring 匹配到的处理方法
     * @param message 错误提示，业务异常可直接展示，系统异常使用通用提示
     * @return
     * 返回值: Object
     * 返回值说明: TableDataInfo或AjaxResult错误结构
     */
    private Object error(HttpServletRequest request, String message)
    {
        if (isTableDataResponse(request))
        {
            return TableDataInfo.error(message);
        }
        return AjaxResult.error(message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:02
     * 说明: 判断当前请求是否来自分页列表接口，优先依据 Controller 方法返回值，兜底兼容 /list 路径
     * @param request 当前请求
     * @return
     * 返回值: boolean
     * 返回值说明: true-应返回TableDataInfo，false-应返回AjaxResult
     */
    private boolean isTableDataResponse(HttpServletRequest request)
    {
        if (request == null)
        {
            return false;
        }
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod handlerMethod)
        {
            return TableDataInfo.class.isAssignableFrom(handlerMethod.getMethod().getReturnType());
        }
        String uri = request.getRequestURI();
        return uri != null && uri.endsWith("/list");
    }
}
