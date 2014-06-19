package com.jiubang.go.backup.pro.mergerecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 抽象类 批量整合Action
 *
 * @author maiyongshen
 */
public abstract class BatchMergeAction extends MergeAction {
	protected List<MergeAction> mMergeActions;
	private int mProgressWeight;

	public BatchMergeAction(RestorableRecord beMergedRecord, IAsyncTaskListener listener) {
		super(beMergedRecord, null, listener);
		mMergeActions = new ArrayList<MergeAction>();
		mProgressWeight = 0;
	}

	public void addRecordToMerge(RestorableRecord toMergeRecord) {
		if (toMergeRecord == null) {
			return;
		}
		if (!shouldMerge(toMergeRecord)) {
			return;
		}
		MergeAction newAction = buildMergeAction(toMergeRecord);
		mMergeActions.add(newAction);
		mProgressWeight += newAction.getProgressWeight();
	}

	public void removeRecordToMerge(RestorableRecord toMergeRecord) {
		if (toMergeRecord == null) {
			return;
		}
		for (MergeAction action : mMergeActions) {
			if (action.mToMergeRecord == toMergeRecord) {
				mMergeActions.remove(action);
				mProgressWeight -= action.getProgressWeight();
				return;
			}
		}
	}

	public boolean isEmpty() {
		return mMergeActions == null || mMergeActions.size() <= 0;
	}

	public void clear() {
		if (mMergeActions != null) {
			mMergeActions.clear();
		}
		mProgressWeight = 0;
	}

	protected abstract MergeAction buildMergeAction(RestorableRecord record);

	protected abstract boolean shouldMerge(RestorableRecord toMergeRecord);

	@Override
	public void forceToStop() {
		super.forceToStop();
		if (!Util.isCollectionEmpty(mMergeActions)) {
			for (MergeAction action : mMergeActions) {
				action.forceToStop();
			}
		}
	}

	@Override
	public boolean execute() {
		if (mBeMergedRecord == null || mMergeActions == null || mMergeActions.size() < 1) {
			if (mListener != null) {
				mListener.onEnd(true, this, null);
			}
			return false;
		}
		if (mListener != null) {
			mListener.onStart(this, null);
		}
		onPreprocessing();
		boolean[] results = new boolean[mMergeActions.size()];
		Arrays.fill(results, false);
		int index = 0;
		for (MergeAction action : mMergeActions) {
			Log.d("GoBackup", "batch merge action start execute");
			final int subActionProgressWeight = action.getProgressWeight();
			results[index++] = action.execute();
			if (mListener != null) {
				float pro = (float) subActionProgressWeight / (float) getProgressWeight() * index;
				mListener.onProceeding((float) subActionProgressWeight
						/ (float) getProgressWeight() * index, this, null, null);
			}
			if (isStopped()) {
				Log.d("GoBackup", "batch merge action stop");
				break;
			}
		}
		boolean result = onPostprocessing(results);
		if (mListener != null) {
			Log.d("GoBackup", "batch merge action end");
			mListener.onEnd(result, this, null);
		}
		return result;
	}

	protected void onPreprocessing() {

	}

	protected boolean onPostprocessing(boolean[] subActionResults) {
		return true;
	}

	@Override
	public int getProgressWeight() {
		return mProgressWeight;
	}

}
