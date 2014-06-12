/*
 * 文 件 名:  AppGameBaseMenu.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.menu;

import android.content.Context;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-23]
 */
public abstract class AppGameBaseMenu implements OnKeyListener {
	/**
	 * 程序上下文
	 */
	protected Context mContext;
	/**
	 * 弹出窗口
	 */
	protected PopupWindow mPopupWindow;

	public abstract void show(View parent);

	public abstract void dismiss();

	public abstract boolean isShowing();

	public abstract void recyle();

	public abstract void setOnItemClickListener(OnItemClickListener listener);
}
