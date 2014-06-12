package com.gau.go.launcherex.theme.cover.ui.action;

import com.gau.go.launcherex.theme.cover.DrawUtils;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * 
 * <br>类描述: 做正弦曲线运动
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-11-24]
 */
public class SineAction extends BaseAction {
	
	private static final int SPEED_X = 4;
	private Bitmap[] mActionBitmaps;
	private long mNextShowTime; //下次精灵出现的时间间隔
	private long mTime;
	private Point mStartPoint; //初始位置
	public SineAction(Drivenable drivenable, Bitmap[] actionBitmaps, int actionIndex, int animatingBitmapType,
			boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mActionBitmaps = actionBitmaps;
		init();
	}
	
	private int mScreenWidth;
//	private int mScreenHeight;
	private float mPeak; //正弦的峰值
	private float mSpan; //正弦一个周期的跨度
	private void init() {
		mScreenWidth = DrawUtils.getScreenViewWidth();
//		mScreenHeight = DrawUtils.getScreenViewHeight();
		mStartPoint = new Point();
		mSpan = mScreenWidth * 0.3f;
		mPeak = mSpan / 6;

		mNextShowTime = 1000 * 10;
		mDrivenable.mSpeedX = SPEED_X;
		
		mStartPoint.x = (int) mDrivenable.mX;
		mStartPoint.y = (int) mDrivenable.mY;

	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		mStartPoint = null;
	}

	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		if (!mActive) {
			long currTime = System.currentTimeMillis();
			if (currTime - mTime < mNextShowTime) {
				return false;
			}
			mActive = true;
			mDrivenable.mIsBaseDraw = true;
		} else {
			mDrivenable.mX += mDrivenable.mSpeedX;
			mDrivenable.mY = mStartPoint.y + mPeak
					* (float) Math.sin((mDrivenable.mX % mSpan) / mSpan * 2 * Math.PI);

			if (mDrivenable.mX > mScreenWidth + mDrivenable.mBitmap.getWidth() / 2) {
				mActive = false;
				mDrivenable.mIsBaseDraw = false;
				mTime = System.currentTimeMillis();
				mDrivenable.mX = mStartPoint.x;
				mDrivenable.mY = mStartPoint.y;
			}
		}
		return false;
	}
	
	@Override
	public Bitmap getBimap(int deccelerate) {
		if (mActionIndex >= mActionBitmaps.length) {
			mActionIndex = 0;
		}
		return mActionBitmaps[mActionIndex++];
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		mActionBitmaps = actionBitmaps;
	}

}
