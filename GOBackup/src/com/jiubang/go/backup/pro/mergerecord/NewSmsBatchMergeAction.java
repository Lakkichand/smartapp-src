package com.jiubang.go.backup.pro.mergerecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
 * @author jiangpeihe 新的短信整合模块
 */
public class NewSmsBatchMergeAction extends BatchMergeAction {
	private Set<NewSmsStruct> mNewSmsStructs = null;
	private File mDescFile = null;
	private BackupDBHelper mDbHelper;
	private static final String FINAL_TEMP_FILE_NAME = "finalsms.temp";
	private static String sRootCachePath = null;

	public NewSmsBatchMergeAction(RestorableRecord beMergedRecord,
			BackupDBHelper dbHelper, IAsyncTaskListener listener,
			Context context) {
		super(beMergedRecord, listener);
		mDescFile = new File(mBeMergedRecord.getRecordRootDir(),
				SmsRestoreEntry.SMS_RESTORE_FILE_NAME);
		mDbHelper = dbHelper;
		sRootCachePath = Constant.buildNetworkBackupCacheDir(context);
		File cacheDir = new File(sRootCachePath);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	public static boolean saveTempSmsToDatFile(int count, File file) {
		if (count < 1 || file == null) {
			return false;
		}
		// 未排序之前，短信个数
		int smsCount = count;
		Set<SmsStruct> allSmsStructsFromTempFile = SmsRestore
				.loadAllSmsSetFromTempFile(file, new File(sRootCachePath,
						FINAL_TEMP_FILE_NAME), smsCount);
		if (allSmsStructsFromTempFile == null
				|| allSmsStructsFromTempFile.isEmpty()) {
			return false;
		}

		// // 排序
		// TreeSet<SmsStruct> smsStructs = new TreeSet<SmsStruct>(new
		// SmsStruct.SmsComparator());
		// if (allSmsStructsFromTempFile != null) {
		// smsStructs.addAll(allSmsStructsFromTempFile);
		// allSmsStructsFromTempFile.clear();
		// allSmsStructsFromTempFile = null;
		// }
		// 排序去重之后短信个数
		smsCount = allSmsStructsFromTempFile.size();
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

			// 写入短信个数
			dataoutput.writeInt(smsCount);
			for (SmsStruct sms : allSmsStructsFromTempFile) {
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
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.ADDRESS)) {
						columnID = Sms.SMS_ADDRESS;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.DATE)) {
						columnID = Sms.SMS_DATE;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.READ)) {
						columnID = Sms.SMS_READ;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.STATUS)) {
						columnID = Sms.SMS_STATUS;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.TYPE)) {
						columnID = Sms.SMS_TYPE;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT)) {
						columnID = Sms.SMS_REPLY_PATH_PRESENT;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.BODY)) {
						columnID = Sms.SMS_BODY;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.LOCKED)) {
						columnID = Sms.SMS_LOCK;
					}

					String value = sms.getValue(columnName);
					dataoutput.writeInt(columnID);
					dataoutput.writeUTF(value);
				}
				values.clear();
				values = null;
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
			allSmsStructsFromTempFile.clear();
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
		return new NewSmsMergeAction(mBeMergedRecord, record, mListener);
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
		return new File(record.getRecordRootDir(),
				SmsRestoreEntry.SMS_RESTORE_FILE_NAME);
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

		mNewSmsStructs = SmsRestore.firstloadSmsFromDatFile(
				mBeMergedRecord.getRecordRootDir()
						+ SmsRestoreEntry.SMS_RESTORE_FILE_NAME,
				sRootCachePath, true, Constant.getPassword());
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
		int smscount = 0;
		if (mNewSmsStructs != null) {
			smscount = mNewSmsStructs.size();
			mNewSmsStructs.clear();
			mNewSmsStructs = null;
		}

		boolean result = changed ? saveTempSmsToDatFile(smscount, mDescFile)
				: true;
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
		String where = DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_SMS;
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_SMS);
		cv.put(DataTable.DATA1, mDescFile.getName());
		cv.put(DataTable.DATA2, smscount);
		result = mDbHelper.reflashDatatable(cv);
		release();
		return result;
	}

	private void release() {
		// if (mSmsStructs != null) {
		// mSmsStructs.clear();
		// }
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.sms) : super
				.getDescription(context);
	}

	/**
	 * 短信整合Action
	 * 
	 * @author WenCan
	 */
	private class NewSmsMergeAction extends MergeAction {

		public NewSmsMergeAction(RestorableRecord beMergedRecord,
				RestorableRecord toMergeRecord, IAsyncTaskListener listener) {
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
			mergeOneSmsDatFile(mNewSmsStructs,
					mToMergeRecord.getRecordRootDir()
							+ SmsRestoreEntry.SMS_RESTORE_FILE_NAME, true,
					Constant.getPassword());
			return true;
		}

		@Override
		public int getProgressWeight() {
			final int m30 = 30;
			return m30;
		}
	}

	/**
	 * @author jiangpeihe 用来标志一条短信
	 */
	public static class NewSmsStruct {
		public String mAdress;
		public String mDate;

		@Override
		public int hashCode() {
			return mAdress.hashCode() + mDate.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof NewSmsStruct)) {
				return false;
			}
			NewSmsStruct sms = (NewSmsStruct) o;
			boolean address = mAdress.equals(sms.mAdress);
			boolean date = mDate.equals(sms.mDate);
			return address && date;
		}
	}

	public void mergeOneSmsDatFile(Set<NewSmsStruct> newSmsStructs,
			String path, boolean b, String password) {

		if (path == null || !new File(path).exists()) {
			return;
		}
		File oraginalSmsFile = new File(path);
		File tempFile = new File(oraginalSmsFile.getParent(),
				SmsRestore.TEMP_FILE_NAME);
		boolean success = SmsRestore.getSmsDatFile(oraginalSmsFile, tempFile,
				b, password);
		if (!success) {
			return;
		}
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			dis = new DataInputStream(new FileInputStream(tempFile));
			if (newSmsStructs != null) {
				dos = new DataOutputStream(new FileOutputStream(new File(
						sRootCachePath, FINAL_TEMP_FILE_NAME), true));
			} else {
				mNewSmsStructs = new HashSet<NewSmsStruct>();
				dos = new DataOutputStream(new FileOutputStream(new File(
						sRootCachePath, FINAL_TEMP_FILE_NAME)));
			}
			// 文件中短信总个数
			int smsCount = dis.readInt();
			for (int i = 0; i < smsCount; i++) {
				SmsStruct sms = new SmsStruct();
				NewSmsStruct newSms = new NewSmsStruct();
				int columncount = dis.readInt();
				for (int j = 0; j < columncount; j++) {
					int columnName = dis.readInt();
					String colvalue = dis.readUTF();

					String column = null;
					switch (columnName) {
					case Sms.SMS_ID:
						column = Sms.TextBasedSmsColumns.SMS_ID;
						break;
					case Sms.SMS_ADDRESS:
						column = Sms.TextBasedSmsColumns.ADDRESS;
						newSms.mAdress = colvalue;
						break;
					case Sms.SMS_DATE:
						column = Sms.TextBasedSmsColumns.DATE;
						newSms.mDate = colvalue;
						break;
					case Sms.SMS_READ:
						column = Sms.TextBasedSmsColumns.READ;
						break;
					case Sms.SMS_STATUS:
						column = Sms.TextBasedSmsColumns.STATUS;
						break;
					case Sms.SMS_TYPE:
						column = Sms.TextBasedSmsColumns.TYPE;
						break;
					case Sms.SMS_REPLY_PATH_PRESENT:
						column = Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT;
						break;
					case Sms.SMS_BODY:
						column = Sms.TextBasedSmsColumns.BODY;
						break;
					case Sms.SMS_LOCK:
						column = Sms.TextBasedSmsColumns.LOCKED;
						break;
					case Sms.SMS_THREAD_ID:
						// column = Sms.TextBasedSmsColumns.THREAD_ID;
						break;
					default:
						break;
					}
					if (column == null) {
						continue;
					} else {
						sms.putFiled(column, colvalue);
					}
				}
				if (mNewSmsStructs.contains(newSms)) {
					continue;
				}
				mNewSmsStructs.add(newSms);
				dos.writeInt(sms.getFieldCount());
				String columnName = null;
				int columnID = 0;

				Set<String> values = sms.getKeySets();
				Iterator<String> iterator = values.iterator();
				while (iterator.hasNext()) {
					columnName = iterator.next();
					if (columnName.equals(Sms.TextBasedSmsColumns.SMS_ID)) {
						columnID = Sms.SMS_ID;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.ADDRESS)) {
						columnID = Sms.SMS_ADDRESS;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.DATE)) {
						columnID = Sms.SMS_DATE;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.READ)) {
						columnID = Sms.SMS_READ;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.STATUS)) {
						columnID = Sms.SMS_STATUS;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.TYPE)) {
						columnID = Sms.SMS_TYPE;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT)) {
						columnID = Sms.SMS_REPLY_PATH_PRESENT;
					} else if (columnName.equals(Sms.TextBasedSmsColumns.BODY)) {
						columnID = Sms.SMS_BODY;
					} else if (columnName
							.equals(Sms.TextBasedSmsColumns.LOCKED)) {
						columnID = Sms.SMS_LOCK;
					}

					String value = sms.getValue(columnName);
					dos.writeInt(columnID);
					dos.writeUTF(value);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 删除临时文件
			try {
				if (tempFile.exists()) {
					tempFile.delete();
				}
				if (dis != null) {
					dis.close();
					dis = null;
				}
				if (dos != null) {
					dos.close();
					dos = null;
				}
			} catch (Exception e) {

			}
		}
	}

}
