package com.jiubang.ggheart.data.tables;

public interface PartsTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "parts";

	/**
	 * 表字段：ID
	 */
	static public String ID = "id";
	/**
	 * 表字段：TITLE
	 */
	static public String TITLE = "title";
	/**
	 * 表字段：INTENT
	 */
	static public String INTENT = "intent";
	/**
	 * 表字段：ITEMTYPE
	 */
	static public String ITEMTYPE = "itemtype";
	/**
	 * 表字段：WIDGETID
	 */
	static public String WIDGETID = "widgetid";
	/**
	 * 表字段：ICONPACKAGE
	 */
	static public String ICONPACKAGE = "iconpackage";
	/**
	 * 表字段：ICONRESOURCE
	 */
	static public String ICONRESOURCE = "iconresource";
	/**
	 * 表字段: ICON
	 */
	static public String ICON = "icon";
	/**
	 * 表字段: URI
	 */
	static public String URI = "uri";
	/**
	 * 表字段：DISPLAYMODE
	 */
	static public String DISPLAYMODE = "dispalymode";

	/**
	 * 表字段：是否内部响应
	 */
	static public String INNERACTION = "inneraction";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table parts " + "(" + "id numeric, "
			+ "title text, " + "intent text, " + "itemtype numeric, " + "widgetid numeric, "
			+ "iconpackage text, " + "iconresource text, " + "icon blob, " + "uri text, "
			+ "dispalymode numeric, " + "inneraction numeric, " + "PRIMARY KEY (id)" + ")";
}
