package com.jiubang.ggheart.apps.gowidget.gostore.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;

/**
 * Go精品主题大图风格详情页滑动view
 * 
 * @author lijunye
 * 
 */
public class GoStoreDetailScrollViewGroup extends ViewGroup {
	private Context mContext = null;
	private Scroller mScroller = null;
	private float mChildScreenWidthPercent = 0.7f; // 默认子view占据70%的宽度
	private int mGap = DrawUtils.dip2px(20f);
	private int mChildWidth = (int) getResources().getDimension(R.dimen.item_width_noneicons);
	private int mChildHeight = (int) getResources().getDimension(R.dimen.item_height_noneions);
	private int mFirstGap = 0;
	private int mCurrentPage = 0;
	private int mLastPage = -1;
	private GoStoreIndicators mGoStoreIndicators = null;

	public final static int HORIZEN_MOVE = 0;
	public final static int VERTICAL_MOVE = 1;
	public final static int NO_MOVE = 3;
	public final static int CRITICAL_VALUE = 5;

	private boolean mIsVirtical = false;
	private int mFlingSpeed = 2;
	private boolean mGotoScreenAnimaiton = true;
	private int mScreenCount = 0;
	private int mTotalWidth = 0;
	private int mDisplayWidth = 0;
	
	public GoStoreDetailScrollViewGroup(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public GoStoreDetailScrollViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		mScroller = new Scroller(mContext);
		mFirstGap = (int) (DrawUtils.sWidthPixels - mChildWidth) / 2;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mDisplayWidth = DrawUtils.sWidthPixels;
	}

	// 只有当前LAYOUT中的某个CHILD导致SCROLL发生滚动，才会致使自己的COMPUTESCROLL被调用
	@Override
	public void computeScroll() {
		// 如果返回true，表示动画还没有结束
		// 因为前面startScroll，所以只有在startScroll完成时 才会为false
		if (mScroller.computeScrollOffset()) {
			// 产生了动画效果 每次滚动一点
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 刷新View 否则效果可能有误差
			postInvalidate();
		} else {
		}
	}
	// // --------------------------------
	// ///--------------------------------
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;
	// --------------------------
	// 处理触摸事件 ~
	public final static int SNAP_VELOCITY = 300;
	private int mTouchSlop = 0;
	private float mLastionMotionX = 0;
	private float mLastMotionY = 0;
	// 处理触摸的速率
	private VelocityTracker mVelocityTracker = null;
	
	private final static int FLING_LEFT = 1;
	private final static int FLING_RIGHT = 2;
	private final static int MOVE = 0;
	private int mFlingState = MOVE;
	private int mMoveState = -1;
	private final static int MOVE_LEFT = 1;
	private final static int MOVE_RIGHT = 2;

	// 这个感觉没什么作用 不管true还是false 都是会执行onTouchEvent的 因为子view里面onTouchEvent返回false了
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		// 表示已经开始滑动了，不需要走该Action_MOVE方法了(第一次时可能调用)。
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
			case MotionEvent.ACTION_MOVE :
				float moveX = ev.getX();
				float moveY = ev.getY();
				int state = mathXY(moveX, mLastionMotionX, moveY, mLastMotionY);
				if (state == HORIZEN_MOVE) {
					mIsVirtical = false;
				} else {
					mIsVirtical = true;
					return false;
				}
				final int xDiff = (int) Math.abs(mLastionMotionX - x);
				// 超过了最小滑动距离
				if (xDiff > mTouchSlop) {
					mTouchState = TOUCH_STATE_SCROLLING;
				}
				break;

			case MotionEvent.ACTION_DOWN :
				mLastionMotionX = x;
				mLastMotionY = y;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

				break;

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				mTouchState = TOUCH_STATE_REST;
				break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (mIsVirtical) {
			return super.onTouchEvent(event);
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}

		mVelocityTracker.addMovement(event);

		super.onTouchEvent(event);

		// 手指位置地点
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				// 如果屏幕的动画还没结束，你就按下了，我们就结束该动画
				if (mScroller != null) {
					if (!mScroller.isFinished()) {
						mScroller.abortAnimation();
					}
				}

