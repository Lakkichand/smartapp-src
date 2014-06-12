package com.jiubang.ggheart.appgame.base.component;

/**
 * 屏幕激活状态改变时的处理接口，从非激活状态到激活状态需要加载内存，从激活状态到非激活状态需要释放内存
 * 
 * 激活状态的定义：当前正在显示的屏幕以及它的左右两屏都属于激活状态，其他的属于非激活状态
 * 
 * @author xiedezhi
 * 
 */
public interface ScreenActiveHandler {

	/**
	 * 
	 * @param isActive
	 *            true代表当前屏从非激活状态转换成激活状态，false代表当前屏从激活状态转换成非激活状态
	 */
	public void onActiveChange(boolean isActive);

}
