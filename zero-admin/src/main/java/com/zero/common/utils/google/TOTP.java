package com.zero.common.utils.google;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: TOTP算法工具，按RFC 6238思路根据密钥和时间窗口生成一次性动态码
 */
public class TOTP
{
    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 私有构造方法，避免算法工具类被实例化
     * @return
     * 返回值: TOTP
     * 返回值说明: 不允许外部创建实例
     */
    private TOTP()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成 TOTP 动态码，默认使用 HmacSHA1
     * @param key 十六进制密钥
     * @param time 十六进制时间窗口
     * @param returnDigits 返回位数
     * @return
     * 返回值: String
     * 返回值说明: 动态码
     */
    public static String generateTOTP(String key, String time, String returnDigits)
    {
        return generateTOTP(key, time, returnDigits, "HmacSHA1");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成指定摘要算法的 TOTP 动态码
     * @param key 十六进制密钥
     * @param time 十六进制时间窗口
     * @param returnDigits 返回位数
     * @param crypto HMAC 摘要算法
     * @return
     * 返回值: String
     * 返回值说明: 动态码
     */
    public static String generateTOTP(String key, String time, String returnDigits, String crypto)
    {
        int codeDigits = Integer.decode(returnDigits);
        while (time.length() < 16)
        {
            time = "0" + time;
        }
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);
        byte[] hash = hmacSha(crypto, k, msg);
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[codeDigits];
        String result = Integer.toString(otp);
        while (result.length() < codeDigits)
        {
            result = "0" + result;
        }
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 使用指定HMAC算法计算摘要
     * @param crypto HMAC摘要算法，例如HmacSHA1
     * @param keyBytes 密钥字节数组
     * @param text 时间窗口字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: HMAC摘要结果；算法不可用时抛出运行期封装异常
     */
    private static byte[] hmacSha(String crypto, byte[] keyBytes, byte[] text)
    {
        try
        {
            Mac hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        }
        catch (GeneralSecurityException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 将十六进制字符串转换为字节数组
     * @param hex 十六进制字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: 转换后的字节数组
     */
    private static byte[] hexStr2Bytes(String hex)
    {
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = bArray[i + 1];
        }
        return ret;
    }
}
