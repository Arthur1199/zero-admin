package com.zero.utils.sms;

/**
 * 作者: Jack
 * 时间: 2022年10月25日下午4:53:33
 * 说明: PaaSoo发送单条短信的返回结果
 *
 */
public class PaaSooSmsSingleResult {

	//状态码为"0"，成功，其他为失败
//	0 - success：成功
//
//	2 - Missing parameters：缺少必要参数
//
//	3 - Invalid parameters：参数格式错误
//
//	4 - Invalid credentials：Key或Secret错误
//
//	5 - Unauthorized IP：IP限制
//
//	6 - Invalid phone number：号码格式错误
//
//	7 - Invalid sender id：from参数格式错误
//
//	8 - Message bombing detected: 3秒内重复请求
//
//	9 - Quota exceeded：欠费或信用额度不足
//
//	10 - Throttling error：超过限速
//
//	11 - System error：系统错误
//
//	19 -Invalid peid / Invalid templateid
	
	private String status;
	//消息id 示例 015bd4-d6dfa7-58w
	private String messageid;
    //状态描述 示例 Missing parameters
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
