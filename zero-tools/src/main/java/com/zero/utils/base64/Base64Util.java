package com.zero.utils.base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 22:18
 * 说明: Base64统一编解码工具，集中处理字符串、字节数组和文件的Base64编码解码
 */
public final class Base64Util {

    /**
     * 文件流读写缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;

    private Base64Util() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 判断字符串是否符合Base64格式
     * @param value 待校验字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-符合Base64格式，false-不符合或为空
     */
    public static boolean isBase64(String value) {
        if (value == null) {
            return false;
        }
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 判断单个字节是否为Base64字符
     * @param octect 待校验字节
     * @return
     * 返回值: boolean
     * 返回值说明: true-属于Base64字符，false-不属于
     */
    public static boolean isBase64(byte octect) {
        return isBase64(String.valueOf((char) octect));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 判断字节数组是否为Base64内容
     * @param arrayOctect 待校验字节数组
     * @return
     * 返回值: boolean
     * 返回值说明: true-符合Base64格式，false-不符合或为空
     */
    public static boolean isArrayByteBase64(byte[] arrayOctect) {
        if (arrayOctect == null) {
            return false;
        }
        return isBase64(new String(arrayOctect));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字符串按平台默认字符集编码为Base64字符串
     * @param src 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: Base64字符串；入参为空时返回null
     */
    public static String encodeString(String src) {
        return encode(src);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字节数组编码为Base64字符串
     * @param src 原始字节数组
     * @return
     * 返回值: String
     * 返回值说明: Base64字符串；入参为空数组时返回null
     */
    public static String encodeBytes(byte[] src) {
        if (src == null || src.length == 0) {
            return null;
        }
        return new String(encode(src), StandardCharsets.ISO_8859_1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字符串按平台默认字符集编码为Base64字符串
     * @param src 原始字符串
     * @return
     * 返回值: String
     * 返回值说明: Base64字符串；入参为null时返回null
     */
    public static String encode(String src) {
        return encodeString(src, Charset.defaultCharset());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字符串按指定字符集编码为Base64字符串
     * @param value 原始字符串
     * @param charset 字符集
     * @return
     * 返回值: String
     * 返回值说明: Base64字符串；入参为null时返回null
     */
    public static String encodeString(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        return new String(encode(value.getBytes(charset)), StandardCharsets.ISO_8859_1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字节数组编码为Base64字节数组
     * @param binaryData 原始字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: Base64编码后的字节数组；入参为null时返回null
     */
    public static byte[] encode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        return Base64.getEncoder().encode(binaryData);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:28
     * 说明: 将字节数组编码为Base64字节数组，兼容历史crypto包方法名
     * @param binaryData 原始字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: Base64编码后的字节数组；入参为null时返回null
     */
    public static byte[] encodeBase64(byte[] binaryData) {
        return encode(binaryData);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字节数组编码为按MIME 76字符换行的Base64字节数组
     * @param binaryData 原始字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: MIME格式Base64编码字节数组；入参为null时返回null
     */
    public static byte[] encodeChunked(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        return Base64.getMimeEncoder().encode(binaryData);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:28
     * 说明: 将字节数组编码为按MIME 76字符换行的Base64字节数组，兼容历史crypto包方法名
     * @param binaryData 原始字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: MIME格式Base64编码字节数组；入参为null时返回null
     */
    public static byte[] encodeBase64Chunked(byte[] binaryData) {
        return encodeChunked(binaryData);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:28
     * 说明: 按指定换行策略编码Base64字节数组
     * @param binaryData 原始字节数组
     * @param isChunked true-按MIME换行编码，false-普通Base64编码
     * @return
     * 返回值: byte[]
     * 返回值说明: Base64编码后的字节数组；入参为null时返回null
     */
    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        return isChunked ? encodeChunked(binaryData) : encode(binaryData);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串按平台默认字符集解码
     * @param src Base64字符串
     * @return
     * 返回值: String
     * 返回值说明: 解码后的字符串；入参为null时返回null
     */
    public static String decode(String src) {
        return decodeStringToText(src);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串按指定字符集解码
     * @param src Base64字符串
     * @param charSet 字符集名称
     * @return
     * 返回值: String
     * 返回值说明: 解码后的字符串；入参为null时返回null
     * @throws UnsupportedEncodingException 字符集名称无效时抛出异常
     */
    public static String decode(String src, String charSet) throws UnsupportedEncodingException {
        return decodeStringToText(src, Charset.forName(charSet));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字节数组解码为原始字节数组
     * @param base64Data Base64编码字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: 解码后的原始字节数组；入参为空数组时返回null，入参为null时返回null
     */
    public static byte[] decode(byte[] base64Data) {
        if (base64Data == null || base64Data.length == 0) {
            return null;
        }
        return Base64.getMimeDecoder().decode(base64Data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:28
     * 说明: 将Base64字节数组解码为原始字节数组，兼容历史crypto包方法名
     * @param base64Data Base64编码字节数组
     * @return
     * 返回值: byte[]
     * 返回值说明: 解码后的原始字节数组；入参为空或null时返回null
     */
    public static byte[] decodeBase64(byte[] base64Data) {
        return decode(base64Data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串解码为字节数组
     * @param base64 Base64字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: 解码后的原始字节数组
     * @throws Exception Base64内容非法时抛出异常
     */
    public static byte[] decodeString(String base64) throws Exception {
        return decode(base64.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:28
     * 说明: 将Base64字符串解码为字节数组，兼容历史decode2方法名
     * @param encoded Base64字符串
     * @return
     * 返回值: byte[]
     * 返回值说明: 解码后的原始字节数组；入参为空时返回null
     */
    public static byte[] decode2(String encoded) {
        if (encoded == null) {
            return null;
        }
        return decode(encoded.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:28
     * 说明: 兼容历史crypto.Base64.decode(String)的二次解码规则，先解码再去掉前40位后再次解码
     * @param cryptoStr 历史加密串
     * @return
     * 返回值: String
     * 返回值说明: 二次解码后的明文；长度不足或Base64格式错误时返回空字符串
     * @throws UnsupportedEncodingException UTF-8字符集不可用时抛出异常
     */
    public static String decodeCryptoString(String cryptoStr) throws UnsupportedEncodingException {
        if (cryptoStr == null || cryptoStr.length() < 40) {
            return "";
        }
        try {
            String tempStr = new String(decode(cryptoStr.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            String result = tempStr.substring(40);
            return new String(decode(result.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            return "";
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串按平台默认字符集解码
     * @param value Base64字符串
     * @return
     * 返回值: String
     * 返回值说明: 解码后的字符串；入参为null时返回null
     */
    public static String decodeStringValue(String value) {
        return decodeStringToText(value, Charset.defaultCharset());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串按平台默认字符集解码
     * @param value Base64字符串
     * @return
     * 返回值: String
     * 返回值说明: 解码后的字符串；入参为null时返回null
     */
    private static String decodeStringToText(String value) {
        return decodeStringToText(value, Charset.defaultCharset());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串按指定字符集解码
     * @param value Base64字符串
     * @param charset 字符集
     * @return
     * 返回值: String
     * 返回值说明: 解码后的字符串；入参为null时返回null
     */
    public static String decodeStringToText(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        return new String(decode(value.getBytes(StandardCharsets.ISO_8859_1)), charset);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字节数组编码为Base64字符串
     * @param bytes 原始字节数组
     * @return
     * 返回值: String
     * 返回值说明: Base64字符串
     * @throws Exception 编码失败时抛出异常
     */
    public static String encodeByte(byte[] bytes) throws Exception {
        return new String(encode(bytes), StandardCharsets.ISO_8859_1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将文件内容编码为Base64字符串
     * @param filePath 文件路径
     * @return
     * 返回值: String
     * 返回值说明: 文件内容对应的Base64字符串
     * @throws Exception 文件读取失败时抛出异常
     */
    public static String encodeFile(String filePath) throws Exception {
        return new String(encode(fileToByte(filePath)), StandardCharsets.ISO_8859_1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将Base64字符串解码后写入文件
     * @param filePath 目标文件路径
     * @param base64 Base64字符串
     * 返回值: void
     * 返回值说明: 解码内容写入目标文件
     * @throws Exception 文件写入失败时抛出异常
     */
    public static void decodeToFile(String filePath, String base64) throws Exception {
        byteArrayToFile(decode(base64.getBytes(StandardCharsets.ISO_8859_1)), filePath);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 读取文件为字节数组
     * @param filePath 文件路径
     * @return
     * 返回值: byte[]
     * 返回值说明: 文件字节数组，文件不存在时返回空数组
     * @throws Exception 文件读取失败时抛出异常
     */
    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file);
                 ByteArrayOutputStream out = new ByteArrayOutputStream(2048)) {
                byte[] cache = new byte[CACHE_SIZE];
                int nRead;
                while ((nRead = in.read(cache)) != -1) {
                    out.write(cache, 0, nRead);
                }
                data = out.toByteArray();
            }
        }
        return data;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 将字节数组写入目标文件
     * @param bytes 文件内容字节数组
     * @param filePath 目标文件路径
     * 返回值: void
     * 返回值说明: 创建或覆盖目标文件
     * @throws Exception 文件写入失败时抛出异常
     */
    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
        try (InputStream in = new ByteArrayInputStream(bytes);
             OutputStream out = new FileOutputStream(prepareFile(filePath))) {
            byte[] cache = new byte[CACHE_SIZE];
            int nRead;
            while ((nRead = in.read(cache)) != -1) {
                out.write(cache, 0, nRead);
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:18
     * 说明: 准备目标文件，父目录不存在时自动创建，并校验目标路径不是目录
     * @param filePath 目标文件路径，不能为空
     * @return
     * 返回值: File
     * 返回值说明: 可写入的目标文件
     * @throws Exception 路径为空、父目录创建失败或目标路径为目录时抛出异常
     */
    private static File prepareFile(String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be empty");
        }

        Path targetPath = Path.of(filePath);
        Path parentPath = targetPath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }

        if (Files.exists(targetPath) && Files.isDirectory(targetPath)) {
            throw new IOException("目标路径是目录，不能作为文件写入: " + filePath);
        }

        if (Files.notExists(targetPath)) {
            Files.createFile(targetPath);
        }
        return targetPath.toFile();
    }
}
