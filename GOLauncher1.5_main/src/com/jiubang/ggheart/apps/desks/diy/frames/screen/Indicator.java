package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.go.util.graphics.DrawUtils;

public class Indicator extends ViewGroup {
	public static int MOVE_DIRECTION_NONE = 0;
	public static int MOVE_DIRECTION_LEFT = 1;
	public static int MOVE_DIRECTION_RIGHT = 2;

	protected int mOffset = 0;
	protected int mTotal; // 总页数
	protected int mCurrent; // 当前页数
	protected float mMovePercent; // 当前滑动到百分之几 0-100
	protected IndicatorListner mListner; // 指示器监听者

	public boolean mIsCanTouch = true; //指示器是否可点击

	// 用于保存down时的位置信息
	protected Point mDownPoint = new Point(-1, -1);

	public static final int CLICK_LIMEN = DrawUtils.dip2px(6); // 在此move范围内不响应move,因为点击会引起小范围move
	protected int mMoveDirection = MOVE_DIRECTION_NONE; // 本组事件是否响应move

	public Indicator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public Indicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public Indicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}

	public void setListner(IndicatorListner listner) {
		mListner = listner;
	}

	public void setCurrent(int current) {

	};

	public void setTotal(int total) {

	};

	public void doWithShowModeChanged() {

	};

	public void setOffset(int offset) {
		mOffset = offset;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = super.onInterceptTouchEvent(ev);
		if (!mIsCanTouch) {
			return true;
		}
		int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mDownPoint.x = (int) ev.getRawX();
				mDownPoint.y = (int) ev.getRawY();
				mMoveDirection = Indicator.MOVE_DIRECTION_NONE;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				float x = ev.getRawX();

				if (mMoveDirection == Indicator.MOVE_DIRECTION_NONE) {
					if (Indicator.CLICK_LIMEN <= (x - mDownPoint.x)) {
						// 当move超过阈值Indicator.CLICK_LIMEN时才响应move
						mMoveDirection = Indicator.MOVE_DIRECTION_RIGHT;
					} else if (Indicator.CLICK_LIMEN <= (mDownPoint.x - x)) {
						mMoveDirection = Indicator.MOVE_DIRECTION_LEFT;
					} else {
						break;
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				break;
			}

			default : {
				break;
			}
		}

		return ret;
	}
}
