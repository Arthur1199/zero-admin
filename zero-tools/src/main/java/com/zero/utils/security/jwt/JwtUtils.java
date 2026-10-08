package com.zero.utils.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:25
 * 说明: JWT Token工具类，提供Token生成、签名校验、负载解析和过期判断能力
 */
public class JwtUtils {

    /**
     * JWT工具日志对象，只记录失败原因，不记录Token和密钥明文。
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    

    
    /**
     * JWT签名密钥，只用于服务端签名和校验，不得输出到日志或返回给前端。
     */
    private static final String TOKEN_SECRET="TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";

    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 根据系统、用户和登录IP生成不指定过期时间的Token
     * @param systemName 系统名称，写入Token负载用于区分来源系统
     * @param userId 用户ID，写入Token负载用于识别登录用户
     * @param loginName 登录名称，写入Token负载中的userName
     * @param loginIp 登录IP，写入Token负载用于记录登录来源
     * @return
     * 返回值: String
     * 返回值说明: JWT Token字符串，生成失败时返回null
     */
    public static String sign(String systemName,String userId,String loginName,String loginIp){
        try {

            Algorithm algorithm=Algorithm.HMAC256(TOKEN_SECRET);

            Map<String,Object> header=new HashMap<>(2);
            header.put("typ","JWT");
            header.put("alg","hs256");

            return  JWT.create()
                    .withHeader(header)
                    .withClaim("systemName",systemName)
                    .withClaim("userId",userId)
                    .withClaim("userName",loginName)
                    .withClaim("loginIp",loginIp)
                    .withClaim("loginTime",System.currentTimeMillis())

                    .sign(algorithm);
        }catch (Exception ex){
        	logger.error("签名失败："+ex.getMessage());
        	ex.printStackTrace();
            return null;
        }
    }

    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 根据系统和登录IP生成语言或公共场景Token
     * @param systemName 系统名称，写入Token负载用于区分来源系统
     * @param loginIp 登录IP，写入Token负载用于记录登录来源
     * @return
     * 返回值: String
     * 返回值说明: JWT Token字符串，生成失败时返回null
     */
    public static String signLanguage(String systemName,String loginIp){
        try {

            Algorithm algorithm=Algorithm.HMAC256(TOKEN_SECRET);

            Map<String,Object> header=new HashMap<>(2);
            header.put("typ","JWT");
            header.put("alg","hs256");

            return  JWT.create()
                    .withHeader(header)
                    .withClaim("systemName",systemName)
                    .withClaim("loginIp",loginIp)
                    .withClaim("loginTime",System.currentTimeMillis())

                    .sign(algorithm);
        }catch (Exception ex){
        	logger.error("签名失败："+ex.getMessage());
        	ex.printStackTrace();
            return null;
        }
    }

    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 根据登录授权信息生成包含过期时间的Token
     * @param authInfo 登录授权信息，需包含系统、用户、IP和过期分钟数
     * @return
     * 返回值: String
     * 返回值说明: JWT Token字符串，生成失败时返回null
     */
    public static String sign(AuthInfo authInfo){
        try {

            Date date =new Date(System.currentTimeMillis() + (long) authInfo.getExpiresMinute() * 60 * 1000);

            Algorithm algorithm=Algorithm.HMAC256(TOKEN_SECRET);

            Map<String,Object> header=new HashMap<>(2);
            header.put("typ","JWT");
            header.put("alg","hs256");

            return  JWT.create()
                    .withHeader(header)
                    .withClaim("systemName",authInfo.getSystemName())
                    .withClaim("userId",authInfo.getUserId())
                    .withClaim("userName",authInfo.getUserName())
                    .withClaim("loginIp",authInfo.getLoginIp())
                    .withClaim("loginTime",System.currentTimeMillis())
                    .withClaim("expiresMinute",authInfo.getExpiresMinute())
                    .withExpiresAt(date)
                    .sign(algorithm);
        }catch (Exception ex){
        	logger.error("签名失败："+ex.getMessage());
        	ex.printStackTrace();
            return null;
        }
    }
    
    

    

    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 校验Token签名是否合法
     * @param token JWT Token字符串，不应在日志或响应中明文暴露
     * @return
     * 返回值: boolean
     * 返回值说明: true-Token签名校验通过，false-Token无效或校验失败
     */
    public static boolean verify(String token){
        try{
            Algorithm algorithm=Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier=JWT.require(algorithm)
                    .build();
            DecodedJWT JWT=verifier.verify(token);
            return true;
        }catch (Exception ex){
        	logger.error("token校验失败："+ex.getMessage());
        	ex.printStackTrace();
            return false;
        }
    }
    
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 从Token负载中解析用户名
     * @param token JWT Token字符串，不应在日志或响应中明文暴露
     * @return
     * 返回值: String
     * 返回值说明: 解析到的负载字段，解析失败时返回null
     */
    public static String getUserName(String token){
        try {
            DecodedJWT jwt=JWT.decode(token);
            return  jwt.getClaim("userName").asString();
        }catch (JWTDecodeException ex){
        	logger.error("用户名解析校验失败："+ex.getMessage());
        	ex.printStackTrace();
            return null;
        }
    }

    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 从Token负载中解析用户ID
     * @param token JWT Token字符串，不应在日志或响应中明文暴露
     * @return
     * 返回值: String
     * 返回值说明: 解析到的负载字段，解析失败时返回null
     */
    public static String  getUserId(String token){
        try {
            DecodedJWT jwt=JWT.decode(token);
            return jwt.getClaim("userId").asString();
        }catch (JWTDecodeException ex){
        	logger.error("用户id解析失败："+ex.getMessage());
        	ex.printStackTrace();
            return null;
        }
    }
    
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 从Token负载中解析登录授权信息
     * @param token JWT Token字符串，不应在日志或响应中明文暴露
     * @return
     * 返回值: AuthInfo
     * 返回值说明: 解析后的登录授权信息，解析失败时返回null
     */
    public static AuthInfo getAuthInfo(String token){
    	 try {
    		 AuthInfo authInfo = new AuthInfo();
             DecodedJWT jwt=JWT.decode(token);
             String systemName = jwt.getClaim("systemName").asString();
             authInfo.setSystemName(systemName);
             String userId = jwt.getClaim("userId").asString();
             authInfo.setUserId(userId);
             String userName = jwt.getClaim("userName").asString();
             authInfo.setUserName(userName);
             String loginIp = jwt.getClaim("loginIp").asString();
             authInfo.setLoginIp(loginIp);
             Long loginTime = jwt.getClaim("loginTime").asLong();
             authInfo.setLoginTime(loginTime);
             Integer expiresMinute = jwt.getClaim("expiresMinute").asInt();
             authInfo.setExpiresMinute(expiresMinute);
             return authInfo;
         }catch (JWTDecodeException ex){
        	 logger.error("解析用户信息失败："+ex.getMessage());
         	 ex.printStackTrace();
             return null;
         }
    }
    
    
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 判断Token是否已过期
     * @param token JWT Token字符串，不应在日志或响应中明文暴露
     * @return
     * 返回值: Boolean
     * 返回值说明: true-Token已过期或解析失败，false-Token未过期
     */
    public static Boolean checkExpire(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);

