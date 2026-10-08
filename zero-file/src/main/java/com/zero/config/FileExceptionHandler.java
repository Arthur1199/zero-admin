package com.zero.config;

import com.zero.dto.TableDataInfo;
import com.zero.model.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 文件服务统一异常处理。
 */
@RestControllerAdvice
public class FileExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(FileExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request)
    {
        return error(request, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request)
    {
        return error(request, e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；")));
    }

    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e, HttpServletRequest request)
    {
        return error(request, e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；")));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request)
    {
        return error(request, "上传文件大小超过限制");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request)
    {
        return error(request, "请求方式不支持：" + e.getMethod());
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request)
    {
        String uri = request == null ? "" : request.getRequestURI();
        log.error("文件服务接口处理异常，uri={}", uri, e);
        return error(request, "文件服务处理异常，请联系管理员");
    }

    private Object error(HttpServletRequest request, String message)
    {
        if (isTableDataResponse(request))
        {
            return TableDataInfo.error(message);
        }
        return AjaxResult.error(message);
    }

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
