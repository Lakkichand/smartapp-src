package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ListView;

public class BaseListMenuView extends ListView {

	private BaseMenu mParent;

	public BaseListMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public BaseListMenuView(Context context) {
		super(context);
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

	public void setParent(BaseMenu parent) {
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
