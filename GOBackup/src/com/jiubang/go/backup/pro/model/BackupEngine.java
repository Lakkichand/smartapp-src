package com.jiubang.go.backup.pro.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BackupableRecord.RecordBackupArgs;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseBackupEntry.BackupState;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.CalendarBackupEntry;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.IBackupable.BackupArgs;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.data.RingtoneBackupEntry;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份引擎
 *
 * @author maiyongshen
 */
public class BackupEngine extends AsyncWorkEngine implements IAsyncTaskListener {
	//	private static final int CONTACTS_ENTRY_PROGRESS_WEIGHT = 20;
	//	private static final int SMS_ENTRY_PROGRESS_WEIGHT = 30;
	//	private static final int MMS_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int CALLLOG_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int USER_DICTIONARY_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int WIFI_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int LAUNCHER_DATA_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int GO_LAUNCHER_SETTTING_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int USER_APP_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int WALLPAPER_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int RINGTONE_ENTRY_PROGRESS_WEIGHT = 5;

	private Context mContext;
	private BackupableRecord mRecord;
	private RecordBackupArgs mBackupArgs;
	private IAsyncTaskListener mListener;

	private List<BaseEntry> mAllBackupEntries;
	private int mCurBackupIndex;
	private BaseBackupEntry mCurBackupEntry;
	private int mTotalEntriesCount;
	private int mSuccessfulEntriesCount;
	private int mSuccessfulAppEntriesCount;
	private int mSuccessfulSystemEntriesCount;

	private int mAppEntriesCount = 0;
	private int mCompletedAppEntriesCount = 0;

	// private BackupPropertiesConfig mConfig;
	private String mFullBackupPath;
	private BackupDBHelper mDbHelper;

	private float mCurrentProgress;
	private float mProgressUnit;

	public BackupEngine(Context context, BackupableRecord record, RecordBackupArgs args,
			IAsyncTaskListener listener) {
		super("BackupEngine" + (int) record.getId());
		if (context == null || args == null) {
			throw new IllegalArgumentException("BackupEngine invalid argument");
		}
		mContext = context;
		mRecord = record;
		mBackupArgs = args;
		mFullBackupPath = Util.ensureFileSeparator(args.mBackupPath);
		mListener = listener;
	}

	@Override
	protected void onWorkStart() {
		init();
		if (mListener != null) {
			mListener.onStart(initAllWorkDetail(), null);
		}
	}

	@Override
	protected void onWorkFinish(boolean finishNormal) {
		if (!finishNormal) {
			cancelRemainsWhenStop();
		}
		// TODO 这里要做异常处理
		updatePropertiesToDb();
		closeDb();
		copyDb();
		// writeBackupProperties();
		if (mListener != null) {
			boolean result = mTotalEntriesCount == mSuccessfulEntriesCount;
			mListener.onEnd(result, getResults(), null);
		}
		release();
		super.onWorkFinish(finishNormal);
	}

	@Override
	protected boolean hasNextWork() {
		return hasNextEntry();
	}

	@Override
	protected void doNextWork() {
		setCurrentWorkState(WorkState.WORKING);
		backupNextEntry();
	}

	@Override
	public String getNotificationMessage() {
		return mContext.getString(R.string.msg_backing_up, mCurBackupEntry != null
				? mCurBackupEntry.getDescription()
				: "", "");
	}

	@Override
	public void onStart(Object arg1, Object arg2) {

	}

	@Override
	public void onProceeding(Object progress, Object curEntry, Object arg3, Object arg4) {
		/*
		 * if (mListener != null && curEntry instanceof BaseBackupEntry) {
		 * BaseBackupEntry entry = (BaseBackupEntry) curEntry; final int
		 * curProgress = (int)(mCurrentProgress + (getProgressWeight(entry) *
		 * mProgressUnit) * (Float) progress); String extraTip = arg3 != null ?
		 * arg3.toString() : null; if (entry instanceof AppBackupEntry) {
		 * extraTip = mContext.getString(R.string.progress_detail,
		 * mCompletedAppEntriesCount, mAppEntriesCount); } WorkDetailBean
		 * workDetailBean = buildWorkDetail(entry, extraTip);
		 * mListener.onProceeding(curProgress, entry, workDetailBean, null); }
		 */
		String extraTips = arg3 != null ? arg3.toString() : null;
		notifyProgressUpdated((Float) progress, (BaseBackupEntry) curEntry, extraTips);
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		BackupState curEntryState = success
				? BackupState.BACKUP_SUCCESSFUL
				: BackupState.BACKUP_ERROR_OCCURRED;
		onFinishWork(mCurBackupEntry, curEntryState);
	}

