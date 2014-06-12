package com.gau.go.launcherex.theme.cover.ui;

import java.util.TimeZone;

import com.gau.go.launcherex.theme.cover.ViewControl;

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
import android.os.Handler;
import android.text.format.Time;
import android.view.MotionEvent;

/**
 * 
 * <br>类描述: 时钟精灵
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-10-20]
 */
//CHECKSTYLE:OFF
public class ClockSpirit implements OnResponTouchListener, ICleanable, IMovable {
	public Bitmap mDail; //表盘
	public float mDailAxisX; //表盘轴心在屏幕的位置
	public float mDailAxisY;

	public Bitmap mHourhand; //时针
	public float mHourPerX; //时针轴心相对时针图片的位置
	public float mHourPerY;

	public Bitmap mMinutehand; //分针
	public float mMinutePerX; //分针轴心相对分针图片的位置
	public float mMinutePerY;

	public Bitmap mSecondhand; //秒针
	public float mSecondPerX; //秒针轴心相对分针图片的位置
	public float mSecondPerY;

	public Bitmap mClockScrew; //时钟螺丝
	private int mScrewWidth; //时钟螺丝宽度
	private int mScrewHeight; //时钟螺丝高度

	public float mStartPerX; //时钟开始位置（相对表盘图片的比例值）
	public float mStartPerY;
	public float mStartLandPerY; //横屏情况
	public float mStartLandPerX;

	public boolean mLimit; //是否有限制拖动范围
	public float mLimitLeftPer; //限制可拖动范围
	public float mLimitRightPer;
	public float mLimitTopPer;
	public float mLimitBottomPer;

	private int mBgWidth; //背景图片宽度
	private int mBgHeight;
	private int mDialWidth; // 表盘宽度
	private int mDialHeight; // 表盘高度

	private int mHourhandWidth; //时针宽度
	private int mHourHandHeight; //时针高度
	private int mMinuteHandWidth; //分针宽度
	private int mMinuteHandHeight; //分针高度
	private int mSecondHandWidth; //秒针宽度
	private int mSecondHandHeight; //秒针高度

	private float mCurrY; //组件Y坐标
	private float mCurrX; //组件X坐标

	private int mHourAxisX; //时针轴心相对时针图片的位置
	private int mHourAxisY;
	private int mMinuteAxisX; //分针轴心相对分针图片的位置
	private int mMinuteAxisY;
	private int mSecondAxisX; //秒针轴心相对分针图片的位置
	private int mSecondAxisY;

	private float mHour; //当前时间：时
	private float mMinutes; //当前时间：分
	private float mSecond;
	private long mTime;

	private final Handler mHandler = new Handler(); // 定一个Handler类实现更新时间
	private int mScreenWidth;
	private int mScreenHeight;

	public boolean mAllowDrag; //是否允许拖动
	public int mLimitLeft; //限制拖动范围
	public int mLimitRight;
	public int mLimitTop;
	public int mLimitBottom;

	private Time mCalendar;
	private Context mContext;
	private Rect mRect;

	private int mStartSecond; //记录第一次开始时的秒数
	private int mStartX; //时钟开始位置
	private int mStartY;

	private int mDialCenterX; //钟表指针的中心坐标
	private int mDialCenterY;

	private int mEdgeDis;
	private float mHourRotate;
	private float mMinutesRotate;
	private static final float ROTATE =  360.0f/60.0f;
	private static final float DIAL_SCALE_H = 0.317f; //时钟在背景图片上占得高度
	private static final float ALLOW_TOUCH_EDGE = 0.2f; //限制点击响应的范围，与四个边的距离（四周不可点击）
	private static final float BG_REALLY_H = 536; //背景图片真实高度
	private static final int DAMP = 4000; //损耗
	private static final int RATTAN_K = 80; //弹簧劲度系数
	private static final int MIN_SPEED = 60; //最小速度
	private static final int MIN_OFFSET = 10; //最小位移

	private ViewControl mViewControl;

	public ClockSpirit(int screenWidth, int screenHeight, ViewControl viewControl) {
		mViewControl = viewControl;
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
	}

	/**
	 * <br>功能简述:设置屏幕宽高
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param screenWidth
	 * @param screenHeight
	 */
	public void setScreenSize(int screenWidth, int screenHeight) {
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
	}

