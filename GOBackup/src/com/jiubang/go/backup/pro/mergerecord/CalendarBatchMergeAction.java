package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.calendar.CalendarOperator;
import com.jiubang.go.backup.pro.calendar.CalendarOperator.CalendarStruct;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.CalendarBackupEntry;
import com.jiubang.go.backup.pro.data.CalendarRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 日历批量整合action
 *
 * @author wencan
 *
 */
public class CalendarBatchMergeAction extends BatchMergeAction {
	private Context mContext;
	private File mDescFile = null;
	private BackupDBHelper mDbHelper;
	private CalendarOperator mBeCalendarOperator;

	public CalendarBatchMergeAction(Context context, RestorableRecord beMergedRecord,
			BackupDBHelper dbHelper, IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mContext = context;
		mDbHelper = dbHelper;
		mDescFile = new File(mBeMergedRecord.getRecordRootDir(),
				CalendarBackupEntry.CALENDAR_FILE_NAME);
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new CalendarMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		String thisBackupFileMd5Code = MD5Util.getFileMd5Code(CalendarRestoreEntry
				.getOrginalFile(mBeMergedRecord.getRecordRootDir()));
		String otherBackupFileMd5Code = MD5Util.getFileMd5Code(CalendarRestoreEntry
				.getOrginalFile(toMergeRecord.getRecordRootDir()));
		if (otherBackupFileMd5Code == null) {
			// 该记录没有短信备份项，不必要合并
			return false;
		}

		// 与被合并的记录文件内容相同，不必要进行合并
		if (TextUtils.equals(thisBackupFileMd5Code, otherBackupFileMd5Code)) {
			return false;
		}
		for (MergeAction action : mMergeActions) {
			final String code = MD5Util.getFileMd5Code(CalendarRestoreEntry
					.getOrginalFile(action.mToMergeRecord.getRecordRootDir()));
			if (TextUtils.equals(code, otherBackupFileMd5Code)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onPreprocessing() {
		if (mBeMergedRecord == null) {
			return;
		}

		List<BaseRestoreEntry> beSmsEntrys = mBeMergedRecord
				.getEntriesByType(EntryType.TYPE_USER_CALENDAR);
		if (Util.isCollectionEmpty(beSmsEntrys)) {
			return;
		}

		File tempFile = new File(mDescFile.getParent(), "calendar.temp");
		if (!Util.decryptFile(mDescFile, tempFile, Constant.getPassword())) {
			return;
		}

		mBeCalendarOperator = new CalendarOperator(mContext, tempFile);

		if (tempFile.exists()) {
			tempFile.delete();
		}
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

		boolean result = true;
		if (changed) {
			if (mBeCalendarOperator == null || mBeCalendarOperator.getEventCount() == 0) {
				return false;
			}

			File tempFile = new File(mDescFile.getParent(), "calendar.temp");
			result = mBeCalendarOperator.persistCalendar(mContext, tempFile, null);
			if (result) {
				// 加密
				result = Util.encryFile(tempFile, mDescFile, Constant.getPassword());
			}
			
			if (tempFile.exists()) {
				tempFile.delete();
			}

			if (!result) {
				return false;
			}

			ContentValues cv = new ContentValues();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CALENDAR);
			cv.put(DataTable.DATA1, mDescFile.getName());
			cv.put(DataTable.DATA2, mBeCalendarOperator.getEventCount());
			cv.put(DataTable.DATA3, mBeCalendarOperator.getCalendarCount());
			cv.put(DataTable.DATA14, new Date().getTime());
			result = mDbHelper.reflashDatatable(cv);
		}

		release();
		return result;
	}

	private void release() {
		if (mBeCalendarOperator != null) {
			mBeCalendarOperator.release();
		}
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.calendar) : super
				.getDescription(context);
	}

	/**
	 * @author wencan
	 *
	 */
	private class CalendarMergeAction extends MergeAction {

		public CalendarMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			if (mBeMergedRecord == null) {
				return false;
			}

			List<BaseRestoreEntry> beSmsEntrys = mToMergeRecord
					.getEntriesByType(EntryType.TYPE_USER_CALENDAR);
			if (Util.isCollectionEmpty(beSmsEntrys)) {
				return false;
			}

			boolean ret = false;
			String toRecordRootDir = mToMergeRecord.getRecordRootDir();
			// 解密文件
			File tempFile = new File(toRecordRootDir, "calendar.temp");
			File srcFile = new File(toRecordRootDir, CalendarBackupEntry.CALENDAR_FILE_NAME);
			ret = Util.decryptFile(srcFile, tempFile, Constant.getPassword());
			if (!ret) {
				// 解密失败
				return false;
			}

			List<CalendarStruct> toCalendarList = CalendarOperator.loadCalendarFromFile(mContext, tempFile);
			// 删除临时文件
			if (tempFile.exists()) {
				tempFile.delete();
			}
			
			CalendarOperator toCo = new CalendarOperator(toCalendarList);
			if (toCo.getEventCount() == 0) {
				return false;
			}

			if (mBeCalendarOperator == null) {
				mBeCalendarOperator = toCo;
				ret = true;
			} else {
				ret = mBeCalendarOperator.mergeCalendar(toCalendarList);
			}


			return ret;
		}

		@Override
		public int getProgressWeight() {
			final int m20 = 20;
			return m20;
		}
	}
}
