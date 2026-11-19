package com.zero.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 读取项目相关配置
 */
@Component
@ConfigurationProperties(prefix = "zero")
public class ZeroConfig
{
    /** 管理后台项目名称。 */
    private String name;

    /** 管理后台版本号。 */
    private String version;

    /** 项目版权年份配置。 */
    private String copyrightYear;

    /** 本地文件存储根目录，用于上传文件和静态资源映射。 */
    private static String profile;

    /** 登录验证码类型，math 为算术题，char 为字符。 */
    private static String captchaType;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 获取项目名称
     * @return
     * 返回值: String
     * 返回值说明：项目名称
     */
    public String getName()
    {
        return name;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 设置项目名称
     * @param name 项目名称
     * @return
     * 返回值: void
     * 返回值说明：更新项目名称配置
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 获取项目版本
     * @return
     * 返回值: String
     * 返回值说明：项目版本
     */
    public String getVersion()
    {
        return version;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 设置项目版本
     * @param version 项目版本
     * @return
     * 返回值: void
     * 返回值说明：更新项目版本配置
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 获取版权年份
     * @return
     * 返回值: String
     * 返回值说明：版权年份
     */
    public String getCopyrightYear()
    {
        return copyrightYear;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 设置版权年份
     * @param copyrightYear 版权年份
     * @return
     * 返回值: void
     * 返回值说明：更新版权年份配置
     */
    public void setCopyrightYear(String copyrightYear)
    {
        this.copyrightYear = copyrightYear;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 获取文件上传根路径
     * @return
     * 返回值: String
     * 返回值说明：文件上传根路径
     */
    public static String getProfile()
    {
        return profile;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:12
     * 说明: 获取验证码类型
     * @return
     * 返回值: String
     * 返回值说明：验证码类型
     */
    public static String getCaptchaType()
    {
        return captchaType;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Profile
     * @param profile profile参数
     * @return
     * 返回值: void
     * 返回值说明：设置Profile
     */

    public void setProfile(String profile)
    {
        ZeroConfig.profile = profile;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置验证码类型
     * @param captchaType 验证码类型参数
     * @return
     * 返回值: void
     * 返回值说明：设置验证码类型
     */

    public void setCaptchaType(String captchaType) {
        ZeroConfig.captchaType = captchaType;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取导入上传路径
     * @return
     * 返回值: String
     * 返回值说明：获取导入上传路径
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取头像上传路径
     * @return
     * 返回值: String
     * 返回值说明：获取头像上传路径
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取下载路径
     * @return
     * 返回值: String
     * 返回值说明：获取下载路径
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取上传路径
     * @return
     * 返回值: String
     * 返回值说明：获取上传路径
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }
}
