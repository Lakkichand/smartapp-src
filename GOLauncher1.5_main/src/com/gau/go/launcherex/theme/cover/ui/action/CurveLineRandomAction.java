package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.gau.go.launcherex.theme.cover.utils.CurveLineUtils;

/**
 * 
 * <br>类描述:直线与曲线随机
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-13]
 */
public class CurveLineRandomAction extends BaseAction {

	private static final int MAX_ROTATE_ANGLE = 45;
	private static final int ACTIVE_DELAY = 1000 * 30;
	private static final int DEFAULT_CURVE_COUNT = 2;
	private static final int END_CURVE_COUNT = 4;
	private static final int DEFAULT_START_ANGLE = 180;
	private static final int DEFAULT_END_ANGLE = -180;
	private static final int BASE_CURVE_DIAMETER = 10;
	private static final int MOVE_STATE_STOP = 3;
	private static final int MOVE_STATE_CURVE_RIGHT = 2;
	private static final int MOVE_STATE_CURVE_LEFT = 1;
	private static final int MOVE_STATE_LINE = 0;
	private volatile int mMoveState = MOVE_STATE_LINE;
	private Point mStartPoint;
	private Point mEndPoint;
	private static final int BASE_LINE_ANIMATION_TIME = 1500;
	private static final int BASE_CURVE_ANIMATION_TIME = 1000;
	private int mAnimationTime;
	private long mLastUpdateTime;
	private Random mRandom;
	private Rect mLimiteRect;
	private Bitmap[] mBitmaps;
	private Point mCenterPoint;
	private Point mCurrentPoint;
	private int mBitmapIndex;
	private Bitmap mShadow;
	private int mDiameter;
	private int mPositionXFactor;
	private int mLimitWidth;
	private int mLimitHeight;
	private int mCurveCount;
	private boolean mEnd;
	private static final int ORIENTATION_LEFTTORIGHT = 0;
	private static final int ORIENTATION_RIGHTTOLEFT = 1;
	private int mOrientation = ORIENTATION_LEFTTORIGHT;

	public CurveLineRandomAction(Drivenable drivenable, int actionIndex, int animatingBitmapType, Bitmap[] bitmaps,
			Bitmap shadow, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mBitmaps = bitmaps;
		mShadow = shadow;
		initData();
	}

	private void initData() {
		mEnd = false;
		mActive = true;
		mRandom = new Random();
		mLimiteRect = mDrivenable.mLimitRect;
		mAnimationTime = BASE_LINE_ANIMATION_TIME + mRandom.nextInt(BASE_LINE_ANIMATION_TIME >> 1);
		mLastUpdateTime = System.currentTimeMillis();
		mLimitWidth = mLimiteRect.right - mLimiteRect.left;
		mLimitHeight = mLimiteRect.bottom - mLimiteRect.top;
		mStartPoint = new Point(mLimiteRect.left, mLimitHeight * 4 / 5);
		mPositionXFactor = 1;
		mCurveCount = DEFAULT_CURVE_COUNT;
		mEndPoint = new Point(
				mRandom.nextInt(mLimitWidth / 8) + mLimitWidth / 8 * mPositionXFactor,
				mRandom.nextInt(mLimitHeight / 6) + mLimitHeight / 3);
		mCurrentPoint = new Point();
		mCenterPoint = new Point();
	}

