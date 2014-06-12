package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;

/**
 * 
 * <br>类描述:正常运动
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-5]
 */
public class NormalAction extends BaseAction {
	private int mActionIndex;
	public Bitmap[] mActionBitmaps;
	private int mDeccelerateSpeedCount;

	NormalAction(Drivenable drivenable, int actionIndex, int animatingBitmapType, Bitmap[] actionBitmaps, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mActionBitmaps = actionBitmaps;
		drivenable.mSpeedX = 0.5f;
		drivenable.mSpeedY = 0.5f;
		nextAngleState();
	}

	@Override
	public boolean doAction() {
		mDrivenable.mX += mDrivenable.mSpeedX;
		mDrivenable.mY += mDrivenable.mSpeedY;
		if (mDrivenable.checkOutOfBound()) {
			nextAngleState();
		}
		return false;
	}
	
	@Override
	public boolean needBitmapYMapping() {
		return !mIsBitmapSymmetric && mDrivenable.mAngle == -180;
	}
	
	private Bitmap getActionOneBitmap(int deccelerate) {
		if (mActionIndex >= mActionBitmaps.length) {
			mActionIndex = 0;
		}
		if (mDrivenable.mActionTwoType == ActionControler.ACTION_TYPE_LINE
				|| mDrivenable.mActionTwoType == ActionControler.ACTION_TYPE_BEZIER) {
			if (++mDeccelerateSpeedCount % deccelerate == 0) {
				mDeccelerateSpeedCount = 0;
				return mActionBitmaps[mActionIndex++];
			} else {
				return mActionBitmaps[mActionIndex];
			}
		} else {
			return mActionBitmaps[mActionIndex++];
		}
	}

	@SuppressWarnings("unused")
	private boolean equalZero(float x) {
		if (Math.abs(x) < 1E-2) {
			return true;
		}
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return getActionOneBitmap(deccelerate);
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		mActionBitmaps = actionBitmaps;
	}

	@Override
	public void cleanUp() {
		mActionBitmaps = null;
	}

}
