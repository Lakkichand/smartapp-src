package com.jiubang.go.backup.pro.ui.ScrollerView;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * @author dengweiming
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

	protected static final float EPSILON = 1e-6f;
	protected static final float ONE_OVER_EPSILON = 1e6f;

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

	protected static final Interpolator VISCOUS_FLUID_INTERPOLATOR = new ViscousFluidInterpolater();
	protected static final Interpolator DEFAULT_INTERPOLATOR = new OvershootInterpolator(0);

	private final float m200f = 200.0f;
	private final int m100 = 100;

	/**
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
		switch (mState) {
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
					mDepth = Math.max(0, mDepth - passedTime / m200f);
					onDepthChanged();
				}
				mFlingProgress = mFlingPassedTime * mFlingDurationReciprocal;
				onComputeFlingOffset(mFlingProgress);
				return true;
			}
			case ON_SCROLL : {
				if (mDepthEnabled && mDepth < 1) {
					long curTime = AnimationUtils.currentAnimationTimeMillis();
					int passedTime = (int) (curTime - mDepthUpdateTime);
					mDepthUpdateTime = curTime;
					mDepth = Math.min(1, mDepth + passedTime / m200f);
					onDepthChanged();
					return true;
				}
				return false;
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
		mFlingDurationReciprocal = 1.0f / (float) mFlingDuration;
		mDepthUpdateTime = AnimationUtils.currentAnimationTimeMillis() + m100;
	}

	protected void onScroll(int offset) {

	}

	protected void onScrollStart() {
		if (mState == FINISHED) {
			mDepthUpdateTime = AnimationUtils.currentAnimationTimeMillis();
		}
		mState = ON_SCROLL;
	}

	protected void onComputeFlingOffset(float t) {
	}

	protected void onDepthChanged() {
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

	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public final int getOrientation() {
		return mOrientation;
	}
}
