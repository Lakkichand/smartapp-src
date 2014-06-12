package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.gau.go.launcherex.theme.cover.ui.ICleanable;
import com.gau.go.launcherex.theme.cover.ui.OnResponTouchListener;

/**
 * 
 * <br>类描述:动作行为
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-3]
 */
public abstract class BaseAction implements OnResponTouchListener, ICleanable {

	public volatile boolean mActive;
	public Drivenable mDrivenable;
	public static final int ANIMATING_BITMAP_TYPE_LASTCONTINUE = 0;
	public static final int ANIMATING_BITMAP_TYPE_CIRCULATION = 1;
	protected int mAnimatingBitmapType = ANIMATING_BITMAP_TYPE_CIRCULATION;
	public BaseAction mAdditionalAction;
	protected boolean mIsBitmapSymmetric;
	public int mActionIndex;
	
	public BaseAction(Drivenable drivenable, int actionIndex, int animatingBitmapType, boolean isBitmapSymmetric) {
		mDrivenable = drivenable;
		mAnimatingBitmapType = animatingBitmapType;
		mIsBitmapSymmetric = isBitmapSymmetric;
		mActionIndex = actionIndex;
		
	}

	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {

	}
	
	public void handAction() {
		
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	public boolean needBitmapYMapping() {
		return false;
	}
	
	public abstract  boolean doAction();

	public abstract Bitmap getBimap(int deccelerate);

	public void resetState() {
		mActive = false;
		mDrivenable = null;
	}

	public void resetData() {
	}
	
	
	public void setDrivenable(Drivenable drivenable) {
		if (drivenable != null) {
			mDrivenable = drivenable;
		}
	}
	
	public void regist() {
		
	};
	public void unRegist() {
		
	};

	protected void nextAngleState() {
		if (mDrivenable.mSpeedY != 0) {
			double tan = Math.atan((float) Math.abs(mDrivenable.mSpeedX)
					/ Math.abs(mDrivenable.mSpeedY));
			tan = Math.toDegrees(tan);
			if (mDrivenable.mSpeedX > 0) {
				if (mDrivenable.mSpeedY > 0) {
					mDrivenable.mAngle = (int) (180 - tan) - mDrivenable.mDefaultAngle;
				} else {
					mDrivenable.mAngle = (int) tan - mDrivenable.mDefaultAngle;
				}
			} else {
				if (mDrivenable.mSpeedY > 0) {
					mDrivenable.mAngle = (int) (tan - 180) - mDrivenable.mDefaultAngle;
				} else {
					mDrivenable.mAngle = (int) -tan - mDrivenable.mDefaultAngle;
				}
			}
		} else {
			if (mDrivenable.mSpeedX > 0) {
				mDrivenable.mAngle = 90 - mDrivenable.mDefaultAngle;
			} else {
				mDrivenable.mAngle = -90 - mDrivenable.mDefaultAngle;
			}
		}
	}

	public abstract void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap);

}
