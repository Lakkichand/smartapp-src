package com.gau.go.launcherex.theme.cover.ui;

import com.gau.go.launcherex.theme.cover.ViewControl;
import com.gau.go.launcherex.theme.cover.ui.action.ActionControler;
import com.gau.go.launcherex.theme.cover.ui.action.BaseAction;
import com.gau.go.launcherex.theme.cover.ui.action.Drivenable;
import com.gau.go.launcherex.theme.cover.ui.action.ShakeAction;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;

/***
 * 固定位置不变，定时更换图片，类似于幻灯片播放
 * @author maxiaojun
 *
 */
public class Slide extends Drivenable implements IMovable, OnResponTouchListener {
	public static final int SLIDE_TYPE_SHAKE = 1; //默认开始图片更换，摇晃手机更换一次
	public static final int SLIDE_TYPE_STEP = 2; //在接到外部通知后才开始图片更换
	public static final int SLIDE_TYPE_SHAKE_CONTINUOUS = 3; //晃动手机，连续更换完

	public int mAnimationType;
	public int mShakeSpeed;
	private boolean mDoSlide; //开始幻灯片切换
	public Bitmap[] mBitmaps;
	private boolean mUpdateSnowHeap = true; // 是否需要定时更换雪堆
	private long mCurrTime;
	private int mBitmapCount = 5; // 暂时共5张，雪堆的切图数量变化时需修改此参数
	private int mSnowHeapIndex; // 雪堆图片的索引
	private boolean mIsReduce = false; // 判断晃动手机时雪堆是要增加还是减少
	private int mImgIndex; //记录当前用了第几张图片
//	private int mScreenWidth;
//	private int mScreenHeight;
	public long mInterval; //两张图片的间隔时间，cover中配置
	private ShakeAction mShakeAction;
	private boolean mIsTouchIn;
	private IActionCallback mIActionCallback;
	public Slide(IActionCallback actionCallback, int width, int height) {
		mIActionCallback = actionCallback;
//		mScreenWidth = width;
//		mScreenHeight = height;
		mUpdateSnowHeap = true;
		mCurrTime = System.currentTimeMillis() + 4800; // 4800：雪花从屏幕顶端出现到第一次落到屏幕低端的时间
	}

	public void init() {
		if (mAnimationType == SLIDE_TYPE_SHAKE) { //对于自动切换类型，mDoSlide一开始就置为true
			mDoSlide = true;
		} else {
			mDoSlide = false;
		}
		if (mBitmaps != null) {
			mBitmapCount = mBitmaps.length;
		}
	}
	
	public void resetData(int width, int height, boolean backToDefault) {
//		mScreenWidth = width;
//		mScreenHeight = height;
		if (mShakeAction != null) {
			mShakeAction.resetData();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mIsTouchIn = isTaped(x, y);
			if (mIsTouchIn) {
                //点击
				handAnimate();
				if (mIActionCallback != null) {
					mIActionCallback.onActionCallback(ViewControl.OBJECT_TYPE_SPIRIT, 0);
				}
				ret = true;
			} else {
				return ret;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsTouchIn) {
				ret = true;
			} else {
				return ret;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsTouchIn) {
				ret = true;
			} else {
				return ret;
			}
			break;
		default:
			break;
		}
		return ret;
	}
	
	public void handAnimate() {
		if (mAnimationType == SLIDE_TYPE_SHAKE_CONTINUOUS) {
			mSnowHeapIndex = 0;
			mHandle.removeCallbacks(mThread);
			mHandle.postDelayed(mThread, 500);  //延迟1000毫秒，为了松树枝的摆动时机与猫头鹰飞的动作相符
			return;
		}
	}
	
	/**
	 * 响应甩动事件
	 * 
	 * @param speed
	 */
	public void handShake(float speed) {
		if (Math.abs(speed) < mShakeSpeed) {
		    return;
		}
		if (mAnimationType == SLIDE_TYPE_SHAKE_CONTINUOUS) {
			mSnowHeapIndex = 0;
			mHandle.postDelayed(mThread, 500);  //延迟300毫秒，为了松树枝的摆动时机与猫头鹰飞的动作相符
			return;
		}
		if (mAnimationType == SLIDE_TYPE_SHAKE) {
			// 甩动手机，雪堆变化
				if (mSnowHeapIndex >= mBitmapCount - 1) {
					mIsReduce = true;
				} else if (mSnowHeapIndex <= 0) {
					mIsReduce = false;
				}
				mUpdateSnowHeap = true;
				if (mIsReduce) {
					mSnowHeapIndex--;
				} else {
					mSnowHeapIndex++;
				}
				mCurrTime = System.currentTimeMillis() - mSnowHeapIndex
						* mInterval;
			}
	}

