/**
 * 
 */
package com.jiubang.ggheart.appgame.appcenter.bean;

import android.graphics.drawable.Drawable;

/**
 * 安装包info
 * @author liguoliang
 *
 */
public class AppPackageInfoBean {
	public String mFilePath;
	
	public Drawable mIcon;
	
	public String mName;
	
	public String mPackageName;
	
	public String mSize;
	
	public String mVersionName;
	
	public int mVersionCode;
	
	/**
	 * 安装包状态
	 */
	public int mState = STATE_INSTALL;
	
	/**
	 * 已安装当前版本
	 */
	public static final int STATE_INSTALLED = 100;
	
	/**
	 * 可安装
	 */
	public static final int STATE_INSTALL = 101;
	
	/**
	 * 可更新
	 */
	public static final int STATE_UPDATE = 102; 
	
	/**
	 * 安装包版本较低
	 */
	public static final int STATE_VERSION_LOWER = 103;
}
