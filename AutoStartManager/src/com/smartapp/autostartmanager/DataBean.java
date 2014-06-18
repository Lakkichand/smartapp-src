package com.smartapp.autostartmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

public class DataBean implements Comparable<DataBean> {
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
	 * 是否已经禁止
	 */
	public boolean mIsForbid;
	/**
	 * 这个应用的开机启动receiver
	 */
	public List<ResolveInfo> mBootReceiver = new ArrayList<ResolveInfo>();
	/**
	 * 这个应用的后台启动receiver
	 */
	public List<ResolveInfo> mBackgroundReceiver = new ArrayList<ResolveInfo>();

	@Override
	public int compareTo(DataBean another) {
		// 根据使用内存排序
		return another.mMemory - mMemory;
	}
}
