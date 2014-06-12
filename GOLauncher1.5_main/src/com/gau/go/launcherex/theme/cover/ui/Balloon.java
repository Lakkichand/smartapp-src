package com.gau.go.launcherex.theme.cover.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

/**
 * 
 * <br>类描述: 热气球精灵
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-11-8]
 */
public class Balloon implements OnResponTouchListener, ICleanable, IMovable {
	private Context mContext;
	public boolean mAllowDrag; //是否允许拖动

	private int mScreenWidth;
	private int mScreenHeight;

	private int mCurrY; //组件Y坐标
	private int mCurrX; //组件X坐标

	public float mStartPerX; //开始位置
	public float mStartPerY;

	public boolean mLimit; //是否有限制拖动范围
	public float mLimitLeftPer; //限制可拖动范围
	public float mLimitRightPer;
	public float mLimitTopPer;
	public float mLimitBottomPer;

	public int mLimitLeft; //限制拖动范围
	public int mLimitRight;
	public int mLimitTop;
	public int mLimitBottom;

	private int mStartX; //开始位置
	private int mStartY;

	public Bitmap mBalloonImg;   //热气球图片
	private int mBalloonWidth;   //热气球宽
	private int mBalloonHeight;  //热气球高

	private int mBatteryLevel; //电量
	private Rect mRect;
	private Paint mPaint;

	private int[] mTextXY;                     //电量的XY值
	private int[] mTextIndex;               //电量
	public int mNumWidth;           //电量字体的宽
	public int mNumHeight;           //电量字体的高
	private boolean mIsShock = false; //上下震动开关

	private int mRoateX;
	private int mRoateY;

	private static final float ROATE_Y = -0.2f;

	//***************小气球的相关属性*************************//
	public Bitmap mAroundImg;    //小气球群的图片
	private int mAroundW;        //小气球群的宽
	private int mAroundH;        //小气球群的高
	private int mAroundStartX;   //小气球群相对热气球的开始坐标
	private int mAroundStartY;

	public ArrayList<Bitmap> mSmallBallImgList;    //飞出去的小气球用到的图片 
	public ArrayList<Bitmap> mIndisTinctImgList; //模糊效果
	private List<BallChildren> mChildMap;          //可见小气球队列
	private List<BallChildren> mInvisibleChildMap; //不可见的小气球队列

	private Random mRandom = new Random();
	private static final byte[] TOUCH_MUTEX = new byte[0];

	private boolean mReDisplayArounds; //小气球飞出的时候，围绕热气球的小球群需重新慢慢显示出来
	private int mTransparency = 255;     //围绕热气球的小球群的透明度

	public ArrayList<Bitmap> mNumBitmapsList;    //显示电量用的图片
	private boolean mIsDrawText;
	private static final int MAX_BALL_NUM = 100; //限制小氣球球數量
	public List<List<Bitmap>> mBlastImgNames; //爆炸圖片
	
	public Balloon(int screenWidth, int screenHeight) {
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
		mChildMap = new ArrayList<BallChildren>();
		mInvisibleChildMap = new ArrayList<BallChildren>();
	}