	/***
	 * <br>功能简述: 初始化坐标信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mRect = new Rect();
		if (mDail != null) {
			mBgWidth = mDail.getWidth();
			mBgHeight = mDail.getHeight();
		}

		mDialWidth = mBgWidth;
		mDialHeight = (int) (mBgHeight * DIAL_SCALE_H);
		mEdgeDis = (int) (mBgWidth * ALLOW_TOUCH_EDGE);

		if (mHourhand != null) {
			mHourhandWidth = mHourhand.getWidth();
			mHourHandHeight = mHourhand.getHeight();
		}
		if (mMinutehand != null) {
			mMinuteHandWidth = mMinutehand.getWidth();
			mMinuteHandHeight = mMinutehand.getHeight();
		}
		if (mSecondhand != null) {
			mSecondHandWidth = mSecondhand.getWidth();
			mSecondHandHeight = mSecondhand.getHeight();
		}
		if (mClockScrew != null) {
			mScrewWidth = mClockScrew.getWidth();
			mScrewHeight = mClockScrew.getHeight();
		}
		mHourAxisX = (int) (mHourhandWidth * mHourPerX);
		mHourAxisY = (int) (mHourHandHeight * mHourPerY);

		mMinuteAxisX = (int) (mMinuteHandWidth * mMinutePerX);
		mMinuteAxisY = (int) (mMinuteHandHeight * mMinutePerY);

		mSecondAxisX = (int) (mSecondHandWidth * mSecondPerX);
		mSecondAxisY = (int) (mSecondHandHeight * mSecondPerY);

		mStartX = (int) (mStartPerX * mScreenWidth);
		mStartY = (int) (mStartPerY * mScreenHeight);

		mCurrX = mStartX;
		mCurrY = mStartY;

		if (mLimit) {
			mLimitLeft = (int) (mScreenWidth * mLimitLeftPer);
			mLimitRight = (int) (mScreenWidth * mLimitRightPer - mBgWidth);
			mLimitTop = (int) (mScreenHeight * mLimitTopPer - mDailAxisY * mBgHeight);
			mLimitBottom = (int) (mScreenHeight * mLimitBottomPer - mDailAxisY * mBgHeight);
			mLimitBottom = Math.min(mLimitBottom, 0);
		} else {
			mLimitLeft = 0;
			mLimitRight = mScreenWidth - mBgWidth;
			mLimitTop = (int) (-mDailAxisY * mBgHeight);
			mLimitBottom = (int) (mScreenHeight - mDailAxisY * mBgHeight);
		}

		initArgument();
		registerTime();
	}

	/**
	 * 重新设置数据
	 */
	public void resetData(int screenWidth, int screenHeight, boolean backToDefault) {
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mStartX = (int) (mStartPerX * mScreenWidth);
		if (mScreenWidth > mScreenHeight) {
			mStartY = (int) (mStartLandPerY * mScreenHeight);
		} else {
			mStartY = (int) (mStartPerY * mScreenHeight);
		}

		if (backToDefault) {
			mCurrX = mStartX;
			mCurrY = mStartY;
		}

		if (mLimit) {
			mLimitLeft = (int) (mScreenWidth * mLimitLeftPer);
			mLimitRight = (int) (mScreenWidth * mLimitRightPer - mBgWidth);
			if (mScreenWidth > mScreenHeight) {
				mLimitTop = (int) (mScreenHeight * mLimitTopPer - mDailAxisY * mBgHeight);
				mLimitBottom = 0;
			} else {
				mLimitTop = (int) (mScreenHeight * mLimitTopPer - mDailAxisY * mBgHeight);
				mLimitBottom = (int) (mScreenHeight * mLimitBottomPer - mDailAxisY * mBgHeight);
				mLimitBottom = Math.min(mLimitBottom, 0);
			}
		} else {
			mLimitLeft = 0;
			mLimitRight = mScreenWidth - mBgWidth;
			mLimitTop = (int) (-mDailAxisY * mBgHeight);
			mLimitBottom = (int) (mScreenHeight - mDailAxisY * mBgHeight);
		}
	}

