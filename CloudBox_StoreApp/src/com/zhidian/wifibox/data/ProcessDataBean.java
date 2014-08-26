package com.zhidian.wifibox.data;

import android.content.pm.ApplicationInfo;

/**
 * 进程管理数据bean
 * 
 * @author xiedezhi
 * 
 */
public class ProcessDataBean implements Comparable<ProcessDataBean> {
	/**
	 * 应用名称
	 */
	public String mName;
	/**
	 * 应用信息
	 */
	public ApplicationInfo mInfo;
	/**
	 * 已使用内存，单位KB
	 */
	public int mMemory;
	/**
	 * cpu使用率
	 */
	public String mCpuRate;
	/**
	 * 是否系统应用
	 */
	public boolean mIsSysApp;
	/**
	 * 是否保护
	 */
	public boolean mIsProtection;

	@Override
	public int compareTo(ProcessDataBean another) {
		// 根据使用内存排序
		return another.mMemory - mMemory;
	}

}
