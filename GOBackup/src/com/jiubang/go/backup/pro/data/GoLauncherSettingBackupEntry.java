package com.jiubang.go.backup.pro.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import com.jiubang.go.backup.pro.util.Util;

/**
 * Go桌面设置备份项
 *
 * @author wencan
 */
public class GoLauncherSettingBackupEntry extends BaseBackupEntry {
	private IAsyncTaskListener mListener = null;

	public static final String GO_LAUNCHER_DB_FILENAME = "androidheart.db";

	public static final String GOLAUNCHER_PACKAGE_NAME = "com.gau.go.launcherex";
	// GO桌面设置备份最小版本号
	public static final int GO_LAUNCHER_SETTING_MIN_VERSION_CODE = 96;
	// 备份桌面Action
	private static final String ACTION_BACKUP_RESTORE_GOLAUNCHER = "com.jiubang.ggheart.apps.desks.settings.BackReceiver";
	// 备份桌面
	private static final String CMD_BACKUP_DB = "com.jiubang.goback.backup_db";
	// 备份/恢复命令
	private static final String BACKCMD = "com.jiubang.goback.backCMD";
	// 备份路径
	private static final String BACKPATH = "com.jiubang.goback.backPath";
	// 返回的备份信息
	private static final String BACKINFO = "com.jiubang.goback.backInfo";

	// 无法完成备份
	private static final String EXPORT_ERROR = "com.jiubang.goback.export_error";
	// 备份成功
	private static final String EXPORT_SUCCESS = "com.jiubang.goback.export_success";
	// 数据库内容不存在
	private static final String DATABASE_NOT_EXIT = "com.jiubang.goback.databasenoexit";

	// 随机字符串
	private static final String RANDOMSTR = "com.jiubang.goback.randomStr";

	public static final String BACKUP_GOLAUNCHER = ".golauncher";
	public static final String BACKUP_GOLANUCHER_DB_ERROR = "_DB_ERROR";
	public static final String GOLAUNCHER_RANDOM_FILE_NAME = "golauncher_setting.prop";

	private static final int MAX_WAIT_TIME = 30 * 1000;

	private Context mContext = null;
	private Context mBackupContext = null;
	private BackupArgs mArgs = null;
	private BackupGoLauncherReceive mReceive = null;
	// private Timer mTimer = null;
	// private TimerTask mTimerTask = null;

	private boolean mHasBackupReturn = false;
	private String mRandomStr = null;

	public GoLauncherSettingBackupEntry(Context ctx) {
		mContext = ctx;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}
		mBackupContext = ctx.getApplicationContext();
		mListener = listener;
		mArgs = (BackupArgs) data;
		// TODO
		setState(BackupState.BACKUPING);
		mListener.onStart(null, null);

		if (mReceive != null && mBackupContext != null) {
			try {
				mBackupContext.unregisterReceiver(mReceive);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mReceive = null;
		}
		mReceive = new BackupGoLauncherReceive();
		try {
			mBackupContext.registerReceiver(mReceive, mReceive.getIntentFilter());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent it = new Intent();
		it.setPackage(GOLAUNCHER_PACKAGE_NAME);
		it.setAction(ACTION_BACKUP_RESTORE_GOLAUNCHER);
		it.putExtra(BACKPATH, mArgs.mBackupPath);
		it.putExtra(BACKCMD, CMD_BACKUP_DB);
		ctx.sendBroadcast(it);
		startTimer();
		return true;
	}

	private void startTimer() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (!mHasBackupReturn) {
					// 如果在规定的时间，没有接收到备份成功的广播，则退出，失败
					finishBackup(null);
				}
			}
		};
		new Timer().schedule(task, MAX_WAIT_TIME);
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

	private void finishBackup(Intent intent) {
		if (mArgs == null || mArgs.mBackupPath == null) {
			if (mListener != null) {
				mListener.onEnd(false, this, null);
			}
			return;
		}

		boolean ret = false;
		if (intent != null) {
			String result = intent.getStringExtra(BACKINFO);
			boolean isDatabaseExit = true;
			if (result.equals(EXPORT_ERROR)) {
				ret = false;
			} else if (result.equals(DATABASE_NOT_EXIT)) {
				// 数据库不存在，桌面程序未启动引起,不写入文件
				mRandomStr = intent.getStringExtra(RANDOMSTR);
				isDatabaseExit = false;
				ret = true;
			} else if (result.equals(EXPORT_SUCCESS)) {
				mRandomStr = intent.getStringExtra(RANDOMSTR);
				isDatabaseExit = true;
				ret = true;
			}

			if (ret && mRandomStr != null && !isDatabaseExit) {
				mRandomStr += BACKUP_GOLANUCHER_DB_ERROR;
			}
		}

		File propFile = new File(mArgs.mBackupPath, GOLAUNCHER_RANDOM_FILE_NAME);
		ret = createRandomFile(propFile.getAbsolutePath());
		// 更新数据库
		if (ret) {
			ret = updateDatabase(mArgs.mDbHelper, propFile.getName());
		}

		BackupState state = ret ? BackupState.BACKUP_SUCCESSFUL : BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);

		if (mListener != null) {
			mListener.onEnd(ret, this, new String[] { propFile.getAbsolutePath(),
					mArgs.mBackupPath + GO_LAUNCHER_DB_FILENAME });
		}
		if (mBackupContext != null && mReceive != null) {
			try {
				mBackupContext.unregisterReceiver(mReceive);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mReceive = null;
		}
		mBackupContext = null;
		mListener = null;
		mArgs = null;
	}

	private boolean updateDatabase(BackupDBHelper dbHelper, String randomFileName) {
		if (dbHelper == null || randomFileName == null) {
			return false;
		}

		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING);
		cv.put(DataTable.DATA1, randomFileName);
		// TODO 确保GO桌面备份的文件名字
		cv.put(DataTable.DATA2, GO_LAUNCHER_DB_FILENAME);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
		// if (dbHelper.update(DataTable.TABLE_NAME, cv, DataTable.MIME_TYPE +
		// "=" + MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING, null) == 0) {
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		// return true;
	}

	/**
	 * 备份go桌面receiver
	 *
	 * @author wencan
	 */
	class BackupGoLauncherReceive extends BroadcastReceiver {
		public static final String ACTION_BACKUP = "com.jiubang.go.backup.ACTION_BACKUP_GOLAUNCHER_FINISH";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();
				if (action.equals(ACTION_BACKUP)) {
					Log.d("backuptest", ACTION_BACKUP);
					mHasBackupReturn = true;
					finishBackup(intent);
				}
			}
		}

		public IntentFilter getIntentFilter() {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION_BACKUP);
			return intentFilter;
		}
	}

	private boolean createRandomFile(String path) {
		if (path == null) {
			return false;
		}

		boolean ret = true;
		FileOutputStream trace = null;
		DataOutputStream dataoutput = null;
		File file = new File(path);

		try {
			if (file.exists()) {
				file.delete();
			}

			trace = new FileOutputStream(file);
			dataoutput = new DataOutputStream(trace);
			dataoutput.writeUTF(mRandomStr);
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
		final DrawableKey key = DrawableProvider.buildDrawableKey(GOLAUNCHER_PACKAGE_NAME);
		final Drawable defaultDrawable = DrawableProvider.getDefaultActivityIcon(context);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}
}
