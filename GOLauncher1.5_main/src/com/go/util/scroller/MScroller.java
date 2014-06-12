package com.go.util.scroller;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 
 * @author dengweiming
 * 
 */
abstract class MScroller {
	/** 水平滚动方向 */
	public static final int HORIZONTAL = 0;
	/** 垂直滚动方向 */
	public static final int VERTICAL = 1;

	protected static final int MAX_FRAME_TIME = 48;

	protected static final int FINISHED = 0;
	protected static final int ON_FLING = 1;
	protected static final int ON_SCROLL = 2;
	protected static final int TO_SCROLL = 3;

	protected static final float EPSILON = 1e-6f;
	protected static final float ONE_OVER_EPSILON = 1e6f;

	private static final float SMOOTHING_SPEED = 0.75f;
	private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
	private static final float ONE_OVER_SMOOTHING_CONSTANT = 1 / SMOOTHING_CONSTANT;
	protected static final float NANOTIME_DIV = 1000000000.0f;
	protected static final float ONE_OVER_NANOTIME_DIV = 1 / NANOTIME_DIV;

	protected int mStartScroll;
	protected int mEndScroll;
	protected int mDeltaScroll;
	protected int mScroll;
	protected int mOrientation;

	private long mFlingStartTime;
	private int mFlingDuration;
	private float mFlingDurationReciprocal;
	private int mFlingPassedTime;
	private float mFlingProgress;
	protected int mState;

	protected float mDepth;
	protected boolean mDepthEnabled = false;
	protected long mDepthUpdateTime;

	private float mSmoothingTime;
	private boolean mFirstSmooth;
	private boolean mScrollComputed;

	protected int mTouchSlop;
	protected int mCurrentTouchSlop;

	protected int mMinFlingVelocity;
	protected int mMaxFlingVelocity;

	// If true, the subclass should directly update mScrollX itself in its
	// computeScroll method
	protected boolean mDeferScrollUpdate = true;

	protected static final Interpolator VISCOUS_FLUID_INTERPOLATOR = new ViscousFluidInterpolater();
	protected static final Interpolator DEFAULT_INTERPOLATOR = new OvershootInterpolator(0);

	MScroller(Context context) {
		if (context != null) {
			ViewConfiguration configuration = ViewConfiguration.get(context);
			mTouchSlop = configuration.getScaledTouchSlop();
			mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity() * 4;
			mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
		} else {
			mTouchSlop = ViewConfiguration.getTouchSlop();
			mMinFlingVelocity = ViewConfiguration.getMinimumFlingVelocity() * 4;
			mMaxFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
		}
	}

	/**
	 * 
	 * Returns whether the scroller has finished scrolling.
	 * 
	 * @return True if the scroller has finished scrolling, false otherwise.
	 */
	public final boolean isFinished() {
		return mState == FINISHED;
	}

	protected final boolean isFlingFinished() {
		return mFlingPassedTime >= mFlingDuration;
	}

	/**
	 * Returns the time elapsed since the beginning of the scrolling.
	 * 
	 * @return The elapsed time in milliseconds.
	 */
	public final int timePassed(long time) {
		if (mFlingStartTime == -1) {
			mFlingStartTime = time;
			return 0;
		}
		return (int) (time - mFlingStartTime);
	}

	/**
	 * Stops the animation. Contrary to {@link #forceFinished(boolean)},
	 * aborting the animating cause the scroller to move to the final x and y
	 * position
	 * 
	 * @see #forceFinished(boolean)
	 */
	public void abortAnimation() {
		if (mState == ON_FLING) {
			mState = FINISHED;
		}
	}

