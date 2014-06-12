package com.jiubang.ggheart.data.tables;

// 统计表
public interface StatisticsTable {
	static public String TABLENAME = "statistics";
	static String KEY = "key";
	static String VALUE = "value";
	public static String CREATETABLESQL = "create table statistics " + "(" + "key numeric, "
			+ "value numeric" + ")";
}
