package com.jiubang.go.backup.pro.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.RootShell;

/**
 * @author maiyongshen
 *
 */
public class AppFreezer {
	private static Set<String> sEnabledPackages;
	private static Set<String> sDisabledPackages;
	
	static {
		sEnabledPackages = new HashSet<String>();
		sDisabledPackages = new HashSet<String>();
	}

	
	public static synchronized List<String> getEnabledPackages(Context context) {
		long t = System.currentTimeMillis();
		final String cmd = "pm list package -e";
		String result = executeCmdWithRootProcess(context, cmd);
		List<String> packages = parseAndFilter(context, result);
		if (Util.isCollectionEmpty(packages)) {
			packages = getEnabledPackagesByApi(context);
		}
		
		sEnabledPackages.clear();
		if (!Util.isCollectionEmpty(packages)) {
			sEnabledPackages.addAll(packages);
		}
		t = System.currentTimeMillis() - t;
//		LogUtil.d("getEnabledPackages time = " + t);
		return packages;
	}
	
	public static synchronized List<String> getDisabledPackages(Context context) {
		long t = System.currentTimeMillis();
		final String cmd = "pm list package -d";
		String result = executeCmdWithRootProcess(context, cmd);
		List<String> packages = parseAndFilter(context, result);
		if (Util.isCollectionEmpty(packages)) {
			packages = getDisabledPackagesByApi(context);
		}
		
		sDisabledPackages.clear();
		if (!Util.isCollectionEmpty(packages)) {
			sDisabledPackages.addAll(packages);
		}
		t = System.currentTimeMillis() - t;
//		LogUtil.d("getDisabledPackages time = " + t);
		return packages;
	}
	
	private static synchronized List<String> getEnabledPackagesByApi(Context context) {
		final String cmd = "pm list package";
		String result = executeCmdWithRootProcess(context, cmd);
		List<String> packages = parseAndFilter(context, result);
		if (Util.isCollectionEmpty(packages)) {
			return null;
		}
		List<String> enabledPackages = new ArrayList<String>();
		PackageManager pm = context.getPackageManager();
		for (String packageName : packages) {
			int state = pm.getApplicationEnabledSetting(packageName);
			if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
				enabledPackages.add(packageName);
			}
		}
		return !Util.isCollectionEmpty(enabledPackages) ? enabledPackages : null;
	}
	
	private static synchronized List<String> getDisabledPackagesByApi(Context context) {
		final String cmd = "pm list package";
		String result = executeCmdWithRootProcess(context, cmd);
		List<String> packages = parseAndFilter(context, result);
		if (Util.isCollectionEmpty(packages)) {
			return null;
		}
		List<String> disabledPackages = new ArrayList<String>();
		PackageManager pm = context.getPackageManager();
		for (String packageName : packages) {
			int state = pm.getApplicationEnabledSetting(packageName);
			if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					|| state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
				disabledPackages.add(packageName);
			}
		}
		return !Util.isCollectionEmpty(disabledPackages) ? disabledPackages : null;
	}
	
	public static synchronized boolean enablePackage(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return false;
		}
		final String cmd = "pm enable " + packageName;
		String result = executeCmdWithRootProcess(context, cmd);
//		LogUtil.d("enablePackage result = " + result);
		if (result != null && result.contains("enabled")) {
			sDisabledPackages.remove(packageName);
			sEnabledPackages.add(packageName);
			return true;
		}
		return false;
	}
	
	public static synchronized boolean disablePackage(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return false;
		}
		final String cmd = Util.getAndroidSystemVersion() >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH
				? "pm disable-user " + packageName
				: "pm disable " + packageName;
		String result = executeCmdWithRootProcess(context, cmd);
//		LogUtil.d("disablePackage result = " + result);
		if (result != null && result.contains("disabled")) {
			sDisabledPackages.add(packageName);
			sEnabledPackages.remove(packageName);
			return true;
		}
		return false;
	}
	
	private static List<String> parseAndFilter(Context context, String string) {
		if (TextUtils.isEmpty(string)) {
			return null;
		}
		String[] strings = string.split("\n");
		List<String> result = new ArrayList<String>();
		final BackupManager bm = BackupManager.getInstance();
		if (strings != null && strings.length > 0) {
			final Pattern pattern = Pattern.compile("package:");
			for (String subString : strings) {
				final Matcher matcher = pattern.matcher(subString);
				if (matcher.find()) {
					String packageName = matcher.replaceAll("");
					if (!TextUtils.isEmpty(packageName)) {
						// 过滤自身
						if (TextUtils.equals(packageName, context.getPackageName())) {
							continue;
						}
						// 过滤系统程序
						AppInfo appInfo = bm.getAppInfo(context, packageName);
						if (appInfo == null || appInfo.isSystemApp()) {
							continue;
						}
						result.add(packageName);
					}
				}
			}
		}
		return result;
	}
	
	private static String executeCmdWithRootProcess(Context context, String cmd) {
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String result = new RootShell.Command(cmd).execute(rootShell);
		return result;
	}
	
	public static int getDisabledPackagesCount(Context context) {
		if (Util.isCollectionEmpty(sDisabledPackages)) {
			getDisabledPackages(context);
		}
		return sDisabledPackages.size();
	}
	
	public static int getEnabledPackagesCount(Context context) {
		if (Util.isCollectionEmpty(sEnabledPackages)) {
			getEnabledPackages(context);
		}
		return sEnabledPackages.size();
	}
}
