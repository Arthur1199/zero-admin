package com.zero.framework.aspectj;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import com.zero.common.annotation.RateLimiter;
import com.zero.common.enums.LimitType;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.IpUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 限流处理
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAspect
{
    /**
     * log字段
     */
    /**
     * RedisTemplate字段
     */

    private RedisTemplate<Object, Object> redisTemplate;
    /**
     * limitScript字段
     */

    private RedisScript<Long> limitScript;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置RedisTemplate1
     * @param redisTemplate RedisTemplate参数
     * @return
     * 返回值: void
     * 返回值说明：设置RedisTemplate1
     */

    @Autowired
    public void setRedisTemplate1(RedisTemplate<Object, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置LimitScript
     * @param limitScript limitScript参数
     * @return
     * 返回值: void
     * 返回值说明：设置LimitScript
     */

    @Autowired
    public void setLimitScript(RedisScript<Long> limitScript)
    {
        this.limitScript = limitScript;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 Redis 脚本按接口及限流维度计数，超限或无法取得计数时中断请求。
     * @param point 配置限流的接口切点
     * @param rateLimiter 定义统计周期、允许次数和限流维度的注解
     * @return
     * 返回值: void
     * 返回值说明: 未超过限制时继续执行接口
     */

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable
    {
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        String combineKey = getCombineKey(rateLimiter, point);
        List<Object> keys = Collections.singletonList(combineKey);
        try
        {
            Long number = redisTemplate.execute(limitScript, keys, count, time);
            if (StringUtils.isNull(number) || number.intValue() > count)
            {
                throw new ServiceException("访问过于频繁，请稍候再试");
            }
            log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("服务器限流异常，请稍候再试");
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用限流前缀、声明类和方法名构造计数键，按 IP 限流时追加客户端地址。
     * @param rateLimiter 定义限流维度和键前缀的注解
     * @param point 配置限流的接口切点
     * @return
     * 返回值: String
     * 返回值说明: 当前限流维度对应的 Redis 键
     */

    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point)
    {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP)
        {
            stringBuffer.append(IpUtils.getIpAddr()).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }
}
