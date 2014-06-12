package com.jiubang.ggheart.data.statistics;

/**
 * 桌面设置的自适应后设置信息静态类
 * 
 * @author jiangxuwen
 * 
 */
public class StaticScreenSettingInfo {

	/**
	 * 是否开启常驻内存的标识
	 */
	public static boolean sIsPemanentMemory = false;

	/**
	 * 是否开启高质量绘图的标识
	 */
	public static boolean sHighQualityDrawing = false;

	/**
	 * 屏幕行数
	 */
	public static int sScreenRow = 4;

	/**
	 * 屏幕列数
	 */
	public static int sScreenCulumn = 4;

	/**
	 * 是否自适应的标识
	 */
	public static boolean sAutofit = false;

	/**
	 * 行列数样式的选择项
	 */
	public static int sColRowStyle = 1;

	// 一些其他的桌面设置可以在往后版本继续添加
	/**
	 * 低配是否减屏
	 */
	public static boolean sNeedDelScreen = false;
}
