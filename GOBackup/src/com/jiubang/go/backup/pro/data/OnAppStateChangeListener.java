package com.jiubang.go.backup.pro.data;

/**
 * 接口 应用程序状态改变listener
 * 
 * @author wencan
 */
public interface OnAppStateChangeListener {
	/**
	 * 应用程序状态
	 * 
	 * @author wencan
	 */
	public enum AppState {
		LABEL_CHANGE, ICON_CHANGE, SIZE_CHANGE
	}

	void onAppStateChange(AppInfo appInfo, AppState state);
}
