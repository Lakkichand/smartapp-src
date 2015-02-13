package com.zhidian.wifibox.data;

public class MemoryBean {
	/**
	 * 手机总内存
	 */
	long memoryAvail;
	/**
	 * 手机剩余内存
	 */
	long memorySize;
	/**
	 * SD卡内存
	 */
	long totalSdMemory;
	/**
	 * SD卡剩余内存
	 */
	long availSdMemory;
	
	public long getMemoryAvail() {
		return memoryAvail;
	}
	public void setMemoryAvail(long memoryAvail) {
		this.memoryAvail = memoryAvail;
	}
	public long getMemorySize() {
		return memorySize;
	}
	public void setMemorySize(long memorySize) {
		this.memorySize = memorySize;
	}
	public long getTotalSdMemory() {
		return totalSdMemory;
	}
	public void setTotalSdMemory(long totalSdMemory) {
		this.totalSdMemory = totalSdMemory;
	}
	public long getAvailSdMemory() {
		return availSdMemory;
	}
	public void setAvailSdMemory(long availSdMemory) {
		this.availSdMemory = availSdMemory;
	}
}
