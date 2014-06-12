package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 鹿
 * 
 * @author chenqiang
 * 
 */
public class Deer extends Drivenable implements IMovable {

	private static final int ANIMATION_INTERVAL = 150;
	private static final int ANIMATION_INTERVAL_D = 300;

	private static final float SIN_DIFF = 0.8945f;
	private static final float SIN_PEEK = 40.0f;
	private static final float SIN_DIFF_Y_RATE = 0.2f;

	private long mLastAnimateUpdateTime;
	private int mBitmapIndex = 0;
	public float mStarX;
	public float mStarY;

	private int mScreenWidth;
	private int mScreenHeight;

	private Matrix mMatrix;
	private Bitmap[] mDeerBitmap;

	public Deer(Bitmap[] deerBitmap, Matrix matrix, int screenWidth,
			int screenHeight) {
		mDeerBitmap = deerBitmap;
		mMatrix = matrix;
		mLastAnimateUpdateTime = System.currentTimeMillis();
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		drawBitmap(mBitmap, false, camera, matrix, canvas, paint);

	}

	private void drawBitmap(Bitmap bitmap, boolean needShadowNext,
			Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (bitmap != null) {
			canvas.save();
			canvas.translate(mX, mY);
			if (mSpeedX > 0) {
				canvas.translate(mBitmap.getWidth(), 0);
				canvas.concat(mMatrix);
			}
			canvas.drawBitmap(bitmap, matrix, paint);
			canvas.restore();
		}
	}

	@Override
	public void moving() {
		long current = System.currentTimeMillis();
		if (mBitmapIndex == 0 || mBitmapIndex == 4) {
			if (current - mLastAnimateUpdateTime >= ANIMATION_INTERVAL_D) {
				mLastAnimateUpdateTime = current;
				mBitmap = getMoveBitmap();
			}
		} else {
			if (current - mLastAnimateUpdateTime >= ANIMATION_INTERVAL) {
				mLastAnimateUpdateTime = current;
				mBitmap = getMoveBitmap();
			}
		}

		mX += mSpeedX;
		if (mSpeedX < 0) {

			mY = (float) ((mStarY + SIN_PEEK
					* mScale
					* (float) Math
							.sin(((mX / SIN_DIFF) % (mScreenWidth / SIN_DIFF))
									/ (mScreenWidth / SIN_DIFF) * 2 * Math.PI))
					* SIN_DIFF - (mScreenWidth - mX) * SIN_DIFF_Y_RATE);

		} else if (mSpeedX > 0) {

			mY = (float) ((mStarY + SIN_PEEK
					* mScale
					* (float) Math
							.sin(((mX / SIN_DIFF) % (mScreenWidth / SIN_DIFF))
									/ (mScreenWidth / SIN_DIFF) * 2 * Math.PI))
					* SIN_DIFF - mX * SIN_DIFF_Y_RATE);
		}

	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

	public Bitmap getMoveBitmap() {
		if (mBitmapIndex == mDeerBitmap.length) {
			mBitmapIndex = 0;
		}
		return mDeerBitmap[mBitmapIndex++];

	}

	@Override
	public void cleanUp() {
		recycle(mDeerBitmap);
		recycle(mBitmap);
	}

	@Override
	public boolean checkOutOfBound() {
		return false;
	}

	public void onConfigurationChanged(int width, int height) {

		mScreenWidth = width;
		mScreenHeight = height;

	}

}