	@Override
	public boolean doAction() {
		if (mActive) {
			switch (mMoveState) {
				case MOVE_STATE_CURVE_LEFT :
					handleCurveLeftState();
					break;
				case MOVE_STATE_CURVE_RIGHT :
					handleCurveRightState();
					break;
				case MOVE_STATE_LINE :
					handleLineState();
					break;
				case MOVE_STATE_STOP :
					handleStopState();
					break;
				default :
					break;
			}
		} else {
			long current = System.currentTimeMillis();
			if (current - mLastUpdateTime > ACTIVE_DELAY) {
				mActive = true;
				mLastUpdateTime = current;
				mEnd = false;
				mCurveCount = DEFAULT_CURVE_COUNT;
				mMoveState = MOVE_STATE_LINE;
				if (mOrientation == ORIENTATION_LEFTTORIGHT) {
					mDrivenable.mX = mCurrentPoint.x = mStartPoint.x = mLimiteRect.left;
					mDrivenable.mY = mCurrentPoint.y = mStartPoint.y = mLimitHeight * 4 / 5;
					mPositionXFactor = 1;
					mEndPoint.x = mRandom.nextInt(mLimitWidth / 8) + mLimitWidth / 8
							* mPositionXFactor;
					mEndPoint.y = mRandom.nextInt(mLimitHeight / 6) + mLimitHeight / 3;
				} else if (mOrientation == ORIENTATION_RIGHTTOLEFT) {
					mDrivenable.mX = mCurrentPoint.x = mStartPoint.x = mLimiteRect.right;
					mDrivenable.mY = mCurrentPoint.y = mStartPoint.y = mLimitHeight * 4 / 5;
					mPositionXFactor = 4;
					mEndPoint.x = mRandom.nextInt(mLimitWidth / 8) + mLimitWidth / 8
							* mPositionXFactor;
					mEndPoint.y = mRandom.nextInt(mLimitHeight / 6) + mLimitHeight / 3;
				}
			}
		}
		return false;
	}

	@Override
	public boolean needBitmapYMapping() {
		return !mIsBitmapSymmetric && mOrientation == ORIENTATION_RIGHTTOLEFT;
	}

	private void handleStopState() {
		long current = System.currentTimeMillis();
		float t = current - mLastUpdateTime;
		if (t >= mAnimationTime) {
			mMoveState = MOVE_STATE_LINE;
			mStartPoint.x = mEndPoint.x;
			mStartPoint.y = mEndPoint.y;
			if (mOrientation == ORIENTATION_LEFTTORIGHT) {
				mEndPoint.x = mLimitWidth * 11 / 14;
			} else if (mOrientation == ORIENTATION_RIGHTTOLEFT) {
				mEndPoint.x = -mLimitWidth * 2 / 14;
			}
			mEndPoint.y = mLimitHeight >> 1;
			mLastUpdateTime = current;
			mAnimationTime = BASE_LINE_ANIMATION_TIME;
			mEnd = true;
		}
	}

	private void handleLineState() {
		long current = System.currentTimeMillis();
		float t = current - mLastUpdateTime;
		if (t <= mAnimationTime) {
			t = t / mAnimationTime;
			CurveLineUtils.getLinePoint(mStartPoint, mEndPoint, t, mCurrentPoint);
			mDrivenable.mY = mCurrentPoint.y;
			mDrivenable.mX = mCurrentPoint.x;
			if (mEndPoint.y > mStartPoint.y) {
				int past = mCurrentPoint.y - mStartPoint.y;
				int all = mEndPoint.y - mStartPoint.y;
				if (past <= (all >> 1)) {
					mDrivenable.mAngle = MAX_ROTATE_ANGLE * past * 2 / all;
				} else {
					mDrivenable.mAngle = MAX_ROTATE_ANGLE * (all - past) * 2 / all;
				}
			} else {
				mDrivenable.mAngle = 0;
			}
		} else {
			mDrivenable.mAngle = 0;
			mDrivenable.mX = mEndPoint.x;
			mDrivenable.mY = mEndPoint.y;
			mLastUpdateTime = current;
			if (mEnd) {
				mActive = false;
				if (mOrientation == ORIENTATION_LEFTTORIGHT) {
					mOrientation = ORIENTATION_RIGHTTOLEFT;
				} else if (mOrientation == ORIENTATION_RIGHTTOLEFT) {
					mOrientation = ORIENTATION_LEFTTORIGHT;
				}
				return;
			}
			if ((mOrientation == ORIENTATION_LEFTTORIGHT && mPositionXFactor == END_CURVE_COUNT)
					|| (mOrientation == ORIENTATION_RIGHTTOLEFT && mPositionXFactor == 0)) {
				mMoveState = MOVE_STATE_STOP;
				mAnimationTime = BASE_CURVE_ANIMATION_TIME * 10;
			} else {
				mMoveState = MOVE_STATE_CURVE_RIGHT;
				mDiameter = mRandom.nextInt(BASE_CURVE_DIAMETER) + BASE_CURVE_DIAMETER;
				mCenterPoint.y = mEndPoint.y;
				mCenterPoint.x = mEndPoint.x + mDiameter;
				mAnimationTime = BASE_CURVE_ANIMATION_TIME;
				mCurveCount = DEFAULT_CURVE_COUNT;
			}

		}
	}

