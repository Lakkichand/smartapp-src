package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 整合Engine
 *
 * @author maiyongshen
 */
public class MergeEngine extends AsyncWorkEngine implements IAsyncTaskListener {
	private static final String DB_TEMP_NAME = "mergine_temp.db";

	private Context mContext;
	private BackupManager mBackupManager;
	private IAsyncTaskListener mListener;
	private RestorableRecord mBeMergedRecord;
	private List<RestorableRecord> mToMergeRecords;
	private List<MergeAction> mMergeActions;
	private MergeAction mCurMergeAction;
	private BackupDBHelper mDbHelper;

	private float mCurProgress;
	private float mProgressUnit;

	private List<ResultBean> mResults;
	private int mResultIndex;
	private String mSelectedRecordIdinStr;

	public MergeEngine(Context context, IAsyncTaskListener listener, String selectedRecordIds) {
		super("MergeEngine" + 0);
		if (context == null) {
			throw new IllegalArgumentException("MergeEngine invalid args");
		}
		mContext = context;
		mListener = listener;
		mSelectedRecordIdinStr = selectedRecordIds;
		mBackupManager = BackupManager.getInstance();
		mToMergeRecords = new ArrayList<RestorableRecord>();
	}

	@Override
	protected void onWorkStart() {
		if (mListener != null) {
			mListener.onStart(null, null);
		}
		init();
	}

	@Override
	protected void onWorkFinish(boolean finishNormal) {
		if (!finishNormal) {
			cancelRemains();
		}

		/*
		 * // 配置文件，保存配置文件 BackupPropertiesConfig bpc =
		 * mBeMergedRecord.getBackupPropertiesConfig(); if(bpc != null) {
		 * bpc.put(BackupPropertiesConfig.P_BACKUP_TIME,
		 * String.valueOf(calendar.getTimeInMillis()));
		 * mBeMergedRecord.updateDate(); //保存 bpc.saveProper(mContext); }
		 */

		boolean ret = finishNormal;
		if (updatePropertiesToDb()) {
			closeDb();
			File dbFilePath = new File(Util.getInternalDatabasePath(mContext, DB_TEMP_NAME));
			ret = Util.copyFile(dbFilePath.getPath(), mBeMergedRecord.getRecordRootDir()
					+ BackupDBHelper.getDBName());
		}

		ret = ret && finishNormal;
		if (ret) {
			for (ResultBean result : mResults) {
				if (!result.result) {
					ret = false;
					break;
				}
			}
		}

		// 刷新记录
		mBeMergedRecord.clear();
		BackupManager.getInstance().removeRecord(mBeMergedRecord);
		mBeMergedRecord = new RestorableRecord(mContext, mBeMergedRecord.getRecordRootDir());
		if (mBeMergedRecord.isEmpty()) {
			mBeMergedRecord.delete();
			mBeMergedRecord = null;
		}

		if (mListener != null) {
			mListener.onEnd(ret, mBeMergedRecord, getResults());
		}
		release();
		super.onWorkFinish(ret);
	}

	private boolean updatePropertiesToDb() {
		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_CONFIG);
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			cv.put(DataTable.DATA1, pi.versionCode);
			cv.put(DataTable.DATA2, pi.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		cv.put(DataTable.DATA3, Build.VERSION.RELEASE);
		cv.put(DataTable.DATA4, BackupDBHelper.getDBVersion());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		cv.put(DataTable.DATA5, calendar.getTimeInMillis());
		File file = new File(mBeMergedRecord.getRecordRootDir());
		if (file.exists()) {
			long size = Util.getFileSize(file.getAbsolutePath());
			// 加上数据库大小
			File dbFile = mContext.getDatabasePath(BackupDBHelper.getDBName());
			if (dbFile != null) {
				size += dbFile.length();
			}
			cv.put(DataTable.DATA9, size);
		}
		return mDbHelper.reflashDatatable(cv);
		// if(mDbHelper.update(DataTable.TABLE_NAME, cv, DataTable.MIME_TYPE +
		// "=" + MimetypeTable.MIMETYPE_VALUE_CONFIG, null) == 0){
		// return mDbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		// return true;
	}

	private boolean createTempDatabase() {
		String internalDbPath = Util.getInternalDatabasePath(mContext, DB_TEMP_NAME);
		File srcDbFile = new File(mBeMergedRecord.getRecordRootDir(), BackupDBHelper.getDBName());
		if (!Util.copyFile(srcDbFile.getAbsolutePath(), internalDbPath)) {
			return false;
		}
		mDbHelper = new BackupDBHelper(mContext, DB_TEMP_NAME, BackupDBHelper.getDBVersion());
		return true;
	}

