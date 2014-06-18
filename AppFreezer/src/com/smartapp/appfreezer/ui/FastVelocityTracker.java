package com.smartapp.appfreezer.ui;

import android.view.MotionEvent;

/**
 * 屏幕单点触摸移动速度检测器
 * 
 * @author dengweiming
 */
public class FastVelocityTracker {
	static final int NUM_PAST = 10;
	static final int LONGEST_PAST_TIME = 200;

	final float mPastX[] = new float[NUM_PAST];
	final float mPastY[] = new float[NUM_PAST];
	final long mPastTime[] = new long[NUM_PAST];

	float mYVelocity;
	float mXVelocity;
	int mLastTouch;

	private final float mPointFiveF = .5f;
	private final int m3 = 3;

	/**
	 * Reset the velocity tracker back to its initial state.
	 */
	public void clear() {
		final long[] pastTime = mPastTime;
		for (int i = 0; i < NUM_PAST; i++) {
			pastTime[i] = Long.MIN_VALUE;
		}
	}

	/**
	 * Add a user's movement to the tracker. You should call this for the
	 * initial {@link MotionEvent#ACTION_DOWN}, the following
	 * {@link MotionEvent#ACTION_MOVE} events that you receive, and the final
	 * {@link MotionEvent#ACTION_UP}. You can, however, call this for whichever
	 * events you desire.
	 * 
	 * @param ev
	 *            The MotionEvent you received and would like to track.
	 */
	public void addMovement(MotionEvent ev) {
		final int mN = ev.getHistorySize();
		if (++mLastTouch >= NUM_PAST) {
			mLastTouch = 0;
		}
		for (int i = 0; i < mN; ++i) {
			mPastX[mLastTouch] = ev.getHistoricalX(i);
			mPastY[mLastTouch] = ev.getHistoricalY(i);
			mPastTime[mLastTouch] = ev.getHistoricalEventTime(i);
			if (++mLastTouch >= NUM_PAST) {
				mLastTouch = 0;
			}
		}
		mPastX[mLastTouch] = ev.getX();
		mPastY[mLastTouch] = ev.getY();
		mPastTime[mLastTouch] = ev.getEventTime();
	}

	/**
	 * Equivalent to invoking {@link #computeCurrentVelocity(int, float)} with a
	 * maximum velocity of Float.MAX_VALUE.
	 * 
	 * @see #computeCurrentVelocity(int, float)
	 */
	public void computeCurrentVelocity(int units) {
		computeCurrentVelocity(units, Float.MAX_VALUE);
	}

	/**
	 * Compute the current velocity based on the points that have been
	 * collected. Only call this when you actually want to retrieve velocity
	 * information, as it is relatively expensive. You can then retrieve the
	 * velocity with {@link #getXVelocity()} and {@link #getYVelocity()}.
	 * 
	 * @param units
	 *            The units you would like the velocity in. A value of 1
	 *            provides pixels per millisecond, 1000 provides pixels per
	 *            second, etc.
	 * @param maxVelocity
	 *            The maximum velocity that can be computed by this method. This
	 *            value must be declared in the same unit as the units
	 *            parameter. This value must be positive.
	 */
	public void computeCurrentVelocity(int units, float maxVelocity) {
		final float[] pastX = mPastX;
		final float[] pastY = mPastY;
		final long[] pastTime = mPastTime;
		final int lastTouch = mLastTouch;

		// find oldest acceptable time
		int oldestTouch = lastTouch;
		if (pastTime[lastTouch] != Long.MIN_VALUE) { // cleared ?
			final float acceptableTime = pastTime[lastTouch]
					- LONGEST_PAST_TIME;
			int nextOldestTouch = (NUM_PAST + oldestTouch - 1) % NUM_PAST;
			while (pastTime[nextOldestTouch] >= acceptableTime
					&& nextOldestTouch != lastTouch) {
				oldestTouch = nextOldestTouch;
				nextOldestTouch = (NUM_PAST + oldestTouch - 1) % NUM_PAST;
			}
		}

		// Kind-of stupid.
		final float oldestX = pastX[oldestTouch];
		final float oldestY = pastY[oldestTouch];
		final long oldestTime = pastTime[oldestTouch];
		float accumX = 0;
		float accumY = 0;
		int mN = (lastTouch - oldestTouch + NUM_PAST) % NUM_PAST + 1;
		// Skip the last received event, since it is probably pretty noisy.
		if (mN > m3) {
			mN--;
		}

		for (int i = 1; i < mN; i++) {
			final int j = (oldestTouch + i) % NUM_PAST;
			final int dur = (int) (pastTime[j] - oldestTime);
			if (dur == 0) {
				continue;
			}
			float dist = pastX[j] - oldestX;
			float vel = (dist / dur) * units; // pixels/frame.
			accumX = (accumX == 0) ? vel : (accumX + vel) * mPointFiveF;

			dist = pastY[j] - oldestY;
			vel = (dist / dur) * units; // pixels/frame.
			accumY = (accumY == 0) ? vel : (accumY + vel) * mPointFiveF;
		}

		mXVelocity = accumX < 0.0f ? Math.max(accumX, -maxVelocity) : Math.min(
				accumX, maxVelocity);
		mYVelocity = accumY < 0.0f ? Math.max(accumY, -maxVelocity) : Math.min(
				accumY, maxVelocity);
	}

	/**
	 * Retrieve the last computed X velocity. You must first call
	 * {@link #computeCurrentVelocity(int)} before calling this function.
	 * 
	 * @return The previously computed X velocity.
	 */
	public float getXVelocity() {
		return mXVelocity;
	}

	/**
	 * Retrieve the last computed Y velocity. You must first call
	 * {@link #computeCurrentVelocity(int)} before calling this function.
	 * 
	 * @return The previously computed Y velocity.
	 */
	public float getYVelocity() {
		return mYVelocity;
	}
}
