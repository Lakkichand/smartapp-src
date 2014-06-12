package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 礼物
 * 
 * @author chenqiang
 * 
 */
public class Gift extends BaseElement implements IMovable {

	private final static float GIFT_FALL_LIMIT = 0.85f;
	private int mSpeedX; // X方向的速度
	private int mSpeedY; // Y方向的速度
	private int mScreenWidth;
	private int mScreenHeight;
	private int mId;

	private IReachAreaWatcher mIReachAreaWatcher;

	public Gift(int id, Bitmap bitmap, float x, float y, int speedX,
			int speedY, int sreenWidth, int sreenHeight) {
		mBitmap = bitmap;
		mX = x;
		mY = y;
		mSpeedX = speedX;
		mSpeedY = speedY;
		mId = id;
		mScreenWidth = sreenWidth;
		mScreenHeight = sreenHeight;
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mBitmap != null) {
			canvas.save();
			canvas.translate(mX, mY);
			canvas.drawBitmap(mBitmap, matrix, paint);
			canvas.restore();
		}

	}

	@Override
	public void moving() {
		mX += mSpeedX;
		mY += mSpeedY;
		if (mY >= mScreenHeight * GIFT_FALL_LIMIT) {
			mIReachAreaWatcher.haveReach(mId, true);
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

	public void setReachWatcher(IReachAreaWatcher reachAreaWatcher) {
		mIReachAreaWatcher = reachAreaWatcher;
	}

	public void onConfigurationChanged(int width, int height) {

		final float widthOld = mScreenWidth;
		final float heightOld = mScreenHeight;
		if (mScreenWidth != width || mScreenHeight != height) {
			mScreenWidth = width;
			mScreenHeight = height;
		}
		mX = (mX / widthOld) * mScreenWidth;
		mY = (mY / heightOld) * mScreenHeight;
	}

}
