package com.smartapp.appfreezer;

import android.content.pm.PackageInfo;

public class ListDataBean {
	/**
	 * 应用信息
	 */
	public PackageInfo mInfo;
	/**
	 * 系统应用
	 */
	public boolean mIsSystemApp;
	/**
	 * 在列表中被选中
	 */
	public boolean mIsSelect;
	/**
	 * 应用名称
	 */
	public String mAppName;
}
