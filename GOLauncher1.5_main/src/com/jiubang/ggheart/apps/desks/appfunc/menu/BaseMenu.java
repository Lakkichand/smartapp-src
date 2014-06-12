package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.app.Activity;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.PopupWindow;

import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;

public abstract class BaseMenu implements OnKeyListener {
	/**
	 * 程序上下文
	 */
	protected Activity mActivity;
	/**
	 * 弹出窗口
	 */
	protected PopupWindow mPopupWindow;

	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeCtrl;

	public abstract void show(View parent);

	public abstract void dismiss();

	public abstract boolean isShowing();

	public abstract void recyle();
}
