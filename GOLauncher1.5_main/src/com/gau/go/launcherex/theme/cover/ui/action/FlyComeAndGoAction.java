package com.gau.go.launcherex.theme.cover.ui.action;

import com.gau.go.launcherex.theme.cover.DrawUtils;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.MotionEvent;
/**
 * 
 * @author maxiaojun
 *
 */
public class FlyComeAndGoAction extends ShakeAction {
	private Bitmap[] mActionBitmaps;
	private Drivenable mDrivenable;
	private static boolean sIsMoving;
	private Point mStartPoint = new Point(); // 起飞的点
	private Point mEndPoint = new Point(); // 结束的点

	private Bitmap[] mUpBitmaps;
	private Bitmap[] mFlyGoBitmaps;

	private Bitmap[] mDownBitmaps;
	private Bitmap[] mFlyBackBitmap;

	private static float sScaleStep;
	private static int sFlySpeedX; // 飞的速度
	private static int sFlySpeedY;

	private long mCurrTime;
	private static final int FRAME_TIME = 100;
	private static final int HIDE_TIME = 3000;   // 猫头鹰从飞走到再次出现间隔时间
	private static final int STATE_NORMAL = -1;  // 静止
	private static int sAnimateState = STATE_NORMAL;    // 当前动画状态
	private static final int STATE_UP = 0;       // 起飞
	private static final int STATE_FLY_GO = 1;   // 飞走
	private static final int STATE_HIDE = 2;     // 从飞走到再次出现的那个状态
	private static final int STATE_FLY_BACK = 3; // 飞回
	private static final int STATE_DOWN = 4;     // 落下

	private int mScreenWidth;
	private int mScreenHeight;
	private int mBitmapIndex;

