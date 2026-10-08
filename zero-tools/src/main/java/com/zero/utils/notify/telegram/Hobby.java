package com.zero.utils.notify.telegram;
/**
 * 作者: Jack
 * 时间: 2025年3月5日17:47:43
 * 说明: 爱好
 *
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