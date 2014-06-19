package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.UserDictionaryBackupEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryRestoreEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryRestoreEntry.WordStruct;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 用户字典批量整合Action
 * 
 * @author maiyongshen
 */
public class UserDictionaryBatchMergeAction extends BatchMergeAction {
	private Set<WordStruct> mMergedWords;
	private Context mContext;
	private BackupDBHelper mDbHelper;

	public UserDictionaryBatchMergeAction(Context context, RestorableRecord beMergedRecord,
			BackupDBHelper dbHelper, IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mContext = context;
		mDbHelper = dbHelper;
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		String beMergedRecordCode = MD5Util
				.getFileMd5Code(getDictionaryBackupFile(mBeMergedRecord));
		String toMergeRecordCode = MD5Util.getFileMd5Code(getDictionaryBackupFile(toMergeRecord));
		if (TextUtils.equals(beMergedRecordCode, toMergeRecordCode)) {
			return false;
		}
		for (MergeAction action : mMergeActions) {
			final String code = MD5Util
					.getFileMd5Code(getDictionaryBackupFile(action.mToMergeRecord));
			if (TextUtils.equals(code, toMergeRecordCode)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new DictionaryMergeAction(mBeMergedRecord, record, mListener);
	}

	private File getDictionaryBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return UserDictionaryRestoreEntry.getOrginalFile(new File(record.getRecordRootDir()));
	}

	private File getDecryptedBackupFile(RestorableRecord record) {
		if (record == null) {
			return null;
		}
		return UserDictionaryRestoreEntry.getDecryptedBackupFile(record.getRecordRootDir());
	}

	private void release() {
		if (mMergedWords != null) {
			mMergedWords.clear();
		}
	}

	@Override
	protected void onPreprocessing() {
		mMergedWords = UserDictionaryRestoreEntry
				.getAllWordsFromBackupFile(getDecryptedBackupFile(mBeMergedRecord));
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
		File savedFile = null;
		if (changed) {
			savedFile = UserDictionaryBackupEntry.saveWordsToFile(mMergedWords, new File(
					mBeMergedRecord.getRecordRootDir()));
			result = savedFile != null;
		}
		/*
		 * BackupPropertiesConfig config =
		 * mBeMergedRecord.getBackupPropertiesConfig(); if (config != null) {
		 * config.put(BackupPropertiesConfig.P_BACKUP_DICTIONAY_WORD_COUNT,
		 * String.valueOf(mMergedWords.size())); config.saveProper(mContext); }
		 */

		// V2.0
		String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY;
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_USER_DICTIONARY);
		if (savedFile != null) {
			cv.put(DataTable.DATA1, savedFile.getName());
		}
		cv.put(DataTable.DATA2, mMergedWords.size());
		result = mDbHelper.reflashDatatable(cv);
		// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
		// result = mDbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		release();
		return result;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.user_dictionary) : super
				.getDescription(context);
	}

	/**
	 * 用户字典整合Action
	 * 
	 * @author maiyongshen
	 */
	private class DictionaryMergeAction extends MergeAction {

		public DictionaryMergeAction(RestorableRecord beMergedRecord,
				RestorableRecord toMergeRecord, IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			if (mToMergeRecord == null) {
				return false;
			}
			File backupFile = UserDictionaryRestoreEntry.getDecryptedBackupFile(mToMergeRecord
					.getRecordRootDir());
			Set<WordStruct> toMergeWords = UserDictionaryRestoreEntry
					.getAllWordsFromBackupFile(backupFile);
			if (Util.isCollectionEmpty(toMergeWords)) {
				return false;
			}
			if (Util.isCollectionEmpty(mMergedWords)) {
				mMergedWords = toMergeWords;
				return true;
			}
			boolean changed = false;
			for (WordStruct word : toMergeWords) {
				if (!mMergedWords.contains(word)) {
					mMergedWords.add(word);
					changed = true;
				}
			}
			toMergeWords.clear();
			return changed;
		}

		@Override
		public int getProgressWeight() {
			final int m5 = 5;
			return m5;
		}
	}

}
