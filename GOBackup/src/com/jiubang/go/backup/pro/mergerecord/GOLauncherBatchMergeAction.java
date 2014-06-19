package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.GoLauncherSettingRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * Go桌面设置批量整合
 * 
 * @author WenCan
 */
public class GOLauncherBatchMergeAction extends BatchMergeAction {

	private static String GO_LAUNCHER_DB_NAME = "androidheart.db";
	private Date mLatestDate = null;
	private BackupDBHelper mDbHelper;

	public GOLauncherBatchMergeAction(RestorableRecord beMergedRecord, BackupDBHelper dbHelper,
			IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		/*
		 * boolean beMergeExist =
		 * GoLauncherSettingRestoreEntry.getRandomFile(mBeMergedRecord
		 * .getRecordRootDir()) != null ? true : false; if(beMergeExist){
		 * mLatestDate = mBeMergedRecord.getDate(); }
		 */
		mDbHelper = dbHelper;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		mLatestDate = record.getDate();
		return new GoLauncherMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		boolean toMergeExist = GoLauncherSettingRestoreEntry.hasOrginalFile(toMergeRecord
				.getRecordRootDir());
		if (!toMergeExist) {
			return false;
		}

		if (mLatestDate == null) {
			clear();
			return true;
		}

		if (mLatestDate.compareTo(toMergeRecord.getDate()) >= 0) {
			// 合并记录的记录比待合并记录新，不用更新合并
			return false;
		}

		clear();
		return true;
	}

	@Override
	protected void onPreprocessing() {
		super.onPreprocessing();
	}

	@Override
	protected boolean onPostprocessing(boolean[] subActionResults) {
		boolean ret = true;
		final int count = subActionResults.length;
		for (int i = 0; i < count; i++) {
			if (!subActionResults[i]) {
				ret = false;
				break;
			}
		}
		return ret;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.golauncher_setting) : super
				.getDescription(context);
	}

	/**
	 * author WenCan
	 */
	private class GoLauncherMergeAction extends MergeAction {

		private static final int WEIGHT = 2;

		public GoLauncherMergeAction(RestorableRecord beMergedRecord,
				RestorableRecord toMergeRecord, IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			return doMerge();
		}

		@Override
		public int getProgressWeight() {
			return WEIGHT;
		}

		private void deleteOldFile(File oldFile) {
			if (oldFile == null) {
				return;
			}
			if (oldFile.exists()) {
				oldFile.delete();
			}
		}

		private boolean doMerge() {
			if (mDbHelper == null) {
				return false;
			}
			String toDbPath = mToMergeRecord.getRecordRootDir() + GO_LAUNCHER_DB_NAME;
			String beDbPath = mBeMergedRecord.getRecordRootDir() + GO_LAUNCHER_DB_NAME;
			File toRandomFile = GoLauncherSettingRestoreEntry.getRandomFile(mToMergeRecord
					.getRecordRootDir());
			File beRandomFile = GoLauncherSettingRestoreEntry.getRandomFile(mBeMergedRecord
					.getRecordRootDir());
			if (toRandomFile == null || toDbPath == null) {
				// Log.d("GOBackup", "doMerge : toRandomFile = " + toRandomFile
				// + ", toDbpath = " + toDbPath);
				return false;
			}

			deleteOldFile(new File(beDbPath));
			deleteOldFile(beRandomFile);

			// 拷贝数据库文件
			if (!Util.copyFile(toDbPath, beDbPath)) {
				// Log.d("GOBackup",
				// "doMerge : copyFile db : faild, todbpath = " + toDbPath +
				// ", bedbpath = " + beDbPath);
				return false;
			}

			// 拷贝随机数文件 TODO
			String beRandomPath = mBeMergedRecord.getRecordRootDir() + toRandomFile.getName();
			if (!Util.copyFile(toRandomFile.getAbsolutePath(), beRandomPath)) {
				// Log.d("GOBackup",
				// "doMerge : copyFile randomfile: faild, torandomFile = " +
				// toRandomFile + ", berandomPath = " + beRandomPath);
				return false;
			}

			// 更新数据库
			ContentValues cv = new ContentValues();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING);
			cv.put(DataTable.DATA1, toRandomFile.getName());
			// TODO 确保GO桌面备份的文件名字
			cv.put(DataTable.DATA2, "androidheart.db");
			return mDbHelper.reflashDatatable(cv);
			// if(mDbHelper.update(DataTable.TABLE_NAME, cv, DataTable.MIME_TYPE
			// + "=" + MimetypeTable.MIMETYPE_VALUE_GOLAUNCHER_SETTING, null) ==
			// 0){
			// return mDbHelper.insert(DataTable.TABLE_NAME, cv);
			// }
			// return true;
		}
	}
}
