package com.jiubang.go.backup.pro.model;

import com.jiubang.go.backup.pro.data.MessageReceiver;
import com.jiubang.go.backup.pro.net.sync.NetRestoreEngine;

/**
 * @author maiyongshen
 */
public class DownloadBackupService extends ForegroundWorkerService implements MessageReceiver {
	private static final int DOWNLOAD_BACKUP_SERVICE_NOTIFICATION_ID = 0x00ffff0005;
	private AsyncWorkEngine mCurWorkEngine;

	@Override
	public int getForegroundNotificationId() {
		return DOWNLOAD_BACKUP_SERVICE_NOTIFICATION_ID;
	}

	@Override
	public String getForegroundNotificationMessage() {
		if (mCurWorkEngine != null) {
			return mCurWorkEngine.getNotificationMessage();
		}
		return null;
	}

	@Override
	public void stopWork() {
		if (mCurWorkEngine != null) {
			mCurWorkEngine.forceToStop();
		}
	}

	@Override
	protected void doWork(Object arg1, Object arg2) {
		if (arg1 instanceof NetSyncTaskDbHelper) {
			mCurWorkEngine = new NetRestoreEngine(getAssociatedContext(),
					(NetSyncTaskDbHelper) arg1, this);
		}
		if (mCurWorkEngine != null) {
			mCurWorkEngine.startRun();
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

	@Override
	public boolean handleMessage(int arg1, int arg2, Object obj) {
		if (mCurWorkEngine != null) {
			return ((NetRestoreEngine) mCurWorkEngine).handleMessage(arg1, arg2, obj);
		}
		return false;
	}
}
