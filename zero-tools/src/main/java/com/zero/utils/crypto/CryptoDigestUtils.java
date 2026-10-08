package com.zero.utils.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 14:16
 * 说明: 摘要算法统一工具，集中提供MD5摘要、签名串拼接和历史兼容算法
 */
public final class CryptoDigestUtils {

    /**
     * 小写十六进制字符表
     */
    private static final char[] LOWER_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 大写十六进制字符表
     */
    private static final char[] UPPER_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private CryptoDigestUtils() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 生成小写32位MD5摘要
     * @param text 原始字符串
     * @param charset 字符集名称
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5字符串
     */
    public static String md5Lower(String text, String charset) {
        return hex(digest(text, charset), LOWER_DIGITS);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 生成大写32位MD5摘要
     * @param text 原始字符串
     * @param charset 字符集名称
     * @return
     * 返回值: String
     * 返回值说明: 大写32位MD5字符串
     */
    public static String md5Upper(String text, String charset) {
        return hex(digest(text, charset), UPPER_DIGITS);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 生成小写16位MD5摘要
     * @param text 原始字符串
     * @param charset 字符集名称
     * @return
     * 返回值: String
     * 返回值说明: 小写16位MD5字符串
     */
    public static String md5Lower16(String text, String charset) {
        return md5Lower(text, charset).substring(8, 24);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 将字节数组转换为十六进制字符数组
     * @param data 原始字节数组
     * @return
     * 返回值: char[]
     * 返回值说明: 小写十六进制字符数组
     */
    public static char[] encodeHex(byte[] data) {
        return hex(data, LOWER_DIGITS).toCharArray();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 拼接支付签名字符串，跳过空值、sign和sign_type
     * @param params 待签名参数
     * @return
     * 返回值: StringBuilder
     * 返回值说明: key=value&key2=value2格式签名串
     */
    public static StringBuilder createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder preset = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value == null || value.isEmpty() || key.equalsIgnoreCase("sign")
                    || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            preset.append(key).append("=").append(value).append("&");
        }
        return preset.isEmpty() ? preset : preset.deleteCharAt(preset.length() - 1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 拼接富友格式签名字符串
     * @param params 待签名参数
     * @return
     * 返回值: StringBuilder
     * 返回值说明: key=value|key2=value2格式签名串
     */
    public static StringBuilder createLinkStringForFy(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder prestr = new StringBuilder();
        for (String key : keys) {
            prestr.append(key).append("=").append(params.get(key)).append("|");
        }
        return prestr.isEmpty() ? prestr : prestr.deleteCharAt(prestr.length() - 1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:16
     * 说明: 历史异或转换算法，保留给旧接口兼容
     * @param text 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 异或转换后的字符串
     */
    public static String convertMd5(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ 't');
        }
        return new String(chars);
    }

    private static byte[] digest(String text, String charset) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(text.getBytes(charset));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("System doesn't support MD5 algorithm.", e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("System doesn't support your EncodingException.", e);
        }
    }

    private static String hex(byte[] data, char[] digits) {
        char[] out = new char[data.length << 1];
        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = digits[(0xF0 & data[i]) >>> 4];
            out[j++] = digits[0x0F & data[i]];
        }
        return new String(out);
    }
}
