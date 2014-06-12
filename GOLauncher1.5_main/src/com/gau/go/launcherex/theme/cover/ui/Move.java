package com.gau.go.launcherex.theme.cover.ui;

import java.util.Random;

import com.gau.go.launcherex.theme.cover.utils.RandomUtils;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 按住屏幕移动时出现的景物
 * @author jiangxuwen
 *
 */
public class Move implements IDrawable, ICleanable, IMovable {

//	private static final float SPEED_FACTOR = 1.0f;
//	private int mLifeTime; // 生存时间
	private Random mRandom = new Random();
	private int mSpeedAlpha;
	private float mFloatSpeedX;
	private float mFloatSpeedY;
	private int mRotateX;
	private int mRotateY;
	private int mTumbleSpdX;
	private int mTumbleSpdY;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mX;
	private int mY;
	private float mScale = 1.0f;
	private Matrix mMatrix = new Matrix();
	private Bitmap mBitmap;
	private boolean mAlive;
	private int mAlpha;

	/**@param bitmaps not null
	 * <默认构造函数>
	 */
	public Move(Bitmap[] bitmaps, int viewWidth, int viewHeight, int x, int y, int speedAlpha,
			int life, int minTumbleSpdx, int maxTumbleSpdX, int minTumbleSpdY, int maxTumbleSpdY,
			int minSpeedX, int maxSpeedX, int minSpeedY, int maxSpeedY, float minScale,
			float maxScale) {
		mScreenHeight = viewHeight;
		mScreenWidth = viewWidth;
		mX = x;
		mY = y;
		mSpeedAlpha = speedAlpha;
//		mRotateX = mRandom.nextInt(180);
//		mRotateY = mRandom.nextInt(180);
		mScale = RandomUtils.randomFloat(maxScale, minScale);
		mTumbleSpdX = RandomUtils.randomInt(maxTumbleSpdX, minTumbleSpdx);
		mTumbleSpdY = RandomUtils.randomInt(maxTumbleSpdY, minTumbleSpdY);
		mFloatSpeedX = RandomUtils.randomFloat(maxSpeedX, minSpeedX);
		mFloatSpeedY = RandomUtils.randomFloat(maxSpeedY, minSpeedY);
		mAlive = true;
		if (bitmaps != null && bitmaps.length > 0) {
			mBitmap = bitmaps[mRandom.nextInt(bitmaps.length)];
		}
//		mLifeTime = life; // 暂不用
		mAlpha = 255;
	}


	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mAlive) {
			int oldAlpha = paint.getAlpha();
			try {
				if (mBitmap != null) {
					mMatrix.set(matrix);
					canvas.save();
					canvas.translate(mX, mY);
					canvas.scale(mScale, mScale);
					camera.save();
					camera.rotateX(mRotateX);
					camera.rotateY(mRotateY);
					camera.getMatrix(mMatrix);
					canvas.concat(mMatrix);
					camera.restore();
					if (mAlpha != oldAlpha) {
						paint.setAlpha(mAlpha);
					}
					canvas.drawBitmap(mBitmap, 0, 0, paint);
					canvas.restore();

					mMatrix.reset();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				paint.setAlpha(oldAlpha);
			}
		}
	}

	@Override
	public void cleanUp() {
		mBitmap = null;
		mRandom = null;
		mMatrix = null;
	}

	public boolean isActive() {
		return mAlive;
	}

	@Override
	public void moving() {
		if (mAlive) {
			mX += mFloatSpeedX;
			mY += mFloatSpeedY;
			mRotateX += mTumbleSpdX;
			mRotateY += mTumbleSpdY;
			mRotateX %= 360;
			mRotateY %= 360;
			mAlpha += mSpeedAlpha;
			if (mY >= mScreenHeight || mAlpha <= 0 || mX >= mScreenWidth || mX <= 0 || mY <= 0) {
				mAlive = false;
			}
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

}
