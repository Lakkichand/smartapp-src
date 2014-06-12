package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Canvas;

/**
 * 桌面的回调接口
 * 桌面会通过这个接口反射回调一些方法，使罩子层与桌面的状态或者样式保持一致
 * @author jiangxuwen
 *
 */
public interface ILauncherCallback {

	/**
	 * 创建的时候回调
	 */
	void onCreate();

	/**
	 * 进入暂停状态时回调
	 */
	void onPause();

	/**
	 * 进入停止状态时回调
	 */
	void onStop();

	/**
	 * 恢复时回调
	 */
	void onResume();

	/**
	 * 销毁时回调
	 */
	void onDestroy();

	/**
	 * wakeUp
	 */
	void onWakeUp(Object a);
	
	/**
	 * 桌面主Activity状态栏发生变化时回调
	 * @param height 变化后的状态栏高度（全屏时为0）
	 */
	void onStatusBarChange(int height);
	
	/**
	 * 获取桌面罩子层版本号
	 * @param version
	 */
	void onReadVersion(String version);
	
	/**
	 * 共桌面截屏
	 * @param canvas
	 */
	void doDraw(Canvas canvas);
}
