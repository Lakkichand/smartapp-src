package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

class WaveEffector extends MSubScreenEffector {

	final static int Radius = 1;

	float mRatio;
	float mScaleMin = 0.2f;
	float mScaleMax = 1.0f;

	public WaveEffector() {
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		mNeedQuality = false;
		float t = (float) Math.cos(offset * mRatio);
		float s = (mScaleMax - mScaleMin) * t * t + mScaleMin;
		float leftTop;
		if (first) {
			leftTop = offset + mScreenSize * (1 - s);
		} else {
			leftTop = offset;
		}

		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + leftTop, (1 - s) * 0.5f * mHeight);
		} else {
			canvas.translate((1 - s) * 0.5f * mWidth, mScroll + leftTop);
		}
		canvas.scale(s, s);
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		mRatio = (float) Math.PI / (Radius * 2 + 1) / mScreenSize;
	}

}
