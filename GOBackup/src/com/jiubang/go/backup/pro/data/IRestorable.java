package com.jiubang.go.backup.pro.data;

import android.content.Context;

import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * 可恢复接口
 * 
 * @author maiyongshen
 */
public interface IRestorable {
	public boolean restore(Context context, Object data, IAsyncTaskListener listener);

	public void stopRestore();

	/**
	 * 恢复参数
	 * 
	 * @author maiyongshen
	 */
	public static class RestoreArgs {
		public String mRestorePath; // 当前待恢复文件路径
		public boolean mIsRoot; // 当前待恢复文件是否在root环境下生成
		// public BackupPropertiesConfig mConfig; //备份配置文件
//		public RecordDescribe mRecordDescribe; // 备份配置信息
												// V2.0版本后，使用数据库保存，不再使用配置文件
	}
}
