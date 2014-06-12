package com.gau.go.launcherex.theme.cover.ui;

import java.util.ArrayList;
import java.util.List;

import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.sensor.AccelerometerDataState;
import com.gau.go.launcherex.theme.cover.ui.action.ActionControler;
import com.gau.go.launcherex.theme.cover.ui.action.BaseAction;
import com.gau.go.launcherex.theme.cover.ui.action.Drivenable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * 可以旋转摆动的景物
 * 
 * @author jiangxuwen
 * 
 */
public class Rotate extends Drivenable implements IMovable, OnResponTouchListener {
	private static final int OFFSET_Y = -150;
	public float mAngle; // 当前的旋转角度
	private float mMaxAngle = 0; // 允许的最大摆幅         
	private boolean mIsShaking = false;

	private float mAngleBeforeShake; // 摇摆前的角度
	private int mWidth; // 宽度
	private int mHeight; // 高度
	
	public boolean mAlive = true;
	public BaseAction mCurrentAction;
	public List<BaseAction> mClickActions;
	public BaseAction mDragAction; 
	private long mLastGravityUpdateTime;
	private static final byte[] MUTEX = new byte[0];
	
	private Rect mRopeRect;
	private Rect mBodyRect;
	//xml文件中配置的属性
//	private boolean mAllowDrag; // 是否可拖拽的标志位
//	private boolean mCreateShow; //刚创建时是否可见
	private int mStartX; // 默认坐标x
	private int mStartY; // 默认坐标y
//	private boolean mNeedShadow; //是否有阴影
//	private int mNextToX; //阴影相对位置坐标x
//	private int mNextToY; //阴影相对位置坐标y
//    private boolean mLimit; //是否限制了区域
//	private int mLandType;
//	private int mPortType;
	private long mDurationMax;
	private long mDuration; // 一个周期动画的时间              
//	private boolean mLoop; // 是否循环动画的标识        
	private float mPerAngle; // 每次变化的旋转角度
    private float mFromAngle;  // 开始旋转的角度
    public float mPivoX;
    public float mPivoY;
    public Bitmap mRopeBitmap; //挂着的绳子   （一般情况必须有绳子）
    public Bitmap mBodyBitmap; //悬挂着的物体  （可以没有，如藤条）
    public int mRopeHeight; //绳子的高度
    
    private float mBodyTopBlank = 0.2f;
    private float mAngleAttenuation = 0.8f; //摆动一次角度衰减度 
    private float mTimeAttenuation = 1f; //摆动一次周期时间衰减度 
    private boolean mIsFlyback;
 	// 加速度
 	private float mAccele;
 	// 初始速度
 	private float mMaxV;
    
 	private boolean mIsPort;
 	
    /**
     * <默认构造函数>
     */
	public Rotate(Context context, Bitmap ropeBitmap, Bitmap bodyBitmap, int ropeHeight, boolean allowDrag, boolean createShow, int startX, int startY, 
			Bitmap shadow, boolean needShadow, int nextToX, int nextToY, boolean limit, Rect limitRect, 
			long duration, boolean loop, float perAngle, float fromAngle, float pivoX, float pivoY) {
		mContext = context;
		mRopeBitmap = ropeBitmap;
		mBodyBitmap = bodyBitmap;
		
//		mAllowDrag = allowDrag;
//		mCreateShow = createShow;
		mStartX = startX;
		mStartY = startY;
		mShadow = shadow;
//		mNeedShadow = needShadow;
//		mNextToX = nextToX;
//		mNextToY = nextToY;
//		mLimit = limit;
		mLimitRect = limitRect;
//		mLandType = landType;
//		mPortType = portType;
		mDurationMax = duration;
//		mLoop = loop;
		mPerAngle = perAngle;
		mFromAngle = fromAngle;

		mX = mStartX;
		mY = mStartY;
		
		mRopeRect = new Rect();
		mBodyRect = new Rect();
		if (mBodyBitmap != null) {
			mWidth = mBodyBitmap.getWidth();
			mRopeHeight = ropeHeight;
			mHeight = mRopeHeight;
		} else {
			mWidth = mRopeBitmap.getWidth();
			mRopeHeight = mRopeBitmap.getHeight();
			mHeight = mRopeBitmap.getHeight();
		}
		mPivoX = pivoX * mWidth;
		mPivoY = pivoY * mHeight;
		
		mClickActions = new ArrayList<BaseAction>();
		mIsPort = DrawUtils.isPort();
	   
	}
	 
