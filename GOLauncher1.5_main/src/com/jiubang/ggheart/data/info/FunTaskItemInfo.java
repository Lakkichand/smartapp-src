package com.jiubang.ggheart.data.info;

public class FunTaskItemInfo extends BaseItemInfo {

	// 进程唯一标识
	private int mPid;
	// 是否是白名单中的
	private boolean mIsIgnore;

	private AppItemInfo mAppItemInfo;

	public FunTaskItemInfo(AppItemInfo appItemInfo) {
		mAppItemInfo = appItemInfo;
	}

	/**
	 * 是否在白名单中
	 * 
	 * @return 是否在白名单中
	 */
	public boolean isInWhiteList() {
		return mIsIgnore;
	}

	/**
	 * 设置是否在白名单中
	 * 
	 * @param isIgnore
	 *            是否在白名单中
	 */
	public void setIsInWhiteList(boolean isIgnore) {
		mIsIgnore = isIgnore;
	}

	/**
	 * 设置数据bean
	 * 
	 * @param appItemInfo
	 */
	public void setAppItemInfo(AppItemInfo appItemInfo) {
		mAppItemInfo = appItemInfo;
		// 监听数据体bean
		if (null != appItemInfo) {
			// mAppItemInfo.registerObserver(this);
		}
	}

	/**
	 * 获取进程唯一标识
	 * 
	 * @return 唯一标识
	 */
	public int getPid() {
		return mPid;
	}

	/**
	 * 设置进程唯一标识
	 * 
	 * @param pid
	 *            唯一标识
	 */
	public void setPid(int pid) {
		this.mPid = pid;
	}

	/**
	 * 获取数据
	 * 
	 * @return
	 */
	public AppItemInfo getAppItemInfo() {
		return mAppItemInfo;
	}
}
