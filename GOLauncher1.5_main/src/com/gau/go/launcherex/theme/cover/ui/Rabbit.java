package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * 兔子
 * 
 * @author chenqiang
 * 
 */
public class Rabbit extends BaseElement implements IMovable,
		OnResponTouchListener {
	
	private final static float GIFT_FALL_LIMIT = 0.85f;
	private static final int RABBIT_TYLE_JUMPING = 1;
	private static final int ANIMATION_INTERVAL = 200;
	private static final int ANIMATION_INTERVAL_BLAST = 100;
	private static final int ALIVE_TIME = 10000;
	private static final int RABBIT_JUMP_HEIGHT = 20;
	private long mLastAnimateUpdateTime;
	private long mBlastLastAnimateUpdateTime;
	private Bitmap[] mRabbitBitmap;
	private Bitmap[] mBlastBitmap;
	private int mBitmapIndex = 0;
	private int mBlastBitmapIndex = 0;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mStyle;
	public boolean mIsBlast = false;
	private float mDefaultY;

	public Rabbit(float x, float y, Bitmap[] rabbitBitmap,
			Bitmap[] blastBitmap, int style, int sreenWidth, int sreenHeight) {

		mScreenWidth = sreenWidth;
		mScreenHeight = sreenHeight;

		mRabbitBitmap = rabbitBitmap;
		mBlastBitmap = blastBitmap;
		mBitmap = mRabbitBitmap[mBitmapIndex];

		mLastAnimateUpdateTime = System.currentTimeMillis();
		mBlastLastAnimateUpdateTime = mLastAnimateUpdateTime;
		mX = x - mBitmap.getWidth() / 2;
		mY = y - mBitmap.getHeight() / 2;
		mDefaultY = mY;
		mStyle = style;
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mAlive) {
			drawBitmap(mBitmap, false, camera, matrix, canvas, paint);
		}

	}

	private void drawBitmap(Bitmap bitmap, boolean needShadowNext,
			Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (bitmap != null) {
			canvas.save();
			canvas.translate(mX, mY);
			canvas.drawBitmap(bitmap, matrix, paint);
			canvas.restore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (isTaped(x, y)) {
				mLastAnimateUpdateTime = System.currentTimeMillis();
				mIsBlast = true;
				ret = true;
			}
			break;
		default:
			break;
		}
		return ret;
	}

	@Override
	public void moving() {
		if (mAlive) {
			long current = System.currentTimeMillis();
			if (current - mBlastLastAnimateUpdateTime > ALIVE_TIME) {
				mIsBlast = true;
			}

			if (mIsBlast) {
				if (current - mLastAnimateUpdateTime >= ANIMATION_INTERVAL_BLAST) {
					mLastAnimateUpdateTime = current;
					if (mBlastBitmapIndex == mBlastBitmap.length) {
						mAlive = false;
						return;
					}
					mBitmap = mBlastBitmap[mBlastBitmapIndex++];
				}
			} else {
				if (current - mLastAnimateUpdateTime >= ANIMATION_INTERVAL) {
					mLastAnimateUpdateTime = current;

					if (mBitmapIndex == mRabbitBitmap.length) {
						mBitmapIndex = 0;
						mY = mDefaultY;
					}
//					if (mStyle == RABBIT_TYLE_JUMPING
//							&& mBitmapIndex == mRabbitBitmap.length - 1) {
//						mY = mY - RABBIT_JUMP_HEIGHT;
//					}
					mBitmap = mRabbitBitmap[mBitmapIndex++];
				}
			}
		}

	}

	@Override
	public boolean isTaped(float x, float y) {
		if (mBitmap != null) {
			return Math.abs(mX + mBitmap.getWidth() / 2 - x) <= mBitmap
					.getWidth() >> 1
					&& Math.abs(mY + mBitmap.getHeight() / 2 - y) <= mBitmap
							.getHeight() >> 1;
		}
		return false;
	}

	public void onConfigurationChanged(int width, int height) {

		final float widthOld = mScreenWidth;
		final float heightOld = mScreenHeight;
		mScreenWidth = width;
		mScreenHeight = height;
		mX = (mX / widthOld) * mScreenWidth;
		mY = height * GIFT_FALL_LIMIT - mBitmap.getHeight() / 2;
		mDefaultY = mY;
	}

}