	public void resetData(float startX, float startY, Rect limitRect, boolean backToDefault) {
			mStartX = (int) startX;
			mStartY = (int) startY;
			mX = mStartX;
			mY = mStartY;
		    mLimitRect = limitRect;
		    mIsPort = DrawUtils.isPort();
		    
			if (mDragAction != null) {
				mDragAction.resetData();
			}
	}
	
	public void setClickAction(int index, int actionType,
			int acrion1DelayTime, int animatingBitmapType, Bitmap[] action1Bitmaps,
			Bitmap shadow, boolean needLoop, int animationTime, boolean isBitmapSymmetric) {
		ActionControler controler = ActionControler.getInstance();
		BaseAction action = controler.getAction(index, actionType, this, acrion1DelayTime, action1Bitmaps
				, shadow, animatingBitmapType, action1Bitmaps, null, null
				, null, null, needLoop, animationTime, isBitmapSymmetric, 0);
		mClickActions.add(index, action);
	}
	
	public void setDragAction(int actionType,
			int acrion1DelayTime, int animatingBitmapType, Bitmap[] action1Bitmaps,
			Bitmap shadow, boolean needLoop, int animationTime, boolean isBitmapSymmetric) {
		ActionControler controler = ActionControler.getInstance();
		mDragAction = controler.getAction(0, actionType, this, acrion1DelayTime, action1Bitmaps
				, shadow, animatingBitmapType, action1Bitmaps, null, null
				, null, null, needLoop, animationTime, isBitmapSymmetric, 0);
	}
	
	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (!mAlive || mBitmap == null) {
			return;
		} 
		if (mCurrentAction != null) {
			mCurrentAction.onDraw(camera, matrix, canvas, paint);
		}
		if (mIsBaseDraw) {
			if (mRopeBitmap != null) {
				canvas.save();
				canvas.translate(mX, mY);
				if (mPivoY < mHeight / 2) {
					canvas.rotate(mAngle, mPivoX, mPivoY);
				} else {
					canvas.rotate(-mAngle, mPivoX, mPivoY);
				}
		
				if (mBodyBitmap != null) {
					mRopeRect.set((mWidth - mRopeBitmap.getWidth()) / 2, OFFSET_Y, (mWidth + mRopeBitmap.getWidth()) / 2, 
							(int) (mRopeHeight + mBodyBitmap.getHeight() * mBodyTopBlank));
				} else {
					mRopeRect.set(0, 0, mWidth, (int) mRopeHeight);
				}
				canvas.drawBitmap(mRopeBitmap, null, mRopeRect, paint);
				canvas.restore();
			}
			
			if (mBodyBitmap != null) {
				canvas.save();
				canvas.translate(mX, mY);
				canvas.rotate(mAngle, mPivoX, mPivoY);
				mBodyRect.set((int) (mBodyBitmap.getWidth() * (1 - mScale)) / 2, mRopeHeight + 
						(int) (mBodyBitmap.getHeight() * (1 - mScale)) / 2, 
						(int) (mBodyBitmap.getWidth() * (1 + mScale)) / 2, mRopeHeight + (int) (mBodyBitmap.getHeight() * (1 + mScale) / 2));
				canvas.drawBitmap(mBodyBitmap, null, mBodyRect, paint);
				canvas.restore();
			}
			
		}
	}
	
	private long mFlybackTime = 600;
	private volatile boolean mIsFlybackAnimating;
	private volatile boolean mIsGravityAnimating;
	private float mStartAngle;
	private int mEndAngle;
	@Override
	public void moving() {
		if (mCurrentAction != null) {
			mCurrentAction.doAction();
		}
		if (mDragAction != null) {
			if (mDragAction.doAction()) { //如果正在上下摆动，就不响应左右摆动了
				return;
			};
		}
		if (!mIsTouchIn) {
			if (mIsShaking) {
				onShake();
			} else if (mIsFlybackAnimating) {
				long t = System.currentTimeMillis() - mLastGravityUpdateTime;
				mAngle = dropToDrop(mStartAngle, mEndAngle, mFlybackTime, t);
				if (mAngle == mEndAngle) {
					mLastGravityUpdateTime = System.currentTimeMillis();
					mIsFlybackAnimating = false;
                	mIsGravityAnimating = false;
				}
			} else if (mIsGravityAnimating) {
				long t = System.currentTimeMillis() - mLastGravityUpdateTime;
				mAngle = dropToDrop(mStartAngle, mEndAngle, mFlybackTime, t);
				if (mAngle == mEndAngle) {
					mLastGravityUpdateTime = System.currentTimeMillis();
					mIsGravityAnimating = false;
				}
			}
		}
	}

	private float mLastGravityX;
	private float mCritical = 5.5f;  //临界点，小于这个点，则响应摇摆动画，且主动回到起始点。
	
	public void handleGravityChange(AccelerometerDataState state) {
		float gravity = mIsPort ? state.getX() : -state.getY();
		mLastGravityX = gravity;
		if (Math.abs(gravity) > mCritical) {
			if (mIsShaking = true) {
				mIsShaking = false;
			}
			if (!mIsGravityAnimating) {
				mIsGravityAnimating = true;
				mIsFlybackAnimating = false;
				mEndAngle = (int) (gravity * 9);
				mStartAngle = mAngle;
				mLastGravityUpdateTime = System.currentTimeMillis();
			}
		} else {
			if (!mIsShaking) {
				if (mAngle != 0) {
					if (!mIsFlybackAnimating) {
						mIsFlybackAnimating = true;
						mStartAngle = mAngle;
						mEndAngle = 0;
						mLastGravityUpdateTime = System.currentTimeMillis();
					}
				} else {
					mIsFlybackAnimating = false;
					mIsGravityAnimating = false;
				}
			}
		}
	}

	/***
	 *  给定运动时间，初始和结束速度都为0，先加速后减速的运动
	 * @param startS      始点
	 * @param endS        终点
	 * @param totalTime   整个运动过程花的时间
	 * @param time        已经经过的时间
	 * @param updateTime  运动结束后重置时间
	 * @return
	 */
	private static float dropToDrop(float startS, int endS, long totalTime, long time) {
		final long halfTime = totalTime / 2;
		final float accele = (endS - startS) / (halfTime * halfTime);
		final float maxV =  accele * halfTime;
		float currS;
		if (time <= totalTime) {
			float angle = 0;
			float half = 0;
			if (time <= totalTime / 2) {
				angle = accele * time * time / 2;
			} else {
				long t = totalTime / 2;
				half = accele * t * t / 2;
				time = time - t;
				angle = maxV * time - accele * time * time / 2;
			}
			currS = startS + half + angle;
		} else {
			currS = endS;
		}
		return currS;
	}

	public void handShake(float speed) {
		if (mAngle != 0 || Math.abs(mLastGravityX) > mCritical) {
			return;
		}
		synchronized (MUTEX) {
			if (!mIsTouching && !mIsShaking) {
				mAngle = 0;
				mMaxAngle = (float) (Math.sqrt(Math.abs(speed)) * 5);

				mDuration = mDurationMax;
				final float deffAngel = mMaxAngle - mAngle; // 角度差
				mAngleBeforeShake = mAngle; // 开始摇摆
				int t = (int) (mDuration / 2);
				mAccele = (deffAngel + 0.1f) / (t * t);
				mMaxV = mAccele * mDuration * 0.5f; // 最大速度
				mIsShaking = true;
				mIsFlyback = false;
				mLastGravityUpdateTime = System.currentTimeMillis();
			}
		}
	}
	
	private void onShake() {
		synchronized (MUTEX) {
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
					mAngle = mAngleBeforeShake + half + angle;
				} else {
					mIsShaking = false;
					mAngle = 0;
					mIsFlybackAnimating = false;
					mIsGravityAnimating = false;
					mLastGravityUpdateTime = System.currentTimeMillis();
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
					mAngle = mAngleBeforeShake + half + angle;
				} else {    // 大于一个周期
					mDuration *= mTimeAttenuation;
					mAngleBeforeShake = mAngle;
					mLastGravityUpdateTime = System.currentTimeMillis();
					if (Math.abs(mMaxAngle) < 10) {  //角度小于一定量后就开始做恢复到起始点的动画
						long t = mDuration / 2;
						mAccele = -mMaxAngle / (t * t);
						mMaxV = mAccele * t;
						mIsFlyback = true;
						return;
					} else {
						mMaxAngle = -mMaxAngle * mAngleAttenuation; // 最大角度反向
						final float deffAngel = mMaxAngle - mAngle;
						long t = mDuration / 2;
						mAccele = (deffAngel + 0.1f) / (t * t);
						mMaxV = mAccele * t;
					}
				}
			}
		}
	}
	
	private void resetAngle() {
		mIsShaking = false;
		mAngle = 0;
	}
	
	public void regist() {
		if (mClickActions != null) {
			for (int i = 0; i < mClickActions.size(); i++) {
				mClickActions.get(i).regist();
			}
		}
		if (mDragAction != null) {
			mDragAction.regist();
		}
	}
	
	public void unRegiste() {
		if (mDragAction != null) {
			mDragAction.regist();
		}
	}

	@Override
	public void cleanUp() {
		if (mClickActions != null) {
			for (int i = 0; i < mClickActions.size(); i++) {
				mClickActions.get(i).cleanUp();
			}
		}
		if (mDragAction != null) {
			mDragAction.cleanUp();
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return isInArea(x, y);
	}
	
	/***
	 * 是否点击在时钟区域内
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isInArea(float x, float y) {
		if (mBodyBitmap == null) {
			return false;
		}
		if (x < mX || x > mX + mBodyBitmap.getWidth() || y < mY + mRopeHeight || y > mY 
				+ mRopeHeight + mBodyBitmap.getHeight()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean checkOutOfBound() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean mIsTouchIn = false;
	private boolean mIsTouching = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		final int action = event.getAction();
		boolean isTaped;
		//处理点击
		switch (action) {
			case MotionEvent.ACTION_DOWN :
					isTaped = isTaped(event.getX(), event.getY());
					if (isTaped) {
						resetAngle();
						mIsTouchIn = true;
						if (mCurrentActionIndex == 0) {
							if (mClickActions != null && mClickActions.size() > 0) {
								mCurrentActionIndex++;
								mCurrentAction = mClickActions.get(mCurrentActionIndex);
							}
						}
					} else {
					    mIsTouchIn = false;
					    return false;
					}
					ret = true;		
				break;
			case MotionEvent.ACTION_MOVE:
				if (!mIsTouchIn) {
					return false;
				}
				ret = true;
				break;
			case MotionEvent.ACTION_UP:
				if (mIsTouchIn) {
					if (mDragAction == null) {  //如果有拖拽就不处理点击
						isTaped = isTaped(event.getX(), event.getY());
						if (mCurrentAction != null && isTaped) {
								mCurrentAction.onTouchEvent(event);
						}
					}
					mIsTouchIn = false;
					ret = true;
				}
				break;
			case MotionEvent.ACTION_CANCEL :
				if (mIsTouchIn) {
					mIsTouchIn = false;
					ret = true;
				}
				break;
			default :
				break;
		}
		
		if (mDragAction != null) {  //如果有拖拽就不处理点击
			 mDragAction.onTouchEvent(event);
		}
		return ret;
	}
	

	public void prepareClickAction(int index, Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps, Bitmap action1Shadow,
			Bitmap action2Shadow, Bitmap defaultAction2Bitmap) {
		if (index >= 0 && index < mClickActions.size() && mClickActions.get(index) != null) {
			mClickActions.get(index) .onResume(actionBitmaps, shadow, action1Bitmaps, action2Bitmaps,
					action1Shadow, action2Shadow, defaultAction2Bitmap);
		}
	}

	public void prepareDragAction(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps, Bitmap action1Shadow,
			Bitmap action2Shadow, Bitmap defaultAction2Bitmap) {
			mDragAction.onResume(actionBitmaps, shadow, action1Bitmaps, action2Bitmaps,
					action1Shadow, action2Shadow, defaultAction2Bitmap);
	}
	
	@Override
	public void onActionDone(BaseAction action, int actionIndex) {
		if (mClickActions != null && mClickActions.size() > 0) {
			actionIndex++;
			mCurrentActionIndex = actionIndex % mClickActions.size();
			mCurrentAction = mClickActions.get(actionIndex);
		}
	}
}