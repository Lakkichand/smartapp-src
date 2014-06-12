package com.jiubang.ggheart.data.tables;

public interface GestureTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "gesture";

	/**
	 * 表字段: GESTUREID
	 */
	static public String GESTUREID = "gestureid";
	/**
	 * 表字段: GESTURENAME
	 */
	static public String GESTURENAME = "gesturename";
	/**
	 * 表字段: GESTURACTION
	 */
	static public String GESTURACTION = "gestureaction";
	/**
	 * 表字段: ACTION
	 */
	static public String ACTION = "action";

	/**
	 * 表字段：Intent
	 */
	static public String INTENT = "intent";

	/**
	 * 表字段：Intent
	 */
	static public String GOSHORTCUTITEM = "goshortcutitem";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table gesture " + "(" + "gestureid numeric, "
			+ "gesturename text, " + "gestureaction numeric, " + "action text, " + "intent text, "
			+ "goshortcutitem numeric" + ")";
}
