package com.go.util.graphics.effector.gridscreen;

import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

class ChariotEffector extends MGridScreenEffector {
	float mRadius;
	float mRatio;

	static Interpolator ACC_INTERPOLATOR = new AccelerateInterpolator(0.618f * 1.5f);
	static Interpolator DEC_INTERPOLATOR = new DecelerateInterpolator(0.618f * 1.5f);

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRadius = (Math.min(w, h) - mContainer.getCellHeight()) * 0.48f;
		mRatio = 2.05f / w; // 改成比2.0大可以让两个圈同时存在的时间段增大
	}

	@Override
	public void onDrawScreen(Canvas canvas, int screen, int offset) {
		final DrawFilter filter = canvas.getDrawFilter();
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);

		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int count = end - index;
		if (count <= 0) {
			return;
		}
		float t = offset * mRatio;
		final float dAngle = -360.f / count;
		final double dRad = Math.toRadians(dAngle);
		final float sin = (float) Math.sin(dRad);
		final float cos = (float) Math.cos(dRad);
		float iconDstX = mRadius, iconDstY = 0, iconDstAngle = -90;
		if (count == 1) {
			iconDstX = 0; // 只有一个的时候把它画在圆心处
			iconDstAngle = -360;
		}
		canvas.translate(mCenterX, mCenterY);
		final boolean isScrollAtEnd = mScroller.isScrollAtEnd();
		if (isScrollAtEnd) {
			t *= 1.5f; // 这样在两端的时候更容易组成圈
		}
		// 以下注释的代码是对cell做包围盒裁剪的，
		// 如果cell的内部结构比较简单，其实优化作用不明显
		float sinWheelAngle = 0, cosWheelAngle = 1;
		if (t > 1) {
			sinWheelAngle = (float) Math.sin((Math.PI * 0.5) * (t - 1));
			cosWheelAngle = (float) Math.cos((Math.PI * 0.5) * (t - 1));
			canvas.rotate(90 * (t - 1));
			t = 1;
		} else if (t < -1) {
			sinWheelAngle = (float) Math.sin((Math.PI * 0.5) * (t + 1));
			cosWheelAngle = (float) Math.cos((Math.PI * 0.5) * (t + 1));
			canvas.rotate(90 * (t + 1));
			t = 1;
		} else if (t < 0) {
			t = -t;
		}
		float t1 = t, t2 = t;
		if (isScrollAtEnd) {
			// 修改插值方式，使在两端恢复时动作更平滑
			t1 = ACC_INTERPOLATOR.getInterpolation(t);
			t2 = DEC_INTERPOLATOR.getInterpolation(t);
		}
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final int cellCenterX = cellWidth / 2;
		final int cellCenterY = cellHeight / 2;
		final int cellRadius = (int) (Math.hypot(cellWidth, cellHeight) * 0.5) + 2;
		// 计算物理屏幕在当前坐标系的左右边界值
		final float screenLeft = -mCenterX - offset;
		final float screenRight = mCenterX - offset;
		for (int i = 0, cellY = paddingTop + cellCenterY; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft + cellCenterX; j < col && index < end; ++j, ++index) {
				final float x = interpolate(cellX - mCenterX, iconDstX, t1);
				final float y = interpolate(cellY - mCenterY, iconDstY, t1);
				final float x2 = x * cosWheelAngle - y * sinWheelAngle;
				if (x2 - cellRadius < screenRight && x2 + cellRadius >= screenLeft) {
					final float a = interpolate(0, iconDstAngle, t2);
					canvas.save();
					canvas.translate(x, y);
					canvas.rotate(a);
					canvas.translate(-cellCenterX, -cellCenterY);
					container.drawGridCell(canvas, index);
					canvas.restore();
				}
				final float xBak = iconDstX;
				iconDstX = cos * xBak - sin * iconDstY;
				iconDstY = sin * xBak + cos * iconDstY;
				iconDstAngle += dAngle;
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
		canvas.setDrawFilter(filter); // restore filter
	}

}
