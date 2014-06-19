package com.jiubang.go.backup.pro.mergerecord;

import android.content.Context;

import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * 抽象类 整合Action
 * 
 * @author maiyongshen
 */
public abstract class MergeAction {
	protected RestorableRecord mBeMergedRecord;
	protected RestorableRecord mToMergeRecord;
	protected IAsyncTaskListener mListener;
	private boolean mStopped = false;

	/**
	 * @param beMergedRecord
	 *            被合并的记录
	 * @param toMergeRecord
	 *            待合并的记录
	 * @param listener
	 */
	public MergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
			IAsyncTaskListener listener) {
		mBeMergedRecord = beMergedRecord;
		mToMergeRecord = toMergeRecord;
		mListener = listener;
	}

	// 描述说明
	public String getDescription(Context context) {
		return "";
	}

	public void forceToStop() {
		mStopped = true;
	}

	public boolean isStopped() {
		return mStopped;
	}

	// 合并操作的实现方法
	public abstract boolean execute();

	// 合并操作的进度权重
	public abstract int getProgressWeight();
}
