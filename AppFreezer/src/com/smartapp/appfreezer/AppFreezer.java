package com.smartapp.appfreezer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

/**
 * 工具类
 */
public class AppFreezer {

	public static boolean isCollectionEmpty(Collection<?> collection) {
		return collection == null || collection.size() <= 0;
	}

	/**
	 * 获取所有已启用和已禁用的应用
	 */
	public static void getEnableAndDisableAppByApi(Context context,
			List<ListDataBean> enableList, List<ListDataBean> disableList) {
		if (enableList == null || disableList == null) {
			return;
		}

		enableList.clear();
		disableList.clear();

		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
		if (isCollectionEmpty(packageInfos)) {
			return;
		}

		for (PackageInfo info : packageInfos) {
			String packageName = info.packageName;
			if (packageName != null
					&& packageName.equals("com.smartapp.appfreezer")) {
				// 过滤掉自己
				continue;
			}

			ListDataBean bean = new ListDataBean();
			bean.mInfo = info;
			bean.mIsSelect = false;
			// 判断是否系统应用
			ApplicationInfo app = info.applicationInfo;
			if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				bean.mIsSystemApp = false;
			} else {
				bean.mIsSystemApp = true;
			}
			// 应用名称
			bean.mAppName = bean.mInfo.applicationInfo.loadLabel(pm).toString()
					.trim();

			int state = pm.getApplicationEnabledSetting(packageName);
			if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
				// su过滤掉
				if (packageIsSuperUser(packageName)) {
					continue;
				}
				// System Service 过滤掉
				if (pm.getLaunchIntentForPackage(packageName) == null) {
					continue;
				}
				enableList.add(bean);
			} else {
				disableList.add(bean);
			}
		}

		Collections.sort(enableList, new IComparator());
		Collections.sort(disableList, new IComparator());
	}

	private static boolean packageIsSuperUser(String packName) {
		return (packName.equals("com.noshufou.android.su"))
				|| (packName.equals("eu.chainfire.supersu"));
	}

	/**
	 * 比较器
	 * 
	 * @author xiedezhi
	 * 
	 */
	public static class IComparator implements Comparator<ListDataBean> {

		private final Collator sCollator = Collator.getInstance();

		@Override
		public final int compare(ListDataBean aa, ListDataBean ab) {
			CharSequence sa = aa.mAppName;
			if (sa == null) {
				sa = "";
			}
			CharSequence sb = ab.mAppName;
			if (sb == null) {
				sb = "";
			}
			return sCollator.compare(sa.toString(), sb.toString());
		}
	}

	/**
	 * 解冻应用
	 */
	public static boolean enablePackage(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return false;
		}
		RootShell shell = null;
		try {
			shell = RootShell.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (shell == null) {
			return false;
		}
		final String cmd = "pm enable " + packageName;
		String result = null;
		try {
			result = shell.excuteCMD(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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
		RootShell shell = null;
		try {
			shell = RootShell.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (shell == null) {
			return false;
		}
		final String cmd = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH ? "pm disable-user "
				+ packageName
				: "pm disable " + packageName;
		String result = null;
		try {
			result = shell.excuteCMD(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (result != null && result.contains("disabled")) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取Android中的Linux内核版本号
	 * 
	 */
	public static String getLinuxKernel() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (null == process) {
			return null;
		}

		// get the output line
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
		String result = "";
		String line;

		// get the whole standard output string
		try {
			while ((line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result != "") {
			String Keyword = "version ";
			int index = result.indexOf(Keyword);
			line = result.substring(index + Keyword.length());
			if (null != line) {
				index = line.indexOf(" ");
				return line.substring(0, index);
			}
		}
		return null;
	}

}
