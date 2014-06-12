package com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture;

/**
 * 
 * <br>类描述:多指操作监听
 * <br>功能详细描述:
 * 
 * @author  chenqiang
 * @date  [2012-10-16]
 */
public interface OnMultiTouchListener {
	public static final int DIRECTION_NONE = 0;  
	public static final int DIRECTION_LEFT = 1;  
	public static final int DIRECTION_RIGHT = 2;
	public static final int DIRECTION_UP = 3;
	public static final int DIRECTION_DOWN = 4;

	/**
	 * 手指滑动（通过PointInfo.getPointCount()获取触摸点数）
	 * 
	 * @param p
	 *            当前触摸点
	 * @param dx
	 *            x方向上的移动距离
	 * @param dy
	 *            y方向上的移动距离
	 * @param direction
	 *            滑动方向
	 * @return
	 */
	boolean onSwipe(MutilPointInfo p, float dx, float dy, int direction);

	/***
	 * 双指向内或向外缩放操作
	 * 
	 * @param p
	 *            当前触摸点
	 * @param scale
	 *            缩放比例
	 * @param angle
	 *            旋转角度
	 * @return
	 */
	boolean onScale(MutilPointInfo p, float scale, float angle);

	/**
	 * 双击屏幕
	 * 
	 * @param p
	 *            当前触摸点
	 * @return
	 */
	boolean onDoubleTap(MutilPointInfo p);
}
