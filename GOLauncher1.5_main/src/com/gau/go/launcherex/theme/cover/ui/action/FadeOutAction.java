package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述:附件的淡出动画
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-10]
 */
public class FadeOutAction extends BaseAction {

	private static final float ANIMATION_TIME = 4000;
	private static final int MAX_ALPHA = 255;
	private Bitmap[] mBitmaps;
	private Bitmap mShadow;
	private long mLastUpdateTime;
	private volatile int mFadeOutAlpha;
	private boolean mActive;
	private int mBitmapIndex;

	public FadeOutAction(Drivenable drivenable, int actionIndex, int animatingBitmapType, Bitmap[] bitmaps,
			Bitmap shadow, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mShadow = shadow;
		mBitmaps = bitmaps;
	}

	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mActive) {
			if (mBitmapIndex >= mBitmaps.length - 1) {
				mBitmapIndex = 0;
			}
			if (mBitmaps[mBitmapIndex] != null) {
				drawBitmap(mBitmaps[mBitmapIndex++], camera, matrix, canvas, paint);
			}
			if (mShadow != null) {
				drawBitmap(mShadow, camera, matrix, canvas, paint);
			}
		}
		super.onDraw(camera, matrix, canvas, paint);
	}

	private void drawBitmap(Bitmap bitmap, Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (bitmap != null) {
			canvas.save();
			canvas.translate(mDrivenable.mX, mDrivenable.mY);
			canvas.translate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
			canvas.rotate(mDrivenable.mAngle, bitmap.getWidth() * mDrivenable.mPivotX,
					bitmap.getHeight() * mDrivenable.mPivotY);
			int oldAlpha = paint.getAlpha();
			if (oldAlpha != mFadeOutAlpha) {
				paint.setAlpha(mFadeOutAlpha);
			}
			canvas.drawBitmap(bitmap, matrix, paint);
			if (oldAlpha != mFadeOutAlpha) {
				paint.setAlpha(oldAlpha);
			}
			canvas.restore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mLastUpdateTime = System.currentTimeMillis();
		mActive = true;
		return super.onTouchEvent(event);
	}

	@Override
	public boolean doAction() {
		if (mActive) {
			long current = System.currentTimeMillis();
			if (current - mLastUpdateTime < ANIMATION_TIME) {
				mFadeOutAlpha = (int) (MAX_ALPHA * (1 - (current - mLastUpdateTime)
						/ ANIMATION_TIME));
			} else {
				mActive = false;
				mFadeOutAlpha = MAX_ALPHA;
				mLastUpdateTime = current;
			}
		}
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		mBitmaps = actionBitmaps;
		mShadow = shadow;
	}

	@Override
	public void cleanUp() {
		mBitmaps = null;
		mShadow = null;
	}
	
}
