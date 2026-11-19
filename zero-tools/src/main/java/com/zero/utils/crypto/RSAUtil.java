package com.zero.utils.crypto;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 封装 RSA 密钥生成、加密解密和签名验签，供现有调用方沿用字符串及 Base64 数据格式。
 */
public class RSAUtil {

    //签名算法名称
    private static final String RSA_KEY_ALGORITHM = "RSA";

    //标准签名算法名称
    private static final String RSA_SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final String RSA2_SIGNATURE_ALGORITHM = "SHA256withRSA";

    //RSA密钥长度,默认密钥长度是1024,密钥长度必须是64的倍数，在512到65536位之间,不管是RSA还是RSA2长度推荐使用2048
    private static final int KEY_SIZE = 2048;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成密钥对
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 返回包含公私钥的map
     */
    public static Map<String, String> generateKey() {
        KeyPairGenerator keygen;
        try {
            keygen = KeyPairGenerator.getInstance(RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA初始化密钥出现错误,算法异常");
        }
        SecureRandom secRand = new SecureRandom();
        //初始化随机产生器
        secRand.setSeed("Alian".getBytes());
        //初始化密钥生成器
        keygen.initialize(KEY_SIZE, secRand);
        KeyPair keyPair = keygen.genKeyPair();
        //获取公钥并转成base64编码
        byte[] pub_key = keyPair.getPublic().getEncoded();
        String publicKeyStr = Base64.getEncoder().encodeToString(pub_key);
        //获取私钥并转成base64编码
        byte[] pri_key = keyPair.getPrivate().getEncoded();
        String privateKeyStr = Base64.getEncoder().encodeToString(pri_key);
        //创建一个Map返回结果
        Map<String, String> keyPairMap = new HashMap<>();
        keyPairMap.put("publicKeyStr", publicKeyStr);
        keyPairMap.put("privateKeyStr", privateKeyStr);
        return keyPairMap;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 公钥加密(用于数据加密)
     * @param data 加密前的字符串
     * @param publicKeyStr base64编码后的公钥
     * @return
     * 返回值: String
     * 返回值说明: base64编码后的字符串
     * @throws Exception
     */
    public static String encryptByPublicKey(String data, String publicKeyStr) throws Exception {
        //Java原生base64解码
        byte[] pubKey = Base64.getDecoder().decode(publicKeyStr);
        //创建X509编码密钥规范
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        //返回转换指定算法的KeyFactory对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        //根据X509编码密钥规范产生公钥对象
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        //用公钥初始化此Cipher对象（加密模式）
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //对数据加密
        byte[] encrypt = cipher.doFinal(data.getBytes());
        //返回base64编码后的字符串
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 私钥解密(用于数据解密)
     * @param data 解密前的字符串
     * @param privateKeyStr 私钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的字符串
     * @throws Exception
     */
    public static String decryptByPrivateKey(String data, String privateKeyStr) throws Exception {
        //Java原生base64解码
        byte[] priKey = Base64.getDecoder().decode(privateKeyStr);
        //创建PKCS8编码密钥规范
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        //返回转换指定算法的KeyFactory对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        //根据PKCS8编码密钥规范产生私钥对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        //用私钥初始化此Cipher对象（解密模式）
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //对数据解密
        byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(data));
        //返回字符串
        return new String(decrypt);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 私钥加密(用于数据签名)
     * @param data 加密前的字符串
     * @param privateKeyStr base64编码后的私钥
     * @return
     * 返回值: String
     * 返回值说明: base64编码后后的字符串
     * @throws Exception
     */
    public static String encryptByPrivateKey(String data, String privateKeyStr) throws Exception {
        //Java原生base64解码
        byte[] priKey = Base64.getDecoder().decode(privateKeyStr);
        //创建PKCS8编码密钥规范
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        //返回转换指定算法的KeyFactory对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        //根据PKCS8编码密钥规范产生私钥对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        //用私钥初始化此Cipher对象（加密模式）
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        //对数据加密
        byte[] encrypt = cipher.doFinal(data.getBytes());
        //返回base64编码后的字符串
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 公钥解密(用于数据验签)
     * @param data 解密前的字符串
     * @param publicKeyStr base64编码后的公钥
     * @return
     * 返回值: String
     * 返回值说明: 解密后的字符串
     * @throws Exception
     */
    public static String decryptByPublicKey(String data, String publicKeyStr) throws Exception {
        //Java原生base64解码
        byte[] pubKey = Base64.getDecoder().decode(publicKeyStr);
        //创建X509编码密钥规范
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        //返回转换指定算法的KeyFactory对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        //根据X509编码密钥规范产生公钥对象
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        //用公钥初始化此Cipher对象（解密模式）
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        //对数据解密
        byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(data));
        //返回字符串
        return new String(decrypt);
    }

//    /**
//     * RSA签名
//     *
//     * @param data     待签名数据
//     * @param priKey   私钥
//     * @param signType RSA或RSA2
//     * @return 签名
//     * @throws Exception
//     */
//    public static String sign(byte[] data, byte[] priKey, String signType) throws Exception {
//        //创建PKCS8编码密钥规范
//        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
//        //返回转换指定算法的KeyFactory对象
//        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
//        //根据PKCS8编码密钥规范产生私钥对象
//        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
//        //标准签名算法名称(RSA还是RSA2)
//        String algorithm = RSA_KEY_ALGORITHM.equals(signType) ? RSA_SIGNATURE_ALGORITHM : RSA2_SIGNATURE_ALGORITHM;
//        //用指定算法产生签名对象Signature
//        Signature signature = Signature.getInstance(algorithm);
//        //用私钥初始化签名对象Signature
//        signature.initSign(privateKey);
//        //将待签名的数据传送给签名对象(须在初始化之后)
//        signature.update(data);
//        //返回签名结果字节数组
//        byte[] sign = signature.sign();
//        //返回Base64编码后的字符串
//        return Base64.getEncoder().encodeToString(sign);
//    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对字符串按当前 JVM 默认字符集取字节，再用指定分支的 RSA 签名算法生成签名。
     * @param sinStr 待签名文本
     * @param priKey Base64 编码的 PKCS#8 私钥，禁止输出
     * @param signType 为 RSA 时走兼容分支，其余取值使用 RSA2 分支
     * @return
     * 返回值: String
     * 返回值说明: Base64 编码的签名
     * @throws Exception 私钥无效或签名初始化、计算失败时抛出
     */
    public static String sign(String sinStr, String priKey, String signType) throws Exception {
    	byte [] data = sinStr.getBytes();
    	byte[] decode = Base64.getDecoder().decode(priKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(decode);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 沿用接入方的算法约定，只有显式传入 RSA 时选择兼容分支。
        String algorithm = RSA_KEY_ALGORITHM.equals(signType) ? RSA_SIGNATURE_ALGORITHM : RSA2_SIGNATURE_ALGORITHM;
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        byte[] sign = signature.sign();
        return Base64.getEncoder().encodeToString(sign);
    }
    
//    /**
//     * RSA校验数字签名
//     *
//     * @param data     待校验数据
//     * @param sign     数字签名
//     * @param pubKey   公钥
//     * @param signType RSA或RSA2
//     * @return boolean 校验成功返回true，失败返回false
//     */
//    public static boolean verify(byte[] data, byte[] sign, byte[] pubKey, String signType) throws Exception {
//        //返回转换指定算法的KeyFactory对象
//        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
//        //创建X509编码密钥规范
//        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
//        //根据X509编码密钥规范产生公钥对象
//        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
//        //标准签名算法名称(RSA还是RSA2)
//        String algorithm = RSA_KEY_ALGORITHM.equals(signType) ? RSA_SIGNATURE_ALGORITHM : RSA2_SIGNATURE_ALGORITHM;
//        //用指定算法产生签名对象Signature
//        Signature signature = Signature.getInstance(algorithm);
//        //用公钥初始化签名对象,用于验证签名
//        signature.initVerify(publicKey);
//        //更新签名内容
//        signature.update(data);
//        //得到验证结果
//        return signature.verify(sign);
//    }
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用 RSA 公钥及指定的兼容签名算法校验文本签名。
     * @param verifyStr 待校验的原始文本
     * @param sign Base64 编码的签名
     * @param pubKey Base64 编码的公钥
     * @param signType 签名算法选择参数
     * @return
     * 返回值: boolean
     * 返回值说明: 签名与原文、公钥匹配时返回 true
     */
    public static boolean verify(String verifyStr, String sign, String pubKey, String signType) throws Exception {
    	byte[] decodePublicKey = Base64.getDecoder().decode(pubKey);
    	byte [] data = verifyStr.getBytes();
    	byte[] decodeSign = Base64.getDecoder().decode(sign);
        //返回转换指定算法的KeyFactory对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        //创建X509编码密钥规范
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(decodePublicKey);
        //根据X509编码密钥规范产生公钥对象
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        //标准签名算法名称(RSA还是RSA2)
        String algorithm = RSA_KEY_ALGORITHM.equals(signType) ? RSA_SIGNATURE_ALGORITHM : RSA2_SIGNATURE_ALGORITHM;
        //用指定算法产生签名对象Signature
        Signature signature = Signature.getInstance(algorithm);
        //用公钥初始化签名对象,用于验证签名
        signature.initVerify(publicKey);
        //更新签名内容
        signature.update(data);
        //得到验证结果
        return signature.verify(decodeSign);
    }
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用临时密钥对演示公钥加密和私钥解密。
     * @return
     * 返回值: void
     * 返回值说明: 向控制台输出演示密钥及往返处理结果，仅供本地调试
     * @throws Exception 生成密钥或加解密失败时抛出
     */
    public void encryptAndDecrypt() throws Exception {
        Map<String, String> keyMap = RSAUtil.generateKey();
        String publicKeyStr = keyMap.get("publicKeyStr");
        String privateKeyStr = keyMap.get("privateKeyStr");
        System.out.println("-----------------生成的公钥和私钥------------------------------");
        System.out.println("获取到的公钥：" + publicKeyStr);
        System.out.println("获取到的私钥：" + privateKeyStr);
        // 待加密数据
        String data = "tranSeq=1920542585&amount=100&payType=wechat";
        // 公钥加密
        System.out.println("-----------------加密和解密------------------------------");
        System.out.println("待加密的数据：" + data);
        String encrypt = RSAUtil.encryptByPublicKey(data, publicKeyStr);
        System.out.println("加密后数据：" + encrypt);
        // 私钥解密
        String decrypt = RSAUtil.decryptByPrivateKey(encrypt, privateKeyStr);
        System.out.println("解密后数据：" + decrypt);
    }
    
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用临时密钥对演示私钥签名和公钥验签。
     * @return
     * 返回值: void
     * 返回值说明: 向控制台输出演示密钥、签名和验签结果，仅供本地调试
     * @throws Exception 生成密钥、签名或验签失败时抛出
     */
    public void signAndVerify() throws Exception {
        Map<String, String> keyMap = RSAUtil.generateKey();
        String publicKeyStr = keyMap.get("publicKeyStr");
        String privateKeyStr = keyMap.get("privateKeyStr");
        System.out.println("-----------------生成的公钥和私钥------------------------------");
        System.out.println("获取到的公钥：" + publicKeyStr);
        System.out.println("获取到的私钥：" + privateKeyStr);
        // 数字签名
        String data = "tranSeq=1920542585&amount=100&payType=wechat";
        System.out.println("待签名的数据：" + data);
        String sign = RSAUtil.sign(data, privateKeyStr, "RSA");
        System.out.println("数字签名结果：" + sign);
        boolean verify = RSAUtil.verify(data, sign, publicKeyStr, "RSA");
        System.out.println("数字签名验证结果：" + verify);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 依次运行 RSA 加解密和签名验签演示。
     * @param args 命令行参数，演示流程不读取
     * @return
     * 返回值: void
     * 返回值说明: 将两组演示结果输出到控制台
     * @throws Exception 任一演示步骤失败时向上传递
     */
    public static void main(String[] args) throws Exception {
        RSAUtil rsaUtil = new RSAUtil();
        rsaUtil.encryptAndDecrypt();
        rsaUtil.signAndVerify();
    }
    
}
