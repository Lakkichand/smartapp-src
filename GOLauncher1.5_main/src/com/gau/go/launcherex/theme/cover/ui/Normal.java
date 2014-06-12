package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.SensorManager;

/**
 * 静止的一般景物
 * 
 * @author jiangxuwen
 * 
 */
public class Normal extends BaseElement {

	public Normal() {
		mAllowDrag = false;
		mAlive = true;
	}

	private float mFromY;
	private float mToY;
	private float mOffset;

	public void init(float from, float to) {
		mFromY = from ;
		mToY = to;
		mState = TYPE_INCREASE;
	}

	public void onOffset(float lastGravityX, float lastGravityY) {
		mX = (int) (mDefaultX * (1 + lastGravityX / SensorManager.GRAVITY_EARTH));
		mY = (int) (mDefaultY * (1 - lastGravityY / SensorManager.GRAVITY_EARTH));
		doLimit();
	}

	private void doLimit() {
		if (mX < mLimitRect.left) {
			mX = mLimitRect.left;
		}
		if (mY < mLimitRect.top) {
			mY = mLimitRect.top;
		}
		if (mX + mWidth > mLimitRect.right) {
			mX = mLimitRect.right - mWidth;
		}
		if (mY + mHeight > mLimitRect.bottom) {
			mY = mLimitRect.bottom - mHeight;
		}
	}

	private long mCurrTime;
	private int mState;
//	private static final int TYPE_NONE = -1;
	private static final int TYPE_INCREASE = 0;
	private static final int TYPE_REDUCE = 1;

	public void moving() {
		long time = System.currentTimeMillis();
		if (mState == TYPE_INCREASE) {
			if (time - mCurrTime > 2000) {
				increase(1);
				mCurrTime = time;
			}
		} else if (mState == TYPE_REDUCE) {
			if (time - mCurrTime > 100) {
				reduce(8);
				mCurrTime = time;
			}
		} else {

		}
	}

	/**
	 * 响应甩动事件
	 * 
	 * @param speed 
	 */
	public void handShake(float speed) {
		if (mState == TYPE_REDUCE) {
			return;
		}
		if (Math.abs(speed) > 100) {
			mState = TYPE_REDUCE;
		}
	}
	
	/**
	 * 增加
	 * 
	 * @param change
	 */
	private void increase(float change) {
		mState = TYPE_INCREASE;
		if (mY > mToY + change) {
			mY -= change;
			mOffset -= change;
		} else {
			mY = mToY;
		}
	}

	private void reduce(float change) {
		if (mY < mFromY - change) {
			mY += change;
			mOffset += change;
		} else {
			mY = mFromY;
			mState = TYPE_INCREASE;
		}
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (!mAlive || mBitmap == null) {
			return;
		}
		canvas.drawBitmap(mBitmap, mX, mY, paint);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}
}
