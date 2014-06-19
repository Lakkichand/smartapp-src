package com.jiubang.go.backup.pro.model;

import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BackupableRecord.RecordBackupArgs;

/**
 * @author maiyongshen
 */
public class BackupService extends ForegroundWorkerService {
	private static final int BACKUP_SERVICE_NOTIFICATION_ID = 0x00ffff0001;
	// private AsyncWorkEngine mBackupEngine;
	private AsyncWorkEngine mCurWorkEngine;

	@Override
	public int getForegroundNotificationId() {
		return BACKUP_SERVICE_NOTIFICATION_ID;
	}

	@Override
	public String getForegroundNotificationMessage() {
		if (mCurWorkEngine != null) {
			return mCurWorkEngine.getNotificationMessage();
		}
		return null;
	}

	@Override
	protected void doWork(Object arg1, Object arg2) {
		if (arg1 instanceof BackupableRecord && arg2 instanceof RecordBackupArgs) {
			BackupableRecord record = (BackupableRecord) arg1;
			RecordBackupArgs args = (RecordBackupArgs) arg2;
			// TODO 应支持多个任务
			mCurWorkEngine = new BackupEngine(getAssociatedContext(), record, args, this);
			mCurWorkEngine.startRun();
		} else {
			onWorkFinish(false, arg1, arg2);
			throw new IllegalArgumentException("BackupService invalid argument");
		}
		// v1.11暂不加上备份后自动整合的功能
		/*
		 * else if (arg1 instanceof BackupEngine) { mCurWorkEngine = new
		 * MergeEngine(getAssociatedContext(), this); mCurWorkEngine.startRun();
		 * }
		 */
	}

	@Override
	protected boolean shouldContinueToWork(Object finishedWork, boolean result) {
		// v1.11暂不加上自动整合的功能
		/*
		 * if (finishedWork instanceof BackupEngine) { boolean
		 * enableSmartMerging = PreferenceManager.getInstance()
		 * .getBoolean(this, PreferenceManager.KEY_ENABLE_SMART_MERGING, false);
		 * if (enableSmartMerging) { return true; } } else if (finishedWork
		 * instanceof MergeEngine) { return false; }
		 */
		return super.shouldContinueToWork(finishedWork, result);
	}

	@Override
	public void stopWork() {
		if (mCurWorkEngine != null) {
			mCurWorkEngine.forceToStop();
		}
	}

	@Override
	public void pauseWork() {
		if (mCurWorkEngine != null) {
			mCurWorkEngine.pause();
		}
	}

	@Override
	public void resumeWork() {
		if (mCurWorkEngine != null) {
			mCurWorkEngine.resume();
		}
	}

	@Override
	public boolean isWorkPaused() {
		if (mCurWorkEngine != null) {
			return mCurWorkEngine.isPaused();
		}
		return false;
	}
}
