package com.gau.go.launcherex.theme.cover.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.gau.go.launcherex.theme.cover.utils.AngleAndPointUtils;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * 圣诞老人的类
 * 
 * @author chenqiang
 * 
 */
public class Santa extends Drivenable implements IMovable,
		OnResponTouchListener, IReachAreaWatcher {

	private static final int RABBIT_TYLE_WAVING = 0; // 摆手兔子类型
	private static final int RABBIT_TYLE_JUMPING = 1; // 跳跃兔子类型
	private static final int GIFT_SPEED_X = 0; // 礼物的X速度
	private static final int GIFT_SPEED_Y = 3; // 礼物的Y速度
	private static final int MAX_COUNT_OF_RABBIT = 2; // 同时显示最大的兔子数量
	private static final int ANIMATION_INTERVAL = 100; // 动画的时间间隔
	private static final int FALLING_ANIMATION_INTERVAL = 60; // 圣诞老人掉下来的动画时间间隔
	private static final int UP_ANIMATION_INTERVAL = 250; // 圣诞老人坐起来的时间间隔
	private static final int RUN_ANIMATION_INTERVAL = 60; // 圣诞老人奔跑的时间间隔

	private static final float SIN_DIFF = 0.8945f; // sin计算参数
	private static final float SIN_PEEK = 40.0f; // sin的峰值
	private static final float SIN_DIFF_Y = 0.2f; // sin曲线相对Y的偏离
	private static final float WIHP_SANTA_DIFF_X = 8; // 鞭子相对圣诞老人的X调整
	private static final float WIHP_SANTA_DIFF_Y = 13; // 鞭子相对圣诞老人的Y调整
	private static final float FLAG_SANTA_DIFF_X = 27; // 旗子相对圣诞老人的X调整

	private Matrix mMatrix;
	private Random mRandom = new Random();

	private long mLastAnimateUpdateTime; // 上一次雪桥车（包括鹿等元素）动画变化的时刻
	private long mLastWhipUpdateTime; // 上一次鞭子动画变化的时刻
	private long mLastFallingUpdateTime; // 上一次圣诞老人坠落动画变化的时刻
	private long mLastRunUpdateTime; // 上一次圣诞奔跑动画变化的时刻
	private long mLastUpUpdateTime; // 上一次圣诞奔跑动画变化的时刻

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
	private Bitmap[] mFallSantaBitmap;
	private Bitmap[] mRunSantaBitmap;
	private Bitmap[] mUpSantaBitmap;

	public Bitmap mWBitmap;
	public Bitmap mFBitmap;
	public Bitmap mFOBitmap;
	public Bitmap mSBitmap;
	public Bitmap mPBitmap;
	private Bitmap mSleighBitmap;

	private int mBitmapIndex = 0;
	private int mWBitmapIndex = 0;
	private int mFBitmapIndex = 0;
	private int mFOBitmapIndex = 0;
	private int mFAllBitmapIndex = 0;
	private int mRunBitmapIndex = 0;
	private int mUpBitmapIndex = 0;

	private boolean mIsClick = false;
	private boolean mIsFallAnimation = false;
	private boolean mStartTORunAnimation = false;

	private IOutWather mIOutWather;
	private int mScreenWidth;
	private int mScreenHeight;

	public float mStarX;
	public float mStarY;
	public float mSX;
	public float mSY;
	public float mPX;
	public float mPY;
	private float mFallSpeed = 0;
	private float mFallAcceleration = 0.3f;
	private int mShakeIndxe = 0;

	private List<Gift> mGiftList = new ArrayList<Gift>();
	private List<Rabbit> mRabbitList = new ArrayList<Rabbit>();

	public Santa(Bitmap[] santaBitmap, Bitmap[] whipBitmap,
			Bitmap[] flagBitmap, Bitmap[] flagOppositeBitmap,
			Bitmap[] giftBitmap, Bitmap[] wavingBitmap, Bitmap[] jumpingBitmap,
			Bitmap[] blastBitmap, Bitmap[] pointBitmap, Matrix matrix,
			int screenWidth, int screenHeight, Bitmap emptySleigh,
			Bitmap[] packageBitmap, Bitmap[] fallSantaBitmap,
			Bitmap[] runSantaBitmap, Bitmap[] upSantaBitmap) {

		mSantaBitmap = santaBitmap;
		mWhipBitmap = whipBitmap;
		mFlagBitmap = flagBitmap;
		mFlagOppositeBitmap = flagOppositeBitmap;
		mGiftBitmap = giftBitmap;
		mWavingBitmap = wavingBitmap;
		mJumpingBitmap = jumpingBitmap;
		mBlastBitmap = blastBitmap;
		mPointBitmap = pointBitmap;

		mSleighBitmap = emptySleigh;
		mPackageBitmap = packageBitmap;
		mFallSantaBitmap = fallSantaBitmap;
		mRunSantaBitmap = runSantaBitmap;
		mUpSantaBitmap = upSantaBitmap;

		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
		mMatrix = matrix;
		mLastWhipUpdateTime = System.currentTimeMillis();
		mSBitmap = fallSantaBitmap[0];
		mPBitmap = packageBitmap[0];
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {

		drawBitmap(mBitmap, mWBitmap, mFBitmap, false, camera, matrix, canvas,
				paint);

		if (mGiftList != null && mGiftList.size() > 0) {

			for (Gift gift : mGiftList) {
				if (gift.mAlive == true) {
					gift.doDraw(camera, matrix, canvas, paint);
				}
			}
		}

		if (mRabbitList != null && mRabbitList.size() > 0) {

			for (Rabbit rabbit : mRabbitList) {
				if (rabbit.mAlive == true) {
					rabbit.doDraw(camera, matrix, canvas, paint);
				}
			}
		}

		if (mIsFallAnimation && mSBitmap != null) {
			if (mPBitmap != null) { // 不对包裹绘制
				canvas.save();
				canvas.translate(mPX, mPY);
				canvas.drawBitmap(mPBitmap, matrix, paint);
				canvas.restore();
			}

			canvas.save();
			canvas.translate(mSX, mSY);
			if (mSpeedX > 0) {
				canvas.translate(mSBitmap.getWidth(), 0);
				canvas.concat(mMatrix);
			}
			canvas.drawBitmap(mSBitmap, matrix, paint);
			canvas.restore();
		}

	}

	@Override
	public void moving() {

		long current = System.currentTimeMillis();
		if (current - mLastAnimateUpdateTime >= ANIMATION_INTERVAL) {
			mLastAnimateUpdateTime = current;
			getMoveBitmap();
		}

		if (!mIsFallAnimation
				&& (mX > (mScreenWidth / 4 - 5 * mScale)
						&& mX < (mScreenWidth / 4 + 5 * mScale) || mX > (mScreenWidth / 2 - 5 * mScale)
						&& mX < (mScreenWidth / 2 + 5 * mScale))) {
			mIsClick = true;
		}

		float x = mX;
		float y = mY;

		double d = 0;

		mX += mSpeedX;
		if (mSpeedX < 0) {
			d = ((mX / SIN_DIFF) % (mScreenWidth / SIN_DIFF))
					/ (mScreenWidth / SIN_DIFF) * 2 * Math.PI;

			mY = (float) ((mStarY + SIN_PEEK * mScale * (float) Math.sin(d))
					* SIN_DIFF - (mScreenWidth - mX) * SIN_DIFF_Y);
			mDefaultAngle = AngleAndPointUtils
					.angleOnSpeed(mX - x, mY - y, -90);
		} else {
			d = ((mX / SIN_DIFF) % (mScreenWidth / SIN_DIFF))
					/ (mScreenWidth / SIN_DIFF) * 2 * Math.PI;
			mY = (float) ((mStarY + SIN_PEEK * mScale * (float) Math.sin(d))
					* SIN_DIFF - mX * SIN_DIFF_Y);
			mDefaultAngle = AngleAndPointUtils.angleOnSpeed(-(mX - x), mY - y,
					-90); // 镜像坐标取反
		}

		if (mGiftList != null && mGiftList.size() > 0) {

			for (Gift gift : mGiftList) {
				if (gift.mAlive == true) {
					gift.moving();
				}
			}
		}

		if (mRabbitList != null && mRabbitList.size() > 0) {

			for (Rabbit rabbit : mRabbitList) {
				if (rabbit.mAlive == true) {
					rabbit.moving();
				}
			}
		}

		if (mIsFallAnimation) { // 出界恢复默认状态
			if ((mX < -mFBitmap.getWidth() - mBitmap.getWidth() && mSpeedX < 0 && mSX < -mSBitmap
					.getWidth())
					|| (mX > mScreenWidth + mFBitmap.getWidth() && mSpeedX > 0)
					&& mSX > mScreenWidth + mSBitmap.getWidth()) {
				mIOutWather.haveOut(true);
				mBitmap = mSantaBitmap[2];
				mIsFallAnimation = false;
				mStartTORunAnimation = false;
				mFAllBitmapIndex = 0;
				mRunBitmapIndex = 0;
				mUpBitmapIndex = 0;
			}
		} else {
			if ((mX < -mFBitmap.getWidth() - mBitmap.getWidth() && mSpeedX < 0)
					|| (mX > mScreenWidth + mFBitmap.getWidth() && mSpeedX > 0)) {
				mIOutWather.haveOut(true);
				mBitmap = mSantaBitmap[2];
				mIsFallAnimation = false;
				mFAllBitmapIndex = 0;
				mRunBitmapIndex = 0;
				mUpBitmapIndex = 0;
			}
		}

		if (mIsFallAnimation) {
			if (mSY >= mScreenHeight - mSBitmap.getHeight()) {
				long currentU = System.currentTimeMillis();
				if (currentU - mLastUpUpdateTime >= UP_ANIMATION_INTERVAL) { // 起来的动作
					mLastUpUpdateTime = currentU;
					if (mUpBitmapIndex == 0) {
						if (mShakeIndxe < 1) {
							mShakeIndxe++;
							mSBitmap = mUpSantaBitmap[mUpBitmapIndex];
						} else {
							mShakeIndxe = 0;
							mSBitmap = mUpSantaBitmap[mUpBitmapIndex++];
						}
					} else if (mUpBitmapIndex == 1) {
						mPBitmap = null;
						mSBitmap = mUpSantaBitmap[mUpBitmapIndex++];
					} else if (mUpBitmapIndex < mUpSantaBitmap.length) { // 摇头的动作
						if (mUpBitmapIndex == 3 || mUpBitmapIndex == 4) {
							if (mShakeIndxe < 3) {
								mShakeIndxe++;
								if (mUpBitmapIndex == 4) {
									mSBitmap = mUpSantaBitmap[4];
									mUpBitmapIndex--;
								} else {
									mSBitmap = mUpSantaBitmap[3];
									mUpBitmapIndex++;
								}
							} else {
								mShakeIndxe = 0;
								mSBitmap = mUpSantaBitmap[mUpBitmapIndex++];
							}
						} else {
							mSBitmap = mUpSantaBitmap[mUpBitmapIndex++];
						}
					} else {
						mStartTORunAnimation = true;
					}
					mSY = mScreenHeight - mSBitmap.getHeight();
				}
			} else {
				long currentF = System.currentTimeMillis(); // 坠落的动作
				if (currentF - mLastFallingUpdateTime >= FALLING_ANIMATION_INTERVAL) {
					mLastFallingUpdateTime = currentF;
					if (mFAllBitmapIndex >= 4) {
						mFAllBitmapIndex = mFAllBitmapIndex - 2;
					} else {
						mSBitmap = mFallSantaBitmap[mFAllBitmapIndex++];
					}
				}
				mSY = mSY + mFallSpeed;
				mFallSpeed = mFallSpeed + mFallAcceleration;
				if (mPY > mScreenHeight - mPBitmap.getHeight()) {

				} else {
					mPY = mPY + mFallSpeed;
					mFallSpeed = mFallSpeed + mFallAcceleration;
				}
			}
		}

		if (mStartTORunAnimation) {
			long currentR = System.currentTimeMillis();
			if (currentR - mLastRunUpdateTime >= RUN_ANIMATION_INTERVAL) {
				mLastRunUpdateTime = currentR;
				if (mRunBitmapIndex >= mRunSantaBitmap.length) { // 奔跑的动作
					mRunBitmapIndex = 0;
					mSBitmap = mRunSantaBitmap[mRunBitmapIndex];
					mRunBitmapIndex++;
				} else {
					mSBitmap = mRunSantaBitmap[mRunBitmapIndex];
					mRunBitmapIndex++;
				}
				mSX = mSX + mSpeedX * 4;
				mSY = mScreenHeight - mSBitmap.getHeight();
			}
		}

	}

	@Override
	public boolean isTaped(float x, float y) {
		if (mBitmap != null) {
			return Math.abs(mX + mBitmap.getWidth() / 2 - x) <= mBitmap
					.getWidth() >> 1
					&& Math.abs(mY + mBitmap.getHeight() / 2 - y) <= mBitmap
							.getHeight() >> 1;

		}
		return false;
	}

	public void getMoveBitmap() {
		if (mIsClick) {
			if (mBitmapIndex == mSantaBitmap.length) {
				mBitmapIndex = 0;
				mBitmap = mSantaBitmap[2];
				mIsClick = false;
				createGift();
			} else {
				mBitmap = mSantaBitmap[mBitmapIndex++];
			}
		}

		long current = System.currentTimeMillis();
		if (current - mLastWhipUpdateTime >= ANIMATION_INTERVAL * 3) {
			if (mWBitmapIndex == mWhipBitmap.length) {
				mLastWhipUpdateTime = current;
				mWBitmapIndex = 0;
			}
			mWBitmap = mWhipBitmap[mWBitmapIndex++];
		}

		if (mSpeedX > 0) {
			if (mFOBitmapIndex == mFlagOppositeBitmap.length) {
				mFOBitmapIndex = 0;
			}
			mFBitmap = mFlagOppositeBitmap[mFOBitmapIndex++];
		} else {
			if (mFBitmapIndex == mFlagBitmap.length) {
				mFBitmapIndex = 0;
			}
			mFBitmap = mFlagBitmap[mFBitmapIndex++];
		}

	}

	private void drawBitmap(Bitmap bitmap, Bitmap wbitmap, Bitmap fbitmap,
			boolean needShadowNext, Camera camera, Matrix matrix,
			Canvas canvas, Paint paint) {
		if (bitmap != null) {
			canvas.save();
			canvas.translate(mX, mY);
			if (mSpeedX > 0) {
				canvas.translate(mBitmap.getWidth(), 0);
				canvas.concat(mMatrix);
			}
			canvas.rotate(mDefaultAngle, bitmap.getWidth() / 2,
					bitmap.getHeight() / 2);
			canvas.drawBitmap(bitmap, matrix, paint);
			if (!mIsClick && !mIsFallAnimation) {
				matrix.postTranslate(-bitmap.getWidth() / 2 - WIHP_SANTA_DIFF_X
						* mScale, WIHP_SANTA_DIFF_Y * mScale);
				canvas.drawBitmap(wbitmap, matrix, paint);
				matrix.reset();
			}
			matrix.postTranslate(
					bitmap.getWidth() - FLAG_SANTA_DIFF_X * mScale,
					bitmap.getHeight() / 2);
			// canvas.rotate(-mDefaultAngle, fbitmap.getWidth() / 2,
			// fbitmap.getHeight() / 2);
			canvas.drawBitmap(fbitmap, matrix, paint);
			matrix.reset();
			canvas.restore();
		}
	}

	@Override
	public void cleanUp() {
		recycleBitmap();
		if (mGiftList != null) {
			for (Gift gift : mGiftList) {
				gift.cleanUp();
			}
		}
		if (mRabbitList != null) {
			for (Rabbit rabiit : mRabbitList) {
				rabiit.cleanUp();
			}
		}
	}

	private void recycleBitmap() {
		recycle(mSantaBitmap);
		recycle(mWhipBitmap);
		recycle(mFlagBitmap);
		recycle(mFlagOppositeBitmap);
		recycle(mGiftBitmap);
		recycle(mWavingBitmap);
		recycle(mJumpingBitmap);
		recycle(mBlastBitmap);
		recycle(mPointBitmap);
		recycle(mPackageBitmap);
		recycle(mFallSantaBitmap);
		recycle(mRunSantaBitmap);
		recycle(mUpSantaBitmap);
	}

	@Override
	public boolean checkOutOfBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (isTaped(x, y) && !mIsFallAnimation) {
				mLastAnimateUpdateTime = System.currentTimeMillis();
				mIsClick = true;
				ret = true;
			}
			if (mRabbitList != null && mRabbitList.size() > 0) {
				for (Rabbit rabbit : mRabbitList) {
					if (rabbit.mAlive && !rabbit.mIsBlast) {
						ret = ret | rabbit.onTouchEvent(event);
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:

			break;
		default:
			break;
		}
		return ret;
	}

	public void setWatcher(IOutWather wather) {
		mIOutWather = wather;
	}

	public void createGift() {

		final Gift gilt = new Gift(mGiftList.size(),
				mGiftBitmap[mRandom.nextInt(2)], mX + mBitmap.getWidth() / 2,
				mY + mBitmap.getHeight(), GIFT_SPEED_X, GIFT_SPEED_Y,
				mScreenWidth, mScreenHeight);
		gilt.setReachWatcher(this);
		mGiftList.add(gilt);
	}

	@Override
	public void haveReach(int id, boolean reach) {
		Rabbit rabbit;
		final Gift gift = mGiftList.get(id);
		final int style = mRandom.nextInt(2);
		if (style == RABBIT_TYLE_WAVING) {
			rabbit = new Rabbit(gift.mX + mGiftBitmap[0].getWidth() / 2,
					gift.mY, mWavingBitmap, mBlastBitmap, RABBIT_TYLE_WAVING,
					mScreenWidth, mScreenHeight);
		} else {
			rabbit = new Rabbit(gift.mX + mGiftBitmap[0].getWidth() / 2,
					gift.mY, mJumpingBitmap, mBlastBitmap, RABBIT_TYLE_JUMPING,
					mScreenWidth, mScreenHeight);
		}
		gift.mAlive = false;

		if (mRabbitList.size() < MAX_COUNT_OF_RABBIT) {
			mRabbitList.add(rabbit);
		} else {
			mRabbitList.get(id - MAX_COUNT_OF_RABBIT).mIsBlast = true;
			mRabbitList.add(rabbit);
		}
	}

	public void onConfigurationChanged(int width, int height) {
		final float widthOld = mScreenWidth;
		final float heightOld = mScreenHeight;
		mScreenWidth = width;
		mScreenHeight = height;

		if (mIsFallAnimation) {
			mSX = (mSX / widthOld) * mScreenWidth;
			mSY = (mSY / heightOld) * mScreenHeight;
			mPX = (mPX / widthOld) * mScreenWidth;
			mPY = (mPY / heightOld) * mScreenHeight;
		}

		if (mGiftList != null && mGiftList.size() > 0) {

			for (Gift gift : mGiftList) {
				if (gift != null) {
					gift.onConfigurationChanged(width, height);
				}
			}
		}

		if (mRabbitList != null && mRabbitList.size() > 0) {

			for (Rabbit rabbit : mRabbitList) {
				if (rabbit != null) {
					rabbit.onConfigurationChanged(width, height);
				}
			}
		}

	}

	public void handShake(float speed) {
		if (mX < mScreenWidth - mSBitmap.getWidth() / 2
				&& mX > -mSBitmap.getWidth() / 2 && !mIsFallAnimation
				&& !mIsClick) {
			mIsFallAnimation = true;
			mLastFallingUpdateTime = System.currentTimeMillis();
			mBitmap = mSleighBitmap;
			mPBitmap = mPackageBitmap[0];
			mFallSpeed = 0;
			mSX = mX;
			mSY = mY;
			if (mSpeedX > 0) {
				mPX = mX;
				mPY = mY + mPBitmap.getHeight();
			} else {
				mPX = mX + mSBitmap.getWidth() / 2;
				mPY = mY + mPBitmap.getHeight();
			}
		}

	}

}
