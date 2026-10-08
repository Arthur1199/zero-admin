package com.zero.vo;

/**
 * 作者: Jack
 * 时间: 2023年4月21日下午3:38:52
 * 说明: 微服务Vo
 *
 */
public class MicroservicesMsgVo {
	//微服务运行环境
	private String runEnv;
	//提示标题
    private String title = "";
	//微服务状态
	private String statusName;
	//微服务名称
	private String clientName;
	//服务器ip地址
	private String clientServerUrl;
	//创建时间
	private String createTime;
	//错误消息
	private String errorMsg;

	public MicroservicesMsgVo(String runEnv,String title, String statusName, String clientName, String clientServerUrl,
			String createTime, String errorMsg) {
		super();
		this.runEnv = runEnv;
		this.title = title;
		this.statusName = statusName;
		this.clientName = clientName;
		this.clientServerUrl = clientServerUrl;
		this.createTime = createTime;
		this.errorMsg = errorMsg;
	}

	public MicroservicesMsgVo() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientServerUrl() {
		return clientServerUrl;
	}

	public void setClientServerUrl(String clientServerUrl) {
		this.clientServerUrl = clientServerUrl;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getRunEnv() {
		return runEnv;
	}

	public void setRunEnv(String runEnv) {
		this.runEnv = runEnv;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "微服务运行环境：---"+ runEnv + "---" + "\r\n" + "**************************************" + "\r\n" + title + "\r\n"+"微服务状态：" + statusName + "\r\n"+"微服务名称：" + clientName
				+ "\r\n"+"微服务器ip地址：" + clientServerUrl + "\r\n"+ "提示信息：" + errorMsg + "\r\n" + "系统时间：" + createTime;
	}
	
}
