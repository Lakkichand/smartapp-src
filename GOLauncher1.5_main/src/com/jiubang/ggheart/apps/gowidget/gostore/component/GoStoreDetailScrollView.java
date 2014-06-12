package com.jiubang.ggheart.apps.gowidget.gostore.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 
 * <br>类描述:Go精品主题详情gallery文字展示区域
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-10-29]
 */
public class GoStoreDetailScrollView extends ScrollView {
	private float mLastX = 0;
	private float mLastY = 0;
	public GoStoreDetailScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public GoStoreDetailScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GoStoreDetailScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mLastX = ev.getX();
			mLastY = ev.getY();
		} else if (action == MotionEvent.ACTION_MOVE) {
			float x = ev.getX();
			float y = ev.getY();
			int state = GoStoreDetailScrollViewGroup.mathXY(x, mLastX, y, mLastY);
			if (state == GoStoreDetailScrollViewGroup.VERTICAL_MOVE) {
				mLastX = ev.getX();
				mLastY = ev.getY();
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		float x = ev.getX();
		float y = ev.getY();
		scrollBy(0, (int) (mLastY - y));
		mLastY = ev.getY();
		return true;
	}
}
