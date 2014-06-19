package com.jiubang.go.backup.pro.data;

import android.content.Context;

import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * 接口 可备份项
 * 
 * @author maiyongshen
 */
public interface IBackupable {
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener);

	/**
	 * 备份参数
	 * 
	 * @author maiyongshen
	 */
	public static class BackupArgs {
		public String mBackupPath; // 当前备份路径
		public boolean mIsRoot; // 当前备份是否root环境
		// public BackupPropertiesConfig mConfig; //备份配置Properties
		public BackupDBHelper mDbHelper; // 备份的数据库
	}
}
