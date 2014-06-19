package com.jiubang.go.backup.pro.mergerecord;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.CallLogRestoreEntry;
import com.jiubang.go.backup.pro.data.CallLogRestoreEntry.CallLogStruct;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 通话记录批量整合
 * 
 * @author ReyZhang
 */
public class CallLogBatchMergeAction extends BatchMergeAction {

	private BackupDBHelper mDbHelper;
	private Set<CallLogStruct> mCallLogStructs = null;
	private File mDescFile = null;

	public CallLogBatchMergeAction(RestorableRecord beMergedRecord, BackupDBHelper dbHelper,
			IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mDescFile = new File(mBeMergedRecord.getRecordRootDir(),
				CallLogRestoreEntry.CALLLOG_BACKUP_FILE_NAME);
		mDbHelper = dbHelper;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new CallLogMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		String thisBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOriginalCallLogBackupFile(mBeMergedRecord));
		String otherBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOriginalCallLogBackupFile(toMergeRecord));
		if (otherBackupFileMd5Code == null) {
			// 该记录没有通话记录备份项，不必要合并
			return false;
		}

		// 与被合并的记录文件内容相同，不必要进行合并
		if (TextUtils.equals(thisBackupFileMd5Code, otherBackupFileMd5Code)) {
			return false;
		}
		for (MergeAction action : mMergeActions) {
			final String code = MD5Util
					.getFileMd5Code(getOriginalCallLogBackupFile(action.mToMergeRecord));
			if (TextUtils.equals(code, otherBackupFileMd5Code)) {
				return false;
			}
		}
		return true;
	}

	private File getOriginalCallLogBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return new File(record.getRecordRootDir(), CallLogRestoreEntry.CALLLOG_BACKUP_FILE_NAME);
	}

	@Override
	protected void onPreprocessing() {
		if (mBeMergedRecord == null) {
			return;
		}

		List<BaseRestoreEntry> beMergeEntrys = mBeMergedRecord
				.getEntriesByType(EntryType.TYPE_USER_CALL_HISTORY);
		if (Util.isCollectionEmpty(beMergeEntrys)) {
			return;
		}
		mCallLogStructs = CallLogRestoreEntry.loadCallLogFromFile(mDescFile, true,
				Constant.getPassword());
	}

	@Override
	protected boolean onPostprocessing(boolean[] subActionResults) {
		if (mDbHelper == null) {
			return false;
		}

		boolean changed = false;
		if (subActionResults != null) {
			final int len = subActionResults.length;
			for (int i = 0; i < len; i++) {
				if (subActionResults[i]) {
					changed = true;
					break;
				}
			}
		}
		boolean result = changed ? saveCallLogToFile(mCallLogStructs, mDescFile) : true;
		// TODO 结果写入属性文件
		/*
		 * if(mBeMergedRecord != null &&
		 * mBeMergedRecord.getBackupPropertiesConfig() != null){
		 * BackupPropertiesConfig bpc =
		 * mBeMergedRecord.getBackupPropertiesConfig();
		 * bpc.put(BackupPropertiesConfig.P_BACKUP_CALLLOG_COUNT,
		 * String.valueOf(mCallLogStructs.size())); }
		 */

		// V2.0
		if (changed && result) {
			ContentValues cv = new ContentValues();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CALLLOG);
			cv.put(DataTable.DATA1, mDescFile.getName());
			cv.put(DataTable.DATA2, mCallLogStructs.size());
			// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
			// result = mDbHelper.insert(DataTable.TABLE_NAME, cv);
			// }
			result = mDbHelper.reflashDatatable(cv);
		}
		release();
		return result;
	}

	private void release() {
		if (mCallLogStructs != null) {
			mCallLogStructs.clear();
		}
	}

	private static boolean saveCallLogToFile(Set<CallLogStruct> callLogs, File file) {
		if (callLogs == null || file == null) {
			return false;
		}

		boolean ret = true;
		FileOutputStream trace = null;
		DataOutputStream dataoutput = null;
		File tempFile = null;

		try {
			tempFile = new File(file.getParent(), "calllog.temp");
			if (tempFile.exists()) {
				tempFile.delete();
			}

			trace = new FileOutputStream(tempFile);
			dataoutput = new DataOutputStream(trace);
			final int count = callLogs.size();
			dataoutput.writeInt(count); // 写入通话记录个数

			for (CallLogStruct callLog : callLogs) {
				final int columnCount = callLog.getFieldCount();
				// 写入列个数
				dataoutput.writeInt(columnCount);

				Set<String> keys = callLog.getKeySets();
				Iterator<String> iteratore = keys.iterator();
				if (iteratore != null) {
					while (iteratore.hasNext()) {
						String key = iteratore.next();
						String value = callLog.getValue(key);
						if (value == null) {
							value = "";
						}
						dataoutput.writeUTF(key);
						dataoutput.writeUTF(value);
					}
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
				if (dataoutput != null) {
					dataoutput.close();
					dataoutput = null;
				}
			} catch (Exception e) {
			}
		}

		if (ret) {
			// 加密
			ret = Util.encryFile(tempFile, file, Constant.getPassword());
		}

		// 删除临时文件
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}
		return ret;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.call_log) : super
				.getDescription(context);
	}

	/**
	 * 通话记录整合
	 * 
	 * @author WenCan
	 */
	private class CallLogMergeAction extends MergeAction {

		public CallLogMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			if (mBeMergedRecord == null) {
				return false;
			}

			List<BaseRestoreEntry> toCallLogEntrys = mToMergeRecord
					.getEntriesByType(EntryType.TYPE_USER_CALL_HISTORY);
			if (Util.isCollectionEmpty(toCallLogEntrys)) {
				return false;
			}

			Set<CallLogStruct> callLogStructs = CallLogRestoreEntry.loadCallLogFromFile(
					new File(mToMergeRecord.getRecordRootDir(),
							CallLogRestoreEntry.CALLLOG_BACKUP_FILE_NAME), true, Constant
							.getPassword());
			if (Util.isCollectionEmpty(callLogStructs)) {
				return false;
			}

			if (Util.isCollectionEmpty(mCallLogStructs)) {
				mCallLogStructs = callLogStructs;
				return true;
			}

			boolean change = false;
			for (CallLogStruct calllog : callLogStructs) {
				if (!mCallLogStructs.contains(calllog)) {
					mCallLogStructs.add(calllog);
					change = true;
				}
			}

			callLogStructs.clear();
			return change;
		}

		@Override
		public int getProgressWeight() {
			final int m10 = 10;
			return m10;
		}
	}
}
