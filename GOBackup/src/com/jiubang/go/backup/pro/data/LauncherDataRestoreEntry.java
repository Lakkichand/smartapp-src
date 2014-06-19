package com.jiubang.go.backup.pro.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.AppWidgetHelper;
import com.jiubang.go.backup.pro.util.AppWidgetHelper.AppWidgetConfig;
import com.jiubang.go.backup.pro.util.PackageUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 桌面数据恢复项
 * 
 * @author wencan
 */
public class LauncherDataRestoreEntry extends BaseRestoreEntry {
	private Context mContext;
	// private List<LauncherDataExtraInfo> mLauncherDataExtraInfo;
	LauncherDataExtraInfo mLauncherDataExtraInfo;
	private boolean mNeedReboot = false;
	private String mRecordDir;

	public LauncherDataRestoreEntry(Context context, String recordDir,
			LauncherDataExtraInfo launcherDataExtraInfo/*
														 * List<
														 * LauncherDataExtraInfo
														 * >
														 * launcherDataExtraInfo
														 */) {
		mContext = context;
		mLauncherDataExtraInfo = launcherDataExtraInfo;
		mRecordDir = recordDir;

		if (isLauncherDataBackupRestorable(mContext, mRecordDir)) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}
		setState(RestoreState.RESTORING);
		listener.onStart(null, null);
		boolean ret = false;
		ret = restoreInternal(context, (RestoreArgs) data, listener);
		RestoreState state = ret
				? RestoreState.RESTORE_SUCCESSFUL
				: RestoreState.RESTORE_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, LauncherDataRestoreEntry.this, null);
		return true;
	}

	@Override
	public void stopRestore() {
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_SYSTEM_LAUNCHER_DATA;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.launcher_layout) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return true;
	}

	public static LauncherDataRestoreEntry generateLauncherDataBackupEntry(Context context,
			BackupDBHelper dbHelper, String rootDir) {
		if (context == null || dbHelper == null || rootDir == null) {
			return null;
		}

		LauncherDataRestoreEntry entry = null;

		// 判断appwidget文件存不存在
		File appWidgetFile = new File(rootDir, LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
		if (!appWidgetFile.exists()) {
			return null;
		}

		String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA;
		Cursor cursor = dbHelper.query(DataTable.TABLE_NAME, null, where, null, null);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		LauncherDataExtraInfo launcherDataExtraInfo = null;
		PackageManager pm = context.getPackageManager();
		// List<ResolveInfo> resolveInfos =
		// PackageUtil.getAppWithHomeAction(context);
		try {
			launcherDataExtraInfo = new LauncherDataExtraInfo();
			launcherDataExtraInfo.packageName = cursor.getString(cursor
					.getColumnIndex(DataTable.DATA1));
			launcherDataExtraInfo.appName = cursor
					.getString(cursor.getColumnIndex(DataTable.DATA2));
			launcherDataExtraInfo.dataDir = cursor
					.getString(cursor.getColumnIndex(DataTable.DATA3));
			launcherDataExtraInfo.versionCode = cursor.getString(cursor
					.getColumnIndex(DataTable.DATA4));
			launcherDataExtraInfo.versionName = cursor.getString(cursor
					.getColumnIndex(DataTable.DATA5));
			launcherDataExtraInfo.buildDisplay = cursor.getString(cursor
					.getColumnIndex(DataTable.DATA6));
			launcherDataExtraInfo.buildVersionSdkInt = cursor.getString(cursor
					.getColumnIndex(DataTable.DATA7));
			launcherDataExtraInfo.buildId = cursor
					.getString(cursor.getColumnIndex(DataTable.DATA8));
			launcherDataExtraInfo.buildModel = cursor.getString(cursor
					.getColumnIndex(DataTable.DATA9));
			/*
			 * if(!PackageUtil.isSystemAndLauncherApp(resolveInfos,
			 * info.packageName)){ //不是现在的system桌面程序 continue; }
			 * if(!PackageUtil.isAppWithSameVersionCode(pm, info.packageName,
			 * Integer.valueOf(info.versionCode))){ continue; }
			 */

			File file = new File(rootDir, launcherDataExtraInfo.packageName + ".tar.gz");
			if (!file.exists()) {
				// 备份文件不存在
				launcherDataExtraInfo = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			launcherDataExtraInfo = null;
		}
		if (launcherDataExtraInfo != null) {
			entry = new LauncherDataRestoreEntry(context, rootDir, launcherDataExtraInfo);
		}

		cursor.close();
		return entry;
	}

	public static boolean hasLauncherDataBackupFile(String recordDir) {
		File files = getLauncherDataBackupFile(recordDir);
		if (files == null) {
			return false;
		}
		return true;
	}

	public static File getLauncherDataBackupFile(String recordDir) {
		if (recordDir == null) {
			return null;
		}

		// 兼容V2.0以前版本
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				if (name.endsWith(LauncherDataBackupEntry.LAUNCHER_DATA_SUFFIXN)) {
					return true;
				}
				return false;
			}
		};
		File file = new File(recordDir);
		File[] allSubFiles = file.listFiles(filter);
		if (allSubFiles != null && allSubFiles.length > 0) {
			return allSubFiles[0];
		}

		// V2.0版本
		File launcherDataFile = new File(recordDir, LauncherDataBackupEntry.LAUNCHER_DATA_FILE_NAME);
		if (launcherDataFile.exists()) {
			return launcherDataFile;
		}
		return null;
	}

	public static boolean isLauncherDataBackupRestorable(Context context, String recordDir) {
		if (!RootShell.isRootValid()) {
			return false;
		}

		File launcherDataFile = getLauncherDataBackupFile(recordDir);
		if (launcherDataFile == null) {
			return false;
		}

		String fileName = null;
		String packageName = null;
		int versionCode = 0;

		// 兼容V2.0以前版本
		if (launcherDataFile.getName().contains(LauncherDataBackupEntry.LAUNCHER_DATA_SUFFIXN)) {
			try {
				fileName = launcherDataFile.getName().replace(
						LauncherDataBackupEntry.LAUNCHER_DATA_SUFFIXN, "");
				int index = fileName.indexOf("_");
				if (index == -1) {
					packageName = fileName.substring(0, fileName.length());
				} else {
					packageName = fileName.substring(0, fileName.indexOf("_"));
					versionCode = Integer.valueOf(fileName.substring(fileName.indexOf("_") + 1));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (launcherDataFile.getName().equals(
				LauncherDataBackupEntry.LAUNCHER_DATA_FILE_NAME)) {
			FileInputStream fis = null;
			DataInputStream dis = null;
			try {
				fis = new FileInputStream(launcherDataFile);
				dis = new DataInputStream(fis);
				packageName = dis.readUTF();
				versionCode = dis.readInt();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
						fis = null;
					}
					if (dis != null) {
						dis.close();
						dis = null;
					}
				} catch (Exception e) {
				}
			}
		}

		if (packageName == null || versionCode == 0) {
			return false;
		}

		List<ResolveInfo> resolveInfos = PackageUtil.getAppWithHomeAction(context);
		if (PackageUtil.isSystemAndLauncherApp(resolveInfos, packageName)
				&& PackageUtil.isAppWithSameVersionCode(context, packageName, versionCode)) {
			return true;
		}
		return false;
	}

	public static String getLauncherDataPackageName(String rootDir) {
		if (rootDir == null) {
			return null;
		}

		File launcherDataFile = getLauncherDataBackupFile(rootDir);
		if (launcherDataFile == null) {
			return null;
		}

		String packageName = null;

		// 兼容V2.0以前版本
		if (launcherDataFile.getName().contains(LauncherDataBackupEntry.LAUNCHER_DATA_SUFFIXN)) {
			try {
				String fileName = launcherDataFile.getName().replace(
						LauncherDataBackupEntry.LAUNCHER_DATA_SUFFIXN, "");
				int index = fileName.indexOf("_");
				if (index == -1) {
					// 最早的版本没有写版本号字段，直接保存包名
					index = fileName.length();
				}
				packageName = fileName.substring(0, index);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (launcherDataFile.getName().equals(
				LauncherDataBackupEntry.LAUNCHER_DATA_FILE_NAME)) {
			FileInputStream fis = null;
			DataInputStream dis = null;
			try {
				fis = new FileInputStream(launcherDataFile);
				dis = new DataInputStream(fis);
				packageName = dis.readUTF();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
						fis = null;
					}
					if (dis != null) {
						dis.close();
						dis = null;
					}
				} catch (Exception e) {
				}
			}
		}

		return packageName;
	}

	private boolean restoreInternal(Context context, RestoreArgs args, IAsyncTaskListener listener) {
		if (mLauncherDataExtraInfo == null) {
			return false;
		}
		final float m05f = 0.5f;
		final float m09f = 0.9f;
		// 首先重构appwidget.xml文件 //TODO有可能系统没有重新加载文件初始化widget，会导致失败，待验证
		if (!rebuildAppWidgetFile(context, args.mRestorePath)) {
			return false;
		}

		if (listener != null) {
			listener.onProceeding(m05f, this, null, null);
		}

		// 恢复桌面数据
		boolean ret = true;
		String rootPath = Util.ensureFileSeparator(args.mRestorePath);
		String internalDataPath = Util.ensureFileSeparator(context.getFilesDir().getAbsolutePath());
		// for(LauncherDataExtraInfo item : mLauncherDataExtraInfo){
		String dataFilePath = rootPath + mLauncherDataExtraInfo.packageName + ".tar.gz";
		File file = new File(dataFilePath);
		if (!file.exists()) {
			return false;
		}

		if (!restoreWithRootProcess(context, internalDataPath, dataFilePath,
				Util.getApplicationDataDirPath(context, mLauncherDataExtraInfo.packageName))) {
			return false;
		}

		// 如果不需要重启，则恢复完成后重启桌面程序
		if (!mNeedReboot) {
			// 重启
			Util.forceKillProcess(context, mLauncherDataExtraInfo.packageName);
		}
		// }

		// if(listener != null){
		// listener.onProceeding(0.6f, this, null, null);
		// }

		// if(ret){
		// //恢复壁纸
		// restoreWallpaper(context, args);
		// }

		if (listener != null) {
			listener.onProceeding(m09f, this, null, null);
		}

		return ret;
	}

	private boolean rebuildAppWidgetFile(Context context, String rootDir) {
		if (context == null || rootDir == null) {
			return false;
		}
		rootDir = Util.ensureFileSeparator(rootDir);
		String internalDataDir = Util.ensureFileSeparator(context.getFilesDir().getAbsolutePath());

		// 拷贝系统的appwidget文件
		String curUsingAppWidgetTempFileName = rootDir + "temp_appwidget.xml";
		RootShell rp = null;
		try {
			rp = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!Util.copyFileWithRootProcess(context, rp,
				LauncherDataBackupEntry.APP_WIDGET_FILE_PATH, curUsingAppWidgetTempFileName)) {
			return false;
		}

		File curUsingAppWidgetTempFile = new File(curUsingAppWidgetTempFileName);
		if (!curUsingAppWidgetTempFile.exists()) {
			return false;
		}

		File backupAppWidgetFile = new File(rootDir, LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
		if (!backupAppWidgetFile.exists()) {
			return false;
		}

		boolean ret = false;
		// 合并两个文件
		boolean dataChange = false;
		FileInputStream streamCur = null;
		FileInputStream streamBackup = null;
		AppWidgetConfig curUsingConfig = null;
		AppWidgetConfig backupConfig = null;
		try {
			streamCur = new FileInputStream(curUsingAppWidgetTempFile);
			streamBackup = new FileInputStream(backupAppWidgetFile);
			curUsingConfig = AppWidgetHelper.readAppWidgetStateFromFile(context, streamCur);
			backupConfig = AppWidgetHelper.readAppWidgetStateFromFile(context, streamBackup);
			dataChange = AppWidgetHelper
					.mergeAppWidgetConfig(context, curUsingConfig, backupConfig);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (streamCur != null) {
					streamCur.close();
				}
				if (streamBackup != null) {
					streamBackup.close();
				}
			} catch (Exception e) {
			}
		}

		if (curUsingConfig != null) {
			FileOutputStream fos = null;
			curUsingAppWidgetTempFile = new File(curUsingAppWidgetTempFileName);
			try {
				fos = new FileOutputStream(curUsingAppWidgetTempFile);
				AppWidgetHelper.writeAppWidgetStateToFile(fos, curUsingConfig);
				ret = true;
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (Exception e) {

				}
			}
		}
		if (curUsingConfig != null) {
			AppWidgetHelper.releaseAppWidgetConfig(curUsingConfig);
			curUsingConfig = null;
		}
		if (backupConfig != null) {
			AppWidgetHelper.releaseAppWidgetConfig(backupConfig);
			backupConfig = null;
		}

		if (!ret) {
			return false;
		}

		if (curUsingAppWidgetTempFile != null) {
			ret = Util.copyFileWithRootProcess(context, rp,
					curUsingAppWidgetTempFile.getAbsolutePath(),
					LauncherDataBackupEntry.APP_WIDGET_FILE_PATH);
		}
		if (!ret) {
			// 拷贝失败
			if (curUsingAppWidgetTempFile.exists()) {
				curUsingAppWidgetTempFile.delete();
				return false;
			}
		}

		// 修改文件属性
		String command = "";
		command += internalDataDir + "busybox chmod 600 "
				+ LauncherDataBackupEntry.APP_WIDGET_FILE_PATH + ";";
		command += "echo chmod_result=$?;";
		command += internalDataDir + "busybox chown 1000:1000 "
				+ LauncherDataBackupEntry.APP_WIDGET_FILE_PATH + ";";
		command += "echo chown_result=$?;";
		command += internalDataDir + "busybox chgrp 1000 "
				+ LauncherDataBackupEntry.APP_WIDGET_FILE_PATH + ";";
		command += "echo chgrp_result=$?";
		Log.d("GOBackup", "cmd = " + command);
		String result = new RootShell.Command(command).execute(rp);
		/*
		 * if(result != null){ Log.d("GOBackup", "result = " + result); ret =
		 * (result.contains("chmod_result=0") &&
		 * result.contains("chown_result=0") &&
		 * result.contains("chgrp_result=0")) ? true : false; }
		 */

		if (ret && dataChange) {
			// 如果文件修改成功并且数据变化，则需要重启更新widget
			mNeedReboot = true;
		}

		if (curUsingAppWidgetTempFile != null) {
			curUsingAppWidgetTempFile.delete();
		}
		return ret;
	}

	private boolean restoreWithRootProcess(Context context, String internalDataPath,
			String dataFilePath, String appDataPath) {
		boolean ret = false;
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		internalDataPath = Util.ensureFileSeparator(internalDataPath);
		String command = "";
		command += internalDataPath + "backup ra ";

		if (dataFilePath != null && appDataPath != null) {
			command += "-i " + appDataPath + " -d " + dataFilePath + " ";
		}
		command += internalDataPath + "busybox";
		String result = new RootShell.Command(command).execute(rootShell);
		if (result != null) {
			Log.d("GOBackup", "result = " + result);
			ret = result.contains("GO_SUCCESS") ? true : false;
		}
		return ret;
	}

	private boolean restoreWallpaper(Context context, RestoreArgs args) {
		File file = new File(args.mRestorePath, LauncherDataBackupEntry.WALLPAPER_FILE_NAME);
		if (!file.exists()) {
			return false;
		}

		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeFile(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (bm == null) {
			return false;
		}

		boolean ret = true;
		WallpaperManager wm = WallpaperManager.getInstance(context);
		try {
			wm.setBitmap(bm);
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_launcher_data);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public boolean isNeedReboot() {
		return mNeedReboot;
	}

	public LauncherDataExtraInfo getLauncherDataExtraInfo() {
		return mLauncherDataExtraInfo;
	}

	/**
	 * 桌面数据信息
	 * 
	 * @author wencan
	 */
	public static class LauncherDataExtraInfo implements Parcelable {
		public String packageName;
		public String appName;
		public String dataDir;
		public String versionCode;
		public String versionName;
		public String buildDisplay;
		public String buildVersionSdkInt;
		public String buildId;
		public String buildModel;

		public LauncherDataExtraInfo() {

		}

		public LauncherDataExtraInfo(Cursor cursor) {
			if (cursor == null) {
				throw new IllegalArgumentException("cursor cannot be null");
			}
			int mimetype = cursor.getInt(cursor.getColumnIndex(DataTable.MIME_TYPE));
			if (mimetype != MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA) {
				throw new IllegalArgumentException("current item is not launcherdata");
			}

			try {
				packageName = cursor.getString(cursor.getColumnIndex(DataTable.DATA1));
				appName = cursor.getString(cursor.getColumnIndex(DataTable.DATA2));
				dataDir = cursor.getString(cursor.getColumnIndex(DataTable.DATA3));
				versionCode = cursor.getString(cursor.getColumnIndex(DataTable.DATA4));
				versionName = cursor.getString(cursor.getColumnIndex(DataTable.DATA5));
				buildDisplay = cursor.getString(cursor.getColumnIndex(DataTable.DATA6));
				buildVersionSdkInt = cursor.getString(cursor.getColumnIndex(DataTable.DATA7));
				buildId = cursor.getString(cursor.getColumnIndex(DataTable.DATA8));
				buildModel = cursor.getString(cursor.getColumnIndex(DataTable.DATA9));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public LauncherDataExtraInfo(Parcel parcel) {
			packageName = parcel.readString();
			appName = parcel.readString();
			dataDir = parcel.readString();
			versionCode = parcel.readString();
			versionName = parcel.readString();
			buildDisplay = parcel.readString();
			buildVersionSdkInt = parcel.readString();
			buildId = parcel.readString();
			buildModel = parcel.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(packageName);
			dest.writeString(appName);
			dest.writeString(dataDir);
			dest.writeString(versionCode);
			dest.writeString(versionName);
			dest.writeString(buildDisplay);
			dest.writeString(buildVersionSdkInt);
			dest.writeString(buildId);
			dest.writeString(buildModel);
		}

		public static final Parcelable.Creator<LauncherDataExtraInfo> CREATOR = new Parcelable.Creator<LauncherDataRestoreEntry.LauncherDataExtraInfo>() {

			@Override
			public LauncherDataExtraInfo createFromParcel(Parcel source) {
				return new LauncherDataExtraInfo(source);
			}

			@Override
			public LauncherDataExtraInfo[] newArray(int size) {
				return new LauncherDataExtraInfo[size];
			}
		};
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_launcher_data);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA, null);
		File launcherDataFile = new File(recordRootPah,
				LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
		if (launcherDataFile.exists()) {
			launcherDataFile.delete();
		}
	}
}
