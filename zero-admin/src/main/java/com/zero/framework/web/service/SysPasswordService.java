package com.zero.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.exception.ServiceException;
import com.zero.common.exception.user.UserPasswordNotMatchException;
import com.zero.common.utils.SecurityUtils;
import com.zero.framework.security.context.AuthenticationContextHolder;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.ISysUserService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 登录密码方法
 */
@Component
public class SysPasswordService
{
    private static final int LOGIN_UNLOCK = 0;

    private static final int LOGIN_LOCKED = 1;

    /**
     * Redis缓存字段
     */
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysUserService userService;
    /**
     * maxRetry数量字段
     */

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;
    /**
     * lock时间字段
     */

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 登录账户密码错误次数缓存键名
     * @param username 用户名
     * @return
     * 返回值: 缓存键key
     * 返回值说明：登录账户密码错误次数缓存键名
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验
     * @param user 用户参数
     * @return
     * 返回值: void
     * 返回值说明：校验
     */

    public void validate(SysUser user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();
        boolean loginLockEnabled = configService.selectLoginLockEnabled();

        if (loginLockEnabled && LOGIN_LOCKED == getLoginLock(user))
        {
            throw new ServiceException("登录已经被锁定,请联系管理员进行解锁!");
        }

        if (matches(user, password))
        {
            clearLoginRecordCache(username);
            return;
        }

        if (loginLockEnabled)
        {
            recordLoginFailure(user, username);
        }
        throw new UserPasswordNotMatchException();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取用户登录锁定状态，兼容历史空值为未锁定
     * @param user 用户信息
     * @return
     * 返回值: int
     * 返回值说明: 0-未锁定，1-已锁定
     */
    private int getLoginLock(SysUser user)
    {
        return user.getLoginLock() == null ? LOGIN_UNLOCK : user.getLoginLock();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 记录登录失败次数，达到最大重试次数后锁定用户账号并清理错误次数缓存
     * @param user 登录用户信息
     * @param username 登录用户名，用于错误次数缓存key和更新人
     * @return
     * 返回值: void
     * 返回值说明: 未达到上限时累计缓存次数，达到上限时锁定账号并抛出业务异常
     */
    private void recordLoginFailure(SysUser user, String username)
    {
        String cacheKey = getCacheKey(username);
        Long retryCount = redisCache.incrementCacheObject(cacheKey);
        if (retryCount != null && retryCount.longValue() == 1L)
        {
            redisCache.expire(cacheKey, lockTime, TimeUnit.MINUTES);
        }
        if (retryCount != null && retryCount.intValue() >= maxRetryCount)
        {
            userService.updateUserLoginLock(user.getUserId(), LOGIN_LOCKED, username);
            redisCache.deleteObject(cacheKey);
            throw new ServiceException("密码错误次数过多，账号已被锁定，请联系管理员解锁");
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: matches
     * @param user 用户参数
     * @param rawPassword raw密码参数
     * @return
     * 返回值: boolean
     * 返回值说明：matches
     */

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清空登录Record缓存
     * @param loginName 登录名称参数
     * @return
     * 返回值: void
     * 返回值说明：清空登录Record缓存
     */

    public void clearLoginRecordCache(String loginName)
    {
        clearLoginRecordCache(loginName, loginName);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 清空登录失败缓存并解除账号锁定状态
     * @param loginName 登录用户名，用于定位缓存和用户
     * @param updateBy 更新者用户名
     * @return
     * 返回值: void
     * 返回值说明: 删除登录失败次数缓存；用户已锁定时同步更新为未锁定
     */
    public void clearLoginRecordCache(String loginName, String updateBy)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.deleteObject(getCacheKey(loginName));
        }
        SysUser user = userService.selectUserByUserName(loginName);
        if (user != null && LOGIN_LOCKED == getLoginLock(user))
        {
            userService.updateUserLoginLock(user.getUserId(), LOGIN_UNLOCK, updateBy);
        }
    }
}
