package com.zero.utils.file.disk;

import com.zero.utils.notify.telegram.TelegramMsgKey;

/**
 * 作者: Jack
 * 时间: 2025年3月5日18:31:35
 * 说明: 磁盘空间对象
 *
 */
public class DiskSpaceSummary {

	//是否警告
	private boolean isWarning = false;
	
	// 所有磁盘的总容量
	@TelegramMsgKey(keyName = "磁盘总容量")
	private String totalCapacity = "";  
	
	//所有磁盘的已用空间
	@TelegramMsgKey(keyName = "磁盘已用空间")
	private String totalUsedSpace = "";
	
	// 所有磁盘的可用空间
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
