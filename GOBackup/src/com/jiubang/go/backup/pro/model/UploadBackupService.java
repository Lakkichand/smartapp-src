package com.jiubang.go.backup.pro.model;

import java.io.File;

import com.jiubang.go.backup.pro.net.sync.NetBackupEngine;

/**
 * @author maiyongshen
 * 
 */
public class UploadBackupService extends ForegroundWorkerService {
	private static final int UPLOAD_BACKUP_SERVICE_NOTIFICATION_ID = 0x00ffff0004;
	private AsyncWorkEngine mCurWorkEngine;

	@Override
	public int getForegroundNotificationId() {
		return UPLOAD_BACKUP_SERVICE_NOTIFICATION_ID;
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
		// 新建网络备份
		/*
		 * if (arg1 instanceof NetworkBackupDbHelper && arg2 instanceof
		 * BackupDBHelper) { // 断点续传备份 NetworkBackupDbHelper taskDbHelper =
		 * (NetworkBackupDbHelper) arg1; BackupDBHelper cacheBackupDBHelper =
		 * (BackupDBHelper) arg2; mCurWorkEngine = new NetworkBackupEngine(
		 * getAssociatedContext(), taskDbHelper, cacheBackupDBHelper, this); }
		 */
		mCurWorkEngine = new NetBackupEngine(getAssociatedContext(), (File) arg1, (File) arg2, this);
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

}
