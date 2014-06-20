package com.smartapp.rootuninstaller;

import java.util.Date;

import android.content.pm.PackageInfo;

/**
 * 应用数据单元封装类
 * 
 * @author xiedezhi
 * 
 */
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
	 * 是否SD卡应用
	 */
	public boolean mIsSDCardApp;
	/**
	 * 缓存大小
	 */
	public long mCacheSize;
	/**
	 * 数据大小
	 */
	public long mDataSize;
	/**
	 * 应用程序大小
	 */
	public long mCodeSize;
	/**
	 * 总大小
	 */
	public long mTotalSize;
	/**
	 * 总大小
	 */
	public String mFileSize;
	/**
	 * 在列表中被选中
	 */
	public boolean mIsSelect;
	/**
	 * 应用名称
	 */
	public String mAppName;
	/**
	 * 应用最后更新时间
	 */
	public Date mDate;
	/**
	 * 应用最后更新时间（年月日）
	 */
	public String mLastModified;
	/**
	 * 运行内存（KB）
	 */
	public int mRunningMemoryInt;
	/**
	 * 运行内存，如果应用没在运行，设为0
	 */
	public String mRunningMemory;

}
