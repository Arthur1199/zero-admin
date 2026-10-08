package com.zero.utils.security.jwt;

import java.io.Serializable;

/**
 * 作者: Jack
 * 时间: 2021年10月21日上午10:50:52
 * 说明: 授权对象
 *
 */
public class AuthInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1223205393429854614L;
//	//授权被调用的用户
//	private String authUser;
//	//授权被调用的用户密码
//	private String authPass;
//	//调用方的ip地址
//	private String authIp;
//---------------------	用来授权的对象-----------------
	//系统名称
	private String systemName;
	//用户id
	private String userId;
	//用户名称
	private String userName;
	//登录ip
	private String loginIp;
	//登录时间 毫秒
	private Long loginTime;
	//超时时间 //分钟
	private Integer expiresMinute;
	
	//无参构造
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
