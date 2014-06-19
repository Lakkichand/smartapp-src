package com.jiubang.go.backup.pro.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 桌面数据备份项
 *
 * @author wencan
 */
public class LauncherDataBackupEntry extends BaseBackupEntry {
	private static final int M16 = 16;
	public static final String WALLPAPER_FILE_NAME = "wallpaper.pg";
	public static final String APP_WIDGET_FILE_PATH = Util.getAndroidSystemVersion() >= M16
			? "/data/system/users/0/appwidgets.xml"
			: "/data/system/appwidgets.xml";
	public static final String APP_WIDGET_FILE_NAME = "appwidgets.xml";
	public static final String LAUNCHER_DATA_SUFFIXN = ".LAUNCHER_DATA";
	public static final String LAUNCHER_DATA_FILE_NAME = "launcher_data.prop";

	public static final String[] PACKAGE_FILTER = { "com.htc.launcher" };

	private final Context mContext;
	private final AppInfo mAppInfo;
	private List<String> mAllBackupFilePath;

	public LauncherDataBackupEntry(Context context, AppInfo appInfo) {
		mContext = context;
		mAppInfo = appInfo;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}
		setState(BackupState.BACKUPING);
		listener.onStart(null, null);
		boolean ret = false;
		ret = backupInternal(ctx, (BackupArgs) data, listener);
		BackupState state = ret ? BackupState.BACKUP_SUCCESSFUL : BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, this, getLauncherDataBackupFiles());
		return true;
	}

	public AppInfo getAppInfo() {
		return mAppInfo;
	}

	private String[] getLauncherDataBackupFiles() {
		if (mAllBackupFilePath == null || mAllBackupFilePath.size() == 0) {
			return null;
		}

		String[] result = new String[mAllBackupFilePath.size()];
		return mAllBackupFilePath.toArray(result);
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

	public static boolean filterIgnorePackage(String packageName) {
		if (packageName == null) {
			return false;
		}

		final int count = PACKAGE_FILTER.length;
		for (int i = 0; i < count; i++) {
			if (packageName.equals(PACKAGE_FILTER[i])) {
				return true;
			}
		}
		return false;
	}

	private boolean backupInternal(Context context, BackupArgs args, IAsyncTaskListener listener) {
		if (mAppInfo == null) {
			return false;
		}

		final float m05f = 0.5f;
		final float m09f = 0.9f;
		mAllBackupFilePath = new ArrayList<String>();

		boolean ret = true;
		ContentValues cv = new ContentValues();
		BackupDBHelper dbHelper = args.mDbHelper;
		String internalDataDir = Util.ensureFileSeparator(context.getFilesDir().getAbsolutePath());

		if (backupWithRootProcess(context, internalDataDir, mAppInfo.dataDir, mAppInfo.packageName,
				Util.ensureFileSeparator(args.mBackupPath))) {
			// 备份/data/system/appwidgets.xml文件
			ret = backupAppWidgetFile(context, internalDataDir, APP_WIDGET_FILE_PATH,
					Util.ensureFileSeparator(args.mBackupPath));
			if (ret) {
				mAllBackupFilePath.add(args.mBackupPath + APP_WIDGET_FILE_NAME);
			}
			if (!ret) {
				return false;
			}

			ret = createLauncherDataFile(args.mBackupPath, mAppInfo);
			if (!ret) {
				return false;
			}

			File file = new File(args.mBackupPath, LAUNCHER_DATA_FILE_NAME);
			// 拷贝文件成功,更新数据库
			ret = true;
			cv.clear();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA);
			cv.put(DataTable.DATA1, mAppInfo.packageName);
			cv.put(DataTable.DATA2, mAppInfo.appName);
			cv.put(DataTable.DATA3, mAppInfo.dataDir);
			cv.put(DataTable.DATA4, mAppInfo.versionCode);
			cv.put(DataTable.DATA5, mAppInfo.versionName);
			cv.put(DataTable.DATA6, Build.DISPLAY); // Rom描述
			cv.put(DataTable.DATA7, Build.VERSION.SDK_INT); // SDK版本
			cv.put(DataTable.DATA8, Build.ID); // ROM 类型
			cv.put(DataTable.DATA9, Build.MODEL); // 手机型号
			cv.put(DataTable.DATA10, file.getName());
			cv.put(DataTable.DATA11, APP_WIDGET_FILE_NAME);
			cv.put(DataTable.DATA13, mAppInfo.packageName + ".tar.gz");
			cv.put(DataTable.DATA14, new Date().getTime());
			ret = dbHelper.reflashDatatable(cv);
			// if (dbHelper.update(DataTable.TABLE_NAME, cv,
			// DataTable.MIME_TYPE + "=" +
			// MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA + " AND " +
			// DataTable.DATA1 + "='" + item.packageName + "'", null) == 0) {
			// ret = dbHelper.insert(DataTable.TABLE_NAME, cv);
			// }
			if (ret) {
				mAllBackupFilePath.add(file.getAbsolutePath());
				mAllBackupFilePath.add(args.mBackupPath + mAppInfo.packageName + ".tar.gz");
			}
		} else {
			ret = false;
		}

		if (listener != null) {
			listener.onProceeding(m05f, this, null, null);
		}

		// if(listener != null){
		// listener.onProceeding(0.5f, this, null, null);
		// }

		// if(ret){
		// //备份壁纸
		// if(backupWallpaper(context, args)){
		// cv.clear();
		// cv.put(DataTable.DATA12, WALLPAPER_FILE_NAME);
		// String where = DataTable.MIME_TYPE + "=" +
		// MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA;
		// ret = dbHelper.update(DataTable.TABLE_NAME, cv, where, null) > 0;
		// mAllBackupFilePath.add(args.mBackupPath + WALLPAPER_FILE_NAME);
		// }
		// }

		if (listener != null) {
			listener.onProceeding(m09f, this, null, null);
		}

		if (!ret) {
			// 备份失败,删除当次备份文件
			deleteBackupFileIfBackupFaild(args.mBackupPath);
		}

		//删除数据库中其他的系统桌面备份，只允许一个最新的系统桌面布局备份（增量备份时可能存在多个）
		if (ret) {
			String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA
					+ " AND " + DataTable.DATA1 + "!='" + mAppInfo.packageName + "'";
			dbHelper.delete(DataTable.TABLE_NAME, where, null);
		}
		return ret;
	}

	private boolean backupWithRootProcess(Context context, String internalDataDir,
			String dataFilePath, String packageName, String descDir) {
		boolean ret = false;
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		Util.forceKillProcess(context, packageName);
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		String command = "";
		command += internalDataDir + "backup ba ";
		command += "-d " + dataFilePath + " ";
		command += internalDataDir + "busybox ";
		command += descDir + " ";
		command += packageName;
		String result = new RootShell.Command(command).execute(rootShell);
		if (result != null) {
			Log.d("GOBackup", "backupAppWithRootProcess : result = " + result);
			ret = result.contains("GO_SUCCESS") ? true : false;
		}
		return ret;
	}

	private boolean backupWallpaper(Context context, BackupArgs args) {
		WallpaperManager wm = WallpaperManager.getInstance(context);
		BitmapDrawable bd = null;

		final int m8129 = 8129;
		Drawable drawable = wm.getDrawable();
		if (!(drawable instanceof BitmapDrawable)) {
			return false;
		}
		bd = (BitmapDrawable) drawable;

		File destFile = new File(args.mBackupPath, WALLPAPER_FILE_NAME);
		byte[] bmBuffer = Util.bitmapToByteArray(bd.getBitmap());
		if (bmBuffer == null) {
			return false;
		}

		boolean ret = true;
		try {
			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bmBuffer));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
			byte[] buffer = new byte[m8129];
			int len;
			while ((len = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			bos.flush();
			bis.close();
			bos.close();
			buffer = null;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		bmBuffer = null;
		return ret;
	}

	private boolean backupAppWidgetFile(Context context, String internalDataDir,
			String appwidgetFilePath, String desPath) {
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		boolean ret = Util.copyFileWithRootProcess(context, rootShell, appwidgetFilePath, desPath);
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

	private void deleteBackupFileIfBackupFaild(String rootDir) {
		if (mAppInfo != null) {
			String packageFileName = mAppInfo.packageName + ".tar.gz";
			File file = new File(rootDir, packageFileName);
			if (file.exists()) {
				file.delete();
			}
		}

		File launchDataFile = new File(rootDir, LAUNCHER_DATA_FILE_NAME);
		if (launchDataFile.exists()) {
			launchDataFile.delete();
		}

		File wallPaperFile = new File(rootDir, WALLPAPER_FILE_NAME);
		if (wallPaperFile.exists()) {
			wallPaperFile.delete();
		}

		File appWidgetFile = new File(rootDir, APP_WIDGET_FILE_NAME);
		if (appWidgetFile.exists()) {
			appWidgetFile.delete();
		}
	}

	private boolean createLauncherDataFile(String rootDir, AppInfo appinfo) {
		if (rootDir == null || appinfo == null) {
			return false;
		}

		File file = new File(rootDir, LAUNCHER_DATA_FILE_NAME);

		boolean ret = true;
		FileOutputStream trace = null;
		DataOutputStream dataoutput = null;

		try {
			if (file.exists()) {
				file.delete();
			}

			trace = new FileOutputStream(file);
			dataoutput = new DataOutputStream(trace);
			dataoutput.writeUTF(appinfo.packageName);
			dataoutput.writeInt(appinfo.versionCode);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (trace != null) {
					trace.close();
					trace = null;
				}
				if (dataoutput != null) {
					dataoutput.close();
					dataoutput = null;
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_launcher_data);
	}
}
