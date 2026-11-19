package com.zero.utils.sms;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 承载 PaaSoo 单条短信接口返回的状态和消息标识，发送入口据此判断受理结果。
 */
public class PaaSooSmsSingleResult {

	/** 供应商返回的业务状态，发送入口只将数值 0 视为受理成功。 */
	private String status;
	/** 供应商返回的消息标识，用于后续查询发送记录。 */
	private String messageid;
	/** 供应商返回的状态说明，例如缺少必填参数。 */
	private String status_code;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessageid() {
		return messageid;
	}

	public void setMessageid(String messageid) {
		this.messageid = messageid;
	}

	public String getStatus_code() {
		return status_code;
	}

	public void setStatus_code(String status_code) {
		this.status_code = status_code;
	}
}
