package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.ArrayList;
import java.util.Random;

import com.gau.go.launcherex.theme.cover.DrawUtils;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述: 抖动一下然后飞走
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-11-24]
 */
public class ShakeLeaveAction extends BaseAction {
	private static final int PLANE_SPEED_X = 40; //飞机速度
	private static final int GAS_SPEED_X = 8; //尾气速度
	private static final int WAIT = 1; //没震动一次经过几个计算周期，影响飞机上下震动的频率
	private Bitmap[] mActionBitmaps;
	private int mScreenWidth;
	private long mTime;
	private Point mStartPoint; //初始位置
	private Random mRandom = new Random();
	public ShakeLeaveAction(Drivenable drivenable, int actionIndex, int animatingBitmapType,
			boolean isBitmapSymmetric, Bitmap[] actionBitmaps) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mActionBitmaps = actionBitmaps;
		initState(mDrivenable.mLimitRect.left, mDrivenable.mLimitRect.top);
	}

	private int mShakeTme;
	private int mSprayOneTime;
	private ArrayList<OneGas> mGasList;
	private void initState(float x, float y) {
		mScreenWidth = DrawUtils.getScreenViewWidth();
		mStartPoint = new Point();
		mStartPoint.x = (int) mDrivenable.mX;
		mStartPoint.y = (int) mDrivenable.mY;
		mShakeTme = 700;
		mSprayOneTime = 150;
		mActive = true;
		mTime = System.currentTimeMillis();
		mGasList = new ArrayList<OneGas>(5);
		mIndex = -1;
	}

	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mGasList != null) {
			for (int i = 0; i < mGasList.size(); i++) {
				OneGas gas = mGasList.get(i);
				canvas.save();
				canvas.translate(gas.mX, gas.mY);
				paint.setAlpha(gas.mAlpha);
				canvas.drawBitmap(gas.mBitmap, 0, 0, paint);
				paint.setAlpha(255);
				canvas.restore();
			}
		}
		super.onDraw(camera, matrix, canvas, paint);
	}

	@Override
	public void cleanUp() {
		for (int i = 0; i < mActionBitmaps.length; i++) {
			mActionBitmaps[i] = null;
		}
		mActionBitmaps = null;
	}

	private int mIsUp = 1;
	private int mTemp1 = 0;
	private int mIndex = 0;
	@Override
	public boolean doAction() {
		if (mActive) {
			long spaceTime = System.currentTimeMillis() - mTime;
			if (mGasList != null && mGasList.size() < mActionBitmaps.length) {
				int currIndex = (int) (spaceTime / mSprayOneTime);
				if (currIndex != mIndex) {
					mIndex = currIndex;
					if (mIndex < mActionBitmaps.length - 1) {
						int dis = mRandom.nextInt(30);
						int y = mRandom.nextBoolean() ? dis : -dis;
						OneGas gas = new OneGas(mActionBitmaps[mIndex], mStartPoint.x
								- mDrivenable.mBitmap.getWidth() / 2, y + mStartPoint.y
								- mDrivenable.mBitmap.getHeight() / 2);
						mGasList.add(mIndex, gas);
					}
				}
			}
			if (mGasList != null) {
				for (int i = 0; i < mGasList.size(); i++) {
					OneGas gas = mGasList.get(i);
					if (gas != null) {
						gas.mX -= GAS_SPEED_X;
						if (gas.mAlpha >= 10) {
							gas.mAlpha -= 10;
						}
					}
				}
			}
			if (spaceTime < mShakeTme) {
				if (mTemp1 > WAIT) {
					mTemp1 = 0;
					if (mIsUp > 1) {
						mIsUp = -1;
					} else {
						mIsUp++;
					}
					mIsUp *= -1;
					mDrivenable.mY = mStartPoint.y + mIsUp * 3;
				} else {
					mTemp1++;
				}
			} else {
				if (mDrivenable.mX < mScreenWidth + mDrivenable.mBitmap.getWidth() / 2) {
					mDrivenable.mX += PLANE_SPEED_X;
				} else {
					mActive = false;
					mDrivenable.mTouchState = Drivenable.TOUCH_STATE_IN_NORMAL;
				}
			}
		}
		return false;
	}
    /**
     * 
     * <br>类描述: 
     * <br>功能详细描述:
     * 
     * @author  maxiaojun
     * @date  [2012-11-24]
     */
	class OneGas {
		public OneGas(Bitmap bitmap, int x, int y) {
			mBitmap = bitmap;
			mX = x;
			mY = y;
		}
		Bitmap mBitmap;
		int mX;
		int mY;
		int mStartTime = 0;
		int mAlpha = 255;
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				initState(x, y);
				break;
			default :
				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		// TODO Auto-generated method stub
          if (actionBitmaps != null) {
        	  mActionBitmaps = actionBitmaps;
          }
	}
}
