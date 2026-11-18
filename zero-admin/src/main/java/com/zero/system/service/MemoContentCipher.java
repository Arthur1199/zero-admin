package com.zero.system.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.zero.common.exception.ServiceException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录正文AES-256-GCM加解密；独立密钥由环境配置提供，不复用JWT密钥，不记录明文或密钥
 */
@Component
public class MemoContentCipher
{
    /** 配置项：Base64编码的32字节密钥，无默认密钥；未配置时仅禁用正文读写。 */
    public static final String KEY_PROPERTY = "zero.memo.encryption-key";
    /** 密文格式版本；格式为v1:Base64(随机数+密文+认证标签)。 */
    private static final String PREFIX = "v1:";
    /** GCM随机数长度，单位字节，每次加密重新生成。 */
    private static final int NONCE_BYTES = 12;
    /** GCM认证标签位数。 */
    private static final int TAG_BITS = 128;
    /** AES-256密钥长度，单位字节。 */
    private static final int KEY_BYTES = 32;
    /** 线程安全的安全随机数源，不复用Cipher实例。 */
    private final SecureRandom random = new SecureRandom();
    /** 外部密钥配置，仅在正文读写时检查，禁止输出到日志。 */
    private final String configuredKey;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 接收独立加密密钥，允许未启用备忘录的环境暂不配置
     * @param configuredKey Base64密钥，可空但正文读写时拒绝
     * @return
     * 返回值: 构造方法
     * 返回值说明: 保存配置，不生成或回退为默认密钥
     */
    public MemoContentCipher(@Value("${" + KEY_PROPERTY + ":${ZERO_MEMO_ENCRYPTION_KEY:}}") String configuredKey)
    {
        this.configuredKey = configuredKey;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 使用随机数加密正文，并绑定创建用户防止跨用户替换密文
     * @param content 原始正文，可空，保留换行与空格
     * @param ownerUserId 服务端创建用户ID
     * @return
     * 返回值: String
     * 返回值说明: 带版本、随机数和认证标签的密文，空正文返回null
     */
    public String encrypt(String content, Long ownerUserId)
    {
        SecretKeySpec key = key();
        if (content == null || content.isEmpty()) return null;
        try
        {
            byte[] nonce = new byte[NONCE_BYTES];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD(String.valueOf(ownerUserId).getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            byte[] packed = Arrays.copyOf(nonce, nonce.length + encrypted.length);
            System.arraycopy(encrypted, 0, packed, nonce.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(packed);
        }
        catch (GeneralSecurityException e)
        {
            throw new ServiceException("备忘录内容加密失败，请联系管理员");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 解密并验证密文完整性，不允许将未知格式当成明文读取
     * @param value 数据库密文，可空
     * @param ownerUserId 数据库中的创建用户ID
     * @return
     * 返回值: String
     * 返回值说明: 解密正文，空密文返回空字符串，损坏或密钥错误时拒绝
     */
    public String decrypt(String value, Long ownerUserId)
    {
        SecretKeySpec key = key();
        if (value == null || value.isEmpty()) return "";
        try
        {
            if (!value.startsWith(PREFIX)) throw new GeneralSecurityException();
            byte[] packed = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (packed.length < NONCE_BYTES + TAG_BITS / 8) throw new GeneralSecurityException();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, packed, 0, NONCE_BYTES));
            cipher.updateAAD(String.valueOf(ownerUserId).getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(packed, NONCE_BYTES, packed.length - NONCE_BYTES), StandardCharsets.UTF_8);
        }
        catch (GeneralSecurityException | IllegalArgumentException e)
        {
            throw new ServiceException("备忘录内容无法解密，请检查密钥或数据完整性");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 检查外部密钥，缺失或格式错误时关闭正文读写，不携带原始异常避免密钥泄露
     * @return
     * 返回值: SecretKeySpec
     * 返回值说明: 有效AES-256密钥；无效时返回固定业务错误
     */
    private SecretKeySpec key()
    {
        try
        {
            byte[] bytes = Base64.getDecoder().decode(configuredKey == null ? "" : configuredKey.trim());
            if (bytes.length != KEY_BYTES) throw new IllegalArgumentException();
            return new SecretKeySpec(bytes, "AES");
        }
        catch (IllegalArgumentException e)
        {
            throw new ServiceException("备忘录加密密钥未配置或格式无效，请联系管理员");
        }
    }
}

