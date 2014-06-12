package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.ScrollView;

/**
 * 
 * @author zhujian
 * 
 */

public class AppDetailScrollViewSlowSpeed extends ScrollView {

	public final static int HORIZEN_MOVE = 0;
	public final static int VERTICAL_MOVE = 1;
	private float mLastMotionX = 0;
	private float mLastMotionY = 0;
	public final static int NO_MOVE = 3;
	public final static int CRITICAL_VALUE = 5;
	private VelocityTracker mVelocityTracker;
	private int mState = -1;

	public AppDetailScrollViewSlowSpeed(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AppDetailScrollViewSlowSpeed(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AppDetailScrollViewSlowSpeed(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		final int action = ev.getAction();
		float x = ev.getX();
		float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			float moveX = ev.getX();
			float moveY = ev.getY();
			mState = mathXY(moveX, mLastMotionX, moveY, mLastMotionY);
			mLastMotionX = x;
			mLastMotionY = y;
			if (mState == VERTICAL_MOVE) {
				return true;
			} else {
				return false;
			}
		case MotionEvent.ACTION_DOWN:
			x = ev.getX();
			y = ev.getY();
			mLastMotionX = x;
			mLastMotionY = y;
			mState = -1;
			break;
		case MotionEvent.ACTION_UP :
			mState = -1;
			break;
		case MotionEvent.ACTION_CANCEL :
			mState = -1;
			break;
		}

		return false;

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			return false;
		}
		obtainVelocityTracker(ev);
		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mLastMotionY = y;
				break;
			case MotionEvent.ACTION_MOVE :
				final int deltaY = (int) (mLastMotionY - y);
				if (deltaY < 0) {
					if (getScrollY() > 0) {
						scrollBy(0, deltaY);
					}
				} else if (deltaY > 0) {
					scrollBy(0, deltaY);
				}
				mLastMotionY = y;
				break;
			case MotionEvent.ACTION_UP :
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int initialVelocity = (int) velocityTracker.getYVelocity();
				if ((Math.abs(initialVelocity) > 400) && getChildCount() > 0) {
					fling(-initialVelocity);
				}
				releaseVelocityTracker();
				mLastMotionY = y;
				break;
		}
		return true;
	}
	
	public int mathXY(float x, float lastX, float y, float lastY) {
		if (mState == -1 || mState == NO_MOVE) {
			if ((Math.abs(y - lastY) / Math.abs(x - lastX)) >= 1.70) {
				mState = VERTICAL_MOVE;
			} else {
				mState = HORIZEN_MOVE;
			}
			if (Math.sqrt(Math.pow(y - lastY, 2) + Math.pow(x - lastX, 2)) <= CRITICAL_VALUE) {
				mState = NO_MOVE;
			}
		}
		return mState;
	}
	
	@Override
    public void fling(int velocityY) {
        super.fling(velocityY);
    }
	
	private void obtainVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
}