package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.MotionEvent;

import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.utils.LineUtils;

/**
 * 
 * <br>类描述:折线运动
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-5]
 */
public class BrokenLineAction extends BaseAction {

	public static final int BITMAP_STATE_ACTION_ONE = 0;
	public static final int BITMAP_STATE_ACTION_TWO = 1;
	public volatile int mBitmapState;
	private static final int BROKENLINE_ANIMATE_DELAY = 4000;
	private static final int LINE_ANIMATE_DELAY = 2000;
	private static final float ANIMATION_TIME_BASE_FACTOR = 600;
	private static final float SPIRIT_START_POSITIONX = -0.12f;
	private static final float SPIRIT_START_POSITIONY = 0.3f;
	private Point mStartBrokenLinePoint = new Point();
	private Point mEndBrokenLinePoint = new Point();
	private int mNextAnimationDelay;
	private int mBrokenLineAnimateCount;
	private long mLastBrokenLineTime;
	private int mAnimationTime = 2000;
	private long mAnimationStartTime;
	private Random mRandom = new Random();
	private int mAction1Index;
	private int mAction2Index;
	private int mCirculationCount = 1;
	private int mDeccelerateSpeedCount;
	public Bitmap[] mAction1Bitmaps;
	public Bitmap[] mAction2Bitmaps;
	public Bitmap mAction1Shadow;
	public Bitmap mAction2Shadow;
	private boolean mNeedLoop;

	BrokenLineAction(Drivenable drivenable, int actionIndex, int animatingBitmapType, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow, boolean needLoop,
			boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mAction1Bitmaps = action1Bitmaps;
		mAction2Bitmaps = action2Bitmaps;
		mAction1Shadow = action1Shadow;
		mAction2Shadow = action2Shadow;
		mNeedLoop = needLoop;
		init();
	}

