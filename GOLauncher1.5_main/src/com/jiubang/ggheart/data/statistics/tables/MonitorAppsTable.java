package com.jiubang.ggheart.data.statistics.tables;

/**
 * 监控安装程序表
 * 
 * @author huyong
 * 
 */
public class MonitorAppsTable {
	public static String TABLENAME = "monitorapps";
	public static String PKG_NAME = "pkgname"; // 监控安装的包名
	public static String ACTION_TIME = "actiontime"; // 开始监控的时刻
	public static String SRC_TYPE = "srctype"; // 监控来源的类型
	public static String SRC_KEYS = "srckey"; // 监控来源的key值

	public static String CREATE_TEABLE_SQL = "create table " + TABLENAME + "( " + PKG_NAME
			+ " text, " + ACTION_TIME + " numeric, " + SRC_TYPE + " numeric, " + SRC_KEYS
			+ " text " + ")";

}
