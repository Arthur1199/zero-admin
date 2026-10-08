package com.zero.common.core.text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import com.zero.common.utils.StringUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 字符集工具类
 */
public class CharsetKit
{
    /**
     * ISO-8859-1
     */
    public static final String ISO_8859_1 = "ISO-8859-1";
    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";
    /**
     * GBK
     */
    public static final String GBK = "GBK";

    /** ISO-8859-1 */
    public static final Charset CHARSET_ISO_8859_1 = Charset.forName(ISO_8859_1);
    /** UTF-8 */
    public static final Charset CHARSET_UTF_8 = Charset.forName(UTF_8);
    /** GBK */
    public static final Charset CHARSET_GBK = Charset.forName(GBK);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 转换为Charset对象
     * @param charset 字符集，为空则返回默认字符集
     * @return
     * 返回值: Charset
     * 返回值说明：转换为Charset对象
     */
    public static Charset charset(String charset)
    {
        return StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 转换字符串的字符集编码
     * @param source 字符串
     * @param srcCharset 源字符集，默认ISO-8859-1
     * @param destCharset 目标字符集，默认UTF-8
     * @return
     * 返回值: 转换后的字符集
     * 返回值说明：转换字符串的字符集编码
     */
    public static String convert(String source, String srcCharset, String destCharset)
    {
        return convert(source, Charset.forName(srcCharset), Charset.forName(destCharset));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 转换字符串的字符集编码
     * @param source 字符串
     * @param srcCharset 源字符集，默认ISO-8859-1
     * @param destCharset 目标字符集，默认UTF-8
     * @return
     * 返回值: 转换后的字符集
     * 返回值说明：转换字符串的字符集编码
     */
    public static String convert(String source, Charset srcCharset, Charset destCharset)
    {
        if (null == srcCharset)
        {
            srcCharset = StandardCharsets.ISO_8859_1;
        }

        if (null == destCharset)
        {
            destCharset = StandardCharsets.UTF_8;
        }

        if (StringUtils.isEmpty(source) || srcCharset.equals(destCharset))
        {
            return source;
        }
        return new String(source.getBytes(srcCharset), destCharset);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: systemCharset
     * @return
     * 返回值: 系统字符集编码
     * 返回值说明：systemCharset
     */
    public static String systemCharset()
    {
        return Charset.defaultCharset().name();
    }
}
