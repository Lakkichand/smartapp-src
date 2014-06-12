/*
 * 文 件 名:  AppGameMenuListView.java
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
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-23]
 */
public class AppGameMenuListView extends ListView {

	private AppGameBaseMenu mParent;

	public AppGameMenuListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AppGameMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			if (ev.getX() > getLeft() && ev.getX() < getRight() && ev.getY() > getTop()
					&& ev.getY() < getBottom()) {
				return super.onTouchEvent(ev);
			} else {
				if (ev.getAction() == MotionEvent.ACTION_UP) {
					if (mParent != null && mParent.isShowing()) {
						mParent.dismiss();
					}
				}
			}
			return super.onTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}
	}

	public void setParent(AppGameBaseMenu parent) {
		mParent = parent;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mParent.isShowing()) {
				mParent.dismiss();
				return true;
			}
		}
		return false;
	}
}
