/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import android.graphics.Bitmap;

/**
 * @author ruxueqin
 * 
 */
public interface OnDockSettingListener {
	/**
	 * 数据改变处理
	 * 
	 * @param MSG
	 *            　消息ＩＤ
	 */
	public void onDataChange(int MSG);

	/**
	 * 
	 * @return　当前选择ＡＰＰ默认图标
	 */
	public Bitmap getAppDefaultIcon();

	/**
	 * 进入DOCK条快捷方式选择界面
	 * 
	 * @param clickOrGesture
	 *            true:点击 false:手势
	 */
	public void selectShortCut(boolean clickOrGesture);

	/***
	 * 设置功能表图标
	 */
	public void setAppFunIcon();

	/**
	 * 设置空白显示
	 */
	public void setBlank();

	/**
	 * 恢复程序默认图标
	 */
	public void resetToDefaultIcon();
}
