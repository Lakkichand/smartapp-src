package com.jiubang.go.backup.pro.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.PackageUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * Go桌面设置恢复项
 *
 * @author wencan
 */
public class GoLauncherSettingRestoreEntry extends BaseRestoreEntry {

	private static final String GOLAUNCHER_PACKAGE_NAME = "com.gau.go.launcherex";
	// 恢复桌面Action
	private static final String ACTION_BACKUP_RESTORE_GOLAUNCHER = "com.jiubang.ggheart.apps.desks.settings.BackReceiver";
	// 恢复桌面
	private static final String CMD_RESTOREDB = "com.jiubang.goback.restore_db";
	// 恢复命令
	private static final String BACKCMD = "com.jiubang.goback.backCMD";
	// 恢复路径
	private static final String BACKPATH = "com.jiubang.goback.backPath";

	// 随机字符串
	private static final String RANDOMSTR = "com.jiubang.goback.randomStr";

	// 返回的恢复信息
	private static final String BACKINFO = "com.jiubang.goback.backInfo";

	// 数据库内容不存在
	private static final String DATABASE_NOT_EXIT = "com.jiubang.goback.databasenoexit";

	// 恢复备份成功
	private static final String DFILE_IMPORT_SUCCESS = "com.jiubang.goback.dbfile_import_success";
	// 无法恢复备份
	private static final String DFILE_IMPORT_ERROR = "com.jiubang.goback.dbfile_import_error";
	// 无法读取SD卡
	private static final String SDCARD_UNMOUNTED = "com.jiubang.goback.sdcard_unmounted";
	// 无法找到备份文件
	private static final String DBFILE_NOT_FOUND = "com.jiubang.goback.db_not_found";

	private static final int MAX_WAIT_TIME = 30 * 1000;

	private Context mContext = null;

	private IAsyncTaskListener mListener = null;
	private Context mRestoreContext = null;
	private RestoreArgs mArgs = null;
	private RestoreGoLauncherReceive mReceive = null;

	private String mRandomStr = null;
	private boolean mDBNotExitWhenBackup = false;

	private Timer mTimer = null;
	private TimerTask mTimerTask = null;

	private boolean mHasRestoreReturn = false;

	private String mRecordDir;

	public GoLauncherSettingRestoreEntry(Context ctx, String recordDir) {
		mContext = ctx;
		mRecordDir = recordDir;
		mRandomStr = getRandomStr(mRecordDir);
		if (mRandomStr != null
				&& mRandomStr.contains(GoLauncherSettingBackupEntry.BACKUP_GOLANUCHER_DB_ERROR)) {
			mDBNotExitWhenBackup = true;
			mRandomStr = mRandomStr.substring(0,
					mRandomStr.indexOf(GoLauncherSettingBackupEntry.BACKUP_GOLANUCHER_DB_ERROR));
			Log.d("test", "mRandomStr = " + mRandomStr);
		} else {
			mDBNotExitWhenBackup = false;
		}

		if (hasOrginalFile(mRecordDir)
				&& PackageUtil.isPackageInstalled(ctx, GOLAUNCHER_PACKAGE_NAME)) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static String getRandomStr(String recordDir) {
		if (recordDir == null) {
			return null;
		}

		String randomStr = null;

		File randomFile = getRandomFile(recordDir);
		if (randomFile == null) {
			return null;
		}

		String fileName = randomFile.getName();
		// 兼容V2.0以前版本
		if (fileName.endsWith(GoLauncherSettingBackupEntry.BACKUP_GOLAUNCHER)) {
			randomStr = fileName.substring(0,
					fileName.indexOf(GoLauncherSettingBackupEntry.BACKUP_GOLAUNCHER));
			return randomStr;
		}

		// 2.0版本
		FileInputStream fis = null;
		DataInputStream dis = null;
		if (!fileName.equals(GoLauncherSettingBackupEntry.GOLAUNCHER_RANDOM_FILE_NAME)) {
			return null;
		}

		try {
			fis = new FileInputStream(randomFile);
			dis = new DataInputStream(fis);
			randomStr = dis.readUTF();
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
		return randomStr;
	}

	public static boolean hasOrginalFile(String recordDir) {
		return getRandomFile(recordDir) != null;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}

		// if(!(context instanceof Activity)){
		// return false;
		// }

		mRestoreContext = context.getApplicationContext();
		mListener = listener;
		mArgs = (RestoreArgs) data;

		setState(RestoreState.RESTORING);
		mListener.onStart(GoLauncherSettingRestoreEntry.this, null);

		// 根据需求，如果在备份时数据库不存在，在恢复时，则不需恢复，直接是恢复成功
		if (mDBNotExitWhenBackup || mRandomStr == null) {
			mListener.onEnd(true, GoLauncherSettingRestoreEntry.this, null);
		} else {
			if (mRestoreContext != null && mReceive != null) {
				try {
					mRestoreContext.unregisterReceiver(mReceive);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mReceive = null;
			}
			mReceive = new RestoreGoLauncherReceive();
			try {
				mRestoreContext.registerReceiver(mReceive, mReceive.getIntentFilter());
			} catch (Exception e) {
				e.printStackTrace();
			}

			Intent it = new Intent();
			it.setAction(ACTION_BACKUP_RESTORE_GOLAUNCHER);
			it.putExtra(BACKPATH, mArgs.mRestorePath);
			if (mDBNotExitWhenBackup) {
				it.putExtra(BACKCMD, DATABASE_NOT_EXIT);
			} else {
				it.putExtra(BACKCMD, CMD_RESTOREDB);
			}
			it.putExtra(RANDOMSTR, mRandomStr);
			context.sendBroadcast(it);

			startTimer();
		}

		return true;
	}

	private void startTimer() {
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (!mHasRestoreReturn) {
					// 如果在规定的时间，没有接收到备份成功的广播，则退出，失败
					Log.d("test", "startTimer : mHasRestoreReturn = " + mHasRestoreReturn);
					finishRestore(null);
					mTimer = null;
					mTimerTask = null;
				}
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTimerTask, MAX_WAIT_TIME);
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_GOLAUNCHER_SETTING;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.golauncher_setting) : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = Util.loadIconFromPackageName(context, GOLAUNCHER_PACKAGE_NAME);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			} else {
				ret = false;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	public static File getRandomFile(String recordDir) {
		if (recordDir == null) {
			return null;
		}

		File randomFile = null;

		// v2.0以前版本
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				if (name.endsWith(GoLauncherSettingBackupEntry.BACKUP_GOLAUNCHER)) {
					return true;
				}
				return false;
			}
		};
		File file = new File(recordDir);
		File[] allSubFiles = file.listFiles(fileFilter);
		if (allSubFiles != null && allSubFiles.length > 0) {
			randomFile = allSubFiles[0];
			return randomFile;
		}

