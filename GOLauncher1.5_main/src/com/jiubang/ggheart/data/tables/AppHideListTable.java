package com.jiubang.ggheart.data.tables;

/**
 * 应用程序隐藏名单表
 * 
 * @author guodanyang
 * 
 */
public class AppHideListTable {
	static public String TABLENAME = "apphidelist";
	static public String INTENT = "intent";
	static public String ISHIDE = "ishide";

	static public String CREATETABLESQL = "create table " + TABLENAME + " ( " + INTENT + " text, "
			+ ISHIDE + " numeric " + " ) ";
}
