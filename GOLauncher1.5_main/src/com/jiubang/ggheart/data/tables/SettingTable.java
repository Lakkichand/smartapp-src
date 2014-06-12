package com.jiubang.ggheart.data.tables;

import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

/**
 * 
 *
 */
public class SettingTable {

	public static final String TABLENAME = "settingtable";

	// 打开app的动画类型 0：没有动画 / 1.2.3.4.
	public static String APPOPENTYPE = "appopentype";
	// 是否为第一次运行GO桌面
	public static String FIRSTRUN = "firstrun";
	// 是否更新过常驻内存
	public static String UPGRADEPEMANENTMEMORY = "upgradepemanentmemory";
	/**
	 * 建表语句
	 */
	static public String CREATETABLESQL = "create table " + TABLENAME + "(" 
						+ APPOPENTYPE + " numeric, " 
						+ FIRSTRUN + " numeric, " 
						+ UPGRADEPEMANENTMEMORY + " numeric" 
						+ ")";
	
	public static String INITTABLESQL =  "INSERT INTO " + TABLENAME + " values("
			+ GoLauncher.TYPE_OPEN_APP_NONE + ", " + 1 + ", " + 0 + ")";

}
