package com.zero.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.zero.common.constant.CacheConstants;
import com.zero.common.enums.LimitType;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 限流key
     * @return
     * 返回值: String
     * 返回值说明: 限流key
     */
    public String key() default CacheConstants.RATE_LIMIT_KEY;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 限流时间,单位秒
     * @return
     * 返回值: int
     * 返回值说明: 限流时间,单位秒
     */
    public int time() default 60;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 限流次数
     * @return
     * 返回值: int
     * 返回值说明: 限流次数
     */
    public int count() default 100;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 限流类型
     * @return
     * 返回值: LimitType
     * 返回值说明: 限流类型
     */
    public LimitType limitType() default LimitType.DEFAULT;
}
