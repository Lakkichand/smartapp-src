package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

/**
 * 
 * @author jiangxuwen
 * 
 */
class StackEffector extends MSubScreenEffector {

	final static int Radius = 1;

	// float mScaleRatio;
	float mAlphaRatio;
	float mScaleMin = 0.65f;
	float mScaleMax = 1.0f;

	public StackEffector() {
		mCombineBackground = false;
		mReverse = true;
	}

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		mNeedQuality = false;
		float t;
		float s;
		float leftTop;

		if (first) {
			s = 0;
			leftTop = offset;
			mAlpha = 255;
		} else {
			leftTop = offset;
			// t = (float)Math.cos(offset * mScaleRatio);
			// s = (mScaleMax - mScaleMin) * t * t + mScaleMin;

			t = 1 - (float) Math.sin(offset * mAlphaRatio);
			s = (mScaleMax - mScaleMin) * t + mScaleMin;
			/**
			 * y = a - (x - b) * (x - b) 当 x = 1 时，y = 1; x = 0.1 时 y = 0;
			 */
			// if(t < 0.1f){
			// mAlpha = 0;
			// return true;
			// }
			// mAlpha = (int) (255 * (1.010f - (t - 1.105f) * (t - 1.105f)));
			mAlpha = (int) (255 * t);
		}

		if (mOrientation == ScreenScroller.HORIZONTAL) {
			if (first) {
				canvas.translate(mScroll + leftTop, 0);
			} else {
				canvas.translate(mScroll + (1 - s) * 0.5f * mWidth, (1 - s) * 0.5f * mHeight);
				canvas.scale(s, s);
			}
		} else {
			if (first) {
				canvas.translate(0, mScroll + leftTop);
			} else {
				canvas.translate((1 - s) * 0.5f * mWidth, mScroll + (1 - s) * 0.5f * mHeight);
				canvas.scale(s, s);
			}
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		// mScaleRatio = (float)Math.PI / (Radius * 2 + 1) / mScreenSize;
		mAlphaRatio = (float) Math.PI / (Radius * 2) / mScreenSize;
	}

}
