package com.escape.uninstaller.ui.scroller;

/**
 * 循环滚屏绘制的接口
 * 
 * @author jiangxuwen
 * 
 */
public interface CycloidDrawListener {

	/**
	 * 返回当前位置的透明度
	 * @return
	 */
	int getCurrentAlpha();
	
	/**
	 * 返回壁纸开始截图的位置x
	 * @return
	 */
	int getBackgroundX(int scroll);
	
	/**
	 * 返回循环滚屏的滚动量
	 * @return
	 */
	int getCycloidScroll();
}
