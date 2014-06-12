package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

class WindmillEffector extends MSubScreenEffector {
	float mAngleRatio; // Angle / ScreenSize;
	float mOffsetAngle;

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		float angle;
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			angle = (float) Math.toDegrees(Math.asin(mCenterX / mHeight)) * 2;
			if (mCenterX <= mHeight && angle <= 90) {
				mCenterY = -(float) Math.sqrt(mHeight * mHeight - mCenterX * mCenterX);
			} else {
				angle = 90;
				mCenterY = -mCenterX;
			}
		} else {
			angle = (float) Math.toDegrees(Math.asin(mCenterY / mWidth)) * 2;
			if (mCenterY <= mWidth && angle <= 90) {
				mCenterX = mWidth + (float) Math.sqrt(mWidth * mWidth - mCenterY * mCenterY);
			} else {
				angle = 90;
				mCenterX = mWidth + mCenterY;
			}
		}
		mAngleRatio = -angle / mScreenSize;
	}

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		mOffsetAngle = offset * mAngleRatio;

		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + mCenterX, mCenterY);
		} else {
			canvas.translate(mCenterX, mScroll + mCenterY);
		}
		canvas.rotate(mOffsetAngle);
		canvas.translate(-mCenterX, -mCenterY);
		return true;
	}
}
