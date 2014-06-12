package com.gau.go.launcherex.theme.cover.ui;

import java.util.Random;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.OnCreateAtomListener;
import com.gau.go.launcherex.theme.cover.ViewControl;
import com.gau.go.launcherex.theme.cover.ui.action.ActionControler;
import com.gau.go.launcherex.theme.cover.ui.action.BaseAction;
import com.gau.go.launcherex.theme.cover.ui.action.Drivenable;
import com.gau.go.launcherex.theme.cover.ui.action.ShakeAction;

/**
 * 
 * <br>
 * 类描述:可运动精灵 <br>
 * 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-22]
 */
public class Spirit extends Drivenable implements IMovable,
		OnResponTouchListener {

	private static final int DEFAULT_DECCLERATE_NONE = 1;
	private static final int DEFAULT_DECCLERATE_8 = 8;
	private static final int DEFAULT_DECCLERATE_16 = 16;
	private static final int ADDITIONAL_NUM_FIRST = 1;
	private static final int ADDITIONAL_NUM_SECOND = 2;
	private int mAnimatingBitmapType;
	public Bitmap[] mAction1Bitmaps;
	public Bitmap[] mAction2Bitmaps;
	public Bitmap mDefaultAction1Bitmap;
	public Bitmap mDefaultAction2Bitmap;
	public Bitmap mAction2Shadow;
	public Bitmap mAction1Shadow;
	private Random mRandom = new Random();
	private int mShadowNextToX;
	private int mShadowNextToY;
	private boolean mAllowDrag;
	private int mAcrion1DelayTime;
	private int mAction2DelayTime;
	private Matrix mMatrix;
	// private float mScale;
	private int mAlpha = 255;
	private BaseAction mActionOne;
	private BaseAction mActionTwo;
	private BaseAction mDragAction;
	private ShakeAction mShakeAction;

	private boolean mNeedGetBitmapInDrawThread;
	private boolean mNeedLoop;
	private int mAnimationTime;
	private BaseAction mAddtionalActionOne;
	private BaseAction mAddtionalActionTwo;
	public OnCreateAtomListener mOnCreateAtomListener;

	private float mStartLandX;
	private float mStartLandY;
	private float mStartX;
	private float mStartY;
	private IActionCallback mIActionCallback;

	/**
	 * 以参数赋值，属性不对外开放 <默认构造函数>
	 */
	public Spirit(IActionCallback actionCallback, Context context,
			OnCreateAtomListener onCreateAtomListener, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow,
			Bitmap action2Shadow, Bitmap defaultAction1, Bitmap defaultAction2,
			int shadowNextToX, int shadowNextToY, int animatingBitmapType,
			Rect limitRect, boolean allowDrag, float rotateX, float rotateY,
			int action1DelayTime, int action2DelayTime, float x, float y,
			int startLandX, int startLandY, int firstAngle, int actionOneType,
			int actionTwoType, boolean needLoop, int animationTime,
			boolean actionOneBitmapSymmetric, boolean actionTwoBitmapSymmetric) {
		mIActionCallback = actionCallback;
		mContext = context;
		mOnCreateAtomListener = onCreateAtomListener;
		mMatrix = new Matrix();
		mMatrix.setValues(new float[] { -1.0f, 0, 0, 0, 1.0f, 0, 0, 0, 1.0f });
		mAcrion1DelayTime = action1DelayTime;
		mAction2DelayTime = action2DelayTime;
		mAlpha = 255;
		mScale = 1;

		mStartX = x;
		mStartY = y;
		mStartLandX = startLandX;
		mStartLandY = startLandY;

		if (DrawUtils.isPort()) {
			mX = mStartX;
			mY = mStartY;
		} else {
			mX = mStartLandX;
			mY = mStartLandY;
		}

		mActionTwoType = actionTwoType;
		mActionOneType = actionOneType;
		mBitmap = defaultAction1;
		mAction1Bitmaps = action1Bitmaps;
		mAction2Bitmaps = action2Bitmaps;
		mShadow = mAction1Shadow = action1Shadow;
		mAction2Shadow = action2Shadow;
		mShadowNextToX = shadowNextToX;
		mShadowNextToY = shadowNextToY;
		mDefaultAction1Bitmap = defaultAction1;
		mDefaultAction2Bitmap = defaultAction2;
		mAnimatingBitmapType = animatingBitmapType;
		mLimitRect = limitRect;
		mAllowDrag = allowDrag;
		mPivotX = rotateX;
		mPivotY = rotateY;
		mDefaultAngle = firstAngle;
		mNeedLoop = needLoop;
		mAnimationTime = animationTime;
		initActionType(mActionOneType, mActionTwoType,
				actionOneBitmapSymmetric, actionTwoBitmapSymmetric);
		mNeedGetBitmapInDrawThread = needGetBitmapInDrawThread();
	}

	public void setAdditionalAction(int additionalNum,
			int additionalActionType, int acrion1DelayTime,
			int animatingBitmapType, Bitmap[] action1Bitmaps,
			Bitmap action1Shadow, boolean needLoop, int animationTime,
			boolean isBitmapSymmetric) {
		ActionControler controler = ActionControler.getInstance();
		BaseAction action = controler.getAdditionalAction(0,
				additionalActionType, this, acrion1DelayTime, action1Bitmaps,
				action1Shadow, animatingBitmapType, needLoop, animationTime,
				isBitmapSymmetric);
		switch (additionalNum) {
		case ADDITIONAL_NUM_FIRST:
			mAddtionalActionOne = action;
			break;
		case ADDITIONAL_NUM_SECOND:
			mAddtionalActionTwo = action;
			break;
		default:
			break;
		}
	}

	private void initActionType(int actionOneType, int actionTwoType,
			boolean actionOneBitmapSymmetric, boolean actionTwoBitmapSymmetric) {
		ActionControler controler = ActionControler.getInstance();
		mActionOne = controler.getAction(0, actionOneType, this,
				mAcrion1DelayTime, mAction1Bitmaps, mAction1Shadow,
				mAnimatingBitmapType, mAction1Bitmaps, mAction2Bitmaps,
				mAction1Shadow, mAction2Shadow, mDefaultAction2Bitmap,
				mNeedLoop, mAnimationTime, actionOneBitmapSymmetric, 0);
		if (actionOneType != actionTwoType) {
			mActionTwo = controler.getAction(0, actionTwoType, this,
					mAction2DelayTime, mAction2Bitmaps, mAction2Shadow,
					mAnimatingBitmapType, mAction1Bitmaps, mAction2Bitmaps,
					mAction1Shadow, mAction2Shadow, mDefaultAction2Bitmap,
					mNeedLoop, mAnimationTime, actionTwoBitmapSymmetric, 0);
		} else {
			mActionTwo = mActionOne;
		}
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {

		if (mNeedGetBitmapInDrawThread) {
			mBitmap = mActionOne.getBimap(DEFAULT_DECCLERATE_NONE);
		}
		switch (mTouchState) {
		case TOUCH_STATE_IN_NORMAL:
			if (mActionOne != null) {
				mActionOne.onDraw(camera, matrix, canvas, paint);
			}
			if (mAddtionalActionOne != null) {
				mAddtionalActionOne.onDraw(camera, matrix, canvas, paint);
			}
			break;
		case TOUCH_STATE_IN_CLICK:
			if (mActionTwo != null) {
				mActionTwo.onDraw(camera, matrix, canvas, paint);
			}
			break;
		default:
			break;
		}

		drawBitmap(mShadow, true, camera, matrix, canvas, paint);
		drawBitmap(mBitmap, false, camera, matrix, canvas, paint);
	}

	private boolean needYMapping() {
		if (mTouchState == TOUCH_STATE_IN_NORMAL) {
			return mActionOne.needBitmapYMapping();
		} else if (mTouchState == TOUCH_STATE_IN_CLICK) {
			return mActionTwo.needBitmapYMapping();
		}
		return false;
	}

	private void drawBitmap(Bitmap bitmap, boolean needShadowNext,
			Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (bitmap != null && mIsBaseDraw) {
			canvas.save();
			canvas.translate(mX, mY);
			canvas.scale(mScale, mScale);
			canvas.translate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
			if (needYMapping()) {
				canvas.translate(mBitmap.getWidth(), 0);
				canvas.concat(mMatrix);
			} else {
				canvas.rotate(mAngle, bitmap.getWidth() * mPivotX,
						bitmap.getHeight() * mPivotY);
			}
			if (needShadowNext) {
				canvas.translate(mShadowNextToX * mScale, mShadowNextToY
						* mScale);
			}
			canvas.drawBitmap(bitmap, matrix, paint);
			canvas.restore();
		}
	}

	private boolean mIsTouchIn;

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
				if (mTouchState == TOUCH_STATE_IN_NORMAL) {
					ret = true;
					if (!((mActionOneType == ActionControler.ACTION_TYPE_BROKENLINE && mActionTwoType == ActionControler.ACTION_TYPE_BROKENLINE) || mActionTwoType == ActionControler.ACTION_TYPE_EMPTY)) {
						mTouchState = TOUCH_STATE_IN_CLICK;
					}
					if (mActionTwo != null) {
						if (mIActionCallback != null) {
							mIActionCallback.onActionCallback(ViewControl.OBJECT_TYPE_SLIDE, 0);
						}
						mActionTwo.onTouchEvent(event);
					}
					if (mAddtionalActionOne != null) {
						mAddtionalActionOne.onTouchEvent(event);
					}
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

		if (mDragAction != null) {
			mDragAction.onTouchEvent(event);
		}
		return ret;
	}

	private boolean needGetBitmapInDrawThread() {
		return mTouchState == TOUCH_STATE_IN_NORMAL
				&& mActionOneType == ActionControler.ACTION_TYPE_BROKENLINE
				|| mActionTwoType == ActionControler.ACTION_TYPE_BROKENLINE
				&& mTouchState == TOUCH_STATE_IN_CLICK;
	}

	public boolean checkOutOfBound() {
		boolean needUpdate = false;
		if (mY < mLimitRect.top) {
			mSpeedY = Math.abs(mSpeedY);
			needUpdate = true;
		}
		float y = mBitmap != null ? mY + (mBitmap.getHeight() >> 1) : mY;
		if (y > mLimitRect.bottom) {
			mSpeedY = -Math.abs(mSpeedY);
			needUpdate = true;
		}
		if (mX < mLimitRect.left) {
			mSpeedX = Math.abs(mSpeedX);
			needUpdate = true;
		}
		float x = mBitmap != null ? mX + (mBitmap.getHeight() >> 1) : mX;
		if (x > mLimitRect.right) {
			mSpeedX = -Math.abs(mSpeedX);
			needUpdate = true;
		}
		return needUpdate;
	}

	@Override
	public void moving() {
		if (mShakeAction != null) {
			if (mShakeAction.doAction()) {
				return;
			}
		}
		if (mDragAction != null) {
			if (mDragAction.doAction()) {
				return;
			}
		}

		switch (mTouchState) {
		case TOUCH_STATE_IN_NORMAL:
			if (mActionOne != null) {
				mActionOne.doAction();
				if (!mNeedGetBitmapInDrawThread) {
					if (mActionTwoType == ActionControler.ACTION_TYPE_LINE) {
						mBitmap = mActionOne.getBimap(DEFAULT_DECCLERATE_16);
					} else if (mActionTwoType == ActionControler.ACTION_TYPE_BEZIER) {
						mBitmap = mActionOne.getBimap(DEFAULT_DECCLERATE_8);
					} else {
						mBitmap = mActionOne.getBimap(DEFAULT_DECCLERATE_NONE);
					}
				}
			}
			if (mAddtionalActionOne != null) {
				mAddtionalActionOne.doAction();
			}
			mShadow = mAction1Shadow;
			break;

		case TOUCH_STATE_IN_CLICK:
			if (mActionTwo != null) {
				mActionTwo.doAction();
				if (!mNeedGetBitmapInDrawThread) {
					mBitmap = mActionTwo.getBimap(DEFAULT_DECCLERATE_NONE);
				}
			}
			mShadow = mAction2Shadow;
			break;

		default:
			break;
		}
	}

	private boolean equalZero(float x) {
		if (Math.abs(x) < 1E-2) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isTaped(float x, float y) {
		if (mBitmap != null) {
			return Math.abs(mX - x) <= mBitmap.getWidth() >> 1
					&& Math.abs(mY - y) <= mBitmap.getHeight() >> 1;
		}
		return false;
	}

	/**
	 * 响应甩动事件
	 * 
	 * @param speed
	 */
//	public void handShake(float speed) {
//		if (mShakeAction != null) {
//			mShakeAction.handShake(speed);
//		}
//	}
	
	public void handAction() {
		if (mTouchState == TOUCH_STATE_IN_NORMAL) {
			if (!((mActionOneType == ActionControler.ACTION_TYPE_BROKENLINE && mActionTwoType == ActionControler.ACTION_TYPE_BROKENLINE) || mActionTwoType == ActionControler.ACTION_TYPE_EMPTY)) {
				mTouchState = TOUCH_STATE_IN_CLICK;
			}
			if (mActionTwo != null) {
				mActionTwo.handAction();
			}
		}
	}
	

	@Override
	public void cleanUp() {
		recycle(mAction1Bitmaps);
		recycle(mAction2Bitmaps);
		recycle(mDefaultAction1Bitmap);
		recycle(mDefaultAction2Bitmap);
		recycle(mAction2Shadow);
		recycle(mAction1Shadow);
		mMatrix = null;
		if (mActionOne != null) {
			recycleAction(mActionOne.mAdditionalAction);
			recycleAction(mActionOne);
		}
		if (mActionTwo != null) {
			recycleAction(mActionTwo.mAdditionalAction);
			recycleAction(mActionTwo);
		}
		if (mDragAction != null) {
			mDragAction.cleanUp();
		}
		if (mShakeAction != null) {
			mShakeAction.cleanUp();
		}
		if (mIActionCallback != null) {
			mIActionCallback = null;
		}
		mActionTwo = null;
	}

	private void recycleAction(BaseAction action) {
		if (action != null) {
			action.cleanUp();
			action = null;
		}
	}

	public void prepare(Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction1Bitmap, Bitmap defaultAction2Bitmap) {
		mAction1Bitmaps = action1Bitmaps;
		mAction1Shadow = action1Shadow;
		mAction2Bitmaps = action2Bitmaps;
		mAction2Shadow = action2Shadow;
		mDefaultAction1Bitmap = defaultAction1Bitmap;
		mDefaultAction2Bitmap = defaultAction2Bitmap;
		ActionControler.getInstance().prepareAction(mActionOne, mActionOneType,
				action1Bitmaps, action1Shadow, action1Bitmaps, action2Bitmaps,
				action1Shadow, action2Shadow, defaultAction2Bitmap);
		ActionControler.getInstance().prepareAction(mActionTwo, mActionTwoType,
				action2Bitmaps, action2Shadow, action1Bitmaps, action2Bitmaps,
				action1Shadow, action2Shadow, defaultAction2Bitmap);
	}

	public void prepareAdditionalActionTwo(Bitmap[] actionBitmaps,
			Bitmap shadow, Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		if (mAddtionalActionTwo != null) {
			mAddtionalActionTwo.onResume(actionBitmaps, shadow, action1Bitmaps,
					action2Bitmaps, action1Shadow, action2Shadow,
					defaultAction2Bitmap);
		}
	}

	public void prepareAdditionalActionOne(Bitmap[] actionBitmaps,
			Bitmap shadow, Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		if (mAddtionalActionOne != null) {
			mAddtionalActionOne.onResume(actionBitmaps, shadow, action1Bitmaps,
					action2Bitmaps, action1Shadow, action2Shadow,
					defaultAction2Bitmap);
		}
	}

	public void resetData(float x, float y, Rect limitRect,
			boolean backToDefault) {
		mX = equalZero(x) ? mRandom.nextInt(DrawUtils.getScreenViewWidth())
					: x;
		mY = equalZero(y) ? mRandom
					.nextInt(DrawUtils.getScreenViewHeight()) : y;
		mLimitRect = limitRect;
		if (mActionOne != null) {
			mActionOne.resetData();
			if (mActionOne.mAdditionalAction != null) {
				mActionOne.mAdditionalAction.resetData();
			}
		}
		if (mActionTwo != null) {
			mActionTwo.resetData();
			if (mActionTwo.mAdditionalAction != null) {
				mActionTwo.mAdditionalAction.resetData();
			}
		}
		if (mDragAction != null) {
			mDragAction.resetData();
		}

		if (mShakeAction != null) {
			mShakeAction.resetData();
		}
	}

	@Override
	public void onActionDone(BaseAction action, int actionIndex) {
		// TODO Auto-generated method stub
	}

	public void setDragAction(int actionType, int acrion1DelayTime,
			int animatingBitmapType, Bitmap[] action1Bitmaps, Bitmap shadow,
			boolean needLoop, int animationTime, boolean isBitmapSymmetric) {
		ActionControler controler = ActionControler.getInstance();
		if (mDragAction != null) {
			mDragAction = controler.getAction(0, actionType, this,
					acrion1DelayTime, action1Bitmaps, shadow,
					animatingBitmapType, action1Bitmaps, null, null, null,
					null, needLoop, animationTime, isBitmapSymmetric, 0);
		}

	}

	public void prepareDragAction(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		if (mDragAction != null) {
			mDragAction.onResume(actionBitmaps, shadow, action1Bitmaps,
					action2Bitmaps, action1Shadow, action2Shadow,
					defaultAction2Bitmap);
		}
	}

	public void setShakeAction(int actionType, int acrion1DelayTime,
			int animatingBitmapType, Bitmap[] action1Bitmaps, Bitmap shadow,
			boolean needLoop, int animationTime, boolean isBitmapSymmetric,
			int shakeSpeed) {
		ActionControler controler = ActionControler.getInstance();
		BaseAction action = controler.getAction(0, actionType, this,
				acrion1DelayTime, action1Bitmaps, shadow, animatingBitmapType,
				action1Bitmaps, null, null, null, null, needLoop,
				animationTime, isBitmapSymmetric, shakeSpeed);
		if (action != null && action instanceof ShakeAction) {
			mShakeAction = (ShakeAction) action;
		}
	}

	public void prepareShakeAction(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		if (mShakeAction != null) {
			mShakeAction.onResume(actionBitmaps, shadow, action1Bitmaps,
					action2Bitmaps, action1Shadow, action2Shadow,
					defaultAction2Bitmap);
		}

	}

}
