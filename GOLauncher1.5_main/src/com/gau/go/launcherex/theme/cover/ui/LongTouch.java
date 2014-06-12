package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * 触摸屏幕时出现的景物
 * @author jiangxuwen
 *
 */
public class LongTouch implements IDrawable, IMovable, ICleanable {

	private static final int LOCATION_TYPE_ON_TOUCH = 1;
	private static final int LOCATION_TYPE_FIXED = 0;
	private static final int LOCATION_TYPE_LIMITAREA = 2;
	private int mLocationType;
	private static final float BASE_SCALE = 1.0f;
	private static final int DROP_DOWN_OFFSET = 30;
	private static final int MAX_ALPHA = 255;
	private int mTopBitmapIndex;
	private int mBottomBitmapIndex;
	public Bitmap[] mTopBitmaps; // 显示的位图数组
	public Bitmap mShadow; // 影子显示的位图
	boolean mAllowDrag; // 是否可拖拽的标志位
	float mScale; // 缩放比例
	int mX; // 坐标x
	int mY; // 坐标y
	int mWidth; // 宽度
	int mHeight; // 高度
	Rect mLimitRect; // 限制的显示区域
	int mAlpha; // 透明度
	long mLifeTime = 2000; // 生存时间
	private int mShadowNextToX;
	private int mShadowNextToY;
	private volatile boolean mIsActive;
	public int mSpeedAlpha;
	private Point mDropDownWaterPoint;
	private int mDropDownSpeedY = 25;
	private int mDropDownAlpha = MAX_ALPHA;
	private int mTopBitmapLength;
	private int mBottomBitmapLength;
	private static final int CANCEL_ANIMATION_COUNT = 1;
	private volatile int mAnimationCount;
	public Bitmap[] mBottomBitmaps;
	private boolean mMissOnTouchUp;
	private long mLastTopUpdateTime;
	private long mLastBottomUpdateTime;
	private int[] mTopBitmapFrameIntervals;
	private int[] mBottomBitmapFrameIntervals;
	private int mStartLocationX;
	private int mStartLocationY;
	private int mTouchOffsetX;
	private int mTouchOffsetY;

	public LongTouch(Bitmap[] topbitmaps, Bitmap[] bottomBitmaps, Bitmap shadow, int x, int y,
			boolean allowDrag, int shadowNextToX, int shadowNextToY, Rect limitRect, long life,
			int loopCount, boolean missOnTouchUp, int[] topBitmapFrameIntervals,
			int[] bottomBitmapFrameIntervals, int locationType, int startLocationX,
			int startLocationY, int inAniamtionType, int touchOffsetX, int touchOffsetY) {
		mTouchOffsetX = touchOffsetX;
		mTouchOffsetY = touchOffsetY;
		mTopBitmapFrameIntervals = topBitmapFrameIntervals;
		mBottomBitmapFrameIntervals = bottomBitmapFrameIntervals;
		mMissOnTouchUp = missOnTouchUp;
		mTopBitmaps = topbitmaps;
		mBottomBitmaps = bottomBitmaps;
		mShadow = shadow;
		mX = x;
		mY = y;
		mScale = BASE_SCALE;
		mAlpha = MAX_ALPHA;
		mAllowDrag = allowDrag;
		mShadowNextToX = shadowNextToX;
		mShadowNextToY = shadowNextToY;
		mLimitRect = limitRect;
		mAnimationCount = loopCount;
		mLifeTime = life;
		mIsActive = true;
		mDropDownWaterPoint = new Point();
		initDropDownPoint();
		mTopBitmapLength = mTopBitmaps != null ? mTopBitmaps.length : 0;
		mBottomBitmapLength = mBottomBitmaps != null ? mBottomBitmaps.length : 0;
		mLastTopUpdateTime = System.currentTimeMillis();
		mLocationType = locationType;
		mStartLocationX = startLocationX;
		mStartLocationY = startLocationY;
		mInAnimationType = inAniamtionType;
	}

	private void initDropDownPoint() {
		mDropDownWaterPoint.x = mX;
		mDropDownWaterPoint.y = mY + DROP_DOWN_OFFSET;
	}

