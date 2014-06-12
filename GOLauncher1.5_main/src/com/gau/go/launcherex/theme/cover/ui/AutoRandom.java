package com.gau.go.launcherex.theme.cover.ui;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.gau.go.launcherex.theme.cover.utils.RandomUtils;

/**
 * 
 * <br>
 * 类描述:随机出现的事物，包括自动出现，吹气出现和摇晃出现 <br>
 * 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-23]
 */
public class AutoRandom extends BaseElement implements IMovable {

	private static final int ANIMATION_TYPE_SINGLEBITMAP = 0;
	private static final int ANIMATION_TYPE_MUTIBITMAP = 1;
	private int mAnimationType;
	private Random mRandom = new Random();
	private int mSpeedAlpha; // 透明速率
	private int mSpeedX; // X方向的速度
	private int mSpeedY; // Y方向的速度
	private int mRotateX; // 旋转X方向的速度
	private int mRotateY; // 旋转Y方向的速度
	private int mTumbleSpdX;
	private int mTumbleSpdY;
	private int mScreenWidth;
	private int mScreenHeight;
	private Matrix mMatrix;
	private Bitmap[] mBitmaps;
	
	public AutoRandom(Bitmap[] bitmaps, boolean allowDrag, int viewWidth,
			int viewHeight, int minTumbleSpdx, int maxTumbleSpdX, int minTumbleSpdY,
			int maxTumbleSpdY, int minSpeedX, int maxSpeedX, int minSpeedY, int maxSpeedY,
			float minScale, float maxScale, int animationType, int showType,
			float startMinLocationX, float startMaxLocationX, float startMinLocationY,
			float startMaxLocationY) {
		mBitmaps = bitmaps;
		mMatrix = new Matrix();
		mAlive = true;
		mScreenHeight = viewHeight;
		mScreenWidth = viewWidth;
		mAllowDrag = allowDrag;
		mDefaultX = (int) (mScreenWidth * startMinLocationX);
		mDefaultY = (int) (mScreenHeight * startMinLocationY);
		mX = RandomUtils.randomFloat(mScreenWidth * startMaxLocationX , mScreenWidth * startMinLocationX);
		mY = RandomUtils.randomFloat(mScreenHeight * startMaxLocationY , mScreenHeight * startMinLocationY);
		mScale = RandomUtils.randomFloat(maxScale, minScale);
		mSpeedAlpha = 0;
		mSpeedX = RandomUtils.randomInt(maxSpeedX, minSpeedX);
		mSpeedY = RandomUtils.randomInt(maxSpeedY, minSpeedY);
//		mRotateX = mRandom.nextInt(45);
//		mRotateY = mRandom.nextInt(45);
		mTumbleSpdX = RandomUtils.randomInt(maxTumbleSpdX, minTumbleSpdx);
		mTumbleSpdY = RandomUtils.randomInt(maxTumbleSpdY, minTumbleSpdY);
		mAnimationType = animationType;
		if (mAnimationType == ANIMATION_TYPE_SINGLEBITMAP) {
			if (mBitmaps != null && mBitmaps.length > 0) {
				mBitmap = mBitmaps[mRandom.nextInt(mBitmaps.length)];
			}
		}
	}

	
	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mAlive) {
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
				int oldAlpha = paint.getAlpha();
				paint.setAlpha(mAlpha);
				canvas.translate(-mBitmap.getWidth() / 2, -mBitmap.getHeight() / 2);
				canvas.drawBitmap(mBitmap, 0, 0, paint);
				canvas.restore();
				if (mAlpha != oldAlpha) {
					paint.setAlpha(oldAlpha);
				}
				mMatrix.reset();
			}
		}
	}

	@Override
	public void moving() {
		if (mAlive) {
			mX += mSpeedX;
			mY += mSpeedY;
			mRotateX += mTumbleSpdX;
			mRotateY += mTumbleSpdY;
			mRotateX %= 360;
			mRotateY %= 360;
			mAlpha += mSpeedAlpha; 
			if (mY >= mScreenHeight || mAlpha <= 0 || mX >= mScreenWidth || mX < mDefaultX || mY < mDefaultY) {
				mAlive = false;
			}
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

	@Override
	public void cleanUp() {
		mMatrix = null;
		mRandom = null;
		super.cleanUp();
	}

}
