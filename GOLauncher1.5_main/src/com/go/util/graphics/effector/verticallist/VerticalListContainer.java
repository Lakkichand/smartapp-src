package com.go.util.graphics.effector.verticallist;

import android.graphics.Canvas;

public interface VerticalListContainer {
	public static final int PART_UP = 0;
	public static final int PART_MID = 1;
	public static final int PART_DOWN = 2;

	/**
	 * 绘制
	 * 
	 * @param canvas
	 *            画布
	 * @param clipTop
	 *            裁剪区域的上边界
	 * @param clipBottom
	 *            裁剪区域的下边界
	 */
	public void onDraw(Canvas canvas, int clipTop, int clipBottom, int part);
}