	/**
	 * Call this when you want to know the new location. If it returns true, the
	 * animation is not yet finished. loc will be altered to provide the new
	 * location.
	 */
	public boolean computeScrollOffset() {
		if (mScrollComputed) {
			if (mState != FINISHED) {
				invalidate();
				return true;
			}
			return false;
		}
		mScrollComputed = true;
		switch (mState) {
			case TO_SCROLL :
			case FINISHED :
				return false;
			case ON_FLING : {
				long curTime = AnimationUtils.currentAnimationTimeMillis();
				mFlingPassedTime = timePassed(curTime);
				// final int exceptTime = mFlingPassedTime + MAX_FRAME_TIME;
				// mFlingPassedTime = timePassed();
				// if(mFlingPassedTime > exceptTime){
				// mFlingDuration += mFlingDuration * (mFlingPassedTime -
				// exceptTime) / exceptTime;
				// mFlingDurationReciprocal = 1.0f / mFlingDuration;
				// }
				if (mFlingPassedTime >= mFlingDuration) {
					mFlingPassedTime = mFlingDuration;
					if (!mDepthEnabled || mDepth <= 0) {
						mState = FINISHED;
					}
				}
				if (mDepthEnabled && curTime >= mDepthUpdateTime) {
					int passedTime = (int) (curTime - mDepthUpdateTime);
					mDepthUpdateTime = curTime;
					mDepth = Math.max(0, mDepth - passedTime / 200.0f);
					onDepthChanged();
				}
				mFlingProgress = mFlingPassedTime * mFlingDurationReciprocal;
				onComputeFlingOffset(mFlingProgress);
				return true;
			}
			case ON_SCROLL : {
				boolean more = false;
				if (mDeferScrollUpdate) {
					final int d = mEndScroll - mScroll;
					if (d > 1 || d < -1) {
						final float now = System.nanoTime() * ONE_OVER_NANOTIME_DIV;
						float e = (float) Math.exp((now - mSmoothingTime)
								* ONE_OVER_SMOOTHING_CONSTANT);
						if (mFirstSmooth) {
							mFirstSmooth = false;
							e *= 0.5f;
						}
						scrollScreenGroup(Math.round(mScroll + d * e));
						mSmoothingTime = now;
						more = true;
					}
				}
				if (mDepthEnabled && mDepth < 1) {
					long curTime = AnimationUtils.currentAnimationTimeMillis();
					int passedTime = (int) (curTime - mDepthUpdateTime);
					mDepthUpdateTime = curTime;
					mDepth = Math.min(1, mDepth + passedTime / 200.0f);
					onDepthChanged();
					if (!more) {
						invalidate();
						more = true;
					}
				}
				return more;
			}
		}
		return false;
	}

	protected void onFling(int begin, int change, int duration) {
		mState = ON_FLING;
		mFlingProgress = 0;
		mFlingDuration = duration;
		mFlingStartTime = -1;
		mStartScroll = begin;
		mDeltaScroll = change;
		mEndScroll = begin + change;
		mFlingDurationReciprocal = 1.0f / mFlingDuration;
		mDepthUpdateTime = AnimationUtils.currentAnimationTimeMillis() + 100;
		invalidateScroll();
		invalidate();
	}

	protected void onScroll(int delta) {
		mEndScroll += delta;
		if (mDeferScrollUpdate) {
			mFirstSmooth = true;
			mSmoothingTime = System.nanoTime() * ONE_OVER_NANOTIME_DIV; // TODO:
																		// 去掉会不会效果更好？
			invalidate();
		} else {
			scrollScreenGroup(mEndScroll);
		}
	}

	protected void onScrollStart() {
		if (mState == FINISHED) {
			mDepthUpdateTime = AnimationUtils.currentAnimationTimeMillis();
		}
		mState = ON_SCROLL;
		mEndScroll = mScroll;
		mSmoothingTime = System.nanoTime() * ONE_OVER_NANOTIME_DIV;
		mScrollComputed = false;
		invalidate();
	}

	protected void scrollScreenGroup(int newScroll) {
		invalidate();
	}

	protected void onComputeFlingOffset(float t) {
	}

	protected void onDepthChanged() {
	}

	protected void invalidate() {
	}

	/**
	 * 因为硬件加速，{{@link #computeScrollOffset()} 会在 {{@link #onDraw(Canvas)}
	 * 之间被调用两次， 这样会影响平滑滚动（拖拽时），因此限制了{{@link #computeScrollOffset()}的重复调用，
	 * 必须在绘制后调用本方法来解除限制。一般重载{@link android.view.View#dispatchDraw(Canvas)}，
	 * 在里面调用本方法。
	 */
	public void invalidateScroll() {
		mScrollComputed = false;
	}

	/**
	 * 响应触摸事件
	 * 
	 * @param event
	 * @param action
	 *            在某些特殊情况下可以强制指定为某一值，但是默认应该为event.getAction()
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event, int action) {
		return false;
	}

	public boolean onDraw(Canvas canvas) {
		return false;
	}

	public void setDepthEnabled(boolean enabled) {
		mDepthEnabled = enabled;
	}

	public float getCurrentDepth() {
		if (mDepthEnabled) {
			float t = 1 - mDepth;
			return 1 - t * t;
		}
		return 0;
	}

	/**
	 * 获取滚动量
	 * 
	 * @return
	 */
	final public int getScroll() {
		return mScroll;
	}

	public void setScroll(int scroll) {
		mScroll = scroll;
	}

	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public final int getOrientation() {
		return mOrientation;
	}
}
