package com.gau.go.launcherex.theme.cover.ui.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.ui.Rotate;

/**
 * 
 * <br>类描述: 上下拖动物体，物体可以上下弹动
 * <br>功能详细描述:
 *    暂时只供rotate使用，因为有强制转换Rotate的操作
 * @author  maxiaojun
 * @date  [2012-11-23]
 */
public class DragAction extends BaseAction  {
	private Bitmap[] mDayBitmaps;  //太阳电量图片
	private Bitmap[] mNightBitmap; //月亮电量图片
	private int mBatteryLevel;     //电量
	private Rotate mRotate;    
	private int mBodyW;            //下面悬挂物体的宽度
	private int mBodyH;            //下面悬挂物体的高度
	private int mStartY;           //开始Y值
	private boolean mIsDay;        //是否白天
	private Rect mLimit;           //限制拖动区域

	public DragAction(Drivenable drivenable, int actionIndex, int animatingBitmapType,
			boolean isBitmapSymmetric, Bitmap[] actionBitmaps) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		// TODO Auto-generated constructor stub
		mIsDay = true;
		setActionBitmap(actionBitmaps);
		if (mDrivenable instanceof Rotate) {  
			mRotate = (Rotate) mDrivenable;
		}
		if (mRotate.mBodyBitmap != null) {
			mBodyW = mRotate.mBodyBitmap.getWidth();
			mBodyH = mRotate.mBodyBitmap.getHeight();
		}
		mStartY =  0;
		mLimit = mDrivenable.mLimitRect;
		init();
		setDayOrNight();
		regist();
	}
	
	private void setActionBitmap(Bitmap[] actionBitmaps) {
		if (actionBitmaps == null) {
			return;
		}
		if (actionBitmaps.length > 0) {
			int size = actionBitmaps.length / 2;
			mDayBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				mDayBitmaps[i] = actionBitmaps[i];
			}
			
			mNightBitmap = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				mNightBitmap[i] = actionBitmaps[size + i];
			}
		}
	}
	
	private void init() {
		if (DrawUtils.isPort()) {
			mLimit.bottom = mDrivenable.mLimitRect.bottom;
		} else {
			mLimit.bottom = DrawUtils.getScreenViewHeight() - mBodyH;
		}
	}
	@Override
	public void resetData() {
		init();
		super.resetData();
	}
	
	private void setDayOrNight() {
		if (mRotate == null || mDayBitmaps == null || mNightBitmap == null) {
			return;
		}

		setBattery(mBatteryLevel);
	}
	
	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		super.onDraw(camera, matrix, canvas, paint);
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		unRegisterBattery();
	}

	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		if (mMoving) {
			onHarmonic();
			return true;
		}
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		return null;
	}

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
		float currX;
		float currY;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				    resetAngle();
					mMoving = false;
					mTempX = mDrivenable.mX;
					mTempY = mDrivenable.mY;
					mRecordX = x;
					mRecordY = y;
                    break;
			case MotionEvent.ACTION_MOVE :
				currX = mTempX + (x - mRecordX);
				currY = mTempY + (y - mRecordY);
				if (currX < mLimit.left) {
					currX = mLimit.left;
				}
				if (currX > mLimit.right - mBodyW / 2) {
					currX = mLimit.right - mBodyW / 2;
				}
				if (currY < mLimit.top) {
					currY = mDrivenable.mLimitRect.top;
				}
				if (currY > mLimit.bottom - mBodyH / 2) {
					currY = mLimit.bottom - mBodyH / 2;
				}
				resetLocation(currX, currY);
				return true;
			case MotionEvent.ACTION_UP :
				if (!mMoving) {
					 mMoving = true;
					 mIsChange = true;
					 handShake();
					return true;
				}
			case MotionEvent.ACTION_CANCEL :
				break;
			default :
				break;
		}
		return false;
	}
	
	private void resetAngle() {
		mMoving = false;
		mOffset = 0;
	}
	
    private boolean mIsChange; //
	private static final long DURATION = 400; //最高点与最高点间做一次运动的时间
	public float mOffset; // 当前的偏移
	private float mMaxAngle = 0; // 允许的最大振幅         
	private long mDuration; // 一个周期动画的时间             
	private long mLastGravityUpdateTime; //记录上次计算时间
	private float mAngleBeforeShake; // 摇摆前的角度
	private float mAngleAttenuation = 0.5f; //摆动一次角度衰减度 
    private float mTimeAttenuation = 1f; //摆动一次周期时间衰减度 
    private boolean mIsFlyback; //小于一定速度后的回归动画
 	private float mAccele; // 加速度
 	private float mMaxV; // 初始速度
 	/**
 	 * 震动前的准备工作
 	 */
	public void handShake() {
		synchronized (this) {
			if (mMoving) {
				mMaxAngle = -(int) (mDrivenable.mY - mStartY);
				mDuration = DURATION;
				mOffset = mDrivenable.mY;
				final float deffAngel = mMaxAngle - mOffset;
				mAngleBeforeShake = mOffset;
				int t = (int) (mDuration / 2);
				mAccele = (deffAngel + 0.1f) / (t * t);
				mMaxV = mAccele * mDuration * 0.5f; // 最大速度
				mIsFlyback = false;
				mLastGravityUpdateTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * 上下震动
	 */
	private void onHarmonic() {
		synchronized (this) {
			long time = System.currentTimeMillis() - mLastGravityUpdateTime;
			if (mIsFlyback) {        //角度小于一定值，先加速后减速回到起点
				if (time <= mDuration) {
					float angle = 0;
					float half = 0;
					if (time <= mDuration / 2) { // 小于半个周期做加速运动
						angle = mAccele * time * time / 2;
					} else { // 大于半个周期先加速然后减速
						long t = mDuration / 2;
						half = mAccele * t * t / 2;
						time = time - t;
						angle = mMaxV * time - mAccele * time * time / 2;
					}
					mOffset = mAngleBeforeShake + half + angle;
				} else {
					mIsChange = false;
					mMoving = false;
					mOffset = 0;
				}
			} else {
				if (time < mDuration) {
					float angle = 0;
					float half = 0;
					if (time <= mDuration / 2) { // 小于半个周期做加速运动
						angle = mAccele * time * time / 2;
					} else { // 大于半个周期先加速然后减速
						long t = mDuration / 2;
						half = mAccele * t * t / 2;
						time = time - t;
						angle = mMaxV * time - mAccele * time * time / 2;
					}
					mOffset = mAngleBeforeShake + half + angle;
				} else {    // 大于一个周期
					mDuration *= mTimeAttenuation;
					mAngleBeforeShake = mOffset;
					mLastGravityUpdateTime = System.currentTimeMillis();
					if (Math.abs(mMaxAngle) < 10) {  //速度小于一定量后就开始做恢复到起始点的动画
						long t = mDuration / 2;
						mAccele = -mMaxAngle / (t * t);
						mMaxV = mAccele * t;
						mIsFlyback = true;
						return;
					} else {
						mMaxAngle = -mMaxAngle * mAngleAttenuation; // 最大角度反向
						final float deffAngel = mMaxAngle - mOffset;
						long t = mDuration / 2;
						mAccele = (deffAngel + 0.1f) / (t * t);
						mMaxV = mAccele * t;
					}
				}
			}
			
			mDrivenable.mY = mOffset;
			if (mIsChange && mDrivenable.mY <  -(mRotate.mRopeHeight + mBodyH)) {
				mIsDay = mIsDay ^ true;
				setDayOrNight();
				mIsChange = false;
			}
		}
	}
    
	private void resetLocation(float x, float y) {
//		mDrivenable.mX = (int) x;
		mDrivenable.mY = (int) y;
	}
	
	/**
	 * 注册电量广播
	 */
	public void regist() {
		mDrivenable.mContext.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); //获得电量
	}

	/**
	 * 反注册电量广播
	 */
	public void unRegisterBattery() {
		mDrivenable.mContext.unregisterReceiver(mBatInfoReceiver);
	}

	/**
	 * 广播出电量值
	 */
	BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			mBatteryLevel = intent.getIntExtra("level", 0);
			setBattery(mBatteryLevel);
		}
	};
	
	private void setBattery(int batteryLevel) {
		int index = batteryLevel / 10;
		Bitmap[] bitmaps;
		if (mIsDay) {
			bitmaps = mDayBitmaps;
		} else {
			bitmaps = mNightBitmap;
		}
		if (mRotate != null && index > 0 && index < bitmaps.length) {
			mRotate.mBodyBitmap = bitmaps[index];
		}
	}
	
	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		// TODO Auto-generated method stub
        setActionBitmap(actionBitmaps);
        setDayOrNight();
	}

}
