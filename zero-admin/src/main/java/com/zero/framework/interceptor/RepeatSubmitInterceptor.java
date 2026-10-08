package com.zero.framework.interceptor;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.zero.common.annotation.RepeatSubmit;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.utils.ServletUtils;

/**
 * 防止重复提交拦截器
 *
 * @author zero
 */
@Component
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor
{
//    @Override
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: preHandle
     * @param request 请求参数
     * @param response 响应参数
     * @param handler 处理器参数
     * @return
     * 返回值: boolean
     * 返回值说明：preHandle
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (handler instanceof HandlerMethod)
        {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null)
            {
                if (this.isRepeatSubmit(request, annotation))
                {
                    AjaxResult ajaxResult = AjaxResult.error(annotation.message());
                    ServletUtils.renderString(response, JSON.toJSONString(ajaxResult));
                    return false;
                }
            }
            return true;
        }
        else
        {
            return true;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 验证是否重复提交由子类实现具体的防重复提交的规则
     * @param request 请求信息
     * @param annotation 防重复注解参数
     * @return
     * 返回值: 结果
     * 返回值说明：验证是否重复提交由子类实现具体的防重复提交的规则
     */
    public abstract boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation);
}