	private void closeDb() {
		if (mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}

	private void release() {
		closeDb();
		File tempDbFile = new File(Util.getInternalDatabasePath(mContext, DB_TEMP_NAME));
		if (tempDbFile.exists()) {
			tempDbFile.delete();
		}
	}

	@Override
	protected boolean hasNextWork() {
		return mMergeActions != null && mMergeActions.size() > 0;
	}

	@Override
	protected void doNextWork() {
		setCurrentWorkState(WorkState.WORKING);
		mCurMergeAction = mMergeActions.remove(0);
		if (mListener != null) {
			mListener.onProceeding((int) mCurProgress, mCurMergeAction, null, null);
		}
		if (mCurMergeAction != null) {
			mCurMergeAction.execute();
		}
	}

	@Override
	protected void continueToWork() {
		synchronized (this) {
			if (mCurMergeAction != null) {
				mCurProgress = mCurProgress + (mCurMergeAction.getProgressWeight() * mProgressUnit);
			}
			if (mListener != null) {
				mListener.onProceeding((int) mCurProgress, mCurMergeAction, null, null);
				mCurMergeAction = null;
			}
		}
		super.continueToWork();
	}

	@Override
	public String getNotificationMessage() {
		return mContext.getString(R.string.msg_merging,
				mCurMergeAction != null ? mCurMergeAction.getDescription(mContext) : "", "");
	}

	@Override
	public synchronized void forceToStop() {
		super.forceToStop();
		if (mCurMergeAction != null) {
			mCurMergeAction.forceToStop();
		}
	}

	// 做一些初始的工作，生成MergeAction对象
	private void init() {
		//获得已选中Id的list
		List<Long> selectedRecordIdList = getSelectedRecordIdList(mSelectedRecordIdinStr);
		List<IRecord> mergableRecords = mBackupManager.getMergableRecords();
		final String preferableBackupRootPath = Util.getDefalutValidBackupRootPath(mContext);
		if (Util.isCollectionEmpty(mergableRecords)) {
			File beMergedRecordDir = new File(
					BackupManager.getBackupsResRootFile(preferableBackupRootPath),
					BackupManager.SMART_MERGED_BACKUP);
			mBeMergedRecord = BackupManager.buildEmptyRecord(mContext,
					beMergedRecordDir.getAbsolutePath());
		} else if (mergableRecords != null) {
			if (mergableRecords.size() == 1) {
				mBeMergedRecord = (RestorableRecord) mergableRecords.get(0);
			} else {
				for (IRecord record : mergableRecords) {
					String dirPath = ((RestorableRecord) record).getRecordRootDir();
					if (dirPath != null && dirPath.startsWith(preferableBackupRootPath)) {
						mBeMergedRecord = (RestorableRecord) record;
						continue;
					}
					mToMergeRecords.add((RestorableRecord) record);
				}
			}
		}
		if (mBeMergedRecord == null) {
			mToMergeRecords.clear();
			return;
		}
		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		List<IRecord> scheduleRecords = mBackupManager.getScheduleRecords();
		List<IRecord> allRecords = new ArrayList<IRecord>();
		if (!Util.isCollectionEmpty(normalRecords)) {
			for (IRecord record : normalRecords) {
				if (selectedRecordIdList.contains(record.getId())) {
					allRecords.add(record);
				}
			}

		}
		if (!Util.isCollectionEmpty(scheduleRecords)) {
			for (IRecord record : scheduleRecords) {
				if (selectedRecordIdList.contains(record.getId())) {
					allRecords.add(record);
				}
			}
		}
		if (allRecords != null) {
			for (IRecord record : allRecords) {
				// TODO 应过滤部分记录
				RestorableRecord restoreRecord = (RestorableRecord) record;
				if (!restoreRecord.dataAvailable()) {
					restoreRecord.loadData(mContext);
				}
				mToMergeRecords.add((RestorableRecord) record);
			}
		}
		allRecords.clear();
		allRecords = null;

		// 打开数据库
		createTempDatabase();

		initMergeActions();

		mCurProgress = 0;
		calcProgressUnit();
	}

	private List<Long> getSelectedRecordIdList(String selectedRecordIdinStr) {
		List<Long> selectedRecordIdList = new ArrayList<Long>();
		String selecteddinStr = selectedRecordIdinStr;
		String[] selectedIdList = selecteddinStr.split("#");
		for (int i = 0; i < selectedIdList.length; i++) {
			selectedRecordIdList.add(Long.parseLong(selectedIdList[i]));
		}

		return selectedRecordIdList;

	}

	private void initMergeActions() {
		// 联系人
		BatchMergeAction contactsMergeAction = new ContactsBatchMergeAction(mContext,
				mBeMergedRecord, mDbHelper, this);
		// 用户词典
		BatchMergeAction dictionaryMergeAction = new UserDictionaryBatchMergeAction(mContext,
				mBeMergedRecord, mDbHelper, this);
		// 短信
		BatchMergeAction smsMergeAction = new NewSmsBatchMergeAction(mBeMergedRecord, mDbHelper,
				this, mContext);
		//		BatchMergeAction smsMergeAction = new SmsBatchMergeAction(mBeMergedRecord, mDbHelper, this);
		// 彩信
		BatchMergeAction mmsMergeAction = new MmsBatchMergeAction(mContext, mBeMergedRecord,
				mDbHelper, this);
		// 通话记录
		BatchMergeAction callLogMergeAction = new CallLogBatchMergeAction(mBeMergedRecord,
				mDbHelper, this);
		// 浏览器书签
		BatchMergeAction bookMarkMergeAction = new BookMarkBatchMergeAction(mBeMergedRecord,
				mDbHelper, this);
		// 应用
		BatchMergeAction appMergeAction = new AppBatchMergeAction(mContext, mBeMergedRecord, this,
				mDbHelper);
		// GO桌面设置
		BatchMergeAction goLauncherSettingMergeAction = new GOLauncherBatchMergeAction(
				mBeMergedRecord, mDbHelper, this);
		// Wi-Fi接入点
		BatchMergeAction wifiMergeAction = new WifiBatchMergeAction(mBeMergedRecord, mDbHelper,
				this);
		// 系统桌面数据
		BatchMergeAction launcherDataMergeAction = new LauncherDataBatchMergeAction(
				mBeMergedRecord, this, mDbHelper);
		// 壁纸
		BatchMergeAction wallpaperMergeAction = new WallpaperBatchMergeAction(mBeMergedRecord,
				mDbHelper, this);

		// TODO 铃声
		BatchMergeAction ringtoneMergeAction = new RingtoneBatchMergeAction(mBeMergedRecord,
				mDbHelper, this);
		// 日历
		BatchMergeAction calendarMergeAction = new CalendarBatchMergeAction(mContext,
				mBeMergedRecord, mDbHelper, this);

		if (mMergeActions == null) {
			mMergeActions = new ArrayList<MergeAction>();
		}
		mMergeActions.add(contactsMergeAction);
		mMergeActions.add(dictionaryMergeAction);
		mMergeActions.add(smsMergeAction);
		mMergeActions.add(mmsMergeAction);
		mMergeActions.add(callLogMergeAction);
		mMergeActions.add(appMergeAction);
		mMergeActions.add(goLauncherSettingMergeAction);
		mMergeActions.add(wifiMergeAction);
		mMergeActions.add(launcherDataMergeAction);
		mMergeActions.add(wallpaperMergeAction);
		mMergeActions.add(ringtoneMergeAction);
		mMergeActions.add(calendarMergeAction);
		mMergeActions.add(bookMarkMergeAction);

		for (RestorableRecord toMergeRecord : mToMergeRecords) {
			for (MergeAction mergeAction : mMergeActions) {
				((BatchMergeAction) mergeAction).addRecordToMerge(toMergeRecord);
			}
		}

		// 去掉空action
		for (Iterator<MergeAction> it = mMergeActions.iterator(); it.hasNext();) {
			MergeAction action = it.next();
			if (action instanceof BatchMergeAction && ((BatchMergeAction) action).isEmpty()) {
				it.remove();
			}
		}

		mResults = new ArrayList<ResultBean>(mMergeActions.size());
		mResultIndex = 0;
	}

	private void calcProgressUnit() {
		final float m100f = 100.0f;
		final float m1f = 1.0f;
		if (mMergeActions == null || mMergeActions.size() <= 0) {
			mProgressUnit = 1.0f;
			return;
		}
		int sum = 0;
		for (MergeAction action : mMergeActions) {
			sum += action.getProgressWeight();
		}
		mProgressUnit = sum > 0 ? m100f / sum : m1f;
	}

	@Override
	public void onStart(Object arg1, Object arg2) {
	}

	@Override
	public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
		if (mListener != null && mCurMergeAction != null) {
			float delta = (Float) progress * mCurMergeAction.getProgressWeight() * mProgressUnit;
			int curProgress = (int) (mCurProgress + delta);
			mListener.onProceeding(curProgress, mCurMergeAction, null, null);
		}
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		final MergeAction action = (MergeAction) arg1;
		ResultBean resultBean = null;
		boolean canceled = false;
		if (action == mCurMergeAction && mCurMergeAction.isStopped()) {
			resultBean = buildResultBean(success, true, action);
			canceled = true;
		} else {
			resultBean = buildResultBean(success, false, action);
		}
		if (!success && !canceled) {
			mResults.add(mResultIndex++, resultBean);
		} else {
			mResults.add(resultBean);
		}

		setCurrentWorkState(success ? WorkState.COMPLETED : WorkState.FAILED);
		continueToWork();
	}

	private ResultBean buildResultBean(boolean result, boolean canceled, MergeAction action) {
		ResultBean resultBean = new ResultBean();
		resultBean.result = result;
		resultBean.title = action.getDescription(mContext);
		if (!result && !canceled) {
			resultBean.desc = mContext.getString(R.string.msg_result_unknown_error);
		} else if (canceled) {
			resultBean.desc = mContext.getString(R.string.msg_result_canceled);
		}
		return resultBean;
	}

	private void cancelRemains() {
		while (mMergeActions.size() > 0) {
			final MergeAction action = mMergeActions.remove(0);
			mResults.add(buildResultBean(false, true, action));
		}
	}

	private ResultBean[] getResults() {
		return mResults.toArray(new ResultBean[mResults.size()]);
	}

	private boolean allWorkFailed(ResultBean[] results) {
		if (results == null) {
			return false;
		}
		final int count = results.length;
		for (int i = 0; i < count; i++) {
			if (results[i].result) {
				return false;
			}
		}
		return true;
	}
}
