package com.go.util.graphics.effector.gridscreen;

import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;

class SphereEffector extends CylinderEffector {
	float mRotateX;
	static final Matrix MATRIX = new Matrix();

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
		float yOffset = mScroller.getTouchDeltaY() / (float) mScroller.getScreenHeight();
		float rotateX = Math.max(-1, Math.min(yOffset * 2.1f, 1)) * -90;
		mRotateX = interpolate(mRotateX, rotateX, t2);
		CAMERA.rotateX(mRotateX * t2);
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
			localAngle = interpolate(0, localAngle, t2); // 在平面和球面间插值角度
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
			int i = 0, iEnd = row, di = 1, dy = cellHeight, index2 = index;
			if (mRotateX * t2 < 0) {
				i = row - 1;
				iEnd = -1;
				di = -di;
				dy = -dy;
				index2 += i * col;
			}
			for (int cellY = paddingTop + cellHeight * i; i != iEnd; i += di, index2 += di * col, cellY += dy) {
				if (index2 >= end) {
					continue;
				}
				canvas.save();
				CAMERA.save();
				final float angle2 = row > 1 ? (i + 0.5f - row * 0.5f) * -90 / (row - 1) : 0;
				CAMERA.rotateX(interpolate(0, angle2, t2));
				CAMERA.translate(0, 0, -mRadius);
				// CAMERA.applyToCanvas(canvas);
				CAMERA.getMatrix(MATRIX);
				canvas.concat(MATRIX);
				// 在平面和球面间插值位置
				canvas.translate(interpolate(cellX - mCenterX, -cellWidth * 0.5f, t2),
						interpolate(cellY - mCenterY, -cellHeight * 0.5f, t2));
				if (alpha == 255) {
					container.drawGridCell(canvas, index2);
				} else if (alpha > 0) {
					container.drawGridCell(canvas, index2, alpha);
				}
				CAMERA.restore();
				canvas.restore();
			}
			CAMERA.restore();
			canvas.restore();
		}
		CAMERA.restore();
		canvas.setDrawFilter(filter); // restore filter
	}
}
