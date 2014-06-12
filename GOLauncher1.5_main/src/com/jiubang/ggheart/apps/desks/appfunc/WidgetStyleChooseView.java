package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.themescan.DotProgressBar;
import com.jiubang.ggheart.apps.gowidget.widgetThemeChoose.IWidgetChooseFrame;

public class WidgetStyleChooseView extends ViewGroup implements ScreenScrollerListener {

	// 触屏状态

	private ScreenScroller mScroller;
	private int mCurrentScreen = 0;
	private int mScrollingDuration = 400;

	/**
	 * viewGroup onInterceptTouchEvent用到
	 */
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;

	private int mTouchState = TOUCH_STATE_REST;

	private float mLastMotionX;

	// 移动多少距离算是scroll
	private int mTouchSlop;

	private DotProgressBar mProgressBar;

	private IWidgetChooseFrame widgetStyleChooseFrame;
	public ArrayList<Integer> mShowView;
	private int mScreens;

	public WidgetStyleChooseView(Context context) {
		super(context, null);
	}

	public WidgetStyleChooseView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mShowView = new ArrayList<Integer>();
		mScreens = 0;
		mScroller = new ScreenScroller(context, this);
		mScroller.setMaxOvershootPercent(0);
		mScroller.setDuration(mScrollingDuration);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

		int childLeft = 0;
		final int count = getChildCount();
		int childSpin = 0;
		if (mScreens < 1) {
			return;
		}

		final int height = b - t;

		for (int i = 0; i < mScreens; i++) {

			if (mShowView.contains(i) && childSpin < count) {

				int index = mShowView.indexOf(i);
				final View child = getChildAt(index);
				if (child.getVisibility() != View.GONE) {
					child.layout(childLeft, 0, childLeft + getWidth(), height);
				}
			}
			childLeft += getWidth();
		}

		// if (mFirstLayout) {
		// mFirstLayout = false;
		// mScroller.setCurrentScreen(0);
		// }
		//
		// mScroller.setScreenCount(getChildCount());
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
		if (!isFocusable()) {
			return false;
		}
		final int screen = indexOfChild(child);
		if (screen != mCurrentScreen || !mScroller.isFinished()) {

			snapToScreen(screen, true);

			return true;
		}
		return false;
	}

	private void snapToScreen(int screen, boolean b) {
		mScroller.gotoScreen(screen, mScrollingDuration, false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;

		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mScroller.onTouchEvent(event, action);

				break;
			}

			case MotionEvent.ACTION_MOVE : {
				mScroller.onTouchEvent(event, action);

				break;
			}

			case MotionEvent.ACTION_UP : {
				mScroller.onTouchEvent(event, action);

				mTouchState = TOUCH_STATE_REST;
				break;
			}

			case MotionEvent.ACTION_CANCEL :
				mTouchState = TOUCH_STATE_REST;
				break;

			default :
				break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

				break;
			}

			// 在view上产生此事件时，如果move了一定距离，要传给viewgroup的onTouchEvent处理
			// 如果在view外产生此事件，不会走到这里，直接调用viewgroup的onTouchEvent处理
			case MotionEvent.ACTION_MOVE : {
				final int xDiff = (int) Math.abs(x - mLastMotionX);
				boolean xMoved = xDiff > mTouchSlop;

				if (xMoved) {
					mTouchState = TOUCH_STATE_SCROLLING;
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mTouchState = TOUCH_STATE_REST;
				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	private void updateDotsIndicator(final int current) {
		if (mProgressBar != null) {
			mProgressBar.setCurProgress(current);
		}
	}

	/**
	 * 获取当前显示的屏幕id
	 * 
	 * @return 当前屏幕.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			if (view != null) {
				view.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

	@Override
	public void onScrollStart() {
		OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// updateSliderIndicator();

		int leftScreen = mScroller.getDrawingScreenA();
		int rightSrceen = mScroller.getDrawingScreenB();

		// Log.i("MyTest", "leftScreen: " +leftScreen);
		// Log.i("MyTest", "mCurrentScreen: "+mCurrentScreen);
		// Log.i("MyTest", "rightSrceen: " + rightSrceen);

		// 往右翻
		if (leftScreen == mCurrentScreen && !mShowView.contains(rightSrceen)) {
			widgetStyleChooseFrame.removeView(mCurrentScreen - 1);
			widgetStyleChooseFrame.addView(rightSrceen);
		}

		// 往左翻
		if (rightSrceen == mCurrentScreen && !mShowView.contains(leftScreen)) {
			widgetStyleChooseFrame.removeView(mCurrentScreen + 1);
			widgetStyleChooseFrame.addView(leftScreen);
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurrentScreen = newScreen;
		updateDotsIndicator(mCurrentScreen);

		if (widgetStyleChooseFrame != null) {
			widgetStyleChooseFrame.updateCurrentView(newScreen, oldScreen);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {

	}

	public void refreshDataAndView() {

	}

	public void changeOrientation(int param) {

	}

	public void setIndicator(DotProgressBar dot) {
		mProgressBar = dot;
	}

	public void setWidgetStyleChooseFrame(IWidgetChooseFrame frame) {
		widgetStyleChooseFrame = frame;
	}

	public int getCount() {
		return getChildCount();
	}

	public void resetScroll(int count) {
		// TODO Auto-generated method stub

		mScreens = count;
		mCurrentScreen = 0;

		mShowView.clear();
		mScroller.setCurrentScreen(0);
		mScroller.setScreenCount(count);
	}

	public void resetView(int count) {
		mScreens = count;

		mScroller.setScreenCount(count);
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		super.dispatchDraw(canvas);
	}

}