            Date expiretime = jwt.getExpiresAt();
            String etStr = String.valueOf(expiretime.getTime());

            String nowTime = String.valueOf(System.currentTimeMillis());

            if(nowTime.compareTo(etStr)>0){
                return true;
            }
            return false;
        } catch (JWTDecodeException e){
        	logger.error("判断touke过期失败："+e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
    
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 23:25
     * 说明: 本地验证Token生成、校验、解析和过期判断流程
     * @param args 启动参数，本地验证方法未使用
     * @return
     * 返回值: void
     * 返回值说明: 方法无返回值，仅用于本地控制台验证
     */
    public static void main(String[] args) {
    	String token = JwtUtils.sign(new AuthInfo("USER", "1", "JACK", "127.0.0.1", 30));
    	System.out.println(token);
    	boolean verify = JwtUtils.verify(token);
    	System.out.println(verify);
    	Boolean checkExpire = JwtUtils.checkExpire(token);
    	System.out.println("checkExpire:"+checkExpire);
    	String userId = JwtUtils.getUserId(token);
    	String userName = JwtUtils.getUserName(token);
    	System.out.println(userName);
    	System.out.println(userId);
    	AuthInfo authInfo = JwtUtils.getAuthInfo(token);
    	System.out.println(authInfo.toString());
	}
}