	/***
	 * <br>功能简述:注册定时刷新时钟时间
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void registerTime() {
		// 注册一个消息过滤器，获取时间改变、时区改变的action
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
		if (mCalendar == null) {
			mCalendar = new Time(); // 获取当前系统时间
		}
		onTimeChanged();
	}

	/**
	 * <br>功能简述: 反注册消息过滤器
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void unRegisterTime() {
		mContext.unregisterReceiver(mIntentReceiver);
	}

	/**
	 * <br>功能简述: 获取时间改变，计算当前的时分秒
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void onTimeChanged() {
		mCalendar.setToNow();
		int hour = mCalendar.hour;
		int minute = mCalendar.minute;
		int second = mCalendar.second;
		mMinutes = minute + second / 60.0f;
		mHour = hour + mMinutes / 60.0f;

		mHourRotate = mHour / 12.0f * 360.0f;
		mMinutesRotate = mMinutes / 60.0f * 360.0f;

		mStartSecond = second;;
		mTime = System.currentTimeMillis();
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		// 监听获取时间改变action
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				String tz = intent.getStringExtra("time-zone");
				mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
			}
			onTimeChanged(); // 获取新的时间
		}
	};

	/**
	 * <br>功能简述: 绘制
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param camera
	 * @param matrix
	 * @param canvas
	 * @param paint
	 */
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		//画表盘
		if (mRect == null) {
			mRect = new Rect();
		}
		if (mDail != null) {
			canvas.save();
			mRect.set((int) mCurrX, (int) mCurrY, (int) mCurrX + mBgWidth, (int) mCurrY + mBgHeight);
			canvas.drawBitmap(mDail, null, mRect, paint);
			canvas.restore();
		}
		mDialCenterX = (int) (mCurrX + mDailAxisX * mBgWidth);
		mDialCenterY = (int) (mCurrY + mDailAxisY * mBgHeight);
		//画时针
		if (mHourhand != null) {
			canvas.save();
			canvas.rotate(mHourRotate, mDialCenterX, mDialCenterY);
			mRect.set(mDialCenterX - mHourAxisX, mDialCenterY - mHourAxisY, mDialCenterX
					+ mHourhandWidth - mHourAxisX, mDialCenterY + mHourHandHeight - mHourAxisY);
			canvas.drawBitmap(mHourhand, null, mRect, paint);
			canvas.restore();
		}

		if (mMinutehand != null) {
			//画分针
			canvas.save();
			canvas.rotate(mMinutesRotate, mDialCenterX, mDialCenterY);
			mRect.set(mDialCenterX - mMinuteAxisX, mDialCenterY - mMinuteAxisY, mDialCenterX
					+ mMinuteHandWidth - mMinuteAxisX, mDialCenterY + mMinuteHandHeight
					- mMinuteAxisY);
			canvas.drawBitmap(mMinutehand, null, mRect, paint);
			canvas.restore();
		}

		if (mSecondhand != null) {
			//画秒针
			canvas.save();
			mSecond = (System.currentTimeMillis() - mTime) / 1000 + mStartSecond;
			canvas.rotate(mSecond * ROTATE, mDialCenterX, mDialCenterY);
			mRect.set(mDialCenterX - mSecondAxisX, mDialCenterY - mSecondAxisY, mDialCenterX
					+ mSecondHandWidth - mSecondAxisX, mDialCenterY + mSecondHandHeight
					- mSecondAxisY);
			canvas.drawBitmap(mSecondhand, null, mRect, paint);
			canvas.restore();
		}

