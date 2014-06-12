package com.jiubang.ggheart.data.tables;
/**
 * 通讯统计应用程序名单表
 * 
 * @author wuziyi
 *
 */
public class NotificationAppSettingTable {

	static public String TABLENAME = "notificationapplist";
	static public String INTENT = "intent";

	//以后某监听应用的包中，含有多个icon时，可做显示开关判断。
	static public String ISSELECTED = "isselected";

	static public String CREATETABLESQL = "create table " + TABLENAME + " ( " + INTENT + " text, "
			+ ISSELECTED + " numeric " + " ) ";
}
