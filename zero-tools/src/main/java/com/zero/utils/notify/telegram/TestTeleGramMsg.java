package com.zero.utils.notify.telegram;

import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 提供基础值、映射和集合字段，用于演示 Telegram 消息组装后的文本结构。
 */
public class TestTeleGramMsg {
	
	@TelegramMsgKey(keyName = "用户名")
	private String userName;
	
	@TelegramMsgKey(keyName = "年龄")
	private Integer age;
	
	@TelegramMsgKey(keyName = "身高")
	private Double high;

	@TelegramMsgKey(keyName = "用户其他信息")
	private Map<String,Object> info;
	
	@TelegramMsgKey(keyName = "用户爱好")
	private List<Hobby> hobbyList;

	public TestTeleGramMsg(String userName, Integer age, Double high, Map<String, Object> info, List<Hobby> hobbyList) {
		super();
		this.userName = userName;
		this.age = age;
		this.high = high;
		this.info = info;
		this.hobbyList = hobbyList;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public List<Hobby> getHobbyList() {
		return hobbyList;
	}

	public void setHobbyList(List<Hobby> hobbyList) {
		this.hobbyList = hobbyList;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Map<String, Object> getInfo() {
		return info;
	}

	public void setInfo(Map<String, Object> info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "TestTeleGramMsg [userName=" + userName + ", age=" + age + ", high=" + high + ", info=" + info
				+ ", hobbyList=" + hobbyList + "]";
	}
	
}
