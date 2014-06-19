package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.lang.reflect.Method;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;

import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.AppTable;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Logger;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author kevin 应用程序备份条目
 */
public class AppBackupEntry extends BaseBackupEntry implements AppEntry {
	private static final String LOG_TAG = "AppBackupEntry";

	private Context mContext;
	private final AppInfo mAppInfo;
	private boolean mHasInitSize = false;
	private long mApkFileSize;

	private AppBackupType mAppBackupType = AppBackupType.APK_DATA;

	/**
	 * 应用程序备份类型
	 *
	 * @author wencan
	 */
	public enum AppBackupType {
		APK, APK_DATA, DATA_ONLY
	}

	/**
	 * 构造函数
	 *
	 * @param appInfo
	 *            Appinfo对象
	 */
	public AppBackupEntry(Context context, AppInfo appInfo) {
		mContext = context.getApplicationContext();
		mAppInfo = appInfo;
		mApkFileSize = new File(mAppInfo.sourceDir).length();
	}

	@Override
	public AppInfo getAppInfo() {
		return mAppInfo;
	}

	public AppBackupType getAppBackupType() {
		return mAppBackupType;
	}

	public void setAppBackupType(AppBackupType appBackupType) {
		mAppBackupType = appBackupType;
	}

	public boolean hasInitSizeFinish() {
		return mHasInitSize;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof AppBackupEntry.AppBackArgs)) {
			return false;
		}
		setState(BackupState.BACKUPING);
		AppBackArgs args = (AppBackArgs) data;
		setAppBackupType(args.mAppBackupType);
		backupApp(ctx, args, listener);
		return true;
	}

	public boolean isProtectedApp() {
		return mAppInfo == null ? true : mAppInfo.isPrivateApp();
	}

	@Override
	public int getId() {
		return mAppInfo == null ? -1 : mAppInfo.packageName.hashCode();
	}

	@Override
	public EntryType getType() {
		return mAppInfo == null ? EntryType.TYPE_UNKNOWN : mAppInfo.isSystemApp()
				? EntryType.TYPE_SYSTEM_APP
				: EntryType.TYPE_USER_APP;
	}

	@Override
	public long getSpaceUsage() {
		return getSpaceUsage(mAppBackupType);
	}

	public long getSpaceUsage(AppBackupType backupType) {
		if (!mHasInitSize) {
			final byte[] lock = new byte[0];
			PackageManager pm = mContext.getPackageManager();
			try {
				Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,
						IPackageStatsObserver.class);
				getPackageSizeInfo.invoke(pm, mAppInfo.packageName, new IPackageStatsObserver.Stub() {
					@Override
					public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
//						LogUtil.d("onGetStatsCompleted " + pStats.packageName + ", succeeded = " + succeeded);
						if (succeeded && pStats != null) {
							final int androidVersion = Util.getAndroidSystemVersion();
							mAppInfo.codeSize = pStats.codeSize
									+ (androidVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
									? pStats.externalCodeSize
											: 0);
							mAppInfo.dataSize = pStats.dataSize;
							mAppInfo.cacheSize = pStats.cacheSize;
							synchronized (lock) {
								mHasInitSize = true;
								lock.notify();
//								LogUtil.d("onGetStatsCompleted " + pStats.packageName + " notify");
							}
						}
					}
				});
				synchronized (lock) {
					try {
						while (!mHasInitSize) {
							lock.wait();
							//	LogUtil.d("onGetStatsCompleted"  + mAppInfo.packageName + " wait");
						}
					} catch (InterruptedException e) {
						//	LogUtil.d("wait exception");
					};
				}
			} catch (Exception e) {
				LogUtil.d("getAppSizeInfo : ERROR : " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		// codeSize在3.0以下的系统而且移除到SD卡的应用程序不准确，应直接使用APK包的大小
		if (backupType == null || backupType == AppBackupType.APK_DATA) {
			return mApkFileSize + mAppInfo.dataSize + mAppInfo.cacheSize;
		} else if (backupType == AppBackupType.APK) {
			return mApkFileSize;
		} else if (backupType == AppBackupType.DATA_ONLY) {
			return getAppDataSize();
		}
		return 0;
	}
	
	@Override
	public String getDescription() {
		return mAppInfo != null ? (mAppInfo.isApplicationNameValid()
				? mAppInfo.appName
				: mAppInfo.packageName) : "";
	}

	public boolean hasAppLabelInited() {
		return mAppInfo != null && mAppInfo.isApplicationNameValid();
	}
	
	
	public long getAppDataSize() {
		return getDataSize() + getCacheSize();
	}
	
	public long getApkFileSize() {
		return mApkFileSize;
	}

	public long getCodeSize() {
		return mAppInfo == null ? 0 : mAppInfo.codeSize;
	}

	public long getDataSize() {
		return mAppInfo == null ? 0 : mAppInfo.dataSize;
	}

	public long getCacheSize() {
		return mAppInfo == null ? 0 : mAppInfo.cacheSize;
	}

	@Override
	public boolean loadIcon(Context context) {
//		if (hasIconInited()) {
//			return true;
//		}
//		if (context != null && mAppInfo != null) {
//			mInitingIcon = true;
//			if (mAppInfo.loadIcon(context.getPackageManager())) {
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
	 * 应用程序备份参数
	 *
	 * @author wencan
	 */
	public static class AppBackArgs extends BackupArgs {
		public AppBackupType mAppBackupType = AppBackupType.APK_DATA;
	}

	private void backupApp(Context ctx, AppBackArgs args, IAsyncTaskListener listener) {
		if (args.mIsRoot) {
			backupAppRoot(ctx, args, listener);
		} else {
			backupAppUnRoot(ctx, args, listener);
		}
	}

	private void backupAppUnRoot(Context ctx, AppBackArgs args, IAsyncTaskListener listener) {
		boolean ret = false;
		try {
			listener.onStart(null, null);

			// 拷贝apk文件
			AppInfo appInfo = getAppInfo();
			String sourceDir = appInfo.sourceDir;
			String backupRootDir = args.mBackupPath;
			if (!backupRootDir.endsWith(File.separator)) {
				backupRootDir += File.separator;
			}
			String apkPathAndName = backupRootDir + appInfo.packageName + ".apk";
			ret = Util.copyFile(sourceDir, apkPathAndName);
			
			if (ret) {
				// 备份成功,写入数据库
				ret = updateDb(ctx, args.mDbHelper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			BackupState state = ret
					? BackupState.BACKUP_SUCCESSFUL
					: BackupState.BACKUP_ERROR_OCCURRED;
			setState(state);
			listener.onEnd(ret, this, getAppBackupFile(args.mBackupPath));
		}
	}

	private String[] getAppBackupFile(String dir) {
		if (dir == null) {
			return null;
		}

		String packageName = getAppInfo().packageName;
		File apkFile = new File(dir, packageName + ".apk");
		File dataFile = new File(dir, packageName + ".tar.gz");
		if (apkFile.exists() && dataFile.exists()) {
			return new String[] { apkFile.getAbsolutePath(), dataFile.getAbsolutePath() };
		} else if (apkFile.exists()) {
			return new String[] { apkFile.getAbsolutePath() };
		} else if (dataFile.exists()) {
			return new String[] { dataFile.getAbsolutePath() };
		}
		return null;
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
		cv.put(AppTable.APP_DATA_PATH, appInfo.dataDir);
		cv.put(AppTable.APP_PACKAGE, appInfo.packageName);
		cv.put(AppTable.APP_PATH, appInfo.sourceDir);
		cv.put(AppTable.APP_VERSION_CODE, appInfo.versionCode);
		cv.put(AppTable.APP_VERSION_NAME, appInfo.versionName);
		if (getSpaceUsage() > 0) {
			cv.put(AppTable.APP_CODE_SIZE, appInfo.codeSize);
			cv.put(AppTable.APP_CACHE_SIZE, appInfo.cacheSize);
			cv.put(AppTable.APP_DATA_SIZE, appInfo.dataSize);
		}
		cv.put(AppTable.APP_TYPE, appInfo.appType);

		Drawable drawable = DrawableProvider.getInstance().getDrawable(context,
				DrawableProvider.buildDrawableKey(appInfo.packageName));
		if (drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			if (bitmap != null) {
				// 限制应用程序图标大小为72 * 72
				final int maxIconSize = 72;
				byte[] iconBlob = scaleIconAndConvertToByteArray(bitmap, maxIconSize);
				cv.put(AppTable.APP_ICON, iconBlob);
			}
		}
		
		if (hasBackupedData()) {
			cv.put(AppTable.APP_DATA_FILENAME, getAppDataFileName());
		}
		
		if (hasBackupedApk()) {
			cv.put(AppTable.APP_APK_FILENAME, getAppApkFileName());
			// 获取apk的md5
			if (appInfo.mApkMd5 == null) {
				appInfo.mApkMd5 = MD5Util.getFileMd5Code(new File(appInfo.sourceDir));
			}
			cv.put(AppTable.APK_MD5, appInfo.mApkMd5);
		}
		return cv;
	}
	
	private byte[] scaleIconAndConvertToByteArray(Bitmap source, int maxSize) {
		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();
		if (srcWidth <= maxSize && srcHeight <= maxSize) {
			return Util.bitmapToByteArray(source);
		}
		double scaleFactor = maxSize * 1.0 / Math.max(srcWidth, srcHeight);
		int destWidth = (int) Math.floor(srcWidth * scaleFactor);
		int destHeight = (int) Math.floor(srcHeight * scaleFactor);
		Bitmap scaledBitmap = null;
		try {
			scaledBitmap = Bitmap.createScaledBitmap(source, destWidth, destHeight, false);
		} catch (OutOfMemoryError e) {
			scaledBitmap = source;
		}
		return Util.bitmapToByteArray(scaledBitmap);
	}

	public String getAppApkFileName() {
		return mAppInfo == null ? null : mAppInfo.packageName + ".apk";
	}

	public String getAppDataFileName() {
		return mAppInfo == null ? null : mAppInfo.packageName + ".tar.gz";
	}

	public boolean hasBackupedData() {
		return mAppBackupType == AppBackupType.APK_DATA
				|| mAppBackupType == AppBackupType.DATA_ONLY;
	}
	
	private boolean hasBackupedApk() {
		return mAppBackupType != AppBackupType.DATA_ONLY;
	}
	
	private void backupAppRoot(Context ctx, AppBackArgs args, IAsyncTaskListener listener) {
		boolean ret = false;
		try {
			listener.onStart(null, null);

			String backupRootDir = Util.ensureFileSeparator(args.mBackupPath);
			AppInfo appInfo = getAppInfo();
//			String sourceDir = appInfo.sourceDir;
//			String dataSorecePath = appInfo.dataDir;
//			Util.killRunningProcess(ctx, appInfo.packageName);
//			boolean needBackupData = args.mAppBackupType == AppBackupType.APK_DATA;
//
//			ret = backupAppWithRootProcess(ctx, ctx.getFilesDir().getAbsolutePath(), sourceDir,
//					dataSorecePath, backupRootDir, appInfo.packageName, needBackupData);
			ret = backupAppWithRootProcess(ctx, appInfo, args.mAppBackupType, backupRootDir);
			
			// 备份成功,写入数据库
			if (ret) {
				ret = updateDb(ctx, args.mDbHelper);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			BackupState state = ret
					? BackupState.BACKUP_SUCCESSFUL
					: BackupState.BACKUP_ERROR_OCCURRED;
			setState(state);
			listener.onEnd(ret, this, getAppBackupFile(args.mBackupPath));
		}
	}
	
	private boolean backupAppWithRootProcess(Context context, AppInfo appInfo, AppBackupType backupType, String destPath) {
		boolean ret = false;
		String internalDataFilePath = context.getFilesDir().getAbsolutePath();
		final String apkFilePath = appInfo.sourceDir;
		final String dataPath = appInfo.dataDir;
		final String packageName = appInfo.packageName;
		
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		Util.killRunningProcess(context, packageName);
		
		String command = "";
		internalDataFilePath = Util.ensureFileSeparator(internalDataFilePath);
		command += internalDataFilePath + "backup ba ";
		
		if (backupType == AppBackupType.APK || backupType == AppBackupType.APK_DATA) {
			command += "-a " + apkFilePath + " ";
		}
		
		if (backupType == AppBackupType.APK_DATA || backupType == AppBackupType.DATA_ONLY) {
			command += "-d " + dataPath + " ";
		}
		command += internalDataFilePath + "busybox ";
		command += destPath + " ";
		command += packageName;
		String result = new RootShell.Command(command).execute(rootShell, 2 * 60 * 1000);
		if (result != null) {
			ret = result.contains("GO_SUCCESS");
		}
		
		// DEBUG
		if (!ret) {
			// 失败，写入debug文件
			Logger.e("AppBackupEntry", "command = " + command + ", result = " + result);
			Logger.flush();
		}
		return ret;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(mAppInfo.packageName);
		final Drawable defaultDrawable = DrawableProvider.getDefaultActivityIcon(context);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}
}
