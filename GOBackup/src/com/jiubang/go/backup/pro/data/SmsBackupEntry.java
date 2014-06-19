package com.jiubang.go.backup.pro.data;

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
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.sms.Sms;
import com.jiubang.go.backup.pro.sms.SmsBackup;
import com.jiubang.go.backup.pro.sms.SmsBackup.SmsBackupArgs;
import com.jiubang.go.backup.pro.sms.SmsBackup.SmsBackupMsg;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 短信备份项
 * 
 * @author wencan
 */
public class SmsBackupEntry extends BaseBackupEntry {
	public static final String MMS_PACKAGE_NAME = "com.android.mms";
	public static final String SMS_BACKUP_FILE_NAME = "sms.dat";

	private final Context mContext;
	private IAsyncTaskListener mListener = null;
	private HandlerThread mThread;
	private BackupSmsThreadHandler mHandler;
	private final String mSmsBackupThreadName = "smsBackupThreadName";
	private int mTotalSmsCount = 0;
	private BackupArgs mBackupArgs = null;

	/**
	 * 短信备份消息handler
	 * 
	 * @author wencan
	 */
	private class BackupSmsThreadHandler extends Handler {

		public BackupSmsThreadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final int m10 = 10;
			switch (msg.what) {
				case SmsBackupMsg.MSG_BACKUP_START :
					break;

				case SmsBackupMsg.MSG_BACKUP_PROCEEDING :
					if (mListener != null
							&& (msg.arg1 == 0 || msg.arg1 == msg.arg2 || msg.arg1 % m10 == 0)) {
						String tips = mContext != null ? mContext.getString(
								R.string.progress_detail, msg.arg1, msg.arg2) : "";
						mListener.onProceeding((float) (msg.arg1) / (float) (msg.arg2),
								SmsBackupEntry.this, tips, null);
					}
					mTotalSmsCount = msg.arg2;
					break;

				case SmsBackupMsg.MSG_BACKUP_END :
					finish(true);
					break;

				case SmsBackupMsg.MSG_BACKUP_ERROR_OCCUR :
					finish(false);
					break;

				case SmsBackupMsg.MSG_SMS_COUNT_ZERO :
					finish(false);
					break;

				default :
					break;
			}
		}
	}

	public SmsBackupEntry(Context ctx) {
		super();
		mContext = ctx;
	}

	private void finish(boolean success) {
		BackupState state = success
				? BackupState.BACKUP_SUCCESSFUL
				: BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);

		if (success && mBackupArgs != null) {
			/*
			 * if(mBackupArgs.mConfig != null){
			 * mBackupArgs.mConfig.put(BackupPropertiesConfig
			 * .P_BACKUP_SMS_COUNT, String.valueOf(mTotalSmsCount)); }
			 */
			success = updateBackupDb(mBackupArgs.mDbHelper);
		}

		if (mListener != null) {
			mListener.onEnd(success, this, getSmsBackupFiles());
		}
		quitBackup();
	}

	private String[] getSmsBackupFiles() {
		String dir = Util.ensureFileSeparator(mBackupArgs.mBackupPath);
		return new String[] { dir + SMS_BACKUP_FILE_NAME };
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_SMS);
		cv.put(DataTable.DATA1, SMS_BACKUP_FILE_NAME);
		cv.put(DataTable.DATA2, mTotalSmsCount);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
		// String where = DataTable.MIME_TYPE + "=" +
		// MimetypeTable.MIMETYPE_VALUE_SMS;
		// if(dbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		//
		// }
		// return true;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}

		if (mThread == null) {
			mThread = new HandlerThread(mSmsBackupThreadName);
			mThread.start();
			mHandler = new BackupSmsThreadHandler(mThread.getLooper());
		}
		setState(BackupState.BACKUPING);
		mBackupArgs = (BackupArgs) data;
		mListener = listener;
		mListener.onStart(null, null);

		SmsBackup backup = new SmsBackup();
		SmsBackupArgs args = new SmsBackupArgs();
		args.mBackupFilePath = Util.ensureFileSeparator(mBackupArgs.mBackupPath)
				+ SMS_BACKUP_FILE_NAME;
		args.mNeedEncrypt = true;
		args.mEncryptPassword = Constant.getPassword();
		args.mHandler = mHandler;
		backup.backupSms(mContext, args);
		return true;
	}

	/**
	 * 获取本地短信个数
	 * 
	 * @param context
	 * @return
	 */
	public static int queryLocalSmsCount(Context context) {
		if (context == null) {
			return 0;
		}

		Cursor cursor = Sms.querySms(context, Sms.CONTENT_URI, null, Sms.TextBasedSmsColumns.TYPE
				+ " != '3'", null, null);
		if (cursor == null) {
			return 0;
		}

		final int count = cursor.getCount();
		cursor.close();
		cursor = null;
		return count;
	}

	private void quitBackup() {
		if (mThread != null && mThread.getLooper() != null) {
			mThread.getLooper().quit();
			mThread = null;
			mHandler = null;
		}
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_SMS;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.sms) : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = Util.loadIconFromPackageName(context, MMS_PACKAGE_NAME);
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
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(MMS_PACKAGE_NAME);
		final Drawable defaultDrawable = mContext.getResources().getDrawable(R.drawable.icon_sms);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}
}
