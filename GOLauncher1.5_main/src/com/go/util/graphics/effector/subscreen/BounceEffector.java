package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

class BounceEffector extends MSubScreenEffector {
	float mRatio;

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		mNeedQuality = false;
		final float offset2 = offset * mRatio * (first ? 1 : -1);
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + offset, offset2);
		} else {
			canvas.translate(offset2, mScroll + offset);
		}
		return true;
	}

	@Override
	public void onSizeChanged() {
		super.onSizeChanged();
		if (mOrientation == ScreenScroller.HORIZONTAL) {
			mRatio = mHeight / (float) mWidth;
		} else {
			mRatio = mWidth / (float) mHeight;
		}
	}
}