	public static LongTouch fromTemplate(LongTouch longTouch, int x, int y) {
		LongTouch ret = new LongTouch(longTouch.mTopBitmaps, longTouch.mBottomBitmaps,
				longTouch.mShadow, -1, -1, longTouch.mAllowDrag, longTouch.mShadowNextToX,
				longTouch.mShadowNextToY, longTouch.mLimitRect, longTouch.mLifeTime,
				longTouch.mAnimationCount, longTouch.mMissOnTouchUp,
				longTouch.mTopBitmapFrameIntervals, longTouch.mBottomBitmapFrameIntervals,
				longTouch.mLocationType, longTouch.mStartLocationX, longTouch.mStartLocationY,
				longTouch.mInAnimationType, longTouch.mTouchOffsetX, longTouch.mTouchOffsetY);
		ret.initDropDownPoint();
		if (ret.mLocationType == LOCATION_TYPE_FIXED) {
			ret.mX = ret.mStartLocationX;
			ret.mY = ret.mStartLocationY;
		} else if (ret.mLocationType == LOCATION_TYPE_ON_TOUCH) {
			if (longTouch.mTopBitmaps != null && longTouch.mTopBitmaps.length > 0) {
				ret.mX = x - (longTouch.mTopBitmaps[0].getWidth() >> 1);
				ret.mY = y - (longTouch.mTopBitmaps[0].getWidth() >> 1);
			} else {
				ret.mX = x;
				ret.mY = y;
			}
		} else if (ret.mLocationType == LOCATION_TYPE_LIMITAREA) {
			if (longTouch.mTopBitmaps != null && longTouch.mTopBitmaps.length > 0) {
				ret.mX = x - (longTouch.mTopBitmaps[0].getWidth() >> 1) + ret.mTouchOffsetX;
				ret.mY = y + ret.mTouchOffsetY;
				if (ret.mY < ret.mLimitRect.top) {
					ret.mY = ret.mLimitRect.top;
				}
				if (ret.mY > ret.mLimitRect.bottom) {
					ret.mY = ret.mLimitRect.bottom;
				}
			}
		}
		ret.initInAnimationType();
		return ret;
	}

	private void initInAnimationType() {
		if (mInAnimationType == ANIAMTIONTYPE_FADELEFTTORIGHT) {
			mDestRect = new Rect();
			mSrcRect = new Rect();
			mSrcRect.left = mSrcRect.right = 0;
			mSrcRect.bottom = mSrcRect.top = 0;
			if (mTopBitmaps != null && mTopBitmapLength > 0) {
				mSrcRect.bottom = mTopBitmaps[0].getHeight() + mSrcRect.top;
				mDestRect.left = 0;
				mDestRect.right = mSrcRect.right;
				mDestRect.top = 0;
				mDestRect.bottom = mDestRect.top + mTopBitmaps[0].getHeight();
			}
			mStartFadeInTime = System.currentTimeMillis();
		}
	}

