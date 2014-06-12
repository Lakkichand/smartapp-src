package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.utils.BezierCalculator;

/**
 * 
 * <br>类描述:bezier运动
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-3]
 */
public class BezierAction extends BaseAction {

	private static final int ANIMATION_INTERVAL = 150;
	private static final int MIN_ANGLE_CHANGE_SLOP = 7;
	private int mBaseAnimationTime = 1000;
	private int mAnimationTime = 2000;
	private Random mRandom = new Random();
	private long mAnimationStartTime;
	private volatile Point[] mBezierPoints = new Point[] { new Point(), new Point(), new Point(),
			new Point() };
	private Point mLastPoint = new Point();
	private float mLastAngle;
	private int mActionDelay;
	private int mActionIndex;
	private long mLastAnimateUpdateTime;
	public Bitmap[] mActionBitmap;
	private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
	private boolean mNeedLoop;
	
	BezierAction(Drivenable drivenable, int actionIndex, int actionDelay, Bitmap[] actionBitmaps, int animatingBitmapType
			, boolean needLoop, int animationTime, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		drivenable.mSpeedX = 1;
		drivenable.mSpeedY = 1;
		mActionDelay = actionDelay;
		mActionBitmap = actionBitmaps;
		nextAngleState();
		mNeedLoop = needLoop;
		if (needLoop) {
			mBaseAnimationTime = animationTime;
			initState(mDrivenable.mLimitRect.left, mDrivenable.mLimitRect.top);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				initState(x, y);
				break;

			default :
				break;
		}
		return super.onTouchEvent(event);
	}

	private void initState(final float x, final float y) {
		final int screenWidth = DrawUtils.getScreenViewWidth();
		mAnimationTime = mRandom.nextInt(mBaseAnimationTime) + mBaseAnimationTime;
		mAnimationStartTime = System.currentTimeMillis();
		mBezierPoints[0].x = (int) x;
		mBezierPoints[0].y = (int) y;
		Point endPoint = BezierCalculator.getEndPoint(mBezierPoints[0], mDrivenable.mLimitRect);
		int endx = endPoint.x;
		int endy = endPoint.y;
		if (Math.sqrt((endx - x) * (endx - x) + (endy - y) * (endy - y)) < screenWidth >> 1) {
			Point controlerPoint = BezierCalculator.getControlerByTwice(mBezierPoints[0],
					endPoint);
			mBezierPoints[1].x = controlerPoint.x;
			mBezierPoints[1].y = controlerPoint.y;
			mBezierPoints[2].x = endx;
			mBezierPoints[2].y = endy;
			mBezierPoints[3].x = 0;
			mBezierPoints[3].y = 0;
		} else {
			Point[] points = BezierCalculator.getControlerByThree(mBezierPoints[0], endPoint);
			mBezierPoints[1].x = points[0].x;
			mBezierPoints[1].y = points[0].y;
			mBezierPoints[2].x = points[1].x;
			mBezierPoints[2].y = points[1].y;
			mBezierPoints[3].x = endx;
			mBezierPoints[3].y = endy;
		}
		mLastPoint.x = (int) mDrivenable.mX;
		mLastPoint.y = (int) mDrivenable.mY;
		mLastAngle = mDrivenable.mAngle;
	}
	
	@Override
	public boolean doAction() {
		handleBezierStyle();
		long current = System.currentTimeMillis();
		if (current - mAnimationStartTime > mActionDelay) {
			if (mAnimatingBitmapType == ANIMATING_BITMAP_TYPE_CIRCULATION) {
				if (mActionIndex == mActionBitmap.length) {
					mActionIndex = 0;
				}
				mDrivenable.mBitmap = mActionBitmap[mActionIndex++];
			}
			if (mAnimatingBitmapType == ANIMATING_BITMAP_TYPE_LASTCONTINUE) {
				if (current - mLastAnimateUpdateTime > ANIMATION_INTERVAL) {
					mLastAnimateUpdateTime = current;
					if (mActionIndex == mActionBitmap.length - 1) {
						mDrivenable.mBitmap = mActionBitmap[mActionIndex];
					} else {
						mDrivenable.mBitmap = mActionBitmap[mActionIndex++];
					}
				}
			}
		}
		return false;
	}

	private float handleBezierStyle() {
		float t = System.currentTimeMillis() - mAnimationStartTime;
		t = (float) t / mAnimationTime;
		if (t >= 0 && t <= 1.0f) {
			t = mInterpolator.getInterpolation(t);
		}
		if (t < 1.0f) {
			Point point;
			if (mBezierPoints[3].x == 0 && mBezierPoints[3].y == 0) {
				point = BezierCalculator.twiceOrderBezier(mBezierPoints, t);
			} else {
				point = BezierCalculator.threeOrderBezier(mBezierPoints, t);
			}
			if (Math.sqrt((mLastPoint.x - point.x) * (mLastPoint.x - point.x)
					+ (mLastPoint.y - point.y) * (mLastPoint.y - point.y)) > MIN_ANGLE_CHANGE_SLOP) {
				double angle1 = Math.atan2(point.x - mLastPoint.x, point.y - mLastPoint.y);
				double angle2 = Math.atan2(mDrivenable.mSpeedX, mDrivenable.mSpeedY);
				mDrivenable.mAngle = (int) (Math.toDegrees(angle2 - angle1) + mLastAngle) - mDrivenable.mDefaultAngle;
				mLastPoint.x = point.x;
				mLastPoint.y = point.y;
			}
			mDrivenable.mX = point.x;
			mDrivenable.mY = point.y;
		} else {
			mDrivenable.mTouchState = Drivenable.TOUCH_STATE_IN_NORMAL;
			int i = 3;
			if (mBezierPoints[3].x == 0 && mBezierPoints[3].y == 0) {
				i = 2;
			}
			int tempX = mBezierPoints[i].x - mBezierPoints[0].x;
			int tempY = mBezierPoints[i].y - mBezierPoints[0].y;
			int factorY = tempY < 0 ? -1 : 1;
			int factorX = tempX < 0 ? -1 : 1;
			mDrivenable.mSpeedY = 0.5f * factorY/* * (1 + mRandom.nextInt(2))*/;
			mDrivenable.mSpeedX = 0.5f * factorX/* * (1 + mRandom.nextInt(2))*/;
			mAnimationStartTime = System.currentTimeMillis();
			nextAngleState();
			if (mNeedLoop) {
				initState(mDrivenable.mX, mDrivenable.mY);
			} 
		}
		return t;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow, Bitmap defaultAction2Bitmap) {
		mActionBitmap = actionBitmaps;
	}

	@Override
	public void cleanUp() {
		mRandom = null;
		mBezierPoints = null;
		mActionBitmap = null;
		mLastPoint = null;
	}

}
