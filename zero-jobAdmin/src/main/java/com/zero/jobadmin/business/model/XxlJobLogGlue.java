package com.zero.jobadmin.business.model;

import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 对应 xxl_job_logglue 的 GLUE 脚本历史记录，保存脚本内容和版本信息。
 */
public class XxlJobLogGlue {
	
	/** 脚本历史版本的主键。 */
	private int id;
	/** 脚本所属任务的主键。 */
	private int jobId;
	/** 脚本处理类型，对应 GlueTypeEnum。 */
	private String glueType;
	/** 该版本保存的完整脚本内容。 */
	private String glueSource;
	/** 维护脚本时填写的版本说明。 */
	private String glueRemark;
	/** 脚本历史版本创建时间。 */
	private Date addTime;
	/** 脚本历史版本最近更新时间。 */
	private Date updateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String getGlueType() {
		return glueType;
	}

	public void setGlueType(String glueType) {
		this.glueType = glueType;
	}

	public String getGlueSource() {
		return glueSource;
	}

	public void setGlueSource(String glueSource) {
		this.glueSource = glueSource;
	}

	public String getGlueRemark() {
		return glueRemark;
	}

	public void setGlueRemark(String glueRemark) {
		this.glueRemark = glueRemark;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
