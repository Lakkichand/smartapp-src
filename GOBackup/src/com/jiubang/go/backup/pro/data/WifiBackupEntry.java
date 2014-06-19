package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份wifi，搜索"/data/misc/wifi/wpa_supplicant.conf"、"/data/wifi/bcm_supp.conf"、
 * "wpa.conf"以及"/data/misc/wifi/bcm_supp.conf"这几个文件，备份这几个文件
 * 
 * @author kevin
 */
public class WifiBackupEntry extends BaseBackupEntry {
	private static final String WIFI_DEFAULT_PATH = "/data/misc/wifi/wpa_supplicant.conf";
	private static final String WIFI_OPTION_PATH1 = "/data/wifi/bcm_supp.conf";
	private static final String WIFI_OPTION_PATH2 = "wpa.conf";
	private static final String WIFI_OPTION_PATH3 = "/data/misc/wifi/bcm_supp.conf";
	public static final String WIFI_BACKUP_NAME = "wifi.encrypt";

	private final Context mContext;
	private String mWifiPath;

	public WifiBackupEntry(Context context) {
		super();
		mContext = context;
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
		BackupArgs args = (BackupArgs) data;
		listener.onStart(null, null);
		boolean ret = false;
		ret = backupWifiInternal(ctx, args);
		/*
		 * if(ret){ ret = updateBackupDb(args.mDbHelper); }
		 */
		BackupState state = ret ? BackupState.BACKUP_SUCCESSFUL : BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, this, getWifiBackupFiles(args.mBackupPath));
		return true;
	}

	private String[] getWifiBackupFiles(String dir) {
		if (dir == null) {
			return null;
		}
		return new String[] { dir + WIFI_BACKUP_NAME };
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_WIFI);
		cv.put(DataTable.DATA1, WIFI_BACKUP_NAME);
		cv.put(DataTable.DATA2, mWifiPath);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
		// if (dbHelper.update(DataTable.TABLE_NAME, cv, DataTable.MIME_TYPE +
		// "=" + MimetypeTable.MIMETYPE_VALUE_WIFI, null) == 0) {
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		// return true;
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_SYSTEM_WIFI;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.wifi_access_points) : "";
	}

	private boolean backupWifiInternal(Context ctx, BackupArgs args) {
		String internalDataDir = ctx.getFilesDir().getAbsolutePath();
		// LinuxShell ls = new LinuxShell();
		// wifiPath = ls.backupWifi(internalDataDir,
		// Util.EnsureFileSeparator(args.mBackupPath));
		mWifiPath = backupWifiWithRootProcess(ctx, internalDataDir,
				Util.ensureFileSeparator(args.mBackupPath));
		if (mWifiPath == null) {
			return false;
		}
		// Log.d("GOBackup", "WifiBackupEntry : wifiPath = " + wifiPath);
		String wifiFileName = mWifiPath.substring(mWifiPath.lastIndexOf(File.separator) + 1);
		// Log.d("GOBackup", "WifiBackupEntry : wifiName = " + wifiFileName);
		File tempWifiFile = new File(args.mBackupPath, wifiFileName);
		if (!tempWifiFile.exists()) {
			return false;
		}

		boolean ret = false;
		File wifiFile = new File(args.mBackupPath, WIFI_BACKUP_NAME);
		if (Util.encryFile(tempWifiFile, wifiFile, Constant.getPassword())) {
			ret = true;
		}

		// 写入备份wifi的文件路径，恢复时需要用到
		/*
		 * if(ret){ args.mConfig.put(BackupPropertiesConfig.P_BACKUP_WIFI_PATH,
		 * mWifiPath); }
		 */
		updateBackupDb(args.mDbHelper);

		if (tempWifiFile != null && tempWifiFile.exists()) {
			tempWifiFile.delete();
		}
		return ret;
	}

	private String backupWifiWithRootProcess(Context context, String internalDataDir, String descDir) {
		String wifiPath = null;
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		String command = "";
		command += internalDataDir + "backup bw ";
		command += "-b ";
		command += internalDataDir + "busybox" + " ";
		command += descDir;
		String result = new RootShell.Command(command).execute(rootShell);
		if (result != null && result.contains("GO_SUCCESS")) {
			String[] allResults = result.split("\n");
			if (allResults != null) {
				for (String str : allResults) {
					if (str.startsWith("WIFI_PATH=")) {
						wifiPath = str.substring(10);
					}
				}
			}
		}
		return wifiPath;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_wifi);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return true;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_wifi);
	}
}
