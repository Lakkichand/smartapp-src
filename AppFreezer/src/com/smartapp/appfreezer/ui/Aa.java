package com.smartapp.appfreezer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 可手势滑动的横向LinearLayout
 * 
 * @author wangzhuobin
 */
public class Aa extends ViewGroup implements
		ScreenScrollerListener {

	// 触屏状态
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;

	// 当前触屏状态
	private int mTouchState = TOUCH_STATE_REST;
	// 上次触屏离开的x坐标
	private float mLastMotionX;
	private float mlastMotionY;

	private ScreenScroller mScreenScroller = null;
	private boolean mIsRecycle = false;
	private ScreenScrollerListener mScreenScrollerListener = null;

	public Aa(Context context, AttributeSet attrs,
			ScreenScrollerListener screenScrollerListener) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context, screenScrollerListener);
	}

	public Aa(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
		initScreenScroller(context);
	}

	public Aa(Context context,
			ScreenScrollerListener screenScrollerListener) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context, screenScrollerListener);
	}

	public void setScreenScrollerListener(
			ScreenScrollerListener screenScrollerListener) {
		mScreenScrollerListener = screenScrollerListener;
	}

	private void init(Context context,
			ScreenScrollerListener screenScrollerListener) {
		// TODO Auto-generated method stub
		mScreenScrollerListener = screenScrollerListener;
		initView(context);
		initScreenScroller(context);
	}

	/**
	 * 初始化与View相关的东西
	 * 
	 * @param context
	 */
	private void initView(Context context) {
		// TODO Auto-generated method stub
	}

	private void initScreenScroller(Context context) {
		// TODO Auto-generated method stub
		final int m450 = 450;
		mScreenScroller = new ScreenScroller(this);
		mScreenScroller.setDuration(m450);
		// 设置总屏数
		mScreenScroller.setScreenCount(1);
	}

	/**
	 * 此方法返回false，则手势事件会向子控件传递；返回true，则调用onTouchEvent方法。
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mLastMotionX = x;
			mlastMotionY = y;
			mTouchState = (mScreenScroller != null && mScreenScroller
					.isFinished()) ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final int m15 = 15;
			final int xoffset = (int) (x - mLastMotionX);
			final int yoffset = (int) (y - mlastMotionY);
			if (Math.abs(yoffset) < Math.abs(xoffset)
					&& Math.abs(xoffset) > m15) {
				mTouchState = TOUCH_STATE_SCROLLING;
				if (mScreenScroller != null) {
					mScreenScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		default:
			break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mScreenScroller == null) {
			return true;
		}
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mScreenScroller.onTouchEvent(event, action);
			break;

		case MotionEvent.ACTION_MOVE:
			mScreenScroller.onTouchEvent(event, action);
			break;

		case MotionEvent.ACTION_UP:
			mScreenScroller.onTouchEvent(event, action);
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mScreenScroller.onTouchEvent(event, action);
			mTouchState = TOUCH_STATE_REST;
			break;

		default:
			break;
		}
		return true;
	}

	public void addScreenView(View view) {
		if (!mIsRecycle && view != null) {
			this.addView(view);
		}
	}

	public void gotoViewByIndex(int index) {
		if (index < 0) {
			return;
		}
		if (mScreenScroller != null) {
			mScreenScroller.gotoScreen(index, 500, true);
		}
	}

	public int getCurrentViewIndex() {
		int index = -1;
		if (mScreenScroller != null) {
			index = mScreenScroller.getDstScreen();
		}
		return index;
	}

	public View getCurrentView() {
		View view = null;
		if (mScreenScroller != null) {
			view = this.getChildAt(mScreenScroller.getDstScreen());
		}
		return view;
	}

	public void setScreenCount(int count) {
		if (mScreenScroller != null) {
			mScreenScroller.setScreenCount(count);
		}
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScreenScroller;
	}

	@Override
	public void setScreenScroller(ScreenScroller screenScroller) {
		this.mScreenScroller = screenScroller;
	}

	// private OnImageChangeListener imagechangeListener;
	// public void setOnImageChangeListener(OnImageChangeListener l)
	// {
	// imagechangeListener = l;
	// }
	//
	// public void destory(){
	// mIsRecycle = true;
	// mScreenScroller = null;
	// mScreenScrollerListener = null;
	// imagechangeListener = null;
	// }
	/*----------------------------------滚动器的监听器的回调方法----------------------------------------------*/
	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onFlingIntercepted();
		}
	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScrollStart();
		}
	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onFlingStart();
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScrollChanged(newScroll, oldScroll);
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScreenChanged(newScreen, oldScreen);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScrollFinish(currentScreen);
		}

		// if(imagechangeListener!=null)
		// imagechangeListener.onImageChange();
	}

	/*----------------------------------滚动器的动作监听操作----------------------------------------------*/
	@Override
	public void computeScroll() {
		if (mScreenScroller != null) {
			mScreenScroller.computeScrollOffset();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// newC
		mScreenScroller.setScreenSize(w, h);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getChildCount();
		for (int i = 0; i < size; i++) {
			View childView = getChildAt(i);
			if (childView == null || childView.getLayoutParams() == null) {
				continue;
			}
			if (childView.getVisibility() != GONE) {
				childView.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int childLeft = 0;
		final int childWidth = r - l;
		int childHeight = 0;
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				childHeight = child.getMeasuredHeight();
				child.layout(childLeft, 0, childLeft + childWidth, childHeight);
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// if(mScreenScroller != null){
		// mScreenScroller.invalidateScroll();
		// }
		super.dispatchDraw(canvas);
	}
}
