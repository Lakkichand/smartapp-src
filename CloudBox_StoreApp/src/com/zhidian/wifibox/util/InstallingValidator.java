package com.zhidian.wifibox.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

/**
 * 
 * 应用安装验证器，负责验证某应用是否已经安装
 * 
 * @author xiedezhi
 * @date [2012-12-25]
 */
public class InstallingValidator {
	/**
	 * 单实例
	 */
	private volatile static InstallingValidator sInstance = null;

	/**
	 * 已经判断过的应用保存起来，不要每次都用PackageManager判断
	 */
	Map<String, Boolean> mInstalledMap = new HashMap<String, Boolean>();

	/**
	 * 默认构造函数
	 */
	private InstallingValidator() {
	}

	/**
	 * 获取单实例
	 */
	public static InstallingValidator getInstance() {
		if (sInstance == null) {
			synchronized (InstallingValidator.class) {
				if (sInstance == null) {
					sInstance = new InstallingValidator();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 判断某应用是否已经安装
	 */
	public boolean isAppExist(Context context, String packageName) {
		if (packageName == null || context == null) {
			return false;
		}
		if (mInstalledMap.containsKey(packageName)) {
			return mInstalledMap.get(packageName);
		}
		boolean ret = AppUtils.isAppExist(context, packageName);
		mInstalledMap.put(packageName, ret);
		return ret;
	}

	/**
	 * 当收到应用安装卸载的广播时，更新当前map
	 */
	public void onAppAction(Context context, String packageName) {
		if (packageName == null || context == null) {
			return;
		}
		boolean ret = AppUtils.isAppExist(context, packageName);
		mInstalledMap.put(packageName, ret);
	}

	/**
	 * 销毁
	 */
	public void destory() {
		mInstalledMap.clear();
	}

}
