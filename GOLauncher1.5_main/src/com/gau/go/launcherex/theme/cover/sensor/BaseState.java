package com.gau.go.launcherex.theme.cover.sensor;

/**
 * 
 * 类描述:感应器状态数据
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-15]
 */
public abstract class BaseState {

	/**
	 * @see ISensorPysicalType
	 */
	private int mSensorType;

	private long mLastUpdateTime;

	public BaseState(int type) {
		mSensorType = type;
	}

	public int getSensorType() {
		return mSensorType;
	}

	public long getLastUpdateTime() {
		return mLastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.mLastUpdateTime = lastUpdateTime;
	}

}
