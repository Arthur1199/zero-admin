package com.zero.utils.security.google;

/**
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted pursuant to, and subject to the license
 * terms contained in, the Simplified BSD License set forth in Section
 * 4.c of the IETF Trust's Legal Provisions Relating to IETF Documents
 * (http://trustee.ietf.org/license-info).
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:42
 * 说明: 根据共享密钥和时间计数器计算 OATH 一次性密码。
 */

public class TOTP {

    private TOTP() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对计数器数据计算指定算法的 HMAC。
     * @param crypto JCE 支持的 HMAC 算法名称
     * @param keyBytes 动态码共享密钥字节
     * @param text 用于计算摘要的计数器字节
     * @return
     * 返回值: byte[]
     * 返回值说明: HMAC 摘要字节
     */

    private static byte[] hmac_sha(String crypto, byte[] keyBytes,
                                   byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey =
                    new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将十六进制字符组成的文本转换为字节数组。
     * @param hex 要解码的十六进制文本
     * @return
     * 返回值: byte[]
     * 返回值说明: 对应的二进制字节
     * @return: a byte array
     */

    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i + 1];
        return ret;
    }

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 HmacSHA1 根据共享密钥和时间计数器计算一次性密码。
     * @param key 十六进制编码的共享密钥
     * @param time 十六进制编码的时间计数器
     * @param returnDigits 输出码的位数文本
     * @return
     * 返回值: String
     * 返回值说明: 按指定长度补齐前导零的数字字符串
     * @return: a numeric String in base 10 that includes
     * {@link truncationDigits} digits
     */

    public static String generateTOTP(String key,
                                      String time,
                                      String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA1");
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 HmacSHA256 根据共享密钥和时间计数器计算一次性密码。
     * @param key 十六进制编码的共享密钥
     * @param time 十六进制编码的时间计数器
     * @param returnDigits 输出码的位数文本
     * @return
     * 返回值: String
     * 返回值说明: 按指定长度补齐前导零的数字字符串
     * @return: a numeric String in base 10 that includes
     * {@link truncationDigits} digits
     */

    public static String generateTOTP256(String key,
                                         String time,
                                         String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA256");
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 HmacSHA512 根据共享密钥和时间计数器计算一次性密码。
     * @param key 十六进制编码的共享密钥
     * @param time 十六进制编码的时间计数器
     * @param returnDigits 输出码的位数文本
     * @return
     * 返回值: String
     * 返回值说明: 按指定长度补齐前导零的数字字符串
     * @return: a numeric String in base 10 that includes
     * {@link truncationDigits} digits
     */

    public static String generateTOTP512(String key,
                                         String time,
                                         String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA512");
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对时间计数器计算 HMAC，再按动态截取及位数约定生成一次性密码。
     * @param key 十六进制编码的共享密钥
     * @param time 十六进制编码的时间计数器
     * @param returnDigits 输出码的位数文本
     * @param crypto JCE 支持的 HMAC 算法名称
     * @return
     * 返回值: String
     * 返回值说明: 按指定长度补齐前导零的数字字符串
     * @return: a numeric String in base 10 that includes
     * {@link truncationDigits} digits
     */

    public static String generateTOTP(String key,
                                      String time,
                                      String returnDigits,
                                      String crypto) {
        int codeDigits = Integer.decode(returnDigits).intValue();
        String result = null;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        while (time.length() < 16)
            time = "0" + time;

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);
        byte[] hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }
}
