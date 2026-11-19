package com.zero.vo;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 承载监控通知的环境、服务地址、状态、发生时间和处理提示，可生成机器人消息文本。
 */
public class MicroservicesMsgVo {
	//微服务运行环境
	/** 实例所属运行环境的显示名称。 */
	private String runEnv;
	//提示标题
    /** 通知标题。 */
    private String title = "";
	//微服务状态
	/** 实例状态的中文显示名称。 */
	private String statusName;
	//微服务名称
	/** 发生状态变化的服务名称。 */
	private String clientName;
	//服务器ip地址
	/** 监控实例注册的服务访问地址。 */
	private String clientServerUrl;
	//创建时间
	/** 用于通知展示的格式化时间。 */
	private String createTime;
	//错误消息
	/** 状态说明或运维提示。 */
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
