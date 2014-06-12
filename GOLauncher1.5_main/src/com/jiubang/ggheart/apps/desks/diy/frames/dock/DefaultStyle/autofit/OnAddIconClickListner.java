package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import android.view.View;

/**
 * 
 * <br>类描述:dock条添加图标点击图标监听器
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-21]
 */
public interface OnAddIconClickListner {

	/**
	 * <br>功能简述:点击其中一个项
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 * @param view
	 * @param position
	 * @param object
	 */
	public abstract void onIconsClick(int type, View view, int position, Object object);

	/**
	 * <br>功能简述:返回按钮
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 */
	public abstract void onBackBtnClick(int type);
}
