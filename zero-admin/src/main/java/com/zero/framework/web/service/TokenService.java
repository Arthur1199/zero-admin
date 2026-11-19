package com.zero.framework.web.service;

import lombok.extern.slf4j.Slf4j;
import com.zero.common.constant.CacheConstants;
import com.zero.common.constant.Constants;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.utils.ServletUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.AddressUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.common.utils.uuid.IdUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: token验证处理
 */
@Slf4j
@Component
public class TokenService
{
    /**
     * log字段
     */
    // 令牌自定义标识
    /**
     * header字段
     */
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    /**
     * secret字段
     */
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    /**
     * expire时间字段
     */
    @Value("${token.expireTime}")
    private int expireTime;
    /**
     * MILLISSECOND字段
     */

    protected static final long MILLIS_SECOND = 1000;
    /**
     * MILLISMINUTE字段
     */

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    /**
     * MILLISMINUTETWENTY字段
     */

    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;
    /**
     * Redis缓存字段
     */

    @Autowired
    private RedisCache redisCache;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户身份信息
     * @param request 请求参数
     * @return
     * 返回值: 用户信息
     * 返回值说明：获取用户身份信息
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                LoginUser user = redisCache.getCacheObject(userKey);
                return user;
            }
            catch (Exception e)
            {
                log.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户身份信息
     * @param loginUser 登录用户参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 验证WebSocket首帧JWT并读取现有登录会话，不续期、不记录凭证或解析异常原文
     * @param token 浏览器首帧发送的原始JWT，不含Bearer前缀
     * @return
     * 返回值: LoginUser
     * 返回值说明: 有效登录会话；凭证无效返回null，Redis异常交由通信层关闭连接
     */
    public LoginUser authenticateWebSocketToken(String token)
    {
        if (!StringUtils.hasText(token))
        {
            return null;
        }
        String sessionId;
        try
        {
            sessionId = parseToken(token).get(Constants.LOGIN_USER_KEY, String.class);
        }
        catch (io.jsonwebtoken.JwtException | IllegalArgumentException e)
        {
            return null;
        }
        return getWebSocketLoginUser(sessionId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16
     * 说明: 按已验证的登录会话编号核对缓存及到期时间，不修改登录状态
     * @param sessionId 从有效JWT解析的登录会话编号，不能使用客户端指定用户ID代替
     * @return
     * 返回值: LoginUser
     * 返回值说明: 有效会话；缓存缺失、数据不完整或过期返回null，基础设施异常向上传递
     */
    public LoginUser getWebSocketLoginUser(String sessionId)
    {
        if (!StringUtils.hasText(sessionId))
        {
            return null;
        }
        LoginUser user = redisCache.getCacheObject(getTokenKey(sessionId));
        if (user == null || user.getUser() == null || user.getUserId() == null
                || !sessionId.equals(user.getToken()) || user.getExpireTime() == null
                || user.getExpireTime() <= System.currentTimeMillis())
        {
            return null;
        }
        return user;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 删除用户身份信息
     * @param token 令牌参数
     * @return
     * 返回值: void
     * 返回值说明：删除用户身份信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 创建令牌
     * @param loginUser 用户信息
     * @return
     * 返回值: 令牌
     * 返回值说明：创建令牌
     */
    public String createToken(LoginUser loginUser)
    {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createToken(claims);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 14:56
     * 说明: 为已通过在线用户列表权限校验的调用方还原现有会话JWT，不创建会话、不刷新缓存或有效期
     * @param loginUser 从登录缓存读取的现有会话，用户或会话编号缺失时返回空字符串
     * @return
     * 返回值: String
     * 返回值说明: 不含Bearer前缀的完整JWT，仅允许在授权响应中展示，不得记录日志
     */
    public String getExistingToken(LoginUser loginUser)
    {
        if (loginUser == null || loginUser.getUser() == null
                || StringUtils.isEmpty(loginUser.getToken()) || StringUtils.isEmpty(loginUser.getUsername()))
        {
            return "";
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, loginUser.getToken());
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createToken(claims);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 验证令牌有效期，相差不足20分钟，自动刷新缓存
     * @param loginUser 登录信息
     * @return
     * 返回值: 令牌
     * 返回值说明：验证令牌有效期，相差不足20分钟，自动刷新缓存
     */
    public void verifyToken(LoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginUser);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 刷新令牌有效期
     * @param loginUser 登录信息
     * @return
     * 返回值: void
     * 返回值说明：刷新令牌有效期
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户代理信息
     * @param loginUser 登录信息
     * @return
     * 返回值: void
     * 返回值说明：设置用户代理信息
     */
    public void setUserAgent(LoginUser loginUser)
    {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 从数据声明生成令牌
     * @param claims 数据声明
     * @return
     * 返回值: 令牌
     * 返回值说明：从数据声明生成令牌
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                .claims(claims)
                .signWith(getSecretKey(), Jwts.SIG.HS512).compact();
        return token;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 从令牌中获取数据声明
     * @param token 令牌
     * @return
     * 返回值: 数据声明
     * 返回值说明：从令牌中获取数据声明
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:08
     * 说明: 生成JWT HS512签名密钥，先用SHA-512派生固定长度密钥，避免配置密钥过短导致新版jjwt拒绝签名
     * @return
     * 返回值: SecretKey
     * 返回值说明：可用于HS512签名和验签的密钥
     */
    private SecretKey getSecretKey()
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return Keys.hmacShaKeyFor(digest.digest(secret.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("生成JWT签名密钥失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 从令牌中获取用户名
     * @param token 令牌
     * @return
     * 返回值: 用户名
     * 返回值说明：从令牌中获取用户名
     */
    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取请求token
     * @param request 请求参数
     * @return
     * 返回值: token
     * 返回值说明：获取请求token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取令牌键名
     * @param uuid uuid参数
     * @return
     * 返回值: String
     * 返回值说明：获取令牌键名
     */

    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }
}
