package com.jiubang.go.backup.pro.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.CallLog;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 通话记录恢复
 * 
 * @author kevin
 */
public class CallLogRestoreEntry extends BaseRestoreEntry {
	private static final String CALLLOG_PACKAGE_NAME = "com.android.phone";
	// 通话记录备份文件名
	public static final String CALLLOG_BACKUP_FILE_NAME = "CallLog.encrypt";

	private final Context mContext;
	private final Object mLock = new Object();
	private boolean mStopFlag = false;
	private String mRecordDir;
	// test
	long mDt;

	public CallLogRestoreEntry(Context ctx, String recordDir) {
		mContext = ctx;
		mRecordDir = recordDir;

		if (getOrginalFile(mRecordDir) != null) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static File getOrginalFile(String recordDir) {
		if (recordDir == null) {
			return null;
		}

		File callLogFile = new File(recordDir, CallLogRestoreEntry.CALLLOG_BACKUP_FILE_NAME);
		if (callLogFile.exists()) {
			return callLogFile;
		}
		return null;
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
		listener.onStart(CallLogRestoreEntry.this, null);
		// long totaldt = System.currentTimeMillis();
		boolean ret = restoreInternal(context, (RestoreArgs) data, listener);
		// Log.d("GOBackup", "CallLogRestoreEntry : restore : totaldt = " +
		// (System.currentTimeMillis() - totaldt));
		listener.onEnd(ret, CallLogRestoreEntry.this, null);
		return true;
	}

	private static boolean getCallLogFile(File srcFile, File desFile, boolean encrypt,
			String password) {
		if (srcFile == null || desFile == null) {
			return false;
		}

		if (!srcFile.exists()) {
			return false;
		}

		boolean ret = false;
		if (!encrypt) {
			ret = Util.copyFile(srcFile.getAbsolutePath(), desFile.getAbsolutePath());
		} else {
			ret = Util.decryptFile(srcFile, desFile, password);
		}
		return ret;
	}

	private boolean restoreInternal(Context ctx, RestoreArgs args, IAsyncTaskListener listener) {
		boolean ret = true;
		String restoreRootPath = args.mRestorePath;
		if (!restoreRootPath.endsWith(File.separator)) {
			restoreRootPath += File.separator;
		}
		String restoreFullPath = restoreRootPath + CALLLOG_BACKUP_FILE_NAME;
		File callLogFile = new File(restoreFullPath);
		File tempFile = new File(restoreRootPath + "calllog.temp");
		if (!callLogFile.exists()) {
			// 不存在通话记录文件
			setState(RestoreState.RESTORE_ERROR_OCCURRED);
			return false;
		}

		// 解密
		// dt = System.currentTimeMillis();
		// EncryptDecrypt ed = new EncryptDecrypt();
		// ret = ed.decrypt(callLogFile, tempFile, Constant.getPassword());
		ret = Util.decryptFile(callLogFile, tempFile, Constant.getPassword());
		// Log.d("GOBackup", "CallLogRestoreEntry : restore : 解密  : dt = " +
		// (System.currentTimeMillis() - dt));

		if (ret) {
			// 解密成功
			ret = writeCallLogToDbFromFile(ctx, tempFile, ctx.getContentResolver(), listener);
		}

		// 删除解密出来的临时文件
		if (tempFile != null) {
			tempFile.delete();
		}
		if (ret) {
			setState(RestoreState.RESTORE_SUCCESSFUL);
		} else {
			synchronized (mLock) {
				if (mStopFlag) {
					setState(RestoreState.RESTORE_CANCELED);
				} else {
					setState(RestoreState.RESTORE_ERROR_OCCURRED);
				}
			}
		}
		return ret;
	}

	public static int mGetCallLogItemCount(String parentDir) {
		if (parentDir == null) {
			return 0;
		}
		int count = 0;
		String dir = Util.ensureFileSeparator(parentDir);
		File srcFile = new File(dir, CALLLOG_BACKUP_FILE_NAME);
		File tempFile = new File(dir, "callLog.temp");
		if (Util.decryptFile(srcFile, tempFile, Constant.getPassword())) {
			count = getCallLogItemCountInternal(tempFile);
		}

		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}
		return count;
	}

	private static int getCallLogItemCountInternal(File srcFile) {
		FileInputStream trace = null;
		DataInputStream datainput = null;
		int count = 0;
		try {
			trace = new FileInputStream(srcFile);
			datainput = new DataInputStream(trace);
			count = datainput.readInt();
		} catch (Exception e) {
			e.printStackTrace();
			count = 0;
		} finally {
			try {
				if (trace != null) {
					trace.close();
					trace = null;
				}
				if (datainput != null) {
					datainput.close();
					datainput = null;
				}
			} catch (Exception e) {
			}
		}
		return count;
	}

