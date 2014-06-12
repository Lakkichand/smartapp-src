package com.go.util.graphics.effector.subscreen;

import android.graphics.Canvas;

import com.go.util.scroller.ScreenScroller;

class BulldozeEffector extends MSubScreenEffector {
	public BulldozeEffector() {
		super();
		mCombineBackground = false;
	}

	@Override
	protected boolean onDrawScreen(Canvas canvas, int screen, int offset, boolean first) {
		mNeedQuality = false;
		int min, max;

		if (first) {
			min = 0;
			max = offset + mScreenSize;
		} else {
			min = offset;
			max = mScreenSize;
		}

		if (mOrientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + min, 0);
			canvas.scale((float) (max - min) / mWidth, 1);
		} else {
			canvas.translate(0, mScroll + min);
			canvas.scale(1, (float) (max - min) / mHeight);
		}
		return true;
	}
}