	// inAniamtionType 的逻辑
	private static final int ANIMATIONTYPE_NORMAL = 0;
	private static final int ANIAMTIONTYPE_FADELEFTTORIGHT = 1;
	private int mInAnimationType;
	private long mStartFadeInTime;
	private Rect mDestRect;
	private Rect mSrcRect;
	private static final int FADEIN_ANIMATION_TIME = 1000;

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mIsActive) {
			if (mTopBitmaps[mTopBitmapIndex] != null) {
				canvas.save();
				canvas.translate(mX, mY);
				canvas.scale(mScale, mScale);
				int oldAlpha = paint.getAlpha();
				if (mTopBitmapIndex == mTopBitmapLength - 1
						&& mInAnimationType == ANIAMTIONTYPE_FADELEFTTORIGHT
						&& mAnimationCount == CANCEL_ANIMATION_COUNT) {
					paint.setAlpha(mDropDownAlpha);
				} else {
					paint.setAlpha(mAlpha);
				}
				if (mInAnimationType == ANIMATIONTYPE_NORMAL) {
					canvas.drawBitmap(mTopBitmaps[mTopBitmapIndex], matrix, paint);
					if (mShadow != null) {
						canvas.translate(mShadowNextToX * mScale, mShadowNextToY * mScale);
						canvas.drawBitmap(mShadow, matrix, paint);
					}
				} else if (mInAnimationType == ANIAMTIONTYPE_FADELEFTTORIGHT) {
					canvas.drawBitmap(mTopBitmaps[mTopBitmapIndex], mSrcRect, mDestRect, paint);
					if (mShadow != null) {
						canvas.translate(mShadowNextToX * mScale, mShadowNextToY * mScale);
						canvas.drawBitmap(mShadow, mSrcRect, mDestRect, paint);
					}
				}

				canvas.restore();
				paint.setAlpha(oldAlpha);
			}
			if (mTopBitmapIndex == mTopBitmapLength - 1 && mBottomBitmaps != null
					&& mBottomBitmaps.length > 0 && mBottomBitmaps[mBottomBitmapIndex] != null) {
				canvas.save();
				canvas.translate(mDropDownWaterPoint.x, mDropDownWaterPoint.y);
				canvas.scale(mScale, mScale);
				int oldAlpha = paint.getAlpha();
				paint.setAlpha(mDropDownAlpha);
				canvas.drawBitmap(mBottomBitmaps[mBottomBitmapIndex], matrix, paint);
				canvas.restore();
				paint.setAlpha(oldAlpha);
				mDropDownWaterPoint.y += mDropDownSpeedY;
			}
		}
	}

	public boolean isActive() {
		return mIsActive;
	}

	public void cancel() {
		if (mMissOnTouchUp) {
			mAnimationCount = CANCEL_ANIMATION_COUNT;
		}
	}

	@Override
	public void moving() {
		if (mIsActive) {
			if (mAnimationCount > 0) {
				long current = System.currentTimeMillis();
				handleInAnimation(current);
				if (current - mLastTopUpdateTime > mTopBitmapFrameIntervals[mTopBitmapIndex]) {
					mTopBitmapIndex++;
					if (mTopBitmapIndex >= mTopBitmapLength) {
						mTopBitmapIndex = 0;
						mAnimationCount--;
						if (mAnimationCount <= 0) {
							mIsActive = false;
						}
					}
					mLastTopUpdateTime = current;
					if (mTopBitmapIndex == mTopBitmapLength - 1 && mBottomBitmaps != null
							&& mBottomBitmapLength > 0) {
						mLastBottomUpdateTime = current;
						initDropDownPoint();
					}
				}
				if (mTopBitmapIndex == mTopBitmapLength - 1 /*&& mBottomBitmaps != null
															&& mBottomBitmaps.length > 0*/) {
					mDropDownAlpha = (int) ((1 - (float) (current - mLastTopUpdateTime)
							/ mTopBitmapFrameIntervals[mTopBitmapIndex]) * MAX_ALPHA);
					if (mBottomBitmapFrameIntervals != null
							&& mBottomBitmapFrameIntervals.length > 0
							&& current - mLastBottomUpdateTime > mBottomBitmapFrameIntervals[mBottomBitmapIndex]) {
						mBottomBitmapIndex++;
						if (mBottomBitmapIndex >= mBottomBitmapLength) {
							mBottomBitmapIndex = 0;
						}
						mLastBottomUpdateTime = current;
					}
				}
			} else {
				mIsActive = false;
			}
		}
	}

	private void handleInAnimation(long current) {
		if (mInAnimationType == ANIAMTIONTYPE_FADELEFTTORIGHT) {
			float t = current - mStartFadeInTime;
			if (t < FADEIN_ANIMATION_TIME) {
				mSrcRect.right = (int) (mSrcRect.left + mTopBitmaps[0].getWidth() * t
						/ FADEIN_ANIMATION_TIME);
				mDestRect.right = mSrcRect.right;
			} else {
				mSrcRect.right = mSrcRect.left + mTopBitmaps[0].getWidth();
				mDestRect.right = mSrcRect.right;
			}
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

	@Override
	public void cleanUp() {
		recycle(mTopBitmaps);
		recycle(mShadow);
		mLimitRect = null;
		if (mDropDownWaterPoint != null) {
			mDropDownWaterPoint = null;
		}
	}

	protected void recycle(Bitmap[] bitmaps) {
		if (bitmaps != null) {
			for (Bitmap bitmap : bitmaps) {
				recycle(bitmap);
			}
		}
	}

	/**
	 * <br>功能简述:不销毁bitmap，因为bitmap属于共用，在{@link BitmapLoader}统一处理,先置为null
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 */
	protected void recycle(Bitmap bitmap) {
		if (bitmap != null) {
			bitmap = null;
		}
	}
}
