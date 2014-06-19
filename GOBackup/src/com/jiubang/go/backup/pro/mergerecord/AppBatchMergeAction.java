package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * appBatchMergeAction
 *
 * @author WenCan
 */
public class AppBatchMergeAction extends BatchMergeAction {
	private BackupDBHelper mDbHelper = null;
	private Context mContext = null;

	public AppBatchMergeAction(Context context, RestorableRecord beMergedRecord, IAsyncTaskListener listener,
			BackupDBHelper dbHelper) {
		super(beMergedRecord, listener);
		mDbHelper = dbHelper;
		mContext = context;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new AppMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		// if(Util.isCollectionEmpty(getAppEntrys(toMergeRecord))){
		// return false;
		// }
		if (toMergeRecord.getUserAppEntryCount() < 1 && toMergeRecord.getSystemAppEntryCount() < 1) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean onPostprocessing(boolean[] subActionResults) {
		// TODO 结果判断
		updateAppConfig();
		if (isStopped()) {
			return false;
		}
		final int count = subActionResults.length;
		for (int i = 0; i < count; i++) {
			if (!subActionResults[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onPreprocessing() {
		super.onPreprocessing();
	}

	private boolean updateAppConfig() {
		if (mDbHelper == null) {
			return false;
		}

		List<BaseRestoreEntry> appEntrys = getAppEntrys(mBeMergedRecord);
		final int count = appEntrys != null ? appEntrys.size() : 0;
		/*
		 * BackupPropertiesConfig bpc =
		 * mBeMergedRecord.getBackupPropertiesConfig(); if(bpc != null){
		 * bpc.put(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT,
		 * String.valueOf(count)); ret = true; }
		 */

		// V2.0
		boolean ret = true;
		String where = DataTable.MIME_TYPE + "=" + MimetypeTable.MIMETYPE_VALUE_CONFIG;
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONFIG);
		cv.put(DataTable.DATA7, count);
		// if(mDbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0){
		// ret = mDbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		ret = mDbHelper.reflashDatatable(cv);
		return ret;
	}

	private List<BaseRestoreEntry> getAppEntrys(RestorableRecord record) {
		if (record == null) {
			return null;
		}

		List<BaseRestoreEntry> result = new ArrayList<BaseRestoreEntry>();

		List<BaseRestoreEntry> tempEntrys = record.getEntriesByType(EntryType.TYPE_SYSTEM_APP);
		if (tempEntrys != null) {
			result.addAll(tempEntrys);
		}
		tempEntrys = record.getEntriesByType(EntryType.TYPE_USER_APP);
		if (tempEntrys != null) {
			result.addAll(tempEntrys);
		}
		return result;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.applications) : super
				.getDescription(context);
	}

	/**
	 * AppMergeAction
	 *
	 * @author WenCan
	 */
	private class AppMergeAction extends MergeAction {
		private static final int WEIGHT = 2;
		private List<BaseRestoreEntry> mToMergeAppEntrys = null;

		public AppMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
			mToMergeAppEntrys = getAppEntrys(mToMergeRecord);
		}

		@Override
		public int getProgressWeight() {
			int count = mToMergeAppEntrys != null ? mToMergeAppEntrys.size() : 0;
			return count * WEIGHT;
		}

		/**
		 * 合并应用程序 返回值表示合并集合有无发生变化
		 */
		@Override
		public boolean execute() {
			if (mToMergeAppEntrys == null || mToMergeAppEntrys.size() <= 0) {
				return true;
			}

			boolean ret = false;
			List<BaseRestoreEntry> beMergeEntrys = getAppEntrys(mBeMergedRecord);
			final int sumProgressWeight = AppBatchMergeAction.this.getProgressWeight();
			final int thisProgressWeight = this.getProgressWeight();
			// 处理待合并的记录entry
			if (mToMergeAppEntrys != null && mToMergeAppEntrys.size() > 0) {
				int index = 0;
				int totalCount = mToMergeAppEntrys.size();

				for (BaseRestoreEntry entry : mToMergeAppEntrys) {
					if (this.isStopped()) {
						break;
					}
					// Log.d("GOBackup", "AppBatchMergeAction : entry : " +
					// entry.getDescription());
					BaseRestoreEntry beMergeEntry = searchAppEntry(beMergeEntrys, entry);
					if (beMergeEntry != null) {
						ret = mergeEntry(entry, beMergeEntry);
					} else {
						ret = addEntry(entry);
					}
					index++;
					if (mListener != null) {
						float progress = (float) index / (float) totalCount * thisProgressWeight
								/ sumProgressWeight;
						mListener.onProceeding(progress, null, null, null);
					}
				}
			}
			mToMergeAppEntrys.clear();
			return ret;
		}

		private BaseRestoreEntry searchAppEntry(List<BaseRestoreEntry> entrys,
				BaseRestoreEntry toSearch) {
			if (toSearch == null || entrys == null) {
				return null;
			}

			for (BaseRestoreEntry entry : entrys) {
				AppRestoreEntry appEntry = (AppRestoreEntry) entry;
				AppRestoreEntry toSearchAppEntry = (AppRestoreEntry) toSearch;
				if (appEntry.equals(toSearchAppEntry)) {
					return appEntry;
				}
			}
			return null;
		}

		private boolean mergeEntry(BaseRestoreEntry toMergeEntry, BaseRestoreEntry beMergeEntry) {
			AppRestoreEntry toMergeAppEntry = (AppRestoreEntry) toMergeEntry;
			AppRestoreEntry beMergeAppEntry = (AppRestoreEntry) beMergeEntry;
			return doMerge(toMergeAppEntry, beMergeAppEntry);
		}

		private boolean addEntry(BaseRestoreEntry entry) {
			AppRestoreEntry appEntry = (AppRestoreEntry) entry;
			String packageName = appEntry.getAppInfo().packageName;
			File toApkFile = new File(mToMergeRecord.getRecordRootDir() + packageName + ".apk");
			File toDataFile = new File(mToMergeRecord.getRecordRootDir() + packageName + ".tar.gz");
			File beApkFile = new File(mBeMergedRecord.getRecordRootDir() + packageName + ".apk");
			File beDataFile = new File(mBeMergedRecord.getRecordRootDir() + packageName + ".tar.gz");

			boolean ret = true;
			
			if (toApkFile.exists()) {
				ret = Util.copyFile(toApkFile.getAbsolutePath(), beApkFile.getAbsolutePath());
				if (!ret) {
					return false;
				}
			}

			// 如果data文件存在
			if (toDataFile.exists()) {
				ret = Util.copyFile(toDataFile.getAbsolutePath(), beDataFile.getAbsolutePath());
				if (!ret) {
					return false;
				}
			}

			AppInfo appInfo = appEntry.getAppInfo();
			BaseRestoreEntry toEntry = new AppRestoreEntry(appInfo,
					mToMergeRecord.getRecordRootDir());
			if (mDbHelper != null) {
				ret = ((AppRestoreEntry) toEntry).updateDb(mContext, mDbHelper);
			}
			if (ret) {
				mBeMergedRecord.addEntry(IRecord.GROUP_USER_APP, toEntry);
			}
			return ret;
		}

		private boolean doMerge(AppRestoreEntry toMerge, AppRestoreEntry beMerge) {
			String toPackage = toMerge.getAppInfo().packageName;
			File toApkFile = new File(mToMergeRecord.getRecordRootDir() + toPackage + ".apk");
			File toDataFile = new File(mToMergeRecord.getRecordRootDir() + toPackage + ".tar.gz");
			
			String bePackage = beMerge.getAppInfo().packageName;
			File beApkFile = new File(mBeMergedRecord.getRecordRootDir() + bePackage + ".apk");
			File beDataFile = new File(mBeMergedRecord.getRecordRootDir() + bePackage + ".tar.gz");
			
			final int toVersionCode = toMerge.getAppInfo().versionCode;
			final int beVersionCode = beMerge.getAppInfo().versionCode;
			
			boolean ret = true;
			if (toApkFile.exists()) {
				if (beApkFile.exists() && toVersionCode > beVersionCode) {
					Util.deleteFile(beApkFile.getAbsolutePath());
					ret = Util.copyFile(toApkFile.getAbsolutePath(), beApkFile.getAbsolutePath());
				} else if (!beApkFile.exists()) {
					ret = Util.copyFile(toApkFile.getAbsolutePath(), beApkFile.getAbsolutePath());
				}
				if (!ret) {
					return false;
				}
			}
			
			if (toDataFile.exists()) {
				if (toVersionCode > beVersionCode || toDataFile.lastModified() > beDataFile.lastModified()) {
					Util.deleteFile(beDataFile.getAbsolutePath());
					ret = Util.copyFile(toDataFile.getAbsolutePath(), beDataFile.getAbsolutePath());
				}
				if (!ret) {
					return false;
				}
			}

			AppInfo appInfo = toMerge.getAppInfo();
			beMerge.setAppInfo(appInfo);

			if (mDbHelper != null) {
				ret = beMerge.updateDb(mContext, mDbHelper);
			}
			return ret;
		}
	}
}
