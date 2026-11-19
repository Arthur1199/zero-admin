package com.zero.common.utils.google;

import java.io.ByteArrayOutputStream;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: Base32编解码工具，用于谷歌动态码密钥在字节数组和Base32字符串之间转换
 */
public class Base32Utils
{
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 私有构造方法，避免工具类被实例化
     * @return
     * 返回值: Base32Utils
     * 返回值说明: 不允许外部创建实例
     */
    private Base32Utils()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将字节数组编码为 Base32 字符串
     * @param data 原始字节
     * @return
     * 返回值: String
     * 返回值说明: Base32 字符串
     */
    public static String encode(byte[] data)
    {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte item : data)
        {
            buffer = (buffer << 8) | (item & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5)
            {
                result.append(ALPHABET[(buffer >> (bitsLeft - 5)) & 0x1f]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0)
        {
            result.append(ALPHABET[(buffer << (5 - bitsLeft)) & 0x1f]);
        }
        return result.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将 Base32 字符串解码为字节数组
     * @param value Base32 字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: 原始字节
     */
    public static byte[] decode(String value)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0;
        int bitsLeft = 0;
        for (char c : value.toUpperCase().replace("=", "").toCharArray())
        {
            int index = decodeChar(c);
            if (index < 0)
            {
                continue;
            }
            buffer = (buffer << 5) | index;
            bitsLeft += 5;
            if (bitsLeft >= 8)
            {
                out.write((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return out.toByteArray();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 解码单个Base32字符
     * @param c Base32字符
     * @return
     * 返回值: int
     * 返回值说明: 字符对应的5位数值；非法字符返回-1
     */
    private static int decodeChar(char c)
    {
        if (c >= 'A' && c <= 'Z')
        {
            return c - 'A';
        }
        if (c >= '2' && c <= '7')
        {
            return c - '2' + 26;
        }
        return -1;
    }
}
