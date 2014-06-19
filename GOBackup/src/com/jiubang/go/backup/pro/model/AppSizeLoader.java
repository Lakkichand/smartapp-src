package com.jiubang.go.backup.pro.model;

import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;

import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>类描述:
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-9-20]
 */
public class AppSizeLoader extends IPackageStatsObserver.Stub {
	private Context mContext;
	private PackageManager mPackageManager;
	private List<AppInfo> mAppInfoList;
	private long mAllAppSize;

	public AppSizeLoader(Context context, List<AppInfo> allAppInfo) {
		mContext = context;
		mPackageManager = mContext.getPackageManager();
		mAppInfoList = allAppInfo;
		mAllAppSize = 0;
	}

	public long calcAllAppSize() {
		if (Util.isCollectionEmpty(mAppInfoList)) {
			return 0;
		}
		mAllAppSize = 0;
		for (int i = 0; i < mAppInfoList.size(); i++) {
			getAppSize(mAppInfoList.get(i));
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return mAllAppSize;
	}

	//获取应用程序信息
	public void getAppSize(AppInfo appinfo) {
		try {
			Method getPackageSizeInfo = mPackageManager.getClass().getMethod("getPackageSizeInfo",
					String.class, android.content.pm.IPackageStatsObserver.class);
			getPackageSizeInfo.invoke(mPackageManager, appinfo.packageName, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
		mAllAppSize += pStats.codeSize + pStats.cacheSize + pStats.dataSize;
		final int androidVersion = Util.getAndroidSystemVersion();
		if (androidVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mAllAppSize += pStats.externalCodeSize;
		}
		synchronized (this) {
			notify();
		}
	}

}
