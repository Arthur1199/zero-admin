package com.zero.common.utils.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: Google Authenticator工具，负责生成谷歌动态码密钥、校验6位动态码和生成绑定二维码内容
 */
public class GoogleAuthenticatorUtils
{
    /**
     * 允许前后各一个 30 秒窗口，降低客户端时间轻微偏差导致的误判。
     */
    private static final int TIME_EXCURSION = 1;

    private GoogleAuthenticatorUtils()
    {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成谷歌动态码密钥
     * @return
     * 返回值: String
     * 返回值说明: Base32 格式密钥
     */
    public static String createSecretKey()
    {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base32Utils.encode(bytes).toLowerCase();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验 6 位谷歌动态码
     * @param secretKey 谷歌密钥
     * @param code 用户输入动态码
     * @return
     * 返回值: boolean
     * 返回值说明: true 校验通过，false 校验失败
     */
    public static boolean verify(String secretKey, String code)
    {
        // 谷歌动态码固定为 6 位数字，先做格式校验可避免无效输入参与算法计算。
        if (secretKey == null || secretKey.trim().isEmpty() || code == null || !code.matches("\\d{6}"))
        {
            return false;
        }
        long time = System.currentTimeMillis() / 1000 / 30;
        for (int i = -TIME_EXCURSION; i <= TIME_EXCURSION; i++)
        {
            if (code.equals(getTOTP(secretKey, time + i)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据指定时间窗口生成动态码
     * @param secretKey 谷歌密钥
     * @param time 30 秒时间窗口
     * @return
     * 返回值: String
     * 返回值说明: 6 位动态码
     */
    public static String getTOTP(String secretKey, long time)
    {
        byte[] bytes = Base32Utils.decode(secretKey);
        String hexKey = bytesToHex(bytes);
        String hexTime = Long.toHexString(time);
        return TOTP.generateTOTP(hexKey, hexTime, "6");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成绑定二维码内容
     * @param secret 谷歌密钥
     * @param account 账号
     * @param issuer 签发方
     * @return
     * 返回值: String
     * 返回值说明: otpauth URI
     */
    public static String createGoogleAuthQRCodeData(String secret, String account, String issuer)
    {
        // otpauth URI 是 Google Authenticator 等动态码工具识别二维码的标准格式。
        String label = encode(issuer + ":" + account);
        return "otpauth://totp/" + label + "?secret=" + encode(secret) + "&issuer=" + encode(issuer);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: URL编码otpauth二维码参数，空格固定编码为%20以兼容扫码工具
     * @param value 待编码内容
     * @return
     * 返回值: String
     * 返回值说明: UTF-8编码后的URL参数值
     */
    private static String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 将字节数组转换为十六进制字符串，供TOTP算法输入使用
     * @param bytes 原始字节数组
     * @return
     * 返回值: String
     * 返回值说明: 小写十六进制字符串
     */
    private static String bytesToHex(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte item : bytes)
        {
            builder.append(String.format("%02x", item));
        }
        return builder.toString();
    }
}
