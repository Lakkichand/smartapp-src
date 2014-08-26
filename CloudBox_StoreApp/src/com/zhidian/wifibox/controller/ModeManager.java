package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 记录当前WIFI盒子信息
 * 
 * @author xiedezhi
 * 
 */
public class ModeManager {
	private static final String XMODEPREFIX = "MI-BOX";

	private static final String XMODEPREFIX_ = "Mi-Box_";

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
	 * 记录当前WIFI盒子名称
	 */
	public synchronized void recordRapName() {
		if (checkRapidly()) {
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
		if (wifiName != null && wifiName.indexOf(XMODEPREFIX_) != -1) {
			return true;
		}
		return false;
	}
}
