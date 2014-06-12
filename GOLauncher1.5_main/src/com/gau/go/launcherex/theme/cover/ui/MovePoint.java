package com.gau.go.launcherex.theme.cover.ui;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 
 * @author
 * 
 */
public class MovePoint extends BaseElement implements IMovable {

	private static final int ALPHA_SPEED = 7;
	private static final int MAX_SNOW_INDEX = 3;
	private long mLastAnimateUpdateTime;
	private long mLastAnimateChangeTime;
	private Random mRandom = new Random();
	private Bitmap[] mPointBitmap;

	private int mLiveTime;
	private int mBitmapIndex;

	private float mPerTime;
	private int mAlpha;
	private int mMaxIndex;

	public MovePoint(Bitmap[] pointBitmap, float x, float y, int liveTime,
			int maxIndex, int alpha) {
		mAlive = true;
		mPointBitmap = pointBitmap;
		mMaxIndex = maxIndex;
		mBitmapIndex = MAX_SNOW_INDEX - maxIndex;
		mBitmap = mPointBitmap[MAX_SNOW_INDEX - maxIndex];
		mX = x;
		mY = y;
		mLiveTime = liveTime;
		mLastAnimateUpdateTime = System.currentTimeMillis();
		mLastAnimateChangeTime = mLastAnimateUpdateTime;
		mPerTime = (float) liveTime / (maxIndex + 1);
		mAlpha = alpha;

	}

	public void onConfigurationChanged(int width, int height) {

	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		final long current = System.currentTimeMillis();
		if (current - mLastAnimateUpdateTime > mLiveTime) {
			mAlive = false;
		}

		if (mAlive) {
			drawBitmap(mBitmap, false, camera, matrix, canvas, paint);
		}
	}

	private void drawBitmap(Bitmap bitmap, boolean needShadowNext,
			Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (bitmap != null) {
			canvas.save();
			canvas.translate(mX, mY);
			int oldAlpha = paint.getAlpha();
			try {
				if (mMaxIndex < 1) {
					mAlpha = mAlpha - ALPHA_SPEED;
					if (oldAlpha != mAlpha) {
						paint.setAlpha(mAlpha--);
					}
				} else {
					if (oldAlpha != mAlpha) {
						paint.setAlpha(mAlpha);
					}
				}
				canvas.drawBitmap(bitmap, matrix, paint);
			} catch (Exception e) {
				// e.printStackTrace();
			} finally {
				paint.setAlpha(oldAlpha);
			}
			canvas.restore();
		}
	}

	@Override
	public void moving() {

		final long current = System.currentTimeMillis();
		if (current - mLastAnimateChangeTime > mPerTime) {
			mLastAnimateChangeTime = current;
			if (mBitmapIndex == mPointBitmap.length) {
				mAlive = false;
			}

			mBitmap = mPointBitmap[mBitmapIndex++];
			mMaxIndex--;
		}

	}

	@Override
	public boolean isTaped(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cleanUp() {
		if (mBitmap != null) {
			mBitmap = null;
		}
		mRandom = null;
		mPointBitmap = null;
		super.cleanUp();
	}

}
