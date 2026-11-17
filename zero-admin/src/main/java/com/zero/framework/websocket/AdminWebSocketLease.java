package com.zero.framework.websocket;

import java.util.List;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16
 * 说明: 用Redis有序集合限制同一登录会话在所有节点的连接数；租约自动清理宕机节点记录
 */
@Component
public class AdminWebSocketLease
{
    /** 使用Redis统一时间原子清理并申请名额，避免节点时钟偏差误回收其它节点连接。 */
    private static final DefaultRedisScript<Long> ACQUIRE = new DefaultRedisScript<>(
            "local t=redis.call('TIME'); local now=tonumber(t[1])*1000+math.floor(tonumber(t[2])/1000); "
            + "redis.call('zremrangebyscore',KEYS[1],'-inf',now); "
            + "if redis.call('zcard',KEYS[1]) >= tonumber(ARGV[2]) then return 0 end; "
            + "redis.call('zadd',KEYS[1],now+tonumber(ARGV[1]),ARGV[3]); "
            + "redis.call('pexpire',KEYS[1],ARGV[1]); return 1", Long.class);
    /** 只续租仍存活的成员，过期成员不能凭旧连接重新占用名额。 */
    private static final DefaultRedisScript<Long> RENEW = new DefaultRedisScript<>(
            "local t=redis.call('TIME'); local now=tonumber(t[1])*1000+math.floor(tonumber(t[2])/1000); "
            + "local score=redis.call('zscore',KEYS[1],ARGV[2]); "
            + "if not score or tonumber(score)<=now then return 0 end; "
            + "redis.call('zadd',KEYS[1],now+tonumber(ARGV[1]),ARGV[2]); "
            + "redis.call('pexpire',KEYS[1],ARGV[1]); return 1", Long.class);
    private final StringRedisTemplate redis;
    private final AdminWebSocketProperties properties;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 使用独立字符串序列化，不改变已有登录缓存格式
     * @param factory 项目现有Redis连接工厂
     * @param properties 环境及连接限制
     */
    public AdminWebSocketLease(RedisConnectionFactory factory, AdminWebSocketProperties properties)
    {
        this.redis = new StringRedisTemplate(factory);
        this.properties = properties;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 按Redis统一时间在所有节点间原子申请连接名额，Redis异常不放行
     * @param sessionId 已验证的登录会话编号
     * @param connectionId 服务端生成的全局连接编号
     * @return
     * 返回值: boolean
     * 返回值说明: true表示申请成功，false表示达到上限
     */
    public boolean acquire(String sessionId, String connectionId)
    {
        long ttl = leaseTtl();
        return Long.valueOf(1).equals(redis.execute(ACQUIRE, List.of(key(sessionId)),
                Long.toString(ttl), Integer.toString(properties.maxConnectionsPerSession()), connectionId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 按Redis统一时间维护活跃连接名额，节点长时间停顿后拒绝恢复已到期租约
     * @param sessionId 已验证的登录会话编号
     * @param connectionId 服务端连接编号
     * @return
     * 返回值: boolean
     * 返回值说明: true表示续租成功，false表示旧名额已失效
     */
    public boolean renew(String sessionId, String connectionId)
    {
        long ttl = leaseTtl();
        return Long.valueOf(1).equals(redis.execute(RENEW, List.of(key(sessionId)), Long.toString(ttl), connectionId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 只释放指定连接名额，不影响同账号其它连接
     * @param sessionId 登录会话编号
     * @param connectionId 服务端连接编号
     * @return
     * 返回值: void
     * 返回值说明: 删除该成员；Redis不可用时由租约到期兜底
     */
    public void release(String sessionId, String connectionId)
    {
        redis.opsForZSet().remove(key(sessionId), connectionId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 租约覆盖三个核对周期和空闲期限，允许短暂调度延迟
     * @return
     * 返回值: long
     * 返回值说明: 租约毫秒数
     */
    private long leaseTtl()
    {
        return Math.max(properties.idleTimeout(), properties.sessionCheckInterval() * 3L);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 连接名额与消息频道使用相同的环境隔离前缀
     * @param sessionId 服务端会话编号
     * @return
     * 返回值: String
     * 返回值说明: 不含JWT的Redis租约键
     */
    private String key(String sessionId)
    {
        return properties.channel() + ":connections:" + sessionId;
    }
}
