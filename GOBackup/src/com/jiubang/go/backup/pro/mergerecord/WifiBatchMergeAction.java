package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord.RecordDescribe;
import com.jiubang.go.backup.pro.data.WifiBackupEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * wifi批量整合Action
 * 
 * @author WenCan
 */
public class WifiBatchMergeAction extends BatchMergeAction {
	private Date mLatestDate = null;
	private BackupDBHelper mDbHelper;

	public WifiBatchMergeAction(RestorableRecord beMergedRecord, BackupDBHelper dbHelper,
			IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		/*
		 * if(getWifiBackupOriginalFile(mBeMergedRecord) != null){ mLatestDate =
		 * mBeMergedRecord.getDate(); }
		 */
		mDbHelper = dbHelper;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		mLatestDate = record.getDate();
		return new WifiMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		if (getWifiBackupOriginalFile(toMergeRecord) == null) {
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

	private File getWifiBackupOriginalFile(RestorableRecord record) {
		File file = new File(record.getRecordRootDir(), WifiBackupEntry.WIFI_BACKUP_NAME);
		if (!file.exists()) {
			return null;
		}
		return file;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.wifi_access_points) : super
				.getDescription(context);
	}

	/**
	 * wifi整合Action
	 * 
	 * @author WenCan
	 */
	private class WifiMergeAction extends MergeAction {
		private static final int WEIGHT = 2;

		public WifiMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
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

		private boolean doMerge() {
			if (mDbHelper == null) {
				return false;
			}

			boolean ret = false;
			String toPath = mToMergeRecord.getRecordRootDir() + WifiBackupEntry.WIFI_BACKUP_NAME;
			String bePath = mBeMergedRecord.getRecordRootDir() + WifiBackupEntry.WIFI_BACKUP_NAME;
			ret = Util.copyFile(toPath, bePath);
			if (ret) {
				/*
				 * //V1.11之前版本通过属性文件保存属性 BackupPropertiesConfig toBcp =
				 * mToMergeRecord.getBackupPropertiesConfig();
				 * BackupPropertiesConfig beBcp =
				 * mBeMergedRecord.getBackupPropertiesConfig(); if(toBcp == null
				 * || beBcp == null){ deleteWifiFile(bePath); return false; }
				 * String toWifiPath =
				 * String.valueOf(toBcp.get(BackupPropertiesConfig
				 * .P_BACKUP_WIFI_PATH)); if(toWifiPath == null){
				 * deleteWifiFile(bePath); return false; }
				 * beBcp.put(BackupPropertiesConfig.P_BACKUP_WIFI_PATH,
				 * toWifiPath);
				 */

				// V2.0版本 通过数据库保存属性
				RecordDescribe toRd = mToMergeRecord.getRecordDescribe();
				if (toRd == null) {
					deleteWifiFile(bePath);
					return false;
				}

				if (toRd.mWifiPath == null) {
					deleteWifiFile(bePath);
					return false;
				}

				ret = true;
				String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_WIFI;
				ContentValues cv = new ContentValues();
				cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_WIFI);
				cv.put(DataTable.DATA1, WifiBackupEntry.WIFI_BACKUP_NAME);
				cv.put(DataTable.DATA2, toRd.mWifiPath);
				// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) ==
				// 0){
				// ret = mDbHelper.insert(DataTable.TABLE_NAME, cv);
				// }
				ret = mDbHelper.reflashDatatable(cv);
			}
			return ret;
		}

		private void deleteWifiFile(String wifiPath) {
			if (wifiPath == null) {
				return;
			}

			File file = new File(wifiPath);
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