	private void init() {
		mAllBackupEntries = mRecord.getSelectedEntries();
		mTotalEntriesCount = mAllBackupEntries != null ? mAllBackupEntries.size() : 0;
		mSuccessfulEntriesCount = 0;
		mSuccessfulAppEntriesCount = 0;
		mSuccessfulSystemEntriesCount = 0;
		mAppEntriesCount = calcAppEntiresCount(mAllBackupEntries);
		mCompletedAppEntriesCount = 0;
		mCurBackupIndex = -1;
		mCurBackupEntry = null;
		mCurrentProgress = 0.0f;
		calcProgressUnit();
		// 创建备份目录
		createBackupDirectory();
		// 创建数据库
		createBackupDbHelper();
		// 创建备份BackupProperties
		// createBackupProperties();
	}

	private void createBackupDirectory() {
		File dir = new File(mFullBackupPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private void createBackupDbHelper() {
		// if (Util.getAndroidSystemVersion() > 7) {
		// mDbHelper = new BackupDBHelper(mContext, mFullBackupPath +
		// BackupDBHelper.getDBName(), BackupDBHelper.getDBVersion());
		// } else {
		File dbFile = mContext.getDatabasePath(BackupDBHelper.getDBName());
		if (dbFile != null && dbFile.exists()) {
			dbFile.delete();
		}

		mDbHelper = new BackupDBHelper(mContext, BackupDBHelper.getDBName(),
				BackupDBHelper.getDBVersion());
		// }
		mDbHelper.cleanAllData();
	}

	/*
	 * private void createBackupProperties(){ mConfig = new
	 * BackupPropertiesConfig(mFullBackupPath +
	 * BackupPropertiesConfig.BACKUP_PROPERTIES_FILE_NAME); }
	 */

	private void cancelRemainsWhenStop() {
		BaseBackupEntry entry = null;
		while ((entry = getNextBackupEntry()) != null) {
			entry.setState(BackupState.BACKUP_CANCELED);
		}
	}

	private boolean hasNextEntry() {
		return mAllBackupEntries != null && mAllBackupEntries.size() > 0
				&& mCurBackupIndex + 1 < mTotalEntriesCount;
	}

	private BaseBackupEntry getNextBackupEntry() {
		if (!hasNextEntry()) {
			return null;
		}
		BaseBackupEntry nextEntry = (BaseBackupEntry) mAllBackupEntries.get(++mCurBackupIndex);
		return nextEntry;
	}

	private void backupNextEntry() {
		BaseBackupEntry toBackupEntry = getNextBackupEntry();
		if (toBackupEntry == null) {
			return;
		}
		mCurBackupEntry = toBackupEntry;
		notifyProgressUpdated(0, mCurBackupEntry, null);

		boolean result = false;
		if (toBackupEntry instanceof AppBackupEntry) {
			AppBackupEntry.AppBackArgs appBackArgs = new AppBackupEntry.AppBackArgs();
			appBackArgs.mDbHelper = mDbHelper;
			appBackArgs.mBackupPath = mFullBackupPath;
			appBackArgs.mIsRoot = mBackupArgs.mIsRoot;
			// appBackArgs.mConfig = mConfig;
			appBackArgs.mAppBackupType = mBackupArgs.mAppBackupType;
			result = toBackupEntry.backup(mContext, appBackArgs, this);
		} else {
			BackupArgs args = new BackupArgs();
			args.mDbHelper = mDbHelper;
			args.mBackupPath = mFullBackupPath;
			args.mIsRoot = mBackupArgs.mIsRoot;
			// args.mConfig = mConfig;
			result = toBackupEntry.backup(mContext, args, this);
		}
		if (!result) {
			// 根据3月30号需求，重新修改为当前备份项失败后继续备份
			onFinishWork(mCurBackupEntry, BackupState.BACKUP_ERROR_OCCURRED);
		}
	}

	// 不同备份项的进度权重
	private int getProgressWeight(BaseBackupEntry entry) {
		//		if (entry instanceof ContactsBackupEntry) {
		//			return CONTACTS_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof SmsBackupEntry) {
		//			return SMS_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof AppBackupEntry) {
		//			return USER_APP_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof GoLauncherSettingBackupEntry) {
		//			return GO_LAUNCHER_SETTTING_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof CallLogBackupEntry) {
		//			return CALLLOG_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof LauncherDataBackupEntry) {
		//			return LAUNCHER_DATA_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof UserDictionaryBackupEntry) {
		//			return USER_DICTIONARY_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof MmsBackupEntry) {
		//			return MMS_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof RingtoneBackupEntry) {
		//			return RINGTONE_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof WallpaperBackupEntry) {
		//			return WALLPAPER_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof WifiBackupEntry) {
		//			return WIFI_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof CalendarBackupEntry) {
		//
		//		}
		return entry != null ? entry.getEntryProgressWeight() : 0;
	}

	private void calcProgressUnit() {
		if (mAllBackupEntries == null || mAllBackupEntries.size() <= 0) {
			mProgressUnit = 1.0f;
			return;
		}
		int sum = 0;
		for (BaseEntry entry : mAllBackupEntries) {
			sum += getProgressWeight((BaseBackupEntry) entry);
		}
		final float progressSum = 100.0f;
		mProgressUnit = sum > 0 ? progressSum / sum : 1.0f;
	}

	private void closeDb() {
		if (mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}

	private boolean copyDb() {
		boolean ret = false;
		// if (Util.getAndroidSystemVersion() > 7) {
		// ret = true;
		// } else {
		File dbFilePath = mContext.getDatabasePath(BackupDBHelper.getDBName());
		ret = Util.copyFile(dbFilePath.getPath(), mFullBackupPath + BackupDBHelper.getDBName());
		// }
		return ret;
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

		Date date = mRecord.getDate() == null ? new Date() : mRecord.getDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		cv.put(DataTable.DATA5, calendar.getTimeInMillis());
		cv.put(DataTable.DATA6, mBackupArgs.mIsRoot);
		cv.put(DataTable.DATA7, mSuccessfulAppEntriesCount);
		cv.put(DataTable.DATA8, mSuccessfulSystemEntriesCount);
		File file = new File(mFullBackupPath);
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
		// return mDbHelper.insert(DataTable.TABLE_NAME, cv);
	}

	// private boolean writeBackupProperties() {
	// mConfig.put(BackupPropertiesConfig.P_DATABASE_VERSION,
	// String.valueOf(BackupDBHelper.getDBVersion()));
	// mConfig.put(BackupPropertiesConfig.P_OS_VERSION, Build.VERSION.RELEASE);
	//
	// mConfig.put(BackupPropertiesConfig.P_ISROOT,
	// String.valueOf(mBackupArgs.mIsRoot));
	//
	// Date date = mRecord.getDate() == null ? new Date() : mRecord.getDate();
	// Calendar calendar = Calendar.getInstance();
	// calendar.setTime(date);
	// mConfig.put(BackupPropertiesConfig.P_BACKUP_TIME,
	// String.valueOf(calendar.getTimeInMillis()));
	//
	// PackageManager pm = mContext.getPackageManager();
	// PackageInfo pi = null;
	// try {
	// pi = pm.getPackageInfo(mContext.getPackageName(), 0);
	// mConfig.put(BackupPropertiesConfig.P_SOFTWARE_VERSION_CODE,
	// String.valueOf(pi.versionCode));
	// mConfig.put(BackupPropertiesConfig.P_SOFTWARE_VERSION_NAME,
	// pi.versionName);
	// } catch (NameNotFoundException e) {
	// e.printStackTrace();
	// }
	//
	// mConfig.put(BackupPropertiesConfig.P_BACKUP_APP_ITEM_COUNT,
	// String.valueOf(mSuccessfulAppEntriesCount));
	// mConfig.put(BackupPropertiesConfig.P_BACKUP_SYSTEM_DATA_ITEM_COUNT,
	// String.valueOf(mSuccessfulSystemEntriesCount));
	// File file = new File(mFullBackupPath);
	// if (file.exists()) {
	// long size = Util.getFileSize(file.getAbsolutePath());
	// mConfig.put(BackupPropertiesConfig.P_BACKUP_SIZE, String.valueOf(size));
	// }
	// return mConfig.saveProper(mContext);
	// }

	private void release() {
		closeDb();
		mContext.deleteDatabase(BackupDBHelper.getDBName());
	}

	private ResultBean[] getResults() {
		if (Util.isCollectionEmpty(mAllBackupEntries)) {
			return null;
		}
		final int entryCount = mAllBackupEntries.size();
		List<ResultBean> resultList = new ArrayList<ResultBean>(entryCount);
		for (int i = 0, j = 0; i < entryCount; i++) {
			BaseBackupEntry entry = (BaseBackupEntry) mAllBackupEntries.get(i);
			BackupState state = entry.getState();
			ResultBean resultBean = new ResultBean();
			resultBean.result = state == BackupState.BACKUP_SUCCESSFUL;
			resultBean.title = entry.getDescription();
			if (state == BackupState.BACKUP_CANCELED) {
				resultBean.desc = mContext.getString(R.string.msg_result_canceled);
			} else if (state == BackupState.BACKUP_ERROR_OCCURRED) {
				if (entry instanceof ContactsBackupEntry) {
					resultBean.desc = mContext
							.getString(R.string.msg_result_handle_contacts_failed);
				} else if (entry instanceof SmsBackupEntry) {
					resultBean.desc = mContext.getString(R.string.msg_result_handle_sms_failed);
				} else if (entry instanceof CallLogBackupEntry) {
					resultBean.desc = mContext
							.getString(R.string.msg_result_handle_call_log_failed);
				} else if (entry instanceof MmsBackupEntry) {
					resultBean.desc = mContext.getString(R.string.msg_result_handle_mms_failed);
				} else if (entry instanceof RingtoneBackupEntry) {
					resultBean.desc = mContext
							.getString(R.string.msg_result_handle_ringtone_failed);
				} else if (entry instanceof CalendarBackupEntry) {
					resultBean.desc = mContext
							.getString(R.string.msg_result_handle_calendar_failed);
				} else {
					resultBean.desc = mContext.getString(R.string.msg_result_unknown_error);
				}
				resultList.add(j, resultBean);
				j++;
				continue;
			}
			resultList.add(i, resultBean);
		}
		return resultList.toArray(new ResultBean[resultList.size()]);
	}

	private int calcAppEntiresCount(List<BaseEntry> allEntries) {
		if (Util.isCollectionEmpty(allEntries)) {
			return 0;
		}
		int count = 0;
		for (BaseEntry entry : allEntries) {
			if (entry instanceof AppBackupEntry) {
				count++;
			}
		}
		return count;
	}

	private List<WorkDetailBean> initAllWorkDetail() {
		if (Util.isCollectionEmpty(mAllBackupEntries)) {
			return null;
		}
		List<WorkDetailBean> allWorkDetailBeans = new ArrayList<AsyncWorkEngine.WorkDetailBean>();
		// 应用程序任务归为一个分类
		WorkDetailBean appWorkDetailBean = null;
		for (BaseEntry entry : mAllBackupEntries) {
			final String progressDesc = mContext.getString(R.string.state_waiting);
			if (entry instanceof AppBackupEntry) {
				if (appWorkDetailBean == null) {
					appWorkDetailBean = buildWorkDetail((BaseBackupEntry) entry, progressDesc);
					allWorkDetailBeans.add(appWorkDetailBean);
				}
				continue;
			}
			allWorkDetailBeans.add(buildWorkDetail((BaseBackupEntry) entry, progressDesc));
		}
		return allWorkDetailBeans;
	}

	private WorkDetailBean buildWorkDetail(BaseBackupEntry entry, String progress) {
		WorkDetailBean workDetail = new WorkDetailBean();
		workDetail.workId = entry.getType().ordinal();
		workDetail.workProgress = progress;
		String workObject = null;
		if (entry instanceof AppBackupEntry) {
			workObject = EntryType.getDescription(mContext, EntryType.TYPE_USER_APP)
					+ mContext.getString(R.string.parenthesized_msg, entry.getDescription());
			workDetail.workObjectType = EntryType.TYPE_USER_APP;
		} else {
			workObject = entry.getDescription();
			workDetail.workObjectType = entry.getType();
		}
		BaseBackupEntry.BackupState state = entry.getState();
		if (state == BackupState.READY_TO_BACKUP) {
			workDetail.title = workObject;
		} else if (state == BackupState.BACKUPING) {
			workDetail.title = mContext.getString(R.string.state_backingup, workObject);
		} else {
			if (entry instanceof AppBackupEntry && mCompletedAppEntriesCount == mAppEntriesCount) {
				workDetail.title = EntryType.getDescription(mContext, EntryType.TYPE_USER_APP);
			} else {
				workDetail.title = workObject;
			}
		}
		return workDetail;
	}

	private void notifyProgressUpdated(float progress, BaseBackupEntry curEntry, String extraTips) {
		if (mListener == null) {
			return;
		}
		if (progress == 0) {
			extraTips = mContext.getString(R.string.msg_preparing);
		}
		final int curProgress = (int) (mCurrentProgress + (getProgressWeight(curEntry) * mProgressUnit)
				* progress);
		if (curEntry instanceof AppBackupEntry) {
			extraTips = mContext.getString(R.string.progress_detail, mCompletedAppEntriesCount,
					mAppEntriesCount);
		}
		WorkDetailBean workDetailBean = buildWorkDetail(curEntry, extraTips);
		mListener.onProceeding(curProgress, curEntry, workDetailBean, null);
	}

	private void onFinishWork(BaseBackupEntry entry, BaseBackupEntry.BackupState state) {
		if (entry == null || state == null) {
			return;
		}

		entry.setState(state);
		if (entry instanceof AppBackupEntry) {
			mCompletedAppEntriesCount++;
		}
		if (state == BackupState.BACKUP_SUCCESSFUL) {
			mSuccessfulEntriesCount++;
			if (entry instanceof AppBackupEntry) {
				mSuccessfulAppEntriesCount++;
			} else if (entry != null) {
				mSuccessfulSystemEntriesCount++;
			}
		}
		notifyProgressUpdated(1.0f, entry, mContext.getString(R.string.state_finished));
		mCurrentProgress += getProgressWeight(entry) * mProgressUnit;
		setCurrentWorkState(WorkState.COMPLETED);
		continueToWork();
	}
}
