/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import android.view.View;

/**
 * @author ruxueqin
 * 
 */
public interface IOperationHandler {
	/**
	 * 显示操作菜单
	 */
	public boolean showQuickActionMenu(View target);

	/**
	 * 隐藏操作菜单
	 */
	public void hideQuickActionMenu(boolean dismissWithCallback);

	public void cleanHandlerMsg();
}
