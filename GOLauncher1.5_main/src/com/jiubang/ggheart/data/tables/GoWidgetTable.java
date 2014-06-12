package com.jiubang.ggheart.data.tables;

import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;

public final class GoWidgetTable {
	public static String TABLENAME = "gowidget";
	public static String WIDGETID = "widgetid";
	public static String TYPE = "type";
	public static String LAYOUT = "layout";
	public static String PACKAGE = "package";
	public static String CLASSNAME = "classname";
	public static String THEME = "theme";
	public static String THEMEID = "themeid";

	/**
	 * 内置原型字段， 非内置此值默认为{@link GoWidgetBaseInfo#PROTOTYPE_NORMAL}
	 */
	public static String PROTOTYPE = "prototype";

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + WIDGETID
			+ " numeric, " + TYPE + " numeric, " + LAYOUT + " text, " + PACKAGE + " text, "
			+ CLASSNAME + " text, " + THEME + " text, " + THEMEID + " numeric," + PROTOTYPE
			+ " numeric" + ")";
}
