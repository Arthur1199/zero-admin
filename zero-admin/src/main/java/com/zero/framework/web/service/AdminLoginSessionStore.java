package com.zero.framework.web.service;

import com.zero.common.constant.CacheConstants;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/21 01:16
 * 说明: 使用项目的 RedisTemplate 保存和续期登录会话，通过账号标记协调跨实例登录替换。
 */
@Component
@RequiredArgsConstructor
public class AdminLoginSessionStore
{
    /** 上一次登录允许多端并存，切换到单端时需要找出该账号的所有旧会话。 */
    private static final String MULTI_CLIENT = "multi";

    /** 查找存量会话时，分批读取的建议条数。 */
    private static final int SCAN_COUNT = 200;

    /** 同一账号的 Redis 事务冲突时允许重新读取并提交的次数上限。 */
    private static final int MAX_SAVE_ATTEMPTS = 8;

    /** 沿用项目的登录缓存及序列化配置。 */
    private final RedisTemplate<Object, Object> redis;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:16
     * 说明: 保存已通过完整验证的新会话，并按开关撤销同一用户的旧会话。
     * @param user 已签发令牌并设置会话编号的登录用户
     * @param minutes 登录会话有效期，单位为分钟
     * @param singleClient 是否替换该用户的旧会话
     * @return 待通知的旧会话编号
     * 返回值: List<String>
     * 返回值说明: 事务提交后返回本次替换的旧会话，冲突时重新读取账号状态
     */
    List<String> save(LoginUser user, int minutes, boolean singleClient)
    {
        Assert.isTrue(minutes > 0, "登录会话有效期必须大于 0 分钟");
        String accountKey = CacheConstants.USER_LOGIN_SESSION_KEY
                + Objects.requireNonNull(user.getUserId(), "登录用户 ID 不能为空");
        for (int attempt = 0; attempt < MAX_SAVE_ATTEMPTS; attempt++)
        {
            List<String> revoked = saveSession(accountKey, user, minutes, singleClient);
            if (revoked != null)
            {
                return revoked;
            }
        }
        throw new ServiceException("该账号登录请求过多，请重试");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:16
     * 说明: 使用 RedisTemplate 的条件写入续期，避免已撤销的会话被旧请求恢复。
     * @param user 当前请求读取的原会话
     * @param minutes 续期时间，单位为分钟
     * @return
     * 返回值: void
     * 返回值说明: 缓存已删除或已过期时跳过写入
     */
    void renew(LoginUser user, int minutes)
    {
        redis.opsForValue().setIfPresent(CacheConstants.LOGIN_TOKEN_KEY + user.getToken(),
                user, minutes, TimeUnit.MINUTES);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:16
     * 说明: 在同一个 Redis 连接中读取账号标记并提交会话变更，保证不同节点的登录不会相互覆盖撤销结果。
     * @param accountKey 当前账号的登录标记键
     * @param user 已通过验证的新会话
     * @param minutes 登录有效期，单位为分钟
     * @param singleClient 是否替换同账号旧会话
     * @return 提交后要通知的旧会话编号
     * 返回值: List<String>
     * 返回值说明: 返回 null 表示账号标记被其它登录修改，本次事务没有提交
     */
    private List<String> saveSession(String accountKey, LoginUser user, int minutes, boolean singleClient)
    {
        return redis.execute(new SessionCallback<List<String>>()
        {
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/11/21 01:16
             * 说明: 按账号监听并发变更，在一个事务内保存新会话和删除旧会话。
             * @param operations 当前模板绑定到同一连接的操作入口
             * @return 提交后要通知的旧会话编号
             * 返回值: List<String>
             * 返回值说明: 返回 null 时由调用方重试
             */
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> List<String> execute(RedisOperations<K, V> operations) throws DataAccessException
            {
                RedisOperations<Object, Object> session = (RedisOperations<Object, Object>) operations;
                List<String> previous = List.of();
                boolean inTransaction = false;
                try
                {
                    if (singleClient)
                    {
                        session.watch(accountKey);
                        Object marker = session.opsForValue().get(accountKey);
                        previous = marker instanceof String id && !MULTI_CLIENT.equals(id)
                                ? List.of(id) : findSessions(session, user.getUserId());
                    }

                    session.multi();
                    inTransaction = true;
                    session.opsForValue().set(CacheConstants.LOGIN_TOKEN_KEY + user.getToken(), user, minutes, TimeUnit.MINUTES);
                    session.opsForValue().set(accountKey, singleClient ? user.getToken() : MULTI_CLIENT, minutes, TimeUnit.MINUTES);
                    for (String previousId : previous)
                    {
                        session.delete(CacheConstants.LOGIN_TOKEN_KEY + previousId);
                    }
                    List<Object> result = session.exec();
                    return result == null || result.isEmpty() ? null : previous;
                }
                catch (RuntimeException failure)
                {
                    try
                    {
                        if (inTransaction)
                        {
                            session.discard();
                        }
                        else if (singleClient)
                        {
                            session.unwatch();
                        }
                    }
                    catch (RuntimeException cleanupFailure)
                    {
                        failure.addSuppressed(cleanupFailure);
                    }
                    throw failure;
                }
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/21 01:16
     * 说明: 从多端模式切换或账号标记过期时，分批查找该账号已有的登录会话。
     * @param session 当前模板绑定到同一连接的操作入口
     * @param userId 已通过验证的账号 ID
     * @return 同账号现有会话编号
     * 返回值: List<String>
     * 返回值说明: 仅包含归属当前账号的登录会话，扫描结果去重
     */
    private List<String> findSessions(RedisOperations<Object, Object> session, Long userId)
    {
        Set<String> ids = new LinkedHashSet<>();
        // 扫描使用独立连接，关闭游标时不会关闭已经监听账号标记的事务连接。
        try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection();
                Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                        .match(CacheConstants.LOGIN_TOKEN_KEY + "*").count(SCAN_COUNT).build()))
        {
            while (cursor.hasNext())
            {
                List<Object> keys = new ArrayList<>(SCAN_COUNT);
                while (keys.size() < SCAN_COUNT && cursor.hasNext())
                {
                    keys.add(redis.getStringSerializer().deserialize(cursor.next()));
                }
                // 按扫描批次批量读取快照，已经过期的键在结果中保留空位。
                List<Object> users = Objects.requireNonNull(session.opsForValue().multiGet(keys));
                for (int i = 0; i < users.size(); i++)
                {
                    if (users.get(i) instanceof LoginUser login && userId.equals(login.getUserId()))
                    {
                        ids.add(((String) keys.get(i)).substring(CacheConstants.LOGIN_TOKEN_KEY.length()));
                    }
                }
            }
        }
        return List.copyOf(ids);
    }
}
