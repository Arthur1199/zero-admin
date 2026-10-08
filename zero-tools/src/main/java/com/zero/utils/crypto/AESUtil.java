package com.zero.utils.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 09:02
 * 说明: AES加密解密工具类，统一使用AES-GCM认证加密替代历史AES-ECB和AES-CBC实现
 */
public class AESUtil {

    /**
     * 默认演示密钥，真实业务应从配置或密钥管理系统读取。
     */
    private static final String KEY = "e1d626c5c932a41e";

    /**
     * AES-GCM算法名称。
     */
    private static final String AES_GCM = "AES/GCM/NoPadding";

    /**
     * AES算法名称。
     */
    private static final String AES_ALGORITHM = "AES";

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
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM加密字节数组，返回内容格式为IV+密文+认证标签
     * @param data 待加密字节数组
     * @param key 密钥字节数组，长度不是16/24/32时会通过SHA-256派生为AES-256密钥
     * @return
     * 返回值: byte[]
     * 返回值说明: AES-GCM密文字节数组，前12字节为随机IV
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(key), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(data == null ? new byte[0] : data);
            return join(iv, encrypted);
        } catch (Exception e) {
            throw new RuntimeException("aes gcm encrypt fail", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM解密字节数组，入参格式必须为IV+密文+认证标签
     * @param data 待解密字节数组，前12字节为IV
     * @param key 密钥字节数组，长度不是16/24/32时会通过SHA-256派生为AES-256密钥
     * @return
     * 返回值: byte[]
     * 返回值说明: 解密后的明文字节数组
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        try {
            if (data == null || data.length <= GCM_IV_LENGTH) {
                throw new IllegalArgumentException("data must contain iv and cipher text");
            }
            byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(data, GCM_IV_LENGTH, data.length);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(key), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("aes gcm decrypt fail", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用明文密钥加密字符串并返回Base64密文
     * @param data 待加密字符串
     * @param key 密钥字符串，内部自动派生为AES密钥
     * @return
     * 返回值: String
     * 返回值说明: Base64格式AES-GCM密文，内容为IV+密文+认证标签
     */
    public static String encryptToBase64(String data, String key) {
        return base64Encode(encrypt(toBytes(data), toBytes(key)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用明文密钥解密Base64格式AES-GCM密文
     * @param data Base64格式AES-GCM密文
     * @param key 密钥字符串，内部自动派生为AES密钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     */
    public static String decryptFromBase64(String data, String key) {
        return new String(decrypt(base64Decode(data), toBytes(key)), StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用Base64格式密钥加密字符串并返回Base64密文
     * @param data 待加密字符串
     * @param key Base64格式密钥
     * @return
     * 返回值: String
     * 返回值说明: Base64格式AES-GCM密文
     */
    public static String encryptWithKeyBase64(String data, String key) {
        return base64Encode(encrypt(toBytes(data), base64Decode(key)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用Base64格式密钥解密Base64格式AES-GCM密文
     * @param data Base64格式AES-GCM密文
     * @param key Base64格式密钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     */
    public static String decryptWithKeyBase64(String data, String key) {
        return new String(decrypt(base64Decode(data), base64Decode(key)), StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 生成随机AES密钥字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: 256位AES密钥字节数组
     */
    public static byte[] genarateRandomKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(AES_ALGORITHM);
            keygen.init(256, SECURE_RANDOM);
            Key key = keygen.generateKey();
            return key.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("generate aes key fail", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 生成Base64格式随机AES密钥
     * @return
     * 返回值: String
     * 返回值说明: Base64格式256位AES密钥
     */
    public static String generateRandomKeyWithBase64() {
        return base64Encode(genarateRandomKey());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用默认密钥解密AES-GCM密文
     * @param encrypt Base64格式AES-GCM密文
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文；失败时返回空字符串
     */
    public static String aesDecrypt(String encrypt) {
        try {
            return aesDecrypt(encrypt, KEY);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用默认密钥加密字符串
     * @param content 待加密内容
     * @return
     * 返回值: String
     * 返回值说明: Base64格式AES-GCM密文；失败时返回空字符串
     */
    public static String aesEncrypt(String content) {
        try {
            return aesEncrypt(content, KEY);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 将byte[]转为指定进制字符串
     * @param bytes 字节数组
     * @param radix 目标进制，超出Character允许范围时按10进制处理
     * @return
     * 返回值: String
     * 返回值说明: 转换后的字符串
     */
    public static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: Base64编码字节数组
     * @param bytes 待编码字节数组
     * @return
     * 返回值: String
     * 返回值说明: Base64字符串；入参为空时返回null
     */
    public static String base64Encode(byte[] bytes) {
        return bytes == null ? null : Base64.getEncoder().encodeToString(bytes);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: Base64解码字符串
     * @param base64Code Base64字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: 解码后的字节数组；入参为空时返回null
     */
    public static byte[] base64Decode(String base64Code) {
        return base64Code == null || base64Code.trim().isEmpty() ? null : Base64.getDecoder().decode(base64Code);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM加密字符串并返回字节数组
     * @param content 待加密内容
     * @param encryptKey 加密密钥
     * @return
     * 返回值: byte[]
     * 返回值说明: AES-GCM密文字节数组，前12字节为随机IV
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) {
        return encrypt(toBytes(content), toBytes(encryptKey));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM加密字符串并返回Base64密文
     * @param content 待加密内容
     * @param encryptKey 加密密钥
     * @return
     * 返回值: String
     * 返回值说明: Base64格式AES-GCM密文
     */
    public static String aesEncrypt(String content, String encryptKey) {
        return base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM解密字节数组
     * @param encryptBytes 待解密字节数组，前12字节为IV
     * @param decryptKey 解密密钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) {
        return new String(decrypt(encryptBytes, toBytes(decryptKey)), StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 解密Base64格式AES-GCM密文
     * @param encryptStr Base64格式AES-GCM密文
     * @param decryptKey 解密密钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) {
        return encryptStr == null || encryptStr.trim().isEmpty() ? null : aesDecryptByBytes(base64Decode(encryptStr), decryptKey);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用默认密钥解密Base64格式AES-GCM密文
     * @param encryptStr Base64格式AES-GCM密文
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     */
    public static String aesDecryptByParam(String encryptStr) {
        return encryptStr == null || encryptStr.trim().isEmpty() ? null : aesDecryptByBytes(base64Decode(encryptStr), KEY);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 保留历史PKCS7补齐入口，AES-GCM不需要补齐，当前仅返回原数据副本
     * @param data 原始字节数组
     * @param blockSize 历史分组大小参数，AES-GCM不再使用
     * @return
     * 返回值: byte[]
     * 返回值说明: 原始字节数组副本
     */
    public static byte[] pkcs7PaddingForCbc256(byte[] data, int blockSize) {
        return data == null ? new byte[0] : Arrays.copyOf(data, data.length);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 生成256位AES密钥
     * @return
     * 返回值: SecretKey
     * 返回值说明: 256位AES密钥
     * @throws Exception 当前运行环境不支持AES密钥生成时抛出异常
     */
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(256, SECURE_RANDOM);
        return keyGen.generateKey();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 生成AES-GCM使用的随机IV
     * @return
     * 返回值: byte[]
     * 返回值说明: 12字节随机IV
     */
    public static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM加密明文，保留原CBC方法名用于兼容demo调用
     * @param plainText 待加密明文
     * @param key 加密密钥
     * @param iv 历史IV参数，AES-GCM会自行生成随机IV，此参数不再参与加密
     * @return
     * 返回值: String
     * 返回值说明: Base64格式AES-GCM密文
     */
    public static String aesCbc256Encrypt(String plainText, String key, String iv) {
        return aesEncrypt(plainText, key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 解密aesCbc256Encrypt生成的Base64密文，保留原CBC方法名用于兼容demo调用
     * @param cipherTextBase64 Base64格式AES-GCM密文
     * @param secretKey AES密钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的明文字符串
     */
    public static String aesCbc256Decrypt(String cipherTextBase64, SecretKey secretKey) {
        return new String(decrypt(base64Decode(cipherTextBase64), secretKey.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 计算字符串MD5摘要并返回Base64编码结果
     * @param input 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: MD5摘要的Base64编码字符串
     */
    public static String md5Base64(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return base64Encode(md.digest(toBytes(input)));
        } catch (Exception e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 使用AES-GCM加密内容，保留历史encryptCBC方法名用于demo兼容
     * @param content 待加密内容
     * @param aesKey AES密钥
     * @param md5Key 历史偏移参数，当前作为附加密钥材料参与密钥派生
     * @return
     * 返回值: String
     * 返回值说明: Base64格式AES-GCM密文；内容或密钥为空时返回null
     */
    public static String encryptCBC(String content, String aesKey, String md5Key) {
        if (content == null || content.trim().isEmpty() || aesKey == null || aesKey.trim().isEmpty()) {
            return null;
        }
        return aesEncrypt(content, aesKey + ":" + (md5Key == null ? "" : md5Key));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 将字符串按UTF-8转换为字节数组
     * @param value 原始字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: UTF-8字节数组；入参为null时返回空数组
     */
    private static byte[] toBytes(String value) {
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 构建AES密钥，长度不是16/24/32字节时通过SHA-256派生为AES-256密钥
     * @param key 原始密钥字节数组
     * @return
     * 返回值: SecretKeySpec
     * 返回值说明: 可用于AES-GCM的密钥
     * @throws Exception 密钥为空或摘要算法不可用时抛出异常
     */
    private static SecretKeySpec buildKey(byte[] key) throws Exception {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("key must not be empty");
        }
        if (key.length == 16 || key.length == 24 || key.length == 32) {
            return new SecretKeySpec(key, AES_ALGORITHM);
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new SecretKeySpec(digest.digest(key), AES_ALGORITHM);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: 合并IV和密文字节数组
     * @param iv 随机IV
     * @param encrypted 密文和认证标签
     * @return
     * 返回值: byte[]
     * 返回值说明: IV+密文+认证标签
     */
    private static byte[] join(byte[] iv, byte[] encrypted) {
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/17 09:02
     * 说明: AES-GCM加解密示例入口
     * @param args 命令行参数，当前未使用
     * @return
     * 返回值: void
     * 返回值说明: 控制台输出示例密文和解密结果
     */
    public static void main(String[] args) {
        String content = "{\"password\":\"123456\",\"loginName\":\"jerry001\"}";
        String encrypted = aesEncrypt(content, KEY);
        System.out.println("加密后：" + encrypted);
        System.out.println("解密后：" + aesDecrypt(encrypted, KEY));
    }
}
