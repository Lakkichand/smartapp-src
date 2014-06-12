package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.Random;

import android.graphics.Bitmap;

/**
 * 
 * @author maxiaojun
 *
 * actionBitmaps 六张图片，前三张眨眼用，后三张震动翅膀用
 */ 
public class SlightMovementAction  extends BaseAction {
	public Bitmap[] mActionBitmaps;
	public Bitmap[] mWinkBitmaps;
	public Bitmap[] mWingsBitmaps;
	private long mCurrTime;
	private static final long INTERVAL = 2000; //每隔两秒判断一次
	private static final int ANIMATE_TIME_INTERVAL = 100;
	private static final int ACTION_TYPE_WINK = 0;  //眨眼
	private static final int ACTION_TYPE_WINGS = 1; //振翅
	private static final int RANGE = 4;
	private int mActionType = -1; //动作类型（眨眼，振翅）
	private Random mRandom = new Random();
	private int mBitmapIndex;

	public SlightMovementAction(Drivenable drivenable, Bitmap[] actionBitmaps, int actionIndex,
			int animatingBitmapType, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		// TODO Auto-generated constructor stub
		setActionBitmap(actionBitmaps);
	}

	private void setActionBitmap(Bitmap[] actionBitmaps) {
		mActionBitmaps = actionBitmaps;
		if (mActionBitmaps == null) {
			return;
		}
		mWinkBitmaps = new Bitmap[3];
		mWingsBitmaps = new Bitmap[6];
		if (mActionBitmaps.length > 2) {
			for (int i = 0; i < 3; i++) {
				mWinkBitmaps[i] = mActionBitmaps[i];
			}
		}
		if (mActionBitmaps.length > 8) {
			for (int i = 0; i < 6; i++) {
				mWingsBitmaps[i] = mActionBitmaps[i + 3];
			}
		}
		mDrivenable.mBitmap = mActionBitmaps[0];

	}
	
	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		mActionBitmaps = null;
	}

	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		long currTime = System.currentTimeMillis();
		if (currTime - mCurrTime > INTERVAL) {
			mActionType = mRandom.nextInt(RANGE);
			mCurrTime = System.currentTimeMillis();
		}

		if (mActionType == ACTION_TYPE_WINK) {
			doAnimate(currTime, mWinkBitmaps);
		} else if (mActionType == ACTION_TYPE_WINGS) {
			doAnimate(currTime, mWingsBitmaps);
		}
		
		return false;
	}
	

	private void doAnimate(long time, Bitmap[] bitmaps) {
		if (time - mCurrTime > ANIMATE_TIME_INTERVAL) {
			mBitmapIndex++;
			if (mBitmapIndex >= bitmaps.length) {
				mBitmapIndex = 0;
				mActionType = -1;
			}
			mDrivenable.mBitmap = bitmaps[mBitmapIndex];
			mCurrTime = System.currentTimeMillis();
		}
	}
	
	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		// TODO Auto-generated method stub
		setActionBitmap(actionBitmaps);
	}

}
