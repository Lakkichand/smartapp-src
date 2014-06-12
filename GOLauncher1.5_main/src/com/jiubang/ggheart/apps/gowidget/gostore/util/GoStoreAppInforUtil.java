package com.jiubang.ggheart.apps.gowidget.gostore.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class GoStoreAppInforUtil {
	/**
	 * 检测是否有更新的方法
	 * 
	 * @param context
	 *            应用上下文
	 * @param packageName
	 *            待检测应用的包名
	 * @param versionCode
	 *            最新的版本,要大于等于0
	 * @return 如果给的版本号高于本机已经安装的版本，就返回TRUE；否则返回FALSE；
	 */
	public static boolean isNewToAlreadyInstall(Context context, String packageName, int versionCode) {
		boolean result = false;
		if (context != null && packageName != null && !"".equals(packageName) && versionCode >= 0) {
			PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
					int localVersionCode = 0;
					if (packageInfo != null) {
						localVersionCode = packageInfo.versionCode;
						if (versionCode > localVersionCode) {
							result = true;
						}
					}
				} catch (NameNotFoundException e) {

				}
			}
		}
		return result;
	}

	/**
	 * 获取应用版本名称的方法
	 * 
	 * @param context
	 *            应用上下文
	 * @param packageName
	 *            待获取应用的包名
	 * @return 应用版本名称
	 */
	public static String getAppVersionName(Context context, String packageName) {
		String versionName = null;
		if (context != null && packageName != null && !"".equals(packageName)) {
			PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
					if (packageInfo != null) {
						versionName = packageInfo.versionName;
					}
				} catch (NameNotFoundException e) {

				}
			}
		}
		return versionName;
	}

	/**
	 * 获取应用版本号的方法
	 * 
	 * @param context
	 *            应用上下文
	 * @param packageName
	 *            待获取应用的包名
	 * @return 应用版本号
	 */
	public static int getAppVersionCode(Context context, String packageName) {
		int versionCode = 0;
		if (context != null && packageName != null && !"".equals(packageName)) {
			PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
					if (packageInfo != null) {
						versionCode = packageInfo.versionCode;
					}
				} catch (NameNotFoundException e) {

				}
			}
		}
		return versionCode;
	}

	/**
	 * 获取本应用版本号的方法
	 * 
	 * @param context
	 *            应用上下文
	 * @return 本应用版本号
	 */
	public static int getThisAppVersionCode(Context context) {
		int versionCode = 0;
		if (context != null) {
			versionCode = getAppVersionCode(context, context.getPackageName());
		}
		return versionCode;
	}

	/**
	 * 通过应用程序包名判断程序是否安装的方法
	 * 
	 * @param packageName
	 *            应用程序包名
	 * @return 程序已安装返回TRUE，否则返回FALSE
	 */
	public static boolean isApplicationExsit(Context context, String packageName) {
		boolean result = false;
		if (context != null && packageName != null) {
			try {
				// context.createPackageContext(packageName,
				// Context.CONTEXT_IGNORE_SECURITY);
				context.getPackageManager().getPackageInfo(packageName,
						PackageManager.GET_SHARED_LIBRARY_FILES);
				result = true;
			} catch (Exception e) {
				// Log.i("store", "ThemeStoreUtil.isApplicationExsit for " +
				// packageName + " is exception");
			}
		}
		return result;
	}

	/**
	 * 是否存在google电子市场
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isExistGoogleMarket(Context context) {
		String googleMarketPkgName = "com.android.vending";
		return isApplicationExsit(context, googleMarketPkgName);
	}
}