		if (mClockScrew != null) {
			//画螺丝
			canvas.save();
			mRect.set(mDialCenterX - mScrewWidth / 2, mDialCenterY - mScrewHeight / 2, mDialCenterX
					+ mScrewWidth / 2, mDialCenterY + mScrewHeight / 2);
			canvas.drawBitmap(mClockScrew, null, mRect, paint);
			canvas.restore();
		}
	}

	private float mRecordX;
	private float mRecordY;
	private float mTempX;
	private float mTempY;
	private boolean mIsTouchIn = false;
	private boolean mMoving = false;
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		float currX;
		float currY;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (isInArea(x, y)) {
					mIsTouchIn = true;
					mMoving = false;
					mTempX = mCurrX;
					mTempY = mCurrY;
					mRecordX = x;
					mRecordY = y;
					//					mIsStop = true;
					return true;
				} else {
					mIsTouchIn = false;
					return false;
				}

			case MotionEvent.ACTION_MOVE :
				if (!mIsTouchIn || mMoving) {
					return false;
				}
				currX = mTempX + (x - mRecordX);
				currY = mTempY + (y - mRecordY);
				if (currX < mLimitLeft) {
					currX = mLimitLeft;
				}
				if (currX > mLimitRight) {
					currX = mLimitRight;
				}
				if (currY < mLimitTop) {
					currY = mLimitTop;
				}
				if (currY > mLimitBottom) {
					currY = mLimitBottom;
				}
				resetLocation(currX, currY);
				return true;
			case MotionEvent.ACTION_UP :
				if (mIsTouchIn && !mMoving) {
					if (mCurrY < mDialHeight - mBgHeight) { //如果是拉到上面去，就把时钟挂起不掉下来了。

					} else {
						mMoving = true;
						//						mIsStop = false;
						mCurrTime = System.currentTimeMillis();
						mCurrSpeed = 0;
						mOffsetS = 0;
						mIsTouchIn = false;
					}
					return true;
				}
			case MotionEvent.ACTION_CANCEL :
				mIsTouchIn = false;

				break;
			default :
				break;
		}
		return false;
	}

	private float mOffsetS; //时钟偏离平衡点的距离
	private float mCurrSpeed; //时钟当前速度
	//	private boolean mIsStop;
	private int mDamp; //损耗
	private int mRattanK; //藤条的弹性因数
	private int mG = 0; //重力系数
	private int mMinOffset; //小于这个偏移就停止运动
	private int mMinSpeed; //小于这个速度就停止运动
	public long mCurrTime; //当前时间
	private static final int M = 1; //质量     
	private float mDeformation = 0; //平衡状态下   重力==弹簧形变tempS后的拉力

	private void initArgument() {
		float scale = mBgHeight / BG_REALLY_H;
		if (scale == 0) {
			scale = 1f;
		}
		mDamp = (int) (DAMP * scale);
		mRattanK = (int) (RATTAN_K * scale);
		//		mG = (int) (0 * scale);
		mMinOffset = (int) (MIN_OFFSET * scale);
		mMinSpeed = (int) (MIN_SPEED * scale);
		//		if (mRattanK != 0) {
		//			mDeformation = M * mG / mRattanK;
		//		}
	}

	/***
	 * <br>功能简述: 是否点击在时钟区域内
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isInArea(float x, float y) {
		if (x < mCurrX + mEdgeDis || x > mCurrX + mDialWidth - mEdgeDis
				|| y < mCurrY + mBgHeight - mDialHeight + 2 * mEdgeDis || y > mCurrY + mBgHeight) {
			return false;
		}
		return true;
	}

	/**
	 * <br>功能简述: 刷新时钟位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x
	 * @param y
	 */
	private void resetLocation(float x, float y) {
		mCurrY = (int) y;
		mCurrX = (int) x;
	}

	@Override
	public void cleanUp() {
		unRegisterTime();
	}
	@Override
	public void moving() {
		if (mMoving) {
			if (Math.abs(mCurrY - mStartY) > mMinOffset || Math.abs(mCurrSpeed) > mMinSpeed) {
				long intervalTiem = System.currentTimeMillis() - mCurrTime;
				mCurrTime = System.currentTimeMillis();
				if (intervalTiem > 100) { //保护  ，预防间隔时间过大的情况
					intervalTiem = 100;
				}
				//计算弹性形变
				if (mCurrY - mStartY > -mDeformation) {
					mOffsetS = Math.abs(mCurrY - mStartY + mDeformation);
				} else {
					mOffsetS = -Math.abs(mCurrY - mStartY + mDeformation);
				}
				float a = mG - 2 * mOffsetS * mRattanK / M;
				//将速度降下来
				if (mCurrSpeed > 0) {
					a = a - mDamp - 2 * Math.abs(mCurrSpeed) - 10 * Math.abs(mOffsetS);
				} else {
					a = a + mDamp + 2 * Math.abs(mCurrSpeed) + 10 * Math.abs(mOffsetS);
				}
				//速度增量
				float at = a * intervalTiem / 1000;
				//周期内位移增量
				float s = mCurrSpeed * intervalTiem / 1000 + at * intervalTiem / 2000;
				//当前速度变化
				float lastSpeed = mCurrSpeed;
				mCurrSpeed = mCurrSpeed + at;
				if (lastSpeed < 0 && mCurrSpeed > 0 && mViewControl != null) { //到最高点了，需要抖点树叶下来
					mViewControl.shakeRandoms(Math.abs(2.5f * mOffsetS));
				}
				//当前位置变化
				mCurrY += s;
				resetLocation(mCurrX, mCurrY);
				//				mIsStop = false;
			} else {
				//				if (!mIsStop) {
				//					if (((mCurrY - mStartY) > 0 && mCurrSpeed < 0)
				//							|| ((mCurrY - mStartY < 0 && mCurrSpeed > 0))) {
				//						long intervalTiem = System.currentTimeMillis() - mCurrTime;
				//						mCurrTime = System.currentTimeMillis();
				//						if (intervalTiem > 100) { //保护  ，预防间隔时间过大的情况
				//							intervalTiem = 100;
				//						}
				//						float vt = mCurrSpeed * intervalTiem / 1000;
				//						float regressionS = vt * vt / (4 * mCurrY - mStartY);
				//						mCurrY -= regressionS;
				//						resetLocation(mCurrX, mCurrY);
				//					} else {
				//						mCurrY = mStartY;
				//						resetLocation(mCurrX, mCurrY);
				//					}
				//				} else {
				//					mIsStop = true;
				//					mMoving = false;
				//				}
				//				mIsStop = true;
				mMoving = false;
			}
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}
}
