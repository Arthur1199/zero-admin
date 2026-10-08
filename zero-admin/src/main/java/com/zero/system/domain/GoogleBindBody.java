package com.zero.system.domain;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 谷歌动态码绑定参数，接收用户绑定谷歌动态码时提交的用户名和6位动态码
 */
public class GoogleBindBody
{
    /** 用户名 */
    private String userName;

    /** 谷歌动态码 */
    private String googleCode;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取用户名
     * @return
     * 返回值: String
     * 返回值说明: 用户名
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置用户名
     * @param userName 用户名
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定参数中的用户名
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取谷歌动态码
     * @return
     * 返回值: String
     * 返回值说明: 用户输入的6位谷歌动态码
     */
    public String getGoogleCode()
    {
        return googleCode;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置谷歌动态码
     * @param googleCode 用户输入的6位谷歌动态码
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定参数中的谷歌动态码
     */
    public void setGoogleCode(String googleCode)
    {
        this.googleCode = googleCode;
    }
}
