package com.zero.utils.security.google;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Locale;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: GoogleAuthenticator 工具类
 */
public class GoogleAuthenticatorUtils {

    /**
     * Base32编码字符表，遵循Google Authenticator使用的RFC4648格式。
     */
    private static final char[] BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    /**
     * 时间前后偏移量
     * 用于防止客户端时间不精确导致生成的TOTP与服务器端的TOTP一直不一致
     * 如果为0,当前时间为 10:10:15
     * 则表明在 10:10:00-10:10:30 之间生成的TOTP 能校验通过
     * 如果为1,则表明在
     * 10:09:30-10:10:00
     * 10:10:00-10:10:30
     * 10:10:30-10:11:00 之间生成的TOTP 能校验通过
     * 以此类推
     */
    private static final int timeExcursion = 0;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成用于动态码绑定的共享密钥。
     * @return
     * 返回值: String
     * 返回值说明: 经过 Base32 编码的密钥文本，禁止输出到日志
     */
    public static String createSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32Encode(bytes).toLowerCase(Locale.ROOT);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验方法
     * @param secretKey 密钥
     * @param code 用户输入的TOTP
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public static boolean verify(String secretKey, String code) {
        long time = System.currentTimeMillis() / 1000 / 30;
        for (int i = -timeExcursion; i <= timeExcursion; i++) {
            String totp = getTOTP(secretKey, time + i);
            if (code.equals(totp)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据密钥获取验证码
     *
     * 返回字符串是因为数值有可能以0开头
     * @param secretKey 密钥
     * @param time 第几个30秒 System.currentTimeMillis() / 1000 / 30
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public static String getTOTP(String secretKey, long time) {
        byte[] bytes = base32Decode(secretKey);
        String hexKey = encodeHexString(bytes);
        String hexTime = Long.toHexString(time);
        return TOTP.generateTOTP(hexKey, hexTime, "6");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 组装身份验证器绑定二维码承载的配置地址。
     * @param secret 动态码共享密钥
     * @param account 绑定账号的显示名称
     * @param issuer 用于区分系统的发行方名称
     * @return
     * 返回值: String
     * 返回值说明: 用于生成绑定二维码的 otpauth 文本
     */
    public static String createGoogleAuthQRCodeData(String secret, String account, String issuer) {
        String qrCodeData = "otpauth://totp/%s?secret=%s&issuer=%s";
        try {
            return String.format(
                qrCodeData, URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20"), URLEncoder.encode(secret, "UTF-8")
                    .replace("+", "%20"),
                URLEncoder.encode(issuer, "UTF-8").replace("+", "%20")
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:41
     * 说明: 将随机密钥字节数组编码为Base32字符串，替代第三方Base32依赖
     * @param data 原始密钥字节数组
     * @return
     * 返回值: String
     * 返回值说明: 不带换行的Base32字符串；入参为空时返回空字符串
     */
    private static String base32Encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte datum : data) {
            buffer = (buffer << 8) | (datum & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                result.append(BASE32_CHARS[(buffer >> (bitsLeft - 5)) & 0x1f]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            result.append(BASE32_CHARS[(buffer << (5 - bitsLeft)) & 0x1f]);
        }
        return result.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:41
     * 说明: 将Google Authenticator密钥从Base32解码为字节数组，兼容大小写和末尾填充符
     * @param secretKey Google Authenticator密钥
     * @return
     * 返回值: byte[]
     * 返回值说明: 解码后的密钥字节数组；空密钥返回空数组
     */
    private static byte[] base32Decode(String secretKey) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            return new byte[0];
        }
        String normalized = secretKey.replace("=", "").replace(" ", "").toUpperCase(Locale.ROOT);
        byte[] result = new byte[normalized.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (int i = 0; i < normalized.length(); i++) {
            int value = base32Value(normalized.charAt(i));
            if (value < 0) {
                continue;
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        if (index == result.length) {
            return result;
        }
        byte[] realResult = new byte[index];
        System.arraycopy(result, 0, realResult, 0, index);
        return realResult;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:41
     * 说明: 获取Base32字符对应的数值
     * @param c Base32字符
     * @return
     * 返回值: int
     * 返回值说明: 合法字符返回0-31，非法字符返回-1
     */
    private static int base32Value(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        }
        if (c >= '2' && c <= '7') {
            return c - '2' + 26;
        }
        return -1;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:41
     * 说明: 将字节数组转换为小写十六进制字符串，供TOTP算法使用
     * @param data 原始字节数组
     * @return
     * 返回值: String
     * 返回值说明: 小写十六进制字符串；空数组返回空字符串
     */
    private static String encodeHexString(byte[] data) {
        char[] digits = "0123456789abcdef".toCharArray();
        char[] out = new char[data.length << 1];
        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = digits[(data[i] >>> 4) & 0x0f];
            out[j++] = digits[data[i] & 0x0f];
        }
        return new String(out);
    }

}
