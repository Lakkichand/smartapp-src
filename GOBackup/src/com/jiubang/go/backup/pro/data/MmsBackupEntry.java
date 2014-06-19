package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.mms.MmsBackup;
import com.jiubang.go.backup.pro.mms.MmsBackup.MmsBackupArgs;
import com.jiubang.go.backup.pro.mms.MmsBackup.MmsBackupMsg;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 彩信备份
 * 
 * @author ReyZhang
 */
public class MmsBackupEntry extends BaseBackupEntry {

	public static final String MMS_DIR_NAME = "MMS_Backup";

	private Context mContext = null;
	private IAsyncTaskListener mListener = null;
	// 异步线程listener
	private HandlerThread mThread;
	// 彩信条数
	private int mTotalMmsCount = 0;
	// 备份参数
	private BackupArgs mBackupArgs = null;
	// 备份线程handler
	private BackupMmsThreadHandler mHandler;
	private String mMmsBackupThreadName = "mmsBackupThreadName";

	private final int mDivNum = 10;

	public MmsBackupEntry(Context context) {
		super();
		mContext = context;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
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
			mThread = new HandlerThread(mMmsBackupThreadName);
			mThread.start();
			mHandler = new BackupMmsThreadHandler(mThread.getLooper());
		}
		// 获取彩信总的条数
		setState(BackupState.BACKUPING);
		mBackupArgs = (BackupArgs) data;
		mListener = listener;
		mListener.onStart(null, null);

		MmsBackup mmsBackup = new MmsBackup();
		MmsBackupArgs args = new MmsBackupArgs();
		args.mHandler = mHandler;
		args.mBackupFilePath = Util.ensureFileSeparator(mBackupArgs.mBackupPath);
		result = mmsBackup.backupMms(mContext, args);
		return result;
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_MMS;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.mms) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_mms);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	/**
	 * 内部类 彩信备份handler
	 * 
	 * @author ReyZhang
	 */
	private class BackupMmsThreadHandler extends Handler {

		public BackupMmsThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MmsBackupMsg.MMS_BACKUP_START :
					break;

				case MmsBackupMsg.MMS_BACKUP_PROCEEDING :
					if (mListener != null
							&& (msg.arg1 == 0 || msg.arg1 == msg.arg2 || msg.arg1 % mDivNum == 0)) {
						mListener.onProceeding((float) (msg.arg1) / (float) (msg.arg2),
								MmsBackupEntry.this, msg.arg1, msg.arg2);
					}
					mTotalMmsCount = msg.arg2;
					break;

				case MmsBackupMsg.MMS_BACKUP_END :
					finish(true);
					break;

				case MmsBackupMsg.MMS_BACKUP_ERROR_OCCUR :
					finish(false);
					break;

				case MmsBackupMsg.MMS_BACKUP_COUNT_ZERO :
					finish(false);
					break;

				default :
					break;
			}
		}
	}

	private void finish(boolean result) {
		BackupState state = result
				? BackupState.BACKUP_SUCCESSFUL
				: BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);

		if (result && mBackupArgs != null) {
			// TODO 去掉config
			// if( mBackupArgs.mConfig != null ){
			// mBackupArgs.mConfig.put(
			// BackupPropertiesConfig.P_BACKUP_MMS_COUNT, String.valueOf(
			// mTotalMmsCount ) );
			// }
			// 更新彩信数据库.
			String filePath = mBackupArgs.mBackupPath + MMS_DIR_NAME;
			result = updateBackupDb(mBackupArgs.mDbHelper, filePath);
		}

		if (mListener != null) {
			mListener
					.onEnd(result, this, getMmsBackupFiles(mBackupArgs.mBackupPath + MMS_DIR_NAME));
		}
		quitBackup();
	}

	private String[] getMmsBackupFiles(String mmsDir) {
		if (mmsDir == null) {
			return null;
		}

		String[] result = null;
		File file = new File(mmsDir);
		File[] fileList = file.listFiles();
		if (fileList != null && fileList.length > 0) {
			result = new String[fileList.length];
			final int len = fileList.length;
			for (int i = 0; i < len; i++) {
				result[i] = fileList[i].getAbsolutePath();
			}
		}
		return result;
	}

	public static boolean updateBackupDb(BackupDBHelper dbHelper, String path) {
		if (dbHelper == null) {
			return false;
		}
		File file = new File(path);
		File[] fileList = file.listFiles();
		int count = fileList != null ? fileList.length : 0;
		if (!file.isDirectory() || count == 0) {
			return false;
		}
		// 如果数据库表中存在mms数据字段，先清空数据库
		Cursor cursor = null;
		try {
			String whereCondition = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_MMS;
			cursor = dbHelper.query(DataTable.TABLE_NAME, null, whereCondition, null, null);
			if (cursor != null && cursor.getCount() != 0) {
				dbHelper.delete(DataTable.TABLE_NAME, whereCondition, null);
			}

			for (int i = 0; i < count; i++) {
				ContentValues cv = new ContentValues();
				cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_MMS);
				cv.put(DataTable.DATA1, fileList[i].getName());
				// dbHelper.insert( DataTable.TABLE_NAME, cv );
				dbHelper.reflashDatatable(cv);
			}
			// 完成以后，差一条数据库彩信的条数信息，用于获取彩信总体条数
			ContentValues cvPDUCount = new ContentValues();
			cvPDUCount.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_MMS);
			cvPDUCount.put(DataTable.DATA2, count);
			cvPDUCount.put(DataTable.DATA14, new Date().getTime());
			// dbHelper.insert( DataTable.TABLE_NAME, cvPDUCount );
			dbHelper.reflashDatatable(cvPDUCount);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// 关闭数据库
			if (cursor != null) {
				cursor.close();
			}
		}
		return true;
	}

	private void quitBackup() {
		if (mThread != null && mThread.getLooper() != null) {
			mThread.getLooper().quit();
			mThread = null;
			mHandler = null;
		}
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_mms);
	}
}
