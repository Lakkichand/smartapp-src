package com.jiubang.go.backup.pro.data;

/**
 * 备份项基类
 * 
 * @author maiyongshen
 */
public abstract class BaseBackupEntry extends BaseEntry implements IBackupable {

	/**
	 * 备份状态
	 * 
	 * @author maiyongshen
	 */
	public enum BackupState {
		READY_TO_BACKUP, // 可以开始备份
		BACKUPING, // 正在备份中
		BACKUP_SUCCESSFUL, // 备份成功
		BACKUP_ERROR_OCCURRED, // 备份过程发生错误
		BACKUP_CANCELED
		// 用户主动取消备份
	};

	protected BackupState mState;

	public BaseBackupEntry() {
		super();
		mState = BackupState.READY_TO_BACKUP;
	}

	public BackupState getState() {
		return mState;
	}

	public void setState(BackupState state) {
		mState = state;
	}

}
