package com.jiubang.ggheart.data.tables;

public interface ScreenTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "screen";

	/**
	 * 表字段：SCREENID
	 */
	static public String SCREENID = "screenid";
	/**
	 * 表字段：MINDEX
	 */
	static public String MINDEX = "mindex";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table screen " + "(" + "screenid numeric, "
			+ "mindex numeric" + ")";
}