	/**
	 * 初始化信息
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mRect = new Rect();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		if (mNumBitmapsList != null && mNumBitmapsList.size() > 0) {
			mNumWidth = mNumBitmapsList.get(0).getWidth();
			mNumHeight = mNumBitmapsList.get(0).getHeight();
		}

		mTextXY = new int[2];
		if (mBalloonImg != null) {
			mBalloonWidth = mBalloonImg.getWidth();
			mBalloonHeight = mBalloonImg.getHeight();
		}

		mStartX = (int) (mStartPerX * mScreenWidth);
		mStartY = (int) (mStartPerY * mScreenHeight);

		mCurrX = mStartX;
		mCurrY = mStartY;

		if (mLimit) {
			mLimitLeft = (int) (mScreenWidth * mLimitLeftPer);
			mLimitRight = (int) (mScreenWidth * mLimitRightPer - mBalloonWidth);
			mLimitTop = (int) (mScreenHeight * mLimitTopPer);
			mLimitBottom = (int) (mScreenHeight * mLimitBottomPer - mBalloonHeight);
		} else {
			mLimitLeft = 0;
			mLimitRight = mScreenWidth - mBalloonWidth;
			mLimitTop = 0;
			mLimitBottom = mScreenHeight - mBalloonHeight;
		}
		if (mAroundImg != null) {
			mAroundW = mAroundImg.getWidth();
			mAroundH = mAroundImg.getHeight();
			mAroundStartX = (mBalloonWidth - mAroundW) / 2;
			mAroundStartY = (int) ((mBalloonHeight - mAroundH) * 0.67f);
		}
		mIsShock = true;
		setTextXY(100);
	}

	/**
	 * 重新设置数据
	 */
	public void resetData(int screenWidth, int screenHeight, boolean backToDefault) {
		
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mStartX = (int) (mStartPerX * mScreenWidth);
		mStartY = (int) (mStartPerY * mScreenHeight);
		
       if (backToDefault) {
   		mCurrX = mStartX;
   		mCurrY = mStartY; 
       }


		if (mLimit) {
			mLimitLeft = (int) (mScreenWidth * mLimitLeftPer);
			mLimitRight = (int) (mScreenWidth * mLimitRightPer - mBalloonWidth);
			mLimitTop = (int) (mScreenHeight * mLimitTopPer);
			mLimitBottom = (int) (mScreenHeight * mLimitBottomPer - mBalloonHeight);
		} else {
			mLimitLeft = 0;
			mLimitRight = mScreenWidth - mBalloonWidth;
			mLimitTop = 0;
			mLimitBottom = mScreenHeight - mBalloonHeight;
		}
	}

