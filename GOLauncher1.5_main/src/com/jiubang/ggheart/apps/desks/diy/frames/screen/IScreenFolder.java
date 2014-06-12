package com.jiubang.ggheart.apps.desks.diy.frames.screen;

/**
 * 文件夹操作接口类
 * 
 * @author luopeihuan
 * 
 */
public interface IScreenFolder {
	public static final int CLOSE_FOLDER = 0;
	public static final int LONG_PRESS_TITLE = 1;
	public static final int START_DRAG = 2;
	public static final int START_ACTIVITY = 3;

	/************** for live folder *****************/
	public static final int START_MANAGING_CURSOR = 4;
	public static final int STOP_MANAGING_CURSOR = 5;
	public static final int QUERY_CURSOR = 6;

	/************** for user folder *****************/
	public static final int ADD_ITEMS = 7;
}
