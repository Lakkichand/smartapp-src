package com.jiubang.ggheart.data.tables;

public interface DesktopTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "desktop";

	/**
	 * 表字段: BACKGROUNDPATTEMSWITCH
	 */
	static public String BACKGROUNDPATTEMSWITCH = "backgroundpattemswitch";
	/**
	 * 表字段: BACKGROUNDPATTEM
	 */
	static public String BACKGROUNDPATTEM = "backgroundpattem";
	/**
	 * 表字段: BACKGROUNDPATTEMID
	 */
	static public String BACKGROUNDPATTEMID = "backgroundpattemid";
	/**
	 * 表字段: BACKGROUNDPATTEMTYPE
	 */
	static public String BACKGROUNDPATTEMTYPE = "backgroundpattemtype";
	/**
	 * 表字段: AUTOSTATUSBAR
	 */
	static public String AUTOSTATUSBAR = "autostatusbar";// 桌面状态栏
	/**
	 * 表字段: HIDETITLE
	 */
	static public String SHOWTITLE = "showtitle";// 桌面程序名
	/**
	 * 表字段: ROW
	 */
	static public String ROW = "gridrow";
	/**
	 * 表字段: COLUMN
	 */
	static public String COLUMN = "gridcolumn";
	/**
	 * 表字段: STYLE
	 */
	static public String STYLE = "style";

	/**
	 * 表字段: THEMEICONSTYLE
	 */
	static public String THEMEICONSTYLE = "themeiconstyle";
	@Deprecated
	static public String THEMEICONPACKAGE = "themeiconspackage";

	/**
	 * 表字段: AUTOFITITEMS
	 */
	static public String AUTOFITITEMS = "autofititems";// 桌面部件和图标自适应
	/**
	 * 表字段: TITLESTYLE
	 */
	static public String TITLESTYLE = "titlestyle";// 名字样式

	static public String CUSTOMAPPBG = "customappbg";// 自定桌面图标背景
	static public String PRESSCOLOR = "presscolor";
	static public String FOCUSCOLOR = "focuscolor";

	static public String LARGEICON = "largeicon";

	static public String FOLDERTHEMEICONPACKAGE = "folderthemeiconspackage";
	static public String GGMENUTHEMEICONPACKAGE = "ggmenuthemeiconspackage";

	static public String ICONSIZE = "iconsize";
	static public String SHOWICONBASE = "showiconbase";
	static public String FONTSIZE = "fontsize";
	static public String CUSTOMTITLECOLOR = "customtitlecolor";
	static public String TITLECOLOR = "titlecolor";
	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table desktop " + "("
			+ "backgroundpattemswitch numeric, " + "backgroundpattem text, "
			+ "backgroundpattemid numeric, " + "backgroundpattemtype numeric, "
			+ "autostatusbar numeric, " + "showtitle numeric, " + "gridrow numeric, "
			+ "gridcolumn numeric, " + "style numeric, " + "themeiconstyle numeric, "
			+ "themeiconspackage text, " + "autofititems numeric, " + "titlestyle numeric, "
			+ "customappbg numeric, " + "presscolor numeric, " + "focuscolor numeric, "
			+ "largeicon numeric, " + "folderthemeiconspackage text, "
			+ "ggmenuthemeiconspackage text, " + "iconsize numeric, " + "showiconbase numeric, "
			+ "fontsize numeric, " + "customtitlecolor numeric, " + "titlecolor numeric" + ")";
}
