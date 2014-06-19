package com.jiubang.go.backup.pro.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreArgs;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry.RestoreState;
import com.jiubang.go.backup.pro.data.IRestorable.RestoreArgs;
import com.jiubang.go.backup.pro.data.MessageReceiver;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord.RecordRestoreArgs;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class RestoreEngine extends AsyncWorkEngine implements IAsyncTaskListener, MessageReceiver {
	//	private static final int CONTACTS_ENTRY_PROGRESS_WEIGHT = 20;
	//	private static final int SMS_ENTRY_PROGRESS_WEIGHT = 50;
	//	private static final int MMS_ENTRY_PROGRESS_WEIGHT = 20;
	//	private static final int CALLLOG_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int USER_DICTIONARY_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int WIFI_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int LAUNCHER_DATA_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int GO_LAUNCHER_SETTTING_ENTRY_PROGRESS_WEIGHT = 5;
	//	private static final int USER_APP_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int WALLPAPER_ENTRY_PROGRESS_WEIGHT = 10;
	//	private static final int RINGTONE_ENTRY_PROGRESS_WEIGHT = 10;

	private Context mContext;
	private RestorableRecord mRecord;
	private RecordRestoreArgs mRestoreArgs;
	private IAsyncTaskListener mListener;

	private List<BaseEntry> mAllRestoreEntries;
	private int mCurRestoreIndex;
	private BaseRestoreEntry mCurRestoreEntry;
	private int mTotalEntriesCount;
	private int mSuccessfulEntriesCount;
	private String mFullBackupResourcePath;
	private String mExtraTips;
	private boolean mShouldReboot = false;

	private int mAppEntriesCount = 0;
	private int mCompletedAppEntriesCount = 0;

	private float mCurrentProgress;
	private float mProgressUnit;

	public RestoreEngine(Context context, RestorableRecord record, RecordRestoreArgs args,
			IAsyncTaskListener listener) {
		super("RestoreEngine" + (int) record.getId());
		if (context == null || args == null) {
			throw new IllegalArgumentException("RestoreEngine invalid argument");
		}
		mContext = context;
		mRecord = record;
		mRestoreArgs = args;
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
		if (mListener != null) {
			boolean result = mTotalEntriesCount == mSuccessfulEntriesCount /*
																			 * ? true : finishNormal
																			 */;
			// mListener.onEnd(result, mSuccessfulEntriesCount,
			// mTotalEntriesCount);
			mListener.onEnd(result, getResults(), mShouldReboot);
		}
		super.onWorkFinish(finishNormal);
	}

	@Override
	protected boolean hasNextWork() {
		return hasNextEntry();
	}

	@Override
	protected void doNextWork() {
		setCurrentWorkState(WorkState.WORKING);
		restoreNextEntry();
	}

	@Override
	public String getNotificationMessage() {
		return mContext.getString(R.string.msg_restoring, mCurRestoreEntry != null
				? mCurRestoreEntry.getDescription()
				: "", "");
	}

	@Override
	public void onStart(Object arg1, Object arg2) {

	}

	@Override
	public void onProceeding(Object progress, Object curEntry, Object arg3, Object arg4) {
		/*
		 * if (mListener != null && curEntry instanceof BaseRestoreEntry) { BaseRestoreEntry entry =
		 * (BaseRestoreEntry) curEntry; final int curProgress = (int) (mCurrentProgress +
		 * ((getProgressWeight(entry) * mProgressUnit) * (Float) progress));
		 * mListener.onProceeding(curProgress, entry, null, null); mExtraTips = arg3 != null ?
		 * arg3.toString() : null; }
		 */
		String extraTips = arg3 != null ? arg3.toString() : null;
		notifyProgressUpdated((Float) progress, (BaseRestoreEntry) curEntry, extraTips);
	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		onFinishWork(mCurRestoreEntry);
	}

	private void init() {
		mAllRestoreEntries = mRecord.getSelectedEntries();
		mTotalEntriesCount = mAllRestoreEntries != null ? mAllRestoreEntries.size() : 0;
		mAppEntriesCount = calcAppEntiresCount(mAllRestoreEntries);
		mCompletedAppEntriesCount = 0;
		mSuccessfulEntriesCount = 0;
		mCurRestoreIndex = -1;
		mCurRestoreEntry = null;
		mCurrentProgress = 0.0f;
		calcProgressUnit();
		mFullBackupResourcePath = mRestoreArgs.mRestorePath;
	}

	private int getProgressWeight(BaseRestoreEntry entry) {
		//		if (entry instanceof ContactsRestoreEntry) {
		//			return CONTACTS_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof SmsRestoreEntry) {
		//			return SMS_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof AppRestoreEntry) {
		//			return USER_APP_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof GoLauncherSettingRestoreEntry) {
		//			return GO_LAUNCHER_SETTTING_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof CallLogRestoreEntry) {
		//			return CALLLOG_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof LauncherDataRestoreEntry) {
		//			return LAUNCHER_DATA_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof UserDictionaryRestoreEntry) {
		//			return USER_DICTIONARY_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof MmsRestoreEntry) {
		//			return MMS_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof WallpaperRestoreEntry) {
		//			return WALLPAPER_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof RingtoneRestoreEntry) {
		//			return RINGTONE_ENTRY_PROGRESS_WEIGHT;
		//		} else if (entry instanceof WifiRestoreEntry) {
		//			return WIFI_ENTRY_PROGRESS_WEIGHT;
		//		}
		return entry != null ? entry.getEntryProgressWeight() : 0;
	}

	private void calcProgressUnit() {
		if (mAllRestoreEntries == null || mAllRestoreEntries.size() <= 0) {
			mProgressUnit = 1.0f;
			return;
		}
		int sum = 0;
		for (BaseEntry entry : mAllRestoreEntries) {
			sum += getProgressWeight((BaseRestoreEntry) entry);
		}
		final float progressSum = 100.0f;
		mProgressUnit = sum > 0 ? progressSum / sum : 1.0f;
	}

	private boolean hasNextEntry() {
		return mAllRestoreEntries != null && mAllRestoreEntries.size() > 0
				&& mCurRestoreIndex + 1 < mTotalEntriesCount;
	}

	private BaseRestoreEntry getNextRestoreEntry() {
		if (!hasNextEntry()) {
			return null;
		}
		BaseRestoreEntry nextEntry = (BaseRestoreEntry) mAllRestoreEntries.get(++mCurRestoreIndex);
		return nextEntry;
	}

	private void restoreNextEntry() {
		BaseRestoreEntry toRestoreEntry = getNextRestoreEntry();
		if (toRestoreEntry == null) {
			return;
		}
		mCurRestoreEntry = toRestoreEntry;
		toRestoreEntry.setState(RestoreState.READY_TO_RESTORE);

		notifyProgressUpdated(0, mCurRestoreEntry, null);

		toRestoreEntry.setState(RestoreState.RESTORING);
		boolean result = false;
		if (toRestoreEntry instanceof AppRestoreEntry) {
			AppRestoreArgs appRestoreArgs = new AppRestoreEntry.AppRestoreArgs();
			appRestoreArgs.mIsRoot = mRestoreArgs.mIsRoot;
			appRestoreArgs.mRestoreResPath = mFullBackupResourcePath;
			appRestoreArgs.mAppRestoreType = mRestoreArgs.mAppRestoreType;
			appRestoreArgs.mSilentRestore = mRestoreArgs.mSilentRestoreApp;
			result = toRestoreEntry.restore(mContext, appRestoreArgs, this);
		} else {
			RestoreArgs args = new RestoreArgs();
			args.mRestorePath = mFullBackupResourcePath;
			args.mIsRoot = mRestoreArgs.mIsRoot;
			// args.mConfig = mRestoreArgs.mConfig;
			//			args.mRecordDescribe = mRestoreArgs.mRecordDescribe;
			result = toRestoreEntry.restore(mContext, args, this);
		}
		if (!result) {
			// 如果直接返回false，则直接驱动下一个恢复项恢复，否则由onEnd驱动下一个恢复项恢复
			mCurRestoreEntry.setState(RestoreState.RESTORE_ERROR_OCCURRED);
			onFinishWork(mCurRestoreEntry);
		}
	}

	private void cancelRemainsWhenStop() {
		BaseRestoreEntry entry = null;
		while ((entry = getNextRestoreEntry()) != null) {
			entry.setState(RestoreState.RESTORE_CANCELED);
		}
	}

	@Override
	public boolean handleMessage(int arg1, int arg2, Object obj) {
		if (mCurRestoreEntry != null && mCurRestoreEntry instanceof MessageReceiver) {
			return ((MessageReceiver) mCurRestoreEntry).handleMessage(arg1, arg2, obj);
		}
		return false;
	}

	@Override
	public synchronized void forceToStop() {
		super.forceToStop();
		if (mCurRestoreEntry != null) {
			mCurRestoreEntry.stopRestore();
		}
	}

	private ResultBean[] getResults() {
		final int entryCount = mAllRestoreEntries.size();
		List<ResultBean> resultList = new ArrayList<ResultBean>(entryCount);
		for (int i = 0, j = 0; i < entryCount; i++) {
			BaseRestoreEntry entry = (BaseRestoreEntry) mAllRestoreEntries.get(i);
			RestoreState state = entry.getState();
			ResultBean resultBean = new ResultBean();
			resultBean.result = state == RestoreState.RESTORE_SUCCESSFUL;
			resultBean.title = entry.getDescription();
			if (state == RestoreState.RESTORE_CANCELED) {
				resultBean.desc = mContext.getString(R.string.msg_result_canceled);
			} else if (state == RestoreState.RESTORE_ERROR_OCCURRED) {
				resultBean.desc = mContext.getString(R.string.msg_result_data_corrupted);
				resultList.add(j, resultBean);
				j++;
				continue;
			} else {
				if (entry.isNeedReboot()) {
					resultBean.desc = mContext.getString(R.string.msg_reboot_info);
					mShouldReboot = true;
				}
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
			if (entry instanceof AppRestoreEntry) {
				count++;
			}
		}
		return count;
	}

	private List<WorkDetailBean> initAllWorkDetail() {
		if (Util.isCollectionEmpty(mAllRestoreEntries)) {
			return null;
		}
		List<WorkDetailBean> allWorkDetailBeans = new ArrayList<AsyncWorkEngine.WorkDetailBean>();
		// 应用程序任务归为一个分类
		WorkDetailBean appWorkDetailBean = null;
		for (BaseEntry entry : mAllRestoreEntries) {
			final String progressDesc = mContext.getString(R.string.state_waiting);
			if (entry instanceof AppRestoreEntry) {
				if (appWorkDetailBean == null) {
					appWorkDetailBean = buildWorkDetail((BaseRestoreEntry) entry, progressDesc);
					allWorkDetailBeans.add(appWorkDetailBean);
				}
				continue;
			}
			allWorkDetailBeans.add(buildWorkDetail((BaseRestoreEntry) entry, progressDesc));
		}
		return allWorkDetailBeans;
	}

	private WorkDetailBean buildWorkDetail(BaseRestoreEntry entry, String progress) {
		WorkDetailBean workDetail = new WorkDetailBean();
		workDetail.workId = entry.getType().ordinal();
		workDetail.workProgress = progress;
		String workObject = null;
		if (entry instanceof AppRestoreEntry) {
			workObject = EntryType.getDescription(mContext, EntryType.TYPE_USER_APP)
					+ mContext.getString(R.string.parenthesized_msg, entry.getDescription());
			workDetail.workObjectType = EntryType.TYPE_USER_APP;
		} else {
			workObject = entry.getDescription();
			workDetail.workObjectType = entry.getType();
		}
		BaseRestoreEntry.RestoreState state = entry.getState();
		if (state == RestoreState.READY_TO_RESTORE) {
			workDetail.title = workObject;
		} else if (state == RestoreState.RESTORING) {
			workDetail.title = mContext.getString(R.string.state_restoring, workObject);
		} else {
			if (entry instanceof AppRestoreEntry && mCompletedAppEntriesCount == mAppEntriesCount) {
				workDetail.title = EntryType.getDescription(mContext, EntryType.TYPE_USER_APP);
			}
			workDetail.title = workObject;
		}
		return workDetail;
	}

	private void notifyProgressUpdated(float progress, BaseRestoreEntry curEntry, String extraTips) {
		if (mListener == null) {
			return;
		}
		if (progress == 0) {
			extraTips = mContext.getString(R.string.msg_preparing);
		}
		final int curProgress = (int) (mCurrentProgress + (getProgressWeight(curEntry) * mProgressUnit)
				* progress);
		if (curEntry instanceof AppRestoreEntry) {
			extraTips = mContext.getString(R.string.progress_detail, mCompletedAppEntriesCount,
					mAppEntriesCount);
		}
		WorkDetailBean workDetailBean = buildWorkDetail(curEntry, extraTips);
		mListener.onProceeding(curProgress, curEntry, workDetailBean, null);
	}

	private void onFinishWork(BaseRestoreEntry entry) {
		if (entry instanceof AppRestoreEntry) {
			mCompletedAppEntriesCount++;
		}
		if (entry.getState() == RestoreState.RESTORE_SUCCESSFUL) {
			mSuccessfulEntriesCount++;
		}

		notifyProgressUpdated(1.0f, entry, mContext.getString(R.string.state_finished));
		mCurrentProgress += getProgressWeight(entry) * mProgressUnit;
		mExtraTips = null;

		setCurrentWorkState(WorkState.COMPLETED);
		continueToWork();
	}
}
