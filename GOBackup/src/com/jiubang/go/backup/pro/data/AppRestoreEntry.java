package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.AppTable;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.LinuxShell;
import com.jiubang.go.backup.pro.util.Logger;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author kevin 应用程序恢复条目
 */
public class AppRestoreEntry extends BaseRestoreEntry implements MessageReceiver, AppEntry {

	public static final int REQUEST_INSTALL_APP = 0x00ff4001;

	private AppInfo mAppInfo;
	// private String mRestoreFilePath; //待恢复app的文件路径
	private String mRestoreDir = null;
	private String mApkFullPath;
	private String mAppDataFullPath;
	private PackageChangeReceiver mReceiver = null;
	private Context mContext = null;
	private AppRestoreArgs mArgs = null;
	private IAsyncTaskListener mListener = null;
	private boolean mHasCancel = true;
	private boolean mRestoreSuccess = false;
	private boolean mNeedRestoreData = false;
	private boolean mApkFileExit = false;
	private boolean mDataFileExit = false;

	private AppRestoreType mAppRestoreType;

	/**
	 * 应用程序恢复类型
	 *
	 * @author wencan
	 */
	public enum AppRestoreType {
		APP,
		APP_DATA,
		DATA_ONLY
	}

	public AppRestoreEntry(AppInfo appInfo, String rootPath) {
		super();
		mAppInfo = appInfo;
		mRestoreDir = rootPath;

		init();
	}

	private void init() {
		if (mRestoreDir != null) {
			mRestoreDir = Util.ensureFileSeparator(mRestoreDir);
			mApkFullPath = mRestoreDir + mAppInfo.packageName + ".apk";
			mAppDataFullPath = mRestoreDir + mAppInfo.packageName + ".tar.gz";
		}

		File appFile = new File(mApkFullPath);
		File dataFile = new File(mAppDataFullPath);
		mApkFileExit = appFile.exists();
		mDataFileExit = dataFile.exists();

		if (mApkFileExit && mDataFileExit) {
			mRestorableState = RestorableState.APP_DATA_RESTORABLE;
		} else if (mApkFileExit) {
			mRestorableState = RestorableState.APP_RESTORABLE;
		} else if (mDataFileExit) {
			mRestorableState = RestorableState.DATA_RESTORABLE;
		} else {
			mRestorableState = RestorableState.DATA_CORRUPTION;
		}

		//		if (mAppInfo != null && mAppInfo.icon != null) {
		//			setIcon(mAppInfo.icon);
		//		}
	}

	@Override
	public AppInfo getAppInfo() {
		return mAppInfo;
	}

	public void setAppInfo(AppInfo appInfo) {
		mAppInfo = appInfo;
		init();
	}

	public RestorableState getRestoreState() {
		return mRestorableState;
	}

	public boolean updateDb(Context context, BackupDBHelper dbHelper) {
		if (context == null || dbHelper == null) {
			return false;
		}

		ContentValues values = buildDbContentValues(context);
		if (values == null) {
			return false;
		}

		return dbHelper.reflashAppTable(values);
	}

