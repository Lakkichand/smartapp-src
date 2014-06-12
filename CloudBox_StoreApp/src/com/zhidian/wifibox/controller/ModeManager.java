package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 只负责记录当前是普通模式还是极速模式
 * 
 * @author xiedezhi
 * 
 */
public class ModeManager {

	 public static final String XMODEPREFIX = "MI-BOX";
	/**
	 * 普通网络模式
	 */
	public static final int COMMON_MODE = 7001;
	/**
	 * 极速网络模式
	 */
	public static final int X_MODE = 7002;

	private boolean mIsRapidly = false;

	/**
	 * 当处于极速模式时，记录连上的WIFI名称
	 */
	private String mRapName = "";

	/**
	 * 单实例
	 */
	private volatile static ModeManager sInstance = null;

	/**
	 * 获取TabDataManager实例对象
	 */
	public static ModeManager getInstance() {
		if (sInstance == null) {
			synchronized (ModeManager.class) {
				if (sInstance == null) {
					sInstance = new ModeManager();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 初始化函数
	 */
	private ModeManager() {
	}

	/**
	 * 当前是否极速模式
	 */
	public synchronized boolean isRapidly() {
		return mIsRapidly;
	}

	/**
	 * 设置是否极速模式
	 */
	public synchronized void setRapidly(boolean rap) {
		this.mIsRapidly = rap;
		if (rap) {
			mRapName = InfoUtil.getCurWifiName(TAApplication.getApplication());
		} else {
			mRapName = "";
		}
	}

	/**
	 * 获取极速模式连上的WIFI名称
	 */
	public synchronized String getRapName() {
		return mRapName;
	}

	/**
	 * 检查是否在极速模式
	 */
	public static boolean checkRapidly() {
		String wifiName = InfoUtil.getCurWifiName(TAApplication
				.getApplication());
		if (wifiName != null && wifiName.indexOf(XMODEPREFIX) != -1) {
			return true;
		}
		return false;
	}
}
