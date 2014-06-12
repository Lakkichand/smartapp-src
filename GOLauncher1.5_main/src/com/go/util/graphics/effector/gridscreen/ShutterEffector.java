package com.go.util.graphics.effector.gridscreen;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;

class ShutterEffector extends MGridScreenEffector {
	float mRatio;
	public boolean mRotateByCell = false; // 绕单元格本身的中心旋转
	static Camera CAMERA = new Camera();
	static final Matrix MATRIX = new Matrix();

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / w;
	}

	@Override
	public void onDrawScreen(Canvas canvas, int screen, int offset) {
		final float angle = offset * mRatio * 180;
		if (Math.abs(angle) > 90) {
			return;
		}
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		final float centerX = cellWidth * 0.5f;
		final float centerY = (mRotateByCell ? cellHeight : container.getHeight()) * 0.5f;
		final float depthZ = centerX * (float) Math.sin(Math.toRadians(Math.abs(angle)));
		CAMERA.save();
		CAMERA.translate(0, 0, depthZ); // 保持最近的边在z=0处
		CAMERA.rotateY(angle);
		canvas.translate(-offset, 0);
		final DrawFilter filter = canvas.getDrawFilter();
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		for (int i = 0, cellY = paddingTop; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index) {
				canvas.save();
				final float cy = mRotateByCell ? centerY : centerY - cellY;
				canvas.translate(cellX + centerX, cellY + cy);
				// CAMERA.applyToCanvas(canvas);
				CAMERA.getMatrix(MATRIX);
				canvas.concat(MATRIX);
				canvas.translate(-centerX, -cy);
				container.drawGridCell(canvas, index);
				canvas.restore();
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
		canvas.setDrawFilter(filter); // restore filter
		CAMERA.restore();
	}
}
