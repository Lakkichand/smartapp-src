package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.Random;
import com.gau.go.launcherex.theme.cover.ui.Rotate;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述: 飞机喷射出的尾气
 * <br>功能详细描述: 放大缩小
 * 
 * @author  maxiaojun
 * @date  [2012-11-23]
 */
public class JetAction extends BaseAction {
	private Random mRandom = new Random();
	private Rotate mRotate;
	private Bitmap mSprayCloudBitmap;
	private Rect mSprayRect;
	private Paint mPaint;
	private int mCloudW;
	private int mCloudH;
	private int mCloudSize;
	public JetAction(Drivenable drivenable, int actionIndex, int animatingBitmapType,
			boolean isBitmapSymmetric, Bitmap[] actionBitmaps) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		if (drivenable instanceof Rotate) {
			mRotate = (Rotate) drivenable;
		}
		if (actionBitmaps != null && actionBitmaps.length > 0) {
			mSprayCloudBitmap = actionBitmaps[0];
		}
		mSprayRect = new Rect();
		mPaint = new Paint();
		if (mRotate != null) {
			mCloudSize = mRotate.mBodyBitmap.getWidth() > mRotate.mBodyBitmap.getHeight()
					? mRotate.mBodyBitmap.getWidth()
					: mRotate.mBodyBitmap.getHeight();
		}
		mCloudW = mSprayCloudBitmap.getWidth();
		mCloudH = mSprayCloudBitmap.getHeight();
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}
	int mTempLeft = 0;
	int mTempTop = 0;
	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mIsSpray) {
			if (mSprayCloudBitmap != null && mRotate != null) {
				for (int i = 0; i < SPRAY_NUM; i++) {
					canvas.save();
					canvas.translate(mDrivenable.mX + mDirection[i][2] * mCloudSize
							* mDirection[i][0] / MAX_SPRAY_SPEED + mXY[i][0], mDrivenable.mY
							+ mDirection[i][3] * mCloudSize * mDirection[i][1] / MAX_SPRAY_SPEED
							+ mXY[i][1]);
					canvas.scale(mScale[i], mScale[i]);
					canvas.rotate(mRotate.mAngle, mRotate.mPivoX, mRotate.mPivoY);
					mTempLeft = (int) (mCloudW * (1 - mScale[i]) / 2);
					mTempTop = (int) (mCloudH * (1 - mScale[i]) / 2);
					mSprayRect.set(mTempLeft, mTempTop, mCloudW - mTempLeft, mCloudH - mTempTop);
					mPaint.setAlpha(mAlpha[i]);
					canvas.drawBitmap(mSprayCloudBitmap, null, mSprayRect, mPaint);
					canvas.restore();
				}
			}
		}

		super.onDraw(camera, matrix, canvas, paint);
	}

	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		if (mIsShake) {
			if (mScaleIndex >= 0 && mScaleIndex < mScaleArray.length) {
				if (mFrame > FRAME_NUM) {
					mFrame = 0;
					mScaleIndex++;
				} else {
					if (mScaleIndex == 0) {
						mChange = (mScaleArray[mScaleIndex] - 1) / FRAME_NUM;
					} else {
						mChange = (mScaleArray[mScaleIndex] - mScaleArray[mScaleIndex - 1])
								/ FRAME_NUM;
					}
					mFrame++;
				}
				if (mScaleIndex == 0) {
					mDrivenable.mScale = 1.0f + mFrame * mChange;
				} else {
					mDrivenable.mScale = mScaleArray[mScaleIndex - 1] + mFrame * mChange;
				}
			} else {
				mIsShake = false;
			}
		}

		if (mIsSpray) {
			if (mRotate != null && mSprayStep <= MAX_SPRAY_DISTANCE / MAX_SPRAY_SPEED) {
				for (int i = 0; i < SPRAY_NUM; i++) {
					mScale[i] = 1 - mSprayScaleStep[i] * mSprayStep;
					mAlpha[i] = 255 - mSprayAlphaStep[i] * mSprayStep;
					mXY[i][0] = mDirection[i][2] * mDirection[i][0] * mSprayStep;
					mXY[i][1] = mDirection[i][3] * mDirection[i][1] * mSprayStep;
				}
				mSprayStep++;
			} else {
				mIsSpray = false;
			}
		}
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean mIsShake; //是否抖动的标识
	private int mFrame; //帧索引
	private float mChange; //每次变化，每帧的变动
	private int mScaleIndex;  //缩放变化数组的索引
	private float[] mScaleArray; //创建一份缩放变化的数据
	private static final int SCALE_ARRAY_SIZE = 7; //抖动时变化大小的次数
	private static final int FRAME_NUM = 3; //每次变化用的帧数

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_UP :
				prepareShake();
				prepareSpray();
				mIsShake = true;
				mFrame = 0;
				mScaleIndex = 0;

				mIsSpray = true;
				mSprayStep = 0;
				break;
		}
		return false;
	}

	/***
	 * <br>功能简述: 抖动前准备
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void prepareShake() {
		mScaleArray = new float[SCALE_ARRAY_SIZE];
		for (int i = 0; i < SCALE_ARRAY_SIZE - 1; i++) {
			if (i % 2 == 0 || i == 0) {
				mScaleArray[i] = mRandom.nextFloat() * 0.05f + 1.1f;
			} else {
				mScaleArray[i] = mRandom.nextFloat() * 0.05f + 0.8f;
			}
		}
		mScaleArray[SCALE_ARRAY_SIZE - 1] = 1.0f;
	}

	//喷出几个小朵云来
	private boolean mIsSpray;
	private int mAlpha[];
	private float mScale[];
	private float mXY[][];
	private float[][] mDirection; //方向数组
	private float[] mSprayScaleStep; //方向数组
	private int[] mSprayAlphaStep; //方向数组
	private static final int SPRAY_NUM = 3;   //喷出去小云的数量
	private static final float MAX_SPRAY_SPEED = 5f; //喷出去小云x方向或y方向的最大速度
	private static final float MAX_SPRAY_DISTANCE = 80; //喷出去小云x方向或y方向的最大移动距离
	private int mSprayStep;
	/**
	 * <br>功能简述:喷出小云前准备
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void prepareSpray() {
		mScale = new float[SPRAY_NUM];
		mAlpha = new int[SPRAY_NUM];
		mXY = new float[SPRAY_NUM][2];
		mDirection = new float[SPRAY_NUM][4];
		mSprayScaleStep = new float[SPRAY_NUM];
		mSprayAlphaStep = new int[SPRAY_NUM];
		float tempY;
		float tempX;
		for (int i = 0; i < SPRAY_NUM; i++) {
			tempY = mRandom.nextFloat() + 0.1f;
			tempX = mRandom.nextFloat() + 0.1f;
			mDirection[i][2] = mRandom.nextBoolean() ? 1 : -1;  //方向 
//			mDirection[i][3] = mRandom.nextBoolean() ? 1 : -1;  //方向 
//			mDirection[i][2] = 1;
			mDirection[i][3] = 1;
			if (tempY > tempX) {
				mDirection[i][0] = MAX_SPRAY_SPEED / tempY * tempX;
				mDirection[i][1] = MAX_SPRAY_SPEED;
			} else {
				mDirection[i][0] = MAX_SPRAY_SPEED;
				mDirection[i][1] = MAX_SPRAY_SPEED / tempX * tempY;
			}
			mSprayScaleStep[i] = (mRandom.nextFloat() / 2 + 0.5f)  * MAX_SPRAY_SPEED / MAX_SPRAY_DISTANCE;
			mSprayAlphaStep[i] = (int) (255 * (mRandom.nextFloat() / 2 + 0.5f) * MAX_SPRAY_SPEED / MAX_SPRAY_DISTANCE);
		}

	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		// TODO Auto-generated method stub
		if (actionBitmaps != null && actionBitmaps.length > 0) {
			mSprayCloudBitmap = actionBitmaps[0];
		}
	}

}
