package com.zero.common.constant;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 缓存的key 常量
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /** 按用户 ID 保存最近一次登录模式和单端会话编号的 Redis 键前缀。 */
    public static final String USER_LOGIN_SESSION_KEY = "login_user_session:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
