package com.zero.system.domain;

import com.zero.common.core.domain.BaseEntity;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 用户谷歌动态码绑定记录，对应用户二次验证绑定信息，googleAuthKey为敏感密钥，列表展示时必须脱敏
 */
public class SysGoogle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 谷歌登录密钥 */
    private String googleAuthKey;

    /** 脱敏后的谷歌登录密钥 */
    private String googleAuthKeyMask;

    /** 是否已绑定 */
    private Boolean bound;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取主键ID
     * @return
     * 返回值: Long
     * 返回值说明: 谷歌动态码绑定记录主键ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置主键ID
     * @param id 谷歌动态码绑定记录主键ID
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定记录主键ID
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取用户ID
     * @return
     * 返回值: Long
     * 返回值说明: 绑定谷歌动态码的用户ID
     */
    public Long getUserId()
    {
        return userId;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置用户ID
     * @param userId 绑定谷歌动态码的用户ID
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定记录关联的用户ID
     */
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取用户名
     * @return
     * 返回值: String
     * 返回值说明: 绑定谷歌动态码的登录用户名
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
     * @param userName 绑定谷歌动态码的登录用户名
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定记录中的用户名
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取用户昵称
     * @return
     * 返回值: String
     * 返回值说明: 绑定谷歌动态码的用户昵称
     */
    public String getNickName()
    {
        return nickName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置用户昵称
     * @param nickName 绑定谷歌动态码的用户昵称
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定记录中的用户昵称
     */
    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取谷歌登录密钥
     * @return
     * 返回值: String
     * 返回值说明: 明文谷歌登录密钥，属于敏感信息，返回前必须按接口场景脱敏或清空
     */
    public String getGoogleAuthKey()
    {
        return googleAuthKey;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置谷歌登录密钥
     * @param googleAuthKey 明文谷歌登录密钥
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定记录中的谷歌登录密钥
     */
    public void setGoogleAuthKey(String googleAuthKey)
    {
        this.googleAuthKey = googleAuthKey;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取脱敏后的谷歌登录密钥
     * @return
     * 返回值: String
     * 返回值说明: 用于列表展示的脱敏密钥片段
     */
    public String getGoogleAuthKeyMask()
    {
        return googleAuthKeyMask;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置脱敏后的谷歌登录密钥
     * @param googleAuthKeyMask 用于列表展示的脱敏密钥片段
     * @return
     * 返回值: void
     * 返回值说明: 设置绑定记录中的脱敏密钥片段
     */
    public void setGoogleAuthKeyMask(String googleAuthKeyMask)
    {
        this.googleAuthKeyMask = googleAuthKeyMask;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取绑定状态
     * @return
     * 返回值: Boolean
     * 返回值说明: true-已绑定谷歌动态码，false或null-未绑定
     */
    public Boolean getBound()
    {
        return bound;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置绑定状态
     * @param bound true-已绑定谷歌动态码，false-未绑定
     * @return
     * 返回值: void
     * 返回值说明: 设置列表展示使用的绑定状态
     */
    public void setBound(Boolean bound)
    {
        this.bound = bound;
    }
}
