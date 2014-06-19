package com.smartapp.rootuninstaller.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Debug;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.Formatter;

import com.smartapp.rootuninstaller.ListDataBean;
import com.smartapp.rootuninstaller.MainActivity;

/**
 * 主界面数据管理类
 * 
 * @author xiedezhi
 * 
 */
public class MainDataController {

	public static boolean isCollectionEmpty(Collection<?> collection) {
		return collection == null || collection.size() <= 0;
	}

	/**
	 * 计算包名对应的运行内存大小，如果一个进程有多个包名，该进程的运行内存平分给每个包名
	 */
	public static Map<String, Integer> getRunningAppMemory(Context context) {
		// TODO 定时更新运行内存信息，再刷新列表
		Map<String, Integer> map = new HashMap<String, Integer>();

		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
				.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
			String[] pkgNameList = appProcess.pkgList;

			int[] myMempid = new int[] { appProcess.pid };
			Debug.MemoryInfo[] memoryInfo = mActivityManager
					.getProcessMemoryInfo(myMempid);
			// 获取进程占内存用信息 kb单位
			int memSize = memoryInfo[0].dalvikPrivateDirty;
			// 每个包名平均内存值
			int perMemSize = memSize / pkgNameList.length;

			for (String pkg : pkgNameList) {
				if (map.containsKey(pkg)) {
					int value = map.get(pkg);
					value = value + perMemSize;
					map.put(pkg, value);
				} else {
					map.put(pkg, perMemSize);
				}
			}
		}
		return map;
	}

	/**
	 * 获取用户应用、系统应用和冻结应用列表
	 */
	public static void getMainAppList(final Context context,
			List<ListDataBean> userApps, List<ListDataBean> systemApps,
			List<ListDataBean> disableApps, final Handler handler) {
		if (userApps == null || systemApps == null || disableApps == null) {
			return;
		}
		userApps.clear();
		systemApps.clear();
		disableApps.clear();

		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
		if (isCollectionEmpty(packageInfos)) {
			return;
		}
		// 获取正在运行的应用的运行内存大小
		Map<String, Integer> map = getRunningAppMemory(context);

		for (PackageInfo info : packageInfos) {
			String packageName = info.packageName;
			if (packageName != null
					&& packageName.equals("com.smartapp.rootuninstaller")) {
				// 过滤掉自己
				continue;
			}
			// TODO 还要过滤掉其他吗？

			final ListDataBean bean = new ListDataBean();
			bean.mInfo = info;
			bean.mIsSelect = false;
			// 应用名称
			bean.mAppName = bean.mInfo.applicationInfo.loadLabel(pm).toString()
					.trim();
			// 判断是否系统应用
			ApplicationInfo app = info.applicationInfo;
			if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				bean.mIsSystemApp = false;
			} else {
				bean.mIsSystemApp = true;
			}
			// 判断是否SD卡应用
			if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
				bean.mIsSDCardApp = true;
			} else {
				bean.mIsSDCardApp = false;
			}
			// 获取程序大小
			try {
				Method getPackageSizeInfo = pm.getClass().getMethod(
						"getPackageSizeInfo", String.class,
						IPackageStatsObserver.class);
				getPackageSizeInfo.invoke(pm, packageName,
						new IPackageStatsObserver.Stub() {
							@Override
							public void onGetStatsCompleted(
									PackageStats pStats, boolean succeeded)
									throws RemoteException {
								bean.mCacheSize = pStats.cacheSize;
								bean.mDataSize = pStats.dataSize;
								bean.mCodeSize = pStats.codeSize;
								bean.mTotalSize = pStats.cacheSize
										+ pStats.dataSize + pStats.codeSize;
								bean.mFileSize = Formatter.formatFileSize(
										context, bean.mTotalSize);
								handler.sendEmptyMessage(MainActivity.REFRESH_LIST);
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 获取应用最后更新时间
			Date date = new Date(
					new File(info.applicationInfo.sourceDir).lastModified());
			bean.mDate = date;
			int imonth = date.getMonth();
			String month = (imonth + 1 < 10) ? ("0" + (imonth + 1))
					: ("" + (imonth + 1));
			int iday = date.getDate();
			String day = (iday < 10) ? ("0" + iday) : ("" + iday);
			bean.mLastModified = (date.getYear() + 1990) + "-" + month + "-"
					+ day;
			// 获取程序运行内存大小
			if (map.containsKey(packageName)) {
				int size = map.get(packageName);
				if (size >= 1024) {
					bean.mRunningMemory = ((int) (map.get(packageName) / 1024.0f * 100.0f))
							/ 100.0f + "MB";
				} else {
					bean.mRunningMemory = size + "KB";
				}
			} else {
				bean.mRunningMemory = null;
			}

			int state = pm.getApplicationEnabledSetting(packageName);
			if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					&& state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
				if (bean.mIsSystemApp) {
					systemApps.add(bean);
				} else {
					userApps.add(bean);
				}
			} else {
				disableApps.add(bean);
			}
		}

	}

}