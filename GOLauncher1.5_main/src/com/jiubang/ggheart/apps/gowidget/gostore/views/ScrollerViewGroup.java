package com.jiubang.ggheart.apps.gowidget.gostore.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.gowidget.gostore.component.OnImageChangeListener;

/**
 * 可手势滑动的横向ViewGroup
 * 
 * @author wangzhuobin
 * 
 */
//CHECKSTYLE:OFF
public class ScrollerViewGroup extends ViewGroup implements ScreenScrollerListener {

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

	/**
	 * 跟页面间的间隙绘制有关的变量
	 */
	// 是否绘制间隙，默认不绘制
	private boolean mIsNeedGap = false;
	// 间隙的宽度，默认是10dp，在hdpi的手机上就是15px
	private int mGapWidth = DrawUtils.dip2px(10.0f);
	// 绘制间隙使用的画笔
	private Paint mPaint = null;
	// 间隙的区域
	private Rect mRect = null;
	// 间隙的颜色值,默认为黑色
	private int mGapColor = Color.BLACK;

	public ScrollerViewGroup(Context context, AttributeSet attrs,
			ScreenScrollerListener screenScrollerListener) {
		super(context, attrs);
		init(context, screenScrollerListener);
	}

	public ScrollerViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, null);
	}

	public ScrollerViewGroup(Context context, ScreenScrollerListener screenScrollerListener) {
		super(context);
		init(context, screenScrollerListener);
	}

	private void init(Context context, ScreenScrollerListener screenScrollerListener) {
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
		mScreenScroller = new ScreenScroller(context, this);
		mScreenScroller.setDuration(450);
		// 设置总屏数
		mScreenScroller.setScreenCount(1);
	}

	/**
	 * 此方法返回false，则手势事件会向子控件传递；返回true，则调用onTouchEvent方法。
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScreenScroller != null && mScreenScroller.getScreenCount() == 1) {
			return false;
		}
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mlastMotionY = y;
				mTouchState = (mScreenScroller != null && mScreenScroller.isFinished())
						? TOUCH_STATE_REST
						: TOUCH_STATE_SCROLLING;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				int currentScreen = mScreenScroller.getCurrentScreen();
				final int xoffset = (int) (x - mLastMotionX);
				final int yoffset = (int) (y - mlastMotionY);
				if ((currentScreen == getChildCount() - 1 && xoffset < 0)
						|| (currentScreen == 0 && xoffset > 0)) {
					break;
				}
				if (Math.abs(yoffset) < Math.abs(xoffset)
						&& Math.abs(xoffset) > DrawUtils.sTouchSlop) {
					mTouchState = TOUCH_STATE_SCROLLING;
					if (mScreenScroller != null) {
						mScreenScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
					}
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mScreenScroller == null) {
			return true;
		} else if (mScreenScroller.getScreenCount() == 1) {
			return true;
		}
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mScreenScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_MOVE :
				mScreenScroller.onTouchEvent(event, action);
				break;

			case MotionEvent.ACTION_UP :
				mScreenScroller.onTouchEvent(event, action);
				mTouchState = TOUCH_STATE_REST;
				break;
			case MotionEvent.ACTION_CANCEL :
				mScreenScroller.onTouchEvent(event, action);
				mTouchState = TOUCH_STATE_REST;
				break;

			default :
				break;
		}
		return true;
	}

	public void addScreenView(View view) {
		if (!mIsRecycle && view != null) {
			this.addView(view);
		}
	}

	/**
	 * 通过下标跳转到某一页的方法 有500毫秒的持续时间
	 * 
	 * @param index
	 */
	public void gotoViewByIndex(int index) {
		if (mScreenScroller != null) {
			mScreenScroller.gotoScreen(index, 500, true);
		}
	}

	/**
	 * 通过下标立马跳转到某一页的方法
	 * 
	 * @param index
	 */
	public void gotoViewByIndexImmediately(int index) {
		if (mScreenScroller != null) {
			mScreenScroller.gotoScreen(index, 0, true);
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

	/**
	 * <br>功能简述:设置屏幕滚动到边缘页面时的滚动缓冲范围
	 * <br>功能详细描述:
	 * <br>注意:padding 限制范围在[0, 0.5]
	 * @param padding 限制范围在[0, 0.5]
	 */
	public void setPadding(float padding) {
		if (mScreenScroller != null) {
			mScreenScroller.setPadding(padding);
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

	private OnImageChangeListener imagechangeListener;

	public void setOnImageChangeListener(OnImageChangeListener l) {
		imagechangeListener = l;
	}

	public void destory() {
		mIsRecycle = true;
		mScreenScroller = null;
		mScreenScrollerListener = null;
		imagechangeListener = null;
	}

	/*----------------------------------滚动器的监听器的回调方法----------------------------------------------*/
	@Override
	public void onFlingIntercepted() {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onFlingIntercepted();
		}
	}

	@Override
	public void onScrollStart() {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScrollStart();
		}
	}

	@Override
	public void onFlingStart() {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onFlingStart();
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScrollChanged(newScroll, oldScroll);
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScreenChanged(newScreen, oldScreen);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		if (mScreenScrollerListener != null) {
			mScreenScrollerListener.onScrollFinish(currentScreen);
		}

		if (imagechangeListener != null) {
			imagechangeListener.onImageChange();
		}
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
		if (mScreenScroller != null) {
			mScreenScroller.invalidateScroll();
		}
		if (!drawPageGap(canvas)) {
			// 绘制间隙失败，使用正常的方法进行绘制
			super.dispatchDraw(canvas);
		}
	}

	/**
	 * 绘制间隙和子页面的方法
	 * 
	 * @param canvas
	 * @return 如果返回true,则绘制成功。如果返回false,则绘制失败
	 */
	private boolean drawPageGap(Canvas canvas) {
		boolean result = false;
		if (mIsNeedGap && !mScreenScroller.isFinished()) {
			// 只有在需要，并且屏幕滚动的情况下，才去绘制间隙
			int childCount = getChildCount();
			if (childCount > 1) {
				// 要大于一屏才绘制间隙
				// 设置绘制画笔
				if (null == mPaint) {
					mPaint = new Paint();
				}
				mPaint.setColor(mGapColor);
				int scrollX = getScrollX();
				int width = getWidth();
				// 计算要绘制页的下标
				int pageIndex = scrollX / width;
				int lastPageIndex = childCount - 1;
				int height = getHeight();
				View child = null;
				for (int i = 0; i < childCount; i++) {
					child = getChildAt(i);
					drawChild(canvas, child, 0);
					if (i == pageIndex && i != lastPageIndex) {
						// 不是最后一页才绘制
						int right = child.getRight();
						int top = child.getTop();
						if (null == mRect) {
							mRect = new Rect();
						}
						mRect.set(right, top, right + mGapWidth, top + height);
						canvas.drawRect(mRect, mPaint);
						canvas.translate(mGapWidth, 0);
					}
				}
				result = true;
			}
		}
		return result;
	}

	/**
	 * 功能简述: 是否让ViewGroup循环
	 * 功能详细描述:
	 * 注意:
	 * @return
	 */
	public void setCircle(boolean status) {
		ScreenScroller.setCycleMode(this, status);
	}

	// //////////////////////////////////////////////////////////////get、set方法///////////////////////////////////////////////////////////

	public boolean isNeedGap() {
		return mIsNeedGap;
	}

	public void setIsNeedGap(boolean isNeedGap) {
		this.mIsNeedGap = isNeedGap;
	}

	public int getGapWidth() {
		return mGapWidth;
	}

	public void setGapWidth(int gapWidth) {
		this.mGapWidth = gapWidth;
	}

	public int getGapColor() {
		return mGapColor;
	}

	public void setGapColor(int color) {
		this.mGapColor = color;
	}

	public void recyle(ScreenScrollerListener listener) {
		mScreenScrollerListener = null;
		mScreenScroller = null;
	}

	public void setScreenScrollerListener(ScreenScrollerListener mScreenScrollerListener) {
		this.mScreenScrollerListener = mScreenScrollerListener;
	}
}
