package com.jiubang.ggheart.data.tables;

public interface ScreenSettingTable {
	/**
	 * 表名
	 */
	static final public String TABLENAME = "screensetting";

	/**
	 * 表字段: ISAUTOHIDE
	 */
	static final public String ISAUTOHIDE = "isautohide";
	/**
	 * 表字段: ENABLE
	 */
	static final public String ENABLE = "enable";
	/**
	 * 表字段: COUNT
	 */
	static final public String COUNT = "count";
	/**
	 * 表字段: MAINSCREEN
	 */
	static final public String MAINSCREEN = "mainscreen";

	static final public String WALLPAPER_SCROLL = "wallpaperscroll";

	static final public String SCREEN_LOOPING = "screenlooping";

	/**
	 * 表字段：锁屏 numeric类型：0为不锁屏，1为锁屏
	 */
	static final public String LOCK_SCREEN = "lockscreen";

	/**
	 * 表字段：页面指示器类型 numeric类型：0为正常模式，1为数字模式
	 */
	@Deprecated
	static final public String INDICATOR_SHOWMODE = "indicatorshowmode";

	/**
	 * 表字段：页面指示器类型
	 * 
	 */
	static public String INDICATORSTYLEPACKAGE = "indicatorstylepackage";

	/**
	 * 表字段：页面指示器位置 top/bottom 类型：top屏幕顶部/ bottom屏幕底部
	 */
	static public String INDICATORPOSITION = "indicatorposition";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table screensetting " + "("
			+ "isautohide numeric, " + "enable numeric, " + "count numeric, "
			+ "mainscreen numeric, " + "wallpaperscroll numeric, " + "screenlooping numeric, "
			+ "lockscreen numeric," + "indicatorshowmode numeric, "
			+ "indicatorstylepackage text, " + "indicatorposition text " + ")";
}
