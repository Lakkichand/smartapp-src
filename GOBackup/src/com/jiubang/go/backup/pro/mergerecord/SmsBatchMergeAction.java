package com.jiubang.go.backup.pro.mergerecord;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.SmsRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.sms.Sms;
import com.jiubang.go.backup.pro.sms.Sms.SmsStruct;
import com.jiubang.go.backup.pro.sms.SmsRestore;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 短信批量整合Action
 *
 * @author WenCan
 */
public class SmsBatchMergeAction extends BatchMergeAction {
	private Set<SmsStruct> mSmsStructs = null;
	private File mDescFile = null;
	private BackupDBHelper mDbHelper;

	public SmsBatchMergeAction(RestorableRecord beMergedRecord, BackupDBHelper dbHelper,
			IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mDescFile = new File(mBeMergedRecord.getRecordRootDir(),
				SmsRestoreEntry.SMS_RESTORE_FILE_NAME);
		mDbHelper = dbHelper;
	}

	public static boolean saveSmsDatFile(Set<SmsStruct> allSms, File file) {
		if (allSms == null || allSms.size() < 1 || file == null) {
			return false;
		}

		// 排序
		TreeSet<SmsStruct> smsStructs = new TreeSet<SmsStruct>(new SmsStruct.SmsComparator());
		smsStructs.addAll(allSms);

		FileOutputStream fos = null;
		DataOutputStream dataoutput = null;
		File tempFile = null;
		boolean ret = true;
		try {
			tempFile = new File(file.getParent(), "sms.temp");
			if (tempFile.exists()) {
				tempFile.delete();
			}

			fos = new FileOutputStream(tempFile);
			dataoutput = new DataOutputStream(fos);

			int smsCount = smsStructs.size();

			// 写入短信个数
			dataoutput.writeInt(smsCount);

			for (SmsStruct sms : smsStructs) {
//				 Log.d("GOBackup", "SmsBatchMergeAction = " + sms.toString());
				// 一短信短信备份所包含的字段个数
				dataoutput.writeInt(sms.getFieldCount());

				String columnName = null;
				int columnID = 0;

				Set<String> values = sms.getKeySets();
				Iterator<String> iterator = values.iterator();
				while (iterator.hasNext()) {
					columnName = iterator.next();

					if (columnName.equals(Sms.TextBasedSmsColumns.SMS_ID)) {
						columnID = Sms.SMS_ID;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.ADDRESS)) {
						columnID = Sms.SMS_ADDRESS;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.DATE)) {
						columnID = Sms.SMS_DATE;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.READ)) {
						columnID = Sms.SMS_READ;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.STATUS)) {
						columnID = Sms.SMS_STATUS;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.TYPE)) {
						columnID = Sms.SMS_TYPE;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT)) {
						columnID = Sms.SMS_REPLY_PATH_PRESENT;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.BODY)) {
						columnID = Sms.SMS_BODY;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.LOCKED)) {
						columnID = Sms.SMS_LOCK;
					}

					String value = sms.getValue(columnName);

					dataoutput.writeInt(columnID);
					dataoutput.writeUTF(value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
					fos = null;
				}
				if (dataoutput != null) {
					dataoutput.close();
					dataoutput = null;
				}
			} catch (Exception e) {
			}
			if (smsStructs != null) {
				smsStructs.clear();
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
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new SmsMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		String thisBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOriginalSmsBackupFile(mBeMergedRecord));
		String otherBackupFileMd5Code = MD5Util
				.getFileMd5Code(getOriginalSmsBackupFile(toMergeRecord));
		if (otherBackupFileMd5Code == null) {
			// 该记录没有短信备份项，不必要合并
			return false;
		}

		// 与被合并的记录文件内容相同，不必要进行合并
		if (TextUtils.equals(thisBackupFileMd5Code, otherBackupFileMd5Code)) {
			return false;
		}
		for (MergeAction action : mMergeActions) {
			final String code = MD5Util
					.getFileMd5Code(getOriginalSmsBackupFile(action.mToMergeRecord));
			if (TextUtils.equals(code, otherBackupFileMd5Code)) {
				return false;
			}
		}
		return true;
	}

	private File getOriginalSmsBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return new File(record.getRecordRootDir(), SmsRestoreEntry.SMS_RESTORE_FILE_NAME);
	}

	@Override
	protected void onPreprocessing() {
		if (mBeMergedRecord == null) {
			return;
		}

		List<BaseRestoreEntry> beSmsEntrys = mBeMergedRecord
				.getEntriesByType(EntryType.TYPE_USER_SMS);
		if (Util.isCollectionEmpty(beSmsEntrys)) {
			return;
		}

		mSmsStructs = SmsRestore.loadSmsFromDatFile(mBeMergedRecord.getRecordRootDir()
				+ SmsRestoreEntry.SMS_RESTORE_FILE_NAME, true, Constant.getPassword());
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
		boolean result = changed ? saveSmsDatFile(mSmsStructs, mDescFile) : true;
		// TODO 结果写入属性文件
		/*
		 * if(mBeMergedRecord != null &&
		 * mBeMergedRecord.getBackupPropertiesConfig() != null){
		 * BackupPropertiesConfig bpc =
		 * mBeMergedRecord.getBackupPropertiesConfig();
		 * bpc.put(BackupPropertiesConfig.P_BACKUP_SMS_COUNT,
		 * String.valueOf(mSmsStructs.size())); }
		 */

		result = true;
		String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_SMS;
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_SMS);
		cv.put(DataTable.DATA1, mDescFile.getName());
		cv.put(DataTable.DATA2, mSmsStructs.size());
		result = mDbHelper.reflashDatatable(cv);
		// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
		// result = mDbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		release();
		return result;
	}

	private void release() {
		if (mSmsStructs != null) {
			mSmsStructs.clear();
		}
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.sms) : super.getDescription(context);
	}

	/**
	 * 短信整合Action
	 *
	 * @author WenCan
	 */
	private class SmsMergeAction extends MergeAction {

		public SmsMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			if (mBeMergedRecord == null) {
				return false;
			}

			List<BaseRestoreEntry> beSmsEntrys = mToMergeRecord
					.getEntriesByType(EntryType.TYPE_USER_SMS);
			if (Util.isCollectionEmpty(beSmsEntrys)) {
				return false;
			}

			// Log.d("GOBackup", "SmsMergeAction : date = " +
			// mToMergeRecord.getDate());
			Set<SmsStruct> smsStructs = SmsRestore.loadSmsFromDatFile(
					mToMergeRecord.getRecordRootDir() + SmsRestoreEntry.SMS_RESTORE_FILE_NAME,
					true, Constant.getPassword());
			if (Util.isCollectionEmpty(smsStructs)) {
				return false;
			}

			if (Util.isCollectionEmpty(mSmsStructs)) {
				mSmsStructs = smsStructs;
				return true;
			}

			boolean change = false;
			for (SmsStruct sms : smsStructs) {
				if (!mSmsStructs.contains(sms)) {
					// Log.d("GOBackup", "sms = " + sms.toString());
					mSmsStructs.add(sms);
					change = true;
				}
			}

			smsStructs.clear();
			return change;
		}

		@Override
		public int getProgressWeight() {
			final int m30 = 30;
			return m30;
		}
	}
}
