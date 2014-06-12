package com.gau.go.launcherex.theme.cover.ui.action;

import com.gau.go.launcherex.theme.cover.DrawUtils;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * 总是靠在屏幕的边缘，点击后弹出设置界面。（供控制器使用）
 * 
 * @author maxiaojun
 * 
 */
public class KeepSideAction extends BaseAction {
	private Bitmap[] mActionBitmaps;
	private Point mStartPoint; // 初始位置
	private Bitmap mImgDefault;
	private Bitmap mImgMove;
	private static final int SPEED = 200;
	private float mCurrX;
	private int mScreenWidth;
	private int mScreenHeight;

	public KeepSideAction(Drivenable drivenable, Bitmap[] actionBitmaps,
			int actionIndex, int animatingBitmapType, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		setActionBitmap(actionBitmaps);
		init();
	}

	private void setActionBitmap(Bitmap[] bitmap) {
		mActionBitmaps = bitmap;
		if (mActionBitmaps != null && mActionBitmaps.length >= 2) {
			mImgDefault = mActionBitmaps[0];
			mImgMove = mActionBitmaps[1];
		} else {
			mImgDefault = mDrivenable.mBitmap;
			mImgMove = mDrivenable.mBitmap;
		}
		mDrivenable.mBitmap = mImgDefault;
	}

	private void init() {
		mScreenWidth = DrawUtils.getScreenViewWidth();
		mScreenHeight = DrawUtils.getScreenViewHeight();
		mStartPoint = new Point();
		mStartPoint.x = 0;
		mStartPoint.y = (int) (mScreenHeight * 0.3f);
		mDrivenable.mX = mStartPoint.x;
		mDrivenable.mY = mStartPoint.y;
	}

	@Override
	public void resetData() {
		init();
		super.resetData();
	}

	private boolean mIsClick = false;
	private static final int JUDGE_CLICK = 5;
	private float mRecordX;
	private float mRecordY;
	private float mTempX;
	private float mTempY;
	private boolean mMoving = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		float currX = 0;
		float currY = 0;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mDrivenable.mBitmap = mImgMove;
			mMoving = false;
			mTempX = mDrivenable.mX;
			mTempY = mDrivenable.mY;
			mRecordX = x;
			mRecordY = y;
			mIsClick = true;
			break;
		case MotionEvent.ACTION_MOVE:
			currX = mTempX + (x - mRecordX);
			currY = mTempY + (y - mRecordY);
			if (Math.abs(x - mRecordX) > JUDGE_CLICK
					|| Math.abs(y - mRecordY) > JUDGE_CLICK) {
				mIsClick = false;
			}
			if (currX < mDrivenable.mLimitRect.left) {
				currX = mDrivenable.mLimitRect.left;
			}
			if (currX > mDrivenable.mLimitRect.right) {
				currX = mDrivenable.mLimitRect.right;
			}
			if (currY < mDrivenable.mLimitRect.top) {
				currY = mDrivenable.mLimitRect.top;
			}
			if (currY > mDrivenable.mLimitRect.bottom) {
				currY = mDrivenable.mLimitRect.bottom;
			}
			resetLocation(currX, currY);
			return true;
		case MotionEvent.ACTION_UP:
			mDrivenable.mBitmap = mImgDefault;
			if (mIsClick) {
				doClick();
			}

			mIsClick = false;
			if (!mMoving) {
				mCurrX = mDrivenable.mX;
				mMoving = true;
				return true;
			}
		case MotionEvent.ACTION_CANCEL:
			mMoving = false;
			mIsClick = false;
			mDrivenable.mBitmap = mImgDefault;
			break;
		default:
			break;
		}
		return false;
	}

	private void resetLocation(float x, float y) {
		mDrivenable.mX = (int) x;
		mDrivenable.mY = (int) y;
	}

	private void doClick() {
		Toast.makeText(mDrivenable.mContext, "弹出设置界面！", 1000).show();
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		mStartPoint = null;
	}

	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		if (mMoving) {
			if (mCurrX > mScreenWidth / 2) {
				if (mDrivenable.mX < mScreenWidth - SPEED) {
					mDrivenable.mX += SPEED;
				} else {
					mDrivenable.mX = mScreenWidth;
					mMoving = false;
				}
			} else {
				if (mDrivenable.mX > SPEED) {
					mDrivenable.mX -= SPEED;
				} else {
					mDrivenable.mX = 0;
					mMoving = false;
				}
			}
		}
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		if (mActionIndex >= mActionBitmaps.length) {
			mActionIndex = 0;
		}
		return mActionBitmaps[mActionIndex++];
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
