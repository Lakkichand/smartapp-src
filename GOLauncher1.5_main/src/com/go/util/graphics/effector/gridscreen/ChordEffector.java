package com.go.util.graphics.effector.gridscreen;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;

class ChordEffector extends MGridScreenEffector {
	float mRatio;
	public boolean mRotateByCell = false; // 绕单元格本身的中心旋转
	static Camera CAMERA = new Camera();
	static Matrix MATRIX = new Matrix();

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / w;
	}

	@Override
	public void onDrawScreen(Canvas canvas, int screen, int offset) {
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final float centerX = cellWidth * 0.5f;
		final float centerY = (mRotateByCell ? cellHeight : container.getHeight()) * 0.5f;
		float t = offset * mRatio;
		if (mScroller.isScrollAtEnd()) {
			col = Math.min(col, end - index);
		} else {
			t *= col;
		}
		canvas.translate(-offset, 0);
		final DrawFilter filter = canvas.getDrawFilter();
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		for (int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index) {
			float angle = 0;
			if (t > 0) {
				angle = Math.max(0, Math.min(t - j, 1)) * 180;
			} else {
				angle = Math.max(-1, Math.min((col - 1 - j) + t, 0)) * 180;
			}
			if (Math.abs(angle) < 90) {
				final float depthZ = centerX * (float) Math.sin(Math.toRadians(Math.abs(angle)));
				CAMERA.save();
				if (mRotateByCell) { // 如果是绕屏幕高度中心旋转这样就不好看
					CAMERA.translate(0, 0, depthZ); // 保持最近的边在z=0处
				}
				CAMERA.rotateY(angle);
				CAMERA.getMatrix(MATRIX);
				CAMERA.restore();
				for (int i = 0, cellY = paddingTop, index2 = index; i < row && index2 < end; ++i) {
					canvas.save();
					final float cy = mRotateByCell ? centerY : centerY - cellY;
					canvas.translate(cellX + centerX, cellY + cy);
					canvas.concat(MATRIX);
					canvas.translate(-centerX, -cy);
					container.drawGridCell(canvas, index2);
					canvas.restore();
					cellY += cellHeight;
					index2 += col;
				}
			}
			cellX += cellWidth;
		}
		canvas.setDrawFilter(filter); // restore filter
	}
}
