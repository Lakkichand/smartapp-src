package com.escape.uninstaller.ui.scroller;

import android.graphics.Canvas;

/**
 * 分屏滚动器的特效器
 * 
 * @author dengweiming
 * 
 */
public interface ScreenScrollerEffector {
	/**
	 * 设置具体某种特效的类型
	 * 
	 * @param type
	 */
	public void setType(int type);

	/**
	 * 使用随机选择特效的时候，选取下一个特效
	 */
	public void updateRandomEffect();

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
	 * @param container
	 */
	public void onAttach(ScreenScrollerListener container);

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
	 * 获取期望的最大过冲比例[0, 50)
	 * 
	 * @return 0表示不使用过冲插值器（切屏时没有回弹的效果）
	 */
	public int getMaxOvershootPercent();

	/**
	 * 设置绘图质量
	 * 
	 * @param quality
	 */
	public void setDrawQuality(int quality);

	/**
	 * 关闭时的处理
	 */
	public void recycle();

	/**
	 * 设置屏幕之前的间隙
	 * 
	 * @param gap
	 */
	public void setScreenGap(int gap);

	/**
	 * 设置上边距
	 * 
	 * @param top
	 */
	public void setTopPadding(int top);
}