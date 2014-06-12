package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 可以发生位移变化的景物
 * 
 * @author jiangxuwen
 * 
 */
public class Translate extends BaseElement implements IMovable {
	boolean mMoving = true; // 是否正在运动
	public long mDuration; // 一个周期动画的时间
	public boolean mLoop; // 是否循环动画的标识
	public boolean mBackRotate; // 在一次位移后是否需要180旋转的标识
	public int mScreenWidth; // 屏幕宽度
	public int mScreenHeight; // 屏幕高度
	public float mSpeedX;
	public float mSpeedY;

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (!mAlive || mBitmap == null) {
			return;
		}
		canvas.save();
		canvas.translate(mX, mY);
		canvas.drawBitmap(mBitmap, 0, 0, paint);
		canvas.restore();
	}

	@Override
	public void moving() {
		if (mBackRotate) {
			if (mX > mScreenWidth || mX < -mBitmap.getWidth()) {
				mSpeedX = -mSpeedX;
				mSpeedY = -mSpeedY;
			}
		} else {
			if (mX > mScreenWidth) {
				mX = -mBitmap.getWidth();
			}
		}
		mX = mX + mSpeedX;
		mY = mY + mSpeedY;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	};

}
