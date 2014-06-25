package com.smartapp.autostartmanager;

import java.util.Collection;

import android.content.Context;
import android.text.TextUtils;

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
	 * 解冻组件
	 */
	public static boolean enableClass(String packageName, String className) {
		if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
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
		String cmd = "pm enable " + packageName + "/" + className;
		String result = new RootShell.Command(cmd).execute(rootShell);
		if (result != null && result.contains("enabled")) {
			return true;
		}
		return false;
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
	 * 冻结组件
	 */
	public static boolean disableClass(String packageName, String className) {
		if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
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
		final String cmd = "pm disable " + packageName + "/" + className;
		String result = new RootShell.Command(cmd).execute(rootShell);
		if (result != null && result.contains("disabled")) {
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
}