	Handler mHandle = new Handler();
	Thread mThread = new Thread(new Runnable() { 
		@Override
		public void run() {
			if (mShakeAction != null) {
				mShakeAction.handShake(0);
			}
			doSlide();
		}
	});
	
	private void doSlide() {
		if (mSnowHeapIndex == 0) {
			mDoSlide = true;
			mUpdateSnowHeap = true;
			mCurrTime =  System.currentTimeMillis();
		}
	}
	
	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		// TODO Auto-generated method stub
		if (mBitmap == null) {
			return;
		}
		canvas.drawBitmap(mBitmap, mX, mY, paint);
		if (mShakeAction != null) {
			mShakeAction.onDraw(camera, matrix, canvas, paint);
		}
	}

	@Override
	public void moving() {
		if (mShakeAction != null) {
			mShakeAction.doAction();
		}
		if (mDoSlide && mUpdateSnowHeap) {
			updateSnowHeap();
		}
	}
	
	private void updateSnowHeap() {
		long time = System.currentTimeMillis() - mCurrTime;
		if (time > 0) {
			mSnowHeapIndex = (int) (time / mInterval);
		} else {
			mSnowHeapIndex = 0;
		}		
		
		if (mAnimationType == SLIDE_TYPE_SHAKE_CONTINUOUS) {
			if (mSnowHeapIndex == mBitmapCount) {
				mSnowHeapIndex = 0;
				mUpdateSnowHeap = false;
				mDoSlide = false;
				return;
			}
		}
		
		if (mSnowHeapIndex < mBitmapCount) {
			if (mSnowHeapIndex >= 0 && mSnowHeapIndex < mBitmaps.length) {
				if (mSnowHeapIndex != mImgIndex) {
					mBitmap = mBitmaps[mSnowHeapIndex];;
					mImgIndex = mSnowHeapIndex;
				}
			}
		} else {
			mSnowHeapIndex = 0;
			mBitmap = mBitmaps[mSnowHeapIndex];;
			mUpdateSnowHeap = false;
		}
	}
	
	public void setShakeAction(int actionType,
			int acrion1DelayTime, int animatingBitmapType, Bitmap[] action1Bitmaps,
			Bitmap shadow, boolean needLoop, int animationTime, boolean isBitmapSymmetric, int shakeSpeed) {
		ActionControler controler = ActionControler.getInstance();
		BaseAction action = controler.getAction(0, actionType, this, acrion1DelayTime, action1Bitmaps
				, shadow, animatingBitmapType, action1Bitmaps, null, null
				, null, null, needLoop, animationTime, isBitmapSymmetric, shakeSpeed);
		if (action != null && action instanceof ShakeAction) {
			mShakeAction = (ShakeAction) action;
		}
	}

	public void prepareShakeAction(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps, Bitmap action1Shadow,
			Bitmap action2Shadow, Bitmap defaultAction2Bitmap) {
		if (mShakeAction != null) {
			mShakeAction.onResume(actionBitmaps, shadow, action1Bitmaps, action2Bitmaps,
					action1Shadow, action2Shadow, defaultAction2Bitmap);
		}
	}
	
	
	@Override
	public boolean isTaped(float x, float y) {
		if (mBitmap == null) {
			return false;
		}
		if (x < mX  || x > mX + mBitmap.getWidth()
				|| y < mY + mBitmap.getHeight() * 0.35  || y > mY + mBitmap.getHeight() * 0.7) {
			return false;
		}
		return true;
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		if (mShakeAction != null) {
			mShakeAction.cleanUp();
		}
		mBitmaps = null;
	}

	@Override
	public boolean checkOutOfBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onActionDone(BaseAction action, int actionIndex) {
		// TODO Auto-generated method stub
		
	}

}
