package com.zero.utils.security.jwt;

import java.io.Serializable;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 传递共享 JWT 工具使用的系统标识、用户登录信息和令牌有效期。
 */
public class AuthInfo implements Serializable{
	/** 兼容已有 Java 序列化数据的版本号。 */
	private static final long serialVersionUID = -1223205393429854614L;
	/** JWT 中标记调用方所属系统的名称。 */
	private String systemName;
	/** JWT 对应的用户标识。 */
	private String userId;
	/** JWT 携带的登录用户名称。 */
	private String userName;
	/** 创建登录信息时记录的客户端 IP。 */
	private String loginIp;
	/** 从令牌读取的登录时间，单位为 Unix 时间戳毫秒。 */
	private Long loginTime;
	/** 令牌有效时长，单位为分钟。 */
	private Integer expiresMinute;
	
	public AuthInfo() {}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public Long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Long loginTime) {
		this.loginTime = loginTime;
	}

	public Integer getExpiresMinute() {
		return expiresMinute;
	}

	public void setExpiresMinute(Integer expiresMinute) {
		this.expiresMinute = expiresMinute;
	}

	public AuthInfo(String systemName, String userId, String userName, String loginIp, Integer expiresMinute) {
		super();
		this.systemName = systemName;
		this.userId = userId;
		this.userName = userName;
		this.loginIp = loginIp;
		this.expiresMinute = expiresMinute;
	}

	@Override
	public String toString() {
		return "AuthInfo [systemName=" + systemName + ", userId=" + userId + ", userName=" + userName + ", loginIp="
				+ loginIp + ", loginTime=" + loginTime + ", expiresMinute=" + expiresMinute + "]";
	}
	
	
}
