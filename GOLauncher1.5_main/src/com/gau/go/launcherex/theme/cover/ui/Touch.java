package com.gau.go.launcherex.theme.cover.ui;

import java.util.ArrayList;
import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 触摸屏幕时出现的景物
 * 
 * @author jiangxuwen
 * 
 */
public class Touch extends BaseElement implements IMovable {
	public static final int TYPE_FALL = 0;
	public static final int TYPE_FLICKER = 1;

	private float mRotateX;
	private float mRotateY;
	// private float mAngle;
	private Random mRandom = new Random();

	private int mSpeedAlpha;
	float mTumbleSpdX;
	float mTumbleSpdY;
	volatile float mSpeedX;
	volatile float mSpeedY;
	private int mScreenWidth;
	private int mScreenHeight;
	private Matrix mMatrix;
	private static final int ROTATE_RANGE = 360;
	private Bitmap[] mTouchBitmaps; // 星星图片，图标从大到小
	private ArrayList<Start> mStartList = new ArrayList<Start>();
	private int mAnimateType;

	public Touch(int screenWidth, int screenHeight, float x, float y,
			Bitmap flowerImg, int type, Bitmap[] touchBitmaps) {
		mMatrix = new Matrix();
		mX = x;
		mY = y;
		mAlive = true;
		mAlpha = 255;
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
		mAnimateType = type;
		switch (type) {
		case TYPE_FALL:
			mScale = getRadomLittleScale();
			mSpeedAlpha = -(mRandom.nextInt(3) + 1);
			mRotateX = mRandom.nextInt(ROTATE_RANGE);
			mRotateY = mRandom.nextInt(ROTATE_RANGE);
			mTumbleSpdX = -4;
			mTumbleSpdY = -2;
			mSpeedX = -3 + mRandom.nextInt(6);
			mSpeedY = mRandom.nextInt(5);
			mBitmap = flowerImg;
			break;
		case TYPE_FLICKER:
			mTouchBitmaps = touchBitmaps;
			int size = mTouchBitmaps.length;
			for (int i = 0; i < size; i++) {
				int count = mRandom.nextInt(5);
				for (int j = 0; j < count; j++) {
					float startX = mRandom.nextBoolean() ? mX
							+ mRandom.nextInt(10 * i + 50) : mX
							- mRandom.nextInt(10 * i + 50);
					float startY = mRandom.nextBoolean() ? mY
							+ mRandom.nextInt(10 * i + 80) : mY
							- mRandom.nextInt(10 * i + 50);
					if (i < count / 2) {
						startX = mRandom.nextBoolean() ? mX
								+ mRandom.nextInt(40) : mX
								- mRandom.nextInt(40);
						startY = mRandom.nextBoolean() ? mY
								+ mRandom.nextInt(50) : mY
								- mRandom.nextInt(40);
					}

					int alpha = mRandom.nextInt(155);
					Start start = new Start(startX, startY, alpha,
							mTouchBitmaps[i]);
					mStartList.add(start);
				}
			}

			break;
		default:
			break;
		}
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		switch (mAnimateType) {
		case TYPE_FALL:
			if (mBitmap != null && mAlive) {
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
				if (mAlpha != oldAlpha) {
					paint.setAlpha(mAlpha);
				}
				canvas.drawBitmap(mBitmap, 0, 0, paint);
				mMatrix.reset();
				canvas.restore();
				if (mAlpha != oldAlpha) {
					paint.setAlpha(oldAlpha);
				}
			}
			break;
		case TYPE_FLICKER:
			int size = mStartList.size();
			int oldAlpha = paint.getAlpha();
			for (int i = 0; i < size; i++) {
				Start start = mStartList.get(i);
				canvas.save();
				canvas.translate(start.mX, start.mY);
				paint.setAlpha(start.mAlpha);
				canvas.drawBitmap(start.mBitmap, 0, 0, paint);
				canvas.restore();
			}
			paint.setAlpha(oldAlpha);
			break;
		}
	}

	@Override
	public void cleanUp() {
		if (mBitmap != null) {
			mBitmap = null;
		}
		if (mMatrix != null) {
			mMatrix = null;
		}
		super.cleanUp();
	}

	@Override
	public void moving() {
		switch (mAnimateType) {
		case TYPE_FALL:
			mX += mSpeedX;
			mY += mSpeedY;
			mRotateX += mTumbleSpdX;
			mRotateY += mTumbleSpdY;
			mRotateX %= ROTATE_RANGE;
			mRotateY %= ROTATE_RANGE;
			mAlpha += mSpeedAlpha;
			if (mY >= mScreenHeight || mAlpha <= 0 || mX >= mScreenWidth
					|| mX <= 0 || mY <= 0) {
				mAlive = false;
			}
			break;
		case TYPE_FLICKER:
			boolean isDead = true;
			for (int i = 0; i < mStartList.size(); i++) {
				Start start = mStartList.get(i);
				long time = System.currentTimeMillis();
				if (start.mAlpha == 255
						&& time - start.mTime < Start.BRIGHT_LAST_TIME) {
					continue;
				} else {
					if (!start.mIncrease) {
						start.mAlpha -= 14;
						if (start.mAlpha < 0) {
							start.mAlpha = 0;
						}
					} else {
						start.mAlpha += 14;
						if (start.mAlpha >= 255) {
							start.mAlpha = 255;
							start.mTime = System.currentTimeMillis();
							start.mIncrease = false;
						}
					}
					if (start.mAlpha != 0) {
						isDead = false;
					}
				}
			}
			mAlive = !isDead;
			break;
		}

	}

	/**
	 * 
	 * @author maxiaojun
	 * 
	 */
	class Start {
		private static final int BRIGHT_LAST_TIME = 150; // 最亮持续时间
		private float mX;
		private float mY;
		private int mAlpha;
		private long mTime;
		private Bitmap mBitmap;
		private boolean mIncrease;

		public Start(float x, float y, int alpha, Bitmap bitmap) {
			mBitmap = bitmap;
			mX = x;
			mY = y;
			mAlpha = alpha;
			mIncrease = true;
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	private float getRadomLittleScale() {
		return 1.0f + (mRandom.nextInt(10) + 0.01f) / 10;
	}

}
