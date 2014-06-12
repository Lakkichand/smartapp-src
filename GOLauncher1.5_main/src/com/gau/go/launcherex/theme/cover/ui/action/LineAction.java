package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述:直线运动
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-5]
 */
public class LineAction extends BaseAction {

	private static final int ANIMATION_INTERVAL = 150;
	private long mLastAnimateUpdateTime;
	private long mAnimationStartTime;
	private float mLastSpeedX;
	private float mLastSpeedY;
	private int mActionDelay;
	private int mActionIndex;
	public Bitmap[] mActionBitmaps;
	public Bitmap mDefaultAction2Bitmap;

	LineAction(Drivenable drienable, int actionIndex, int actionDelay, Bitmap[] actionBitmap,
			int animatingBitmapType, Bitmap defaultAction2Bitmap, boolean isBitmapSymmetric) {
		super(drienable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mActionBitmaps = actionBitmap;
		mDefaultAction2Bitmap = defaultAction2Bitmap;
		mActionDelay = actionDelay;
		drienable.mSpeedY = 0;
		drienable.mSpeedX = 0.5f;
		nextAngleState();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mAnimationStartTime = System.currentTimeMillis();
				mLastSpeedX = mDrivenable.mSpeedX;
				mLastSpeedY = mDrivenable.mSpeedY;
				mDrivenable.mSpeedX = 0;
				mDrivenable.mSpeedY = 0;
				mDrivenable.mBitmap = mDefaultAction2Bitmap;
				mActive = true;
				break;

			default :
				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean doAction() {
		long currentLine = System.currentTimeMillis();
		if (currentLine - mAnimationStartTime > mActionDelay) {
			if (mAnimatingBitmapType == ANIMATING_BITMAP_TYPE_CIRCULATION) {
				if (mActionIndex == mActionBitmaps.length) {
					mActionIndex = 0;
				}
				mDrivenable.mBitmap = mActionBitmaps[mActionIndex++];
			}
			if (mAnimatingBitmapType == ANIMATING_BITMAP_TYPE_LASTCONTINUE) {
				if (currentLine - mLastAnimateUpdateTime > ANIMATION_INTERVAL) {
					mLastAnimateUpdateTime = currentLine;
					if (mActionIndex == mActionBitmaps.length - 1) {
						mDrivenable.mTouchState = Drivenable.TOUCH_STATE_IN_NORMAL;
						mDrivenable.mBitmap = mActionBitmaps[mActionIndex];
						mDrivenable.mSpeedX = mLastSpeedX;
						mDrivenable.mSpeedY = mLastSpeedY;
						mAnimationStartTime = System.currentTimeMillis();
						mActionIndex = 0;
					} else {
						mDrivenable.mBitmap = mActionBitmaps[mActionIndex++];
					}
				}
			}
		}
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow, Bitmap defaultAction2Bitmap) {
		mActionBitmaps = actionBitmaps;
		mDefaultAction2Bitmap = defaultAction2Bitmap;
	}

	@Override
	public void cleanUp() {
		mActionBitmaps = null;
		mDefaultAction2Bitmap = null;
	}

}
