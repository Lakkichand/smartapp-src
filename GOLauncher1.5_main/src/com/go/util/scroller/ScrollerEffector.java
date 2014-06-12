package com.go.util.scroller;

import android.graphics.Canvas;

public interface ScrollerEffector {
	/**
	 * 绘制视图
	 * 
	 * @param canvas
	 * @return
	 */
	public boolean onDraw(Canvas canvas);

	/**
	 * 被加载时的响应
	 * 
	 * @param scroller
	 * @param container
	 */
	public void onAttach(Scroller scroller, ScrollerListener container);

	/**
	 * 被卸载时的响应
	 */
	public void onDetach();

	/**
	 * 视图大小或者滚动方向变化时的响应
	 * 
	 * @param w
	 * @param h
	 * @param orientation
	 */
	public void onSizeChanged(int w, int h, int orientation);

	/**
	 * 设置绘图质量
	 * 
	 * @param quality
	 */
	public void setDrawQuality(int quality);
}
