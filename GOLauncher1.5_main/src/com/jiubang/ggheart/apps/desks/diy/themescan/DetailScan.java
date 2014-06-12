/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IDetailScanHandler;

/**
 * @author ruxueqin
 * 
 */
public class DetailScan extends ViewGroup implements ScreenScrollerListener, ICleanable {
	protected int mLayoutWidth = 0;
	protected int mLayoutHeight = 0;

	// 当前屏为主题信息
	protected final static int THEME_INFO_SCREEN = 0;
	// 增加主题信息屏数
	protected final static int ADD_INFO_SCREEN = 1;

	/**
	 * 滚动器
	 */
	protected ScreenScroller mScroller;
	protected int mCurrentScreen = 0;
	protected int mTotalScreenNum = 0;

	protected IDetailScanHandler mScanHandler; // 用于通知更新指示器

	protected Object mInfoBean;
	
	private float mLastX = 0;
	private float mLastY = 0;
	
	/**
	 * @return the mInfoBean
	 */
	public Object getmInfoBean() {
		return mInfoBean;
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public DetailScan(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	@Override
	public void cleanup() {

	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// mIsScreenChanged = true;
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mCurrentScreen = currentScreen;
	}

	/**
	 * @param mScanHandler
	 *            the mScanHandler to set
	 */
	public void setIndicatorHandler(IDetailScanHandler mScanHandler) {
		this.mScanHandler = mScanHandler;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mScroller = new ScreenScroller(getContext(), this);
		mScroller.setDuration(450);
	}

	protected void initData() {

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
		mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//注意：目前主题预览中的主题详细介绍由于只有一个View，所以不要左右移动
		//所以直接跳过	mScroller.onTouchEvent(ev, ev.getAction());的处理
		if (getChildCount() == 1) {
			return false;
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mLastX = ev.getX();
			mLastY = ev.getY();
		} else if (ev.getAction() == MotionEvent.ACTION_CANCEL
				|| ev.getAction() == MotionEvent.ACTION_UP) {
			float x = ev.getX();
			float y = ev.getY();
			if (Math.abs(x - mLastX) > 50
					|| Math.abs(y - mLastY) > 50) {
				return mScroller.onTouchEvent(ev, ev.getAction());
				
			} else {
				mScroller.onTouchEvent(ev, ev.getAction());
				return super.onInterceptTouchEvent(ev);
			}
		}
		mScroller.onTouchEvent(ev, ev.getAction());
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		AbstractFrame frame = GoLauncher.getTopFrame();
		int action = event.getAction();
		// if(frame != null && frame.getId() ==
		// IDiyFrameIds.SCREEN_EDIT_BOX_FRAME
		// && (event.getAction() == MotionEvent.ACTION_UP
		// || event.getAction() == MotionEvent.ACTION_CANCEL)
		// && mScroller.isScrollAtEnd())
		// {
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
		// IDiyMsgIds.PICK_WIDGET_START_ROTATION, 0, null, null);
		// }
		// else
		// {
		mScroller.onTouchEvent(event, action);
		// }

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurrentScreen = newScreen;
		if (null != mScanHandler) {
			mScanHandler.updateIndicatorCurrent(mCurrentScreen);
		}
	}

	public int getScreenCount() {
		return mTotalScreenNum;
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}
	
	public void setCurrentScreen(int currentScreen) {
		mCurrentScreen = currentScreen;
	}

	/**
	 * @param screen
	 * @param noElastic
	 *            是否使用弹性效果
	 * @param duration
	 *            小于0则自动计算时间
	 */
	public void snapToScreen(int screen, boolean noElastic, int duration) {
		mScroller.gotoScreen(screen, duration, noElastic);
	}

	public void setInfoBean(Object bean) throws IllegalArgumentException {
		mInfoBean = bean;
		initData();
	}

	public void gotoInfo() {
		mScroller.gotoScreen(THEME_INFO_SCREEN, 300, true);
	}

	public void gotoInfoNoAnimation() {
		mScroller.setCurrentScreen(THEME_INFO_SCREEN);
	}

	public void gotoImage(int oldScreen) {
		mScroller.gotoScreen(oldScreen, 300, true);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		super.dispatchDraw(canvas);
	}
}
