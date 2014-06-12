/**
 * 
 */
package com.jiubang.ggheart.apps.desks.ggmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.go.util.graphics.effector.subscreen.SubScreenContainer;
import com.go.util.graphics.effector.subscreen.SubScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.framework.ICleanable;

/**
 * @author ruxueqin
 * 
 */
public class GlMenuGridViewsContainer extends RelativeLayout
		implements
			ScreenScrollerListener,
			ICleanable,
			SubScreenContainer,
			OnGestureListener {
	private GGMenuGridView[] mGridViews;

	// GlMenuTabView的监听器，用于滑动时，改变tab栏的状态
	private TabViewListener mListener;

	// 1屏不滚动，不构造mScroller
	private ScreenScroller mScroller;
	// 注意：创建滑动的动画，不能删除
	private SubScreenEffector mEffector;

	private int mTabCount;

	// 标记是否在滑动
	private boolean mIsMoveTouch;

	protected GestureDetector mGestureDetector;

	private ScrollerAddViewListener mAddViewListener;

	/**
	 * @param context
	 * @param attrs
	 */
	public GlMenuGridViewsContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mGestureDetector = new GestureDetector(this);
	}

	public GGMenuGridView[] getmGridViews() {
		return mGridViews;
	}

	public void setmGridViews(int lenght) {
		// 生成一个循环滚动滚动器,1屏不滚动
		ScreenScroller.setCycleMode(this, true);
		mScroller.setDuration(450);
		mScroller.setScreenCount(lenght);
		mScroller.setBackgroundAlwaysDrawn(true);
		mEffector = new SubScreenEffector(mScroller);
	}

	public void setAddViewListener(ScrollerAddViewListener addViewListener) {
		mAddViewListener = addViewListener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int maxheight = 0;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childheight = child.getMeasuredHeight();
			if (maxheight < childheight) {
				maxheight = childheight;
			}
		}
		setMeasuredDimension(getMeasuredWidth(), maxheight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width = r - l;
		int height = b - t;
		int childcount = getChildCount();

		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		for (int i = 0; i < childcount; i++) {
			right = left + width;
			bottom = top + height;
			View childView = getChildAt(i);
			childView.layout(left, top, right, bottom);
			left += width;
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (null != mScroller) {
			mScroller.invalidateScroll();
		}
		if (null != mScroller && !mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null != mScroller) {
			mScroller.onTouchEvent(event, event.getAction());
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (null == mScroller) {
			return false;
		} else if (!mScroller.isFinished()) {
			return true;
		}

		mGestureDetector.onTouchEvent(ev);
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mIsMoveTouch = false;
				mScroller.onTouchEvent(ev, ev.getAction());
				break;

			case MotionEvent.ACTION_MOVE :
				break;

			case MotionEvent.ACTION_UP :
				break;

			default :
				break;
		}
		return mIsMoveTouch;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (null != mScroller) {
			mScroller.setScreenSize(w, h);
		}
	}

	@Override
	public void computeScroll() {
		if (null != mScroller) {
			mScroller.computeScrollOffset();
		}
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

	}

	@Override
	public void onFlingStart() {
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mListener.changeTabStateMent(newScreen);
	}

	@Override
	public void onScrollFinish(int currentScreen) {
	}

	public void gotoTab(int tabId) {
		mScroller.gotoScreen(tabId, 300, true);
	}

	public void setTabCount(int count) {
		mTabCount = count;
	}

	public int getTabCount() {
		return mTabCount;
	}

	public void setTabViewListener(TabViewListener listener) {
		mListener = listener;
	}

	@Override
	public void cleanup() {
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			//由于分页加载。空白页首先加入了一个LinearLayout
			if (view instanceof LinearLayout) {
				if (((LinearLayout) view).getChildCount() > 0) {
					View gridview = ((LinearLayout) view).getChildAt(0);
					if (gridview instanceof GGMenuGridView) {
						Adapter adapter = ((GGMenuGridView) gridview).getAdapter();
						if (adapter instanceof GGMenuApdater) {
							((GGMenuApdater) adapter).cleanup();
						}
					}
				}
			} else if (view instanceof GGMenuGridView) {
				Adapter adapter = ((GGMenuGridView) view).getAdapter();
				if (adapter instanceof GGMenuApdater) {
					((GGMenuApdater) adapter).cleanup();
				}
			}
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen) {
		//当刷新的时候判断是否已经有View。没有就添加View进去
		//不在onScrollStart操作是因为快速滑动有可能不执行onScrollStart方法。而且不容易获取添加的页面
		if (mAddViewListener != null) {
			mAddViewListener.addView(screen);
		}

		View child = getChildAt(screen);
		if (null != child) {
			child.draw(canvas);
		}
	}

	@Override
	public void drawScreen(Canvas canvas, int screen, int alpha) {

	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (e1 == null || e2 == null) {
			return false;
		}

		boolean re = isNeedMove(e1.getX(), e2.getX(), e1.getY(), e2.getY());
		if (re) {
			mIsMoveTouch = true;
			cancelLongPress();
			return true;
		}
		return false;
	}

	private boolean isNeedMove(float x1, float x2, float y1, float y2) {
		boolean ret = true;
		if (Math.abs(y1 - y2) > Math.abs(x1 - x2)) {
			ret = false;
		}
		return ret;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
}
