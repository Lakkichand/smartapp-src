package com.jiubang.ggheart.apps.desks.diy.frames.preview;

/**
 * 卡片事件定义文件
 * 
 * @author yuankai
 * @version 1.0
 */
public interface ICardEvent {
	/**
	 * 房子按钮被点击
	 */
	public final static int HOME_CLICK = 1;

	/**
	 * 删除按钮被点击
	 */
	public final static int DEL_CLICK = 2;

	/**
	 * 预览图片被点击
	 */
	public final static int PREVIEW_CLICK = 3;

	/**
	 * 添加图片被点击
	 */
	public final static int ADD_CLICK = 4;

	/**
	 * 预览图片被长按
	 */
	public final static int PREVIEW_LONG_CLICK = 5;
	/**
	 * 卡片被Touch Down
	 */
	public final static int TOUCH_DOWN = 6;
}
