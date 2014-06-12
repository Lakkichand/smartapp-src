package com.jiubang.ggheart.data.tables;

public interface RecentAppTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "recentapp";

	/**
	 * 表字段：PARTID
	 */
	static public String PARTID = "partid";
	/**
	 * 表字段：MINDEX
	 */
	static public String INDEX = "mindex";

	static public String INTENT = "intent";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table recentapp " + "(" + "partid numeric, "
			+ "mindex numeric, " + "intent text" + ")";
}