	public FlyComeAndGoAction(Drivenable drivenable, Bitmap[] actionBitmaps,
			int actionIndex, int animatingBitmapType,
			boolean isBitmapSymmetric, int shakeSpeed) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric,
				shakeSpeed);
		mDrivenable = drivenable;
		setActionBitmaps(actionBitmaps);
		init();
	}
 
	private void init() {
		sIsMoving = false;
		sAnimateState = STATE_NORMAL;
		if (mDownBitmaps != null) {
			mDrivenable.mBitmap =  mDownBitmaps[mDownBitmaps.length - 1];
		}
		mDrivenable.mTouchState = Drivenable.TOUCH_STATE_IN_NORMAL;
		initData();
	}
	
	private void initData() {
		mStartPoint.x = (int) mDrivenable.mX;
		mStartPoint.y = (int) mDrivenable.mY;
		mScreenWidth = DrawUtils.getScreenViewWidth();
		mScreenHeight = DrawUtils.getScreenViewHeight();
		if (mDrivenable.mBitmap != null) {
			mEndPoint.x = mScreenWidth + mDrivenable.mBitmap.getWidth();
		} else {
			mEndPoint.x = mScreenWidth + 100;
		}
		mEndPoint.y = (int) (mScreenHeight * 0.1);
		mDrivenable.mScale = 1.0f;
		sFlySpeedX = mEndPoint.x > mStartPoint.x ? 5 : -5;
		if (mEndPoint.x != mStartPoint.x) {
			sFlySpeedY = sFlySpeedX * (mEndPoint.y - mStartPoint.y)
					/ (mEndPoint.x - mStartPoint.x);
			sScaleStep = (float) Math.abs((1.0 - 0.22f)
					/ ((mEndPoint.x - mStartPoint.x) / sFlySpeedX));
		} else {
			sFlySpeedY = 0;
			sScaleStep = 0;
		}
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			startFly();
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public void handAction() {
		startFly();
	}
	

	private void setActionBitmaps(Bitmap[] actionBitmaps) {
		mActionBitmaps = actionBitmaps;
		if (mActionBitmaps == null || mActionBitmaps.length < 18) {
			return;
		}
		mUpBitmaps = new Bitmap[7]; // 0-6张是从松树上飞起
		for (int i = 0; i < 7; i++) {
			mUpBitmaps[i] = mActionBitmaps[i];
		}
		mFlyGoBitmaps = new Bitmap[3]; // 7-9 飞走
		for (int i = 0; i < 3; i++) {
			mFlyGoBitmaps[i] = mActionBitmaps[7 + i];
		}
		mFlyBackBitmap = new Bitmap[3]; // 10-12 飞回来
		for (int i = 0; i < 3; i++) {
			mFlyBackBitmap[i] = mActionBitmaps[10 + i];
		}
		mDownBitmaps = new Bitmap[5]; // 13-16 落回松树
		for (int i = 0; i < 5; i++) {
			mDownBitmaps[i] = mActionBitmaps[13 + i];
		}
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		if (mActionBitmaps != null) {
			mActionBitmaps = null;
		}
		if (mUpBitmaps != null) {
			mUpBitmaps = null;
		}
		if (mFlyGoBitmaps != null) {
			mFlyGoBitmaps = null;
		}
		if (mDownBitmaps != null) {
			mDownBitmaps = null;
		}
		if (mFlyBackBitmap != null) {
			mFlyBackBitmap = null;
		}
		mEndPoint = null;
		mStartPoint = null;	
	}

	public void resetData() {
		init();
	}

	@Override
	public boolean doAction() {
		if (sIsMoving) { // 开始动画
			long time = System.currentTimeMillis();
			if (time - mCurrTime > FRAME_TIME) {
				switch (sAnimateState) {
				case STATE_UP:
					mCurrTime = System.currentTimeMillis();
					if (mBitmapIndex >= 0 && mBitmapIndex < mUpBitmaps.length) {
						mDrivenable.mBitmap = mUpBitmaps[mBitmapIndex];
					}
					if (mBitmapIndex < mUpBitmaps.length - 1) {
						mBitmapIndex++;
					} else {
						sAnimateState = STATE_FLY_GO;
						mBitmapIndex = 0;
						mDrivenable.mBitmap = mUpBitmaps[mUpBitmaps.length - 1];
					}
					break;
				case STATE_FLY_GO:
					mCurrTime = System.currentTimeMillis();
					if (mBitmapIndex < mFlyGoBitmaps.length - 1) {
						mBitmapIndex++;
					} else {
						mBitmapIndex = 0;
					}
					mDrivenable.mBitmap = mFlyGoBitmaps[mBitmapIndex];
					break;
				case STATE_HIDE:
					if (time - mCurrTime > HIDE_TIME) { // 开始往回飞
						mCurrTime = System.currentTimeMillis();
						mBitmapIndex = 0;
						sAnimateState = STATE_FLY_BACK;
						sFlySpeedX = -sFlySpeedX;
						if (sFlySpeedX != 0 && mStartPoint.x != mEndPoint.x) {
							sFlySpeedY = sFlySpeedX * (mStartPoint.y - mEndPoint.y)
									/ (mStartPoint.x - mEndPoint.x);
							sScaleStep = (float) Math.abs((1.0 - 0.2)
									/ ((mEndPoint.x - mStartPoint.x) / sFlySpeedX));
						} else {
							//出错了
							init();
						}
					}
					break;
				case STATE_FLY_BACK:
					mCurrTime = System.currentTimeMillis();
					if (mBitmapIndex < mFlyBackBitmap.length - 1) {
						mBitmapIndex++;
					} else {
						mBitmapIndex = 0;
					}

					mDrivenable.mBitmap = mFlyBackBitmap[mBitmapIndex];
					break;
				case STATE_DOWN:
					mCurrTime = System.currentTimeMillis();
					if (mBitmapIndex >= 0 && mBitmapIndex < mDownBitmaps.length) {
						mDrivenable.mBitmap = mDownBitmaps[mBitmapIndex];
					}

					if (mBitmapIndex < mDownBitmaps.length - 1) {
						mBitmapIndex++;
					} else {
						sAnimateState = STATE_NORMAL;
						mDrivenable.mTouchState = Drivenable.TOUCH_STATE_IN_NORMAL;
						mBitmapIndex = 0;
						mDrivenable.mScale = 1.0f;
						sIsMoving = false;
					}
					break;
				default:
					break;
				}
			}
			switch (sAnimateState) {
			case STATE_FLY_GO:
				if (Math.abs(mDrivenable.mX - mEndPoint.x) <= Math
						.abs(sFlySpeedX)) {
					mEndPoint.x = (int) mDrivenable.mX;
					mEndPoint.y = (int) mDrivenable.mY;
					sAnimateState = STATE_HIDE;
					mBitmapIndex = 0;
					break;
				}
				mDrivenable.mX += sFlySpeedX;
				mDrivenable.mY += sFlySpeedY;
				mDrivenable.mScale -= sScaleStep;
				if (mDrivenable.mScale < 0) {
					mDrivenable.mScale = 0;
				}
				break;
			case STATE_FLY_BACK:
				if (Math.abs(mDrivenable.mX - mStartPoint.x) <= Math
						.abs(sFlySpeedX)) {
					mDrivenable.mX = mStartPoint.x;
					mDrivenable.mY = mStartPoint.y;
					sAnimateState = STATE_DOWN;
					mBitmapIndex = 0;
					break;
				}
				mDrivenable.mX += sFlySpeedX;
				mDrivenable.mY += sFlySpeedY;
				mDrivenable.mScale += sScaleStep;

				if (mDrivenable.mScale > 1.0f) {
					mDrivenable.mScale = 1.0f;
				}
				break;
			default:
				break;
			}
			return true;
		}
		return false;
	}
 
	private void startFly() {
		if (sAnimateState == STATE_NORMAL && !sIsMoving) {
			sIsMoving = true;
			sAnimateState = STATE_UP;
			initData();
		}
	}
	
	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		setActionBitmaps(actionBitmaps);
	}

	@Override
	public void handShake(float speed) {
	}

}
