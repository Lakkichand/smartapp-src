package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.LauncherDataBackupEntry;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry.LauncherDataExtraInfo;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 桌面数据批量整合
 * 
 * @author WenCan
 */
public class LauncherDataBatchMergeAction extends BatchMergeAction {
	private BackupDBHelper mDbHelper;
	private Date mLatestDate = null;

	public LauncherDataBatchMergeAction(RestorableRecord beMergedRecord,
			IAsyncTaskListener listener, BackupDBHelper dbHelper) {
		super(beMergedRecord, listener);
		mDbHelper = dbHelper;
		/*
		 * if(getLauncherPackageName(mBeMergedRecord) != null){ mLatestDate =
		 * mBeMergedRecord.getDate(); }
		 */
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		mLatestDate = record.getDate();
		return new LauncherDataMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		String toLauncherPackageName = getLauncherPackageName(toMergeRecord);
		if (toLauncherPackageName == null) {
			return false;
		}

		if (mLatestDate == null) {
			clear();
			return true;
		}

		if (mLatestDate.compareTo(toMergeRecord.getDate()) >= 0) {
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

	private String getLauncherPackageName(RestorableRecord record) {
		return LauncherDataRestoreEntry.getLauncherDataPackageName(record.getRecordRootDir());
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.launcher_layout) : super
				.getDescription(context);
	}

	/**
	 * 桌面数据整合
	 * 
	 * @author WenCan
	 */
	private class LauncherDataMergeAction extends MergeAction {
		private String mBePackageName = null;
		private String mToPackageName = null;

		public LauncherDataMergeAction(RestorableRecord beMergedRecord,
				RestorableRecord toMergeRecord, IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
			mBePackageName = getLauncherPackageName(mBeMergedRecord);
			mToPackageName = getLauncherPackageName(mToMergeRecord);
		}

		@Override
		public boolean execute() {
			return doMerge();
		}

		@Override
		public int getProgressWeight() {
			return 2;
		}

		private void deleteOldResource() {
			final String recordDir = mBeMergedRecord.getRecordRootDir();
			File appWidgetsFile = new File(recordDir, LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
			if (appWidgetsFile.exists()) {
				appWidgetsFile.delete();
			}

			File wallpaperFile = new File(recordDir, LauncherDataBackupEntry.WALLPAPER_FILE_NAME);
			if (wallpaperFile.exists()) {
				wallpaperFile.delete();
			}

			File allLauncherFile = LauncherDataRestoreEntry.getLauncherDataBackupFile(recordDir);
			if (allLauncherFile != null && allLauncherFile.exists()) {
				allLauncherFile.delete();
			}

			if (mBePackageName != null) {
				File dataFile = new File(recordDir, mBePackageName + ".tar.gz");
				if (dataFile.exists()) {
					dataFile.delete();
				}

				if (mDbHelper != null) {
					String where = DataTable.DATA1 + "='" + mBePackageName + "' AND "
							+ DataTable.MIME_TYPE + " = "
							+ MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA;
					mDbHelper.delete(DataTable.TABLE_NAME, where, null);
				}
			}
		}

		private LauncherDataExtraInfo getLauncherDataExtraInfo(RestorableRecord record,
				String packageName) {
			if (record == null || packageName == null) {
				return null;
			}

			List<BaseRestoreEntry> launcherDataRestoreEntrys = record
					.getEntriesByType(EntryType.TYPE_SYSTEM_LAUNCHER_DATA);
			if (Util.isCollectionEmpty(launcherDataRestoreEntrys)) {
				return null;
			}

			LauncherDataRestoreEntry entry = (LauncherDataRestoreEntry) launcherDataRestoreEntrys
					.get(0);
			LauncherDataExtraInfo info = entry.getLauncherDataExtraInfo();
			if (info == null) {
				return null;
			}

			if (info.packageName.equals(packageName)) {
				return info;
			}
			return null;
		}

		private boolean doMerge() {
			if (mToPackageName == null) {
				return false;
			}

			if (mDbHelper == null) {
				return false;
			}

			// 删除原来的备份文件
			deleteOldResource();

			final String toRecordDir = mToMergeRecord.getRecordRootDir();
			final String beRecordDir = mBeMergedRecord.getRecordRootDir();

			// 拷贝appwidget文件
			boolean ret = false;
			ret = Util.copyFile(toRecordDir + LauncherDataBackupEntry.APP_WIDGET_FILE_NAME,
					beRecordDir + LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
			if (!ret) {
				return false;
			}

			// 拷贝壁纸文件
			boolean wallpaperExist = Util.copyFile(toRecordDir
					+ LauncherDataBackupEntry.WALLPAPER_FILE_NAME, beRecordDir
					+ LauncherDataBackupEntry.WALLPAPER_FILE_NAME);

			// 拷贝data文件
			LauncherDataExtraInfo extraInfo = getLauncherDataExtraInfo(mToMergeRecord,
					mToPackageName);
			if (extraInfo == null) {
				return false;
			}

			File toLauncherDataFile = LauncherDataRestoreEntry
					.getLauncherDataBackupFile(toRecordDir);
			Util.copyFile(toLauncherDataFile.getAbsolutePath(), new File(beRecordDir,
					toLauncherDataFile.getName()).getAbsolutePath());

			ContentValues cv = new ContentValues();
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_LAUCHER_DATA);
			cv.put(DataTable.DATA1, extraInfo.packageName);
			cv.put(DataTable.DATA2, extraInfo.appName);
			cv.put(DataTable.DATA3, extraInfo.dataDir);
			cv.put(DataTable.DATA4, extraInfo.versionCode);
			cv.put(DataTable.DATA5, extraInfo.versionName);
			cv.put(DataTable.DATA6, extraInfo.buildDisplay); // Rom描述
			cv.put(DataTable.DATA7, extraInfo.buildVersionSdkInt); // SDK版本
			cv.put(DataTable.DATA8, extraInfo.buildId); // ROM 类型
			cv.put(DataTable.DATA9, extraInfo.buildModel); // 手机型号
			cv.put(DataTable.DATA10, toLauncherDataFile.getName());
			cv.put(DataTable.DATA11, LauncherDataBackupEntry.APP_WIDGET_FILE_NAME);
			if (wallpaperExist) {
				cv.put(DataTable.DATA12, LauncherDataBackupEntry.WALLPAPER_FILE_NAME);
			}
			cv.put(DataTable.DATA13, extraInfo.packageName + ".tar.gz");
			// ret = mDbHelper.insert(DataTable.TABLE_NAME, cv);
			ret = mDbHelper.reflashDatatable(cv);

			if (!ret) {
				return false;
			}

			ret = Util.copyFile(mToMergeRecord.getRecordRootDir() + mToPackageName + ".tar.gz",
					mBeMergedRecord.getRecordRootDir() + mToPackageName + ".tar.gz");
			if (!ret) {
				return false;
			}
			return ret;
		}
	}
}
