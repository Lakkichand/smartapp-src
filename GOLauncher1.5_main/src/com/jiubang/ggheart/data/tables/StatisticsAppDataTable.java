package com.jiubang.ggheart.data.tables;

public class StatisticsAppDataTable {

	public static String TABLENAME = "statisticsapp";

	public static String PKGNAME = "pkgname";

	public static String CLICKCNT = "clickcnt";

	public static String VERCODE = "vercode";

	public static String VERNAME = "vername";

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + PKGNAME + " text, "
			+ CLICKCNT + " numeric, " + VERCODE + " numeric, " + VERNAME + " text" + ")";

}
