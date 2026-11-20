package com.zero.framework.aspectj;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.zero.common.annotation.RateLimiter;
import com.zero.common.core.redis.RedisWatchTransaction;
import com.zero.common.enums.LimitType;
import com.zero.common.exception.ServiceException;
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
    /** 项目现有的 Redis 模板，沿用限流计数键的字符串格式。 */
    private final RedisTemplate<Object, Object> redisTemplate;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:23
     * 说明: 注入执行接口限流事务的 Redis 模板。
     * @param redisTemplate 管理后台使用的 Redis 模板
     * @return
     * 返回值: void
     * 返回值说明: 创建限流切面
     */

    @Autowired
    public RateLimiterAspect(RedisTemplate<Object, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 Redis 事务按接口及限流维度计数，超限或无法取得计数时中断请求。
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
        try
        {
            long number = increment(combineKey, time, count);
            if (number > count)
            {
                throw new ServiceException("访问过于频繁，请稍候再试");
            }
            log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number, combineKey);
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
     * 时间: 2026/11/21 01:23
     * 说明: 在同一事务中递增计数，并在首次计数时设置固定窗口的过期时间。
     * @param key 当前接口及限流维度的计数键
     * @param seconds 限流窗口秒数
     * @param limit 窗口内允许的请求次数
     * @return 本次窗口计数
     * 返回值: long
     * 返回值说明: 已计入一次超限请求后停止递增，窗口到期后重新计数
     */
    long increment(String key, int seconds, int limit)
    {
        byte[] rawKey = redisTemplate.getStringSerializer().serialize(key);
        return RedisWatchTransaction.execute(redisTemplate, rawKey, connection -> {
            byte[] stored = connection.stringCommands().get(rawKey);
            long current = stored == null ? 0 : Long.parseLong(new String(stored, StandardCharsets.US_ASCII));
            if (stored != null && current > limit)
            {
                connection.unwatch();
                return current;
            }
            connection.multi();
            connection.stringCommands().incr(rawKey);
            if (current == 0)
            {
                connection.keyCommands().expire(rawKey, seconds);
            }
            return RedisWatchTransaction.commit(connection) ? current + 1 : null;
        });
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