	private void handleCurveRightState() {
		long current = System.currentTimeMillis();
		float t = current - mLastUpdateTime;
		if (t <= mAnimationTime) {
			t = t / mAnimationTime;
			CurveLineUtils.getCurvePoint(mCenterPoint, mDiameter, DEFAULT_START_ANGLE,
					DEFAULT_END_ANGLE, t, mCurrentPoint);
			mDrivenable.mY = mCurrentPoint.y;
			mDrivenable.mX = mCurrentPoint.x;
		} else {
			mLastUpdateTime = current;
			mMoveState = MOVE_STATE_CURVE_LEFT;
			mDiameter = mRandom.nextInt(BASE_CURVE_DIAMETER) + BASE_CURVE_DIAMETER;
			mCenterPoint.y = mEndPoint.y;
			mCenterPoint.x = mEndPoint.x - mDiameter;
			mAnimationTime = BASE_CURVE_ANIMATION_TIME;
		}
	}

	private void handleCurveLeftState() {
		long current = System.currentTimeMillis();
		float t = current - mLastUpdateTime;
		if (t <= mAnimationTime) {
			t = t / mAnimationTime;
			CurveLineUtils.getCurvePoint(mCenterPoint, mDiameter, 0, 360, t, mCurrentPoint);
			mDrivenable.mY = mCurrentPoint.y;
			mDrivenable.mX = mCurrentPoint.x;
		} else {
			mCurveCount--;
			if (mCurveCount > 0) {
				mDrivenable.mX = mEndPoint.x;
				mDrivenable.mY = mEndPoint.y;
				mLastUpdateTime = current;
				mMoveState = MOVE_STATE_CURVE_RIGHT;
				mDiameter = mRandom.nextInt(BASE_CURVE_DIAMETER) + BASE_CURVE_DIAMETER;
				mCenterPoint.y = mEndPoint.y;
				mCenterPoint.x = mEndPoint.x + mDiameter;
				mAnimationTime = BASE_CURVE_ANIMATION_TIME;
			} else {
				if (mOrientation == ORIENTATION_LEFTTORIGHT) {
					mPositionXFactor++;
				} else if (mOrientation == ORIENTATION_RIGHTTOLEFT) {
					mPositionXFactor--;
				}
				mLastUpdateTime = current;
				mStartPoint.x = mEndPoint.x;
				mStartPoint.y = mEndPoint.y;
				mMoveState = MOVE_STATE_LINE;
				if (mOrientation == ORIENTATION_LEFTTORIGHT && mPositionXFactor == END_CURVE_COUNT) {
					mAnimationTime = BASE_LINE_ANIMATION_TIME >> 1;
					mEndPoint.x = mRandom.nextInt(mLimitWidth / 8) + mLimitWidth / 7
							* mPositionXFactor;
					mEndPoint.y = mLimitHeight * 4 / 7;
				} else if (mOrientation == ORIENTATION_RIGHTTOLEFT && mPositionXFactor == 0) {
					mAnimationTime = BASE_LINE_ANIMATION_TIME >> 1;
					mEndPoint.x = -(mRandom.nextInt(mLimitWidth / 8) + mLimitWidth / 7);
					mEndPoint.y = mLimitHeight * 4 / 7;
				} else {
					mEndPoint.x = mRandom.nextInt(mLimitWidth / 8) + mLimitWidth / 8
							* mPositionXFactor;
					mEndPoint.y = mRandom.nextInt(mLimitHeight / 6) + mLimitHeight / 3;
					mAnimationTime = BASE_LINE_ANIMATION_TIME;
				}
			}
		}
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mBitmapIndex >= mBitmaps.length) {
			mBitmapIndex = 0;
		}
		mDrivenable.mBitmap = mBitmaps[mBitmapIndex++];
		super.onDraw(camera, matrix, canvas, paint);
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		mBitmaps = actionBitmaps;
	}

	@Override
	public void cleanUp() {
		mBitmaps = null;
		mRandom = null;
		mShadow = null;
		mLimiteRect = null;
	}

}
