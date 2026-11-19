package com.zero.config;

import lombok.extern.slf4j.Slf4j;
import com.zero.dto.TableDataInfo;
import com.zero.model.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerMapping;

import java.util.stream.Collectors;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件服务统一异常处理。
 */
@Slf4j
@RestControllerAdvice
public class FileExceptionHandler
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 返回文件接口可预期的参数错误。
     * @param e 请求参数异常
     * @param request 用于识别列表响应类型的当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 与原接口响应格式匹配的错误结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request)
    {
        return error(request, e.getMessage());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 合并请求体验证失败的字段提示，并去除重复信息。
     * @param e 请求体验证异常
     * @param request 用于识别列表响应类型的当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 与原接口响应格式匹配的错误结果
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
     * 时间: 2026/11/20 01:36
     * 说明: 合并表单或查询参数绑定失败的字段提示，并去除重复信息。
     * @param e 绑定异常
     * @param request 用于识别列表响应类型的当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 与原接口响应格式匹配的错误结果
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
     * 时间: 2026/11/20 01:36
     * 说明: 将 multipart 上传超过限制的异常转换为统一提示。
     * @param e 上传大小超限异常
     * @param request 用于识别列表响应类型的当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 与原接口响应格式匹配的错误结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request)
    {
        return error(request, "上传文件大小超过限制");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在错误描述中说明当前请求使用了不支持的 HTTP 方法。
     * @param e 包含所用 HTTP 方法的异常
     * @param request 用于识别列表响应类型的当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 与原接口响应格式匹配的错误结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request)
    {
        return error(request, "请求方式不支持：" + e.getMethod());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对未预期的异常记录请求路径及异常，给调用方返回统一提示。
     * @param e 文件接口处理异常
     * @param request 用于识别列表响应类型的当前请求
     * @return
     * 返回值: Object
     * 返回值说明: 与原接口响应格式匹配的错误结果
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request)
    {
        String uri = request == null ? "" : request.getRequestURI();
        log.error("文件服务接口处理异常，uri={}", uri, e);
        return error(request, "文件服务处理异常，请联系管理员");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为分页列表保留 rows 和 total 错误结构，其他接口使用普通错误响应。
     * @param request 当前文件服务请求
     * @param message 返回给调用方的错误描述
     * @return
     * 返回值: Object
     * 返回值说明: 分页错误或普通错误响应
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
     * 时间: 2026/11/20 01:36
     * 说明: 优先按已匹配的 Controller 方法判断分页类型，尚未匹配时使用列表路径兜底。
     * @param request 当前文件服务请求，可为空
     * @return
     * 返回值: boolean
     * 返回值说明: 接口期望分页响应时返回 true
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
