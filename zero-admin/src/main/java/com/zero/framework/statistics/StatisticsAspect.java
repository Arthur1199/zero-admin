package com.zero.framework.statistics;

import com.zero.system.domain.statistics.StatisticsOutcome;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.common.exception.user.UserException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 20:46
 * 说明: 以切面补充登录与异常证据，不修改登录校验、Token生成及异常响应。
 */
@Aspect
@Component
public class StatisticsAspect
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 获取请求上下文，非HTTP调用不参与统计。
     * @return 请求上下文或null
     * 返回值: StatisticsContext
     * 返回值说明: 请求上下文或null
     */
    private StatisticsContext context()
    {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs?StatisticsContext.get(attrs.getRequest()):null;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 22:02
     * 说明: 从已通过TokenService验签和会话读取的结果提取用户ID，覆盖退出等不进入MVC的安全链路。
     * @param point 原令牌读取方法，参数为正在处理的HTTP请求
     * @param user 原方法返回的现有登录用户，可空
     * @return
     * 返回值: void
     * 返回值说明: 只关联用户ID，不读取令牌字段、不修改会话、不依赖MVC请求上下文已经初始化
     */
    @AfterReturning(pointcut="execution(public com.zero.common.core.domain.model.LoginUser com.zero.framework.web.service.TokenService.getLoginUser(jakarta.servlet.http.HttpServletRequest))", returning="user")
    public void authenticatedUser(JoinPoint point, LoginUser user)
    {
        if (user == null || user.getUser() == null || !(point.getArgs()[0] instanceof HttpServletRequest request)) return;
        StatisticsContext context = StatisticsContext.get(request);
        if (context != null) context.event.setUserId(user.getUserId());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 登录创建令牌时关联已认证用户ID，不读取生成的Token。
     * @param point 令牌创建入参
     * @return 关联登录事件用户
     * 返回值: void
     * 返回值说明: 关联登录事件用户
     */
    @Before("execution(public String com.zero.framework.web.service.TokenService.createToken(com.zero.common.core.domain.model.LoginUser))")
    public void tokenUser(JoinPoint point)
    {
        StatisticsContext context=context();
        if (context!=null && context.event.isLogin() && point.getArgs()[0] instanceof LoginUser user) context.event.setUserId(user.getUserId());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 仅在登录服务最终正常返回后标记登录成功。
     * @return 保存最终登录成功证据
     * 返回值: void
     * 返回值说明: 保存最终登录成功证据
     */
    @AfterReturning("execution(public String com.zero.framework.web.service.SysLoginService.login(..))")
    public void loginSuccess()
    {
        StatisticsContext context=context();
        if (context!=null && context.event.isLogin()) context.event.setLoginSuccess(true);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 补充进入统一异常处理器之前的异常类别，不保存异常文本。
     * @param point 统一异常处理器入参
     * @return 保存业务或系统异常证据
     * 返回值: void
     * 返回值说明: 保存业务或系统异常证据
     */
    @Before("execution(* com.zero.framework.web.exception.GlobalExceptionHandler.*(..))")
    public void handledException(JoinPoint point)
    {
        StatisticsContext context=context();
        if (context==null) return;
        for (Object arg:point.getArgs()) if (arg instanceof Throwable error) classify(context,error);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 补充登录校验失败及服务异常分类。
     * @param error 登录抛出的异常
     * @return 记录失败类别
     * 返回值: void
     * 返回值说明: 记录失败类别
     */
    @AfterThrowing(pointcut="execution(public String com.zero.framework.web.service.SysLoginService.login(..))",throwing="error")
    public void loginFailure(Throwable error)
    {
        StatisticsContext context=context(); if (context!=null) classify(context,error);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 20:46
     * 说明: 按异常类型分类，业务错误不得直接当作程序故障。
     * @param context 当前事件
     * @param error 原始异常
     * @return 更新分类证据
     * 返回值: void
     * 返回值说明: 更新分类证据
     */
    private void classify(StatisticsContext context,Throwable error)
    {
        if (error instanceof org.springframework.security.access.AccessDeniedException) context.failure=StatisticsOutcome.AUTH.name();
        else if (error instanceof ServiceException || error instanceof UserException || error instanceof IllegalArgumentException
            || error instanceof org.springframework.validation.BindException
            || error instanceof org.springframework.web.bind.MethodArgumentNotValidException
            || error instanceof org.springframework.http.converter.HttpMessageNotReadableException
            || error instanceof org.springframework.web.bind.ServletRequestBindingException
            || error instanceof org.springframework.web.HttpRequestMethodNotSupportedException
            || error instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
            || error instanceof com.zero.common.exception.DemoModeException) context.failure=StatisticsOutcome.BUSINESS.name();
        else context.systemError=true;
    }
}
