package com.jiubang.go.backup.pro.data;

import com.jiubang.go.backup.pro.model.BackupDBHelper;

/**
 * 抽象类 恢复项抽象类
 * 
 * @author maiyongshen
 */
public abstract class BaseRestoreEntry extends BaseEntry implements IRestorable {

	/**
	 * 恢复状态
	 * 
	 * @author maiyongshen
	 */
	public enum RestoreState {
		READY_TO_RESTORE, // 可以开始恢复
		RESTORING, // 正在恢复中
		RESTORE_SUCCESSFUL, // 恢复成功
		RESTORE_ERROR_OCCURRED, // 恢复过程发生错误
		RESTORE_CANCELED
		// 用户主动取消恢复
	};

	/**
	 * 可恢复性状态
	 * 
	 * @author maiyongshen
	 */
	public enum RestorableState {
		UNRESTORABLE, // 不可恢复状态，例如不同包名或版本的桌面布局恢复
		APP_RESTORABLE, // 可恢复app
		APP_DATA_RESTORABLE, // 可恢复app和data
		DATA_RESTORABLE, // 数据可恢复，包括app数据或者联系人、短信、桌面布局等数据
		DATA_CORRUPTION
		// 数据损坏
	};

	protected RestorableState mRestorableState;

	protected RestoreState mState;

	public RestoreState getState() {
		return mState;
	}

	public void setState(RestoreState state) {
		mState = state;
	}

	public RestorableState getRestorableState() {
		return mRestorableState;
	}

	public void setRestorableState(RestorableState state) {
		mRestorableState = state;
	}

	public BaseRestoreEntry() {
		super();
		mState = RestoreState.READY_TO_RESTORE;
		mRestorableState = RestorableState.UNRESTORABLE;
	}

	public boolean isRestorable() {
		return mRestorableState != RestorableState.UNRESTORABLE
				&& mRestorableState != RestorableState.DATA_CORRUPTION;
	}

	public boolean isNeedReboot() {
		return false;
	}

	

	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		
	}
}
