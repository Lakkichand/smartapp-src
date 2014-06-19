package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.ringtone.RingtoneBackup;
import com.jiubang.go.backup.pro.ringtone.RingtoneBackup.RingtoneBackupArgs;
import com.jiubang.go.backup.pro.ringtone.RingtoneBackup.RingtoneBackupMsg;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 铃声备份项
 * 
 * @author chenchangming
 */
public class RingtoneBackupEntry extends BaseBackupEntry {

	private Context mContext = null;
	private RingtoneBackup mRingtoneBackup = new RingtoneBackup();
	private IAsyncTaskListener mListener = null;
	// 异步线程listener
	private HandlerThread mThread;
	// 备份参数
	private BackupArgs mBackupArgs = null;
	// 备份线程handler
	private BackupRingtoneThreadHandler mHandler;
	private String mRingtoneBackupThreadName = "ringtoneBackupThreadName";

	public RingtoneBackupEntry(Context context) {
		super();
		mContext = context;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		// TODO Auto-generated method stub
		boolean result = true;
		if (ctx == null || data == null || listener == null) {
			result = false;
			return result;
		}
		if (!(data instanceof BackupArgs)) {
			result = false;
			return result;
		}
		if (mThread == null) {
			mThread = new HandlerThread(mRingtoneBackupThreadName);
			mThread.start();
			mHandler = new BackupRingtoneThreadHandler(mThread.getLooper());
		}

		mBackupArgs = (BackupArgs) data;
		mListener = listener;
		mListener.onStart(null, null);

		// ringtoneBackup = new RingtoneBackup();
		RingtoneBackupArgs args = new RingtoneBackupArgs();
		args.mHandler = mHandler;
		args.mBackupFilePath = Util.ensureFileSeparator(mBackupArgs.mBackupPath);
		mRingtoneBackup.backupRingtone(mContext, args);
		return true;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EntryType getType() {
		// TODO Auto-generated method stub
		return EntryType.TYPE_SYSTEM_RINGTONE;
	}

	@Override
	public long getSpaceUsage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return mContext != null ? mContext.getString(R.string.ringtone) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 备份铃声线程handler
	 * 
	 * @author chenchangming
	 */
	private class BackupRingtoneThreadHandler extends Handler {

		public BackupRingtoneThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final float m025f = 0.25f;
			switch (msg.what) {
				case RingtoneBackupMsg.RINGTONE_BACKUP_START :
					break;

				case RingtoneBackupMsg.RINGTONE_BACKUP_PROCEEDING :
					if (mListener != null) {
						mListener.onProceeding(m025f, RingtoneBackupEntry.this, msg.arg1, msg.arg2);
					}
					// mTotalMmsCount = msg.arg2;
					break;

				case RingtoneBackupMsg.RINGTONE_BACKUP_END :
					finish(true);
					break;

				case RingtoneBackupMsg.RINGTONE_BACKUP_ERROR_OCCUR :
					finish(false);
					break;

				default :
					break;
			}
		}
	}

	private void finish(boolean result) {
		if (result && mBackupArgs != null) {
			// 更新铃声数据库.
			result = updateBackupDb(mBackupArgs.mDbHelper);
		}
		BackupState state = result
				? BackupState.BACKUP_SUCCESSFUL
				: BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);

		if (mListener != null) {
			mListener.onEnd(result, this, getRingtoneBackupFiles());
		}
		quitBackup();
	}

	public String[] getRingtoneBackupFiles() {
		Map<String, File> ringtoneFiles = mRingtoneBackup.getRingtoneFiles();
		if (ringtoneFiles == null) {
			return null;
		}

		Collection<File> allFiles = ringtoneFiles.values();
		Iterator<File> iterator = allFiles.iterator();
		List<String> result = new ArrayList<String>();
		while (iterator.hasNext()) {
			String file = iterator.next().getAbsolutePath();
			if (!result.contains(file)) {
				result.add(file);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		boolean ret = false;
		Cursor cursor = null;
		try {
			String whereCondition = DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_RINGTONE;
			cursor = dbHelper.query(DataTable.TABLE_NAME, null, whereCondition, null, null);
			if (cursor != null && cursor.getCount() != 0) {
				dbHelper.delete(DataTable.TABLE_NAME, whereCondition, null);
			}
			ContentValues cv = new ContentValues();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_RINGTONE);
			int count = mRingtoneBackup.getRingtoneCount();
			cv.put(DataTable.DATA1, count);
			cv.put(DataTable.DATA2, RingtoneBackup.MANAGER_DATA);

			Map<String, File> allFiles = mRingtoneBackup.getRingtoneFiles();
			if (allFiles == null) {
				return false;
			}
			if (allFiles.containsKey(String.valueOf(RingtoneManager.TYPE_ALARM))) {
				File file = allFiles.get(String.valueOf(RingtoneManager.TYPE_ALARM));
				cv.put(DataTable.DATA3, file.getName());
			}
			if (allFiles.containsKey(String.valueOf(RingtoneManager.TYPE_ALL))) {
				File file = allFiles.get(String.valueOf(RingtoneManager.TYPE_ALL));
				cv.put(DataTable.DATA4, file.getName());
			}
			if (allFiles.containsKey(String.valueOf(RingtoneManager.TYPE_NOTIFICATION))) {
				File file = allFiles.get(String.valueOf(RingtoneManager.TYPE_NOTIFICATION));
				cv.put(DataTable.DATA5, file.getName());
			}
			if (allFiles.containsKey(String.valueOf(RingtoneManager.TYPE_RINGTONE))) {
				File file = allFiles.get(String.valueOf(RingtoneManager.TYPE_RINGTONE));
				cv.put(DataTable.DATA6, file.getName());
			}
			cv.put(DataTable.DATA14, System.currentTimeMillis());
			ret = dbHelper.reflashDatatable(cv);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// 关闭数据库
			if (cursor != null) {
				cursor.close();
			}
		}
		return ret;
	}

	private void quitBackup() {
		if (mThread != null && mThread.getLooper() != null) {
			mThread.getLooper().quit();
			mThread = null;
			mHandler = null;
		}
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_ringtone);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_ringtone);
	}
}
