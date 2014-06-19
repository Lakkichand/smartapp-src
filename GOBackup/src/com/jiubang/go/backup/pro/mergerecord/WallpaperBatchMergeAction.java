package com.jiubang.go.backup.pro.mergerecord;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.WallpaperBackupEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 桌面壁纸批量整合Action
 * 
 * @author WenCan
 */
public class WallpaperBatchMergeAction extends BatchMergeAction {
	private Date mLatestDate = null;
	private BackupDBHelper mDbHelper;

	public WallpaperBatchMergeAction(RestorableRecord beMergedRecord, BackupDBHelper dbHelper,
			IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mDbHelper = dbHelper;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		mLatestDate = record.getDate();
		return new WallpaperMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		if (!toMergeRecord.hasWallpaperEntry()) {
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
		return context != null ? context.getString(R.string.wallpaper) : super
				.getDescription(context);
	}

	/**
	 * 桌面壁纸整合Action
	 * 
	 * @author WenCan
	 */
	private class WallpaperMergeAction extends MergeAction {
		private static final int WEIGHT = 2;

		public WallpaperMergeAction(RestorableRecord beMergedRecord,
				RestorableRecord toMergeRecord, IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			return doMerge();
		}

		private boolean doMerge() {
			if (mDbHelper == null) {
				return false;
			}

			boolean ret = false;
			String toPath = mToMergeRecord.getRecordRootDir()
					+ WallpaperBackupEntry.WALLPAPER_FILE_NAME;
			String bePath = mBeMergedRecord.getRecordRootDir()
					+ WallpaperBackupEntry.WALLPAPER_FILE_NAME;
			ret = Util.copyFile(toPath, bePath);
			if (ret) {
				String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_WALLPAPER;
				ContentValues cv = new ContentValues();
				cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_WALLPAPER);
				cv.put(DataTable.DATA1, WallpaperBackupEntry.WALLPAPER_FILE_NAME);
				// TODO
				// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) ==
				// 0){
				// ret = mDbHelper.insert(DataTable.TABLE_NAME, cv);
				// }
				ret = mDbHelper.reflashDatatable(cv);
			}
			return ret;
		}

		@Override
		public int getProgressWeight() {
			return WEIGHT;
		}
	}
}
