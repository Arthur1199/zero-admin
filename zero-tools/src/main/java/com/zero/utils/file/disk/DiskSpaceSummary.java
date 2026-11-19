package com.zero.utils.file.disk;

import com.zero.utils.notify.telegram.TelegramMsgKey;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 保存所有磁盘根目录汇总后的容量、占用、可用空间和告警状态，支持输出到监控通知。
 */
public class DiskSpaceSummary {

	/** 汇总可用空间达到告警阈值时为 true。 */
	private boolean isWarning = false;
	
	/** 各磁盘根目录的总容量，带展示单位。 */
	@TelegramMsgKey(keyName = "磁盘总容量")
	private String totalCapacity = "";  
	
	/** 总容量扣除 JVM 可用空间后的汇总值，带展示单位。 */
	@TelegramMsgKey(keyName = "磁盘已用空间")
	private String totalUsedSpace = "";
	
	/** 各磁盘根目录对当前进程可用的汇总空间，带展示单位。 */
	@TelegramMsgKey(keyName = "磁盘可用空间")
	private String totalUsableSpace = "";  
	

	public String getTotalCapacity() {
		return totalCapacity;
	}

	public void setTotalCapacity(String totalCapacity) {
		this.totalCapacity = totalCapacity;
	}

	public String getTotalUsableSpace() {
		return totalUsableSpace;
	}

	public void setTotalUsableSpace(String totalUsableSpace) {
		this.totalUsableSpace = totalUsableSpace;
	}

	public String getTotalUsedSpace() {
		return totalUsedSpace;
	}

	public void setTotalUsedSpace(String totalUsedSpace) {
		this.totalUsedSpace = totalUsedSpace;
	}

	public DiskSpaceSummary(String totalCapacity, String totalUsedSpace, String totalUsableSpace) {
		super();
		this.totalCapacity = totalCapacity;
		this.totalUsedSpace = totalUsedSpace;
		this.totalUsableSpace = totalUsableSpace;
	}

	public DiskSpaceSummary() {
		super();
	}

	public boolean isWarning() {
		return isWarning;
	}

	public void setWarning(boolean isWarning) {
		this.isWarning = isWarning;
	}
	
}
