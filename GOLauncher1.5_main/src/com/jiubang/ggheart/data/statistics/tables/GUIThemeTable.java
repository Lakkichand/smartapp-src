package com.jiubang.ggheart.data.statistics.tables;

/**
 * GUI收费数据统计表
 * 
 * @author yangbing
 * */
public class GUIThemeTable {
	/**
	 * 表名
	 */
	public final static String TABLENAME = "guiTheme";
	/**
	 * 类型
	 */
	public final static String THEME_TYPE = "themetype";
	/**
	 * 包名
	 */
	public final static String PACKAGE = "package";
	/**
	 * 位置
	 */
	public final static String POSITION = "position";
	/**
	 * 点击数
	 */
	public final static String CLICK_COUNT = "clickcount";
	/**
	 * 安装数
	 */
	public final static String INSTALL_COUNT = "installcount";
	/**
	 * 入口
	 */
	public final static String ENTRY = "entry";
	/**
	 * 包类型
	 */
	public final static String PKG_TYPE = "pkgtype";
	/**
	 * 详情点击量
	 */
	public final static String DETAIL_CLICK = "detail_click";
	/**
	 * 详情获取点击量
	 */
	public final static String DETAIL_GET_CLICK = "detail_get_click";

	/**
	 * 表语句
	 */
	public final static String CREATE_TABLE_SQL = "create table " + TABLENAME + "(" + THEME_TYPE
			+ " numeric, " + PACKAGE + " text, " + POSITION + " numeric, " + CLICK_COUNT
			+ " numeric, " + INSTALL_COUNT + " numeric, " + ENTRY + " numeric, " + PKG_TYPE
			+ " text, " + DETAIL_CLICK + " numeric, " + DETAIL_GET_CLICK + " numeric " + ")";
}