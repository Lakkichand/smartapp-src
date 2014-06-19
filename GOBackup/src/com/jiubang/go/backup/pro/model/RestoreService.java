package com.jiubang.go.backup.pro.model;

import com.jiubang.go.backup.pro.data.MessageReceiver;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord.RecordRestoreArgs;

/**
 * @author maiyongshen
 */
public class RestoreService extends ForegroundWorkerService implements MessageReceiver {
	private static final int RESTORE_SERVICE_NOTIFICATION_ID = 0x00ffff0002;
	private AsyncWorkEngine mRestoreEngine;

	@Override
	public int getForegroundNotificationId() {
		return RESTORE_SERVICE_NOTIFICATION_ID;
	}

	@Override
	public String getForegroundNotificationMessage() {
		if (mRestoreEngine != null) {
			return mRestoreEngine.getNotificationMessage();
		}
		return null;
	}

	// TODO 指定某个备份/恢复进程停止
	@Override
	public void stopWork() {
		if (mRestoreEngine != null) {
			mRestoreEngine.forceToStop();
		}
	}

	@Override
	public boolean handleMessage(int arg1, int arg2, Object obj) {
		if (mRestoreEngine != null) {
			return ((RestoreEngine) mRestoreEngine).handleMessage(arg1, arg2, obj);
		}
		return false;
	}

	@Override
	protected void doWork(Object arg1, Object arg2) {
		if (arg1 instanceof RestorableRecord && arg2 instanceof RecordRestoreArgs) {
			RestorableRecord record = (RestorableRecord) arg1;
			RecordRestoreArgs args = (RecordRestoreArgs) arg2;
			mRestoreEngine = new RestoreEngine(getAssociatedContext(), record, args, this);
			mRestoreEngine.startRun();
		} else {
			onWorkFinish(false, arg1, arg2);
			throw new IllegalArgumentException("RestoreService invalid argument");
		}
	}

	@Override
	public void pauseWork() {
		if (mRestoreEngine != null) {
			mRestoreEngine.pause();
		}
	}

	@Override
	public void resumeWork() {
		if (mRestoreEngine != null) {
			mRestoreEngine.resume();
		}
	}

	@Override
	public boolean isWorkPaused() {
		if (mRestoreEngine != null) {
			return mRestoreEngine.isPaused();
		}
		return false;
	}
}
