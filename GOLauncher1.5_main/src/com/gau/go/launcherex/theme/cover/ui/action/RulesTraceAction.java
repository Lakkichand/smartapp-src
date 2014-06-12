package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.SparseArray;

import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.utils.AngleAndPointUtils;
import com.gau.go.launcherex.theme.cover.utils.LineUtils;
import com.gau.go.launcherex.theme.cover.utils.TraceDataProvider;

/**
 * 
 * <br>类描述:限定轨迹
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-8]
 */
public class RulesTraceAction extends BaseAction {

	private static final int MAX_ALPHA = 255;
	private static final int ANIMATION_STEP_FIRST = 0;
	private static final int ANIMATION_STEP_SECOND = 1;
	private static final int ANIMATION_STEP_THIRD = 2;
	private int mAnimationStep = ANIMATION_STEP_FIRST;
	private static final float DEFAULT_SCREEN_WIDTH = 720;
	private static final float DEFAULT_SCREEN_HEIGHT = 1086;
	private int mTraceLength = 126;
	private volatile int[] mTraceXs = new int[mTraceLength];
	private volatile int[] mTraceYs = new int[mTraceLength];
	private Point mStartPoint;
	private Point mEndPoint;
	private static final int START_ANIMATION_TIME = 1000;
	private static final int END_ANIAMTION_TIME = 500;
	private int mTraceIndex;
	private long mLastUpdateTime;
	private int mNextAnimationDelay;
	private Point mCalcTempStartPoint;
	private Point mCalcTempEndPoint;
	private Random mRandom;
	private static final int ANIMATION_DELAY = 1000 * 45;
	public Bitmap[] mBitmaps;
	private Bitmap mBitmap;
	private int mBitmapIndex;
	private int mBitmapLength;
	private static final int ANIMATION_CONTINUED_TIME = 3000;
	private long mStartContinuedTime;
	private int mContinuedAlpha;
	private Point mLastDrawPoint;
	private Point mCurrentTracePoint;
	private int mAngle;
	private SparseArray<Point> mIndexToPosition;
	private SparseArray<Point> mPrePosition;
	private SparseArray<Point> mNextPosition;
	private SparseArray<Bitmap> mDrawBitmaps;

