package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.util.Comparator;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.model.BackupDBHelper.AppTable;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 应用程序Info
 *
 * @author maiyongshen
 */
public class AppInfo {
	public static final int APP_SYSTEM = 1;
	public static final int APP_USER = 2;
	public static final int APP_PRIVATE = 3;
	public static final int APP_INSTALL_EXTERNAL = 1;
	public static final int APP_INSTALL_INTERNAL = 2;

	private ApplicationInfo mApplicationInfo;
	public String appName;
	public String packageName = "";
	public String versionName = "";
	public int versionCode = 0;
	private int mFlags;
	public String publicSourceDir;
	public String sourceDir;
	public String dataDir;
	public long codeSize;
	public long dataSize;
	public long cacheSize;
	public int appType;
	public String mApkMd5;
	public long installTime;

	public AppInfo() {

	}

	public AppInfo(Cursor cursor) {
		appName = cursor.getString(cursor.getColumnIndex(AppTable.APP_NAME));
		cacheSize = cursor.getLong(cursor.getColumnIndex(AppTable.APP_CACHE_SIZE));
		codeSize = cursor.getLong(cursor.getColumnIndex(AppTable.APP_CODE_SIZE));
		dataSize = cursor.getLong(cursor.getColumnIndex(AppTable.APP_DATA_SIZE));
//		Bitmap iconBitmap = Util.byteArrayToBitmap(cursor.getBlob(cursor
//				.getColumnIndex(AppTable.APP_ICON)));
//		icon = iconBitmap != null ? new BitmapDrawable(iconBitmap) : null;
		packageName = cursor
				.getString(cursor.getColumnIndex(AppTable.APP_PACKAGE));
		sourceDir = cursor.getString(cursor.getColumnIndex(AppTable.APP_PATH));
		dataDir = cursor.getString(cursor.getColumnIndex(AppTable.APP_DATA_PATH));
		appType = cursor.getInt(cursor.getColumnIndex(AppTable.APP_TYPE));
		versionCode = cursor.getInt(cursor
				.getColumnIndex(AppTable.APP_VERSION_CODE));
		versionName = cursor.getString(cursor
				.getColumnIndex(AppTable.APP_VERSION_NAME));
		mApkMd5 = cursor.getString(cursor.getColumnIndex(AppTable.APK_MD5));
	}

	public AppInfo(PackageInfo pi, PackageManager pm) {
		if (pi != null && pm != null) {
			mApplicationInfo = pi.applicationInfo;
			if (mApplicationInfo != null) {
				mFlags = mApplicationInfo.flags;
				publicSourceDir = mApplicationInfo.publicSourceDir;
				sourceDir = mApplicationInfo.sourceDir;
				dataDir = mApplicationInfo.dataDir;
			}
			packageName = pi.packageName;
			versionCode = pi.versionCode;
			versionName = pi.versionName;
			appType = isSystemApp() ? APP_SYSTEM : (isPrivateApp() ? APP_PRIVATE : APP_USER);
			installTime = Util.getAndroidSystemVersion() >= 9 ? pi.firstInstallTime : new File(publicSourceDir).lastModified();
		}
	}
	
	public AppInfo(AppInfo toCopy) {
		mApplicationInfo = toCopy.mApplicationInfo;
		appName = toCopy.appName != null ? new String(toCopy.appName) : null;
		packageName = toCopy.packageName != null ? new String(toCopy.packageName) : null;
		versionName = toCopy.versionName != null ? new String(toCopy.versionName) : null;
		versionCode = toCopy.versionCode;
		mFlags = toCopy.mFlags;
		publicSourceDir = toCopy.publicSourceDir != null ? new String(toCopy.publicSourceDir) : null;
		sourceDir = toCopy.sourceDir != null ? new String(toCopy.sourceDir) : null;
		dataDir = toCopy.dataDir != null ? new String(toCopy.dataDir) : null;
		codeSize = toCopy.codeSize;
		dataSize = toCopy.dataSize;
		cacheSize = toCopy.cacheSize;
		appType = toCopy.appType;
		mApkMd5 = toCopy.mApkMd5 != null ? new String(toCopy.mApkMd5) : null;
		installTime = toCopy.installTime;
	}

	public long getTotalSize() {
		return codeSize + dataSize + cacheSize;
	}

	public boolean isSystemApp() {
		return (mFlags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}

	public boolean isPrivateApp() {
		if (sourceDir == null) {
			return true;
		}
		if (sourceDir.startsWith("/data/app-private")) {
			return true;
		}
		return false;
	}

	public boolean isUpdatedSystemApp() {
		return (mFlags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
	}

/*	public boolean loadLabel(PackageManager pm) {
		if (pm == null || mApplicationInfo == null) {
			return false;
		}
		appName = Util.trimAllSpace(mApplicationInfo.loadLabel(pm).toString());
		return true;
	}*/
	
	public boolean isApplicationNameValid() {
		return !TextUtils.isEmpty(appName);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static interface OnAppNameLoadListener {
		public void onAppNameLoaded(String packageName, String appName);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static class InstallTimeComparator implements Comparator<AppInfo> {
		@Override
		public int compare(AppInfo lhs, AppInfo rhs) {
			long time1 = lhs.installTime > 0 ? lhs.installTime : new File(lhs.publicSourceDir).lastModified();
			long time2 = rhs.installTime > 0 ? rhs.installTime : new File(rhs.publicSourceDir).lastModified();
			return time1 > time2 ? -1 : time1 < time2 ? 1 : 0;
		}
	}
}
