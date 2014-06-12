package com.go.util.graphics.effector.gridscreen;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;

import com.go.util.scroller.ScreenScroller;

class CylinderEffector extends MGridScreenEffector {
	float mRatio;
	float mRadius;
	static final Camera CAMERA = new Camera();
	static final Matrix MATRIX = new Matrix();
	static final float THETA = 180;

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / w;
		mRadius = w / (float) Math.toRadians(THETA) * 2;
	}

	@Override
	public void onDrawScreen(Canvas canvas, int screen, int offset) {
		float t = offset * mRatio;
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		float globalAngle = THETA * t;
		canvas.translate(mCenterX - offset, mCenterY);
		final DrawFilter filter = canvas.getDrawFilter();
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		final float ratio = THETA / col;
		final float absT = Math.abs(t);
		float t2 = Math.min(Math.max(absT * col, mScroller.getCurrentDepth()), 1);
		CAMERA.save();
		CAMERA.translate(0, 0, mRadius);
		CAMERA.rotateY(globalAngle);
		int j = 0, jEnd = col, dj = 1, dx = cellWidth;
		if (t > 0) { // 为了实现深度排序
			j = col - 1;
			jEnd = -1;
			dj = -dj;
			dx = -dx;
			index += j;
		}
		for (int cellX = paddingLeft + cellWidth * j; j != jEnd; j += dj, index += dj, cellX += dx) {
			if (index >= end) {
				continue;
			}
			float localAngle = (j + 0.5f - col * 0.5f) * ratio;
			localAngle = interpolate(0, localAngle, t2); // 在平面和圆柱面间插值角度
			float angle = globalAngle + localAngle;
			if (angle < -180) {
				angle += 360;
			}
			if (angle >= 180) {
				angle -= 360;
			}
			if (angle < 0) {
				angle = -angle;
			}
			int alpha = 255;
			int fadeAngle = 75;
			if (angle > fadeAngle) {
				alpha = (int) interpolate(255, 64, (angle - fadeAngle) / (180 - fadeAngle));
			}
			if (Math.abs(t) * col > col - 1) {
				alpha *= (1 - Math.abs(t)) * col; // 在[1-1/col,
													// 1]及对称范围内插值alpha，避免刚触摸时左右移动的不连续
			}
			canvas.save();
			CAMERA.save();
			CAMERA.rotateY(localAngle);
			CAMERA.translate(0, 0, -mRadius);
			// CAMERA.applyToCanvas(canvas);
			CAMERA.getMatrix(MATRIX);
			CAMERA.restore();
			canvas.concat(MATRIX);
			// 在平面和圆柱面间插值位置
			canvas.translate(interpolate(cellX - mCenterX, -cellWidth * 0.5f, t2), -mCenterY
					+ paddingTop);
			for (int i = 0, cellY = paddingTop, index2 = index; i < row && index2 < end; ++i) {
				if (alpha == 255) {
					container.drawGridCell(canvas, index2);
				} else if (alpha > 0) {
					container.drawGridCell(canvas, index2, alpha);
				}
				canvas.translate(0, cellHeight);
				cellY += cellHeight;
				index2 += col;
			}
			canvas.restore();
		}
		CAMERA.restore();
		canvas.setDrawFilter(filter); // restore filter
	}

	@Override
	public void onAttach(GridScreenContainer container, ScreenScroller scroller) {
		super.onAttach(container, scroller);
		scroller.setDepthEnabled(true);
	}

	@Override
	public void onDetach() {
		mScroller.setDepthEnabled(false);
		super.onDetach();
	}

	@Override
	protected boolean isCurrentScreenOnTop() {
		return true; // 为了实现深度排序
	}
}
