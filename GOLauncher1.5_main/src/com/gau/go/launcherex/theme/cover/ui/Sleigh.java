package com.gau.go.launcherex.theme.cover.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.gau.go.launcherex.theme.cover.CoverBitmapLoader;
import com.gau.go.launcherex.theme.cover.DrawUtils;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * 雪橇车（包含鹿圣诞老人等）
 * 
 * @author chenqiang
 * 
 */
public class Sleigh extends Drivenable implements IMovable, IOutWather,
		OnResponTouchListener {

	private static final byte[] MOVE_POINT_MUTEX = new byte[0];
	private static final int LINE_FROM_LEFT_TO_RIGHT = 0;
	private static final int LINE_FROM_RIGHT_TO_LEFT = 1;
	private static final float SIN_DIFF = 0.8945f;
	private static final float SIN_PEEK = 40.0f;
	private static final float SIN_DIFF_Y_RATE = 0.2f;
	private static final float DOUBLE_DEER_DIFF_X = 20.0f;
	private static final float DOUBLE_DEER_DIFF_Y = 20.0f;

	private final static float RATE_OF_START_X = 1.0f;
	private final static float RATE_OF_START_UP_Y = 0.4f;
	private final static float RATE_OF_START_BOTTOM_Y = 0.7f;
	private final static int sSPEED_OF_START_X = 2;
	private final static int sDEER_PADDING = -25;
	private final static int SHAKE_SPEED = 110;

	private static final int ANIMATION_INTERVAL = 200;
	private static final int TAIL_SANTA_DIFF_X = 27;
	private static final int TAIL_SANTA_DIFF_Y = 16;

	private Random mRandom = new Random();
	private long mPointLastAnimateUpdateTime;

	private Bitmap[] mDeerBitmap;  //鹿的位图
	private Bitmap[] mSantaBitmap;
	private Bitmap[] mWhipBitmap;
	private Bitmap[] mFlagBitmap;
	private Bitmap[] mFlagOppositeBitmap;
	private Bitmap[] mGiftBitmap;
	private Bitmap[] mWavingBitmap;
	private Bitmap[] mJumpingBitmap;
	private Bitmap[] mBlastBitmap;
	private Bitmap[] mPointBitmap;
	private Bitmap[] mPackageBitmap;
	private Bitmap mSleighBitmap;
	private Bitmap[] mFallSantaBitmap;
	private Bitmap[] mRunSantaBitmap;
	private Bitmap[] mUpSantaBitmap;

	private Matrix mMatrix;
	private int mScreenWidth;
	private int mScreenHeight;

	private Deer mDeer1;
	private Deer mDeer2;
	private Deer mDeer11;
	private Deer mDeer22;
	private Santa mSanta;

	private List<MovePoint> mMovePointList = new ArrayList<MovePoint>();
	private List<MovePoint> mMovePointInvisibleList = new ArrayList<MovePoint>();
	private int mCycleIndex = 0;

	private boolean mIsPort;

	public Sleigh(CoverBitmapLoader bitmapLoader, int screenWidth,
			int screenHeight, Bitmap[] deerBitmap, Bitmap[] santaBitmap,
			Bitmap[] whipBitmap, Bitmap[] flagBitmap,
			Bitmap[] flagOppositeBitmap, Bitmap[] giftBitmap,
			Bitmap[] wavingBitmap, Bitmap[] jumpingBitmap,
			Bitmap[] blastBitmap, Bitmap[] pointBitmap,
			Bitmap[] fallSantaBitmap, Bitmap[] upSantaBitmap,
			Bitmap[] runSantaBitmap, Bitmap[] emptySleighBitmap,
			Bitmap[] packageBitmap) {
		setScale(); // 设置缩放率
		mMatrix = new Matrix();
		mMatrix.setValues(new float[] { -1.0f, 0, 0, 0, 1.0f, 0, 0, 0, 1.0f });
		// getBitmaps(bitmapLoader);
		mDeerBitmap = deerBitmap;
		mSantaBitmap = santaBitmap;
		mWhipBitmap = whipBitmap;
		mFlagBitmap = flagBitmap;
		mFlagOppositeBitmap = flagOppositeBitmap;
		mGiftBitmap = giftBitmap;
		mWavingBitmap = wavingBitmap;
		mJumpingBitmap = jumpingBitmap;
		mBlastBitmap = blastBitmap;
		mPointBitmap = pointBitmap;
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mFallSantaBitmap = fallSantaBitmap;
		mRunSantaBitmap = runSantaBitmap;
		mUpSantaBitmap = upSantaBitmap;
		mSleighBitmap = emptySleighBitmap[0];
		mPackageBitmap = packageBitmap;

		mDeer1 = new Deer(mDeerBitmap, mMatrix, mScreenWidth, mScreenHeight);
		mDeer2 = new Deer(mDeerBitmap, mMatrix, mScreenWidth, mScreenHeight);
		mDeer11 = new Deer(mDeerBitmap, mMatrix, mScreenWidth, mScreenHeight);
		mDeer22 = new Deer(mDeerBitmap, mMatrix, mScreenWidth, mScreenHeight);
		mSanta = new Santa(mSantaBitmap, mWhipBitmap, mFlagBitmap,
				mFlagOppositeBitmap, mGiftBitmap, mWavingBitmap,
				mJumpingBitmap, mBlastBitmap, mPointBitmap, mMatrix,
				mScreenWidth, mScreenHeight, mSleighBitmap, mPackageBitmap,
				mFallSantaBitmap, mRunSantaBitmap, mUpSantaBitmap);
		mSanta.setWatcher(this);
		setDefaultPic();
		mDeer1.mScale = mScale;
		mDeer2.mScale = mScale;
		mDeer11.mScale = mScale;
		mDeer22.mScale = mScale;
		mSanta.mScale = mScale;

		getMoveLine();

		mPointLastAnimateUpdateTime = System.currentTimeMillis();
		mIsPort = DrawUtils.isPort();

	}

	// 设置初始图片
	private void setDefaultPic() {
		mDeer1.mBitmap = mDeerBitmap[0];
		mDeer2.mBitmap = mDeerBitmap[0];
		mDeer11.mBitmap = mDeerBitmap[0];
		mDeer22.mBitmap = mDeerBitmap[0];
		mSanta.mBitmap = mSantaBitmap[2]; // 圣诞老人初始图片
		mSanta.mWBitmap = mWhipBitmap[0]; // 鞭子初始图片
		mSanta.mFBitmap = mFlagBitmap[0]; // 旗子初始图片
	}

	public void onConfigurationChanged(int width, int height) {
		final float widthOld = mScreenWidth;
		final float heightOld = mScreenHeight;
		mScreenWidth = width;
		mScreenHeight = height;

		if (mDeer1 != null && mDeer2 != null && mDeer11 != null
				&& mDeer22 != null && mSanta != null) {
			mDeer1.onConfigurationChanged(width, height);
			mDeer2.onConfigurationChanged(width, height);
			mDeer11.onConfigurationChanged(width, height);
			mDeer22.onConfigurationChanged(width, height);
			mSanta.onConfigurationChanged(width, height);

			float startY = (mDeer1.mStarY / heightOld) * mScreenHeight;
			float x;
			if (mDeer1.mSpeedX > 0) {
				x = mDeer1.mX;
				mDeer1.mX = (mDeer1.mX / widthOld) * width;
				mDeer1.mY = (float) ((startY + SIN_PEEK
						* mScale
						* (float) Math
								.sin(((mX / SIN_DIFF) % (mScreenWidth / SIN_DIFF))
										/ (mScreenWidth / SIN_DIFF)
										* 2
										* Math.PI))
						* SIN_DIFF - mX * SIN_DIFF_Y_RATE);
				mDeer1.mStarY = startY;

				mDeer11.mX = mDeer1.mX + DOUBLE_DEER_DIFF_X * mScale;
				mDeer11.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
				mDeer11.mStarY = startY;

				mDeer22.mX = mDeer1.mX - mDeer22.mBitmap.getWidth()
						- sDEER_PADDING * mScale + DOUBLE_DEER_DIFF_X * mScale;
				mDeer22.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
				mDeer22.mStarY = startY;

				mDeer2.mX = mDeer1.mX - mDeer2.mBitmap.getWidth()
						- sDEER_PADDING * mScale;
				mDeer2.mY = mDeer1.mY;
				mDeer2.mStarY = startY;

				mSanta.mX = mDeer2.mX - mSanta.mBitmap.getWidth();
				mSanta.mY = mDeer2.mY;
				mSanta.mStarY = startY;

			} else {
				x = mDeer1.mX;
				mDeer1.mX = (mDeer1.mX / widthOld) * width;
				mDeer1.mY = (float) ((startY + SIN_PEEK
						* mScale
						* (float) Math
								.sin(((mX / SIN_DIFF) % (mScreenWidth / SIN_DIFF))
										/ (mScreenWidth / SIN_DIFF)
										* 2
										* Math.PI))
						* SIN_DIFF - (mScreenWidth - mX) * SIN_DIFF_Y_RATE);
				mDeer1.mStarY = startY;

				mDeer11.mX = mDeer1.mX + DOUBLE_DEER_DIFF_X * mScale;
				mDeer11.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
				mDeer11.mStarY = startY;

				mDeer22.mX = mDeer1.mX + mDeer22.mBitmap.getWidth()
						+ DOUBLE_DEER_DIFF_X * mScale + sDEER_PADDING * mScale;
				mDeer22.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
				mDeer22.mStarY = startY;

				mDeer2.mX = mDeer1.mX + mDeer2.mBitmap.getWidth()
						+ sDEER_PADDING * mScale;
				mDeer2.mY = mDeer1.mY;
				mDeer2.mStarY = startY;

				mSanta.mX = mDeer2.mX + mDeer2.mBitmap.getWidth();
				mSanta.mY = mDeer2.mY;
				mSanta.mStarY = startY;
			}
		}
        synchronized (MOVE_POINT_MUTEX) {
        	for (MovePoint movePoint : mMovePointList) {
        		movePoint.mAlive = false;
        	}
		}
	}

	public void resetData(CoverBitmapLoader bitmapLoader) {
		prepareBitmaps(bitmapLoader);
		setDefaultPic();
	}

	private void prepareBitmaps(CoverBitmapLoader bitmapLoader) {
		for (int i = 0; i < mSantaBitmap.length; i++) {
			mDeerBitmap[i] = bitmapLoader.getCoverBitmap("reindeer" + (i + 1));
			mSantaBitmap[i] = bitmapLoader.getCoverBitmap("sleigh" + (i + 1));
		}
		for (int i = 0; i < mWhipBitmap.length; i++) {
			mWhipBitmap[i] = bitmapLoader.getCoverBitmap("whip" + (i + 1));
		}
		for (int i = 0; i < mFlagBitmap.length; i++) {
			mFlagBitmap[i] = bitmapLoader.getCoverBitmap("flag" + (i + 1));
		}
		for (int i = 0; i < mFlagOppositeBitmap.length; i++) {
			mFlagOppositeBitmap[i] = bitmapLoader.getCoverBitmap("flagfan"
					+ (i + 1));
		}
		for (int i = 0; i < mGiftBitmap.length; i++) {
			mGiftBitmap[i] = bitmapLoader.getCoverBitmap("gift" + (i + 1));
		}
		for (int i = 0; i < mWavingBitmap.length; i++) {
			mWavingBitmap[i] = bitmapLoader.getCoverBitmap("waving" + (i + 1));
		}
		for (int i = 0; i < mJumpingBitmap.length; i++) {
			mJumpingBitmap[i] = bitmapLoader
					.getCoverBitmap("jumping" + (i + 1));
		}
		for (int i = 0; i < mBlastBitmap.length; i++) {
			mBlastBitmap[i] = bitmapLoader.getCoverBitmap("blast" + (i + 1));
		}
		for (int i = 0; i < mPointBitmap.length; i++) {
			mPointBitmap[i] = bitmapLoader.getCoverBitmap("point" + (i + 1));
		}

	}

	/**
	 * 获取图片资源
	 * 
	 * @param bitmapLoader
	 */
	private void getBitmaps(CoverBitmapLoader bitmapLoader) {
		prepareBitmaps(bitmapLoader);
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		long current = System.currentTimeMillis();
		if (current - mPointLastAnimateUpdateTime >= ANIMATION_INTERVAL) {
			mPointLastAnimateUpdateTime = current;
			initMovePoint(mSanta.mX + mSanta.mBitmap.getWidth()
					- TAIL_SANTA_DIFF_X * mScale,
					mSanta.mY + mSanta.mBitmap.getHeight() - TAIL_SANTA_DIFF_Y
							* mScale);
		}
		for (MovePoint movePoint : mMovePointList) {
			if (movePoint.mAlive) {
				movePoint.doDraw(camera, matrix, canvas, paint);
			} else {
				mMovePointInvisibleList.add(movePoint);
			}
		}
		synchronized (MOVE_POINT_MUTEX) {
			for (MovePoint movePoint : mMovePointInvisibleList) {
				mMovePointList.remove(movePoint);
				movePoint.cleanUp();
				movePoint = null;
			}
			mMovePointInvisibleList.clear();
		}
		mDeer1.doDraw(camera, matrix, canvas, paint);
		mDeer2.doDraw(camera, matrix, canvas, paint);

		mDeer11.doDraw(camera, matrix, canvas, paint);
		mDeer22.doDraw(camera, matrix, canvas, paint);

		mSanta.doDraw(camera, matrix, canvas, paint);

	}

	@Override
	public void cleanUp() {
		if (mDeer1 != null) {
			mDeer1.cleanUp();
//			mDeer1 = null;
		}
		if (mDeer2 != null) {
			mDeer2.cleanUp();
//			mDeer2 = null;
		}
		if (mDeer11 != null) {
			mDeer11.cleanUp();
//			mDeer11 = null;
		}
		if (mDeer22 != null) {
			mDeer22.cleanUp();
//			mDeer22 = null;
		}
		if (mSanta != null) {
			mSanta.cleanUp();
//			mSanta = null;
		}
		if (mMovePointList != null) {
			mMovePointList.clear();
//			mMovePointList = null;
		}
		if (mMovePointInvisibleList != null) {
			mMovePointInvisibleList.clear();
//			mMovePointInvisibleList = null;
		}
		
//		mDeerBitmap = null;
//		mSantaBitmap = null;
//		mWhipBitmap = null;
//		mFlagBitmap = null;
//		mFlagOppositeBitmap = null;
//		mGiftBitmap = null;
//		mWavingBitmap = null;
//		mJumpingBitmap = null;
//		mBlastBitmap = null;
//		mPointBitmap = null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mSanta.onTouchEvent(event);
	}

	@Override
	public void moving() {
		try {
			mDeer1.moving();
			mDeer2.moving();
			mDeer11.moving();
			mDeer22.moving();
			mSanta.moving();
			synchronized (MOVE_POINT_MUTEX) {
				for (MovePoint movePoint : mMovePointList) {
					if (movePoint.mAlive) {
						movePoint.moving();
					}
				}
			}
		} catch (Exception e) {

		}

	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

	@Override
	public boolean checkOutOfBound() {
		return false;
	}

	@Override
	public void haveOut(boolean out) {
		if (out) {
			mCycleIndex++;
			if (mCycleIndex >= 2) {
				mCycleIndex = 0;
				getMoveLine();
				return;
			}
			if (mSanta.mSpeedX < 0) {
				fromLeftToRight();
			} else {
				fromRightToLeft();
			}

		}
	}

	/**
	 * 从左边到右边
	 */
	private void fromLeftToRight() {
		mDeer1.mSpeedX = sSPEED_OF_START_X * mScale;
		mDeer11.mSpeedX = sSPEED_OF_START_X * mScale;
		mDeer2.mSpeedX = mDeer1.mSpeedX;
		mDeer22.mSpeedX = mDeer1.mSpeedX;
		mSanta.mSpeedX = mDeer2.mSpeedX;

		if (mCycleIndex == 1) {
			mDeer1.mY = mScreenHeight * RATE_OF_START_UP_Y;
			mDeer1.mStarY = mDeer1.mY;
		} else {
			mDeer1.mY = mScreenHeight * RATE_OF_START_BOTTOM_Y;
			mDeer1.mStarY = mDeer1.mY;
		}
		mDeer1.mX = -mDeer1.mBitmap.getWidth();

		mDeer2.mX = mDeer1.mX - mDeer2.mBitmap.getWidth() - sDEER_PADDING
				* mScale;
		mDeer2.mY = mDeer1.mY;
		mDeer2.mStarY = mDeer2.mY;

		mDeer11.mX = mDeer1.mX + DOUBLE_DEER_DIFF_X * mScale;
		mDeer11.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
		mDeer11.mStarY = mDeer11.mY;

		mDeer22.mX = mDeer1.mX - mDeer22.mBitmap.getWidth() - sDEER_PADDING
				* mScale + DOUBLE_DEER_DIFF_X * mScale;
		mDeer22.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
		mDeer22.mStarY = mDeer22.mY;

		mSanta.mX = mDeer2.mX - mSanta.mBitmap.getWidth();
		mSanta.mY = mDeer2.mY;
		mSanta.mStarY = mSanta.mY;
	}

	/**
	 * 从右边到左边
	 */
	private void fromRightToLeft() {
		mDeer1.mSpeedX = -sSPEED_OF_START_X * mScale;
		mDeer2.mSpeedX = mDeer1.mSpeedX;
		mSanta.mSpeedX = mDeer2.mSpeedX;

		mDeer11.mSpeedX = mDeer1.mSpeedX;
		mDeer22.mSpeedX = mDeer1.mSpeedX;

		mDeer1.mX = (float) mScreenWidth * RATE_OF_START_X;
		if (mCycleIndex == 1) {
			mDeer1.mY = mScreenHeight * RATE_OF_START_UP_Y;
			mDeer1.mStarY = mDeer1.mY;
		} else {
			mDeer1.mY = mScreenHeight * RATE_OF_START_BOTTOM_Y;
			mDeer1.mStarY = mDeer1.mY;
		}

		mDeer2.mX = mDeer1.mX + mDeer2.mBitmap.getWidth() + sDEER_PADDING
				* mScale;
		mDeer2.mY = mDeer1.mY;
		mDeer2.mStarY = mDeer2.mY;

		mDeer11.mX = mDeer1.mX + DOUBLE_DEER_DIFF_X * mScale;
		mDeer11.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
		mDeer11.mStarY = mDeer11.mY;

		mDeer22.mX = mDeer1.mX + mDeer22.mBitmap.getWidth()
				+ DOUBLE_DEER_DIFF_X * mScale + sDEER_PADDING * mScale;
		mDeer22.mY = mDeer1.mY - DOUBLE_DEER_DIFF_Y * mScale;
		mDeer22.mStarY = mDeer22.mY;

		mSanta.mX = mDeer2.mX + mDeer2.mBitmap.getWidth();
		mSanta.mY = mDeer2.mY;
		mSanta.mStarY = mSanta.mY;
	}

	private void getMoveLine() {
		final int line = mRandom.nextInt(2);
		if (line == LINE_FROM_LEFT_TO_RIGHT) {
			fromLeftToRight();
		} else {
			fromRightToLeft();
		}
	}

	private void setScale() {
		final int srcWidth = DrawUtils.isPort() ? 480 : 800;
		// 用屏幕宽作为缩放比例参照
		mScale = (DrawUtils.getScreenViewWidth() + 0.1f) / srcWidth;
	}

	private void initMovePoint(float x, float y) {
		// TODO 魔法数字解决
		int time;
		if (mSanta.mSpeedX > 0) {
			x = x - mSanta.mBitmap.getWidth() + TAIL_SANTA_DIFF_X * mScale;
			x = x + TAIL_SANTA_DIFF_Y * mScale;
		}
		if (x > 0 && y > 0) {
			for (int i = 0; i < 8; i++) {
				float px = getFloatRandom(x - 5 * mScale, x + 5 * mScale);
				float py = getFloatRandom(y - 6 * mScale, y + 6 * mScale);
				time = (int) getFloatRandom(2600, 3800);
				mMovePointList.add(new MovePoint(mPointBitmap, px, py, time, 2,
						255));
			}
			for (int i = 0; i < 3; i++) {
				float px = getFloatRandom(x - 5 * mScale, x + 5 * mScale);
				float py = getFloatRandom(y - 12 * mScale, y - 8 * mScale);
				time = (int) getFloatRandom(2000, 2800);
				mMovePointList.add(new MovePoint(mPointBitmap, px, py, time, 2,
						255));
			}
			for (int i = 0; i < 4; i++) {
				float px = getFloatRandom(x - 5 * mScale, x + 5 * mScale);
				float py = getFloatRandom(y - 18 * mScale, y - 10 * mScale);
				time = (int) getFloatRandom(1000, 2000);
				mMovePointList.add(new MovePoint(mPointBitmap, px, py, time,
						(int) getFloatRandom(1, 3), 255));
			}

			for (int i = 0; i < 6; i++) {
				float px = getFloatRandom(x - 5 * mScale, x + 5 * mScale);
				float py = getFloatRandom(y + 8 * mScale, y + 12 * mScale);
				time = (int) getFloatRandom(2000, 2800);
				mMovePointList.add(new MovePoint(mPointBitmap, px, py, time, 2,
						mRandom.nextInt(100)));
			}

			for (int i = 0; i < 6; i++) {
				float px = getFloatRandom(x - 5 * mScale, x + 5 * mScale);
				float py = getFloatRandom(y + 10 * mScale, y + 18 * mScale);
				time = (int) getFloatRandom(1000, 2000);
				mMovePointList.add(new MovePoint(mPointBitmap, px, py, time,
						(int) getFloatRandom(1, 3), mRandom.nextInt(100)));
			}
		}

	}

	private float getFloatRandom(float min, float max) {
		float ret = 0;
		while (ret < min) {
			ret = mRandom.nextInt((int) (max * 100)) / 100;
		}
		return ret;
	}

	/**
	 * 响应甩动事件
	 * 
	 * @param speed
	 */
	public void handShake(float speed) {
		if (speed > SHAKE_SPEED) {
			mSanta.handShake(speed);
		}

	}

}
