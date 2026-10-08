package com.zero.common.constant;

/**
 * 谷歌动态码常量
 */
public class GoogleAuthConstants
{
    /**
     * 谷歌动态码登录开关
     */
    public static final String OPEN_GOOGLE_AUTH = "sys.account.openGoogleAuth";

    /**
     * 接口级谷歌动态码二次验证总开关，true-开启，false或未配置-关闭
     */
    public static final String API_GOOGLE_AUTH_ENABLED = "sys.api.isNeedGoogleAuth";

    /**
     * 接口级谷歌动态码请求头名称，前端弹窗后将6位动态码放入该请求头
     */
    public static final String API_GOOGLE_CODE_HEADER = "X-Google-Code";

    /**
     * 临时密钥缓存前缀
     */
    public static final String GOOGLE_BIND_CACHE_KEY = "google_bind_key:";

    /**
     * 需要绑定谷歌秘钥
     */
    public static final int GOOGLE_BIND_REQUIRED = -1001;

    /**
     * 谷歌验证码不能为空
     */
    public static final int GOOGLE_CODE_NOT_NULL = -1002;

    /**
     * 谷歌动态码校验失败
     */
    public static final int GOOGLE_CODE_ERROR = -1003;
}