	private ContentValues buildDbContentValues(Context context) {
		AppInfo appInfo = getAppInfo();
		ContentValues cv = new ContentValues();
		cv.put(AppTable.APP_NAME, appInfo.appName);
		cv.put(AppTable.APP_PACKAGE, appInfo.packageName);
		cv.put(AppTable.APP_PATH, appInfo.sourceDir);
		cv.put(AppTable.APP_VERSION_CODE, appInfo.versionCode);
		cv.put(AppTable.APP_VERSION_NAME, appInfo.versionName);
		cv.put(AppTable.APP_CODE_SIZE, appInfo.codeSize);
		cv.put(AppTable.APP_CACHE_SIZE, appInfo.cacheSize);
		cv.put(AppTable.APP_DATA_SIZE, appInfo.dataSize);
		cv.put(AppTable.APP_TYPE, appInfo.appType);

		//		BitmapDrawable bd = (BitmapDrawable) appInfo.icon;
		//		byte[] iconBlob = bd != null ? Util.bitmapToByteArray(bd.getBitmap()) : null;
		//		cv.put(AppTable.APP_ICON, iconBlob);
		Drawable drawable = DrawableProvider.getInstance().getDrawable(context,
				DrawableProvider.buildDrawableKey(appInfo.packageName));
		if (drawable instanceof BitmapDrawable) {
			byte[] iconBlob = Util.bitmapToByteArray(((BitmapDrawable) drawable).getBitmap());
			cv.put(AppTable.APP_ICON, iconBlob);
		}
		cv.put(AppTable.APP_DATA_PATH, appInfo.dataDir);
		cv.put(AppTable.APP_APK_FILENAME, appInfo.packageName + ".apk");
		if (mDataFileExit) {
			cv.put(AppTable.APP_DATA_FILENAME, appInfo.packageName + ".tar.gz");
		}
		cv.put(AppTable.APK_MD5, appInfo.mApkMd5);
		return cv;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof AppRestoreEntry.AppRestoreArgs)) {
			return false;
		}

		mContext = context;
		mArgs = (AppRestoreArgs) data;
		mListener = listener;

		setState(RestoreState.RESTORING);
		mListener.onStart(AppRestoreEntry.this, null);

		startRestore(context, mArgs);
		return true;
	}

	private void startRestore(Context ctx, AppRestoreArgs args) {
		if (mReceiver != null) {
			if (mContext != null) {
				try {
					mContext.unregisterReceiver(mReceiver);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mReceiver = null;
		}
		mReceiver = new PackageChangeReceiver();
		try {
			ctx.registerReceiver(mReceiver, mReceiver.getIntentFilter());
		} catch (Exception e) {
			e.printStackTrace();
		}
		resotoreApp(ctx, args);
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		if (mAppInfo == null) {
			return EntryType.TYPE_UNKNOWN;
		}
		if (mAppInfo.appType == AppInfo.APP_SYSTEM) {
			return EntryType.TYPE_SYSTEM_APP;
		} else if (mAppInfo.appType == AppInfo.APP_USER || mAppInfo.appType == AppInfo.APP_PRIVATE) {
			return EntryType.TYPE_USER_APP;
		}
		return EntryType.TYPE_UNKNOWN;
	}

	@Override
	public long getSpaceUsage() {
		return getSpaceUsage(mAppRestoreType);
	}

	public long getSpaceUsage(AppRestoreType type) {
		if (mAppInfo == null) {
			return 0;
		}
		// 1.03之前的版本因代码失误，导致数据库中记录的大小为0， 此外是为了兼容旧版本，只能得到apk的大小，数据与缓存大小已经无法得知
		if (mAppInfo.codeSize <= 0) {
			mAppInfo.codeSize = getApkSize();
		}

		if (mRestorableState == RestorableState.APP_RESTORABLE) {
			return mAppInfo != null ? mAppInfo.codeSize : 0;
		} else if (mRestorableState == RestorableState.DATA_RESTORABLE) {
			return getAppDataSize();
		} else {
			if (type == null || type == AppRestoreType.APP_DATA) {
				return mAppInfo.cacheSize + mAppInfo.codeSize + mAppInfo.dataSize;
			} else if (type == AppRestoreType.APP) {
				return mAppInfo != null ? mAppInfo.codeSize : 0;
			} else if (type == AppRestoreType.DATA_ONLY) {
				return getAppDataSize();
			}
		}

		return 0;
	}

	public long getApkSize() {
		File apkFile = new File(mApkFullPath);
		return apkFile.exists() ? apkFile.length() : 0;
	}

	/**
	 * 数据库内记载的数据大小（包括缓存），非实际备份的tar文件大小，而是数据恢复到手机后实际占用的空间大小
	 * @return
	 */
	public long getAppDataSize() {
		if (mAppInfo == null) {
			return 0;
		}
		return mAppInfo.cacheSize + mAppInfo.dataSize;
	}

	@Override
	public String getDescription() {
		return mAppInfo != null ? mAppInfo.appName : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		//		if (hasIconInited()) {
		//			return true;
		//		}
		//
		//		if (context != null && mAppInfo != null) {
		//			mInitingIcon = true;
		//			Drawable icon = Util.loadIconFromAPK(context, mApkFullPath);
		//			if (icon != null) {
		//				mAppInfo.icon = icon;
		//				setIcon(mAppInfo.icon);
		//				return true;
		//			}
		//			mInitingIcon = false;
		//		}
		return false;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	/**
	 * 应用程序恢复参数
	 *
	 * @author wencan
	 */
	public static class AppRestoreArgs {
		// 备份路径
		public String mRestoreResPath;
		// 是否root
		public boolean mIsRoot;
		// App恢复类型
		public AppRestoreType mAppRestoreType = AppRestoreType.APP;
		// 是否静默恢复
		public boolean mSilentRestore;
	}

	private void finishRestore() {
		if (mContext != null && mReceiver != null) {
			try {
				mContext.unregisterReceiver(mReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mReceiver = null;
		}

		if (mHasCancel) {
			setState(RestoreState.RESTORE_CANCELED);
		} else {
			RestoreState state = mRestoreSuccess
					? RestoreState.RESTORE_SUCCESSFUL
					: RestoreState.RESTORE_ERROR_OCCURRED;
			setState(state);
		}

		if (mListener != null) {
			mListener.onEnd(mRestoreSuccess, AppRestoreEntry.this, null);
		}
		mHasCancel = false;
	}

	private void resotoreApp(Context ctx, AppRestoreArgs args) {
		// Log.d("GOBackup", "args.mIsRoot = " + args.mIsRoot);
		// 验证md5有效性
		if (!validateApkMd5(ctx)) {
			// md5不一致，文件损坏
			finishRestore();
			return;
		}

		if (args.mIsRoot/* && args.mSilentRestore */) {
			if (args.mSilentRestore) {
				silentRestoreApp(ctx, args);
				finishRestore();
			} else {
				mNeedRestoreData = mRestorableState == RestorableState.APP_DATA_RESTORABLE
						&& args.mAppRestoreType == AppRestoreType.APP_DATA;
				restoreAppUnRoot(ctx, args);
			}
		} else {
			restoreAppUnRoot(ctx, args);
		}
	}

	private boolean validateApkMd5(Context context) {
		if (mAppInfo == null) {
			return false;
		}

		if (mAppInfo.mApkMd5 == null) {
			// 旧版本没有保存md5，默认true
			return true;
		}

		String oldMd5 = mAppInfo.mApkMd5;
		String newMd5 = MD5Util.getFileMd5Code(new File(mApkFullPath));
		boolean ret = oldMd5.equals(newMd5);
		if (!ret) {
			// 写入日记到文件
			StringBuilder sb = new StringBuilder();
			sb.append("packageName = ");
			sb.append(mAppInfo.packageName);
			sb.append("\n");
			sb.append("oldMd5 = ");
			sb.append(oldMd5);
			sb.append("\n");
			sb.append("newMd5 = ");
			sb.append(newMd5);
			sb.append("\n");
			Logger.e("AppRestoreEntry", "error_msg:\n" + sb.toString());
			Logger.flush();
		}
		return ret;
	}

	/**
	 * 静默恢复app，适用root
	 *
	 * @param ctx
	 * @param args
	 * @param listener
	 */
	private void silentRestoreApp(Context ctx, AppRestoreArgs args) {
		// long dt = System.currentTimeMillis();
		// 静默安装，不允许取消
		mHasCancel = false;

		if (mRestorableState == RestorableState.DATA_CORRUPTION) {
			mRestoreSuccess = false;
			return;
		}

		//		if (!restoreApp) {
		//			mRestoreSuccess = false;
		//			return;
		//		}

		//		String packageName = mAppInfo.packageName;
		//		String rootDir = args.mRestoreResPath;
		//		if (!rootDir.endsWith(File.separator)) {
		//			rootDir += File.separator;
		//		}
		//		// 判断文件是否存在
		//		String apkFilePath = rootDir + packageName + ".apk";
		//		String appDataPath = rootDir + packageName + ".tar.gz";

		String apkFilePath = mApkFullPath;
		String appDataPath = mAppDataFullPath;

		// 解决2.1机器通过pm
		// install命令安装在sd卡的apk文件an失败问题，解决方法是，将apk文件拷贝的/data/local/tmp目录
		if (Util.getAndroidSystemVersion() <= 7) {
			String internalCacheDirPath = ctx.getCacheDir().getAbsolutePath();
			internalCacheDirPath = Util.ensureFileSeparator(internalCacheDirPath);
			String tempApkPath = internalCacheDirPath + "temp.apk";
			if (!Util.copyFile(apkFilePath, tempApkPath)) {
				mRestoreSuccess = false;
				return;
			}
			// 改变可读
			LinuxShell ls = new LinuxShell();
			ls.chmod("755", new String[] { tempApkPath }, false);
			ls = null;
			apkFilePath = tempApkPath;
		}

		mRestoreSuccess = restoreAppWithRootProcess(ctx, args.mAppRestoreType, mAppInfo,
				apkFilePath, appDataPath);

		if (Util.getAndroidSystemVersion() <= 7) {
			// 删除缓存apk文件
			File tempFile = new File(apkFilePath);
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
		// Log.d("GOBackup", "AppRestoreEntry : silentRestoreApp : dt = " +
		// (System.currentTimeMillis() - dt));
	}

	private boolean restoreAppWithRootProcess(Context context, AppRestoreType restoreType,
			AppInfo appInfo, String apkFileFullPath, String dataFileFullPath) {
		boolean ret = false;
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		boolean restoreApp = (mRestorableState == RestorableState.APP_DATA_RESTORABLE || mRestorableState == RestorableState.APP_RESTORABLE)
				&& (restoreType == AppRestoreType.APP || restoreType == AppRestoreType.APP_DATA);
		boolean restoreData = (mRestorableState == RestorableState.APP_DATA_RESTORABLE || mRestorableState == RestorableState.DATA_RESTORABLE)
				&& (restoreType == AppRestoreType.APP_DATA || restoreType == AppRestoreType.DATA_ONLY);

		if (restoreApp) {
			String cmd = buildRestoreCommand(context, appInfo, AppRestoreType.APP,
					apkFileFullPath, dataFileFullPath);
			if (!TextUtils.isEmpty(cmd)) {
				String result = new RootShell.Command(cmd).execute(rootShell, 60 * 1000);
				if (result != null) {
					ret = result.contains("GO_SUCCESS");
				}
				if (!ret) {
					// 失败，写入debug文件
					Logger.e("AppRestoreEntry", "command = " + cmd + ", result = " + result);
					Logger.flush();
					return false;
				}
			}
		}

		if (restoreData) {
			String cmd = buildRestoreCommand(context, appInfo, AppRestoreType.DATA_ONLY,
					apkFileFullPath, dataFileFullPath);
			if (!TextUtils.isEmpty(cmd)) {
				String result = new RootShell.Command(cmd).execute(rootShell, 60 * 1000);
				if (result != null) {
					ret = result.contains("GO_SUCCESS");
				}
				if (!ret) {
					// 失败，写入debug文件
					Logger.e("AppRestoreEntry", "command = " + cmd + ", result = " + result);
					Logger.flush();
					return false;
				}
			}
		}
		return ret;
	}

	private String buildRestoreCommand(Context context, AppInfo appInfo,
			AppRestoreType restoreType, String apkFilePath, String dataFilePath) {
		if (restoreType != AppRestoreType.APP && restoreType != AppRestoreType.DATA_ONLY) {
			return null;
		}
		String internalDataFilePath = Util.ensureFileSeparator(context.getFilesDir()
				.getAbsolutePath());
		String command = "";
		command += internalDataFilePath + "backup ra ";
		if (restoreType == AppRestoreType.APP) {
			command += "-a " + apkFilePath + " ";
			boolean sdk14 = Build.VERSION.SDK_INT >= 14;
			if (sdk14) {
				command += "-D LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH") + " ";
			}
			if (appInfo.isPrivateApp()) {
				command += "-l ";
			}
		} else if (restoreType == AppRestoreType.DATA_ONLY) {
			final String packageName = appInfo.packageName;
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = null;
			try {
				pi = pm.getPackageInfo(packageName, 0);
			} catch (NameNotFoundException e) {
				return null;
			}
			if (pi == null) {
				return null;
			}
			String appDataDir = pi.applicationInfo.dataDir;
			command += "-i " + appDataDir + " -d " + dataFilePath + " ";
		}
		command += internalDataFilePath + "busybox";
		return command;
	}

	/**
	 * 静默恢复数据
	 *
	 * @param args
	 * @return
	 */
	private boolean silentRestoreData(Context ctx, final AppRestoreArgs args) {
		if (!mDataFileExit) {
			return false;
		}

		String packageName = mAppInfo.packageName;
		String rootDir = args.mRestoreResPath;
		if (!rootDir.endsWith(File.separator)) {
			rootDir += File.separator;
		}
		String appDataPath = rootDir + packageName + ".tar.gz";
		return restoreAppWithRootProcess(ctx, AppRestoreType.DATA_ONLY, mAppInfo, mApkFullPath,
				mAppDataFullPath);
		// return new
		// LinuxShell().restoreApp(ctx.getFilesDir().getAbsolutePath(), null,
		// appDataPath, Util.getApplicationDataDirPath(ctx, packageName),
		// false);
	}

	/**
	 * 非root恢复应用
	 *
	 * @param ctx
	 * @param args
	 */
	private void restoreAppUnRoot(Context ctx, AppRestoreArgs args) {
		if (!mApkFileExit) {
			mRestoreSuccess = false;
			finishRestore();
		}
		String packageName = mAppInfo.packageName;
		String rootDir = args.mRestoreResPath;
		if (!rootDir.endsWith(File.separator)) {
			rootDir += File.separator;
		}
		String apkFilePath = rootDir + packageName + ".apk";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(apkFilePath)),
				"application/vnd.android.package-archive");
		if (ctx != null && ctx instanceof Activity) {
			((Activity) ctx).startActivityForResult(intent, REQUEST_INSTALL_APP);
		}
	}

	/**
	 * packagechangereceiver
	 *
	 * @author wencan
	 */
	class PackageChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String packageName = intent.getDataString();
			if (packageName != null) {
				String packagePrex = "package:";
				if (packageName.startsWith(packagePrex)) {
					packageName = packageName.substring(packagePrex.length());
				}
			}

			String action = intent.getAction();
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				String curPackageName = mAppInfo.packageName;
				if (curPackageName.equals(packageName)) {
					mHasCancel = false;
				}
			}
		}

		public IntentFilter getIntentFilter() {
			IntentFilter intetnFilter = new IntentFilter();
			intetnFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			intetnFilter.addDataScheme("package");
			return intetnFilter;
		}
	}

	@Override
	public boolean handleMessage(int arg1, int arg2, Object obj) {
		if (arg1 == AppRestoreEntry.REQUEST_INSTALL_APP) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (!mHasCancel) {
						mRestoreSuccess = true;
					}
					if (mRestoreSuccess && mNeedRestoreData) {
						mRestoreSuccess = silentRestoreData(mContext, mArgs);
					}

					try {
						Thread.sleep(1500);
					} catch (Exception e) {
					}

					finishRestore();
				}
			}).start();
			return true;
		}
		return false;
	}

	@Override
	public void stopRestore() {

	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AppRestoreEntry)) {
			return false;
		}
		AppRestoreEntry entry = (AppRestoreEntry) o;
		return mAppInfo.packageName.equals(entry.mAppInfo.packageName);
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return getIcon(context, null, listener);
	}

	public Drawable getIcon(Context context, byte[] iconData, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(mAppInfo.packageName, iconData,
				new File(mApkFullPath));
		final Drawable defaultDrawable = DrawableProvider.getDefaultActivityIcon(context);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}

	public void setAppRestoreType(AppRestoreType type) {
		mAppRestoreType = type;
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		String appName = ((AppRestoreEntry) entry).getAppInfo().appName;
		String packageName = ((AppRestoreEntry) entry).getAppInfo().packageName;
		backupDBHelper.delete(AppTable.TABLE_NAME, AppTable.APP_NAME + "=?" + " AND "
				+ AppTable.APP_PACKAGE + "=?", new String[] { appName, packageName });
		File apkFile = new File(recordRootPah, packageName + ".apk");
		File dataFile = new File(recordRootPah, packageName + ".tar.gz");
		if (apkFile.exists()) {
			apkFile.delete();
		}
		if (dataFile.exists()) {
			dataFile.delete();
		}
	}
}
