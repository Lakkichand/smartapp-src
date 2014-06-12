package com.jiubang.ggheart.data.tables;

public interface GravityTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "gravity";

	/**
	 * 表字段: EABLE
	 */
	static public String ENABLE = "enable";
	/**
	 * 表字段: LANDSCAPE
	 */
	static public String LANDSCAPE = "landscape";
	/**
	 * 表字段: Orientation type
	 */
	static public String ORIENTATIONTYPE = "orientation";
	/**
	 * 表语句
	 */
	// static public String CREATETABLESQL =
	// "create table gravity " +
	// "(" +
	// "enable numeric" +
	// ")";
	static public String CREATETABLESQL = "create table gravity " + "(" + "enable numeric ,"
			+ "landscape numeric, " + "orientation  numeric  " + ")";
	// static public String CREATETABLESQL =
	// "create table gravity " +
	// "(" +
	// "orientation  numeric  " +
	// ")";
}
