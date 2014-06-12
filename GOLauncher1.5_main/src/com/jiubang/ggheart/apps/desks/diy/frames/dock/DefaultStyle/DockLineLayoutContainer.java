package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;

/**
 * 
 * <br>类描述:dock行排版窗口
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-10-16]
 */
public class DockLineLayoutContainer extends RelativeLayout
		implements
			ScreenScrollerListener,
			SubScreenContainer,
			OnTouchListener {
	private ScreenScroller mScroller; // 滚动器

	private CoupleScreenEffector mDeskScreenEffector; // 配合滚动器使用的效果器，主要作用是实现循环滚动

	private int mTouchSlop; // 区分滑动和点击的阈值

	private Point mDownPoint = new Point(-1, -1); // 下手时的坐标点

	private int mInterceptTouchMoveX; // 记录x方向的触屏滑动是否超过mTouchSlop

	private int mInterceptTouchMoveY; // 记录y方向的触屏滑动是否超过mTouchSlop

	public boolean mLongClicked = false; // 是否已响应长按

	private boolean mRespondGestured = false; // 是否已响应手势

	private OnDockGestureListner mGestureListner; // 手势响应者

	//TODO:3.15版本去除此变量，直接拿screenscroller
	private int mCurLine; // 当前显示第几行

	private DockIconView mCurrentView; // 当前操作ICON对象

	public DockLineLayoutContainer(Context context) {
		super(context);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

		mScroller = new ScreenScroller(context, this);
		mScroller.setDuration(450);
		mDeskScreenEffector = new CoupleScreenEffector(mScroller, CoupleScreenEffector.PLACE_DESK,
				CoupleScreenEffector.SUBSCREEN_EFFECTOR_TYPE);
		mScroller.setBackgroundAlwaysDrawn(true);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (AbsDockView.sPortrait) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}
	}

	private void layoutPort(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();

		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;

		for (int i = 0; i < count; i++) {
			AbsLineLayout layout = (AbsLineLayout) getChildAt(i);
			left = i * (r - l);
			right = left + (r - l);
			top = 0;
			bottom = top + (b - t);
			layout.layout(left, top, right, bottom);
		}
	}

	private void layoutLand(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();

		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;

		for (int i = 0; i < count; i++) {
			AbsLineLayout layout = (AbsLineLayout) getChildAt(i);
			left = 0;
			right = left + (r - l);
			top = i * (b - t);
			bottom = top + (b - t);
			layout.layout(left, top, right, bottom);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (!mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
	}

	public void setOrientation(int orientation) {
		mScroller.setOrientation(orientation);
	}

	public void setScreenCount(int count) {
		mScroller.setScreenCount(count);
	}

	public void setCurrentScreen(int dstscreen) {
		mScroller.setCurrentScreen(dstscreen);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return mScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub
		mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurLine = newScreen;
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// checkShowSlipGuide();
	}

	public int getCurLine() {
		return mCurLine;
	}

	public AbsLineLayout getCurLineLayout() {
		if (mCurLine >= 0 && mCurLine < getChildCount()) {
			return (AbsLineLayout) getChildAt(mCurLine);
		}

		return null;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mCurrentView = null;
				mLongClicked = false;
				mRespondGestured = false;
				mInterceptTouchMoveX = 0;
				mInterceptTouchMoveY = 0;
				mDownPoint.set(x, y);
				mScroller.onTouchEvent(ev, ev.getAction());
				break;

			case MotionEvent.ACTION_MOVE :
				if (mInterceptTouchMoveX < mTouchSlop && mInterceptTouchMoveY < mTouchSlop) {
					mInterceptTouchMoveX = Math.abs(x - mDownPoint.x);
					mInterceptTouchMoveY = Math.abs(y - mDownPoint.y);
				}
				break;

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				mDownPoint.set(-1, -1);
				break;

			default :
				break;
		}

		boolean intercepteTouch = mInterceptTouchMoveX >= mTouchSlop
				|| mInterceptTouchMoveY >= mTouchSlop;
		if (intercepteTouch) {
			// 手势响应
			if (AbsDockView.sPortrait) {
				mRespondGestured = mInterceptTouchMoveY > mInterceptTouchMoveX;
			} else {
				mRespondGestured = mInterceptTouchMoveX > mInterceptTouchMoveY;
			}
			if (!mLongClicked && mRespondGestured && null != mCurrentView) {
				mGestureListner.respondGesture(mCurrentView.getInfo().mGestureInfo.mUpIntent);
				mCurrentView.setmIsBgShow(false);
			}
		}

		return intercepteTouch || !mScroller.isFinished();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mLongClicked || mRespondGestured) {
			return true;
		}
		mScroller.onTouchEvent(event, event.getAction());
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				if (null != mCurrentView) {
					mCurrentView.setmIsBgShow(false);
				}
				break;

			default :
				break;
		}
		return true;
	}

	/**
	 * @param mGestureListner
	 *            the mGestureListner to set
	 */
	public void setmGestureListner(OnDockGestureListner mGestureListner) {
		this.mGestureListner = mGestureListner;
	}

	public void setLongClicked() {
		mLongClicked = true;
	}

	/**
	 * 设置循环模式
	 * 
	 * @param bool
	 *            是否循环
	 */
	public void setCycle(boolean bool) {
		ScreenScroller.setCycleMode(this, bool);
		mScroller.setOvershootPercent(0); // 设置弹跳值
	}

	/**
	 * 跳到某一行
	 * 
	 * @param screen
	 */
	public void snapToScreen(int screen) {
		mScroller.gotoScreen(screen, 450, false);
	}

	@Override
	public void drawScreen(Canvas canvas, int screen) {
		View view = null;
		view = getChildAt(screen);
		if (null != view) {
			view.draw(canvas);
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				if (v instanceof DockIconView) {
					mCurrentView = (DockIconView) v;
					mCurrentView.setmIsBgShow(true);
				}
			}
				break;

			case MotionEvent.ACTION_UP : {
				((DockIconView) v).setmIsBgShow(false);
			}
				break;

			default :
				break;
		}

		return false;
	}

	public DockIconView getCurrentIcon() {
		return mCurrentView;
	}

	public void setCurrentIcon(DockIconView view) {
		mCurrentView = view;
	}

	public boolean isTouching() {
		return mDownPoint.x >= 0 && mDownPoint.y >= 0;
	}

	public Point getDownPoint() {
		return mDownPoint;
	}
}
