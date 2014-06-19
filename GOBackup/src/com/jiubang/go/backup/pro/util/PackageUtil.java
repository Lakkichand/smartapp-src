package com.jiubang.go.backup.pro.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

/**
 * @author GoBackup Dev Team
 */
public class PackageUtil {

	public static List<ResolveInfo> getAppWithHomeAction(Context context) {
		if (context == null) {
			return null;
		}
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		return pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	}

	public static boolean isSystemAndLauncherApp(List<ResolveInfo> systemAndLauncherApps,
			String packageName) {
		if (systemAndLauncherApps == null || packageName == null) {
			return false;
		}

		for (ResolveInfo item : systemAndLauncherApps) {
			String name = item.activityInfo.packageName;
			if (name != null && name.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAppWithSameVersionCode(Context context, String packageName,
			int versionCode) {
		if (context == null) {
			throw new IllegalArgumentException("Context cannot be null!");
		}
		PackageManager pm = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(packageName, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (info == null) {
			return false;
		}
		if (info.versionCode != versionCode) {
			return false;
		}
		return true;
	}

	public static boolean isPackageInstalled(Context context, String packageName) {
		if (context == null || packageName == null) {
			return false;
		}

		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			pi = null;
		}
		return pi != null;
	}
}