	public RulesTraceAction(Drivenable drivenable, int actionIndex, Bitmap[] bitmaps, int animatingBitmapType,
			boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		mBitmaps = bitmaps;
		mBitmapLength = 0;
		if (bitmaps != null) {
			mBitmap = bitmaps[0];
			mBitmapLength = bitmaps.length;
		}
		initData();
		mRandom = new Random();
		mNextAnimationDelay = mRandom.nextInt(ANIMATION_DELAY / 2) + ANIMATION_DELAY / 2;
		mActive = false;
		mLastUpdateTime = System.currentTimeMillis();
		mCalcTempStartPoint = new Point(mStartPoint.x, mStartPoint.y);
		mCalcTempEndPoint = new Point(mTraceXs[0], mTraceYs[0]);
		mDrivenable.mX = mStartPoint.x;
		mDrivenable.mY = mStartPoint.y;
	}

	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		mDrivenable.mBitmap = null;
		if (mAnimationStep == ANIMATION_STEP_SECOND || mAnimationStep == ANIMATION_STEP_THIRD
				|| System.currentTimeMillis() - mStartContinuedTime < ANIMATION_CONTINUED_TIME) {
			for (int i = mStartTraceIndex; i < mTraceIndex; i++) {
				Point drawPoint = mIndexToPosition.get(i);
				if (!(drawPoint.x == 0 && drawPoint.y == 0)) {
					canvas.save();
					canvas.translate(drawPoint.x, drawPoint.y);
					int oldAlpha = paint.getAlpha();
					paint.setAlpha(mContinuedAlpha);
					Point nextPoint = mNextPosition.get(i);
					Point prePoint = mPrePosition.get(i);
					mAngle = AngleAndPointUtils.angleOnSpeed(nextPoint.x - prePoint.x, nextPoint.y
							- prePoint.y, 0);
					canvas.rotate(mAngle);
					mBitmap = mDrawBitmaps.get(i);
					canvas.translate(-mBitmap.getWidth() / 2, -mBitmap.getHeight() / 2);
					canvas.drawBitmap(mBitmap, matrix, paint);
					paint.setAlpha(oldAlpha);
					canvas.restore();
				}
			}
		}
		super.onDraw(camera, matrix, canvas, paint);
	}

	@Override
	public boolean doAction() {
		long current = System.currentTimeMillis();
		if (mActive) {
			doActive(current);
		} else {
			if (current - mLastUpdateTime > mNextAnimationDelay) {
				mLastUpdateTime = current;
				mAnimationStep = ANIMATION_STEP_FIRST;
				mDrivenable.mSpeedX = mCalcTempEndPoint.x - mCalcTempStartPoint.x;
				mDrivenable.mSpeedY = mCalcTempEndPoint.y - mCalcTempStartPoint.y;
				nextAngleState();
				mActive = true;
			} else {
				if (current - mStartContinuedTime < ANIMATION_CONTINUED_TIME) {
					mContinuedAlpha = (int) (MAX_ALPHA - MAX_ALPHA
							* (current - mStartContinuedTime) / ANIMATION_CONTINUED_TIME);
				} else {
					mTraceIndex = 0;
					mContinuedAlpha = 0;
					if (mShapeIndex == SHAPE_INDEX_TRACE_HEART) {
						mTraceXs = mTraceXs2;
						mTraceYs = mTraceYs2;
						mShapeIndex = SHAPE_INDEX_TRACE_GO;
						mTraceLength = mTraceXs.length;
						mPrePosition = mPrePosition2;
						mNextPosition = mNextPosition2;
						mDrawBitmaps = mDrawBitmaps2;
						mIndexToPosition = mIndexToPosition2;
						mEndPoint.x = mTraceGoEndPoint.x;
						mEndPoint.y = mTraceGoEndPoint.y;
						mStartPoint.x = mTraceGoStartPoint.x;
						mStartPoint.y = mTraceGoStartPoint.y;
						mStartTraceIndex = START_TRACE_INDEX_GO;
					} else if (mShapeIndex == SHAPE_INDEX_TRACE_GO) {
						mTraceXs = mTraceXs3;
						mTraceYs = mTraceYs3;
						mShapeIndex = SHAPE_INDEX_TRACE_TWO;
						mTraceLength = mTraceXs.length;
						mPrePosition = mPrePosition3;
						mNextPosition = mNextPosition3;
						mDrawBitmaps = mDrawBitmaps3;
						mIndexToPosition = mIndexToPosition3;
						mEndPoint.x = mTraceTwoEndPoint.x;
						mEndPoint.y = mTraceTwoEndPoint.y;
						mStartPoint.x = mTraceTwoStartPoint.x;
						mStartPoint.y = mTraceTwoStartPoint.y;
						mStartTraceIndex = START_TRACE_INDEX_TWO;
					} else if (mShapeIndex == SHAPE_INDEX_TRACE_TWO) {
						mShapeIndex = SHAPE_INDEX_TRACE_HEART;
						mTraceXs = mTraceXs1;
						mTraceYs = mTraceYs1;
						mTraceLength = mTraceXs.length;
						mPrePosition = mPrePosition1;
						mNextPosition = mNextPosition1;
						mDrawBitmaps = mDrawBitmaps1;
						mIndexToPosition = mIndexToPosition1;
						mEndPoint.x = mTraceHeartEndPoint.x;
						mEndPoint.y = mTraceHeartEndPoint.y;
						mStartPoint.x = mTraceHeartStartPoint.x;
						mStartPoint.y = mTraceHeartStartPoint.y;
						mStartTraceIndex = START_TRACE_INDEX_HEART;
					}
					mCalcTempStartPoint.x = mStartPoint.x;
					mCalcTempStartPoint.y = mStartPoint.y;
					mCalcTempEndPoint.x = mTraceXs[0];
					mCalcTempEndPoint.y = mTraceYs[0];
				}
			}
		}
		return false;
	}

	private void doActive(long current) {
		float t;
		switch (mAnimationStep) {
			case ANIMATION_STEP_FIRST :
				t = current - mLastUpdateTime;
				t = t / START_ANIMATION_TIME;
				if (t <= 1.0f) {
					Point outPoint = new Point();
					LineUtils.getInterpolatorPoint(mCalcTempStartPoint, mCalcTempEndPoint,
							outPoint, t);
					mDrivenable.mX = outPoint.x;
					mDrivenable.mY = outPoint.y;
				} else {
					mDrivenable.mX = mTraceXs[0];
					mDrivenable.mY = mTraceYs[0];
					mAnimationStep = ANIMATION_STEP_SECOND;
					mLastUpdateTime = current;
				}
				break;
			case ANIMATION_STEP_SECOND :
				mNeedChange = !mNeedChange;
				if (!mNeedChange && mShapeIndex != SHAPE_INDEX_TRACE_TWO
						&& mShapeIndex != SHAPE_INDEX_TRACE_HEART) {
					return;
				}
				if (mTraceIndex < mTraceXs.length - 1) {
					mTraceIndex += 1;
					mDrivenable.mSpeedX = mTraceXs[mTraceIndex] - mTraceXs[mTraceIndex - 1];
					mDrivenable.mSpeedY = mTraceYs[mTraceIndex] - mTraceYs[mTraceIndex - 1];
					mDrivenable.mX = mTraceXs[mTraceIndex];
					mDrivenable.mY = mTraceYs[mTraceIndex];
				} else {
					mCalcTempStartPoint.x = mTraceXs[mTraceXs.length - 1];
					mCalcTempStartPoint.y = mTraceYs[mTraceYs.length - 1];
					mCalcTempEndPoint.x = mEndPoint.x;
					mCalcTempEndPoint.y = mEndPoint.y;
					mDrivenable.mSpeedX = mCalcTempEndPoint.x - mCalcTempStartPoint.x;
					mDrivenable.mSpeedY = mCalcTempEndPoint.y - mCalcTempStartPoint.y;
					mAnimationStep = ANIMATION_STEP_THIRD;
					mDrivenable.mX = mCalcTempStartPoint.x;
					mDrivenable.mY = mCalcTempStartPoint.y;
				}
				if (mTraceIndex > 1 && mTraceIndex < mTraceXs.length - 1) {
					mDrivenable.mAngle = AngleAndPointUtils.angleOnAverage(
							mTraceXs[mTraceIndex - 1], mTraceYs[mTraceIndex - 1],
							mTraceXs[mTraceIndex], mTraceYs[mTraceIndex],
							mTraceXs[mTraceIndex + 1], mTraceYs[mTraceIndex + 1]);
				}
				mLastUpdateTime = current;
				mContinuedAlpha = MAX_ALPHA;
				break;
			case ANIMATION_STEP_THIRD :
				t = current - mLastUpdateTime;
				t = t / END_ANIAMTION_TIME;
				if (t <= 1.0f) {
					Point outPoint = new Point();
					LineUtils.getInterpolatorPoint(mCalcTempStartPoint, mCalcTempEndPoint,
							outPoint, t);
					mDrivenable.mX = outPoint.x;
					mDrivenable.mY = outPoint.y;
					mDrivenable.mSpeedX = mEndPoint.x - outPoint.x;
					mDrivenable.mSpeedY = mEndPoint.y - outPoint.y;
					nextAngleState();
				} else {
					mCalcTempStartPoint.x = (int) (mDrivenable.mX = mStartPoint.x);
					mCalcTempStartPoint.y = (int) (mDrivenable.mY = mStartPoint.y);
					mCalcTempEndPoint.x = mTraceXs[0];
					mCalcTempEndPoint.y = mTraceYs[0];
					mAnimationStep = ANIMATION_STEP_FIRST;
					mLastUpdateTime = current;
					mActive = false;
					mStartContinuedTime = current;
					mNextAnimationDelay = mRandom.nextInt(ANIMATION_DELAY / 2) + ANIMATION_DELAY
							/ 2;
				}
				mContinuedAlpha = MAX_ALPHA;
				break;
			default :
				break;
		}
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	// 新增的样式的逻辑
	private boolean mNeedChange;
	private volatile int mStartTraceIndex = START_TRACE_INDEX_HEART;
	private volatile int mShapeIndex;
	private static final int START_TRACE_INDEX_HEART = 11;
	private static final int START_TRACE_INDEX_TWO = 35;
	private static final int START_TRACE_INDEX_GO = 1;
	private static final int SHAPE_INDEX_TRACE_HEART = 0;
	private static final int SHAPE_INDEX_TRACE_GO = 1;
	private static final int SHAPE_INDEX_TRACE_TWO = 2;
	private int[] mTraceXs1;
	private int[] mTraceYs1;
	private int[] mTraceXs2;
	private int[] mTraceYs2;
	private int[] mTraceXs3;
	private int[] mTraceYs3;
	private SparseArray<Point> mIndexToPosition1;
	private SparseArray<Point> mPrePosition1;
	private SparseArray<Point> mNextPosition1;
	private SparseArray<Bitmap> mDrawBitmaps1;
	private SparseArray<Point> mIndexToPosition2;
	private SparseArray<Point> mPrePosition2;
	private SparseArray<Point> mNextPosition2;
	private SparseArray<Bitmap> mDrawBitmaps2;
	private SparseArray<Point> mIndexToPosition3;
	private SparseArray<Point> mPrePosition3;
	private SparseArray<Point> mNextPosition3;
	private SparseArray<Bitmap> mDrawBitmaps3;
	private TraceDataProvider mProvider;
	private Point mTraceTwoEndPoint;
	private Point mTraceGoEndPoint;
	private Point mTraceHeartEndPoint;
	private Point mTraceTwoStartPoint;
	private Point mTraceGoStartPoint;
	private Point mTraceHeartStartPoint;

	private void initDrawData(int[] traceXs, int[] traceYs, SparseArray<Point> prePosition,
			SparseArray<Point> nextPosition, SparseArray<Bitmap> drawBitmaps,
			SparseArray<Point> indexToPosition) {
		for (int i = 0; i < traceXs.length; i++) {
			Point drawPoint = new Point(0, 0);
			mCurrentTracePoint.x = traceXs[i];
			mCurrentTracePoint.y = traceYs[i];
			if (AngleAndPointUtils.isLongerThan(mLastDrawPoint, mCurrentTracePoint,
					mBitmap.getHeight() / 2)) {
				drawPoint.x = (traceXs[i] + mLastDrawPoint.x) / 2;
				drawPoint.y = (traceYs[i] + mLastDrawPoint.y) / 2;
				Point prePoint = new Point(mLastDrawPoint.x, mLastDrawPoint.y);
				prePosition.put(i, prePoint);
				Point nextPoint = new Point(mCurrentTracePoint.x, mCurrentTracePoint.y);
				nextPosition.put(i, nextPoint);
				mLastDrawPoint.x = traceXs[i];
				mLastDrawPoint.y = traceYs[i];
				if (mBitmapIndex >= mBitmapLength) {
					mBitmapIndex = 0;
				}
				if (mBitmaps != null) {
					drawBitmaps.put(i, mBitmaps[mBitmapIndex++]);
				}
			}
			indexToPosition.put(i, drawPoint);
		}
	}

	private void initData() {
		mShapeIndex = SHAPE_INDEX_TRACE_HEART;
		mProvider = TraceDataProvider.getProvider();
		mTraceXs = mTraceXs1 = mProvider.getTraceXs1();
		mTraceYs = mTraceYs1 = mProvider.getTraceYs1();
		mTraceXs2 = mProvider.getTraceXs2();
		mTraceYs2 = mProvider.getTraceYs2();
		mTraceXs3 = mProvider.getTraceXs3();
		mTraceYs3 = mProvider.getTraceYs3();

		int screenWidth = DrawUtils.getScreenViewWidth();
		int screenHeight = DrawUtils.getScreenViewHeight();
		prepareData(screenWidth, screenHeight, mTraceXs1, mTraceYs1);
		prepareData(screenWidth, screenHeight, mTraceXs2, mTraceYs2);
		prepareData(screenWidth, screenHeight, mTraceXs3, mTraceYs3);
		mStartPoint = new Point();
		mStartPoint.x = (int) (screenWidth * -0.2f);
		mStartPoint.y = (int) (screenHeight >> 1);
		mEndPoint = new Point();
		mTraceGoEndPoint = new Point();
		mTraceTwoEndPoint = new Point();
		mTraceTwoStartPoint = new Point();
		mTraceHeartStartPoint = new Point();
		mTraceGoStartPoint = new Point();
		mTraceHeartEndPoint = new Point();
		mTraceTwoEndPoint.x = mTraceGoEndPoint.x = mEndPoint.x = (int) (screenWidth * (1 + 0.1f));
		mTraceTwoEndPoint.y = mTraceGoEndPoint.y = mEndPoint.y = (int) (screenHeight >> 1);
		mTraceHeartEndPoint.x = (int) (screenWidth * (1 + 0.1f));
		mTraceHeartEndPoint.y = (int) (screenHeight * 0.48);
		mTraceTwoStartPoint.x = mStartPoint.x;
		mTraceTwoStartPoint.y = (int) (screenHeight * 2 / 3);
		mTraceGoStartPoint.x = screenWidth;
		mTraceGoStartPoint.y = (int) (-screenHeight * 0.2f);
		mTraceHeartStartPoint.x = mStartPoint.x;
		mTraceHeartStartPoint.y = (int) (screenHeight * 2 / 3);
		mLastDrawPoint = new Point(mTraceXs[0], mTraceYs[0]);
		mCurrentTracePoint = new Point();
		mIndexToPosition = new SparseArray<Point>();
		mPrePosition = new SparseArray<Point>();
		mNextPosition = new SparseArray<Point>();
		mDrawBitmaps = new SparseArray<Bitmap>();
		mPrePosition1 = new SparseArray<Point>();
		mNextPosition1 = new SparseArray<Point>();
		mIndexToPosition1 = new SparseArray<Point>();
		mDrawBitmaps1 = new SparseArray<Bitmap>();
		mPrePosition2 = new SparseArray<Point>();
		mNextPosition2 = new SparseArray<Point>();
		mIndexToPosition2 = new SparseArray<Point>();
		mDrawBitmaps2 = new SparseArray<Bitmap>();
		mPrePosition3 = new SparseArray<Point>();
		mNextPosition3 = new SparseArray<Point>();
		mIndexToPosition3 = new SparseArray<Point>();
		mDrawBitmaps3 = new SparseArray<Bitmap>();
		initDrawData(mTraceXs1, mTraceYs1, mPrePosition1, mNextPosition1, mDrawBitmaps1,
				mIndexToPosition1);
		initDrawData(mTraceXs2, mTraceYs2, mPrePosition2, mNextPosition2, mDrawBitmaps2,
				mIndexToPosition2);
		initDrawData(mTraceXs3, mTraceYs3, mPrePosition3, mNextPosition3, mDrawBitmaps3,
				mIndexToPosition3);
		mPrePosition = mPrePosition1;
		mNextPosition = mNextPosition1;
		mDrawBitmaps = mDrawBitmaps1;
		mIndexToPosition = mIndexToPosition1;
	}

	private void prepareData(int screenWidth, int screenHeight, int[] traceXs, int[] traceYs) {
		float widthScale = screenWidth / DEFAULT_SCREEN_WIDTH;
		float heightScale = screenHeight / DEFAULT_SCREEN_HEIGHT;
		widthScale = widthScale < heightScale ? widthScale : heightScale;
		for (int i = 0; i < traceXs.length; i++) {
			traceXs[i] = (int) (traceXs[i] * widthScale);
		}
		for (int i = 0; i < traceYs.length; i++) {
			traceYs[i] = (int) (traceYs[i] * widthScale);
		}
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		mBitmaps = actionBitmaps;
		for (int i = 0; i < mTraceXs1.length; i++) {
			if (mBitmapIndex >= mBitmapLength) {
				mBitmapIndex = 0;
			}
			if (mBitmaps != null) {
				mDrawBitmaps.put(i, mBitmaps[mBitmapIndex++]);
			}
		}
		mDrawBitmaps3 = mDrawBitmaps2 = mDrawBitmaps1 = mDrawBitmaps;
	}

	@Override
	public void cleanUp() {
		mBitmap = null;
		mBitmaps = null;
		mIndexToPosition.clear();
		mNextPosition.clear();
		mPrePosition.clear();
		mPrePosition = null;
		mNextPosition = null;
		mTraceHeartStartPoint = null;
		mTraceGoStartPoint = null;
		mTraceTwoStartPoint = null;
		mTraceHeartEndPoint = null;
		mTraceGoEndPoint = null;
		mTraceTwoEndPoint = null;
		mProvider = null;
		mDrawBitmaps3 = null;
		mNextPosition3 = null;
		mPrePosition3 = null;
		mIndexToPosition3 = null;
		mDrawBitmaps2 = null;
		mNextPosition2 = null;
		mPrePosition2 = null;
		mIndexToPosition2 = null;
		mDrawBitmaps1 = null;
		mNextPosition1 = null;
		mPrePosition1 = null;
		mIndexToPosition1 = null;
		mTraceYs3 = null;
		mTraceXs3 = null;
		mTraceYs2 = null;
		mTraceXs2 = null;
		mTraceYs1 = null;
		mTraceXs1 = null;
	}
}
