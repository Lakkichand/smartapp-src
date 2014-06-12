package com.go.util.log;

public class FpsCounter {
	private static int CLOCKS_PER_SEC = 1000;

	private int mUpdateSeconds; // update value when exceed these seconds
	long mFrameTime;
	long mSecondTime;
	long mUpdateTime;
	int mFps;
	float mSumFps;
	float mAverageFps;

	/**
	 * 
	 * @param updateSeconds
	 *            更新帧率的时间，单位为秒
	 */
	public FpsCounter(int updateSeconds) {
		mUpdateSeconds = updateSeconds;
		Reset();
	}

	/**
	 * 计算帧率
	 * 
	 * @return 是否需要更新帧率
	 */
	public boolean computeFps(long drawingTime) {
		boolean updated = false;
		mFrameTime = drawingTime;
		if (mUpdateTime == 0) {
			mUpdateTime = mSecondTime = mFrameTime;
		}

		if (mFrameTime >= (mSecondTime + CLOCKS_PER_SEC)) {
			mSecondTime = mFrameTime;
			mSumFps += mFps;
			mFps = 0;
		}

		if (mFrameTime >= (mUpdateTime + CLOCKS_PER_SEC * mUpdateSeconds)) {
			mUpdateTime = mFrameTime;
			mAverageFps = mSumFps / mUpdateSeconds;
			mSumFps = 0;
			updated = true;
		}
		++mFps;
		return updated;
	}

	public void Reset() {
		mUpdateTime = mSecondTime = mFrameTime = 0;
		mSumFps = mFps = 0;
	}

	public float GetFps() {
		return mAverageFps;
	}

}
