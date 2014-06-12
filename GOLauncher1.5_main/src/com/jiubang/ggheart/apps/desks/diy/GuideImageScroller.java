package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
/**
 * 
 * @author 
 *
 */
public class GuideImageScroller extends ViewGroup implements ScreenScrollerListener {

	private ScreenScroller mScroller;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	// 当前显示的是第几屏
	private int mCurrentScreen = 0;
	private int mLastScreen = 0;
	private boolean mRespondMove = true; // 是否响应移位
	// 上次触屏离开的x坐标
	private float mLastMotionX;
	// 当前触屏状态
	private int mTouchState = TOUCH_STATE_REST;
	private Activity mActivity;

	public GuideImageScroller(Context context) {
		super(context);
		mActivity = (Activity) context;
		mScroller = new ScreenScroller(context, this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setMaxOvershootPercent(0);
		mScroller.setDuration(300);
		// mScroller.setInterpolator(new DecelerateInterpolator(1.5f));
	}

	public GuideImageScroller(Context context, AttributeSet set) {
		super(context, set);
		mActivity = (Activity) context;
		mScroller = new ScreenScroller(context, this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setDuration(300);
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
		mScroller.setScreenCount(getChildCount());
		ScreenScroller.setCycleMode(this, false);
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
		// 更新指示器
		mCurrentScreen = newScreen;
		((GoGuideActivity) mActivity).updateIndicator(newScreen);
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mCurrentScreen = currentScreen;
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
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				mRespondMove = true;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				final int xoffset = (int) (x - mLastMotionX);
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
		// newC
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

	public void gotoScreen(int dstScreen, int duration, boolean noElastic) {
		mScroller.gotoScreen(dstScreen, duration, noElastic);
	}

	public void setView() {
		Resources resources = getContext().getResources();
		int count = getChildCount();
		for (int position = 0; position < count; position++) {
			View view = getChildAt(position);
			ImageView img = (ImageView) view.findViewById(R.id.img_guid);
			TextView txt2 = (TextView) view.findViewById(R.id.guide_txt2);
			switch (position) {
				case 0 :
					txt2.setText(R.string.guid_new_theme_title);
					img.setImageDrawable(resources.getDrawable(R.drawable.guide001));
					txt2.setVisibility(View.VISIBLE);
					break;
				case 1 :
					txt2.setText(R.string.guid_new_operation_title);
					img.setImageDrawable(resources.getDrawable(R.drawable.guide002));
					txt2.setVisibility(View.VISIBLE);
					break;
				case 2 :
					txt2.setText(R.string.guid_new_manager_title);
					img.setImageDrawable(resources.getDrawable(R.drawable.guide003));
					txt2.setVisibility(View.VISIBLE);
					break;
				case 3 :
					img.setImageDrawable(null);
					break;
				default :
					img.setImageDrawable(null);
					break;
			}
		}
	}

	public int getmLastScreen() {
		return mLastScreen;
	}
}
