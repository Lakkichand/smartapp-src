package com.jiubang.go.backup.pro.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;
import android.provider.CallLog.Calls;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.EncryptDecrypt;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 通话记录备份
 *
 * @author kevin
 */
public class CallLogBackupEntry extends BaseBackupEntry {
	public static final String CALLLOG_PACKAGE_NAME = "com.android.phone";
	// 通话记录备份文件名
	public static final String CALLLOG_BACKUP_FILE_NAME = "CallLog.encrypt";
	// 通话记录备份加密密码
	private static final String CALLLOG_BACKUP_ENCRYPT_PASSWORD = "GO_BACKUP";
	private Context mContext = null;
	private static final String EMPTY_TEXT = "";
	private BackupArgs mBackupArgs = null;
	private int mTotalCallLogCount = 0;

	public CallLogBackupEntry(Context ctx) {
		super();
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
		mBackupArgs = (BackupArgs) data;
		setState(BackupState.BACKUPING);
		listener.onStart(null, null);
		boolean ret = backupInternal(ctx, (BackupArgs) data);
		/*
		 * if(ret && mBackupArgs != null && mBackupArgs.mConfig != null){
		 * mBackupArgs.mConfig.put(
		 * BackupPropertiesConfig.P_BACKUP_CALLLOG_COUNT,
		 * String.valueOf(mTotalCallLogCount));
		 * updateBackupDb(mBackupArgs.mDbHelper); }
		 */
		if (ret && mBackupArgs != null) {
			ret = updateBackupDb(mBackupArgs.mDbHelper);
		}
		listener.onEnd(ret, this, getCallLogBackupFiles());
		return true;
	}

	private String[] getCallLogBackupFiles() {
		String dir = Util.ensureFileSeparator(mBackupArgs.mBackupPath);
		return new String[] { dir + CALLLOG_BACKUP_FILE_NAME };
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}

		String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_CALLLOG;
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CALLLOG);
		cv.put(DataTable.DATA1, CALLLOG_BACKUP_FILE_NAME);
		cv.put(DataTable.DATA2, mTotalCallLogCount);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
		// if (dbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0) {
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		// return true;
	}

	private boolean backupInternal(Context ctx, BackupArgs args) {
		boolean ret = true;
		String backupRootPath = Util.ensureFileSeparator(args.mBackupPath);
		String callLogFullPath = backupRootPath + CALLLOG_BACKUP_FILE_NAME;
		File callLogFile = new File(callLogFullPath);
		File tempFile = new File(backupRootPath + "calllog.temp");
		try {
			ret = callLogFile.createNewFile();
			tempFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		// 写入数据

		ContentResolver cr = ctx.getContentResolver();
		String[] projection = new String[] { Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE,
				Calls.NEW, Calls.CACHED_NAME, Calls.CACHED_NUMBER_TYPE, Calls.CACHED_NUMBER_LABEL };

		Cursor cursor = null;
		try {
			cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, null, null,
					CallLog.Calls.DEFAULT_SORT_ORDER);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (cursor != null) {
			// 根据需求，如果通话记录为0，则按照失败处理（有可能是实际为空或者是第三方监控软件拒绝了授权引起）
			final int count = cursor.getCount();
			mTotalCallLogCount = cursor.getCount();
			if (count > 0) {
				ret = writeCallLogToFile(cursor, tempFile);
				// 加密
				if (ret) {
					EncryptDecrypt ed = new EncryptDecrypt();
					ret = ed.encrypt(tempFile, callLogFile, CALLLOG_BACKUP_ENCRYPT_PASSWORD);
				}
			} else {
				ret = false;
			}
			cursor.close();
			cursor = null;
		}

		// 删除temp文件
		if (tempFile != null) {
			tempFile.delete();
			tempFile = null;
		}
		if (!ret) {
			// 删除文件
			callLogFile.delete();
			setState(BackupState.BACKUP_ERROR_OCCURRED);
		} else {
			setState(BackupState.BACKUP_SUCCESSFUL);
		}
		return ret;
	}

	private boolean writeCallLogToFile(Cursor cursor, File file) {
		boolean ret = true;
		FileOutputStream trace = null;
		DataOutputStream dataoutput = null;
		try {
			trace = new FileOutputStream(file);
			dataoutput = new DataOutputStream(trace);
			final int count = cursor.getCount();
			dataoutput.writeInt(count); // 写入通话记录个数
			if (cursor.moveToFirst()) {
				do {
					String columnName = null;
					String columnValue = null;
					int columnCount = cursor.getColumnCount();
					dataoutput.writeInt(columnCount); // 写入列个数
					for (int i = 0; i < columnCount; i++) {
						columnName = cursor.getColumnName(i);
						columnValue = cursor.getString(i);
						if (columnValue == null) {
							columnValue = EMPTY_TEXT;
						}
						dataoutput.writeUTF(columnName);
						dataoutput.writeUTF(columnValue);
					}
				} while (cursor.moveToNext());
			}
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
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_CALL_HISTORY;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.call_log) : "";
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = Util.loadIconFromPackageName(context, CALLLOG_PACKAGE_NAME);
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

	/**
	 * 获取本地通话记录个数
	 *
	 * @param context
	 * @return
	 */
	public static int queryLocalCallLogCount(Context context) {
		if (context == null) {
			return 0;
		}
		int count = 0;
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null,
					CallLog.Calls.DEFAULT_SORT_ORDER);
			if (cursor == null) {
				return 0;
			}
			count = cursor.getCount();
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(CALLLOG_PACKAGE_NAME);
		final Drawable defaultDrawable = mContext.getResources().getDrawable(R.drawable.icon_call_log);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}
}
