package com.jiubang.go.backup.pro.model;

import com.jiubang.go.backup.pro.mergerecord.MergeEngine;

/**
 * @author maiyongshen
 */
public class MergeService extends ForegroundWorkerService {
	private static final int MERGE_SERVICE_NOTIFICATION_ID = 0x00ffff0003;
	private AsyncWorkEngine mMergeEngine;

	@Override
	public int getForegroundNotificationId() {
		return MERGE_SERVICE_NOTIFICATION_ID;
	}

	@Override
	public String getForegroundNotificationMessage() {
		if (mMergeEngine != null) {
			return mMergeEngine.getNotificationMessage();
		}
		return null;
	}

	@Override
	public void stopWork() {
		if (mMergeEngine != null) {
			mMergeEngine.forceToStop();
		}
	}

	@Override
	protected void doWork(Object arg1, Object arg2) {
		mMergeEngine = new MergeEngine(getAssociatedContext(), this, (String) arg1);
		mMergeEngine.startRun();
	}

	@Override
	public void pauseWork() {
		if (mMergeEngine != null) {
			mMergeEngine.pause();
		}
	}

	@Override
	public void resumeWork() {
		if (mMergeEngine != null) {
			mMergeEngine.resume();
		}
	}

	@Override
	public boolean isWorkPaused() {
		if (mMergeEngine != null) {
			return mMergeEngine.isPaused();
		}
		return false;
	}
}
