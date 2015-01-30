package com.zhidian.wifibox.util;

import java.util.Collection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.ta.TAApplication;
import com.zhidian.wifibox.root.RootShell;

/**
 * 应用冻结/解冻工具类
 * 
 * @author xiedezhi
 * 
 */
public class AppFreezer {

	public static boolean isCollectionEmpty(Collection<?> collection) {
		return collection == null || collection.size() <= 0;
	}

	/**
	 * 解冻应用
	 */
	public static boolean enablePackage(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return false;
		}
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (rootShell == null) {
			return false;
		}
		String cmd = "pm enable " + packageName;
		String result = new RootShell.Command(cmd).execute(rootShell);
		if (result != null && result.contains("enabled")) {
			return true;
		}
		return false;
	}

	/**
	 * 冻结应用
	 */
	public static boolean disablePackage(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return false;
		}
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (rootShell == null) {
			return false;
		}
		final String cmd = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH ? "pm disable-user "
				+ packageName
				: "pm disable " + packageName;
		String result = new RootShell.Command(cmd).execute(rootShell);
		if (result != null && result.contains("disabled")) {
			return true;
		}
		return false;
	}

	/**
	 * 应用是否被冻结
	 */
	public static boolean isAppFreeze(Context context, String packageName) {
		if (AppUtils.isAppExist(context, packageName)) {
			PackageManager packmanager = context.getPackageManager();
			int state = packmanager.getApplicationEnabledSetting(packageName);
			if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					|| state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
				return true;
			}
		}
		return false;
	}
}
