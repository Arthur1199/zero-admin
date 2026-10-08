package com.zero.utils.md5;

import com.zero.utils.crypto.CryptoDigestUtils;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 14:20
 * 说明: 历史MD5签名工具兼容入口，通用摘要和签名串拼接统一委托给CryptoDigestUtils
 */
public class Md5Utils {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 生成大写32位MD5摘要，兼容原crypto.hash.MD5Util.MD5方法
     * @param s 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 大写32位MD5字符串；异常时返回null
     */
    public final static String MD5(String s) {
        try {
            return CryptoDigestUtils.md5Upper(s, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 判断两个字符串的MD5摘要是否一致
     * @param first 第一个字符串
     * @param second 第二个字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-摘要一致，false-入参为空或摘要不一致
     */
    public static boolean theSameMd5(String first, String second) {
        if (first == null || first.trim().length() < 1 || second == null || second.trim().length() < 1) {
            return false;
        }
        return MD5(first).equals(MD5(second));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 生成小写32位MD5摘要
     * @param inStr 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5字符串
     * @throws Exception 摘要生成失败时抛出异常
     */
    public static String md5Encode(String inStr) throws Exception {
        return CryptoDigestUtils.md5Lower(inStr, "UTF-8");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 生成小写32位MD5摘要，保留原crypto.hash.MD5Util.md5的ISO-8859-1字符集语义
     * @param inStr 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5字符串
     */
    public static String md5(String inStr) {
        return CryptoDigestUtils.md5Lower(inStr, "ISO-8859-1");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 历史异或转换算法，保留给旧接口兼容
     * @param inStr 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 转换后的字符串
     */
    public static String convertMD5(String inStr) {
        return CryptoDigestUtils.convertMd5(inStr);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 历史异或还原算法
     * @param str 已转换字符串
     * @return
     * 返回值: String
     * 返回值说明: 还原后的字符串
     */
    public static String md5Decode(String str) {
        return convertMD5(convertMD5(str));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 生成大写32位MD5摘要，兼容原crypto.hash.MD5Code.getMD5ofStr方法
     * @param inbuf 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 大写32位MD5字符串
     */
    public static String getMD5ofStr(String inbuf) {
        return CryptoDigestUtils.md5Upper(inbuf, "ISO-8859-1");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 将byte按无符号方式转换为long，兼容原MD5Code辅助方法
     * @param b 字节值
     * @return
     * 返回值: long
     * 返回值说明: 无符号字节对应的long值
     */
    public static long b2iu(byte b) {
        return b < 0 ? (b & 0x7F) + 128 : b;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:42
     * 说明: 将byte转换为两位大写十六进制字符串，兼容原MD5Code辅助方法
     * @param ib 字节值
     * @return
     * 返回值: String
     * 返回值说明: 两位大写十六进制字符串
     */
    public static String byteHEX(byte ib) {
        char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                'B', 'C', 'D', 'E', 'F' };
        char[] ob = new char[2];
        ob[0] = digit[(ib >>> 4) & 0X0F];
        ob[1] = digit[ib & 0X0F];
        return new String(ob);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 拼接支付签名字符串，跳过空值、sign和sign_type
     * @param params 待签名参数
     * @return
     * 返回值: StringBuilder
     * 返回值说明: key=value&key2=value2格式签名串
     */
    public static StringBuilder CreateLinkString(Map<String, String> params) {
        return CryptoDigestUtils.createLinkString(params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 使用参数和密钥生成MD5签名
     * @param sArray 待签名参数
     * @param key 签名密钥
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5签名；参数为空时返回null
     */
    public static String BuildMysign(Map<String, String> sArray, String key) {
        if (sArray != null && sArray.size() > 0) {
            StringBuilder prestr = CreateLinkString(sArray);
            return md5(prestr.toString() + key, "UTF-8");
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 生成小写32位MD5摘要
     * @param text 原始字符串
     * @param charset 字符集名称
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5字符串
     */
    public static String md5(String text, String charset) {
        return CryptoDigestUtils.md5Lower(text, charset);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 将字节数组转换为小写十六进制字符数组
     * @param data 原始字节数组
     * @return
     * 返回值: char[]
     * 返回值说明: 小写十六进制字符数组
     */
    public static char[] encodeHex(byte[] data) {
        return CryptoDigestUtils.encodeHex(data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 拼接富友格式签名字符串
     * @param params 待签名参数
     * @return
     * 返回值: StringBuilder
     * 返回值说明: key=value|key2=value2格式签名串
     */
    public static StringBuilder CreateLinkStringForFY(Map<String, String> params) {
        return CryptoDigestUtils.createLinkStringForFy(params);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 使用富友格式参数和密钥生成MD5签名
     * @param sArray 待签名参数
     * @param key 签名密钥
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5签名；参数为空时返回null
     */
    public static String BuildMysignForFY(Map<String, String> sArray, String key) {
        if (sArray != null && sArray.size() > 0) {
            StringBuilder prestr = CreateLinkStringForFY(sArray);
            return md5(prestr.toString() + key, "UTF-8");
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 历史MD5后再Base64两次的兼容编码
     * @param str 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 兼容旧逻辑的编码结果
     * @throws Exception 摘要或编码失败时抛出异常
     */
    public static String Encode(String str) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] encode = Base64.getEncoder().encode(md5.digest(str.getBytes(StandardCharsets.UTF_8)));
        return Base64.getEncoder().encodeToString(encode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 生成小写32位MD5摘要
     * @param str 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 小写32位MD5字符串
     * @throws Exception 摘要生成失败时抛出异常
     */
    public static String Encode32(String str) throws Exception {
        return CryptoDigestUtils.md5Lower(str, "utf-8");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 生成小写16位MD5摘要
     * @param str 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: 小写16位MD5字符串
     * @throws Exception 摘要生成失败时抛出异常
     */
    public static String Encode16(String str) throws Exception {
        return CryptoDigestUtils.md5Lower16(str, "utf-8");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 14:20
     * 说明: 原逻辑为text拼接key后调用Encode，保留历史行为
     * @param text 原始字符串
     * @param key 密钥
     * @return
     * 返回值: String
     * 返回值说明: 兼容旧逻辑的编码结果
     * @throws Exception 摘要或编码失败时抛出异常
     */
    public static String Encode16(String text, String key) throws Exception {
        return Encode(text + key);
    }
}
