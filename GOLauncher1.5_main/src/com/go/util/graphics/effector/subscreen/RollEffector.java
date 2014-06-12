package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

class RollEffector extends MSubScreenEffector {
	float mAngleRatio;
	float mDistanceRatio;

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		final float angle = offset * mAngleRatio;
		final float dist = offset * mDistanceRatio;
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + dist + mCenterX, mCenterY);
			canvas.rotate(angle);
			canvas.translate(-mCenterX, -mCenterY);
		} else {
			canvas.translate(mCenterX, mScroll + dist + mCenterY);
			canvas.rotate(angle);
			canvas.translate(-mCenterX, -mCenterY);
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mAngleRatio = 180.0f / mScreenSize;
		mDistanceRatio = (float) Math.hypot(mWidth, mHeight) / mScreenSize;
	}

}