	/**
	 * 设置显示电量的文字
	 * @param battery
	 */
	private void setTextXY(int battery) {
		mIsDrawText = true;
		if (mNumBitmapsList == null || mNumBitmapsList.size() < 10) {
			mIsDrawText = false;
			return;
		}
		String batteryText = Integer.toString(battery);

		if (batteryText.contains(".")) {   //保护操作，不太可能出现，可选择去掉
			batteryText = batteryText.substring(0, batteryText.indexOf("."));
		}
		int length = batteryText.length();
		if (length <= 3) {
			mTextIndex = new int[length + 1];
			for (int i = 0; i < length; i++) {
				mTextIndex[i] = Integer.parseInt(batteryText.substring(i, i + 1));
			}
			mTextIndex[length] = 10; //加上电量
		} else {
			mIsDrawText = false;
			return;
		}

		if (mTextXY == null) {
			mTextXY = new int[2];
		}
		float w = mNumWidth * (length + 1);
		mTextXY[0] = (int) ((mBalloonWidth - w) / 2);
		mTextXY[1] = (int) ((mBalloonHeight - mNumHeight) / 4);
	}

	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mRect == null) {
			mRect = new Rect();
		}
		if (mReDisplayArounds) {
			mTransparency = 0;
			mReDisplayArounds = false;
		}

		//热气球
		if (mBalloonImg != null) {
			mRoateX = mCurrX + mBalloonWidth / 2;
			mRoateY = (int) (mCurrY + mBalloonHeight * ROATE_Y);
			canvas.save();
			if (mSwing) {
				canvas.rotate(mSwingAmplitude, mRoateX, mRoateY);
				canvas.translate(-mSwingAmplitude * 3f, 0);
			}

			mRect.set((int) mCurrX, (int) (mCurrY + mAmplitude), mCurrX + mBalloonWidth,
					(int) (mCurrY + mBalloonHeight + mAmplitude));
			canvas.drawBitmap(mBalloonImg, null, mRect, paint);

			if (mIsDrawText) {
				mPaint.setAlpha(255);
				int size = mTextIndex.length;
				for (int i = 0; i < size; i++) {
					mRect.set(mCurrX + mTextXY[0] + mNumWidth * i,
							(int) (mCurrY + mTextXY[1] + mAmplitude), mCurrX + mTextXY[0]
									+ mNumWidth + mNumWidth * i, (int) (mCurrY + mTextXY[1]
									+ mAmplitude + mNumHeight));
					canvas.drawBitmap(mNumBitmapsList.get(mTextIndex[i]), null, mRect, mPaint);
				}
			}
			canvas.restore();
		}

		//围绕热气球的小球群
		if (mTransparency < 255) {
			mTransparency += 6;  //小气球群重新可见的速度
		}

		if (mTransparency > 255) {
			mTransparency = 255;
		}

		if (mAroundImg != null) {
			canvas.save();
			if (mSwing) {
				canvas.translate(-mSwingAmplitude * 3f, 0);
			}

			mRect.set((int) mCurrX + mAroundStartX,
					(int) (mCurrY + mAroundStartY + mAmplitude / 3), mCurrX + mAroundStartX
							+ mAroundW, (int) (mCurrY + mAroundStartY + mAroundH + mAmplitude / 3));
			mPaint.setAlpha(mTransparency);
			canvas.drawBitmap(mAroundImg, null, mRect, mPaint);
			canvas.restore();

		}

		//飞出来的小球们
		synchronized (TOUCH_MUTEX) {
			if (mChildMap != null && mChildMap.size() > 0) {
				for (BallChildren child : mChildMap) {
					child.doDraw(camera, matrix, canvas, paint);
					if (!child.mAlive) {
						mInvisibleChildMap.add(child);
					}
				}
			}
			for (BallChildren child : mInvisibleChildMap) {
				mChildMap.remove(child);
				child.cleanUp();
				child = null;
			}
			mInvisibleChildMap.clear();
		}
	}

	private static final int MAX_AMPLITUDE = 6; //上下震动的最大幅度
	private static final float STEP = 0.2f; //上下震动每次最小位移
	private boolean mIsUp = false;
	private float mAmplitude = 0;

	private static final float MAX_SWING_AMPLITUDE = 10f; //左右摆动的最大幅度
	private static final float SWING_STEP = 0.1f; //左右摆动每次最小位移
	private boolean mIsLeft = false;
	private float mSwingAmplitude = 0;
	private float mSwingLimite;
	private static final int WAIT = 5;
	private int mCountDown = WAIT;

	public void moving() {
		if (mSwing) {  //左右摆动
			if (mSwingLimite >= 0) {
				if (mIsLeft) {
					if (mSwingAmplitude > -mSwingLimite) {
						mSwingAmplitude -= SWING_STEP + 1.1 * mSwingLimite / MAX_SWING_AMPLITUDE;; //速度与偏移角度成及最大便宜角度成正比
					} else {
						mSwingLimite -= 0.2f + mSwingLimite / 2;
						mIsLeft = false;
					}
				} else {
					if (mSwingAmplitude < mSwingLimite) {
						mSwingAmplitude += SWING_STEP + 1.1 * mSwingLimite / MAX_SWING_AMPLITUDE; //速度与偏移角度成及最大便宜角度成正比
					} else {
						mSwingLimite -= 0.2f + mSwingLimite / 2;
						mIsLeft = true;
					}
				}
			} else {
				mSwing = false;
			}
		}

		if (mIsShock) {  //上下震动
			if (mIsUp) {
				if (mAmplitude < MAX_AMPLITUDE) {
					mCountDown = WAIT;
					mAmplitude += STEP - Math.abs(mAmplitude / MAX_AMPLITUDE / 10);
				} else {
					if (mCountDown > 0) {
						mCountDown-- ;
					} else {
						mIsUp = false;
					}
				}
			} else {
				if (mAmplitude > -MAX_AMPLITUDE) {
					mCountDown = WAIT;
					mAmplitude -= STEP - Math.abs(mAmplitude / MAX_AMPLITUDE / 10);
				} else {
					if (mCountDown > 0) {
						mCountDown-- ;
					} else {
						mIsUp = true;
					}
				}
			}
		}

		for (BallChildren child : mChildMap) {
			child.moving();
		}

	}
	//***************TouchEvent相关*************************//
	private float mRecordX;
	private float mRecordY;
	private float mTempX;
	private float mTempY;
	private boolean mIsTouchIn = false;
	private boolean mSwing = false;

	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		float currX;
		float currY;
		
		//飞出来的小球们
		if (mChildMap != null && mChildMap.size() > 0) {
			for (int i = mChildMap.size() - 1; i >= 0; i--) {
				if (mChildMap.get(i).onTouchEvent(event)) { 
					return true;
				}
			}
		}
		
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (isInArea(x, y)) {
					mIsTouchIn = true;
					mSwing = false;
					mTempX = mCurrX;
					mTempY = mCurrY;
					mRecordX = x;
					mRecordY = y;
					return true;
				} else {
					mIsTouchIn = false;
					return false;
				}

			case MotionEvent.ACTION_MOVE :
				if (!mIsTouchIn || mSwing) {
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
				if (mIsTouchIn && !mSwing) {
					mSwing = true;
					mSwingLimite = MAX_SWING_AMPLITUDE;
					mIsTouchIn = false;
			    if (mChildMap != null && mChildMap.size() < MAX_BALL_NUM) {
					createChild();
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
	
	/***
	 * 创建小球对象
	 */
	private void createChild() {
		if (mIndisTinctImgList != null) {
		}
		if (mSmallBallImgList == null || mIndisTinctImgList == null || mSmallBallImgList.size() == 0 ||
				mIndisTinctImgList.size() !=  mSmallBallImgList.size()) {
			return;
		}
		int count = 10;
		//计算速度，并排序
		int[] speedUp;
		int[] speedDown;
		int upNum = (int) (count * 0.2) + mRandom.nextInt((int) (count * 0.4)); //移动到远处的气球数量
		speedUp = new int[upNum];
		speedDown = new int[count - upNum];
		for (int i = 0; i < upNum; i++) {
			speedUp[i] = -1 - mRandom.nextInt(BallChildren.MAX_SPEED_UP - 1);
		}
		for (int i = 0; i < count - upNum; i++) {
			speedDown[i] = 1 + mRandom.nextInt(BallChildren.MAX_SPEED_DOWN - 1);
		}
		speedUp = sortArray(speedUp);
		speedDown = sortArray(speedDown);

		//创建小气球对象
		List<BallChildren> child = new ArrayList<BallChildren>(count);

		int y;
		int x;

		int sizeUp = speedUp.length;
		int ballIndex;
		for (int i = 0; i < sizeUp; i++) {
			y = mCurrY + mAroundStartY + mRandom.nextInt(mAroundH);
			x = mCurrX + mAroundStartX + mRandom.nextInt(mAroundW);
			ballIndex = mRandom.nextInt(mSmallBallImgList.size());
			child.add(new BallChildren(mScreenWidth, mScreenHeight, x, y, mSmallBallImgList
					.get(ballIndex), mIndisTinctImgList.get(ballIndex), mBlastImgNames.get(ballIndex), speedUp[i], true)); 
		}

		int sizeDown = speedDown.length;
		for (int i = 0; i < sizeDown; i++) {
			y = mCurrY + mAroundStartY + mRandom.nextInt(mAroundH);
			x = mCurrX + mAroundStartX + mRandom.nextInt(mAroundW);
			ballIndex = mRandom.nextInt(mSmallBallImgList.size());
			child.add(new BallChildren(mScreenWidth, mScreenHeight, x, y, mSmallBallImgList
					.get(ballIndex),  mIndisTinctImgList.get(ballIndex), mBlastImgNames.get(ballIndex), speedDown[i], false));
		}

		mReDisplayArounds = true;
		synchronized (TOUCH_MUTEX) {
			mChildMap.addAll(child);
		}
	}

	public int[] sortArray(int[] array) {
		int n = array.length;
		for (int i = 1; i < n; i++) {
			for (int j = 0; j < n - i; j++) {
				if (array[j] > array[j + 1]) //比较交换相邻元素 
				{
					int temp;
					temp = array[j];
					array[j] = array[j + 1];
					array[j + 1] = temp;
					break;
				}
			}
		}
		return array;
	}

	/***
	 * 是否点击在时钟区域内
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isInArea(float x, float y) {
		if (x < mCurrX || x > mCurrX + mBalloonWidth || y < mCurrY || y > mCurrY + mBalloonHeight) {
			return false;
		}
		return true;
	}

	/**
	 * 刷新时钟位置
	 * @param x
	 * @param y
	 */
	private void resetLocation(float x, float y) {
		mCurrY = (int) y;
		mCurrX = (int) x;
	}

	/**
	 * 注册电量广播
	 */
	public void registerBattery() {
		mContext.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); //获得电量
	}

	/**
	 * 反注册电量广播
	 */
	public void unRegisterBattery() {
		mContext.unregisterReceiver(mBatInfoReceiver);
	}

	/**
	 * 广播出电量值
	 */
	BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			mBatteryLevel = intent.getIntExtra("level", 0);
			setTextXY(mBatteryLevel);
		}
	};

	public boolean isTaped(float x, float y) {
		return false;
	}

	/**
	 *  清理资源
	 */
	public void cleanUp() {
		unRegisterBattery();
		if (mChildMap != null) {
			mChildMap.clear();
		}
		if (mInvisibleChildMap != null) {
			mInvisibleChildMap.clear();
		}
	}

}