		// 2.0版本
		randomFile = new File(recordDir, GoLauncherSettingBackupEntry.GOLAUNCHER_RANDOM_FILE_NAME);
		if (randomFile.exists()) {
			return randomFile;
		}
		return null;
	}

	private void finishRestore(Intent intent) {
		if (mTimer != null) {
			mTimer.cancel();
		}
		boolean ret = false;
		if (intent != null) {
			String result = intent.getStringExtra(BACKINFO);
			Log.d("test", "finishBackup : result = " + result);
			if (result != null) {
				if (result.equals(DFILE_IMPORT_ERROR) || result.equals(SDCARD_UNMOUNTED)
						|| result.equals(DBFILE_NOT_FOUND)) {
					ret = false;
				} else if (result.equals(DFILE_IMPORT_SUCCESS)) {
					ret = true;
				}
			}
		} else {
			ret = false;
		}

		// 设置恢复状态
		RestoreState state = ret
				? RestoreState.RESTORE_SUCCESSFUL
				: RestoreState.RESTORE_ERROR_OCCURRED;
		setState(state);

		if (mListener != null) {
			mListener.onEnd(ret, GoLauncherSettingRestoreEntry.this, null);
		}

		if (mRestoreContext != null && mReceive != null) {
			try {
				mRestoreContext.unregisterReceiver(mReceive);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mReceive = null;
		}

		mRestoreContext = null;
		mListener = null;
		mArgs = null;
		mTimer = null;
		mTimerTask = null;
	}

	/**
	 * 恢复Go桌面receiver
	 *
	 * @author wencan
	 */
	class RestoreGoLauncherReceive extends BroadcastReceiver {
		public static final String ACTION_RESTORE = "com.jiubang.go.backup.ACTION_RESTORE_GOLAUNCHER_FINISH";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("test", "RestoreGoLauncherReceive : onreceive : action = " + intent.getAction());
			mHasRestoreReturn = true;
			finishRestore(intent);
		}

		public IntentFilter getIntentFilter() {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION_RESTORE);
			return intentFilter;
		}
	}

	@Override
	public void stopRestore() {

	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(GOLAUNCHER_PACKAGE_NAME);
		final Drawable defaultDrawable = DrawableProvider.getDefaultActivityIcon(context);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING, null);
		File goLauncherSettingDBFile = new File(recordRootPah,
				GoLauncherSettingBackupEntry.GO_LAUNCHER_DB_FILENAME);
		if (goLauncherSettingDBFile.exists()) {
			goLauncherSettingDBFile.delete();
		}
		File goLauncherSettingFile = new File(recordRootPah,
				GoLauncherSettingBackupEntry.GOLAUNCHER_RANDOM_FILE_NAME);
		if (goLauncherSettingFile.exists()) {
			goLauncherSettingFile.delete();
		}
	}
}
