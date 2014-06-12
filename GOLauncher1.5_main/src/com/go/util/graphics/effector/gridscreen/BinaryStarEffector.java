package com.go.util.graphics.effector.gridscreen;

import android.graphics.Canvas;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

class BinaryStarEffector extends MGridScreenEffector {
	float mRatio;

	static Interpolator INTERPOLATOR = new AccelerateInterpolator();

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 2.0f / w;
	}

	@Override
	public void onDrawScreen(Canvas canvas, int screen, int offset) {
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		float t = offset * mRatio;
		if (t < 0) {
			t = -t;
		}
		if (t > 1) {
			t = 1;
		}
		if (mScroller.isScrollAtEnd()) {
			t = INTERPOLATOR.getInterpolation(t);
		}
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final float x1 = mCenterX - cellWidth * 0.5f;
		final float y1 = mCenterY - cellHeight * 0.5f;
		float lastX = 0, lastY = 0;
		for (int i = 0, cellY = paddingTop; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index) {
				final float x = interpolate(cellX, x1, t);
				final float y = interpolate(cellY, y1, t);
				canvas.translate(x - lastX, y - lastY);
				container.drawGridCell(canvas, index);
				lastX = x;
				lastY = y;
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
	}

}