				mLastionMotionX = x;
				break;
			case MotionEvent.ACTION_MOVE :
				int detaX = (int) (mLastionMotionX - x);
				int scrollX = getScrollX();
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);

				int velocityX = (int) velocityTracker.getXVelocity();

				if (checkIfCanMove(detaX, scrollX)) {
					// 滑动速率达到了一个标准(快速向右滑屏，返回上一个屏幕) 马上进行切屏处理
					if (velocityX > SNAP_VELOCITY && mCurrentPage > 0) {
						if (mMoveState == MOVE_RIGHT) {
							mFlingState = FLING_RIGHT;
						} else {
							mFlingState = MOVE;
						}
					}
					// 快速向左滑屏，返回下一个屏幕)
					else if (velocityX < -SNAP_VELOCITY && mCurrentPage < (getChildCount() - 1)) {
						if (mMoveState == MOVE_LEFT) {
							mFlingState = FLING_LEFT;
						} else {
							mFlingState = MOVE;
						}
					}
					// 以上为快速移动的 ，强制切换屏幕
					else {
						// 我们是缓慢移动的，因此先判断是保留在本屏幕还是到下一屏幕
						mFlingState = MOVE;
					}
					scrollBy(detaX, 0);
				} else {
					mFlingState = MOVE;
				}
				mLastionMotionX = x;

				break;
			case MotionEvent.ACTION_UP :
				mMoveState = -1;
				switch (mFlingState) {
					case FLING_RIGHT :
						snapToScreen(mCurrentPage - 1);
						break;
					case FLING_LEFT:
						snapToScreen(mCurrentPage + 1);
						break;
					default :
						snapToDestination();
						break;
				}
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}

				mTouchState = TOUCH_STATE_REST;

				break;
			case MotionEvent.ACTION_CANCEL :
				mTouchState = TOUCH_STATE_REST;
				break;
		}

		return true;
	}
	
	@Override
	public void scrollBy(int x, int y) {
		if (x < 0 && mMoveState < 0) {
			mMoveState = MOVE_RIGHT;
		} else if (x > 0 && mMoveState < 0) {
			mMoveState = MOVE_LEFT;
		}
		super.scrollBy(x, y);
	}

	/**
	 * 缓慢移动
	 */
	private void snapToDestination() {
		int destScreen = (getScrollX() + (mChildWidth + mGap) / 2) / (mChildWidth + mGap);
		snapToScreen(destScreen);
	}

	private void snapToScreen(int whichScreen) {
		// 简单的移到目标屏幕，可能是当前屏或者下一屏幕
		// 需要再次滑动的距离 屏或者下一屏幕的继续滑动距离
		mCurrentPage = whichScreen;
		int childCount = getChildCount();
		if (mCurrentPage > childCount - 1) {
			mCurrentPage = childCount - 1;
		} else if (mCurrentPage < 0) {
			mCurrentPage = 0;
		}
		int dx = mCurrentPage * (mChildWidth + mGap) - getScrollX();
		if (!mGotoScreenAnimaiton) {
			mScroller.startScroll(getScrollX(), 0, dx, 0, 0);
			mGotoScreenAnimaiton = true;
		} else {
			mScroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx) * mFlingSpeed);
		}
		// 此时需要手动刷新View 否则没效果
		invalidate();
		if (mGoStoreIndicators != null) {
			mGoStoreIndicators.setHightlightIndicator(mCurrentPage);
		}
		if (mLastPage < 0) {
			mLastPage = mCurrentPage;
			return;
		} else if (mLastPage == mCurrentPage) {
			return;
		}
		mLastPage = mCurrentPage;
		if (mGroupListener != null) {
			mGroupListener.scrollFinish(mCurrentPage);
		}
	}

	@Override
	public void addView(View child) {
		super.addView(child);
	}
	
	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
	}
	
	@Override
	public void addView(View child, LayoutParams params) {
		super.addView(child, params);
	}
	public void removeAllViews() {
		mScreenCount = 0;
		super.removeAllViews();
	}
	
	// measure过程
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 设置该ViewGroup的大小
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = getMeasuredHeight();
		if (height < mChildHeight) {
			mChildHeight = height;
			mChildWidth = (int) (mChildHeight * 0.6);
			mFirstGap = (int) (DrawUtils.sWidthPixels - mChildWidth) / 2;
		}
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mChildWidth, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mChildHeight, MeasureSpec.EXACTLY);
		measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
	}
	
	// layout过程
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int startTop = (b - t - mChildHeight) / 2; // 间距设置为10px 相当于
													// android：marginTop= "10px"
		int childCount = getChildCount();
		int left = 0; // 每个子视图的起始布局坐标
		mScreenCount = childCount;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			// 即使可见的，才划到屏幕上
			if (child != null && child.getVisibility() != View.GONE) {
				if (i == 0) {
					child.layout(mFirstGap, startTop, mFirstGap + mChildWidth, startTop
							+ mChildHeight);
					left = mFirstGap + mChildWidth + mGap;
				} else {
					child.layout(left, startTop, left + mChildWidth, startTop + mChildHeight);
					left = left + mChildWidth + mGap; // 校准每个子View的起始布局位置
				}
			}

		}
		mTotalWidth = mFirstGap * 2 + (mChildWidth + mGap) * childCount - mGap;
	}

	private boolean checkIfCanMove(int detaX, int scrollX) {
		if (getChildCount() == 1) {
			return false;
		} else if (detaX < 0 && -scrollX > (mDisplayWidth - mChildWidth - mFirstGap)) {
			return false;
		} else if (detaX > 0 && scrollX > (mTotalWidth - mChildWidth - mFirstGap)) {
			return false;
		} else {
			return true;
		}
	}

	public static int mathXY(float x, float lastX, float y, float lastY) {
		int state = -1;
		if ((Math.abs(y - lastY) / Math.abs(x - lastX)) >= 0.58) {
			state = VERTICAL_MOVE;
		} else {
			state = HORIZEN_MOVE;
		}
		if (Math.sqrt(Math.pow(y - lastY, 2) + Math.pow(x - lastX, 2)) <= CRITICAL_VALUE) {
			state = NO_MOVE;
		}
		return state;
	}

	//////////////////////对外设置方法////////////////////////////////
	/**
	 * 默认滑动行为，有滑动效果
	 * 
	 * @param page
	 *            滑动目标页数
	 */
	public void goToScreen(int page) {
		mGotoScreenAnimaiton = true;
		snapToScreen(page);
	}

	/**
	 * 滑动行为，可设置是否有滑动效果
	 * 
	 * @param page
	 *            滑动目标页数
	 * @param b
	 *            true有滑动效果，false没有
	 */
	public void goToScreen(int page, boolean b) {
		mGotoScreenAnimaiton = b;
		snapToScreen(page);
	}
	
	/**
	 * 直接跳转到相应页，没有滑动效果
	 * 
	 * @param page
	 *            滑动目标页数
	 */
	public void goToScreenDirectly(int page) {
		mGotoScreenAnimaiton = false;
		snapToScreen(page);
	}

	/**
	 * 获取当前显示页
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		return mCurrentPage;
	}

	/**
	 * 子view的宽，在addview之前设置
	 * 
	 * @param width
	 */
	public void setChildWidth(int width) {
		//		mChildWidth = width;
	}

	/**
	 * 子view的高度，在addview之前设置
	 * 
	 * @param height
	 */
	public void setChildHeight(int height) {
		//		mChildHeight = height;
		//		mFirstGap = (int) (DrawUtils.sWidthPixels - mChildWidth) / 2;
	}

	/**
	 * 子view占父view的宽度百分比，在addview之前设置
	 * 
	 * @param percent
	 */
	public void setChildWidthPercent(float percent) {
		mChildScreenWidthPercent = percent;
		mChildWidth = (int) (DrawUtils.sWidthPixels * mChildScreenWidthPercent);
	}

	/**
	 * 子view之间的间隙
	 * 
	 * @param percent
	 */
	public void setGap(int gap) {
		mGap = gap;
	}

	/**
	 * 设置指示器
	 * 
	 * @param goStoreIndicators
	 */
	public void setIndicators(GoStoreIndicators goStoreIndicators) {
		mGoStoreIndicators = goStoreIndicators;
	}

	/**
	 * 设置滑动动画速度
	 * 
	 * @param goStoreIndicators
	 */
	public void setFlingSpeed(int speed) {
		if (speed < 0) {
			speed = 0;
		}
		mFlingSpeed = speed;
	}
	
	/**
	 * <br>功能简述:左右调整
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void fixView() {
		snapToDestination();
	}
	
	private ViewGroupListener mGroupListener = null;
	public void setScrollFinish(ViewGroupListener groupListener) {
		mGroupListener = groupListener;
	}
	
	/**
	 * 
	 * <br>类描述: 用于滑动结束后图片回收
	 * <br>功能详细描述:
	 * 
	 * @author  lijunye
	 * @date  [2012-12-19]
	 */
	public interface ViewGroupListener {
		public void scrollFinish(int page);
	}
}