	private void init() {
		final int screenWidth = DrawUtils.getScreenViewWidth();
		final int screenHeight = DrawUtils.getScreenViewHeight();
		mStartBrokenLinePoint.x = (int) (-screenWidth * SPIRIT_START_POSITIONX);
		mStartBrokenLinePoint.y = (int) (screenHeight * SPIRIT_START_POSITIONY);
		mNextAnimationDelay = 0;
		mAnimationTime = 2000;
		mAnimationStartTime = System.currentTimeMillis();
		mEndBrokenLinePoint = LineUtils.nextBrokenLinePoint(mStartBrokenLinePoint, screenWidth,
				screenHeight);
		mDrivenable.mSpeedX = mEndBrokenLinePoint.x - mStartBrokenLinePoint.x;
		mDrivenable.mSpeedY = mEndBrokenLinePoint.y - mStartBrokenLinePoint.y;
		nextAngleState();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				final int screenWidth = DrawUtils.getScreenViewWidth();
				final int screenHeight = DrawUtils.getScreenViewHeight();
				mStartBrokenLinePoint.x = (int) mDrivenable.mX;
				mStartBrokenLinePoint.y = (int) mDrivenable.mY;
				mEndBrokenLinePoint = LineUtils.nextBrokenLinePoint(mStartBrokenLinePoint,
						screenWidth, screenHeight);
				mDrivenable.mSpeedX = mEndBrokenLinePoint.x - mStartBrokenLinePoint.x;
				mDrivenable.mSpeedY = mEndBrokenLinePoint.y - mStartBrokenLinePoint.y;
				mAnimationTime = (int) ((Math.sqrt(mDrivenable.mSpeedX * mDrivenable.mSpeedX
						+ mDrivenable.mSpeedY * mDrivenable.mSpeedY)
						/ screenWidth + 1) * ANIMATION_TIME_BASE_FACTOR);
				mNextAnimationDelay = 0;
				mLastBrokenLineTime = 0;
				mBrokenLineAnimateCount = 0;
				mAnimationStartTime = System.currentTimeMillis();
				break;
			default :
				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean doAction() {
		long current = System.currentTimeMillis();
		if (current - mLastBrokenLineTime < mNextAnimationDelay) {
			return false;
		}
		float t = current - mAnimationStartTime;
		t = (float) t / mAnimationTime;
		if (t < 1.0f) {
			Point outPoint = new Point();
			LineUtils.getInterpolatorPoint(mStartBrokenLinePoint, mEndBrokenLinePoint, outPoint, t);
			mDrivenable.mX = outPoint.x;
			mDrivenable.mY = outPoint.y;
			nextAngleState();
			mBitmapState = BITMAP_STATE_ACTION_TWO;
		} else {
			boolean needLongDistance = false;
			mBrokenLineAnimateCount++;
			if (mBrokenLineAnimateCount % 4 == 0) {
				if (!mNeedLoop) {
					return false;
				}
				mBrokenLineAnimateCount = 0;
				mNextAnimationDelay = mRandom.nextInt(BROKENLINE_ANIMATE_DELAY)
						+ BROKENLINE_ANIMATE_DELAY;
				mBitmapState = BITMAP_STATE_ACTION_ONE;
			} else if (mBrokenLineAnimateCount % 3 == 0) {
				needLongDistance = true;
				mNextAnimationDelay = LINE_ANIMATE_DELAY + mRandom.nextInt(LINE_ANIMATE_DELAY);
			} else {
				mNextAnimationDelay = LINE_ANIMATE_DELAY + mRandom.nextInt(LINE_ANIMATE_DELAY);
			}
			final int screenWidth = DrawUtils.getScreenViewWidth();
			final int screenHeight = DrawUtils.getScreenViewHeight();
			mDrivenable.mX = mStartBrokenLinePoint.x = mEndBrokenLinePoint.x;
			mDrivenable.mY = mStartBrokenLinePoint.y = mEndBrokenLinePoint.y;
			mEndBrokenLinePoint = needLongDistance ? LineUtils.getRattanAreaPoint(
					mEndBrokenLinePoint, screenWidth, screenHeight) : LineUtils
					.nextBrokenLinePoint(mEndBrokenLinePoint, screenWidth, screenHeight);
			mLastBrokenLineTime = System.currentTimeMillis();
			mDrivenable.mSpeedX = mEndBrokenLinePoint.x - mStartBrokenLinePoint.x;
			mDrivenable.mSpeedY = mEndBrokenLinePoint.y - mStartBrokenLinePoint.y;
			mAnimationTime = (int) ((Math.sqrt(mDrivenable.mSpeedX * mDrivenable.mSpeedX
					+ mDrivenable.mSpeedY * mDrivenable.mSpeedY)
					/ screenWidth + 1) * 600);
			mAnimationStartTime = mLastBrokenLineTime + mNextAnimationDelay;
		}
		return false;
	}

	private void resetBitmapState() {
		if (mBitmapState == BITMAP_STATE_ACTION_ONE) {
			if (mAction1Index == mAction1Bitmaps.length - 1) {
				mCirculationCount++;
				mAction1Index = 0;
			}
			if (++mDeccelerateSpeedCount % mCirculationCount == 0) {
				mDeccelerateSpeedCount = 0;
				mDrivenable.mBitmap = mAction1Bitmaps[mAction1Index++];
			} else {
				mDrivenable.mBitmap = mAction1Bitmaps[mAction1Index];
			}
			mDrivenable.mShadow = mAction1Shadow;
			mAction2Index = 0;
		} else if (mBitmapState == BITMAP_STATE_ACTION_TWO) {
			if (mAction2Index == mAction2Bitmaps.length) {
				mAction2Index = 0;
			}
			mDrivenable.mShadow = mAction2Shadow;
			mDrivenable.mBitmap = mAction2Bitmaps[mAction2Index++];
			mAction1Index = 0;
			mCirculationCount = 1;
		}
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		resetBitmapState();
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		mAction1Bitmaps = action1Bitmaps;
		mAction1Shadow = action1Shadow;
		mAction2Bitmaps = action2Bitmaps;
		mAction2Shadow = action2Shadow;
	}

	@Override
	public void cleanUp() {
		mRandom = null;
		mAction1Bitmaps = null;
		mAction1Shadow = null;
		mAction2Bitmaps = null;
		mAction2Shadow = null;
	}

}
