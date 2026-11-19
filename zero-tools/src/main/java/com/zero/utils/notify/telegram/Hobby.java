package com.zero.utils.notify.telegram;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 承载 Telegram 消息组装演示中的单项兴趣和描述。
 */
public class Hobby{
	
	private String hobbyName;
	
	private String hobbyDesc;
	
	public Hobby(String hobbyName, String hobbyDesc) {
		this.hobbyName = hobbyName;
		this.hobbyDesc = hobbyDesc;
	}

	public String getHobbyName() {
		return hobbyName;
	}

	public void setHobbyName(String hobbyName) {
		this.hobbyName = hobbyName;
	}

	public String getHobbyDesc() {
		return hobbyDesc;
	}

	public void setHobbyDesc(String hobbyDesc) {
		this.hobbyDesc = hobbyDesc;
	}
	
}