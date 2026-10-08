package com.zero.common.core.domain.model;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户登录对象
 */
public class LoginBody
{
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;

    /**
     * 谷歌动态码
     */
    private String googleCode;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Username
     * @return
     * 返回值: String
     * 返回值说明：获取Username
     */

    public String getUsername()
    {
        return username;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Username
     * @param username username参数
     * @return
     * 返回值: void
     * 返回值说明：设置Username
     */

    public void setUsername(String username)
    {
        this.username = username;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取密码
     * @return
     * 返回值: String
     * 返回值说明：获取密码
     */

    public String getPassword()
    {
        return password;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置密码
     * @param password 密码参数
     * @return
     * 返回值: void
     * 返回值说明：设置密码
     */

    public void setPassword(String password)
    {
        this.password = password;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取编码
     * @return
     * 返回值: String
     * 返回值说明：获取编码
     */

    public String getCode()
    {
        return code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置编码
     * @param code 编码参数
     * @return
     * 返回值: void
     * 返回值说明：设置编码
     */

    public void setCode(String code)
    {
        this.code = code;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Uuid
     * @return
     * 返回值: String
     * 返回值说明：获取Uuid
     */

    public String getUuid()
    {
        return uuid;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Uuid
     * @param uuid uuid参数
     * @return
     * 返回值: void
     * 返回值说明：设置Uuid
     */

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    /**
     * 获取谷歌动态码
     *
     * @return 谷歌动态码
     */
    public String getGoogleCode()
    {
        return googleCode;
    }

    /**
     * 设置谷歌动态码
     *
     * @param googleCode 谷歌动态码
     */
    public void setGoogleCode(String googleCode)
    {
        this.googleCode = googleCode;
    }
}
