package com.jiubang.ggheart.apps.desks.diy.frames.animation;

import android.graphics.Canvas;

public interface Transition {

	/**
	 * 计算动画参数
	 * 
	 * @param t
	 *            插值后的时间，范围为[0, 1]
	 */
	public void onAnimate(float t, BinaryView view);

	public void onDraw(Canvas canvas, BinaryView view);
}
