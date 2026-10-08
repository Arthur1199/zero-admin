package com.zero.utils.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 08:55
 * 说明: 历史DES工具兼容入口，内部已升级为AES-GCM加密，保留原类名和方法名方便旧代码迁移
 */
public class DESUtils {

    /**
     * AES-GCM算法名称，替代不安全的DES-CBC。
     */
    private static final String AES_GCM = "AES/GCM/NoPadding";

    /**
     * AES算法名称。
     */
    private static final String AES = "AES";

    /**
     * GCM认证标签长度，单位bit。
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM推荐随机向量长度，单位byte。
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 安全随机数生成器，用于每次加密生成随机IV。
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 历史密钥字段，现在作为AES-GCM密钥派生输入。
     */
    private String keys;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 获取当前密钥原文
     * @return
     * 返回值: String
     * 返回值说明: 当前密钥字符串
     */
    public String getKeys() {
        return keys;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 设置加解密密钥，内部会通过SHA-256派生为AES-256密钥
     * @param keys 密钥字符串，不能为空
     * 返回值: void
     * 返回值说明: 更新当前工具对象使用的密钥
     */
    public void setKeys(String keys) {
        this.keys = keys;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 创建未设置密钥的加密工具对象，调用加解密前需要先设置keys
     */
    public DESUtils() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 使用指定密钥创建加密工具对象
     * @param keys 密钥字符串，内部派生为AES-GCM密钥
     */
    public DESUtils(String keys) {
        this.keys = keys;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 使用AES-GCM加密字节数组，返回内容格式为IV+密文+认证标签
     * @param plainText 待加密明文字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: AES-GCM加密后的字节数组，前12字节为随机IV
     * @throws Exception 密钥为空或加密失败时抛出异常
     */
    public byte[] desEncrypt(byte[] plainText) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, buildKey(keys), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plainText);
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 使用AES-GCM解密字节数组，入参格式必须为IV+密文+认证标签
     * @param encryptText 待解密密文字节数组，前12字节为IV
     * @return
     * 返回值: byte[]
     * 返回值说明: 解密后的明文字节数组
     * @throws Exception 密钥为空、密文格式错误或认证失败时抛出异常
     */
    public byte[] desDecrypt(byte[] encryptText) throws Exception {
        if (encryptText == null || encryptText.length <= GCM_IV_LENGTH) {
            throw new IllegalArgumentException("encryptText must contain iv and cipher text");
        }
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encrypted = new byte[encryptText.length - GCM_IV_LENGTH];
        System.arraycopy(encryptText, 0, iv, 0, iv.length);
        System.arraycopy(encryptText, iv.length, encrypted, 0, encrypted.length);
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.DECRYPT_MODE, buildKey(keys), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(encrypted);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 加密UTF-8字符串并输出大写十六进制密文
     * @param input 待加密明文字符串
     * @return
     * 返回值: String
     * 返回值说明: 大写十六进制AES-GCM密文，内容为IV+密文+认证标签
     * @throws Exception 密钥为空或加密失败时抛出异常
     */
    public String encrypt(String input) throws Exception {
        return parseByte2HexStr(desEncrypt(input.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 解密十六进制AES-GCM密文并返回UTF-8明文
     * @param input 十六进制AES-GCM密文
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     * @throws Exception 密钥为空、密文格式错误或认证失败时抛出异常
     */
    public String decrypt(String input) throws Exception {
        return new String(desDecrypt(parseHexStr2Byte(input)), StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 将十六进制字符串转换为字节数组
     * @param hexStr 十六进制字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: 转换后的字节数组；入参为空时返回空数组
     */
    public byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) {
            return new byte[0];
        }
        if ((hexStr.length() & 1) == 1) {
            throw new IllegalArgumentException("hexStr length must be even");
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 将字节数组转换为大写十六进制字符串
     * @param buf 待转换字节数组
     * @return
     * 返回值: String
     * 返回值说明: 大写十六进制字符串；入参为空时返回空字符串
     */
    public String parseByte2HexStr(byte[] buf) {
        if (buf == null || buf.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(buf.length * 2);
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: 将任意长度密钥字符串派生为AES-256密钥，避免继续限制8位DES密钥
     * @param keyText 密钥字符串
     * @return
     * 返回值: SecretKeySpec
     * 返回值说明: AES-256密钥
     * @throws Exception 密钥为空或SHA-256不可用时抛出异常
     */
    private static SecretKeySpec buildKey(String keyText) throws Exception {
        if (keyText == null || keyText.trim().isEmpty()) {
            throw new IllegalArgumentException("key must not be empty");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new SecretKeySpec(digest.digest(keyText.getBytes(StandardCharsets.UTF_8)), AES);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 08:55
     * 说明: AES-GCM加密示例入口，仅用于本地验证工具输出
     * @param args 命令行参数，当前未使用
     * @return
     * 返回值: void
     * 返回值说明: 控制台输出示例密文和解密结果
     * @throws Exception 加解密失败时抛出异常
     */
    public static void main(String[] args) throws Exception {
        DESUtils util = new DESUtils("demo-secret");
        String encrypted = util.encrypt("agent=btx");
        System.out.println("密文:" + encrypted);
        System.out.println("明文:" + util.decrypt(encrypted));
    }
}
