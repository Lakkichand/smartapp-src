package com.zhidian3g.wifibox.view.scroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 分屏视图的滚动器。 默认滚动到两端会阻塞(block)停留在该端， 除非调用
 * {@link #setCycleMode(ScreenScrollerListener, boolean)}方法。
 * 
 * @author dengweiming
 * 
 */
public class ScreenScroller extends MScroller {

	/** 无效索引值 */
	public static final int INVALID_SCREEN = -1;

	protected FastVelocityTracker mVelocityTracker;
	protected int mFlingVelocity;
	protected int mFlingVelocityX;
	protected int mFlingVelocityY;

	protected ScreenScrollerListener mListener;
	protected Interpolator mInterpolator;
	protected Interpolator mInterpolatorBak;

	protected ScreenScrollerEffector mEffector;

	protected int mMinScroll;
	protected int mMaxScroll;
	protected int mLastScreenPos; // 最后一屏的位置
	protected int mOldScroll;
	protected int mScrollRange;
	protected int mTotalSize;
	protected float mScrollRatio;
	protected float mTotalSizeInv;
	protected float mScreenCountInv;
	protected float mPaddingFactor = 0.5f;

	protected int mScreenCount = 1; // 为了防止除0，初始化为1
	protected int mScreenWidth;
	protected int mScreenHeight;
	protected int mScreenSize = 1; // 为了防止除0，初始化为1
	protected int mCurrentScreen;
	protected int mDstScreen;
	protected boolean mIsOvershooting;

	protected int mTouchDownScreen;
	protected int mTouchDownP;
	protected int mTouchDownX;
	protected int mTouchDownY;
	protected int mTouchDownScrollP;
	protected int mLastTouchP;
	protected int mLastTouchX;
	protected int mLastTouchY;

	protected int mScrollingDuration = 1000; // 切屏最大时间限制
	protected int mDecelerateDuration = 500;

	protected PorterDuffColorFilter mColorFilter;
	protected int mBackgroundWidth;
	protected int mBackgroundHeight;
	protected int mScreenOffsetY;
	protected int mBackgroundOffsetY;
	boolean mBackgroundScrollEnabled = true;
	boolean mUseEffectorMaxOvershootPercent = true;
	protected boolean mCycloid;
	protected boolean mBgAlwaysDrawn;
//	protected Drawable mBackgroundDrawable;
//	protected Bitmap mBitmap;
//	protected Paint mPaint;

	protected int mMaxOverShootPercent = 49;
	protected int mOverShootPercent;

	private float mLayoutScale = 1.0f;
	protected IWallpaperDrawer mWallpaperDrawer;

	/**
	 * 构造滚动器并绑定到一个分屏视图上，注意还要另外调用 {@link #setScreenSize}, {@link #setScreenCount}
	 * 方法。
	 * 
	 * @param context
	 *            可以为null，此时使用默认的touch slop
	 * @param screenGroup
	 */
	public ScreenScroller(Context context, ScreenScrollerListener screenGroup) {
		this(context, screenGroup, null);
	}

	/**
	 * 构造滚动器并绑定到一个分屏视图上，注意还要另外调用 {@link #setScreenSize}, {@link #setScreenCount}
	 * 方法。
	 * 
	 * @param context
	 *            可以为null，此时使用默认的touch slop
	 * @param screenGroup
	 * @param tracker
	 *            外部传入的触摸速度检测器，如果为null则内部建立一个
	 */
	public ScreenScroller(Context context, ScreenScrollerListener screenGroup,
			FastVelocityTracker tracker) {
		super(context);
		assert screenGroup != null; // 如果为null也就没任何意义了
		mInterpolatorBak = MScroller.DEFAULT_INTERPOLATOR;
		mInterpolator = mInterpolatorBak;
		mListener = screenGroup;
		mVelocityTracker = tracker != null ? tracker : new FastVelocityTracker();
	}

	/**
	 * 设置屏幕大小
	 * 
	 * @param width
	 * @param height
	 */
	public void setScreenSize(int width, int height) {
		abortAnimation();
		if (mScreenWidth == width && mScreenHeight == height) {
			return;
		}
		if (width <= 0 || height <= 0) {
			return;
		}
		mScreenWidth = width;
		mScreenHeight = height;
		setBackgroundOffsetY();
		updateSize();
	}

	/**
	 * 设置滚动方向，默认为{@link #HORIZONTAL}
	 * 
	 * @param orientation
	 *            取值为{@link #HORIZONTAL}, {@link #VERTICAL}
	 */
	@Override
	public void setOrientation(int orientation) {
		abortAnimation();
		if (orientation == mOrientation) {
			return;
		}
		// 取消原来方向的偏移量
		mScroll = 0;
		if (orientation == HORIZONTAL) {
			mListener.scrollBy(0, -mListener.getScrollY());
		} else {
			mListener.scrollBy(-mListener.getScrollX(), 0);
		}
		mOrientation = orientation;
		updateSize();
	}

	protected void updateSize() {
		// 取消当前方向的偏移量
		mScroll = 0;
		if (mOrientation == HORIZONTAL) {
			mScreenSize = mScreenWidth;
			mListener.scrollBy(-mListener.getScrollX(), 0);
		} else {
			mScreenSize = mScreenHeight;
			mListener.scrollBy(0, -mListener.getScrollY());
		}
		if (mEffector != null) {
			mEffector.onSizeChanged(mScreenWidth, mScreenHeight, mOrientation);
		}
		final int oldCount = mScreenCount;
		mScreenCount = -1; // 为了使updateScreenGroupChildCount中重新计算
		setScreenCount(oldCount);
	}

	/**
	 * 设置屏幕数量，删除或者增加新的子视图后调用
	 * 
	 * @param count
	 */
	public void setScreenCount(int count) {
		// 在abortAnimation前面先作这个判断避免无谓的onLayout引起动画中断
		if (mScreenCount == count) {
			return;
		}
		abortAnimation();
		if (count <= 0) {
			return;
		}
		mScreenCount = count;
		mScreenCountInv = mScreenCount > 0 ? 1.0f / mScreenCount : 0;
		mLastScreenPos = mScreenSize * (mScreenCount - 1);
		mTotalSize = mScreenSize * mScreenCount;
		mTotalSizeInv = mTotalSize > 0 ? 1.0f / mTotalSize : 0;
		// mDstScreen = Math.max(0, Math.min(mDstScreen, mScreenCount - 1));
		float oldPaddingFactor = mPaddingFactor;
		mPaddingFactor = -1;
		setPadding(oldPaddingFactor);
	}

	/**
	 * 设置视图容器两端拖动最大超出距离相对于屏幕的比例，初始化时是0.5
	 * 
	 * @param paddingFactor
	 *            限制范围在[0, 0.5]
	 */
	public void setPadding(float paddingFactor) {
		abortAnimation();
		if (mPaddingFactor == paddingFactor) {
			return;
		}
		mPaddingFactor = Math.max(0, Math.min(paddingFactor, 0.5f));
		// 限制最小值和最大值，防止computeScreenIndex()的结果越界
		mMinScroll = Math.max(-(int) (mScreenSize * paddingFactor), -mScreenSize / 2);
		mMaxScroll = Math.min(mLastScreenPos + (int) (mScreenSize * paddingFactor), mLastScreenPos
				+ mScreenSize / 2 - 1);
		mMaxScroll = Math.max(mMinScroll, mMaxScroll);

		mScrollRatio = mMaxScroll > mMinScroll ? 1.0f / (mMaxScroll - mMinScroll) : 0;

		// 重设当前滚动量
		scrollScreenGroup(getDstScreen() * mScreenSize);
	}

	/**
	 * 卷动视图容器
	 * 
	 * @param newScroll
	 *            不检查是否越界
	 */
	@Override
	protected void scrollScreenGroup(int newScroll) {
		mOldScroll = mScroll;
		mScroll = newScroll;
		if (mScroll != mOldScroll) {
			if (mOrientation == HORIZONTAL) {
				mListener.scrollBy(mScroll - mOldScroll, 0);
			} else {
				mListener.scrollBy(0, mScroll - mOldScroll);
			}
			mListener.onScrollChanged(mScroll, mOldScroll);
			final int oldScreen = mCurrentScreen;
			mCurrentScreen = computeScreenIndex(mScroll);
			if (mCurrentScreen != oldScreen) {
				mListener.onScreenChanged(mCurrentScreen, oldScreen);
				if (mWallpaperDrawer != null) {
					mWallpaperDrawer.updateScreen(mCurrentScreen, oldScreen);
					updateBackgroundDraw();
				}
			}
		} else {
			super.scrollScreenGroup(newScroll);
		}
	}

	/**
	 * 设置插值器
	 * 
	 * @param interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		if (interpolator == null) {
			interpolator = MScroller.DEFAULT_INTERPOLATOR;
		}
		mInterpolator = interpolator;
		mInterpolatorBak = mInterpolator;
	}

	/**
	 * 设置切换一屏需要的最长时间
	 * 
	 * @param duration
	 *            单位为毫秒，默认为1000
	 */
	public void setDuration(int duration) {
		duration = Math.max(1, duration);
		mScrollingDuration = duration;
	}

	/**
	 * 设置动画效果
	 * 
	 * @param effector
	 *            为null表示不使用效果
	 */
	public void setEffector(ScreenScrollerEffector effector) {
		ScreenScrollerEffector oldEffector = mEffector;
		mEffector = effector;
		if (oldEffector != mEffector && oldEffector != null) {
			oldEffector.onDetach();
		}
		if (mEffector != null) {
			mEffector.onAttach(mListener);
		}
	}

	/**
	 * 设置当前屏幕位置，不会产生动画。 调用者必须先检查dstScreen是否越界，或者先调用{@link #setScreenCount}使其不越界。
	 * 
	 * @param dstScreen
	 */
	public void setCurrentScreen(int dstScreen) {
		// if(isFinished()){
		// return;
		// }
		abortAnimation();
		mDstScreen = dstScreen; // 不作任何限制，因为监听者可能在调用本方法时还没有添加所有子屏
		if (mDstScreen == 0 && mScroll == 0) {
			final int oldScreen = mCurrentScreen;
			mCurrentScreen = 0;
			if (mCurrentScreen != oldScreen) {
				mListener.onScreenChanged(mCurrentScreen, oldScreen);
				if (mWallpaperDrawer != null) {
					mWallpaperDrawer.updateScreen(mCurrentScreen, oldScreen);
				}
			}
		} else {
			scrollScreenGroup(mDstScreen * mScreenSize);
		}
	}

	/**
	 * 直接设置偏移量，同样也可能会引起监听者的onScrollChanged和onScreenChanged的回调 注意在连续调用完本方法后，要调用
	 * {@link #setCurrentScreen(int)} 或者{@link #gotoScreen(int, int, boolean)}
	 * 方法来修正位置（以及还原到非滚动状态）
	 * 
	 * @param percent
	 *            0对应第一屏，100对应最后一屏
	 */
	public void setScrollPercent(float percent) {
		if (mState != ON_SCROLL) {
			onScrollStart();
		}
		onScroll((int) (percent * mLastScreenPos * 0.01f) - mEndScroll);
	}

	/**
	 * 直接设置偏移量，同样也可能会引起监听者的onScrollChanged和onScreenChanged的回调 注意在连续调用完本方法后，要调用
	 * {@link #setCurrentScreen(int)} 或者{@link #gotoScreen(int, int, boolean)}
	 * 方法来修正位置（以及还原到非滚动状态）
	 * 
	 * @param index
	 *            屏幕的索引，支持浮点数值
	 */
	public void setScrollIndex(float index) {
		if (mState != ON_SCROLL) {
			onScrollStart();
		}
		index = Math.max(-mPaddingFactor, Math.min(index, mScreenCount - 1 + mPaddingFactor));
		onScroll((int) (index * mScreenSize) - mEndScroll);
	}

	/**
	 * 从当前位置滚动到指定的屏幕位置，会产生动画
	 * 
	 * @param dstScreen
	 * @param duration
	 */
	protected boolean flingToScreen(int dstScreen, int duration) {
		// 在两端采用默认插值器；在中间采用指定的插值器
		Interpolator interpolator = mInterpolatorBak;
		if (mScroll < 0 || dstScreen < 0) {
			dstScreen = 0;
			duration = mDecelerateDuration;
			interpolator = MScroller.VISCOUS_FLUID_INTERPOLATOR;
		} else if (mScroll >= mLastScreenPos || dstScreen >= mScreenCount) {
			dstScreen = mScreenCount - 1;
			duration = mDecelerateDuration;
			interpolator = MScroller.VISCOUS_FLUID_INTERPOLATOR;
		}
		return gotoScreen(dstScreen, duration, interpolator);
	}

	/**
	 * 检查索引范围
	 * 
	 * @param screen
	 * @return
	 */
	protected int checkScreen(int screen) {
		return Math.max(0, Math.min(screen, mScreenCount - 1));
	}

	/**
	 * 从当前位置使用指定的插值器滚动到指定的屏幕位置，会产生动画
	 * 
	 * @param dstScreen
	 * @param duration
	 *            切换的时间
	 * @param interpolator
	 *            如果为null，使用默认的插值器
	 */
	protected boolean gotoScreen(int dstScreen, int duration, Interpolator interpolator) {
		mInterpolator = interpolator != null ? interpolator : MScroller.DEFAULT_INTERPOLATOR;

		mDstScreen = checkScreen(dstScreen);
		final int delta = mDstScreen * mScreenSize - mScroll;
		if (delta == 0 && getCurrentDepth() == 0) {
			final int oldState = mState;
			if (mState != MScroller.FINISHED) {
				mState = MScroller.FINISHED;
				mListener.onScrollFinish(getDstScreen());
			}
			// 如果没有开始滚动就收到 ACTION_UP 事件，那么返回 false 表示不处理该事件
			return oldState != MScroller.FINISHED;
		}
		if (mFlingVelocity != 0 && mInterpolator != VISCOUS_FLUID_INTERPOLATOR) {
			// 计算一个合理的时间，但是限制最大值，不能太慢
			duration = Math.min(duration, computeFlingDuration(delta, mFlingVelocity));
			mFlingVelocity = 0;
		}
		onFling(mScroll, delta, duration);
		mListener.onFlingStart();
		return true;
	}

	/**
	 * 从当前屏幕使用当前的插值器滚动到指定的屏幕位置，会产生动画。 由外部直接调用（如响应Home键时）
	 * 
	 * @param duration
	 *            小于0则自动计算时间
	 * 
	 */
	public void gotoScreen(int dstScreen, int duration, boolean noElastic) {
		mListener.onScrollStart();
		// TODO: 使用一个合理的初速度来求时间
		gotoScreen(dstScreen, duration < 0 ? mScrollingDuration : duration, noElastic
				? MScroller.DEFAULT_INTERPOLATOR
				: mInterpolatorBak);
	}

	public final Interpolator getInterpolator() {
		return mInterpolatorBak;
	}

	public final ScreenScrollerEffector getEffector() {
		return mEffector;
	}

	public final int getScreenWidth() {
		return mScreenWidth;
	}

	public final int getScreenHeight() {
		return mScreenHeight;
	}

	public final int getScreenSize() {
		return mScreenSize;
	}

	public final int getScreenCount() {
		return mScreenCount;
	}

	/**
	 * 获取当前屏（显示面积较大的那屏）的索引
	 * 
	 * @return
	 */
	public final int getCurrentScreen() {
		return mCurrentScreen;
	}

	/**
	 * 获取最终停留的屏幕索引
	 * 
	 * @return
	 */
	public int getDstScreen() {
		return mDstScreen;
	}

	/**
	 * 获取当前屏（显示面积较大的那屏）的偏移量
	 * 
	 * @return
	 */
	public final int getCurrentScreenOffset() {
		return mCurrentScreen * mScreenSize - mScroll;
	}

	/**
	 * 计算当前屏的索引
	 * 
	 * @param scroll
	 * @return
	 */
	protected int computeScreenIndex(int scroll) {
		return (scroll + mScreenSize / 2) / mScreenSize;
	}

	/**
	 * 是否循环滚屏
	 * 
	 * @return
	 */
	public boolean isCircular() {
		return mCycloid;
	}

	/**
	 * 响应触摸事件
	 * 
	 * @param event
	 * @param action
	 *            在某些特殊情况下可以强制指定为某一值，但是默认应该为event.getAction()
	 * @return
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event, int action) {
		mLastTouchX = (int) event.getX();
		mLastTouchY = (int) event.getY();
		final int p = mOrientation == HORIZONTAL ? mLastTouchX : mLastTouchY;
		int delta = mLastTouchP - p;
		mLastTouchP = p;

		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				int eventAction = event.getAction() & MotionEvent.ACTION_MASK;
				mCurrentTouchSlop = eventAction == action ? mTouchSlop : 0;
				mVelocityTracker.addMovement(event);
				mTouchDownP = mLastTouchP;
				mTouchDownX = mLastTouchX;
				mTouchDownY = mLastTouchY;
				mTouchDownScrollP = mScroll;
				mTouchDownScreen = mCurrentScreen;
				if (mState == MScroller.ON_FLING) {
					mState = MScroller.TO_SCROLL;
					mListener.onFlingIntercepted();
				}
			}
				break;
			case MotionEvent.ACTION_MOVE : {
				mVelocityTracker.addMovement(event);
				// mVelocityTracker.computeCurrentVelocity(1000);
				// mFlingVelocity = (int)(mOrientation == HORIZONTAL
				// ? mVelocityTracker.getXVelocity()
				// : mVelocityTracker.getYVelocity());
				// if(Math.abs(mFlingVelocity) < FLING_VELOCITY){
				// mListener.onFlingIntercepted();
				// }
				if (mState != MScroller.ON_SCROLL) {
					if (Math.abs(mLastTouchP - mTouchDownP) >= mCurrentTouchSlop) {
						// 开始拖动
						mTouchDownP = mLastTouchP;
						mTouchDownX = mLastTouchX;
						mTouchDownY = mLastTouchY;
						// delta = 0;
						onScrollStart();
						mListener.onScrollStart();
					}
				}
				if (mState == MScroller.ON_SCROLL) {
					onScroll(delta);
				}
			}
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL : {
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
				mFlingVelocityX = (int) mVelocityTracker.getXVelocity();
				mFlingVelocityY = (int) mVelocityTracker.getYVelocity();
				mFlingVelocity = mOrientation == HORIZONTAL ? mFlingVelocityX : mFlingVelocityY;
				mVelocityTracker.clear();
				if (mState == MScroller.TO_SCROLL) {
					// 在很短促的甩动情况下会没有ACTION_MOVE事件导致没有启动拖动（从而不去创建绘图缓冲或者启动硬件加速），这里保证启动拖动
					onScrollStart();
					mListener.onScrollStart();
				}
				if (mFlingVelocity > mMinFlingVelocity && mTouchDownP <= p) {
					flingToScreen(mTouchDownScreen - 1, mScrollingDuration);
				} else if (mFlingVelocity < -mMinFlingVelocity && mTouchDownP >= p) {
					flingToScreen(mTouchDownScreen + 1, mScrollingDuration);
				} else {
					mFlingVelocity = mMinFlingVelocity;
					return flingToScreen(computeScreenIndex(mScroll), mScrollingDuration);
				}
			}
				break;
			default :
				return false;
		}
		return true;
	}

	public int getFlingVelocityX() {
		return mFlingVelocityX;
	}

	public int getFlingVelocityY() {
		return mFlingVelocityY;
	}

	@SuppressLint("WrongCall")
	@Override
	public boolean onDraw(Canvas canvas) {
		invalidateScroll();
		if (mScreenCount < 1) {
			return true;
		}
		return mEffector != null && mEffector.onDraw(canvas);
	}

	public boolean isBackgroundAlwaysDrawn() {
		return mBgAlwaysDrawn;
	}

	protected int onScrollAtEnd(int delta) {
		delta /= 2;
		// 限制 mMinScroll <= mScroll + delta <= mMaxScroll
		delta = Math.max(mMinScroll - mScroll, Math.min(delta, mMaxScroll - mScroll));
		return delta;
	}

	@Override
	public void onScroll(int delta) {
		final int newScroll = mScroll + delta;
		if (newScroll < 0 || newScroll >= mLastScreenPos) {
			delta = onScrollAtEnd(delta);
		}

		if (delta == 0) {
			return;
		}
		super.onScroll(delta);
	}

	@Override
	protected void invalidate() {
		mListener.invalidate();
	}

	@Override
	protected void onComputeFlingOffset(float t) {
		t = mInterpolator.getInterpolation(t);
		int scroll;
		scroll = isFlingFinished() ? mEndScroll : mStartScroll + Math.round(t * mDeltaScroll);
		mIsOvershooting = !isFlingFinished() && t > 1;
		scrollScreenGroup(scroll);
		if (isFinished()) {
			if (!isOldScrollAtEnd() && mEffector != null) {
				mEffector.updateRandomEffect(); // 切换随机特效
			}
			mListener.onScrollFinish(getDstScreen());
		}
	}

	@Override
	public void abortAnimation() {
		if (mState == ON_FLING) {
			super.abortAnimation();
			onComputeFlingOffset(1);
		}
	}

	/**
	 * 是否滚屏到达两端
	 * 
	 * @return
	 */
	public boolean isScrollAtEnd() {
		return mScroll < 0 || mScroll >= mLastScreenPos;
	}

	/**
	 * 上一次是否滚屏到达两端
	 * 
	 * @return
	 */
	public boolean isOldScrollAtEnd() {
		return mOldScroll < 0 || mOldScroll >= mLastScreenPos;
	}

	public void setEffectorMaxOvershootEnabled(boolean enabled) {
		mUseEffectorMaxOvershootPercent = enabled;
		setOvershootPercent(mMaxOverShootPercent);
	}

	/**
	 * 设置背景内容
	 * 
	 * @param drawable
	 */
	public void setBackground(Drawable drawable) {
//		Drawable backgroundDrawable = drawable;
//		Bitmap bitmap = null;
//		if (backgroundDrawable != null) {
//			mBackgroundWidth = backgroundDrawable.getIntrinsicWidth();
//			mBackgroundHeight = backgroundDrawable.getIntrinsicHeight();
//			// 如果是从WallpaperManager.getDrawable得到的Drawable对象，则未有设置边界，绘制不出来的
//			backgroundDrawable.setBounds(0, 0, mBackgroundWidth, mBackgroundHeight);
//			if (backgroundDrawable instanceof BitmapDrawable) {
//				bitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
//				if (DrawUtils.sVirtualDensityDpi != -1
//						&& DrawUtils.sDensityDpi != DrawUtils.sVirtualDensityDpi) {
//					bitmap.setDensity(DrawUtils.sDensityDpi);
//				}
//				mBackgroundWidth = bitmap.getWidth();
//				mBackgroundHeight = bitmap.getHeight();
//			}
//			setBackgroundOffsetY();
//		} 
//		else {
//			// 横屏下是不需要x两个屏幕的宽度的
//			final int factor = GoLauncher.isPortait() ? 2 : 1;
//			mBackgroundWidth = DrawUtils.sWidthPixels * factor;
//			mBackgroundHeight = DrawUtils.sHeightPixels;
//		}
//
//		if (mWallpaperDrawer != null) {
//			mWallpaperDrawer.setBackground(backgroundDrawable, bitmap);
//		}
	}

	/**
	 * 设置背景的掩码颜色（没什么用）
	 * 
	 * @param color
	 */
	public void setBackgroundColorFilter(int color) {
		if ((color >>> 24) == 0) {
			color = 0;
		}
		if (color == 0) {
			mColorFilter = null;
//			if (mPaint != null) {
//				mPaint.setColorFilter(null);
//			}
		} else {
			mColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_OVER);
//			if (mPaint == null) {
//				mPaint = new Paint();
//			}
//			mPaint.setColorFilter(mColorFilter);
		}
	}

	/**
	 * 设置视图相对于背景的垂直方向偏移量（桌面上有通知栏的情况）
	 * 
	 * @param y
	 */
	public void setScreenOffsetY(int y) {
		mScreenOffsetY = y;
		setBackgroundOffsetY();
		if (mWallpaperDrawer != null) {
			mWallpaperDrawer.updateOffsetY(y, true);
		}
	}

	/**
	 * 设置背景是否跟随滑动
	 * 
	 * @param enabled
	 */
	public void setBackgroundScrollEnabled(Boolean enabled) {
		mBackgroundScrollEnabled = enabled;
		if (mWallpaperDrawer != null) {
			mWallpaperDrawer.setMiddleScrollEnabled(enabled);
		}
	}

	protected void setBackgroundOffsetY() {
		// 假设屏幕只有状态栏和视图两个区域，使壁纸在屏幕上垂直居中
		// 计算视图左上角相对于壁纸左上角的Y坐标
		// 此表达式受三个值的影响，因此在setScreenOffsetY, setBackground,
		// setViewGroup方法中都要调用它来更新
		mBackgroundOffsetY = (int) ((mScreenOffsetY + mBackgroundHeight - mScreenHeight
				/ mLayoutScale) / 2);
	}

	protected int getBackgroundOffsetX(int scroll) {
//		if (mBackgroundScrollEnabled) {
//			float res = (mBackgroundWidth - DrawUtils.sWidthPixels) * (scroll - mMinScroll)
//					* mScrollRatio;
//			return (int) (res + 0.5f);
//		} else {
//			return (mBackgroundWidth - DrawUtils.sWidthPixels) / 2;
//		}
		return 0;
	}

	public Drawable getBackground() {
		if (mWallpaperDrawer != null) {
			return mWallpaperDrawer.getBackgroundDrawable();
		}
		return null;
	}

	public int getBackgroundOffsetX() {
		return getBackgroundOffsetX(mScroll);
	}

	public int getBackgroundOffsetY() {
		return mBackgroundOffsetY;
	}

	public boolean drawBackground(Canvas canvas, int scroll) {
		if (mWallpaperDrawer == null) {
			return false;
		}
		mWallpaperDrawer.setUpdateBackground(false);
		int x = -getBackgroundOffsetX(scroll);
		int y = -mBackgroundOffsetY;
		if (mOrientation == HORIZONTAL) {
			x += mScroll;
		} else {
			y += mScroll;
		}
//		if (mWallpaperDrawer.getBackgroundBitmap() != null 
//				&& mWallpaperDrawer.getBackgroundBitmap().isRecycled()) {
//			// 如果背景壁纸被其他应用更改了，图片会失效
//			mWallpaperDrawer.setBackground(null, null);
//			return false;
//		}
//		mWallpaperDrawer.setAlpha(mAlpha);
		mWallpaperDrawer.updateXY(x, y);
		mWallpaperDrawer.updateOffsetX(mScroll, true);
//		mWallpaperDrawer.doDraw(canvas);
//		if (mBitmap != null) {
//			canvas.drawBitmap(mBitmap, x, y, mPaint);
//		} else {
//			canvas.translate(x, y);
//			if (mColorFilter != null) {
//				mBackgroundDrawable.setColorFilter(mColorFilter);
//			}
//			mBackgroundDrawable.draw(canvas);
//			if (mColorFilter != null) {
//				mBackgroundDrawable.setColorFilter(null);
//			}
//			canvas.translate(-x, -y);
//		}
		return true;
	}

	/**
	 * 绘制背景到第screen屏上
	 * 
	 * @param canvas
	 * @param screen
	 */
	public boolean drawBackgroundOnScreen(Canvas canvas, int screen) {
		if (mBgAlwaysDrawn || mWallpaperDrawer == null || mWallpaperDrawer.getBackgroundDrawable() == null) {
			return false;
		}
		mWallpaperDrawer.setUpdateBackground(true);
		screen = checkScreen(screen);
		final int x = -getBackgroundOffsetX(screen * mScreenSize);
		final int y = -mBackgroundOffsetY;
		
//		if (mWallpaperDrawer.getBackgroundBitmap() != null
//				&& mWallpaperDrawer.getBackgroundBitmap().isRecycled()) {
//			// 如果背景壁纸被其他应用更改了，图片会失效
//			mWallpaperDrawer.setBackground(null, null);
//			return false;
//		}
//		mWallpaperDrawer.updateXY(x, y);
//		mWallpaperDrawer.updateOffsetX(mScroll, true);
		mWallpaperDrawer.doDraw(canvas, x, y);
//		if (mBitmap != null && mBitmap.isRecycled()) {
//			// 如果背景壁纸被其他应用更改了，图片会失效
//			mBitmap = null;
//			mBackgroundDrawable = null;
//			return false;
//		}
//		if (mBitmap != null) {
//			canvas.drawBitmap(mBitmap, x, y, mPaint);
//		} else {
//			canvas.translate(x, y);
//			if (mColorFilter != null) {
//				mBackgroundDrawable.setColorFilter(mColorFilter);
//			}
//			mBackgroundDrawable.draw(canvas);
//			if (mColorFilter != null) {
//				mBackgroundDrawable.setColorFilter(null);
//			}
//			canvas.translate(-x, -y);
//		}
		return true;
	}

	/**
	 * 设置当前使用的插值器过冲的百分比，受{@link #setMaxOvershootPercent}的影响
	 * 
	 * @param percent
	 *            建议值[0, 50]
	 */
	public void setOvershootPercent(int percent) {
		if (!mUseEffectorMaxOvershootPercent && percent != mMaxOverShootPercent) {
			return;
		}
		if (mUseEffectorMaxOvershootPercent && percent == mMaxOverShootPercent && mEffector != null) {
			percent = Math.min(percent, mEffector.getMaxOvershootPercent());
		}
		percent = Math.min(percent, mMaxOverShootPercent);
		if (mOverShootPercent == percent) {
			return;
		}
		mOverShootPercent = percent;
		if (percent <= 0) {
			setInterpolator(DEFAULT_INTERPOLATOR);
		} else {
			final float tension = solveOvershootInterpolatorTension(percent);
			setInterpolator(new OvershootInterpolator(tension));
		}
	}

	/**
	 * 设置插值器过冲的最大百分比
	 * 
	 * @param percent
	 *            建议值[0, 50)
	 */
	public void setMaxOvershootPercent(int percent) {
		mMaxOverShootPercent = Math.max(0, Math.min(percent, 49));
		setOvershootPercent(mMaxOverShootPercent);
	}

	public float getProgress() {
		return mScroll * mTotalSizeInv;
	}

	/**
	 * 获取指示器滑块的偏移量。 假设在第一屏和最后一屏滑块和边缘对齐，滑块宽度为屏幕宽度/屏幕数量
	 * 
	 * @return
	 */
	public int getIndicatorOffset() {
		int scroll = Math.max(0, Math.min(mScroll, mLastScreenPos));
		return (int) (scroll * mScreenCountInv + 0.5f);
	}

	public void setBackgroundAlwaysDrawn(boolean bgDrawn) {
		mBgAlwaysDrawn = bgDrawn;
	}

	void recycle() {
	}

	/**
	 * 获取当前屏的前一屏
	 * 
	 * @return
	 */
	public int getPreviousScreen() {
		return mCurrentScreen - 1;
	}

	/**
	 * 获取当前屏的下一屏
	 * 
	 * @return
	 */
	public int getNextScreen() {
		return mCurrentScreen + 1;
	}

	/**
	 * 返回当前绘制的左边子屏索引
	 * 
	 * @return {@link #INVALID_SCREEN}表示无效索引
	 */
	public int getDrawingScreenA() {
		int drawingScreenA = mCurrentScreen;
		if (getCurrentScreenOffset() > 0) {
			--drawingScreenA;
		}
		if (drawingScreenA < 0 || drawingScreenA >= mScreenCount) {
			return INVALID_SCREEN;
		}
		return drawingScreenA;
	}

	/**
	 * 返回当前绘制的右边子屏索引
	 * 
	 * @return {@link #INVALID_SCREEN}表示无效索引（在只绘制一屏的时候也是返回该值）
	 */
	public int getDrawingScreenB() {
		int drawingScreenB = mCurrentScreen;
		final int offset = getCurrentScreenOffset();
		if (offset == 0) {
			return INVALID_SCREEN;
		}
		if (offset < 0) {
			++drawingScreenB;
		}
		if (drawingScreenB < 0 || drawingScreenB >= mScreenCount) {
			return INVALID_SCREEN;
		}
		return drawingScreenB;
	}

	public int getTouchDeltaX() {
		return mLastTouchX - mTouchDownX;
	}

	public int getTouchDeltaY() {
		return mLastTouchY - mTouchDownY;
	}

	/**
	 * 根据当前插值器的设置计算甩动一段距离需要的时间
	 * 
	 * @param change
	 *            甩动的距离，以像素为单位
	 * @param velocity
	 *            甩动的初速度，以像素/秒为单位，必须不能为0
	 * @return 需要的时间，以毫秒为单位
	 */
	protected int computeFlingDuration(int change, int velocity) {
		/*
		 * 令f(x)为插值器函数，则实际运动方程为F(t)=b+f(t/d) * c， （b<=>begin, t<=>time,
		 * d<=>duration, c<=>change<=>end-begin），
		 * 则速度即一阶导数F'(t)=c/d*f'(t/d)，给定v，则d=c*f'(0)/v。
		 * 
		 * 对于n次方的减速曲线插值，f(x)=1-(1-x)^n， 有f'(x)=n(1-x)^(n-1), f'(0)=n，则d=nc/v。
		 * 
		 * 对于过冲插值，f(x)=(k+1)(x-1)^3+k(x-1)^2+1，其中k为张力参数，
		 * 则f'(x)=3(k+1)(x-1)^2+2k(x-1)，f'(0)=k+3。
		 * 
		 * 但是区分插值器类型太麻烦了，采用差分近似方法来求f'(0)，并且支持任意插值器。
		 */
		float diff = mInterpolator.getInterpolation(EPSILON) * ONE_OVER_EPSILON;
		return (int) Math.abs(change * diff * 1000 / velocity);
	}

	/**
	 * 设置循环模式
	 * 
	 * @param scroller
	 * @param cycle
	 * @return
	 */
	public static void setCycleMode(ScreenScrollerListener listener, boolean cycle) {
		if (listener == null) {
			return;
		}
		ScreenScroller scroller = listener.getScreenScroller();
		if (scroller != null && scroller.isCircular() == cycle) {
			return;
		}
		ScreenScroller newScroller = cycle
				? (new CycloidScreenScroller(null, listener))
				: (new ScreenScroller(null, listener));
		listener.setScreenScroller(newScroller);
		if (scroller != null) {
			copyScrollerAttributes(scroller, newScroller);
			scroller.recycle();
		}
	}

	/**
	 * 复制滚动器的状态
	 * 
	 * @param scroller
	 * @param newScroller
	 */
	private static void copyScrollerAttributes(ScreenScroller scroller, ScreenScroller newScroller) {
		newScroller.setWallpaperDrawer(scroller.mWallpaperDrawer);
		newScroller.mDstScreen = scroller.getDstScreen();
		newScroller.mScreenOffsetY = scroller.mScreenOffsetY;
		newScroller.mPaddingFactor = scroller.mPaddingFactor;
		newScroller.mScreenCount = scroller.mScreenCount;
		newScroller.mOrientation = scroller.mOrientation;
		newScroller.mTouchSlop = scroller.mTouchSlop;
		newScroller.mMinFlingVelocity = scroller.mMinFlingVelocity;
		newScroller.mMaxFlingVelocity = scroller.mMaxFlingVelocity;
		newScroller.mVelocityTracker = scroller.mVelocityTracker;
		newScroller.setScreenSize(scroller.mScreenWidth, scroller.mScreenHeight); // 这里根据以上的设置重新计算
		newScroller.setInterpolator(scroller.getInterpolator());
		newScroller.setDuration(scroller.mScrollingDuration);
		newScroller.setEffector(scroller.mEffector);
		newScroller.setDepthEnabled(scroller.mDepthEnabled);
		newScroller.setBackgroundAlwaysDrawn(scroller.mBgAlwaysDrawn);
		newScroller.setMaxOvershootPercent(scroller.mMaxOverShootPercent);
		newScroller.setBackgroundScrollEnabled(scroller.mBackgroundScrollEnabled);
		newScroller.setBackground(scroller.getBackground());
	}

	/**
	 * 计算OvershootInterpolator的张力tension
	 * 
	 * @param percent
	 *            超出部分的百分比
	 * @return
	 */
	private static float solveOvershootInterpolatorTension(int percent) {
		/*
		 * OvershootInterpolator的计算公式：k为张力>=0，t为时间[0, 1]
		 * f(t)=(t-1)^2*((k+1)*(t-1)+k)+1=(k+1)(t-1)^3+k(t-1)^2+1
		 * 导数f'(t)=3(k+1)(t-1)^2+2k(t-1)^2 令f'(t)==0，解得t=1-2k/(3(k+1))，或t=1（舍去）
		 * 代入f(t)，得max(f(t))=4k^3/(27(k+1)^2)+1
		 * 即最大超出部分为g(k)=max(f(t))-1=4k^3/(27(k+1)^2) 使用Mathematica命令
		 * Solve[4k^3/(27(k+1)^2)==0.1, k]
		 * http://www.wolframalpha.com/input/?i=Solve
		 * [4k^3%2F%2827%28k%2B1%29^2%29%3D%3D0.1%2C+k] 解g(k)=0.1，得k=1.70154
		 * 解g(k)=0.5，得k=4.89486 如果我们指定g，那么通过解g(k)的方程就得到张力k了——
		 * Solve[4k^3/(27(k+1)^2)==g, k]
		 * http://www.wolframalpha.com/input/?i=Solve
		 * [4k^3%2F%2827%28k%2B1%29^2%29%3D%3Dg%2C+k] 部分结果如下： percent = 0
		 * tension=NaN percent = 10 tension=1.7015402 percent = 20
		 * tension=2.5923889 percent = 30 tension=3.3940518 percent = 40
		 * tension=4.155745 percent = 50 tension=4.8948593
		 */

		// if(percent <= 0) return 0; // 注意percent为0的时候最后除0会得到NaN
		// // 直接设张力为0，退化成DecelerateInterpolator(1.5f)
		// float g = percent / 100.0f;
		// float g2 = g * g;
		// float g3 = g * g2;
		// double d = 27 * g3 + 36 * g2 + 8 * Math.sqrt(g3 + g2) + 8 * g;
		// d = Math.pow(d, 1.0f / 3);
		// return (float)(0.75f * d + (729 * g2 + 648 * g) / (108 * d) + 2.25f *
		// g);

		// 用查找表记录percent=0,5,10,...,50的结果，其他percent使用线性插值计算，对<5的时候还是有10%以上误差
		final float[] tension = { 0.0f, 1.1652954f, 1.7015402f, 2.1642938f, 2.5923889f, 3.0f,
				3.3940518f, 3.7784798f, 4.155745f, 4.5274878f, 4.8948593f, };
		percent = Math.max(0, Math.min(percent, 49));
		int i = percent / 5;
		return tension[i] + (tension[i + 1] - tension[i]) * (percent / 5.0f - i);
	}

	public void setLayoutScale(float scale) {
		mLayoutScale = scale;
	}

	public float getLayoutScale() {
		return mLayoutScale;
	}

	public void setWallpaperDrawer(IWallpaperDrawer wallpaperDrawer) {
		mWallpaperDrawer = wallpaperDrawer;
		if (mWallpaperDrawer != null) {
			mWallpaperDrawer.setCycloidDrawListener(null);
		}
	}
	
	/**
	 * 绘制当前屏幕可见壁纸
	 * @param canvas
	 * @param scroll
	 * @return
	 */
	public boolean drawCurBackground(Canvas canvas, int scroll) {
		if (drawBackground(canvas, scroll) && mWallpaperDrawer != null) {
			mWallpaperDrawer.drawBackground(canvas);
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * 更新壁纸的绘制，在翻转特效（带壁纸）需要用到
	 */
	private void updateBackgroundDraw() {
		if (mWallpaperDrawer != null && mWallpaperDrawer.needUpdateBackground()) {
			mCurrentScreen = checkScreen(mCurrentScreen);
			int x = -getBackgroundOffsetX(mCurrentScreen * mScreenSize);
			int y = -mBackgroundOffsetY;
			if (mOrientation == HORIZONTAL) {
				x += mScroll;
			} else {
				y += mScroll;
			}
			mWallpaperDrawer.updateXY(x, y);
			mWallpaperDrawer.updateOffsetX(mScroll, false);
		}
	}
}