	private boolean writeCallLogToDbFromFile(Context context, File file, ContentResolver cr,
			IAsyncTaskListener listener) {
		boolean ret = true;
		FileInputStream trace = null;
		DataInputStream datainput = null;
		Cursor callLogCursor = null;
		final int m10 = 10;
		try {
			// 查询所有通话记录，在内存中匹配，提高速度
			callLogCursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null,
					CallLog.Calls.DEFAULT_SORT_ORDER);

			trace = new FileInputStream(file);
			datainput = new DataInputStream(trace);
			ContentValues cv = new ContentValues();
			// 通话记录个数
			final int callLogCount = datainput.readInt();
			for (int i = 0; i < callLogCount; i++) {
				synchronized (mLock) {
					if (mStopFlag) {
						// 停止恢复
						ret = false;
						break;
					}
				}

				if (listener != null && ((i + 1) % m10 == 0 || i == callLogCount - 1)) {
					String tips = mContext != null ? mContext.getString(R.string.progress_detail,
							i + 1, callLogCount) : "";
					listener.onProceeding((float) i / (float) callLogCount, this, tips, null);
				}
				// 列个数
				int columnCount = datainput.readInt();

				String columnName = null;
				String columnValue = null;
				for (int j = 0; j < columnCount; j++) {
					columnName = datainput.readUTF();
					columnValue = datainput.readUTF();
					cv.put(columnName, columnValue);
				}
				// 查询是否已经存在通话记录
				String number = cv.getAsString(CallLog.Calls.NUMBER);
				String date = cv.getAsString(CallLog.Calls.DATE);
				if (number == null) {
					number = "";
				}
				if (date == null) {
					date = "";
				}

				mDt = System.currentTimeMillis();
				if (!isExistCallLog(callLogCursor, number, date)) {
					// Log.d("GOBackup",
					// "CallLogRestoreEntry : restore : 查询通话记录 : dt = " +
					// (System.currentTimeMillis() - dt));
					mDt = System.currentTimeMillis();
					// 不存在,则插入
					cr.insert(CallLog.Calls.CONTENT_URI, cv);
					// Log.d("GOBackup",
					// "CallLogRestoreEntry : restore : 插入通话记录 : " + i +
					// ", dt = " + (System.currentTimeMillis() - dt));
				} else {
					continue;
				}
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
				if (datainput != null) {
					datainput.close();
					datainput = null;
				}
			} catch (Exception e) {
			}
			if (callLogCursor != null) {
				callLogCursor.close();
				callLogCursor = null;
			}
		}
		return ret;
	}

	private boolean isExistCallLog(Cursor callLogCursor, String number, String date) {
		if (callLogCursor == null || number == null || date == null) {
			return false;
		}
		if (callLogCursor.getCount() < 1) {
			return false;
		}
		if (callLogCursor.moveToFirst()) {
			do {
				try {
					String tempNumber = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.NUMBER));
					String tempDate = callLogCursor.getString(callLogCursor
							.getColumnIndex(CallLog.Calls.DATE));
					if (number.equals(tempNumber) && date.equals(tempDate)) {
						return true;
					}
				} catch (Exception e) {
					continue;
				}
			} while (callLogCursor.moveToNext());
		}
		return false;
	}

	@Override
	public void stopRestore() {
		synchronized (mLock) {
			mStopFlag = true;
		}
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

	public static Set<CallLogStruct> loadCallLogFromFile(File file, boolean encrypt, String password) {
		if (file == null) {
			return null;
		}
		if (encrypt && password == null) {
			return null;
		}

		File tempFile = new File(file.getParent() + "temp.calllog");
		if (!getCallLogFile(file, tempFile, encrypt, password)) {
			return null;
		}

		FileInputStream fis = null;
		DataInputStream dis = null;
		Set<CallLogStruct> callLogs = null;
		try {
			fis = new FileInputStream(tempFile);
			dis = new DataInputStream(fis);

			// 通话记录个数
			final int callLogCount = dis.readInt();
			if (callLogCount > 0) {
				callLogs = new HashSet<CallLogStruct>();
			}

			for (int i = 0; i < callLogCount; i++) {
				CallLogStruct callLog = new CallLogStruct();
				// 列个数
				int columnCount = dis.readInt();

				String columnName = null;
				String columnValue = null;
				for (int j = 0; j < columnCount; j++) {
					columnName = dis.readUTF();
					columnValue = dis.readUTF();

					// 充值new字段
					if (columnName.equals(CallLog.Calls.NEW)) {
						columnValue = "0";
					}

					callLog.putFiled(columnName, columnValue);
				}
				callLogs.add(callLog);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dis != null) {
					dis.close();
					dis = null;
				}
				if (fis != null) {
					fis.close();
					fis = null;
				}
				if (tempFile != null && tempFile.exists()) {
					tempFile.delete();
					tempFile = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return callLogs;
	}

	/**
	 * 通话记录结构
	 * 
	 * @author wencan
	 */
	public static class CallLogStruct {
		HashMap<String, String> mData;

		public CallLogStruct() {
			mData = new HashMap<String, String>();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof CallLogStruct)) {
				return false;
			}

			CallLogStruct callLog = (CallLogStruct) o;
			return mData.equals(callLog.mData);
		}

		@Override
		public int hashCode() {
			return mData.hashCode();
		}

		public void putFiled(String key, String value) {
			mData.put(key, value);
		}

		public String getFiled(String key) {
			return mData.get(key);
		}

		public int getFieldCount() {
			return mData != null ? mData.size() : 0;
		}

		public Set<String> getKeySets() {
			return mData.keySet();
		}

		public String getValue(String key) {
			return mData.get(key);
		}

		@Override
		public String toString() {
			return mData.toString();
		}
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		final DrawableKey key = DrawableProvider.buildDrawableKey(CALLLOG_PACKAGE_NAME);
		final Drawable defaultDrawable = mContext.getResources().getDrawable(
				R.drawable.icon_call_log);
		return DrawableProvider.getInstance().getDrawable(context, key, defaultDrawable, listener);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_CALLLOG, null);
		File contactLogFile = new File(recordRootPah, CallLogBackupEntry.CALLLOG_BACKUP_FILE_NAME);
		if (contactLogFile.exists()) {
			contactLogFile.delete();
		}
	}
}
