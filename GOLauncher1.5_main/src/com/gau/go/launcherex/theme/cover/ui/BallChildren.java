package com.gau.go.launcherex.theme.cover.ui;

import java.util.List;
import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-11-10]
 */
public class BallChildren extends BaseElement implements IMovable {
	private Random mRandom = new Random();
	private float mSpeedAlpha;

	volatile float mSpeedX;
	volatile float mSpeedY;
	private int mScreenWidth;
	private int mScreenHeight;
	private Matrix mMatrix;

	public static final int MAX_SCALE = 3; //放大倍数，做多不超过MAX_SCALE倍
	public static final int MIN_SCALE = 1; //缩放倍数，做多不超过MIN_SCALE倍

	public static final int MAX_SPEED_UP = 2; //往上最大速度
	public static final int MAX_SPEED_DOWN = 3; //往下最大速度

	public boolean mIsUp;
	private float mStepScale = 0f; //每次计算放大或缩小的幅度
	private int mWait = 100;  //等200次计算后再开始变透明度
	private int mImgWidth;
	private int mImgHeight;

	private int mIndisTinctX; //模糊效果的图片位置
	private int mIndisTinctY;
	
	Paint mPaint = new Paint();
	private List<Bitmap> mBlastImgs;

	public BallChildren(int screenWidth, int screenHeight, float x, float y, Bitmap img, Bitmap indistinct, List<Bitmap> blastImgs, 
			int speedY, boolean isUp) {
		if (img == null) {
			   return;
		}
		
		mMatrix = new Matrix();
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mX = x;
		mY = y;
		mIsUp = isUp;
		mSpeedAlpha = mRandom.nextFloat() + 1.0f;
		mSpeedY = speedY;
		mSpeedX = -2 + mRandom.nextFloat() * 6;

		if (mSpeedX < 0) { //去掉直上直下的运行	
			mSpeedX -= 0.3;
		} else {
			mSpeedX += 0.3;
		}

		mScale = 0.33f; //由于小气球图片需要大图，所以默认要缩小的原来的三分之一
		mBlastImgs = blastImgs;
		
		mBitmap = img;
		mShadow = indistinct;

		mImgWidth = mBitmap.getWidth();
		mImgHeight = mBitmap.getHeight();
		
		int mIndisTinctWidth = 0;
		int mIndisTinctHeight = 0;
		if (mShadow != null) {
			mIndisTinctWidth = mShadow.getWidth();
			mIndisTinctHeight = mShadow.getHeight();
		}
	
		mIndisTinctX = (mImgWidth - mIndisTinctWidth) / 2;
		mIndisTinctY = (mImgHeight - mIndisTinctHeight) / 2;
		
		mAlive = true;
		mAlpha = 255;
		if (isUp) {
			mStepScale = mSpeedY * MIN_SCALE * 1.0f / mScreenHeight;
		} else {
			mStepScale = mSpeedY * MAX_SCALE * 1.0f / mScreenHeight;
		}
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mBlastImgs != null && mBlastImgIndex >= mBlastImgs.size()) {
			mAlive = false;
			return;
		}
		
		if (mIsBlast && mBlastImgs != null) {	
			canvas.save();
			canvas.translate(mX, mY);
			canvas.scale(mScale, mScale);
			canvas.drawBitmap(mBlastImgs.get(mBlastImgIndex), 0, 0, paint);
			mBlastImgIndex ++;
			canvas.restore();	
		} else if (mBitmap != null && mAlive) {
			mMatrix.set(matrix);
			canvas.save();
			canvas.translate(mX, mY);
			canvas.scale(mScale, mScale);
			int oldAlpha = paint.getAlpha();
			if (mAlpha != oldAlpha) {
				paint.setAlpha(mAlpha);
			}
			canvas.drawBitmap(mBitmap, 0, 0, paint);
//			if (mShadow != null) {
//				mPaint.setAlpha(255 - mAlpha);
//				canvas.drawBitmap(mShadow, mIndisTinctX, mIndisTinctY, mPaint);
//			}
			mMatrix.reset();
			canvas.restore();
			if (mAlpha != oldAlpha) {
				paint.setAlpha(oldAlpha);
			}
		}
	}
	
	private boolean mIsTouchIn;
    private boolean mIsBlast;
    private int mBlastImgIndex;
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (isInArea(x, y)) {
					mIsTouchIn = true;
					return true;
				} else {
					mIsTouchIn = false;
				}
			case MotionEvent.ACTION_MOVE :
				return false;
			case MotionEvent.ACTION_UP :
				if (mIsTouchIn && isInArea(x, y)) {
					mIsTouchIn = false;
					// 爆炸
					mIsBlast = true;
					mBlastImgIndex = 0;
					return true;
				}
				break;
			default :
				break;
		}

		return false;
	}

	
	/***
	 * 是否点击在时钟区域内
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isInArea(float x, float y) {
		if (x < mX || x > mX + mImgWidth || y < mY || y > mY + mImgHeight) {
			return false;
		}
		return true;
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
		mX += mSpeedX;
		mY += mSpeedY;
		
		if (mY > mScreenHeight * 0.52) {
			if (mAlpha >= mSpeedAlpha) {
				mAlpha -= mSpeedAlpha;
			}
		}

//		
//		if (mWait > 0) {
//			mWait -= 1;
//		} else {
//			if(mAlpha >= mSpeedAlpha){
//				mAlpha -= mSpeedAlpha;
//			}
//		}
		
		mScale += mStepScale;

		if (mY >= mScreenHeight  || mX >= mScreenWidth || mX <= -mImgWidth || mY <= 0
				|| mScale < 0.05) {
			mAlive = false;
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

}
