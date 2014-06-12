package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 
 * @author 
 *
 */
public class GuidePageScroller extends ViewGroup implements ScreenScrollerListener {
	private ScreenScroller mScroller;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	// 当前显示的是第几屏
	private int mCurrentScreen = 0;
	private boolean mRespondMove = true; // 是否响应移位
	// 上次触屏离开的x坐标
	private float mLastMotionX;
	// 当前触屏状态
	private int mTouchState = TOUCH_STATE_REST;
	private Activity mActivity;
	private GuideImageScroller mImageScroller;
	private static final int DURATION_TIME = 300;
	//记录是否进入ＧＯ精品引导页
	private boolean mIsInToPage_For_Gostore = false;

	public GuidePageScroller(Context context) {
		super(context);
		mActivity = (Activity) context;
		mScroller = new ScreenScroller(context, this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setMaxOvershootPercent(0);
		// mScroller.setInterpolator(new DecelerateInterpolator(1.5f));
		mScroller.setDuration(DURATION_TIME);
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
		// 设置总屏数
		mScroller.setScreenCount(childCount);

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

	}

	@Override
	public void onScrollStart() {
		postInvalidate();

	}

	@Override
	public void onFlingStart() {

		View focusedChild = getFocusedChild();
		if (focusedChild != null && mScroller.getDstScreen() != mCurrentScreen) {
			focusedChild.clearFocus();
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurrentScreen = newScreen;

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mCurrentScreen = currentScreen;
		if (currentScreen == getChildCount() - 1) {
//			GoGuideActivity.showTipsDialog();
//			mActivity.finish();
			if (!mIsInToPage_For_Gostore) {
				mIsInToPage_For_Gostore = true;
				StatisticsData.saveStatData(mActivity,
						StatisticsData.GUIDE_PAGE_FOR_GOSTORE,
						StatisticsData.ENTER_GUIDE_PAGE_FOR_GOSTORE);
				StatisticsData.saveStatData(mActivity,
						StatisticsData.GUIDE_ENTER_GUIDE_PAGE,
						StatisticsData.GUIDE_ENTER_GUIDE_PAGE_ID);
			}
			GuideControler guideCloudView = GuideControler.getInstance(mActivity);
			guideCloudView.showCustomGesture();
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mScroller.invalidateScroll();
		super.dispatchDraw(canvas);
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX();
		if (mImageScroller != null
				&& mScroller.isFinished()
				&& mScroller.getDstScreen() == 0
				&& mImageScroller.getScreenScroller().getDstScreen() != mImageScroller
						.getChildCount() - 1) {
			return false;
		}
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if (action == MotionEvent.ACTION_MOVE) {
			if (mImageScroller != null && mScroller.isFinished() && x > mLastMotionX
					&& mScroller.getDstScreen() == 0) {
				return false;
			}
		}
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				mRespondMove = true;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
			// 如果当前是最后一页，并且是右滑，则不执行
			final int xoffset = (int) (x - mLastMotionX);
			if (getmCurrentScreen() == getChildCount() - 1 && xoffset < 0) {
				break;
			}
				
				if (Math.abs(xoffset) > DrawUtils.sTouchSlop && mRespondMove) {
					mTouchState = TOUCH_STATE_SCROLLING;
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL :
				final int xoffset = (int) (x - mLastMotionX);
				if (Math.abs(xoffset) > DrawUtils.sTouchSlop && mRespondMove) {
					mTouchState = TOUCH_STATE_SCROLLING;
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}
				break;
			case MotionEvent.ACTION_UP : {
				mTouchState = TOUCH_STATE_REST;
				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_MOVE :
			// 如果当前是最后一页，并且是右滑，则不执行
			int xoffset = (int) (event.getX() - mLastMotionX);
			if (getmCurrentScreen() == getChildCount() - 1 && xoffset < 0) {
				break;
			}
				mScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_UP :
				mScroller.onTouchEvent(event, action);
				mTouchState = TOUCH_STATE_REST;
				break;
			case MotionEvent.ACTION_CANCEL :
				mTouchState = TOUCH_STATE_REST;
				break;

			default :
				break;
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
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

	public int getmCurrentScreen() {
		return mCurrentScreen;
	}

	public void setImageScroll(GuideImageScroller imageScroller) {
		if (imageScroller instanceof GuideImageScroller) {
			mImageScroller = imageScroller;
		}
	}

	public void gotoScreen(int dstScreen, int duration, boolean noElastic) {
		mScroller.gotoScreen(dstScreen, duration, noElastic);
	}

	
}
