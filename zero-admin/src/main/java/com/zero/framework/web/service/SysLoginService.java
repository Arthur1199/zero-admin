package com.zero.framework.web.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.zero.common.constant.CacheConstants;
import com.zero.common.constant.Constants;
import com.zero.common.constant.GoogleAuthConstants;
import com.zero.common.constant.IpWhiteConstants;
import com.zero.common.constant.UserConstants;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.exception.ServiceException;
import com.zero.common.exception.user.BlackListException;
import com.zero.common.exception.user.CaptchaException;
import com.zero.common.exception.user.CaptchaExpireException;
import com.zero.common.exception.user.UserNotExistsException;
import com.zero.common.exception.user.UserPasswordNotMatchException;
import com.zero.common.utils.DateUtils;
import com.zero.common.utils.MessageUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.ip.IpUtils;
import com.zero.framework.manager.AsyncManager;
import com.zero.framework.manager.factory.AsyncFactory;
import com.zero.framework.security.context.AuthenticationContextHolder;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.ISysGoogleService;
import com.zero.system.service.ISysIpWhiteService;
import com.zero.system.service.ISysUserService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 登录校验方法
 */
@Component
public class SysLoginService
{
    /**
     * 令牌服务字段
     */
    @Autowired
    private TokenService tokenService;
    /**
     * authentication管理器字段
     */

    @Resource
    private AuthenticationManager authenticationManager;
    /**
     * Redis缓存字段
     */

    @Autowired
    private RedisCache redisCache;
    /**
     * 用户服务字段
     */

    @Autowired
    private ISysUserService userService;
    /**
     * 参数配置服务字段
     */

    @Autowired
    private ISysConfigService configService;

    /**
     * 谷歌动态码服务
     */
    @Autowired
    private ISysGoogleService googleService;

    /**
     * 登录IP白名单服务，登录开关开启后用于校验普通账号当前IP是否允许登录
     */
    @Autowired
    private ISysIpWhiteService ipWhiteService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 登录验证
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @param googleCode 谷歌动态码
     * @return
     * 返回值: 结果
     * 返回值说明：登录验证
     */
    public String login(String username, String password, String code, String uuid, String googleCode)
    {
        // 验证码校验
        validateCaptcha(username, code, uuid);
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 密码验证通过后校验登录IP白名单，admin账号保留绕过能力，避免生产IP变化导致无法进入后台维护。
        validateIpWhite(loginUser);
        // 密码验证通过后、生成令牌前校验谷歌动态码，避免未通过二次验证的用户拿到 token。
        validateGoogleCode(loginUser, googleCode);
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 05:06
     * 说明: 校验登录IP白名单；开关未开启时直接放行，admin账号绕过白名单，普通账号必须命中启用白名单
     * @param loginUser 已通过密码验证的登录用户
     * @return
     * 返回值: void
     * 返回值说明: 未命中白名单会记录登录失败日志并中断登录
     */
    public void validateIpWhite(LoginUser loginUser)
    {
        String chekIpWhite = configService.selectConfigByKey(IpWhiteConstants.CHEK_IP_WHITE);
        if (!Boolean.parseBoolean(chekIpWhite) || loginUser.getUser().isAdmin())
        {
            return;
        }
        String ipAddr = IpUtils.getIpAddr();
        if (!ipWhiteService.isLoginIpAllowed(ipAddr))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginUser.getUsername(), Constants.LOGIN_FAIL, "登录IP未加入白名单"));
            throw new ServiceException("当前IP未加入登录白名单，请联系管理员");
        }
    }

    /**
     * 校验谷歌动态码
     *
     * @param loginUser 登录用户
     * @param googleCode 谷歌动态码
     */
    public void validateGoogleCode(LoginUser loginUser, String googleCode)
    {
        String openGoogleAuth = configService.selectConfigByKey(GoogleAuthConstants.OPEN_GOOGLE_AUTH);
        // 未配置或配置为 false 时按关闭处理，和验证码开关保持一致。
        if (Boolean.parseBoolean(openGoogleAuth))
        {
            googleService.validateLoginCode(loginUser.getUserId(), loginUser.getUsername(), googleCode);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 校验验证码
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return
     * 返回值: 结果
     * 返回值说明：校验验证码
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     * @return
     * 返回值: void
     * 返回值说明：登录前置校验
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 记录登录信息
     * @param userId 用户ID
     * @return
     * 返回值: void
     * 返回值说明：记录登录信息
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
