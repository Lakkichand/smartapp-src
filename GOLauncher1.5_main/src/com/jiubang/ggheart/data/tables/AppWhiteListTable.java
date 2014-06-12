package com.jiubang.ggheart.data.tables;

/**
 * 应用程序白名单表
 * 
 * @author huyong
 * 
 */
public class AppWhiteListTable {
	static public String TABLENAME = "appwhitelist";

	static public String INTENT = "intent";

	static public String ISIGNORE = "isignore";

	// static public String ISAUTOKILL = "isautokill";
	//
	// static public String AUTOKILLTIME = "autokilltime";

	static public String CREATETABLESQL = "create table " + TABLENAME + " ( " + INTENT + " text, "
			+ ISIGNORE + " numeric " +
			/*
			 * ISAUTOKILL + " numeric, " + AUTOKILLTIME + " text" +
			 */
			" ) ";
}
