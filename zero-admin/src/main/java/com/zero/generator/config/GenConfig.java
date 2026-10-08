package com.zero.generator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 读取代码生成相关配置
 */
@Component
@ConfigurationProperties(prefix = "gen")
@PropertySource(value = { "classpath:generator.yml" })
public class GenConfig
{
    /**
     * author字段
     */
    public static String author;

    /**
     * 生成包路径
     */
    public static String packageName;

    /**
     * 自动去除表前缀
     */
    public static boolean autoRemovePre;

    /**
     * 表前缀
     */
    public static String tablePrefix;

    /**
     * 是否允许生成文件覆盖到本地（自定义路径）
     */
    public static boolean allowOverwrite;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Author
     * @return
     * 返回值: String
     * 返回值说明：获取Author
     */

    public static String getAuthor()
    {
        return author;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Author
     * @param author author参数
     * @return
     * 返回值: void
     * 返回值说明：设置Author
     */

    @Value("${author}")
    public void setAuthor(String author)
    {
        GenConfig.author = author;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Package名称
     * @return
     * 返回值: String
     * 返回值说明：获取Package名称
     */

    public static String getPackageName()
    {
        return packageName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Package名称
     * @param packageName package名称参数
     * @return
     * 返回值: void
     * 返回值说明：设置Package名称
     */

    @Value("${packageName}")
    public void setPackageName(String packageName)
    {
        GenConfig.packageName = packageName;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Auto删除Pre
     * @return
     * 返回值: boolean
     * 返回值说明：获取Auto删除Pre
     */

    public static boolean getAutoRemovePre()
    {
        return autoRemovePre;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Auto删除Pre
     * @param autoRemovePre auto删除Pre参数
     * @return
     * 返回值: void
     * 返回值说明：设置Auto删除Pre
     */

    @Value("${autoRemovePre}")
    public void setAutoRemovePre(boolean autoRemovePre)
    {
        GenConfig.autoRemovePre = autoRemovePre;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取表Prefix
     * @return
     * 返回值: String
     * 返回值说明：获取表Prefix
     */

    public static String getTablePrefix()
    {
        return tablePrefix;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置表Prefix
     * @param tablePrefix 表Prefix参数
     * @return
     * 返回值: void
     * 返回值说明：设置表Prefix
     */

    @Value("${tablePrefix}")
    public void setTablePrefix(String tablePrefix)
    {
        GenConfig.tablePrefix = tablePrefix;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断AllowOverwrite
     * @return
     * 返回值: boolean
     * 返回值说明：判断AllowOverwrite
     */

    public static boolean isAllowOverwrite()
    {
        return allowOverwrite;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置AllowOverwrite
     * @param allowOverwrite allowOverwrite参数
     * @return
     * 返回值: void
     * 返回值说明：设置AllowOverwrite
     */

    @Value("${allowOverwrite}")
    public void setAllowOverwrite(boolean allowOverwrite)
    {
        GenConfig.allowOverwrite = allowOverwrite;
    }
}
